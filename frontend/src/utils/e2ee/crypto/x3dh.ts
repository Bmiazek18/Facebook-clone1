import { ecdh, importECDHPub, exportPubKey } from './keys'
import { hkdf } from './hkdf'
import { concatBytes, base64ToBytes } from './serializer'

function bytesToHex(bytes: Uint8Array): string {
  return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')
}

export async function processPreKeyBundle(
  myIdentityPriv: CryptoKey,
  bundle: any
): Promise<{ rootKey: Uint8Array, dhSendKeyPair: CryptoKeyPair, dhReceivePub: Uint8Array }> {
  const remoteIdentity = await importECDHPub(base64ToBytes(bundle.identityKey).slice(0, 65))
  const remoteSignedPreKey = await importECDHPub(base64ToBytes(bundle.signedPreKey.publicKey))
  const remoteOneTimePreKey = bundle.preKey ? await importECDHPub(base64ToBytes(bundle.preKey.publicKey)) : null

  const ephemeralPair = await window.crypto.subtle.generateKey({ name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits'])

  const dh1 = await ecdh(myIdentityPriv, remoteSignedPreKey)
  const dh2 = await ecdh(ephemeralPair.privateKey, remoteIdentity)
  const dh3 = await ecdh(ephemeralPair.privateKey, remoteSignedPreKey)

  let dh4 = new Uint8Array(0)
  if (remoteOneTimePreKey) dh4 = await ecdh(ephemeralPair.privateKey, remoteOneTimePreKey)

  const masterSecret = concatBytes(dh1, dh2, dh3, dh4)
  const rootKey = await hkdf(masterSecret, new Uint8Array(32), 'X3DH', 32)

  const ephPubBytes = await exportPubKey(ephemeralPair.publicKey)

  console.warn('[X3DH-SENDER-DEBUG]', {
    remoteIdentityECDHPub: bytesToHex(base64ToBytes(bundle.identityKey).slice(0, 5)),
    remoteSignedPreKeyPub: bytesToHex(base64ToBytes(bundle.signedPreKey.publicKey).slice(0, 5)),
    remoteOneTimePreKeyPub: remoteOneTimePreKey ? bytesToHex(base64ToBytes(bundle.preKey.publicKey).slice(0, 5)) : 'none',
    ephemeralPub: bytesToHex(ephPubBytes.slice(0, 5)),
    dh1: bytesToHex(dh1.slice(0, 4)),
    dh2: bytesToHex(dh2.slice(0, 4)),
    dh3: bytesToHex(dh3.slice(0, 4)),
    dh4: bytesToHex(dh4.slice(0, 4)),
    masterSecret: bytesToHex(masterSecret.slice(0, 4)),
    rootKey: bytesToHex(rootKey.slice(0, 4))
  })

  return { rootKey, dhSendKeyPair: ephemeralPair, dhReceivePub: base64ToBytes(bundle.signedPreKey.publicKey) }
}
