<script setup lang="ts">
import BaseModal from './BaseModal.vue';
import CreatePost from './CreatePost.vue';
import type { PostData } from '@/types/StoryElement';

defineProps<{
  isOpen: boolean;
  post: PostData;
}>();

const emit = defineEmits<{
  close: [];
  share: [comment: string];
}>();

const handlePublish = (content: string) => {
  emit('share', content);
};

const close = () => {
  emit('close');
};
</script>

<template>
  <BaseModal
    v-if="isOpen"
    title="Utwórz post"
    @close="close"
  >
    <CreatePost
      :shared-post="post"
      @publish="handlePublish"
    />
  </BaseModal>
</template>