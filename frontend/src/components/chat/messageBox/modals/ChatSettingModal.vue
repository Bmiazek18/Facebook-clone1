<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Dropdown } from 'floating-vue'

// Importy ikon - dopasowane do zrzutów ekranu
import LockOutlineIcon from 'vue-material-design-icons/LockOutline.vue';
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue';
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue';
import PencilOutlineIcon from 'vue-material-design-icons/PencilOutline.vue';
import AccountMultiplePlusOutlineIcon from 'vue-material-design-icons/AccountMultiplePlusOutline.vue';
import BellOutlineIcon from 'vue-material-design-icons/BellOutline.vue';
import AccountCancelOutlineIcon from 'vue-material-design-icons/AccountCancelOutline.vue';
import MessageOffOutlineIcon from 'vue-material-design-icons/MessageOffOutline.vue';
import EyeOutlineIcon from 'vue-material-design-icons/EyeOutline.vue';
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue';
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue';
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';

import BaseModal from '@/components/common/BaseModal.vue';
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue';
import MessangerTheme from '@/components/chat/shared/MessangerTheme.vue';
import { useConversationsStore } from '@/stores/conversations';

const props = defineProps({
  chatId: {
    type: [String, Number],
    required: true,
  },
});

const convStore = useConversationsStore();
const router = useRouter();

const showEmojiModal = ref(false);
const showThemeModal = ref(false);

const openEmojiModal = () => { showEmojiModal.value = true; };
const closeEmojiModal = () => { showEmojiModal.value = false; };

const openThemeModal = () => { showThemeModal.value = true; };
const closeThemeModal = () => { showThemeModal.value = false; };

const closeThemeModalAndSave = () => {
  const themeId = convStore.selectedThemeId as string
  convStore.setChatThemeById(props.chatId, themeId)
  const theme = convStore.themes.find((t) => t.id === themeId)
  if (theme) {
    convStore.messages.push({
      id: `local-action-${Date.now()}`,
      chatId: String(props.chatId),
      sender: 'me',
      type: 'action',
      time: Date.now(),
      content: `SYSTEM_ACTION:CHANGE_THEME:${theme.id}`,
      subType: 'CHANGE_THEME',
      payload: theme.id,
    } as any)
  }
  closeThemeModal()
}

const onEmojiSelect = (emoji: any) => {
  const native = emoji.native || '👍'
  convStore.setChatEmoji(props.chatId, native)
  convStore.messages.push({
    id: `local-action-${Date.now()}`,
    chatId: String(props.chatId),
    sender: 'me',
    type: 'action',
    time: Date.now(),
    content: `SYSTEM_ACTION:CHANGE_E:${native}`,
    subType: 'CHANGE_E',
    payload: native,
  } as any)
  closeEmojiModal()
}

const removeEmoji = () => {
  convStore.setChatEmoji(props.chatId, '👍')
  closeEmojiModal()
}

const openInMessenger = () => {
  if (props.chatId) {
    router.push({ name: 'chatMessages', params: { chatId: props.chatId } });
  }
};

// Pełna struktura zakładek z podziałem na grupy
const menuGroups = computed(() => [
  {
    items: [
      { text: 'W pełni szyfrowane', icon: LockOutlineIcon },
      { text: 'Otwórz w aplikacji Messenger', icon: FacebookMessengerIcon, action: openInMessenger },
      { text: 'Wyświetl profil', icon: AccountCircleOutlineIcon },
    ]
  },
  {
    items: [
      { text: 'Zmień motyw', isThemeIcon: true, action: openThemeModal },
      { text: 'Ikona emoji', isEmoji: true, emoji: convStore.selectedEmoji || '👍', action: openEmojiModal },
      { text: 'Nicki', icon: PencilOutlineIcon },
    ]
  },
  {
    items: [
      { text: 'Utwórz grupę', icon: AccountMultiplePlusOutlineIcon },
    ]
  },
  {
    items: [
      { text: 'Wycisz powiadomienia', icon: BellOutlineIcon },
      { text: 'Zablokuj', icon: AccountCancelOutlineIcon },
      { text: 'Ogranicz', icon: MessageOffOutlineIcon },
      { text: 'Zweryfikuj pełne szyfrowanie', icon: LockOutlineIcon },
      { text: 'Potwierdzenia odczytu', icon: EyeOutlineIcon, subtitle: 'Wł.', hasArrow: true },
    ]
  },
  {
    items: [
      { text: 'Zarchiwizuj czat', icon: CloseBoxOutlineIcon },
      { text: 'Usuń czat', icon: DeleteOutlineIcon },
      { text: 'Zgłoś', icon: AlertOutlineIcon },
    ]
  }
]);

const handleItemClick = (item: any, hide: () => void) => {
  if (item.action) {
    item.action();
  }
  // Zamknięcie dropdownu następuje po kliknięciu
  hide();
};
</script>

<template>
  <div>
    <!-- Główny Dropdown Menu -->
    <Dropdown :distance="10" placement="left-start">
      <slot />
      <template #popper="{ hide }">
        <!-- Zewnętrzny kontener odpowiada za cień, border i zaokrąglone rogi -->
        <div class="w-[340px] bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">

          <!-- Wewnętrzny kontener ma maksymalną wysokość i scrollbar -->
          <div class="max-h-[344px] overflow-y-auto py-1.5 custom-scrollbar">
            <div v-for="(group, gIndex) in menuGroups" :key="gIndex">
              <ul class="py-0.5">
                <li
                  v-for="(item, iIndex) in group.items"
                  :key="iIndex"
                  @click="handleItemClick(item, hide)"
                  class="flex items-center px-3 py-1.5 hover:bg-gray-100 active:bg-gray-200 cursor-pointer transition-colors"
                >
                  <!-- Kontener na ikonę -->
                  <div class="w-9 flex items-center justify-center mr-2 shrink-0">
                    <div v-if="item.isThemeIcon" class="w-[20px] h-[20px] rounded-full bg-[#4640f5] flex items-center justify-center">
                      <div class="w-2 h-2 rounded-full bg-white"></div>
                    </div>
                    <span v-else-if="item.isEmoji" class="text-[20px] leading-none">
                      {{ item.emoji }}
                    </span>
                    <component
                      v-else
                      :is="item.icon"
                      :size="22"
                      class="text-gray-900"
                    />
                  </div>

                  <!-- Tekst -->
                  <div class="flex-1 flex items-center justify-between min-w-0 pr-1">
                    <div class="flex flex-col">
                      <span class="text-[14px] font-medium text-gray-950 truncate leading-tight">
                        {{ item.text }}
                      </span>
                      <span v-if="item.subtitle" class="text-[12px] text-gray-500 mt-0.5 leading-tight">
                        {{ item.subtitle }}
                      </span>
                    </div>

                    <ChevronRightIcon v-if="item.hasArrow" :size="20" class="text-gray-500" />
                  </div>
                </li>
              </ul>
              <hr v-if="gIndex < menuGroups.length - 1" class="border-gray-200 mx-3 my-0.5" />
            </div>
          </div>

        </div>
      </template>
    </Dropdown>

    <!-- Modal: Zmień motyw -->
    <BaseModal v-if="showThemeModal" :title="$t('chat.zmienMotyw')" @close="closeThemeModal">
      <MessangerTheme @apply="closeThemeModalAndSave" />
    </BaseModal>

    <!-- Modal: Ikona emoji -->
    <BaseModal v-if="showEmojiModal" :title="$t('chat.ikonaEmoji')" @close="closeEmojiModal">
      <div class="bg-white flex items-center justify-between px-4 py-3 border-b border-gray-100">
        <div class="flex flex-col">
          <span class="text-[15px] font-semibold text-gray-900 leading-tight">{{ $t('chat.biezaceEmoji') }}</span>
          <div class="text-xl mt-1 leading-none">
            {{ convStore.selectedEmoji || '👍' }}
          </div>
        </div>

        <button
          @click="removeEmoji"
          class="flex items-center gap-1.5 px-4 py-2 bg-[#E4E6EB] hover:bg-gray-300 transition-colors rounded-lg font-semibold text-[13px] text-gray-900"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>{{ $t('notifications_page.delete') }}</button>
      </div>
      <LazyEmojiPicker class="w-full shadow-none border-none" @select="onEmojiSelect" />
    </BaseModal>
  </div>
</template>

<style scoped>
/* Delikatny pasek przewijania dla dropdownu */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  margin-top: 6px; /* Żeby scroll nie dotykał samego zaokrąglonego rogu */
  margin-bottom: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #d1d5db; /* Szary kciuk (gray-300) */
  border-radius: 20px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #9ca3af; /* Ciemniejszy po najechaniu (gray-400) */
}
</style>
