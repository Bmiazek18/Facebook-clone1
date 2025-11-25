export interface BaseMessage {
  id: number;
  sender: 'me' | 'other';
  time: number;
  isReply?: boolean;
  replyToSender?: string;
  replyToContentSnippet?: string;
  iconSizeState?: 'default' | 'small' | 'medium' | 'large';
}

export interface TextMessage extends BaseMessage {
  type: 'text';
  content: string;
}

export interface ImageMessage extends BaseMessage {
  type: 'image';
  content: string;
  imageUrl: string;
}

export interface GifMessage extends BaseMessage {
  type: 'gif';
  content: string;
  imageUrl: string;
}

export interface AudioMessage extends BaseMessage {
  type: 'audio';
  content: string;
  audioUrl: string;
  duration: number;
}

export type Message = TextMessage | ImageMessage | GifMessage | AudioMessage;

export interface AudioState {
  isPlaying: boolean;
  duration: number;
  currentTime: number;
}
