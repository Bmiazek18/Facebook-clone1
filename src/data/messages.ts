import type { Message } from '@/types/Message';

// helper to create timestamps a few minutes/hours/days ago
const createTimestamp = (minutesAgo = 0, hoursAgo = 0, daysAgo = 0) => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - minutesAgo);
  d.setHours(d.getHours() - hoursAgo);
  d.setDate(d.getDate() - daysAgo);
  return d.getTime();
};

export type ChatMessage = Message & { chatId: number };

const messages: ChatMessage[] = [
  // chat 1 - mixed
  {
    id: 1001,
    chatId: 1,
    sender: 'other',
    type: 'text',
    content: 'Hej! Masz chwilę, żeby ogarnąć ten task?',
    time: createTimestamp(10),
  },
  {
    id: 1002,
    chatId: 1,
    sender: 'me',
    type: 'text',
    content: 'Tak, zaraz popatrzę. Wydaje się proste.',
    time: createTimestamp(8),
  },
  {
    id: 1003,
    chatId: 1,
    sender: 'other',
    type: 'image',
    content: 'Zobacz zdjęcie',
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria10.jpg',
    time: createTimestamp(7),
  },

  // chat 3 - more messages
  {
    id: 3001,
    chatId: 3,
    sender: 'other',
    type: 'text',
    content: 'Wciąż aktualne spotkanie w piątek?',
    time: createTimestamp(0, 2),
  },
  {
    id: 3002,
    chatId: 3,
    sender: 'me',
    type: 'audio',
    content: 'Wiadomość głosowa',
    audioUrl: 'https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3',
    duration: 12,
    time: createTimestamp(0, 1, 0),
  },

  // chat 4 - group with poll
  {
    id: 4001,
    chatId: 4,
    sender: 'other',
    type: 'text',
    content: 'Stawiamy się wszyscy na 18?',
    time: createTimestamp(0, 4),
  },
  {
    id: 4002,
    chatId: 4,
    sender: 'me',
    type: 'poll',
    content: 'Utworzono ankietę',
    pollData: {
      question: 'Kiedy mamy spotkanie?',
      allowMultiple: false,
      allowAddOption: true,
      options: [
        { id: 1, text: 'Wtorek 19:00', votes: 3, votedByMe: false },
        { id: 2, text: 'Środa 20:00', votes: 1, votedByMe: true },
      ],
    },
    time: createTimestamp(0, 5),
  },

  // chat 5 - file + gif
  {
    id: 5001,
    chatId: 5,
    sender: 'other',
    type: 'file',
    content: 'Przesyłam raport',
    fileName: 'report.pdf',
    fileSize: 345_678,
    fileUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    time: createTimestamp(30, 12),
  },
  {
    id: 5002,
    chatId: 5,
    sender: 'me',
    type: 'gif',
    content: 'Haha',
    imageUrl: 'https://media.giphy.com/media/Ju7l5y9osyymQ/giphy.gif',
    time: createTimestamp(0, 24, 1),
  },

  // chat 7 - multiple types
  {
    id: 7001,
    chatId: 7,
    sender: 'me',
    type: 'text',
    content: 'Kto idzie na trening?',
    time: createTimestamp(0, 0, 2),
  },
  {
    id: 7002,
    chatId: 7,
    sender: 'other',
    type: 'text',
    content: 'Ja, pewnie będę o 18:00',
    time: createTimestamp(0, 0, 2),
  },
  {
    id: 7003,
    chatId: 7,
    sender: 'other',
    type: 'video',
    content: 'Filmik z meczu',
    videoUrl: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
    time: createTimestamp(0, 0, 2),
  },

  // chat 11 - single short
  {
    id: 11001,
    chatId: 11,
    sender: 'other',
    type: 'text',
    content: '🙌 Dzięki!',
    time: createTimestamp(0, 0, 3),
  },
  // chat 2 - short thread with reactions
  {
    id: 2001,
    chatId: 2,
    sender: 'other',
    type: 'text',
    content: 'Czy widziałeś nowe zmiany w projekcie?',
    time: createTimestamp(45),
    reactions: ['👍'],
  },
  {
    id: 2002,
    chatId: 2,
    sender: 'me',
    type: 'text',
    content: 'Tak, zrobiłem review — parę poprawek wrzucę jeszcze dziś.',
    time: createTimestamp(40),
  },

  // chat 6 - images gallery and reply
  {
    id: 6001,
    chatId: 6,
    sender: 'me',
    type: 'image',
    content: 'Galeria z eventu',
    imageUrl: 'https://picsum.photos/seed/1/800/600',
    mediaUrls: [
      'https://picsum.photos/seed/1/800/600',
      'https://picsum.photos/seed/2/800/600',
      'https://picsum.photos/seed/3/800/600'
    ],
    time: createTimestamp(0, 6),
  },
  {
    id: 6002,
    chatId: 6,
    sender: 'other',
    type: 'text',
    content: 'Super zdjęcia! Które są najlepsze?',
    time: createTimestamp(0, 5),
    isReply: true,
    replyToSender: 'me',
    replyToContentSnippet: 'Galeria z eventu',
  },

  // chat 8 - longer conversation with file and reactions
  {
    id: 8001,
    chatId: 8,
    sender: 'other',
    type: 'text',
    content: 'Masz plan na jutro?',
    time: createTimestamp(0, 10),
  },
  {
    id: 8002,
    chatId: 8,
    sender: 'me',
    type: 'text',
    content: 'Mogę wieczorem, a ty?',
    time: createTimestamp(0, 9),
    reactions: ['🔥', '🙂'],
  },
  {
    id: 8003,
    chatId: 8,
    sender: 'other',
    type: 'file',
    content: 'Plik z harmonogramem',
    fileName: 'harmonogram.xlsx',
    fileSize: 123456,
    fileUrl: 'https://file-examples.com/wp-content/uploads/2017/02/file_example_XLS_10.xls',
    time: createTimestamp(0, 8),
  },

  // chat 9 - GIF and short text
  {
    id: 9001,
    chatId: 9,
    sender: 'other',
    type: 'gif',
    content: 'Ten mem mnie rozwalił',
    imageUrl: 'https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif',
    time: createTimestamp(0, 50, 1),
  },

  // chat 10 - threaded replies
  {
    id: 10001,
    chatId: 10,
    sender: 'other',
    type: 'text',
    content: 'Kto bierze udział w prezentacji?',
    time: createTimestamp(0, 0, 4),
  },
  {
    id: 10002,
    chatId: 10,
    sender: 'me',
    type: 'text',
    content: 'Ja i Ania, mamy slajdy gotowe.',
    time: createTimestamp(0, 0, 4),
    isReply: true,
    replyToSender: 'other',
    replyToContentSnippet: 'Kto bierze udział w prezentacji?',
  },

  // chat 12 - poll result and follow-up
  {
    id: 12001,
    chatId: 12,
    sender: 'me',
    type: 'poll',
    content: 'Co preferujecie?',
    pollData: {
      question: 'Lunch: pizza czy sushi?',
      allowMultiple: false,
      allowAddOption: false,
      options: [
        { id: 1, text: 'Pizza', votes: 4, votedByMe: true },
        { id: 2, text: 'Sushi', votes: 2, votedByMe: false }
      ]
    },
    time: createTimestamp(0, 3, 2),
  },
  {
    id: 12002,
    chatId: 12,
    sender: 'other',
    type: 'text',
    content: 'Pizza wygrywa, zamawiamy!',
    time: createTimestamp(0, 2, 2),
  },

  // chat 13 - small friendly ping
  {
    id: 13001,
    chatId: 13,
    sender: 'me',
    type: 'text',
    content: 'Hej, pamiętasz o spotkaniu?',
    time: createTimestamp(0, 15, 3),
    reactions: ['👀'],
  },
  {
    id: 13002,
    chatId: 13,
    sender: 'other',
    type: 'text',
    content: 'Tak, będę za 10 minut.',
    time: createTimestamp(0, 14, 3),
  },
];

export default messages;
