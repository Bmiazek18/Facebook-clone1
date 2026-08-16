import { generateECDH, exportPubKey, exportPrivKey } from './keys'
import { IdentityKeyPair } from './identity'

export interface PreKeyRecord {
  keyId: number
  keyPair: CryptoKeyPair
  pubBytes: Uint8Array
  privBytes: Uint8Array
}

export interface SignedPreKeyRecord extends PreKeyRecord {
  signature: Uint8Array
}

export class PreKeys {
  static async generatePreKey(keyId: number): Promise<PreKeyRecord> {
    const pair = await generateECDH()
    const pub = await exportPubKey(pair.publicKey)
    const priv = await exportPrivKey(pair.privateKey)
    return { keyId, keyPair: pair, pubBytes: pub, privBytes: priv }
  }

  static async generateSignedPreKey(identity: IdentityKeyPair, keyId: number): Promise<SignedPreKeyRecord> {
    const base = await this.generatePreKey(keyId)
    const signature = await identity.sign(base.pubBytes)
    return { ...base, signature }
  }
}
