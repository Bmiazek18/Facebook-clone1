<script setup lang="ts">
import { ref, computed } from 'vue'

// Import ikon
import LockIcon from 'vue-material-design-icons/Lock.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue'

// Stan
const searchQuery = ref('')
const message = ref('')
const selectedUsers = ref([1, 2])

import { users } from '@/data/users';

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users
  return users.filter(user =>
    user.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const hasSelection = computed(() => selectedUsers.value.length > 0)

const toggleUserSelection = (userId: number) => {
  const index = selectedUsers.value.indexOf(userId);
  if (index > -1) {
    selectedUsers.value.splice(index, 1);
  } else {
    selectedUsers.value.push(userId);
  }
};
</script>

<template>
  <div class="w-[500px] mx-auto relative bg-white h-[700px] flex flex-col font-sans text-gray-900 border-x border-gray-100 shadow-xl">

    <div class="px-4 py-3 text-xs text-gray-500 bg-white">
      </div>

    <div class="px-4 pb-2">
      <div class="bg-gray-100 rounded-full flex items-center h-10 px-3">
        <MagnifyIcon :size="20" class="text-gray-500 mr-2" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Szukaj osób i grup"
          class="bg-transparent w-full outline-none text-sm placeholder-gray-500 text-gray-900"
        />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto pb-32">



      <div
        v-for="user in filteredUsers"
        :key="user.id"
        class="flex items-center justify-between px-4 py-2 hover:bg-gray-50 cursor-pointer transition-colors"
        @click="toggleUserSelection(user.id)"
      >
        <div class="flex items-center gap-3">
          <div class="relative w-10 h-10 shrink-0">
            <img :src="user.avatar" class="w-full h-full rounded-full object-cover border border-gray-100"/>


          </div>
          <span class="font-semibold text-sm select-none truncate pr-2">{{ user.name }}</span>
        </div>

        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            :value="user.id"
            v-model="selectedUsers"
            class="custom-checkbox w-6 h-6 rounded-full border border-gray-300 transition-colors shrink-0"
          />
        </label>
      </div>
    </div>

    <div class="absolute bottom-0 left-0 right-0 w-full max-w-[500px] mx-auto bg-white px-4 pt-2 pb-5 border-t border-gray-100 z-10">

      <div class="relative mb-3">
        <input
          v-model="message"
          type="text"
          placeholder="Dodaj opcjonalną wiadomość tutaj..."
          class="w-full bg-gray-100 text-sm rounded-2xl py-3 pl-4 pr-10 outline-none text-gray-900 placeholder-gray-500"
        />
      </div>

      <button
        :disabled="!hasSelection"
        class="w-full py-3 rounded-xl font-semibold text-sm transition-colors flex justify-center items-center gap-2 text-white shadow-sm"
        :class="hasSelection
          ? 'bg-blue-600 hover:bg-blue-700 active:bg-blue-800'
          : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
      >
        <FacebookMessengerIcon v-if="hasSelection" :size="18" class="text-white" />
        <span>{{ hasSelection ? 'Wyślij osobno' : 'Wyślij' }}</span>
      </button>
    </div>

  </div>
</template>

<style scoped>
.custom-checkbox {
  appearance: none;
  background-color: transparent;
  margin: 0;
  cursor: pointer;
  position: relative;
  border: 2px solid #e5e7eb;
  border-radius: 4px;
}

.custom-checkbox:checked {
  background-color: #2563eb;
  border-color: #2563eb;
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
}

.custom-checkbox:focus {
  outline: none;
}
</style>
