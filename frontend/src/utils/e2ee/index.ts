export {
  registerNewDevice,
  initIdentityKeys,
  hasLocalPrivateKey,
  deleteLocalPrivateKey,
  encryptFanoutMessage,
  decryptSignalMessage,
  encryptMessage,
  decryptMessage,
  decryptionCache,
  backupChatHistoryToVault,
  restoreChatHistoryFromVault,
  getVerificationSessionKeys,
  fetchPrekeyBundleFromServer,
  type EncryptedPayload
} from './signalService.client'

export {
  setupVaultPin,
  unlockVaultAndRestoreHistory,
  hasVaultOnServer
} from './hsmClient.client'

import * as signalService from './signalService.client'
import * as hsmClient from './hsmClient.client'

if (typeof window !== 'undefined') {
  (window as any).__e2ee = {
    ...signalService,
    ...hsmClient
  }
}
