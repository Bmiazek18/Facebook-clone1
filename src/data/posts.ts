import type { Post, ReactionType } from '@/types/Post';
import { getUserById } from './users';

// Helper to get reaction count (not used directly in data, but for conceptual understanding)
const countReactions = (post: Post) => {
    let count = 0;
    if (post.reactions) {
        Object.values(post.reactions).forEach(userIds => {
            count += userIds ? userIds.length : 0;
        });
    }
    return count;
};

// Available user IDs for reactions (from users.json)
const availableUserIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// Helper to generate a random subset of user IDs for reactions
const getRandomUserIds = (maxUsers: number, includeCurrentUser = false): number[] => {
    let ids = [...availableUserIds];
    if (includeCurrentUser && !ids.includes(1)) {
        ids.unshift(1); // Ensure current user is in the list if specified
    }
    
    // Shuffle and pick a random number of unique IDs
    for (let i = ids.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [ids[i], ids[j]] = [ids[j], ids[i]];
    }
    const numToPick = Math.floor(Math.random() * maxUsers) + 1; // At least 1 reaction
    return ids.slice(0, numToPick);
};

// Helper to assign random reactions
const generateRandomReactions = (): Partial<Record<ReactionType, number[]>> => {
    const reactionTypes: ReactionType[] = ['like', 'love', 'haha', 'wow', 'sad', 'angry'];
    const reactions: Partial<Record<ReactionType, number[]>> = {};

    // Ensure 'like' reaction is always present and often includes current user
    reactions['like'] = getRandomUserIds(availableUserIds.length / 2, true);

    // Add other random reactions
    reactionTypes.forEach(type => {
        if (type !== 'like' && Math.random() > 0.6) { // 40% chance to have other reactions
            reactions[type] = getRandomUserIds(availableUserIds.length / 3);
        }
    });
    
    // Ensure all reaction arrays are unique and contain actual IDs
    for (const type in reactions) {
        if (reactions[type as ReactionType]) {
            reactions[type as ReactionType] = Array.from(new Set(reactions[type as ReactionType]));
        }
    }

    return reactions;
};


export const posts: Post[] = [
  {
    id: '100',
    content: 'Hello everyone! I hope you are all having a wonderful day. The weather is absolutely beautiful today, and I decided to take a long walk in the park. It was so peaceful and relaxing. How are you spending your weekend? 🌞🌳',
    authorId: 100, // Assuming 100 is a valid user for testing international posts
    date: '4 stycznia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=100', altText: 'Beautiful park view' },
        ]
    },
    context: {
        privacy: 'public'
    },
    stats: {
        comments: 8,
        shares: 2,
    },
    reactions: generateRandomReactions(),
    comments: [],
    detectedLanguage: 'en',
  },
  {
    id: '101',
    content: 'Guten Morgen! Heute ist ein wunderschöner Tag. Ich freue mich sehr auf das Wochenende mit meiner Familie. Wir werden zusammen wandern gehen und die Natur genießen. Was habt ihr für Pläne? 🏔️🌲',
    authorId: 101, // Assuming 101 is a valid user for testing international posts
    date: '4 stycznia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=101', altText: 'Mountain hiking view' },
            { src: 'https://picsum.photos/800/600?random=102', altText: 'Nature landscape' },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 5, shares: 1 },
    reactions: generateRandomReactions(),
    comments: [],
    detectedLanguage: 'de',
  },
  {
    id: '102',
    content: 'Bonjour à tous! Quelle magnifique journée ensoleillée! Je viens de terminer un délicieux déjeuner au café près de la Seine. Paris est vraiment magique en cette saison. Profitez bien de votre journée! ☕🥐🗼',
    authorId: 102, // Assuming 102 is a valid user for testing international posts
    date: '4 stycznia',
    timestamp: Date.now(),
    media: {},
    context: { privacy: 'public' },
    stats: { comments: 12, shares: 3 },
    reactions: generateRandomReactions(),
    comments: [],
    detectedLanguage: 'fr',
  },
  {
    id: '0',
    content: 'Niesamowity zachód słońca nad oceanem 🌅 #zachod',
    authorId: 1, // Bartosz Miazek
    date: '17 grudnia',
    timestamp: Date.now(),
    media: {
        videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
    },
    context: { privacy: 'public' },
    stats: { comments: 23, shares: 8 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '1',
    content: 'Piękny dzień na spacer! 🌞',
    authorId: 2, // Anna Kowalska
    date: '16 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
          {
            src: 'https://picsum.photos/800/600?random=1',
            altText: 'A beautiful day for a walk',
            tags: [
                  {
                    id: '1',
                    x: 50,
                    y: 50,
                    user: { id: 2, name: 'Anna Kowalska', imageUrl: '...', commonFriends: 0, isFriend: true }, // Simplified for tag
                  },
                ]
          },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 3, shares: 1 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '2',
    content: 'Weekendowy wypad z rodziną 👨‍👩‍👧‍👦',
    authorId: 3, // Jan Nowak
    date: '15 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=2', altText: 'Family weekend trip' },
            { src: 'https://picsum.photos/800/600?random=3', altText: 'Family weekend trip' },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 8, shares: 2 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '3',
    content: 'Nowa kolekcja zdjęć z podróży ✈️',
    authorId: 3, // Jan Nowak
    date: '15 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/800?random=4', altText: 'Travel photo' },
            { src: 'https://picsum.photos/800/600?random=5', altText: 'Travel photo' },
            { src: 'https://picsum.photos/800/600?random=6', altText: 'Travel photo' },
        ]
    },
    context: { privacy: 'public' },
    stats: { comments: 15, shares: 5 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '4',
    content: 'Cztery pory roku w jednym poście 🍂🌸☀️❄️',
    authorId: 4, // Katarzyna Wiśniewska
    date: '14 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=7', altText: 'Four seasons in one post' },
            { src: 'https://picsum.photos/800/600?random=8', altText: 'Four seasons in one post' },
            { src: 'https://picsum.photos/800/600?random=9', altText: 'Four seasons in one post' },
            { src: 'https://picsum.photos/800/600?random=10', altText: 'Four seasons in one post' },
        ]
    },
    context: { privacy: 'public' },
    stats: { comments: 12, shares: 3 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '5',
    content: 'Album z imprezy urodzinowej 🎂🎉',
    authorId: 5, // Piotr Kowalczyk
    date: '13 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=11', altText: 'Birthday party album' },
            { src: 'https://picsum.photos/800/600?random=12', altText: 'Birthday party album' },
            { src: 'https://picsum.photos/800/600?random=13', altText: 'Birthday party album' },
            { src: 'https://picsum.photos/800/600?random=14', altText: 'Birthday party album' },
            { src: 'https://picsum.photos/800/600?random=15', altText: 'Birthday party album' },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 45, shares: 12 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '6',
    content: 'Cały album z wakacji w Grecji 🇬🇷🏖️',
    authorId: 6, // Maria Lewandowska
    date: '12 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
          { src: 'https://picsum.photos/800/600?random=16', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=17', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=18', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=19', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=20', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=21', altText: 'Greece vacation album' },
          { src: 'https://picsum.photos/800/600?random=22', altText: 'Greece vacation album' },
        ]
    },
    context: { privacy: 'public' },
    stats: { comments: 89, shares: 34 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '7',
    content: 'Mega album - 10 najlepszych zdjęć tego roku! 📸',
    authorId: 7, // Tomasz Zieliński
    date: '10 grudnia',
    timestamp: Date.now(),
    media: {
        images: Array.from({length: 10}, (_, i) => ({ src: `https://picsum.photos/800/600?random=${23+i}`, altText: 'Top photo' }))
    },
    context: { privacy: 'public' },
    stats: { comments: 156, shares: 78 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '8',
    content: 'Kolejne piękne ujęcie 📷',
    authorId: 8, // Ewa Kamińska
    date: '9 grudnia',
    timestamp: Date.now(),
    media: {
        images: [{ src: 'https://picsum.photos/800/600?random=33', altText: 'Another beautiful shot' }]
    },
    context: { privacy: 'friends' },
    stats: { comments: 6, shares: 1 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '9',
    content: 'Przed i po remoncie 🏠',
    authorId: 9, // Michał Wójcik
    date: '8 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/600?random=34', altText: 'Before' },
            { src: 'https://picsum.photos/800/600?random=35', altText: 'After' },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 23, shares: 4 },
    reactions: generateRandomReactions(),
    comments: [],
  },
  {
    id: '10',
    content: 'Trzy wspaniałe momenty 💫',
    authorId: 10, // Aleksandra Dąbrowska
    date: '7 grudnia',
    timestamp: Date.now(),
    media: {
        images: [
            { src: 'https://picsum.photos/800/800?random=36', altText: 'Moment 1' },
            { src: 'https://picsum.photos/800/600?random=37', altText: 'Moment 2' },
            { src: 'https://picsum.photos/800/600?random=38', altText: 'Moment 3' },
        ]
    },
    context: { privacy: 'friends' },
    stats: { comments: 34, shares: 9 },
    reactions: generateRandomReactions(),
    comments: [],
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
  if (post && post.media.images && post.media.images[imageIndex]) {
    return post.media.images[imageIndex].src
  }
  return undefined
}