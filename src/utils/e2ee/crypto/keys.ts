export async function generateECDH(): Promise<CryptoKeyPair> {
  return window.crypto.subtle.generateKey(
    { name: 'ECDH', namedCurve: 'P-256' }, true, ['deriveBits']
  )
}

export async function generateECDSA(): Promise<CryptoKeyPair> {
  return window.crypto.subtle.generateKey(
    { name: 'ECDSA', namedCurve: 'P-256' }, true, ['sign', 'verify']
  )
}

export async function exportPubKey(key: CryptoKey): Promise<Uint8Array> {
  const buf = await window.crypto.subtle.exportKey('raw', key)
  return new Uint8Array(buf)
}

export async function exportPrivKey(key: CryptoKey): Promise<Uint8Array> {
  const buf = await window.crypto.subtle.exportKey('pkcs8', key)
  return new Uint8Array(buf)
}

export async function importECDHPub(bytes: Uint8Array): Promise<CryptoKey> {
  return window.crypto.subtle.importKey(
    'raw', bytes, { name: 'ECDH', namedCurve: 'P-256' }, true, []
  )
}

export async function importECDSAPub(bytes: Uint8Array): Promise<CryptoKey> {
  return window.crypto.subtle.importKey(
    'raw', bytes, { name: 'ECDSA', namedCurve: 'P-256' }, true, ['verify']
  )
}

export async function ecdh(privateKey: CryptoKey, publicKey: CryptoKey): Promise<Uint8Array> {
  const secret = await window.crypto.subtle.deriveBits(
    { name: 'ECDH', public: publicKey }, privateKey, 256
  )
  return new Uint8Array(secret)
}
