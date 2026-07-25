import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/utils/users'

export const useUsersStore = defineStore('users', () => {
  const allUsers = ref<User[]>([
    {
      id: '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce',
      name: 'Jan Wiśniewski',
      avatar: 'https://i.pravatar.cc/150?u=jan',
      bio: 'Programista z pasji',
      location: 'Warszawa',
      website: 'wisniewski.pl',
      joinDate: '2020-01-01',
      followersCount: 120,
      followingCount: 150,
      friendsCount: 200,
      postsCount: 10,
      cover: '',
      status: 'online'
    },
    {
      id: '1',
      name: 'Bartosz Miazek',
      avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
      bio: 'Developer',
      location: 'Gdańsk',
      website: 'miazek.dev',
      joinDate: '2021-01-01',
      followersCount: 500,
      followingCount: 400,
      friendsCount: 300,
      postsCount: 5,
      cover: '',
      status: 'online'
    }
  ])

  const usersMap = computed(() => {
    const map: Record<string, User> = {}
    allUsers.value.forEach((user) => {
      map[String(user.id)] = user
    })
    return map
  })

  return {
    allUsers,
    usersMap
  }
})
