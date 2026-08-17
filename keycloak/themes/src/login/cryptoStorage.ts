export interface UserProfile {
  id: number;
  name: string;
  username: string;
  avatar: string;
  hasActiveSession?: boolean;
  lastLogin?: number;
}

const DB_NAME = "profile-crypto-db";
const STORE_NAME = "keys-store";
const KEY_NAME = "encryption-key";

function getOrGenerateKey(): Promise<CryptoKey> {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, 1);
        
        request.onupgradeneeded = () => {
            const db = request.result;
            if (!db.objectStoreNames.contains(STORE_NAME)) {
                db.createObjectStore(STORE_NAME);
            }
        };
        
        request.onsuccess = () => {
            const db = request.result;
            const transaction = db.transaction(STORE_NAME, "readwrite");
            const store = transaction.objectStore(STORE_NAME);
            const getReq = store.get(KEY_NAME);
            
            getReq.onsuccess = () => {
                if (getReq.result) {
                    resolve(getReq.result as CryptoKey);
                } else {
                    window.crypto.subtle.generateKey(
                        {
                            name: "AES-GCM",
                            length: 256
                        },
                        false, // extractable = false (prevents exporting/extracting the key)
                        ["encrypt", "decrypt"]
                    ).then(key => {
                        const putTransaction = db.transaction(STORE_NAME, "readwrite");
                        const putStore = putTransaction.objectStore(STORE_NAME);
                        putStore.put(key, KEY_NAME);
                        putTransaction.oncomplete = () => resolve(key);
                        putTransaction.onerror = () => reject(putTransaction.error);
                    }).catch(reject);
                }
            };
            getReq.onerror = () => reject(getReq.error);
        };
        request.onerror = () => reject(request.error);
    });
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = "";
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}

function base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binaryString = window.atob(base64);
    const len = binaryString.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
}

export async function encryptAndSaveProfiles(profiles: UserProfile[]): Promise<void> {
    try {
        const key = await getOrGenerateKey();
        const jsonString = JSON.stringify(profiles);
        const encoder = new TextEncoder();
        const dataBytes = encoder.encode(jsonString);
        
        // Generate a random 12-byte IV for AES-GCM
        const iv = window.crypto.getRandomValues(new Uint8Array(12));
        
        const encryptedBuffer = await window.crypto.subtle.encrypt(
            {
                name: "AES-GCM",
                iv: iv
            },
            key,
            dataBytes
        );
        
        // Combine IV and Ciphertext
        const combined = new Uint8Array(iv.length + encryptedBuffer.byteLength);
        combined.set(iv, 0);
        combined.set(new Uint8Array(encryptedBuffer), iv.length);
        
        const base64Data = arrayBufferToBase64(combined.buffer);
        localStorage.setItem("recent_profiles_enc", base64Data);
        
        // Remove plain text storage if it exists
        localStorage.removeItem("recent_profiles");
    } catch (e) {
        console.error("Failed to encrypt profiles:", e);
    }
}

export async function loadAndDecryptProfiles(): Promise<UserProfile[]> {
    if (typeof window === "undefined") return [];

    const base64Data = localStorage.getItem("recent_profiles_enc");
    if (!base64Data) {
        // Migration: read plain text profiles if present, encrypt them, and return
        const legacy = localStorage.getItem("recent_profiles");
        if (legacy) {
            try {
                const parsed = JSON.parse(legacy);
                await encryptAndSaveProfiles(parsed);
                return parsed;
            } catch (e) {
                return [];
            }
        }
        return [];
    }
    
    try {
        const key = await getOrGenerateKey();
        const combinedBuffer = base64ToArrayBuffer(base64Data);
        const combinedBytes = new Uint8Array(combinedBuffer);
        
        const iv = combinedBytes.slice(0, 12);
        const ciphertext = combinedBytes.slice(12);
        
        const decryptedBuffer = await window.crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: iv
            },
            key,
            ciphertext
        );
        
        const decoder = new TextDecoder();
        const jsonString = decoder.decode(decryptedBuffer);
        return JSON.parse(jsonString) as UserProfile[];
    } catch (e) {
        console.error("Failed to decrypt profiles:", e);
        return [];
    }
}
