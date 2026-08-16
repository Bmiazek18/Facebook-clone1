import { openDB, type IDBPDatabase } from 'idb'
import { ProtocolAddress, type SessionState as SessionRecord } from './crypto/session'
import type { IdentityKeyPair } from './crypto/identity'
import type { PreKeyRecord, SignedPreKeyRecord } from './crypto/prekeys'

const DB_NAME = 'e2ee_signal_store'
const DB_VERSION = 1
const STORE_NAME = 'key_value_store'

// Odtwarzamy brakujące Enumy wcześniej importowane z libsignal
export enum IdentityChange {
  NewOrUnchanged = 0,
  ReplacedExisting = 1
}

export enum Direction {
  Sending = 0,
  Receiving = 1
}

// Zastępczy interfejs dla Kybera
export interface KyberPreKeyRecord {
  keyId: number
  pubBytes: Uint8Array
  signature: Uint8Array
}

async function getDB(): Promise<IDBPDatabase> {
  return openDB(DB_NAME, DB_VERSION, {
    upgrade(db) {
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME)
      }
    }
  })
}

function getStorePrefix(): string {
  if (typeof localStorage === 'undefined') return ''
  const raw = localStorage.getItem('auth-original-user-id') || localStorage.getItem('my_user_id') || localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || ''
  const clean = String(raw).replace(/^user_/, '')
  return clean ? `${clean}:` : ''
}

function addressKey(address: ProtocolAddress): string {
  // Nasz ProtocolAddress z 'crypto/session.ts' ma właściwości publiczne, nie metody
  return `${address.name}_${address.deviceId}`
}

export class SignalIndexedDBStore {
  async getIdentityKey(): Promise<Uint8Array> {
    const pair = await this.getIdentityKeyPair()
    if (!pair) throw new Error('Brak klucza tożsamości')
    return pair.privBytes
  }

  async getIdentityKeyPair(): Promise<IdentityKeyPair | null> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}identityKeyPair`)) || (await db.get(STORE_NAME, 'identityKeyPair'))
    if (!data) return null
    return data as IdentityKeyPair
  }

  async hasIdentityKeyPair(): Promise<boolean> {
    const db = await getDB()
    const p = getStorePrefix()
    return !!((await db.get(STORE_NAME, `${p}identityKeyPair`)) || (await db.get(STORE_NAME, 'identityKeyPair')))
  }

  async getLocalRegistrationId(): Promise<number> {
    const db = await getDB()
    const p = getStorePrefix()
    let id = await db.get(STORE_NAME, `${p}registrationId`)
    if (id === undefined || id === null) {
      id = await db.get(STORE_NAME, 'registrationId')
    }
    if (id === undefined || id === null) throw new Error('Registration ID not initialized')
    return Number(id)
  }

  async saveIdentity(name: ProtocolAddress, identityKey: Uint8Array): Promise<IdentityChange> {
    const db = await getDB()
    const p = getStorePrefix()
    const key = `${p}identityKey_${addressKey(name)}`
    const existing = await db.get(STORE_NAME, key)

    await db.put(STORE_NAME, identityKey, key)

    if (!existing) return IdentityChange.NewOrUnchanged

    // Porównanie bajt po bajcie weryfikujące, czy klucz jest ten sam
    const isSame = existing.length === identityKey.length &&
                   existing.every((v: number, i: number) => v === identityKey[i])

    return isSame ? IdentityChange.NewOrUnchanged : IdentityChange.ReplacedExisting
  }

  async isTrustedIdentity(
    _name: ProtocolAddress,
    _identityKey: Uint8Array,
    _direction: Direction
  ): Promise<boolean> {
    return true
  }

  async getIdentity(name: ProtocolAddress): Promise<Uint8Array | null> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}identityKey_${addressKey(name)}`)) || (await db.get(STORE_NAME, `identityKey_${addressKey(name)}`))
    return data || null
  }

  async getSession(address: ProtocolAddress): Promise<SessionRecord | null> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}session_${addressKey(address)}`)) || (await db.get(STORE_NAME, `session_${addressKey(address)}`))
    return data || null
  }

  async saveSession(address: ProtocolAddress, record: SessionRecord | null): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    const key = `${p}session_${addressKey(address)}`
    if (record === null) {
      await db.delete(STORE_NAME, key)
    } else {
      await db.put(STORE_NAME, record, key)
    }
  }

  async getExistingSessions(addresses: ProtocolAddress[]): Promise<SessionRecord[]> {
    const sessions: SessionRecord[] = []
    for (const address of addresses) {
      const session = await this.getSession(address)
      if (!session) {
        throw new Error(`No session for ${address.name}:${address.deviceId}`)
      }
      sessions.push(session)
    }
    return sessions
  }

  async getPreKey(keyId: number): Promise<PreKeyRecord> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}prekey_${keyId}`)) || (await db.get(STORE_NAME, `prekey_${keyId}`))
    if (!data) throw new Error(`PreKey ${keyId} not found`)
    return data as PreKeyRecord
  }

  async savePreKey(keyId: number, record: PreKeyRecord): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.put(STORE_NAME, record, `${p}prekey_${keyId}`)
  }

  async removePreKey(keyId: number): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.delete(STORE_NAME, `${p}prekey_${keyId}`)
  }

  async getSignedPreKey(keyId: number): Promise<SignedPreKeyRecord> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}signedprekey_${keyId}`)) || (await db.get(STORE_NAME, `signedprekey_${keyId}`))
    if (!data) throw new Error(`SignedPreKey ${keyId} not found`)
    return data as SignedPreKeyRecord
  }

  async saveSignedPreKey(keyId: number, record: SignedPreKeyRecord): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.put(STORE_NAME, record, `${p}signedprekey_${keyId}`)
  }

  async getKyberPreKey(kyberPreKeyId: number): Promise<KyberPreKeyRecord> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}kyberprekey_${kyberPreKeyId}`)) || (await db.get(STORE_NAME, `kyberprekey_${kyberPreKeyId}`))
    if (!data) throw new Error(`KyberPreKey ${kyberPreKeyId} not found`)
    return data as KyberPreKeyRecord
  }

  async saveKyberPreKey(kyberPreKeyId: number, record: KyberPreKeyRecord): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.put(STORE_NAME, record, `${p}kyberprekey_${kyberPreKeyId}`)
  }

  async markKyberPreKeyUsed(
    _kyberPreKeyId: number,
    _signedPreKeyId: number,
    _baseKey: Uint8Array
  ): Promise<void> {}

  async saveOwnIdentity(keyPair: IdentityKeyPair, registrationId: number): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.put(STORE_NAME, keyPair, `${p}identityKeyPair`)
    await db.put(STORE_NAME, registrationId, `${p}registrationId`)
  }

  async getCustomValue<T>(key: string): Promise<T | null> {
    const db = await getDB()
    const p = getStorePrefix()
    const data = (await db.get(STORE_NAME, `${p}${key}`)) || (await db.get(STORE_NAME, key))
    return data !== undefined ? (data as T) : null
  }

  async saveCustomValue<T>(key: string, value: T): Promise<void> {
    const db = await getDB()
    const p = getStorePrefix()
    await db.put(STORE_NAME, value, `${p}${key}`)
  }

  async clearAll(): Promise<void> {
    const db = await getDB()
    await db.clear(STORE_NAME)
  }
}

export const signalStore = new SignalIndexedDBStore()
