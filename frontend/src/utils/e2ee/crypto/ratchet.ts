import { hkdf, hmacSha256 } from './hkdf'
import { generateECDH, exportPubKey, importECDHPub, ecdh } from './keys'
import { base64ToBytes, bufferToBase64 } from './serializer'

export async function kdfRK(rk: Uint8Array, dhOut: Uint8Array): Promise<[Uint8Array, Uint8Array]> {
  const derived = await hkdf(dhOut, rk, 'KDF_RK', 64)
  return [derived.slice(0, 32), derived.slice(32, 64)]
}

export async function kdfCK(ck: Uint8Array): Promise<[Uint8Array, Uint8Array]> {
  const mk = await hmacSha256(ck, new Uint8Array([0x01]))
  const nextCk = await hmacSha256(ck, new Uint8Array([0x02]))
  return [nextCk, mk]
}

export async function encryptAES_GCM(mk: Uint8Array, plaintext: Uint8Array): Promise<Uint8Array> {
  const encKey = await hkdf(mk, new Uint8Array(32), 'AES', 32)
  const iv = await hkdf(mk, new Uint8Array(32), 'IV', 12)
  const key = await window.crypto.subtle.importKey('raw', encKey, { name: 'AES-GCM' }, false, ['encrypt'])
  const ct = await window.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, plaintext)
  return new Uint8Array(ct)
}

export async function decryptAES_GCM(mk: Uint8Array, ciphertext: Uint8Array): Promise<Uint8Array> {
  const decKey = await hkdf(mk, new Uint8Array(32), 'AES', 32)
  const iv = await hkdf(mk, new Uint8Array(32), 'IV', 12)
  const key = await window.crypto.subtle.importKey('raw', decKey, { name: 'AES-GCM' }, false, ['decrypt'])
  const pt = await window.crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ciphertext)
  return new Uint8Array(pt)
}
