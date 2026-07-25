import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Group } from '@/types/Group'

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref<Group[]>([
    {
      id: '1',
      name: 'Frontend Developers',
      description: 'A group for frontend developers to share knowledge and best practices.',
      members: 1200,
      privacy: 'public',
      images: [
        'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80',
      ],
      image:
        'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80',
      lastActive: '12 min temu',
    },
    {
      id: '2',
      name: 'Vue.js Enthusiasts',
      description: 'A group for Vue.js enthusiasts to discuss the latest features and projects.',
      members: 2500,
      privacy: 'public',
      images: [
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80',
      ],
      image:
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80',
      lastActive: '30 min temu',
    },
    {
      id: '3',
      name: 'Tailwind CSS Fans',
      description: 'A group for Tailwind CSS fans to share tips and tricks.',
      members: 800,
      privacy: 'private',
      images: [
        'https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80',
      ],
      image:
        'https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80',
      lastActive: '2 godz. temu',
    },
    {
      id: '4',
      name: 'Kolegium Sędziów BOZPN',
      description: 'Oficjalna grupa Kolegium Sędziów BOZPN.',
      members: 140,
      privacy: 'private',
      images: [
        'https://ui-avatars.com/api/?name=KS&background=3F6212&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=KS&background=3F6212&color=fff&size=128&font-size=0.4',
      lastActive: '2 dni temu',
    },
    {
      id: '5',
      name: 'Absurdalnie Tanie Loty',
      description: 'Grupa dzieląca się informacjami o najtańszych lotach i okazjach podróżniczych.',
      members: 95000,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4',
      lastActive: '37 min temu',
    },
    {
      id: '6',
      name: 'Reprezentacja Polski Diabetyków w Futsalu',
      description: 'Wsparcie i aktualności dotyczące Reprezentacji Polski Diabetyków w Futsalu.',
      members: 320,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=PL&background=DC2626&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=PL&background=DC2626&color=fff&size=128&font-size=0.4',
      lastActive: 'tydzień temu',
    },
    {
      id: '7',
      name: 'Praca dla początkujących programistów: Staż, Junior',
      description: 'Oferty pracy, porady, staże dla początkujących programistów.',
      members: 42000,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=IT&background=F3F4F6&color=111&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=IT&background=F3F4F6&color=111&size=128&font-size=0.4',
      lastActive: '6 dni temu',
    },
    {
      id: '8',
      name: 'Piłkarski Świat',
      description: 'Dyskusje o piłce nożnej z całego świata.',
      members: 15400,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=PS&background=E5E7EB&color=111&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=PS&background=E5E7EB&color=111&size=128&font-size=0.4',
      lastActive: '46 min temu',
    },
    {
      id: '9',
      name: 'Grupa Piłkarzyki',
      description: 'Lokalna grupa graczy w piłkarzyki stołowe.',
      members: 58,
      privacy: 'private',
      images: [
        'https://ui-avatars.com/api/?name=GP&background=1F2937&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=GP&background=1F2937&color=fff&size=128&font-size=0.4',
      lastActive: '22 godz. temu',
    },
    {
      id: '10',
      name: 'BILETY // POLSKA SIATKÓWKA // INFORMACJE',
      description: 'Kupno, sprzedaż biletów oraz informacje na temat polskiej siatkówki.',
      members: 2400,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=VP&background=1E3A8A&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=VP&background=1E3A8A&color=fff&size=128&font-size=0.4',
      lastActive: '5 dni temu',
    },
    {
      id: '11',
      name: 'EA SPORTS FC 26 - POLSKA',
      description: 'Polska społeczność gry EA SPORTS FC 26.',
      members: 31200,
      privacy: 'public',
      images: [
        'https://ui-avatars.com/api/?name=EA&background=450a0a&color=fff&size=128&font-size=0.4',
      ],
      image: 'https://ui-avatars.com/api/?name=EA&background=450a0a&color=fff&size=128&font-size=0.4',
      lastActive: '5 min temu',
    },
  ])

  const getGroupById = (id: string) => {
    return groups.value.find((group) => group.id === id)
  }

  const addGroup = (group: Omit<Group, 'id'>) => {
    const newId = (groups.value.length + 1).toString()
    const newGroup: Group = {
      ...group,
      id: newId,
    }
    groups.value.push(newGroup)
    return newGroup
  }

  return {
    groups,
    getGroupById,
    addGroup,
  }
})
