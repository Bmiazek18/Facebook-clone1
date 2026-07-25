<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Dropdown } from 'floating-vue'
import LockIcon from 'vue-material-design-icons/Lock.vue';
import MessageOutlineIcon from 'vue-material-design-icons/MessageOutline.vue';
import SnowflakeIcon from 'vue-material-design-icons/Snowflake.vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue';
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue';
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
  // Tutaj docelowo logika zapisu motywu przez Apollo Mutation
  closeThemeModal();
};

const onEmojiSelect = (emoji: any) => {
  // Poprawiono z emoji.i na emoji.native (standard dla bibliotek emoji)
  convStore.selectedEmoji = emoji.native || '👍';
  closeEmojiModal();
};

const removeEmoji = () => {
  convStore.selectedEmoji = '👍'; // Domyślna wartość lub null
  closeEmojiModal();
};

const openInMessenger = () => {
  if (props.chatId) {
    router.push({ name: 'chatMessages', params: { chatId: props.chatId } });
  }
};

// Dynamiczne grupy menu (emoji reaguje na stan w store)
const menuGroups = computed(() => [
  {
    items: [
      { text: 'W pełni szyfrowane', icon: LockIcon },
      { text: 'Otwórz w Messengerze', icon: MessageOutlineIcon, action: openInMessenger },
    ]
  },
  {
    items: [
      { text: 'Zmień motyw', icon: SnowflakeIcon, customStyle: 'bg-blue-50 text-blue-400 rounded-full p-0.5', action: openThemeModal },
      { text: 'Ikona emoji', isEmoji: true, emoji: convStore.selectedEmoji || '👍', action: openEmojiModal },
      { text: 'Nicki', icon: PencilIcon },
    ]
  },
  {
    items: [
      { text: 'Zarchiwizuj czat', icon: CloseBoxOutlineIcon },
      { text: 'Usuń czat', icon: DeleteOutlineIcon },
    ]
  }
]);

const handleItemClick = (item: any, hide: () => void) => {
  if (item.action) {
    item.action();
  }
  hide();
};
</script>

<template>
  <div>
    <!-- Główny Dropdown Menu -->
    <Dropdown :distance="10" placement="left-start">
      <slot />
      <template #popper="{ hide }">
        <div class="w-60 bg-white py-1 overflow-hidden">
          <div v-for="(group, gIndex) in menuGroups" :key="gIndex">
            <ul class="py-0.5">
              <li
                v-for="(item, iIndex) in group.items"
                :key="iIndex"
                @click="handleItemClick(item, hide)"
                class="flex items-center px-3.5 py-2 hover:bg-gray-50 active:bg-gray-100 cursor-pointer transition-colors"
              >
                <div class="w-8 flex items-center justify-start mr-1">
                  <template v-if="item.isEmoji">
                    <span class="text-xl leading-none">{{ item.emoji }}</span>
                  </template>
                  <template v-else>
                    <component
                      :is="item.icon"
                      :size="20"
                      class="text-gray-700"
                      :class="item.customStyle"
                    />
                  </template>
                </div>

                <span class="text-[14px] font-medium text-gray-900 truncate">
                  {{ item.text }}
                </span>
              </li>
            </ul>
            <hr v-if="gIndex < menuGroups.length - 1" class="border-gray-100 ml-12 my-0.5" />
          </div>
        </div>
      </template>
    </Dropdown>

    <!-- Modal: Zmień motyw -->
    <BaseModal v-if="showThemeModal" title="Zmień motyw" @close="closeThemeModal">
      <MessangerTheme @apply="closeThemeModalAndSave" />
    </BaseModal>

    <!-- Modal: Ikona emoji -->
    <BaseModal v-if="showEmojiModal" title="Ikona emoji" @close="closeEmojiModal">
      <div class="bg-white flex items-center justify-between px-4 py-3 border-b border-gray-100">
        <div class="flex flex-col">
          <span class="text-[15px] font-semibold text-gray-900 leading-tight">Bieżące emoji</span>
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
          </svg>
          Usuń
        </button>
      </div>
      <LazyEmojiPicker class="w-full shadow-none border-none" @select="onEmojiSelect" />
    </BaseModal>
  </div>
</template>
