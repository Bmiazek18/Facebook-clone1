<script setup lang="ts">
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import MinusIcon from 'vue-material-design-icons/Minus.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';

defineProps<{ title: string; users: string[], boxId: string | number }>();
import { useChatStore } from '@/stores/chat';



const chatStore = useChatStore()

const close = (boxId: string | number) => {
    chatStore.removeMessageBox(boxId)
};
const minimize = (boxId: string | number) => {
    chatStore.toggleMinimize(boxId)
};
</script>
<template>
  <header class="flex items-center justify-between p-3 border-b border-gray-200 bg-white shadow-sm">
    <div class="flex items-center space-x-2 min-w-0">
      <div class="relative shrink-0">
        <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center border-2 border-gray-200">
          <span class="text-xl text-blue-500">🧑‍🤝‍🧑</span>
        </div>
      </div>
      <div class="flex flex-col min-w-0">
        <span class="font-bold text-gray-800 text-lg truncate">{{ title }}</span>
      </div>
      <ChevronDownIcon :size="20" class="text-gray-400 shrink-0" />
    </div>
    <div class="flex space-x-3 text-gray-500 shrink-0">
      <PhoneIcon :size="20" class="hover:text-purple-600 cursor-pointer" />
      <VideoOutlineIcon :size="20" class="hover:text-purple-600 cursor-pointer" />
      <MinusIcon @click="minimize(boxId)" :size="20" class="hover:text-purple-600 cursor-pointer" />
      <CloseIcon @click="close(boxId)" :size="20" class="hover:text-purple-600 cursor-pointer" />
    </div>
  </header>
</template>
