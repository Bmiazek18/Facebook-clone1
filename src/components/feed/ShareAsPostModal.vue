<script setup lang="ts">
import BaseModal from '@/components/common/BaseModal.vue'
import CreatePost from '@/components/create/createPost/CreateModal.vue'
import type { Post } from '@/types/Post'
import { useI18n } from 'vue-i18n'
import { getUserById } from '@/utils/users'
import { computed } from 'vue'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    isOpen: boolean
    post?: Post
  }>(),
  {
    isOpen: false,
    post: () => ({} as any),
  }
)

const emit = defineEmits<{
  close: []
}>()

const close = () => {
  emit('close')
}

const postData = computed(() => {
  if (!props.post || !props.post.id) return null
  const author = props.post.authorId ? getUserById(props.post.authorId) : null
  return {
    id: props.post.id,
    author: {
      name: author?.name || (props.post.author ? [props.post.author.firstName, props.post.author.lastName].filter(Boolean).join(' ') : 'Unknown'),
      avatar: author?.avatar || props.post.author?.avatar || '',
      id: props.post.authorId || props.post.author?.id,
    },
    content: props.post.content,
    imageUrl: props.post.media?.[0]?.src || (props.post.media as any)?.images?.[0]?.src,
    images: (props.post.media as any)?.images || props.post.media,
    videoUrl: (props.post.media as any)?.videoUrl,
    timestamp: props.post.timestamp,
  }
})
</script>

<template>
  <BaseModal v-if="isOpen" :title="t('post.createPost')" @close="close">
    <CreatePost :shared-post="postData" @close="close" />
  </BaseModal>
</template>
