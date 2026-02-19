<script setup lang="ts">
import { ref, computed } from 'vue';
import { useChatThemeStore, type Theme } from '@/stores/chatTheme';
import { storeToRefs } from 'pinia';
import { useChatStore } from '@/stores/chat';

import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import MinusIcon from 'vue-material-design-icons/Minus.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import Information from 'vue-material-design-icons/Information.vue';
import IncomingCallModal from './IncomingCallModal.vue';
import ChatSettingModal from './ChatSettingModal.vue'; // Import the new modal component

const props = defineProps<{
  title: string;
  subtitle?: string; // np. "Aktywna 9 min temu"
  avatarUrl?: string; // Link do zdjęcia (jeśli jest)
  users: string[];
  boxId: string | number;
  hideIcons?: boolean;
  themes?: Theme;
}>();

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'show-info'): void
}>();

const { selectedTheme } = storeToRefs(useChatThemeStore());
const activeTheme = computed(() => props.themes || selectedTheme.value);

const chatStore = useChatStore()

const close = (boxId: string | number) => {
    chatStore.removeMessageBox(boxId)
};
const minimize = (boxId: string | number) => {
    chatStore.toggleMinimize(boxId)
};

const isCallIncoming = ref(false);
const isChatSettingModalOpen = ref(false); // Ref to control the visibility of ChatSettingModal

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
  <header
   class="flex items-center justify-between px-3 py-2.5 rounded-t-xl border-b border-black/5"
    :class="{'h-[48px]': !hideIcons, 'h-[64px]': hideIcons}"
    :style="{ backgroundColor: activeTheme.headerColor || 'transparent' }"
  >
    <div class="flex items-center gap-2 min-w-0">

      <button v-tooltip.top="'Wróć do listy'" @click="emit('back')" class="md:hidden shrink-0 hover:bg-black/5 rounded-full p-1 transition-colors">
        <ArrowLeftIcon :size="hideIcons ? 32 : 24" :fillColor="activeTheme.iconColor" />
      </button>

      <div :class="{' w-8 h-8': !hideIcons, ' w-10 h-10': hideIcons}" class="relative shrink-0 w-8 h-8 rounded-full overflow-hidden bg-black/5 flex items-center justify-center border border-black/10">
        <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" class="w-full h-full object-cover" />
        <span v-else class="text-xl">🧑‍🤝‍🧑</span>
      </div>
  <ChatSettingModal
    :is-open="isChatSettingModalOpen"
    @close="isChatSettingModalOpen = false"
    :chatId="boxId"
  >
      <div   class="flex items-center min-w-0 hover:bg-black/5 py-1 cursor-pointer rounded-lg" :class="{'gap-1.5': !hideIcons, 'gap-2': hideIcons}">
        <div class="flex flex-col min-w-0 leading-tight">
          <span class="font-semibold truncate" :class="{'text-[15px]': !hideIcons, 'text-[16px]': hideIcons}" :style="{ color: activeTheme.headerTextColor || '#111827' }">
            {{ title }}
          </span>
          <span class="truncate" :class="{'text-[12px]': !hideIcons, 'text-[13px]': hideIcons}" :style="{ color: activeTheme.headerTextColor, opacity: 0.6 }">
            {{ subtitle || 'Aktywna 34 min temu' }}
          </span>
        </div>

        <button v-if="!hideIcons" class="shrink-0 p-1 rounded-full hover:bg-black/5 transition-colors flex items-center justify-center">
          <ChevronDownIcon :size="20" :fillColor="activeTheme.primaryColor || activeTheme.iconColor" />
        </button>
      </div>
      </ChatSettingModal>
    </div>

    <div class="flex items-center shrink-0" :class="{'space-x-1': !hideIcons, 'space-x-3': hideIcons}">

      <button v-tooltip.top="'Rozpocznij połączenie głosowe'" @click="isCallIncoming = true" class="opacity-50 hover:opacity-100 transition-opacity flex items-center justify-center">
        <PhoneIcon :size="hideIcons ? 24 : 18" :fillColor="activeTheme.headerTextColor || activeTheme.iconColor" />
      </button>

      <button v-tooltip.top="'Rozpocznij połączenie wideo'" @click="isCallIncoming = true" class="opacity-50 hover:opacity-100 transition-opacity flex items-center justify-center">
        <VideoOutlineIcon :size="hideIcons ? 24 : 18" :fillColor="activeTheme.headerTextColor || activeTheme.iconColor" />
      </button>

      <button v-tooltip.top="'Informacje o czacie'" v-if="hideIcons" @click="emit('show-info')" class="hover:opacity-80 transition-opacity flex items-center justify-center">
        <Information :size="hideIcons ? 24 : 20" :fillColor="activeTheme.primaryColor || activeTheme.iconColor" />
      </button>

      <button v-tooltip.top="'Minimalizuj'" v-if="!hideIcons" @click="minimize(boxId)" class="hidden md:flex items-center justify-center hover:opacity-80 transition-opacity rounded-full hover:bg-black/5 p-0.5">
        <MinusIcon :size="22" :fillColor="activeTheme.primaryColor || activeTheme.iconColor" />
      </button>

      <button v-tooltip.top="'Zamknij czat'" v-if="!hideIcons" @click="close(boxId)" class="hidden md:flex items-center justify-center hover:opacity-80 transition-opacity rounded-full hover:bg-black/5 p-0.5">
        <CloseIcon :size="22" :fillColor="activeTheme.primaryColor || activeTheme.iconColor" />
      </button>
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

<style scoped>
button {
  outline: none;
}
</style>
