<script setup lang="ts">
import { ref, computed } from 'vue';
import BaseModal from './BaseModal.vue';
import { useConversationsStore } from '@/stores/conversations';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/auth';
import type { Post } from '@/types/Post';
import type { Conversation } from '@/types/Message';
import Close from 'vue-material-design-icons/Close.vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';

const props = defineProps<{
  isOpen: boolean;
  post: Post | null;
}>();

const emit = defineEmits(['close', 'share']);

const conversationsStore = useConversationsStore();
const { chats: allConversations } = storeToRefs(conversationsStore); // Use conversations from conversationsStore

const authStore = useAuthStore();
const searchTerm = ref('');
const messageContent = ref('');
const selectedRecipients = ref<Conversation[]>([]);

const availableConversations = computed(() => {
  if (!searchTerm.value) {
    return allConversations.value; // Use .value for ref
  }
  return allConversations.value.filter(conv => // Use .value for ref
    conv.participants.some(p =>
      p.name.toLowerCase().includes(searchTerm.value.toLowerCase())
    )
  );
});

const toggleRecipient = (conversation: Conversation) => {
  const index = selectedRecipients.value.findIndex(r => r.id === conversation.id);
  if (index > -1) {
    selectedRecipients.value.splice(index, 1);
  } else {
    selectedRecipients.value.push(conversation);
  }
};

const isRecipientSelected = (conversation: Conversation) => {
  return selectedRecipients.value.some(r => r.id === conversation.id);
};

const handleShare = () => {
  if (selectedRecipients.value.length > 0 && props.post) {
    const recipientIds = selectedRecipients.value.map(conv => conv.id); // Emit only IDs
    emit('share', recipientIds, messageContent.value);
  }
};

const closeModal = () => {
  selectedRecipients.value = [];
  messageContent.value = '';
  searchTerm.value = '';
  emit('close');
};
</script>

<template>
  <BaseModal :title="$t('sharePost.title')" :isOpen="isOpen" @close="closeModal">
    <div v-if="post" class="flex flex-col h-full max-h-[90vh] bg-theme-bg-secondary rounded-lg overflow-hidden">
      <!-- Post Preview (simplified) -->
      <div class="p-4 border-b border-theme-border flex items-center gap-3">
        <img :src="post.authorAvatar" alt="Author Avatar" class="w-10 h-10 rounded-full object-cover" />
        <div>
          <p class="font-semibold text-theme-text">{{ post.authorName }}</p>
          <p class="text-sm text-theme-text-secondary line-clamp-1">{{ post.content }}</p>
        </div>
      </div>

      <!-- Search and Recipient List -->
      <div class="flex-1 p-4 overflow-y-auto">
        <div class="relative mb-4">
          <Magnify :size="20" class="absolute left-3 top-1/2 -translate-y-1/2 text-theme-text-secondary" />
          <input
            type="text"
            v-model="searchTerm"
            :placeholder="$t('sharePost.searchPlaceholder')"
            class="w-full pl-10 pr-4 py-2 rounded-full bg-theme-bg-third text-theme-text focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="space-y-2">
          <div
            v-for="conversation in availableConversations"
            :key="conversation.id"
            @click="toggleRecipient(conversation)"
            class="flex items-center gap-3 p-2 rounded-lg cursor-pointer hover:bg-theme-hover transition-colors"
            :class="{ 'bg-blue-100 dark:bg-blue-900/40': isRecipientSelected(conversation) }"
          >
            <img
              :src="conversation.participants.find(p => p.id !== authStore.user?.id)?.avatar || conversation.participants[0].avatar"
              alt="Recipient Avatar"
              class="w-8 h-8 rounded-full object-cover"
            />
            <p class="flex-1 font-medium text-theme-text">
              {{ conversation.participants.find(p => p.id !== authStore.user?.id)?.name || conversation.participants[0].name }}
            </p>
            <input
              type="checkbox"
              :checked="isRecipientSelected(conversation)"
              class="form-checkbox h-5 w-5 text-blue-600 rounded-full"
            />
          </div>
        </div>
      </div>

      <!-- Message Input and Share Button -->
      <div class="p-4 border-t border-theme-border">
        <textarea
          v-model="messageContent"
          :placeholder="$t('sharePost.messagePlaceholder')"
          rows="3"
          class="w-full p-2 rounded-lg bg-theme-bg-third text-theme-text focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3 resize-none"
        ></textarea>
        <button
          @click="handleShare"
          :disabled="selectedRecipients.length === 0"
          class="w-full py-2 rounded-lg font-semibold transition-colors"
          :class="{
            'bg-blue-500 text-white hover:bg-blue-600': selectedRecipients.length > 0,
            'bg-gray-300 text-gray-500 cursor-not-allowed': selectedRecipients.length === 0,
          }"
        >
          {{ $t('sharePost.shareButton') }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>