export interface Event {
  id: string;
  userId: string;
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
    userId: '1',
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
    userId: '1',
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
  {
    id: '3',
    userId: '2',
    name: 'Art Exhibition',
    title: 'Art Exhibition',
    startDate: '2026-04-10',
    startTime: '10:00',
    endDate: '2026-04-12',
    endTime: '18:00',
    type: 'offline',
    privacy: 'public',
    description: 'A three-day art exhibition showcasing the work of local artists. Discover a diverse range of styles and mediums, from paintings and sculptures to photography and mixed media. This is a great opportunity to support local talent, meet the artists, and purchase unique pieces for your collection. The exhibition will also feature live music, food trucks, and hands-on activities for all ages. Join us for a weekend of creativity, inspiration, and community.',
    images: [
      'https://images.unsplash.com/photo-1547891654-e66ed711b934?w=600&h=400&fit=crop',
      'https://images.unsplash.com/photo-1506806782133-e95cde1a4dba?w=600&h=400&fit=crop',
      'https://images.unsplash.com/photo-1536924940846-222ab34d490c?w=600&h=400&fit=crop',
    ],
    location: 'City Art Gallery',
    locationName: 'City Art Gallery',
    address: '123 Art Street, Gdańsk, Poland',
    showGuestList: true,
    hosts: ['City Art Gallery', 'Local Artists Association'],
    date: '10 kwietnia o 10:00',
    responses: 350,
    guestsGoing: 275,
    guestsInterested: 75,
    coordinates: [54.3520, 18.6466], // Gdańsk, Poland
  },
  {
    id: '4',
    userId: '1',
    name: 'Music Festival',
    title: 'Music Festival',
    startDate: '2026-07-15',
    startTime: '12:00',
    endDate: '2026-07-17',
    endTime: '23:00',
    type: 'offline',
    privacy: 'public',
    description: 'A three-day music festival with a lineup of international and local bands. Enjoy a weekend of live music, food, and fun. The festival will feature multiple stages with a variety of genres, from rock and pop to electronic and folk. There will also be a wide selection of food and drink vendors, as well as a market with local crafts and merchandise. This is the perfect event for music lovers of all ages. Get your tickets now and get ready to dance the weekend away!',
    images: [
      'https://images.unsplash.com/photo-151452525253161-7a46d19cd819?w=600&h=400&fit=crop',
      'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=600&h=400&fit=crop',
      'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=600&h=400&fit=crop',
    ],
    location: 'Gdańsk Festival Grounds',
    locationName: 'Gdańsk Festival Grounds',
    address: 'Gdańsk, Poland',
    showGuestList: true,
    hosts: ['Gdańsk Events', 'Music Unlimited'],
    date: '15 lipca o 12:00',
    responses: 5000,
    guestsGoing: 4500,
    guestsInterested: 500,
    coordinates: [54.3520, 18.6466], // Gdańsk, Poland
  },
];

export const getEventById = (id: string): Event | undefined => {
  return events.find((event) => event.id === id);
};