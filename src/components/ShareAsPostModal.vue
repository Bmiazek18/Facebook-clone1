<script setup lang="ts">
import BaseModal from './BaseModal.vue';
import CreatePost from './CreatePost.vue';
import type { Post } from '@/types/Post';

defineProps<{
  isOpen: boolean;
  post: Post;
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
      :author-name="post.authorName"
      :author-avatar="post.authorAvatar"
      @publish="handlePublish"
    />
  </BaseModal>
</template>