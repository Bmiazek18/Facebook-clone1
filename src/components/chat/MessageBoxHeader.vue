<script setup lang="ts">
  import { ref } from 'vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import MinusIcon from 'vue-material-design-icons/Minus.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import Information from 'vue-material-design-icons/Information.vue';
defineProps<{ title: string; users: string[], boxId: string | number, hideIcons?: boolean }>();
const emit = defineEmits<{
  (e: 'back'): void
  (e: 'show-info'): void
}>();

import { useChatStore } from '@/stores/chat';



const chatStore = useChatStore()

const close = (boxId: string | number) => {
    chatStore.removeMessageBox(boxId)
};
const minimize = (boxId: string | number) => {
    chatStore.toggleMinimize(boxId)
};
import IncomingCallModal from './IncomingCallModal.vue';

const isCallIncoming = ref(false);

const onAccept = () => {
  console.log("Połączenie odebrane!");
  isCallIncoming.value = false;
  // Tutaj logika przekierowania do pokoju wideo
};

const onReject = () => {
  console.log("Połączenie odrzucone.");
  isCallIncoming.value = false;
};
</script>



<template>
  <header class="flex items-center justify-between p-3 border-b border-theme-border bg-theme-bg-secondary shadow-sm">
    <div class="flex items-center space-x-2 min-w-0">
      <!-- Przycisk wstecz na mobile -->
      <button @click="emit('back')" class="md:hidden mr-2 hover:bg-theme-hover rounded-full p-1">
        <ArrowLeftIcon :size="24" class="text-theme-text" />
      </button>

      <div class="relative shrink-0">
        <div class="w-10 h-10 rounded-full bg-theme-bg-tertiary flex items-center justify-center border-2 border-theme-border">
          <span class="text-xl text-theme-primary">🧑‍🤝‍🧑</span>
        </div>
      </div>
      <div class="flex flex-col min-w-0">
        <span class="font-bold text-theme-text text-lg truncate">{{ title }}</span>
      </div>
      <ChevronDownIcon :size="20" class="text-theme-text-secondary shrink-0 hidden md:block" />
    </div>
    <div  class="flex space-x-3 text-theme-text-secondary shrink-0">
      <PhoneIcon @click="isCallIncoming = true" :size="20" class="hover:text-theme-primary cursor-pointer" />
      <VideoOutlineIcon @click="isCallIncoming = true" :size="20" class="hover:text-theme-primary cursor-pointer" />
      <Information v-if="hideIcons" @click="emit('show-info')" :size="20" class="hover:text-theme-primary cursor-pointer" />


      <MinusIcon v-if="!hideIcons" @click="minimize(boxId)" :size="20" class="hover:text-theme-primary cursor-pointer hidden md:block" />
      <CloseIcon v-if="!hideIcons" @click="close(boxId)" :size="20" class="hover:text-theme-primary cursor-pointer hidden md:block" />
    </div>
  </header>
  <IncomingCallModal
      :is-open="isCallIncoming"
      caller-name="Wiktoria Szerszeń"
      caller-avatar="https://i.pravatar.cc/150?u=wiktoria"
      @close="isCallIncoming = false"
      @reject="onReject"
      @accept="onAccept"
    />
</template>
