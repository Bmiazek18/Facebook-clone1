export interface Event {
  id: string;
  name: string;
  title?: string;
  startDate: string;
  startTime?: string;
  endDate?: string;
  endTime?: string;
  type: 'online' | 'offline';
  privacy: 'public' | 'private';
  description?: string;
  images: string[];
  location?: string;
  locationName?: string;
  address?: string;
  showGuestList?: boolean;
  hosts?: string[];
  date?: string;
  responses?: number;
  guestsGoing?: number;
  guestsInterested?: number;
  coordinates?: [number, number];
  frequency?: string;
}

export const events: Event[] = [
  // Example event
  {
    id: '1',
    name: 'Community Meetup',
    title: 'Community Meetup',
    startDate: '2026-01-23',
    startTime: '19:00',
    endDate: '2026-01-23',
    endTime: '21:00',
    type: 'offline',
    privacy: 'public',
    description: 'A casual meetup for community members to connect and share ideas. Join us for an evening of networking, fun conversations, and making new friends. Whether you\'re looking to expand your social circle or simply enjoy spending time with like-minded people, this event is perfect for you. We\'ll have refreshments, games, and lots of opportunities to meet and connect with others. All are welcome! Come as you are and bring your positive energy. We look forward to seeing you there and creating lasting memories together.',
    images: [
      // Group of friends laughing in a park
      'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=600&h=400&fit=crop',
      // Picnic setup / outdoors
      'https://images.unsplash.com/photo-1552664730-d307ca884978?w=600&h=400&fit=crop',
      // People toasting/cheering
      'https://images.unsplash.com/photo-1561489413-985b06da5bee?w=600&h=400&fit=crop',
    ],
    location: 'Central Park',
    locationName: 'Central Park',
    address: 'New York, USA',
    showGuestList: true,
    hosts: ['Community Organizers', 'Local Leaders', 'Volunteers'],
    date: '23 stycznia o 19:00',
    responses: 245,
    guestsGoing: 189,
    guestsInterested: 56,
    coordinates: [40.7829, -73.9654], // Central Park, New York
  },
  {
    id: '2',
    name: 'Tech Conference 2026',
    title: 'Tech Conference 2026',
    startDate: '2026-03-20',
    startTime: '09:00',
    endDate: '2026-03-22',
    endTime: '17:00',
    type: 'online',
    privacy: 'public',
    description: 'Annual tech conference featuring speakers from leading companies. This year\'s conference brings together industry experts, innovators, and technology enthusiasts from around the world. Over three days, we\'ll explore cutting-edge technologies, emerging trends, and future innovations. Our agenda includes keynote presentations from renowned tech leaders, interactive workshops, panel discussions on hot topics, and networking sessions. Whether you\'re a developer, entrepreneur, designer, or business professional, there\'s something for everyone. Join us for inspiring talks, practical insights, and the chance to connect with thousands of fellow tech enthusiasts. Don\'t miss this incredible opportunity to learn, grow, and be part of the tech community.',
    images: [
      // Auditorium / Conference stage
      'https://images.unsplash.com/photo-1544531586-fde5298cdd40?w=600&h=400&fit=crop',
      // Coding / Developer working
      'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600&h=400&fit=crop',
      // Abstract Technology / Network
      'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600&h=400&fit=crop',
    ],
    location: 'Online',
    locationName: 'Online Event',
    address: 'Available worldwide',
    frequency: 'Nigdy',
    showGuestList: true,
    hosts: ['Tech Innovators', 'Global Speakers', 'Industry Leaders'],
    date: '20 marca o 09:00',
    responses: 1250,
    guestsGoing: 987,
    guestsInterested: 263,
    coordinates: [54.371661, 18.619082], // Gdańsk, Poland
  },
];
