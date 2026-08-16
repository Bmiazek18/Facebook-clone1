import { signalStore } from './signalStore.client'
import { IdentityKeyPair } from './crypto/identity'
import { PreKeys } from './crypto/prekeys'
import { ProtocolAddress } from './crypto/session'
import { processPreKeyBundle } from './crypto/x3dh'
import { kdfCK, kdfRK, encryptAES_GCM, decryptAES_GCM } from './crypto/ratchet'
import { serializeMessage, deserializeMessage } from './crypto/message'
import { bufferToBase64, base64ToBytes, concatBytes } from './crypto/serializer'
import { generateVerificationHash } from './crypto/verification'
import { generateECDH, exportPubKey, exportPrivKey, importECDHPub, ecdh } from './crypto/keys'
import { hkdf } from './crypto/hkdf'

const ENVELOPE_PREFIX = 'e2ee:4:'
const DEVICE_ID_KEY = 'my_device_id'
const USER_ID_KEY = 'my_user_id'
const REG_FLAG_PREFIX = 'signal_prekeys_reg_'

export interface EncryptedPayload {
  targetUserId: string
  targetDeviceId: number
  type: number
  body: string
}

interface EnvelopeV4 {
  v: 4
  senderDeviceId: number
  type: number
  body: string
  targetDeviceId: number
  senderCopy?: string
}

interface DeviceBundleResponse {
  exists: boolean
  deviceId: number
  bundle: any
}

function getApiBase(): string { return import.meta.env.VITE_BFF_API_URL || '' }
function getAuthHeaders(): Record<string, string> {
  const headers: Record<string, string> = {}
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('keycloak-token') : null
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

function resolveCurrentUserId(): string {
  if (typeof localStorage === 'undefined') return ''
  const raw = localStorage.getItem(USER_ID_KEY) || localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || ''
  const clean = String(raw).replace(/^user_/, '')
  if (clean) localStorage.setItem(USER_ID_KEY, clean)
  return clean
}

function getMyDeviceId(): number {
  if (typeof localStorage === 'undefined') return 1
  const stored = Number(localStorage.getItem(DEVICE_ID_KEY) || '1')
  if (!Number.isFinite(stored) || stored < 1 || stored > 127) {
    localStorage.setItem(DEVICE_ID_KEY, '1')
    return 1
  }
  return stored
}

async function deriveSenderCopyKey(): Promise<CryptoKey> {
  const identityData = await signalStore.getIdentityKeyPair()
  if (!identityData) throw new Error('Brak klucza tożsamości')
  const material = await window.crypto.subtle.digest('SHA-256', identityData.privBytes)
  return window.crypto.subtle.importKey('raw', material, { name: 'AES-GCM', length: 256 }, false, ['encrypt', 'decrypt'])
}

async function sealSenderCopy(plaintext: string): Promise<string> {
  const key = await deriveSenderCopyKey()
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const ciphertext = await window.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, new TextEncoder().encode(plaintext))
  return `${bufferToBase64(iv)}.${bufferToBase64(ciphertext)}`
}

async function openSenderCopy(sealed: string): Promise<string> {
  const [ivB64, cipherB64] = sealed.split('.')
  if (!ivB64 || !cipherB64) throw new Error('Uszkodzona kopia nadawcy')
  const key = await deriveSenderCopyKey()
  const plain = await window.crypto.subtle.decrypt({ name: 'AES-GCM', iv: base64ToBytes(ivB64) }, key, base64ToBytes(cipherB64))
  return new TextDecoder().decode(plain)
}

async function deriveGroupFallbackKey(chatId: string): Promise<CryptoKey> {
  const seed = new TextEncoder().encode(`fb-clone-e2ee-group-v1-${chatId}`)
  const hash = await window.crypto.subtle.digest('SHA-256', seed)
  return window.crypto.subtle.importKey('raw', hash, { name: 'AES-GCM', length: 256 }, false, ['encrypt', 'decrypt'])
}

async function encryptGroupFallback(content: string, chatId: string): Promise<string> {
  const key = await deriveGroupFallbackKey(chatId)
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const ciphertext = await window.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, new TextEncoder().encode(content))
  return `e2ee:1:${bufferToBase64(iv)}:${bufferToBase64(ciphertext)}`
}

async function decryptGroupFallback(encryptedContent: string, chatId: string): Promise<string> {
  const parts = encryptedContent.split(':')
  if (parts.length !== 4 || !parts[2] || !parts[3]) return encryptedContent
  const key = await deriveGroupFallbackKey(chatId)
  const plain = await window.crypto.subtle.decrypt({ name: 'AES-GCM', iv: base64ToBytes(parts[2]) }, key, base64ToBytes(parts[3]))
  return new TextDecoder().decode(plain)
}

async function uploadPrekeyBundle(bundle: Record<string, unknown>): Promise<void> {
  const userId = resolveCurrentUserId()
  if (!userId) throw new Error('Brak userId do rejestracji prekeys')

  const formData = new URLSearchParams()
  formData.append('userId', userId)
  formData.append('bundleJson', JSON.stringify(bundle))

  const response = await fetch(`${getApiBase()}/api/chat/e2ee/prekeys/register`, {
    method: 'POST',
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData
  })
  if (!response.ok) throw new Error(`Rejestracja prekeys nieudana: ${response.status}`)
  localStorage.setItem(`${REG_FLAG_PREFIX}${userId}`, 'true')
}

export async function fetchPrekeyBundleFromServer(userId: string, consume: boolean = false): Promise<DeviceBundleResponse | null> {
  const cleanId = String(userId).replace(/^user_/, '')
  try {
    const response = await fetch(`${getApiBase()}/api/chat/e2ee/prekeys/fetch?userId=${encodeURIComponent(cleanId)}&consume=${consume}`, { headers: getAuthHeaders() })
    if (!response.ok) return null
    const data = await response.json()
    if (!data?.exists || !data?.bundle) return null
    return data as DeviceBundleResponse
  } catch (err) { return null }
}

export async function registerNewDevice() {
  const identityKeyPair = await IdentityKeyPair.generate()
  const registrationId = Math.floor(Math.random() * 16380) + 1

  await signalStore.saveOwnIdentity(identityKeyPair, registrationId)

  const signedKeyId = 1
  const signedPreKey = await PreKeys.generateSignedPreKey(identityKeyPair, signedKeyId)
  await signalStore.saveSignedPreKey(signedKeyId, signedPreKey)

  const kyberKeyId = 1
  const kyberPair = await generateECDH()
  const kyberPub = await exportPubKey(kyberPair.publicKey)
  const kyberSig = await identityKeyPair.sign(kyberPub)

  const preKeysForServer = []
  for (let i = 0; i < 50; i++) {
    const preKey = await PreKeys.generatePreKey(i + 1)
    await signalStore.savePreKey(i + 1, preKey)
    preKeysForServer.push({ keyId: preKey.keyId, publicKey: bufferToBase64(preKey.pubBytes) })
  }

  const deviceId = 1
  localStorage.setItem(DEVICE_ID_KEY, String(deviceId))
  resolveCurrentUserId()

  return {
    registrationId,
    deviceId,
    identityKey: bufferToBase64(identityKeyPair.pubBytes),
    signedPreKey: { keyId: signedKeyId, publicKey: bufferToBase64(signedPreKey.pubBytes), signature: bufferToBase64(signedPreKey.signature) },
    kyberPreKey: { keyId: kyberKeyId, publicKey: bufferToBase64(kyberPub), signature: bufferToBase64(kyberSig) },
    preKeys: preKeysForServer
  }
}

let initPromise: Promise<{ publicKeyB64: string }> | null = null

export async function initIdentityKeys(): Promise<{ publicKeyB64: string }> {
  if (typeof window === 'undefined') return { publicKeyB64: '' }
  if (initPromise) return initPromise

  initPromise = (async () => {
    try {
      const userId = resolveCurrentUserId()
      const existing = await signalStore.getIdentityKeyPair()
      const regFlag = userId ? localStorage.getItem(`${REG_FLAG_PREFIX}${userId}`) : null

      if (existing && regFlag) return { publicKeyB64: bufferToBase64(existing.pubBytes) }

      if (existing && !regFlag) {
        try {
          const registrationId = await signalStore.getLocalRegistrationId()
          const signedPreKey = await signalStore.getSignedPreKey(1)
          const preKeys = []
          for (let i = 1; i <= 50; i++) {
            const pk = await signalStore.getPreKey(i)
            if (pk) preKeys.push({ keyId: i, publicKey: bufferToBase64(pk.pubBytes) })
          }
          await uploadPrekeyBundle({
            registrationId, deviceId: getMyDeviceId(), identityKey: bufferToBase64(existing.pubBytes),
            signedPreKey: { keyId: signedPreKey.keyId, publicKey: bufferToBase64(signedPreKey.pubBytes), signature: bufferToBase64(signedPreKey.signature) },
            kyberPreKey: { keyId: 1, publicKey: bufferToBase64(signedPreKey.pubBytes), signature: bufferToBase64(signedPreKey.signature) },
            preKeys
          })
        } catch (err) {}
        return { publicKeyB64: bufferToBase64(existing.pubBytes) }
      }

      const bundle = await registerNewDevice()
      await uploadPrekeyBundle(bundle)
      return { publicKeyB64: bundle.identityKey }
    } catch (err) {
      initPromise = null
      throw err
    }
  })()

  return initPromise
}

export async function hasLocalPrivateKey(): Promise<boolean> { return signalStore.hasIdentityKeyPair() }
export async function deleteLocalPrivateKey(): Promise<void> {
  const userId = resolveCurrentUserId()
  initPromise = null
  await signalStore.clearAll()
  localStorage.removeItem(DEVICE_ID_KEY)
  if (userId) localStorage.removeItem(`${REG_FLAG_PREFIX}${userId}`)
}

export async function encryptFanoutMessage(content: string, recipientUserId: string, recipientDevices: any[]): Promise<EncryptedPayload[]> {
  const cleanRecipientId = String(recipientUserId).replace(/^user_/, '')
  const plaintext = new TextEncoder().encode(content)
  const allPayloads: EncryptedPayload[] = []

  for (const device of recipientDevices) {
    const address = ProtocolAddress.new(cleanRecipientId, device.deviceId)
    let session = await signalStore.getSession(address)
    let type = 1

    if (!session && device.bundle) {
      const myIdentity = await signalStore.getIdentityKeyPair()
      const myIdentityPrivKey = await window.crypto.subtle.importKey(
        'pkcs8', myIdentity.privBytes.slice(1, 1 + myIdentity.privBytes[0]), { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits']
      )

      const { rootKey, dhSendKeyPair, dhReceivePub } = await processPreKeyBundle(myIdentityPrivKey, device.bundle)
      session = {
        rootKey: bufferToBase64(rootKey), chainKeySend: bufferToBase64(rootKey), chainKeyReceive: '',
        messageNumberSend: 0, messageNumberReceive: 0, previousChainLength: 0,
        dhSendPriv: bufferToBase64(await exportPrivKey(dhSendKeyPair.privateKey)),
        dhSendPub: bufferToBase64(await exportPubKey(dhSendKeyPair.publicKey)),
        dhReceivePub: bufferToBase64(dhReceivePub)
      }
      type = 3
    }

    if (!session) continue

    if (!session.chainKeySend) {
      const dhRemotePub = await importECDHPub(base64ToBytes(session.dhReceivePub))
      const newKeyPair = await generateECDH()
      session.dhSendPriv = bufferToBase64(await exportPrivKey(newKeyPair.privateKey))
      session.dhSendPub = bufferToBase64(await exportPubKey(newKeyPair.publicKey))

      const dhOut = await ecdh(newKeyPair.privateKey, dhRemotePub)
      const [rk, ckS] = await kdfRK(base64ToBytes(session.rootKey), dhOut)
      session.rootKey = bufferToBase64(rk)
      session.chainKeySend = bufferToBase64(ckS)
    }

    const [nextCk, mk] = await kdfCK(base64ToBytes(session.chainKeySend))
    session.chainKeySend = bufferToBase64(nextCk)

    const ciphertext = await encryptAES_GCM(mk, plaintext)
    const preKeyId = type === 3 ? device.bundle?.preKey?.keyId : undefined
    const msgBytes = serializeMessage(session.messageNumberSend, session.previousChainLength, base64ToBytes(session.dhSendPub), ciphertext, preKeyId)
    session.messageNumberSend++

    await signalStore.saveSession(address, session)

    allPayloads.push({
      targetUserId: cleanRecipientId, targetDeviceId: device.deviceId, type, body: bufferToBase64(msgBytes)
    })
  }
  return allPayloads
}

export const decryptionCache = new Map<string, string>()

export async function decryptSignalMessage(encryptedPayload: EncryptedPayload & { senderDeviceId: number }, senderUserId: string): Promise<string> {
  const cacheKey = `${encryptedPayload.senderDeviceId}:${encryptedPayload.body}`
  if (decryptionCache.has(cacheKey)) return decryptionCache.get(cacheKey)!

  const cleanId = String(senderUserId).replace(/^user_/, '')
  const address = ProtocolAddress.new(cleanId, encryptedPayload.senderDeviceId)
  let session = await signalStore.getSession(address)

  const msg = deserializeMessage(base64ToBytes(encryptedPayload.body))
  const dhRemotePubBytes = base64ToBytes(msg.dhPub)
  const ctBytes = base64ToBytes(msg.ct)

  if (!session || encryptedPayload.type === 3) {
    if (encryptedPayload.type === 3) {
      const senderBundle = await fetchPrekeyBundleFromServer(cleanId, false)
      if (!senderBundle?.bundle?.identityKey) throw new Error('Brak klucza tożsamości nadawcy')
      
      const remoteIdentityPub = await importECDHPub(base64ToBytes(senderBundle.bundle.identityKey).slice(0, 65))
      const remoteEphemeralPub = await importECDHPub(dhRemotePubBytes)

      const myIdentity = await signalStore.getIdentityKeyPair()
      const myIdentityPrivKey = await window.crypto.subtle.importKey(
        'pkcs8', myIdentity.privBytes.slice(1, 1 + myIdentity.privBytes[0]), { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits']
      )

      const mySignedPreKey = await signalStore.getSignedPreKey(1)
      const mySignedPreKeyPriv = await window.crypto.subtle.importKey(
        'pkcs8', mySignedPreKey.privBytes, { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits']
      )

      const dh1 = await ecdh(mySignedPreKeyPriv, remoteIdentityPub)
      const dh2 = await ecdh(myIdentityPrivKey, remoteEphemeralPub)
      const dh3 = await ecdh(mySignedPreKeyPriv, remoteEphemeralPub)

      let dh4 = new Uint8Array(0)
      if (msg.preKeyId) {
        try {
          const myOneTimePreKey = await signalStore.getPreKey(msg.preKeyId)
          const myOneTimePreKeyPriv = await window.crypto.subtle.importKey(
            'pkcs8', myOneTimePreKey.privBytes, { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits']
          )
          dh4 = await ecdh(myOneTimePreKeyPriv, remoteEphemeralPub)
        } catch (e) {
          console.warn('[decryptSignalMessage] Failed to load one-time prekey with ID:', msg.preKeyId, e)
        }
      }

      const masterSecret = concatBytes(dh1, dh2, dh3, dh4)
      const rootKey = await hkdf(masterSecret, new Uint8Array(32), 'X3DH', 32)

      const bytesToHex = (bytes: Uint8Array) => Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')
      console.warn('[X3DH-RECEIVER-DEBUG]', {
        remoteIdentityECDHPub: bytesToHex(base64ToBytes(senderBundle.bundle.identityKey).slice(0, 5)),
        mySignedPreKeyPub: bytesToHex(mySignedPreKey.pubBytes.slice(0, 5)),
        myOneTimePreKeyPub: msg.preKeyId ? bytesToHex((await signalStore.getPreKey(msg.preKeyId)).pubBytes.slice(0, 5)) : 'none',
        ephemeralPub: bytesToHex(dhRemotePubBytes.slice(0, 5)),
        dh1: bytesToHex(dh1.slice(0, 4)),
        dh2: bytesToHex(dh2.slice(0, 4)),
        dh3: bytesToHex(dh3.slice(0, 4)),
        dh4: bytesToHex(dh4.slice(0, 4)),
        masterSecret: bytesToHex(masterSecret.slice(0, 4)),
        rootKey: bytesToHex(rootKey.slice(0, 4))
      })

      session = {
        rootKey: bufferToBase64(rootKey),
        chainKeySend: '',
        chainKeyReceive: bufferToBase64(rootKey),
        messageNumberSend: 0,
        messageNumberReceive: 0,
        previousChainLength: 0,
        dhSendPriv: bufferToBase64(mySignedPreKey.privBytes),
        dhSendPub: bufferToBase64(mySignedPreKey.pubBytes),
        dhReceivePub: msg.dhPub
      }
    } else {
      throw new Error('Brak sesji dla wiadomości')
    }
  }

  if (msg.dhPub !== session.dhReceivePub) {
    session.previousChainLength = session.messageNumberSend
    session.messageNumberSend = 0
    session.dhReceivePub = msg.dhPub

    const dhRemotePub = await importECDHPub(dhRemotePubBytes)
    const dhLocalPriv = await window.crypto.subtle.importKey('pkcs8', base64ToBytes(session.dhSendPriv), { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits'])
    const dhOut = await ecdh(dhLocalPriv, dhRemotePub)

    const [rk, ckR] = await kdfRK(base64ToBytes(session.rootKey), dhOut)
    session.rootKey = bufferToBase64(rk)
    session.chainKeyReceive = bufferToBase64(ckR)

    const newKeyPair = await generateECDH()
    session.dhSendPriv = bufferToBase64(await exportPrivKey(newKeyPair.privateKey))
    session.dhSendPub = bufferToBase64(await exportPubKey(newKeyPair.publicKey))

    const dhOutSend = await ecdh(newKeyPair.privateKey, dhRemotePub)
    const [rk2, ckS] = await kdfRK(rk, dhOutSend)
    session.rootKey = bufferToBase64(rk2)
    session.chainKeySend = bufferToBase64(ckS)
  }

  const [nextCk, mk] = await kdfCK(base64ToBytes(session.chainKeyReceive))
  session.chainKeyReceive = bufferToBase64(nextCk)
  session.messageNumberReceive++

  const plaintextBytes = await decryptAES_GCM(mk, ctBytes)
  await signalStore.saveSession(address, session)

  const plaintext = new TextDecoder().decode(plaintextBytes)
  decryptionCache.set(cacheKey, plaintext)
  return plaintext
}

async function fetchRemoteSenderKey(groupId: string, userId: string): Promise<string | null> {
  const cleanUser = String(userId).replace(/^user_/, '')
  const cleanGroup = String(groupId).replace(/^group_/, '')
  try {
    const response = await fetch(`${getApiBase()}/api/chat/e2ee/sender-key/fetch?groupId=${encodeURIComponent(cleanGroup)}&userId=${encodeURIComponent(cleanUser)}`, {
      headers: getAuthHeaders()
    })
    const data = await response.json()
    if (data && data.exists) {
      return data.senderKeyCard
    }
    return null
  } catch (err) {
    console.error('[fetchRemoteSenderKey] Failed:', err)
    return null
  }
}

async function decryptGroupSenderKeyMessage(encryptedContent: string, groupId: string): Promise<string> {
  const parts = encryptedContent.split(':')
  if (parts.length < 6 || parts[0] !== 'e2ee' || parts[1] !== 'group' || parts[2] !== '1') {
    return encryptedContent
  }
  const senderUserId = String(parts[3]).replace(/^user_/, '')
  const ivB64 = parts[4]
  const cipherB64 = parts[5]
  const cleanGroup = String(groupId).replace(/^group_/, '')
  
  let keyB64 = await signalStore.getCustomValue<string>(`sender_key_${cleanGroup}_${senderUserId}`)
  if (!keyB64) {
    keyB64 = await fetchRemoteSenderKey(cleanGroup, senderUserId)
    if (keyB64) {
      await signalStore.saveCustomValue(`sender_key_${cleanGroup}_${senderUserId}`, keyB64)
    }
  }
  
  if (!keyB64) {
    // Fallback: Spróbuj odszyfrować kluczem fallback grupy
    try {
      return await decryptGroupFallback(encryptedContent, cleanGroup)
    } catch {
      throw new Error('Klucz nadawcy niedostępny')
    }
  }
  
  const rawKey = base64ToBytes(keyB64)
  const cryptoKey = await window.crypto.subtle.importKey('raw', rawKey, { name: 'AES-GCM', length: 256 }, false, ['decrypt'])
  const plain = await window.crypto.subtle.decrypt({ name: 'AES-GCM', iv: base64ToBytes(ivB64) }, cryptoKey, base64ToBytes(cipherB64))
  return new TextDecoder().decode(plain)
}

async function encryptGroupSenderKeyMessage(content: string, groupId: string): Promise<string> {
  const rawUser = resolveCurrentUserId() || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce'
  const myUserId = String(rawUser).replace(/^user_/, '')
  const cleanGroup = String(groupId).replace(/^group_/, '')

  let keyB64 = await signalStore.getCustomValue<string>(`sender_key_${cleanGroup}_${myUserId}`)
  let cryptoKey: CryptoKey
  
  if (!keyB64) {
    const rawKey = window.crypto.getRandomValues(new Uint8Array(32))
    keyB64 = bufferToBase64(rawKey)
    await signalStore.saveCustomValue(`sender_key_${cleanGroup}_${myUserId}`, keyB64)
    
    try {
      await fetch(`${getApiBase()}/api/chat/e2ee/sender-key/register`, {
        method: 'POST',
        headers: { ...getAuthHeaders(), 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          groupId: cleanGroup,
          userId: myUserId,
          senderKeyCard: keyB64
        })
      })
    } catch (e) {
      console.warn('[encryptGroupSenderKeyMessage] Failed to register sender key with backend:', e)
    }
    
    cryptoKey = await window.crypto.subtle.importKey('raw', rawKey, { name: 'AES-GCM', length: 256 }, false, ['encrypt'])
  } else {
    const rawKey = base64ToBytes(keyB64)
    cryptoKey = await window.crypto.subtle.importKey('raw', rawKey, { name: 'AES-GCM', length: 256 }, false, ['encrypt'])
  }
  
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const ciphertext = await window.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, cryptoKey, new TextEncoder().encode(content))
  
  return `e2ee:group:1:${myUserId}:${bufferToBase64(iv)}:${bufferToBase64(ciphertext)}`
}

export async function encryptMessage(content: string, chatId: string, isPrivate: boolean = true): Promise<string> {
  if (!content || typeof window === 'undefined') return content
  try {
    await initIdentityKeys()
    if (!isPrivate) return encryptGroupSenderKeyMessage(content, String(chatId).replace(/^user_/, ''))

    const cleanId = String(chatId).replace(/^user_/, '')
    const existingSession = await signalStore.getSession(ProtocolAddress.new(cleanId, 1))

    let device: { deviceId: number; bundle: any }
    if (existingSession) device = { deviceId: 1, bundle: null }
    else {
      const remote = await fetchPrekeyBundleFromServer(cleanId, true)
      if (!remote?.bundle) return content
      device = { deviceId: remote.deviceId || 1, bundle: remote.bundle }
    }

    const payloads = await encryptFanoutMessage(content, cleanId, [device])
    if (!payloads[0]) {
      console.warn('[encryptMessage] No payloads generated for E2EE')
      return content
    }

    const envelope: EnvelopeV4 = {
      v: 4, senderDeviceId: getMyDeviceId(), type: payloads[0].type,
      body: payloads[0].body, targetDeviceId: payloads[0].targetDeviceId,
      senderCopy: await sealSenderCopy(content)
    }
    return `${ENVELOPE_PREFIX}${JSON.stringify(envelope)}`
  } catch (err) {
    console.error('[encryptMessage] E2EE Encryption failed, falling back to plaintext:', err)
    return content
  }
}

async function getCachedDecrypted(encryptedContent: string): Promise<string | null> {
  if (decryptionCache.has(encryptedContent)) return decryptionCache.get(encryptedContent)!
  try {
    const hashBuffer = await window.crypto.subtle.digest('SHA-256', new TextEncoder().encode(encryptedContent))
    const hashHex = Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, '0')).join('')
    const stored = await signalStore.getCustomValue<string>(`msg_dec_${hashHex}`)
    if (stored) {
      decryptionCache.set(encryptedContent, stored)
      return stored
    }
  } catch {}
  return null
}

async function setCachedDecrypted(encryptedContent: string, plain: string): Promise<void> {
  decryptionCache.set(encryptedContent, plain)
  try {
    const hashBuffer = await window.crypto.subtle.digest('SHA-256', new TextEncoder().encode(encryptedContent))
    const hashHex = Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, '0')).join('')
    await signalStore.saveCustomValue(`msg_dec_${hashHex}`, plain)
  } catch {}
}

export async function decryptMessage(encryptedContent: string, chatId: string, isPrivate: boolean = true): Promise<string> {
  if (!encryptedContent || typeof window === 'undefined') return encryptedContent
  const cached = await getCachedDecrypted(encryptedContent)
  if (cached) return cached

  try {
    if (encryptedContent.startsWith(ENVELOPE_PREFIX)) {
      await initIdentityKeys()
      const envelope = JSON.parse(encryptedContent.slice(ENVELOPE_PREFIX.length)) as EnvelopeV4
      const otherUserId = String(chatId).replace(/^user_/, '')
      const myUserId = resolveCurrentUserId()

      // 1. Jeśli to nasza własna wiadomość (jesteśmy nadawcą), otwórz kopię nadawcy
      if (envelope.senderCopy) {
        try {
          const plain = await openSenderCopy(envelope.senderCopy)
          await setCachedDecrypted(encryptedContent, plain)
          return plain
        } catch {
          // Nie jesteśmy nadawcą tej wiadomości, przechodzimy do deszyfracji jako odbiorca
        }
      }

      // 2. Jesteśmy odbiorcą: deszyfrujemy protokołem Signal (Double Ratchet)
      try {
        const plaintext = await decryptSignalMessage({ targetUserId: myUserId, targetDeviceId: envelope.targetDeviceId, type: envelope.type, body: envelope.body, senderDeviceId: envelope.senderDeviceId }, otherUserId)
        if (plaintext.startsWith('SYSTEM_ACTION:BACKUP_SENDER_KEY:')) {
          const parts = plaintext.split(':')
          const gId = parts[2]
          const sKey = parts[3]
          if (gId && sKey) {
            await signalStore.saveCustomValue(`sender_key_${gId}_${otherUserId}`, sKey)
          }
        }
        await setCachedDecrypted(encryptedContent, plaintext)
        return plaintext
      } catch (signalErr) {
        console.warn('[decryptMessage] decryptSignalMessage failed:', signalErr)
        return '🔒 [Błąd deszyfrowania]'
      }
    }

    if (encryptedContent.startsWith('e2ee:group:1:')) {
      const plain = await decryptGroupSenderKeyMessage(encryptedContent, String(chatId).replace(/^user_/, ''))
      await setCachedDecrypted(encryptedContent, plain)
      return plain
    }

    if (encryptedContent.startsWith('e2ee:1:') && !isPrivate) {
      const plain = await decryptGroupFallback(encryptedContent, String(chatId).replace(/^user_/, ''))
      await setCachedDecrypted(encryptedContent, plain)
      return plain
    }
    return encryptedContent.startsWith('e2ee:') ? '🔒 [Zaszyfrowana wiadomość]' : encryptedContent
  } catch (err) {
    console.error('[decryptMessage] E2EE Decryption failed:', err)
    return '🔒 [Błąd deszyfrowania]'
  }
}

export async function backupChatHistoryToVault(exportKey: string, chatHistory: string): Promise<string> {
  const exportKeyBytes = new TextEncoder().encode(exportKey)
  const keyHash = await window.crypto.subtle.digest('SHA-256', exportKeyBytes)
  const cryptoKey = await window.crypto.subtle.importKey('raw', keyHash, { name: 'AES-GCM', length: 256 }, false, ['encrypt'])
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const ciphertext = await window.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, cryptoKey, new TextEncoder().encode(chatHistory))
  return `vault:v1:${bufferToBase64(iv)}:${bufferToBase64(ciphertext)}`
}

export async function restoreChatHistoryFromVault(exportKey: string, encryptedHistory: string): Promise<string> {
  if (!encryptedHistory.startsWith('vault:v1:')) throw new Error('Nieprawidłowy format')
  const parts = encryptedHistory.split(':')
  const iv = base64ToBytes(parts[2]), ciphertext = base64ToBytes(parts[3])
  const keyHash = await window.crypto.subtle.digest('SHA-256', new TextEncoder().encode(exportKey))
  const cryptoKey = await window.crypto.subtle.importKey('raw', keyHash, { name: 'AES-GCM', length: 256 }, false, ['decrypt'])
  const decryptedBytes = await window.crypto.subtle.decrypt({ name: 'AES-GCM', iv }, cryptoKey, ciphertext)
  return new TextDecoder().decode(decryptedBytes)
}

export async function getVerificationSessionKeys(chatId: string) {
  try {
    const myId = await signalStore.getIdentityKeyPair()
    const otherRemote = await fetchPrekeyBundleFromServer(String(chatId).replace(/^user_/, ''))
    if (!myId || !otherRemote?.bundle?.identityKey) {
      console.warn('[getVerificationSessionKeys] Missing local identity key or remote prekey bundle for chatId:', chatId, { hasMyId: !!myId, hasOtherRemoteBundle: !!otherRemote?.bundle })
      return null
    }

    return {
      conversationCode: await generateVerificationHash(myId.pubBytes, base64ToBytes(otherRemote.bundle.identityKey)),
      myDeviceKey: '', otherDeviceKey: ''
    }
  } catch (err) {
    console.error('[getVerificationSessionKeys] Error computing verification keys:', err)
    return null
  }
}
