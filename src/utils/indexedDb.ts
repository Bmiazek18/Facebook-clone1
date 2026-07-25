import type { ChatMessage } from '@/types/Message'

const DB_NAME = 'facebook_clone_chat_db'
const DB_VERSION = 1
const STORE_NAME = 'messages'

export function initDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    if (!import.meta.client) {
      reject(new Error('IndexedDB is only available on client side'))
      return
    }

    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onerror = () => {
      reject(request.error)
    }

    request.onsuccess = () => {
      resolve(request.result)
    }

    request.onupgradeneeded = (event) => {
      const db = request.result
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' })
        store.createIndex('chatId', 'chatId', { unique: false })
        store.createIndex('time', 'time', { unique: false })
      }
    }
  })
}

export async function saveMessage(msg: ChatMessage): Promise<void> {
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite')
    const store = tx.objectStore(STORE_NAME)
    const request = store.put(msg)

    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}

export async function saveMessages(msgs: ChatMessage[]): Promise<void> {
  if (msgs.length === 0) return
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite')
    const store = tx.objectStore(STORE_NAME)

    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)

    for (const msg of msgs) {
      store.put(msg)
    }
  })
}

export async function getMessagesForChat(chatId: string): Promise<ChatMessage[]> {
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly')
    const store = tx.objectStore(STORE_NAME)
    const index = store.index('chatId')
    const request = index.getAll(IDBKeyRange.only(chatId))

    request.onsuccess = () => {
      const results = request.result as ChatMessage[]
      // Sort by time ascending
      results.sort((a, b) => (a.time || 0) - (b.time || 0))
      resolve(results)
    }
    request.onerror = () => reject(request.error)
  })
}

export async function getLastMessageForChat(chatId: string): Promise<ChatMessage | null> {
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly')
    const store = tx.objectStore(STORE_NAME)
    const index = store.index('chatId')
    const request = index.openCursor(IDBKeyRange.only(chatId), 'prev')

    request.onsuccess = () => {
      const cursor = request.result
      if (cursor) {
        resolve(cursor.value as ChatMessage)
      } else {
        resolve(null)
      }
    }
    request.onerror = () => reject(request.error)
  })
}

export async function clearAllMessages(): Promise<void> {
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite')
    const store = tx.objectStore(STORE_NAME)
    const request = store.clear()

    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}
