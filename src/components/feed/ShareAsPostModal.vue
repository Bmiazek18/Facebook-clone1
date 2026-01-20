<script setup lang="ts">
import BaseModal from '@/components/common/BaseModal.vue';
import CreatePost from '@/components/createPost/CreateModal.vue';
import type { Post } from '@/types/Post';
import { useI18n } from 'vue-i18n';
import { getUserById } from '@/data/users';
import { computed } from 'vue';

const { t } = useI18n();

const props = defineProps<{
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

const postData = computed(() => {
    const author = getUserById(props.post.authorId);
    return {
        id: props.post.id,
        author: {
            name: author?.name || 'Unknown',
            avatar: author?.avatar || '',
            id: props.post.authorId
        },
        content: props.post.content,
        imageUrl: props.post.media?.images?.[0]?.src,
        images: props.post.media?.images,
        videoUrl: props.post.media?.videoUrl,
        timestamp: props.post.timestamp
    };
});
</script>

<template>
  <BaseModal
    v-if="isOpen"
    :title="t('post.createPost')"
    @close="close"
  >
    <CreatePost
      :shared-post="postData"
      @publish="handlePublish"
    />
  </BaseModal>
</template>
