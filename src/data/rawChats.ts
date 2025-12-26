import { getUserById } from './users'; // Import users data



export enum ChatType {
  Private = 'private',
  Group = 'group',
}

export interface GroupMember {
  id: number;
  name: string;
  nickname?: string;
  addedByUserId?: number; // Add addedByUserId field
}

export interface Chat {
  id: number;
  name: string;
  avatarUrl: string;
  lastMessage: string;
  timeAgo: string;
  unread: boolean;
  isActive: boolean;
  isPinch?: boolean;
  type: ChatType; // Use ChatType enum
  groupMembers?: GroupMember[]; // Optional for group chats
  extraAvatars?: string[];
  otherUserNickname?: string; // Add otherUserNickname for private chats
}

const rawChats: Chat[] = [
  {
    id: 1,
    name: 'Carbonara 🤠',
    avatarUrl: 'https://randomuser.me/api/portraits/men/32.jpg',
    lastMessage: 'Użytkownik Carbonara 🤠 wysłał ...',
    timeAgo: '21 min',
    unread: false,
    isActive: false,
    isPinch: true,
    type: ChatType.Private,
  },
  {
    id: 2,
    name: 'Łuków24',
    avatarUrl: 'https://randomuser.me/api/portraits/men/45.jpg',
    lastMessage: 'Użytkownik Łuków24 wysłał ...',
    timeAgo: '49 min',
    unread: false,
    isActive: false,
    type: ChatType.Private,
  },
  {
    id: 3,
    name: 'Pati Kochanska',
    avatarUrl: 'https://randomuser.me/api/portraits/women/44.jpg',
    lastMessage: 'jeszcze w zime',
    timeAgo: '5 godz.',
    unread: false,
    isActive: true,
    type: ChatType.Private,
  },
  {
    id: 4,
    name: 'Grupa 7 (casual)',
    avatarUrl: 'https://randomuser.me/api/portraits/men/22.jpg',
    lastMessage: 'Paweł: chyba tak',
    timeAgo: '6 godz.',
    unread: false,
    isActive: false,
    type: ChatType.Group,
    groupMembers: [
      { id: 1, name: getUserById(1)?.name || 'Unknown User', nickname: 'Bartosz G.', addedByUserId: 1 },
      { id: 2, name: getUserById(2)?.name || 'Unknown User', nickname: 'Ania K.', addedByUserId: 1 },
      { id: 3, name: getUserById(3)?.name || 'Unknown User', nickname: 'Jan N.', addedByUserId: 2 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/22.jpg',
      'https://randomuser.me/api/portraits/women/33.jpg',
    ],
  },
  {
    id: 5,
    name: 'Koalicja 2 Grudnia',
    avatarUrl: 'https://randomuser.me/api/portraits/men/67.jpg',
    lastMessage: 'Ty: Aż tak za lukowe...',
    timeAgo: '9 godz.',
    unread: false,
    isActive: false,
    type: ChatType.Group,
    groupMembers: [
      { id: 4, name: getUserById(4)?.name || 'Unknown User', nickname: 'Kasia W.', addedByUserId: 1 },
      { id: 5, name: getUserById(5)?.name || 'Unknown User', nickname: 'Piotr K.', addedByUserId: 4 },
      { id: 6, name: getUserById(6)?.name || 'Unknown User', nickname: 'Marysia L.', addedByUserId: 5 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/women/12.jpg',
      'https://randomuser.me/api/portraits/men/15.jpg',
    ],
  },
  {
    id: 6,
    name: 'Infa 2025',
    avatarUrl: 'https://randomuser.me/api/portraits/women/28.jpg',
    lastMessage: 'Natalia: Okej',
    timeAgo: '13 godz.',
    unread: false,
    isActive: false,
    type: ChatType.Group,
    groupMembers: [
      { id: 7, name: getUserById(7)?.name || 'Unknown User', nickname: 'Tomek Z.', addedByUserId: 1 },
      { id: 8, name: getUserById(8)?.name || 'Unknown User', nickname: 'Ewa K.', addedByUserId: 7 },
      { id: 9, name: getUserById(9)?.name || 'Unknown User', nickname: 'Michał W.', addedByUserId: 8 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/women/28.jpg',
      'https://randomuser.me/api/portraits/men/19.jpg',
    ],
  },
  {
    id: 7,
    name: 'Milf Hunters',
    avatarUrl: 'https://randomuser.me/api/portraits/men/75.jpg',
    lastMessage: 'Mateusz: Piłkarzami z przypadp...',
    timeAgo: '2 dni',
    unread: true,
    isActive: true,
    type: ChatType.Group,
    groupMembers: [
      { id: 1, name: getUserById(1)?.name || 'Unknown User', nickname: 'Bartek G.', addedByUserId: 10 },
      { id: 10, name: getUserById(10)?.name || 'Unknown User', nickname: 'Ola D.', addedByUserId: 1 },
      { id: 2, name: getUserById(2)?.name || 'Unknown User', nickname: 'Ania K.', addedByUserId: 10 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/75.jpg',
      'https://randomuser.me/api/portraits/men/81.jpg',
    ],
  },
  {
    id: 8,
    name: 'Legia Futsal',
    avatarUrl: 'https://randomuser.me/api/portraits/men/52.jpg',
    lastMessage: 'Bramka Luci Prioriego nomi...',
    timeAgo: '2 dni',
    unread: true,
    isActive: false,
    type: ChatType.Group,
    groupMembers: [
      { id: 3, name: getUserById(3)?.name || 'Unknown User', nickname: 'Jan N.', addedByUserId: 1 },
      { id: 4, name: getUserById(4)?.name || 'Unknown User', nickname: 'Kasia W.', addedByUserId: 3 },
      { id: 5, name: getUserById(5)?.name || 'Unknown User', nickname: 'Piotr K.', addedByUserId: 4 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/52.jpg',
      'https://randomuser.me/api/portraits/men/61.jpg',
    ],
  },
  {
    id: 9,
    name: 'Mateusz Bieniek',
    avatarUrl: 'https://randomuser.me/api/portraits/men/41.jpg',
    lastMessage: 'Nie dam rady',
    timeAgo: '2 dni',
    unread: false,
    isActive: false,
    type: ChatType.Private,
  },
  {
    id: 10,
    name: 'Zgrupowanie Reprezentacja Se...',
    avatarUrl: 'https://randomuser.me/api/portraits/men/36.jpg',
    lastMessage: 'Michał: Nie, tym razem to nie ...',
    timeAgo: '3 dni',
    unread: true,
    isActive: true,
    type: ChatType.Group,
    groupMembers: [
      { id: 6, name: getUserById(6)?.name || 'Unknown User', nickname: 'Marysia L.', addedByUserId: 1 },
      { id: 7, name: getUserById(7)?.name || 'Unknown User', nickname: 'Tomek Z.', addedByUserId: 6 },
      { id: 8, name: getUserById(8)?.name || 'Unknown User', nickname: 'Ewa K.', addedByUserId: 7 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/36.jpg',
      'https://randomuser.me/api/portraits/men/47.jpg',
    ],
  },
  {
    id: 11,
    name: 'Wioletta Miazek',
    avatarUrl: 'https://randomuser.me/api/portraits/women/65.jpg',
    lastMessage: '🙌 3 dni',
    timeAgo: '3 dni',
    unread: true,
    isActive: false,
    type: ChatType.Private,
  },
  {
    id: 12,
    name: 'Adam Zarzycki',
    avatarUrl: 'https://randomuser.me/api/portraits/men/88.jpg',
    lastMessage: 'Ty: Gdzie ty jesteś?',
    timeAgo: '3 dni',
    unread: true,
    isActive: false,
    type: ChatType.Private,
  },
  {
    id: 13,
    name: 'WC UPOSiF',
    avatarUrl: 'https://randomuser.me/api/portraits/men/29.jpg',
    lastMessage: '...',
    timeAgo: '3 dni',
    unread: false,
    isActive: false,
    type: ChatType.Group,
    groupMembers: [
      { id: 9, name: getUserById(9)?.name || 'Unknown User', nickname: 'Michał W.', addedByUserId: 1 },
      { id: 10, name: getUserById(10)?.name || 'Unknown User', nickname: 'Ola D.', addedByUserId: 9 },
      { id: 1, name: getUserById(1)?.name || 'Unknown User', nickname: 'Bartosz M.', addedByUserId: 10 },
    ],
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/29.jpg',
      'https://randomuser.me/api/portraits/women/55.jpg',
    ],
  },
];

export default rawChats;
