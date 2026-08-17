import { bufferToBase64, base64ToBytes } from './serializer'

export interface SignalMessagePayload {
  n: number
  pn: number
  dhPub: string
  ct: string
  preKeyId?: number
}

export function serializeMessage(n: number, pn: number, dhPub: Uint8Array, ct: Uint8Array, preKeyId?: number): Uint8Array {
  const payload: SignalMessagePayload = { n, pn, dhPub: bufferToBase64(dhPub), ct: bufferToBase64(ct), preKeyId }
  return new TextEncoder().encode(JSON.stringify(payload))
}

export function deserializeMessage(bytes: Uint8Array): SignalMessagePayload {
  return JSON.parse(new TextDecoder().decode(bytes))
}
