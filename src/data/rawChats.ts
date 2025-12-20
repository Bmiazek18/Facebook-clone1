export interface Chat {
  id: number;
  name: string;
  avatarUrl: string;
  lastMessage: string;
  timeAgo: string;
  unread: boolean;
  isActive: boolean;
  isPinch?: boolean;
  isGroup?: boolean;
  extraAvatars?: string[];
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
  },
  {
    id: 2,
    name: 'Łuków24',
    avatarUrl: 'https://randomuser.me/api/portraits/men/45.jpg',
    lastMessage: 'Użytkownik Łuków24 wysłał ...',
    timeAgo: '49 min',
    unread: false,
    isActive: false,
  },
  {
    id: 3,
    name: 'Pati Kochanska',
    avatarUrl: 'https://randomuser.me/api/portraits/women/44.jpg',
    lastMessage: 'jeszcze w zime',
    timeAgo: '5 godz.',
    unread: false,
    isActive: true,
  },
  {
    id: 4,
    name: 'Grupa 7 (casual)',
    avatarUrl: 'https://randomuser.me/api/portraits/men/22.jpg',
    lastMessage: 'Paweł: chyba tak',
    timeAgo: '6 godz.',
    unread: false,
    isActive: false,
    isGroup: true,
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
    isGroup: true,
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
    isGroup: true,
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
    isGroup: true,
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
    isGroup: true,
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
  },
  {
    id: 10,
    name: 'Zgrupowanie Reprezentacja Se...',
    avatarUrl: 'https://randomuser.me/api/portraits/men/36.jpg',
    lastMessage: 'Michał: Nie, tym razem to nie ...',
    timeAgo: '3 dni',
    unread: true,
    isActive: true,
    isGroup: true,
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
  },
  {
    id: 12,
    name: 'Adam Zarzycki',
    avatarUrl: 'https://randomuser.me/api/portraits/men/88.jpg',
    lastMessage: 'Ty: Gdzie ty jesteś?',
    timeAgo: '3 dni',
    unread: true,
    isActive: false,
  },
  {
    id: 13,
    name: 'WC UPOSiF',
    avatarUrl: 'https://randomuser.me/api/portraits/men/29.jpg',
    lastMessage: '...',
    timeAgo: '3 dni',
    unread: false,
    isActive: false,
    isGroup: true,
    extraAvatars: [
      'https://randomuser.me/api/portraits/men/29.jpg',
      'https://randomuser.me/api/portraits/women/55.jpg',
    ],
  },
];

export default rawChats;
