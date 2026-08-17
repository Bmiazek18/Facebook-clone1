<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'

import type { User } from '@/utils/users'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import { useFriendSearch } from '@/composables/shared/useFriendSearch'

const createPostStore = useCreatePostStore()
const { users: allUsers, isLoading, loadSuggestions, searchUsers } = useFriendSearch()

const selectedUsers = ref<User[]>([...createPostStore.postData.taggedUsers])
const searchQuery = ref('')

const isSelected = (user: User) =>
  selectedUsers.value.some((u) => String(u.id) === String(user.id))

onMounted(() => {
  loadSuggestions()
})

watch(searchQuery, (newVal) => {
  searchUsers(newVal)
})

const filteredUsers = computed(() => allUsers.value)

const toggleUser = (user: User) => {
  if (isSelected(user)) {
    selectedUsers.value = selectedUsers.value.filter((u) => String(u.id) !== String(user.id))
  } else {
    selectedUsers.value.push(user)
  }
}

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter((u) => String(u.id) !== String(user.id))
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

      <div v-if="selectedUsers.length" class="flex flex-wrap gap-2 mb-4 px-1">
        <div
          v-for="user in selectedUsers"
          :key="String(user.id)"
          class="flex items-center gap-1.5 bg-[#E7F3FF] text-[#1877F2] rounded-full pl-1 pr-2 py-1"
        >
          <img :src="user.avatar" class="w-6 h-6 rounded-full object-cover" :alt="user.name" />
          <span class="text-[13px] font-semibold">{{ user.name }}</span>
          <button @click="removeUser(user)" class="hover:bg-black/5 rounded-full p-0.5">
            <CloseIcon :size="14" />
          </button>
        </div>
      </div>

      <div v-if="isLoading" class="text-center text-gray-500 py-8 text-sm">Ładowanie...</div>

      <div v-else class="flex flex-col">
        <div
          v-for="user in filteredUsers"
          :key="String(user.id)"
          @click="toggleUser(user)"
          class="flex items-center gap-3 px-2 py-2 hover:bg-gray-100 rounded-lg cursor-pointer"
        >
          <img :src="user.avatar" class="w-10 h-10 rounded-full object-cover" :alt="user.name" />
          <span class="flex-1 font-medium text-[15px] text-gray-900">{{ user.name }}</span>
          <div
            class="w-6 h-6 rounded-full border-2 flex items-center justify-center"
            :class="
              isSelected(user)
                ? 'bg-[#1877F2] border-[#1877F2]'
                : 'border-gray-400 bg-white'
            "
          >
            <svg
              v-if="isSelected(user)"
              class="w-3.5 h-3.5 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>
        </div>
        <div v-if="!filteredUsers.length" class="text-center text-gray-500 py-8 text-sm">
          Brak wyników
        </div>
      </div>
    </HoverScrollbar>
  </div>
</template>
