export class ProtocolAddress {
  constructor(public name: string, public deviceId: number) {}
  toString() { return `${this.name}.${this.deviceId}` }
  static new(name: string, deviceId: number) { return new ProtocolAddress(name, deviceId) }
}

export interface SessionState {
  rootKey: string
  chainKeySend: string
  chainKeyReceive: string
  messageNumberSend: number
  messageNumberReceive: number
  previousChainLength: number
  dhSendPriv: string
  dhSendPub: string
  dhReceivePub: string
}

export class SessionRecord {
  constructor(public address: string, public state: SessionState) {}
}
