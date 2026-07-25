<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'

import type { User } from '@/utils/users'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'



const createPostStore = useCreatePostStore()
const authStore = useAuthStore()

const allUsers = ref<User[]>([])
const selectedUsers = ref<User[]>([...createPostStore.postData.taggedUsers])
const searchQuery = ref('')
const isLoading = ref(false)

const isSelected = (user: User) => selectedUsers.value.some((u) => u.id === user.id)

const loadSuggestions = async () => {
  isLoading.value = true
  try {
    const response = await fetch('http://localhost:8080/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        query: `
          query GetFriendSuggestions($currentUserId: ID!) {
            getFriendSuggestions(currentUserId: $currentUserId) {
              userId
              user {
                id
                firstName
                lastName
                avatarId
              }
            }
          }
        `,
        variables: {
          currentUserId: String(authStore.currentUserId),
        },
      }),
    })

    const resJson = await response.json()
    if (resJson.data?.getFriendSuggestions) {
      allUsers.value = resJson.data.getFriendSuggestions.map((s: any) => {
        const u = s.user || {}
        const fullName = `${u.firstName || ''} ${u.lastName || ''}`.trim() || `User ${s.userId}`
        const avatarUrl = u.avatarId
          ? `http://localhost:9000/avatars/${u.avatarId}`
          : `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`

        return {
          id: Number(s.userId),
          name: fullName,
          avatar: avatarUrl,
        } as unknown as User
      })
    }
  } catch (e) {
    console.warn('Failed to load friend suggestions from userService:', e)
  } finally {
    isLoading.value = false
  }
}

const searchUsers = async (queryText: string) => {
  if (!queryText.trim()) {
    await loadSuggestions()
    return
  }
  isLoading.value = true
  try {
    const response = await fetch('http://localhost:8080/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        query: `
          query SearchUsers($query: String!, $currentUserId: ID) {
            searchUsers(query: $query, currentUserId: $currentUserId) {
              id
              firstName
              lastName
              avatarId
            }
          }
        `,
        variables: {
          query: queryText,
          currentUserId: String(authStore.currentUserId),
        },
      }),
    })

    const resJson = await response.json()
    if (resJson.data?.searchUsers) {
      allUsers.value = resJson.data.searchUsers.map((u: any) => {
        const fullName = `${u.firstName || ''} ${u.lastName || ''}`.trim() || `User ${u.id}`
        const avatarUrl = u.avatarId
          ? `http://localhost:9000/avatars/${u.avatarId}`
          : `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`

        return {
          id: Number(u.id),
          name: fullName,
          avatar: avatarUrl,
        } as unknown as User
      })
    }
  } catch (e) {
    console.warn('Failed to search users from userService:', e)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadSuggestions()
})

watch(searchQuery, (newVal) => {
  searchUsers(newVal)
})

const filteredUsers = computed(() => {
  return allUsers.value
})

const toggleUser = (user: User) => {
  if (isSelected(user)) {
    selectedUsers.value = selectedUsers.value.filter((u) => u.id !== user.id)
  } else {
    selectedUsers.value.push(user)
  }
}

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter((u) => u.id !== user.id)
}

const confirmSelection = () => {
  createPostStore.postData.taggedUsers = selectedUsers.value
  createPostStore.navigateBack()
}
</script>

<template>
  <div class="flex flex-col h-[600px] w-full mx-auto">
    <HoverScrollbar class="flex-1 overflow-y-auto custom-scrollbar">
      <div class="flex items-center gap-3 mb-6">
        <div class="flex-1 relative">
          <div
            class="absolute inset-y-0 left-3 flex items-center pointer-events-none text-gray-500"
          >
            <MagnifyIcon :size="20" />
          </div>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Szukaj"
            class="w-full bg-gray-100 text-black placeholder-gray-500 rounded-full py-2 pl-10 pr-4 focus:outline-none focus:ring-1 focus:ring-gray-300"
          />
        </div>
        <button
          @click="confirmSelection"
          class="text-blue-600 font-semibold hover:text-blue-800 text-[15px]"
        >
          Gotowe
        </button>
      </div>

      <div v-if="selectedUsers.length > 0" class="mb-6">
        <h3 class="text-xs font-semibold text-gray-500 tracking-wide mb-3 uppercase">Oznaczeni</h3>
        <div class="border border-gray-200 rounded-lg p-2 min-h-10 flex flex-wrap gap-2">
          <div
            v-for="user in selectedUsers"
            :key="user.id"
            class="flex items-center bg-blue-50 text-blue-600 px-2 py-1.5 rounded-sm cursor-default"
          >
            <span class="font-medium text-sm mr-1">{{ user.name }}</span>
            <button
              @click="removeUser(user)"
              class="hover:bg-blue-200 flex items-center cursor-pointer"
            >
              <CloseIcon :size="16" />
            </button>
          </div>
        </div>
      </div>

      <div>
        <h3 class="text-xs font-semibold text-theme-text-secondary tracking-wide mb-3 uppercase">
          Propozycje
        </h3>
        <div class="flex flex-col gap-1">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            @click="toggleUser(user)"
            class="flex items-center p-2 rounded-lg cursor-pointer hover:bg-theme-hover transition-colors"
          >
            <div class="relative mr-3">
              <img
                :src="user.avatar"
                :alt="user.name"
                class="w-10 h-10 rounded-full object-cover border border-gray-100"
              />
            </div>

            <div class="flex-1 flex flex-col justify-center">
              <span class="text-[15px] font-medium text-theme-text leading-tight">{{
                user.name
              }}</span>
              <span class="text-[13px] text-gray-500 text-theme-text-secondary leading-tight mt-0.5"
                >Znajomy</span
              >
            </div>

            <div
              v-if="isSelected(user)"
              class="w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center text-white"
            >
              <svg
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="4"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </HoverScrollbar>
  </div>
</template>
