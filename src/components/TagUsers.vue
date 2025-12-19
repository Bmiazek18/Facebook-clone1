<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'; // Zakładam, że masz tę ikonę, jeśli nie - użyj SVG poniżej
import { getAllUsers } from '@/data/users';
import type { User } from '@/data/users';
import HoverScrollbar from './HoverScrollbar.vue';

const emit = defineEmits<{
  (e: 'confirm', users: User[]): void;
  (e: 'back'): void;
}>();

const props = defineProps<{ initialSelected?: User[] }>();
const allUsers = ref<User[]>(getAllUsers());
const selectedUsers = ref<User[]>(props.initialSelected ? [...props.initialSelected] : []);
const searchQuery = ref('');

// Synchronizuj selectedUsers gdy initialSelected się zmienia
watch(() => props.initialSelected, (newVal) => {
  selectedUsers.value = newVal ? [...newVal] : [];
});

const isSelected = (user: User) => selectedUsers.value.some(u => u.id === user.id);

// Filtrowanie użytkowników po nazwie
const filteredUsers = computed(() => {
  if (!searchQuery.value) return allUsers.value;
  return allUsers.value.filter(u =>
    u.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const toggleUser = (user: User) => {
  if (isSelected(user)) {
    selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
  } else {
    selectedUsers.value.push(user);
    // Opcjonalnie: wyczyść szukanie po wybraniu
    // searchQuery.value = '';
  }
};

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

const confirmSelection = () => {
  emit('confirm', selectedUsers.value);
};
</script>

<template>
  <div class=" flex flex-col h-[600px] w-full  mx-auto">



    <HoverScrollbar class="flex-1 overflow-y-auto  custom-scrollbar">

      <div class="flex items-center gap-3 mb-6">
        <div class="flex-1 relative">
          <div class="absolute inset-y-0 left-3 flex items-center pointer-events-none text-gray-500">
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
        <h3 class="text-xs font-bold text-gray-500 tracking-wide mb-3 uppercase">Oznaczeni</h3>
        <div class="border border-gray-200 rounded-lg p-2 min-h-[40px] flex flex-wrap gap-2">
          <div
            v-for="user in selectedUsers"
            :key="user.id"
            class="flex items-center bg-blue-50 text-blue-600 px-2 py-1.5 rounded-[4px] cursor-default"
          >
            <span class="font-semibold text-sm mr-1">{{ user.name }}</span>
            <button @click="removeUser(user)" class="hover:text-blue-800 flex items-center">
              <CloseIcon :size="16" />
            </button>
          </div>
        </div>
      </div>

      <div>
        <h3 class="text-xs font-bold text-gray-500 tracking-wide mb-3 uppercase">Propozycje</h3>
        <div class="flex flex-col gap-1">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            @click="toggleUser(user)"
            class="flex items-center p-2 rounded-lg cursor-pointer hover:bg-gray-100 transition-colors"
          >
            <div class="relative mr-3">
               <img :src="user.avatar" :alt="user.name" class="w-10 h-10 rounded-full object-cover border border-gray-100" />
            </div>

            <div class="flex-1 flex flex-col justify-center">
              <span class="text-[15px] font-semibold text-black leading-tight">{{ user.name }}</span>
              <span class="text-[13px] text-gray-500 leading-tight mt-0.5">Znajomy</span>
            </div>

            <div v-if="isSelected(user)" class="w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center text-white">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
             </div>
          </div>
        </div>
      </div>

    </HoverScrollbar>
  </div>
</template>

<style scoped>
/* Ukrycie standardowego scrollbara dla estetyki, opcjonalnie */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 20px;
}
</style>
