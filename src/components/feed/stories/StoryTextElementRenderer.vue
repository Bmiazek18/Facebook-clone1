<script setup lang="ts">
import { computed } from 'vue'
import type { TextElement } from '@/types/StoryElement'
import { extractMentionUserIds } from '@/utils/storyMetadata'
import { getUserById } from '@/utils/users'

const props = defineProps<{
  element: TextElement
}>()

const router = useRouter()

const mentionedUserId = computed(() => {
  const ids = extractMentionUserIds(props.element.content || '')
  if (ids[0]) return ids[0]

  // Fallback for plain @Name text (legacy fabric mentions)
  const match = props.element.content?.match(/@([\w\s.-]+)/)
  if (!match?.[1]) return null
  return null
})

const navigateToUserProfile = () => {
  if (!mentionedUserId.value) return
  // Warm cache
  getUserById(mentionedUserId.value)
  router.push(`/profile/${mentionedUserId.value}`)
}
</script>

<template>
  <div
    v-if="mentionedUserId"
    class="absolute cursor-pointer z-30 border-2 border-transparent hover:border-white/50 rounded transition"
    :style="{
      width: '100%',
      height: '100%',
      backgroundColor: 'transparent',
    }"
    @click.stop="navigateToUserProfile"
  ></div>
</template>
