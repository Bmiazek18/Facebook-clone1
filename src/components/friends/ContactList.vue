<template>
  <div class="mt-2">
    <div
      v-for="contact in contacts"
      :key="contact.id"
      @click="openChatBox(contact.id)"
      class="flex items-center justify-between p-2 hover:bg-[#F2F4F7] dark:hover:bg-[#3A3B3C] rounded-lg transition duration-150 ease-in-out cursor-pointer mx-2"
    >
      <div class="flex items-center space-x-3 w-full overflow-hidden">
        <div class="relative shrink-0">
          <img
            :src="contact.avatarUrl"
            :alt="contact.name"
            class="w-10 h-10 rounded-full object-cover border border-gray-100 dark:border-gray-700"
            loading="lazy"
          />
        </div>

        <div class="flex flex-col truncate">
          <span class="text-[15px] font-medium text-[#050505] dark:text-[#E4E6EB] truncate">
            {{ contact.name }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import rawChats from '@/data/rawChats';
import { useChatStore } from '@/stores/chat';

const chatStore = useChatStore();

const contacts = computed(() => {
  return rawChats.map(chat => ({
    id: chat.id,
    name: chat.name,
    avatarUrl: chat.avatarUrl,
  }));
});

const openChatBox = (id: number) => {
  chatStore.addMessageBox(id);
};
</script>
