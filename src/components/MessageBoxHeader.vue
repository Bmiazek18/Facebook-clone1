<script setup lang="ts">
  import { ref } from 'vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import MinusIcon from 'vue-material-design-icons/Minus.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';

defineProps<{ title: string; users: string[], boxId: string | number }>();
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
import IncomingCallModal from '../components/IncomingCallModal.vue';

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
  <header class="flex items-center justify-between p-3 border-b border-gray-200 bg-white shadow-sm">
    <div class="flex items-center space-x-2 min-w-0">
      <!-- Przycisk wstecz na mobile -->
      <button @click="emit('back')" class="md:hidden mr-2 hover:bg-gray-100 rounded-full p-1">
        <ArrowLeftIcon :size="24" class="text-gray-600" />
      </button>

      <div class="relative shrink-0">
        <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center border-2 border-gray-200">
          <span class="text-xl text-blue-500">🧑‍🤝‍🧑</span>
        </div>
      </div>
      <div class="flex flex-col min-w-0">
        <span class="font-bold text-gray-800 text-lg truncate">{{ title }}</span>
      </div>
      <ChevronDownIcon :size="20" class="text-gray-400 shrink-0 hidden md:block" />
    </div>
    <div class="flex space-x-3 text-gray-500 shrink-0">
      <PhoneIcon @click="isCallIncoming = true" :size="20" class="hover:text-purple-600 cursor-pointer" />
      <VideoOutlineIcon @click="isCallIncoming = true" :size="20" class="hover:text-purple-600 cursor-pointer" />
      <InformationOutlineIcon @click="emit('show-info')" :size="20" class="hover:text-purple-600 cursor-pointer lg:hidden" />
      <MinusIcon @click="minimize(boxId)" :size="20" class="hover:text-purple-600 cursor-pointer hidden md:block" />
      <CloseIcon @click="close(boxId)" :size="20" class="hover:text-purple-600 cursor-pointer hidden md:block" />
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
