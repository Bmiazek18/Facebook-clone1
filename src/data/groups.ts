import type { Group } from '@/types/Group';

export const groups: Group[] = [
  {
    id: '1',
    name: 'Frontend Developers',
    description: 'A group for frontend developers to share knowledge and best practices.',
    members: 1200,
    privacy: 'public',
    images: [
        'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80'
    ],
    image: 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: '2',
    name: 'Vue.js Enthusiasts',
    description: 'A group for Vue.js enthusiasts to discuss the latest features and projects.',
    members: 2500,
    privacy: 'public',
    images: [
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80'
    ],
    image: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: '3',
    name: 'Tailwind CSS Fans',
    description: 'A group for Tailwind CSS fans to share tips and tricks.',
    members: 800,
    privacy: 'private',
    images: [
        'https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80'
    ],
    image: 'https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80'
  },
];
