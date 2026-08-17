export interface BaseMessage {
  id: number | string
  sender: 'me' | 'them'
  time: number
  timestamp?: string
  isReply?: boolean
  replyToSender?: string
  replyToContentSnippet?: string
  replyToId?: number
  iconSizeState?: 'default' | 'small' | 'medium' | 'large'
  reactions?: Record<string, string>
  isPinned?: boolean
}

export interface TextMessage extends BaseMessage {
  type: 'text'
  content: string
}

/** Wiadomość używana przez store konwersacji i dane zwracane przez API czatu. */
export interface ChatMessage extends Omit<BaseMessage, 'reactions'> {
  chatId: string
  type: Message['type']
  content: string
  imageUrl?: string
  audioUrl?: string
  duration?: number
  reactions?: Record<string, string[]>
  isPinned?: boolean
  pollData?: PollData
  systemActionPayload?: string
}

export interface ImageMessage extends BaseMessage {
  type: 'image'
  content: string
  imageUrl: string
  mediaUrls?: string[]
}

export interface GifMessage extends BaseMessage {
  type: 'gif'
  content: string
  imageUrl: string
}

export interface AudioMessage extends BaseMessage {
  type: 'audio'
  content: string
  audioUrl: string
  duration: number
}

export interface VideoMessage extends BaseMessage {
  type: 'video'
  content: string
  videoUrl: string
}

export interface FileMessage extends BaseMessage {
  type: 'file'
  content: string
  fileName: string
  fileSize: number
  fileUrl: string
}

export interface PollOption {
  id: string | number
  text: string
  votes: number
  votedByMe: boolean
}

export interface PollData {
  question: string
  options: PollOption[]
  allowMultiple: boolean
  allowAddOption: boolean
}

export interface PollMessage extends BaseMessage {
  type: 'poll'
  content: string
  pollData: PollData
}
export type MessageActionType = 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME'

export interface ActionsMessage extends BaseMessage {
  type: 'action'
  subType: MessageActionType
  payload: string // Payload for the action
}
// --- NOWE TYPY ---

export interface CallMessage extends BaseMessage {
  type: 'call'

  duration: number // Czas trwania (np. string "2 min" lub number w sekundach do sformatowania)
}

export interface CallRejectedMessage extends BaseMessage {
  type: 'call_rejected'
}

export interface LinkMessage extends BaseMessage {
  type: 'link'

  url: string
  content: string
}
export interface PostLinkMessage extends BaseMessage {
  type: 'feed-link' | 'post_link'

  sharedPostId?: string
  url?: string
  linkUrl?: string
  content?: string
}

// ----------------

export type Message =
  | TextMessage
  | ImageMessage
  | GifMessage
  | AudioMessage
  | VideoMessage
  | FileMessage
  | PollMessage
  | CallMessage
  | CallRejectedMessage
  | ActionsMessage
  | LinkMessage
  | PostLinkMessage

export interface AudioState {
  isPlaying: boolean
  duration: number
  currentTime: number
}
