export interface BaseMessage {
  id: number;
  sender: 'me' | 'other';
  time: number;
  isReply?: boolean;
  replyToSender?: string;
  replyToContentSnippet?: string;
  iconSizeState?: 'default' | 'small' | 'medium' | 'large';
  reactions?: string[];
}

export interface TextMessage extends BaseMessage {
  type: 'text';
  content: string;
}

export interface ImageMessage extends BaseMessage {
  type: 'image';
  content: string;
  imageUrl: string;
  mediaUrls?: string[]; // Obsługa galerii zdjęć
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

export interface VideoMessage extends BaseMessage {
  type: 'video';
  content: string;
  videoUrl: string;
}

export interface FileMessage extends BaseMessage {
  type: 'file';
  content: string;
  fileName: string;
  fileSize: number;
  fileUrl: string;
}


export interface PollOption {
  id: string | number;
  text: string;
  votes: number;
  votedByMe: boolean;
}

export interface PollData {
  question: string;
  options: PollOption[];
  allowMultiple: boolean;
  allowAddOption: boolean;
}

export interface PollMessage extends BaseMessage {
  type: 'poll';
  content: string;
  pollData: PollData;
}



export type Message =
  | TextMessage
  | ImageMessage
  | GifMessage
  | AudioMessage
  | VideoMessage
  | FileMessage
  | PollMessage;

export interface AudioState {
  isPlaying: boolean;
  duration: number;
  currentTime: number;
}
