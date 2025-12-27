import type { User } from './users';
import { getUserById } from './users';

import type { Person } from '@/types/Person';

import type { Post } from '@/types/Post';

const anna = getUserById(2)

const userToPerson = (user: User): Person => ({
  id: user.id,
  name: user.name,
  imageUrl: user.avatar,
  commonFriends: user.mutualFriendsCount || 0,
  isFriend: true,
})

export const posts: Post[] = [
  // Post z video
  {
    id: '0',
    content: 'Niesamowity zachód słońca nad oceanem 🌅 #zachod',
    images: [],
    videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
    authorName: 'Bartosz Miazek',
    authorAvatar:
      'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
    authorId: 1,
    date: '17 grudnia',
    likesCount: 156,
    commentsCount: 23,
    sharesCount: 8,
    isLiked: false,
    reactionCount: 156,
    commentCount: 23,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 1 zdjęciem
  {
    id: '1',
    content: 'Piękny dzień na spacer! 🌞',
    images: [
      {
        src: 'https://picsum.photos/800/600?random=1',
        altText: 'A beautiful day for a walk',
        tags: anna
          ? [
              {
                id: '1',
                x: 50,
                y: 50,
                user: userToPerson(anna),
              },
            ]
          : [],
      },
    ],
    authorName: 'Anna Kowalska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/44.jpg',
    authorId: 2,
    date: '16 grudnia',
    likesCount: 24,
    commentsCount: 3,
    sharesCount: 1,
    isLiked: false,
    reactionCount: 24,
    commentCount: 3,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 2 zdjęciami
  {
    id: '2',
    content: 'Weekendowy wypad z rodziną 👨‍👩‍👧‍👦',
    images: [
      { src: 'https://picsum.photos/800/600?random=2', altText: 'Family weekend trip' },
      { src: 'https://picsum.photos/800/600?random=3', altText: 'Family weekend trip' },
    ],
    authorName: 'Jan Nowak',
    authorAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    authorId: 3,
    date: '15 grudnia',
    likesCount: 56,
    commentsCount: 8,
    sharesCount: 2,
    isLiked: false,
    reactionCount: 56,
    commentCount: 8,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 3 zdjęciami
  {
    id: '3',
    content: 'Nowa kolekcja zdjęć z podróży ✈️',
    images: [
      { src: 'https://picsum.photos/800/800?random=4', altText: 'Travel photo' },
      { src: 'https://picsum.photos/800/600?random=5', altText: 'Travel photo' },
      { src: 'https://picsum.photos/800/600?random=6', altText: 'Travel photo' },
    ],
    authorName: 'Jan Nowak',
    authorAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    authorId: 3,
    date: '15 grudnia',
    likesCount: 112,
    commentsCount: 15,
    sharesCount: 5,
    isLiked: false,
    reactionCount: 112,
    commentCount: 15,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 4 zdjęciami
  {
    id: '4',
    content: 'Cztery pory roku w jednym poście 🍂🌸☀️❄️',
    images: [
      { src: 'https://picsum.photos/800/600?random=7', altText: 'Four seasons in one post' },
      { src: 'https://picsum.photos/800/600?random=8', altText: 'Four seasons in one post' },
      { src: 'https://picsum.photos/800/600?random=9', altText: 'Four seasons in one post' },
      { src: 'https://picsum.photos/800/600?random=10', altText: 'Four seasons in one post' },
    ],
    authorName: 'Katarzyna Wiśniewska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/28.jpg',
    authorId: 4,
    date: '14 grudnia',
    likesCount: 89,
    commentsCount: 12,
    sharesCount: 3,
    isLiked: false,
    reactionCount: 89,
    commentCount: 12,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 5 zdjęciami
  {
    id: '5',
    content: 'Album z imprezy urodzinowej 🎂🎉',
    images: [
      { src: 'https://picsum.photos/800/600?random=11', altText: 'Birthday party album' },
      { src: 'https://picsum.photos/800/600?random=12', altText: 'Birthday party album' },
      { src: 'https://picsum.photos/800/600?random=13', altText: 'Birthday party album' },
      { src: 'https://picsum.photos/800/600?random=14', altText: 'Birthday party album' },
      { src: 'https://picsum.photos/800/600?random=15', altText: 'Birthday party album' },
    ],
    authorName: 'Piotr Kowalczyk',
    authorAvatar: 'https://randomuser.me/api/portraits/men/45.jpg',
    authorId: 5,
    date: '13 grudnia',
    likesCount: 234,
    commentsCount: 45,
    sharesCount: 12,
    isLiked: false,
    reactionCount: 234,
    commentCount: 45,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 7 zdjęciami (więcej niż 5)
  {
    id: '6',
    content: 'Cały album z wakacji w Grecji 🇬🇷🏖️',
    images: [
      { src: 'https://picsum.photos/800/600?random=16', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=17', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=18', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=19', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=20', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=21', altText: 'Greece vacation album' },
      { src: 'https://picsum.photos/800/600?random=22', altText: 'Greece vacation album' },
    ],
    authorName: 'Maria Lewandowska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/65.jpg',
    authorId: 6,
    date: '12 grudnia',
    likesCount: 567,
    commentsCount: 89,
    sharesCount: 34,
    isLiked: false,
    reactionCount: 567,
    commentCount: 89,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 10 zdjęciami
  {
    id: '7',
    content: 'Mega album - 10 najlepszych zdjęć tego roku! 📸',
    images: [
      { src: 'https://picsum.photos/800/600?random=23', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=24', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=25', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=26', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=27', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=28', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=29', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=30', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=31', altText: 'Top 10 photos of the year' },
      { src: 'https://picsum.photos/800/600?random=32', altText: 'Top 10 photos of the year' },
    ],
    authorName: 'Tomasz Zieliński',
    authorAvatar: 'https://randomuser.me/api/portraits/men/67.jpg',
    authorId: 7,
    date: '10 grudnia',
    likesCount: 1234,
    commentsCount: 156,
    sharesCount: 78,
    isLiked: false,
    reactionCount: 1234,
    commentCount: 156,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 1 zdjęciem (dla porównania)
  {
    id: '8',
    content: 'Kolejne piękne ujęcie 📷',
    images: [{ src: 'https://picsum.photos/800/600?random=33', altText: 'Another beautiful shot' }],
    authorName: 'Ewa Kamińska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/33.jpg',
    authorId: 8,
    date: '9 grudnia',
    likesCount: 45,
    commentsCount: 6,
    sharesCount: 1,
    isLiked: false,
    reactionCount: 45,
    commentCount: 6,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 2 zdjęciami
  {
    id: '9',
    content: 'Przed i po remoncie 🏠',
    images: [
      { src: 'https://picsum.photos/800/600?random=34', altText: 'Before and after renovation' },
      { src: 'https://picsum.photos/800/600?random=35', altText: 'Before and after renovation' },
    ],
    authorName: 'Michał Wójcik',
    authorAvatar: 'https://randomuser.me/api/portraits/men/52.jpg',
    authorId: 9,
    date: '8 grudnia',
    likesCount: 78,
    commentsCount: 23,
    sharesCount: 4,
    isLiked: false,
    reactionCount: 78,
    commentCount: 23,
    comments: [],
    timestamp: Date.now(),
  },
  // Post z 3 zdjęciami
  {
    id: '10',
    content: 'Trzy wspaniałe momenty 💫',
    images: [
      { src: 'https://picsum.photos/800/800?random=36', altText: 'Three wonderful moments' },
      { src: 'https://picsum.photos/800/600?random=37', altText: 'Three wonderful moments' },
      { src: 'https://picsum.photos/800/600?random=38', altText: 'Three wonderful moments' },
    ],
    authorName: 'Aleksandra Dąbrowska',
    authorAvatar: 'https://randomuser.me/api/portraits/women/55.jpg',
    authorId: 10,
    date: '7 grudnia',
    likesCount: 167,
    commentsCount: 34,
    sharesCount: 9,
    isLiked: false,
    reactionCount: 167,
    commentCount: 34,
    comments: [],
    timestamp: Date.now(),
  },
]

export const getPostById = (id: string): Post | undefined => {
  return posts.find(post => post.id === id)
}

export const getPostImage = (
  postId: string,
  imageIndex: number,
): string | undefined => {
  const post = getPostById(postId)
  if (post && post.images[imageIndex]) {
    return post.images[imageIndex].src
  }
  return undefined
}
