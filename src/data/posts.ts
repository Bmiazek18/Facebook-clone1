export interface Post {
  id: number
  content: string
  images: string[]
  videoUrl?: string
  authorName: string
  authorAvatar: string
  date: string
  likesCount: number
  commentsCount: number
  sharesCount: number
}

export const posts: Post[] = [
  // Post z video
  {
    id: 0,
    content: 'Niesamowity zachód słońca nad oceanem �',
    images: [],
    videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
    authorName: 'Bartosz Miazek',
    authorAvatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
    date: '17 grudnia',
    likesCount: 156,
    commentsCount: 23,
    sharesCount: 8
  },
  // Post z 1 zdjęciem
  {
    id: 1,
    content: 'Piękny dzień na spacer! 🌞',
    images: ['https://picsum.photos/800/600?random=1'],
    authorName: 'Anna Kowalska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/44.jpg',
    date: '16 grudnia',
    likesCount: 24,
    commentsCount: 3,
    sharesCount: 1
  },
  // Post z 2 zdjęciami
  {
    id: 2,
    content: 'Weekendowy wypad z rodziną 👨‍👩‍👧‍👦',
    images: [
      'https://picsum.photos/800/600?random=2',
      'https://picsum.photos/800/600?random=3'
    ],
    authorName: 'Jan Nowak',
    authorAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    date: '15 grudnia',
    likesCount: 56,
    commentsCount: 8,
    sharesCount: 2
  },
  // Post z 3 zdjęciami
  {
    id: 3,
    content: 'Nowa kolekcja zdjęć z podróży ✈️',
    images: [
      'https://picsum.photos/800/800?random=4',
      'https://picsum.photos/800/600?random=5',
      'https://picsum.photos/800/600?random=6'
    ],
    authorName: 'Jan Nowak',
    authorAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    date: '15 grudnia',
    likesCount: 112,
    commentsCount: 15,
    sharesCount: 5
  },
  // Post z 4 zdjęciami
  {
    id: 4,
    content: 'Cztery pory roku w jednym poście 🍂🌸☀️❄️',
    images: [
      'https://picsum.photos/800/600?random=7',
      'https://picsum.photos/800/600?random=8',
      'https://picsum.photos/800/600?random=9',
      'https://picsum.photos/800/600?random=10'
    ],
    authorName: 'Katarzyna Wiśniewska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/28.jpg',
    date: '14 grudnia',
    likesCount: 89,
    commentsCount: 12,
    sharesCount: 3
  },
  // Post z 5 zdjęciami
  {
    id: 5,
    content: 'Album z imprezy urodzinowej 🎂🎉',
    images: [
      'https://picsum.photos/800/600?random=11',
      'https://picsum.photos/800/600?random=12',
      'https://picsum.photos/800/600?random=13',
      'https://picsum.photos/800/600?random=14',
      'https://picsum.photos/800/600?random=15'
    ],
    authorName: 'Piotr Kowalczyk',
    authorAvatar: 'https://randomuser.me/api/portraits/men/45.jpg',
    date: '13 grudnia',
    likesCount: 234,
    commentsCount: 45,
    sharesCount: 12
  },
  // Post z 7 zdjęciami (więcej niż 5)
  {
    id: 6,
    content: 'Cały album z wakacji w Grecji 🇬🇷🏖️',
    images: [
      'https://picsum.photos/800/600?random=16',
      'https://picsum.photos/800/600?random=17',
      'https://picsum.photos/800/600?random=18',
      'https://picsum.photos/800/600?random=19',
      'https://picsum.photos/800/600?random=20',
      'https://picsum.photos/800/600?random=21',
      'https://picsum.photos/800/600?random=22'
    ],
    authorName: 'Maria Lewandowska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/65.jpg',
    date: '12 grudnia',
    likesCount: 567,
    commentsCount: 89,
    sharesCount: 34
  },
  // Post z 10 zdjęciami
  {
    id: 7,
    content: 'Mega album - 10 najlepszych zdjęć tego roku! 📸',
    images: [
      'https://picsum.photos/800/600?random=23',
      'https://picsum.photos/800/600?random=24',
      'https://picsum.photos/800/600?random=25',
      'https://picsum.photos/800/600?random=26',
      'https://picsum.photos/800/600?random=27',
      'https://picsum.photos/800/600?random=28',
      'https://picsum.photos/800/600?random=29',
      'https://picsum.photos/800/600?random=30',
      'https://picsum.photos/800/600?random=31',
      'https://picsum.photos/800/600?random=32'
    ],
    authorName: 'Tomasz Zieliński',
    authorAvatar: 'https://randomuser.me/api/portraits/men/67.jpg',
    date: '10 grudnia',
    likesCount: 1234,
    commentsCount: 156,
    sharesCount: 78
  },
  // Post z 1 zdjęciem (dla porównania)
  {
    id: 8,
    content: 'Kolejne piękne ujęcie 📷',
    images: ['https://picsum.photos/800/600?random=33'],
    authorName: 'Ewa Kamińska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/33.jpg',
    date: '9 grudnia',
    likesCount: 45,
    commentsCount: 6,
    sharesCount: 1
  },
  // Post z 2 zdjęciami
  {
    id: 9,
    content: 'Przed i po remoncie 🏠',
    images: [
      'https://picsum.photos/800/600?random=34',
      'https://picsum.photos/800/600?random=35'
    ],
    authorName: 'Michał Wójcik',
    authorAvatar: 'https://randomuser.me/api/portraits/men/52.jpg',
    date: '8 grudnia',
    likesCount: 78,
    commentsCount: 23,
    sharesCount: 4
  },
  // Post z 3 zdjęciami
  {
    id: 10,
    content: 'Trzy wspaniałe momenty 💫',
    images: [
      'https://picsum.photos/800/800?random=36',
      'https://picsum.photos/800/600?random=37',
      'https://picsum.photos/800/600?random=38'
    ],
    authorName: 'Aleksandra Dąbrowska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/55.jpg',
    date: '7 grudnia',
    likesCount: 167,
    commentsCount: 34,
    sharesCount: 9
  }
]

export const getPostById = (id: number): Post | undefined => {
  return posts.find(post => post.id === id)
}

export const getPostImage = (postId: number, imageIndex: number): string | undefined => {
  const post = getPostById(postId)
  if (post && post.images[imageIndex]) {
    return post.images[imageIndex]
  }
  return undefined
}
