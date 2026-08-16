import { bufferToBase64 } from './serializer'

export async function generateVerificationHash(myPub: Uint8Array, otherPub: Uint8Array): Promise<string> {
  const combined = new Uint8Array(myPub.length + otherPub.length)

  // Sort by raw bytes for consistent ordering
  let meFirst = true
  for (let i = 0; i < myPub.length; i++) {
    if (myPub[i] !== otherPub[i]) {
      meFirst = myPub[i] < otherPub[i]
      break
    }
  }

  if (meFirst) {
    combined.set(myPub, 0)
    combined.set(otherPub, myPub.length)
  } else {
    combined.set(otherPub, 0)
    combined.set(myPub, otherPub.length)
  }

  const hashBytes = await window.crypto.subtle.digest('SHA-256', combined)
  return Array.from(new Uint8Array(hashBytes)).map(b => b.toString(16).padStart(2, '0').toUpperCase()).join(' ')
}
