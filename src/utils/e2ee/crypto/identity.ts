import { generateECDH, generateECDSA, exportPubKey, exportPrivKey, importECDSAPub } from './keys'
import { concatBytes } from './serializer'

export class IdentityKeyPair {
  constructor(
    public ecdh: CryptoKeyPair,
    public ecdsa: CryptoKeyPair,
    public pubBytes: Uint8Array,
    public privBytes: Uint8Array
  ) {}

  static async generate(): Promise<IdentityKeyPair> {
    const ecdhPair = await generateECDH()
    const ecdsaPair = await generateECDSA()

    const ecdhPub = await exportPubKey(ecdhPair.publicKey)
    const ecdsaPub = await exportPubKey(ecdsaPair.publicKey)
    const pubBytes = concatBytes(ecdhPub, ecdsaPub) // 130 bytes total

    const ecdhPriv = await exportPrivKey(ecdhPair.privateKey)
    const ecdsaPriv = await exportPrivKey(ecdsaPair.privateKey)
    const privLen = new Uint8Array([ecdhPriv.length])
    const privBytes = concatBytes(privLen, ecdhPriv, ecdsaPriv)

    return new IdentityKeyPair(ecdhPair, ecdsaPair, pubBytes, privBytes)
  }

  async sign(data: Uint8Array): Promise<Uint8Array> {
    const sig = await window.crypto.subtle.sign(
      { name: 'ECDSA', hash: 'SHA-256' }, this.ecdsa.privateKey, data
    )
    return new Uint8Array(sig)
  }

  static async verify(pubBytes: Uint8Array, data: Uint8Array, signature: Uint8Array): Promise<boolean> {
    const ecdsaPubBytes = pubBytes.slice(65, 130)
    const key = await importECDSAPub(ecdsaPubBytes)
    return window.crypto.subtle.verify({ name: 'ECDSA', hash: 'SHA-256' }, key, signature, data)
  }
}
