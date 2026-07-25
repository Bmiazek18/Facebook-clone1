<script setup lang="ts">
import { computed } from 'vue'
import type { TextElement } from '@/types/StoryElement'
import { getAllUsers } from '@/utils/users'

const props = defineProps<{
  element: TextElement
}>()

const router = useRouter()

// Find the first mention and compute its corresponding user ID
const firstMentionedUser = computed(() => {
  const mentionRegex = /@(\w+)/ // Find the first @username
  const match = props.element.content.match(mentionRegex)
  console.log(match)
  if (match) {
    const username = match[1]
    const user = getAllUsers().find((u) => String(u.id) === username)
    console.log(user)
    return user
  }
  return null
})

const navigateToUserProfile = () => {
  console.log('sss')
  if (firstMentionedUser.value) {
    router.push(`/profile/${firstMentionedUser.value.id}`)
  }
}
</script>

<template>
  <div
    class="absolute cursor-pointer"
    :style="{
      ...element.styles,
      width: '50px',
      height: '50px',
      backgroundColor: 'transparent',
      // Optionally show a border on hover for debugging/usability
      border: '2px solid rgba(255, 255, 255, 0.5)',
    }"
    @click="navigateToUserProfile"
  ></div>
</template>
