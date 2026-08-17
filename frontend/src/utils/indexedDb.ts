import type { ChatMessage } from '@/types/Message'
import { encryptMessage, decryptMessage } from '@/utils/e2ee'

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
      const db = request.result
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.close()
        const delReq = indexedDB.deleteDatabase(DB_NAME)
        delReq.onsuccess = () => {
          const req2 = indexedDB.open(DB_NAME, DB_VERSION)
          req2.onupgradeneeded = () => {
            const db2 = req2.result
            if (!db2.objectStoreNames.contains(STORE_NAME)) {
              const store = db2.createObjectStore(STORE_NAME, { keyPath: 'id' })
              store.createIndex('chatId', 'chatId', { unique: false })
              store.createIndex('time', 'time', { unique: false })
            }
          }
          req2.onsuccess = () => resolve(req2.result)
          req2.onerror = () => reject(req2.error)
        }
        delReq.onerror = () => reject(delReq.error)
        return
      }
      resolve(db)
    }

    request.onupgradeneeded = () => {
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
  const isPrivate = msg.isPrivate !== false
  const secureMsg = { ...msg, isPrivate }
  
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite')
    const store = tx.objectStore(STORE_NAME)
    const request = store.put(secureMsg)

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
      store.put({ ...msg, isPrivate: msg.isPrivate !== false })
    }
  })
}


export async function getMessagesForChat(chatId: string): Promise<ChatMessage[]> {
  const cleanChatId = String(chatId).replace('user_', '')
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly')
    const store = tx.objectStore(STORE_NAME)
    const index = store.index('chatId')

    const req1 = index.getAll(IDBKeyRange.only(cleanChatId))
    const req2 = index.getAll(IDBKeyRange.only('user_' + cleanChatId))

    let results1: ChatMessage[] = []
    let results2: ChatMessage[] = []
    let completed = 0

    const processResults = async () => {
      completed++
      if (completed === 2) {
        const merged = [...results1, ...results2]
        const uniqueMap = new Map<string, ChatMessage>()
        for (const msg of merged) {
          uniqueMap.set(String(msg.id), msg)
        }
        const results = Array.from(uniqueMap.values())

        const decryptedResults: ChatMessage[] = []
        for (const msg of results) {
          const isPrivate = msg.isPrivate !== false
          let content = msg.content
          if (content && typeof content === 'string' && content.startsWith('e2ee:')) {
            try {
              const targetId = isPrivate
                ? ((msg as any).senderId || (msg as any).sender_id || (msg as any).sender || msg.chatId)
                : msg.chatId
              content = await decryptMessage(content, String(targetId), isPrivate)
            } catch {
              content = '🔒 [Zaszyfrowana wiadomość]'
            }
          }
          decryptedResults.push({ ...msg, content })
        }
        
        decryptedResults.sort((a, b) => (a.time || 0) - (b.time || 0))
        resolve(decryptedResults)
      }
    }

    req1.onsuccess = () => {
      results1 = req1.result || []
      processResults().catch(reject)
    }
    req1.onerror = () => reject(req1.error)

    req2.onsuccess = () => {
      results2 = req2.result || []
      processResults().catch(reject)
    }
    req2.onerror = () => reject(req2.error)
  })
}

export async function getLastMessageForChat(chatId: string): Promise<ChatMessage | null> {
  const cleanChatId = String(chatId).replace('user_', '')
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly')
    const store = tx.objectStore(STORE_NAME)
    const index = store.index('chatId')

    const req1 = index.openCursor(IDBKeyRange.only(cleanChatId), 'prev')
    const req2 = index.openCursor(IDBKeyRange.only('user_' + cleanChatId), 'prev')

    let msg1: ChatMessage | null = null
    let msg2: ChatMessage | null = null
    let completed = 0

    const processResults = async () => {
      completed++
      if (completed === 2) {
        let lastMsg = msg1
        if (msg2) {
          if (!lastMsg || (msg2.time || 0) > (lastMsg.time || 0)) {
            lastMsg = msg2
          }
        }

        if (lastMsg) {
          const isPrivate = lastMsg.isPrivate !== false
          let content = lastMsg.content
          if (content && typeof content === 'string' && content.startsWith('e2ee:')) {
            try {
              content = await decryptMessage(content, lastMsg.chatId, isPrivate)
            } catch {
              content = '🔒 [Zaszyfrowana wiadomość]'
            }
          }
          resolve({ ...lastMsg, content })
        } else {
          resolve(null)
        }
      }
    }

    req1.onsuccess = () => {
      const cursor = req1.result
      if (cursor) msg1 = cursor.value as ChatMessage
      processResults().catch(reject)
    }
    req1.onerror = () => reject(req1.error)

    req2.onsuccess = () => {
      const cursor = req2.result
      if (cursor) msg2 = cursor.value as ChatMessage
      processResults().catch(reject)
    }
    req2.onerror = () => reject(req2.error)
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
