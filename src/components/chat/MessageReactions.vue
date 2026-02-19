<script setup lang="ts">
import { ref, computed } from 'vue';
import ReplyIcon from 'vue-material-design-icons/Reply.vue';
import VerticalDotsIcon from 'vue-material-design-icons/DotsVertical.vue';
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import BaseModal from '../common/BaseModal.vue';
import ShareModal from './ShareModal.vue';

const props = defineProps<{
  messageId: number | string;
  reactions: Record<string, number> | undefined;
  isMe: boolean;
}>();

const emit = defineEmits<{
  (e: 'add-reaction', payload: { messageId: number | string; emoji: string }): void;
  (e: 'reply', messageId: number | string): void;
  (e: 'confirm-unsend', payload: { messageId: number | string; mode: 'everyone' | 'me' }): void;
  (e: 'forward', messageId: number | string): void;
  (e: 'pin', messageId: number | string): void;
}>();

const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏'];
const isUnsendModalOpen = ref(false);
const selectedUnsendMode = ref<'everyone' | 'me'>('everyone');
const isShareModalOpen = ref(false);
const isReactionsDropdownOpen = ref(false);
const isActionsDropdownOpen = ref(false);

const isAnyDropdownOpen = computed(
  () => isReactionsDropdownOpen.value || isActionsDropdownOpen.value
);

const selectReaction = (emoji: string) => {
  emit('add-reaction', { messageId: props.messageId, emoji });
};

const handleConfirmUnsend = () => {
  emit('confirm-unsend', {
    messageId: props.messageId,
    mode: selectedUnsendMode.value
  });
  isUnsendModalOpen.value = false;
};
</script>

<template>
  <div
    class="flex items-center gap-1 mx-1 transition-opacity"
    :class="[isMe ? 'flex-row-reverse' : '', isAnyDropdownOpen ? 'opacity-100' : 'opacity-0 group-hover:opacity-100']"
  >
    <VDropdown
      :placement="isMe ? 'top-end' : 'top-start'"
      :distance="10"
      :triggers="['click']"
       v-model:shown="isReactionsDropdownOpen"
    >
      <button class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors">
        <EmoticonHappyOutlineIcon :size="18" class="text-black" />
      </button>
      <template #popper>
        <div class="flex  p-1.5 ">
          <span
            v-for="emoji in reactionsList"
            :key="emoji"
            class="cursor-pointer text-[25px] hover:scale-125 transition-transform px-1"
            @click.stop="selectReaction(emoji)"
          >
            {{ emoji }}
          </span>
        </div>
      </template>
    </VDropdown>

    <button
      @click="emit('reply', props.messageId)"
      class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors"
    >
      <ReplyIcon :size="18" class="text-black" />
    </button>

    <VDropdown
      :placement="isMe ? 'top-end' : 'top-start'"
      :distance="10"
      :triggers="['click']"
      v-model:shown="isActionsDropdownOpen"
    >
      <button class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors">
        <VerticalDotsIcon :size="18" class="text-black" />
      </button>

      <template #popper="{ hide }">
        <div class="flex flex-col min-w-[160px] p-1.5 bg-white rounded-xl shadow-2xl border border-gray-50">
          <button
            @click="isUnsendModalOpen = true; hide()"
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            Cofnij wysłanie
          </button>
          <button
            @click="isShareModalOpen = true; hide()"
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            Prześlij
          </button>
          <button
            @click="emit('pin', props.messageId)"
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            Przypnij
          </button>
        </div>
      </template>
    </VDropdown>

   <BaseModal v-if="isUnsendModalOpen" @close="isUnsendModalOpen = false" title="W przypadku kogo chcesz cofnąć wysłanie tej wiadomości?" :showCloseButton="false">
        <div class=" w-[700px] overflow-hidden">
          <div class="p-6 space-y-6">

            <label class="flex items-start gap-4 cursor-pointer group" @click="selectedUnsendMode = 'everyone'">
              <div class="mt-1">
                <div
                  class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all"
                  :class="selectedUnsendMode === 'everyone' ? 'border-blue-600' : 'border-gray-300'"
                >
                  <div v-if="selectedUnsendMode === 'everyone'" class="w-2.5 h-2.5 bg-blue-600 rounded-full"></div>
                </div>
              </div>
              <div class="flex-1">
                <h3 class="font-bold text-gray-900 text-[16px]">Cofnij wysłanie do wszystkich</h3>
                <p class="text-gray-500 text-[14px] leading-relaxed mt-1">
                  Wysłanie tej wiadomości zostanie cofnięte w odniesieniu do wszystkich uczestników czatu. Inni mogli już ją zobaczyć.
                </p>
              </div>
            </label>

            <label class="flex items-start gap-4 cursor-pointer group" @click="selectedUnsendMode = 'me'">
              <div class="mt-1">
                <div
                  class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all"
                  :class="selectedUnsendMode === 'me' ? 'border-blue-600' : 'border-gray-300'"
                >
                  <div v-if="selectedUnsendMode === 'me'" class="w-2.5 h-2.5 bg-blue-600 rounded-full"></div>
                </div>
              </div>
              <div class="flex-1">
                <h3 class="font-bold text-gray-900 text-[16px]">Cofnij wysłanie dla Ciebie</h3>
                <p class="text-gray-500 text-[14px] leading-relaxed mt-1">
                  Spowoduje to usunięcie wiadomości z Twoich urządzeń. Inni uczestnicy nadal będą mogli ją zobaczyć.
                </p>
              </div>
            </label>
          </div>

          <div class="flex justify-end gap-2 p-4 border-t border-gray-50">
            <button
              @click="isUnsendModalOpen = false"
              class="px-4 py-2 text-[15px] font-bold text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
            >
              Anuluj
            </button>
            <button
              @click="handleConfirmUnsend"
              class="px-6 py-2 text-[15px] font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-md transition-colors"
            >
              Usuń
            </button>
          </div>
        </div>
     </BaseModal>
     <BaseModal v-if="isShareModalOpen" title="Przekaż " @close="isShareModalOpen = false">
        <ShareModal />
      </BaseModal>
  </div>
</template>

<style scoped>
/* Wyłączenie domyślnych strzałek floating-vue dla czystszego wyglądu */
:deep(.v-popper__arrow-outer),
:deep(.v-popper__arrow-inner) {
  display: none;
}




</style>
