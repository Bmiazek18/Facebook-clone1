<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStoriesStore } from '@/composables/feed/useAppState'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'

const props = defineProps<{
  user: {
    id: number | string
    name?: string
    avatar?: string
  }
  size?: number | string
  disableLink?: boolean
  hideStoryRing?: boolean
}>()

const router = useRouter()
const storiesStore = useStoriesStore()

const hasStory = computed(() => {
  if (!props.user?.id) return false
  const userStories = storiesStore.getUserStories(props.user.id.toString())
  return !!userStories && userStories.stories.length > 0
})

const handleClick = (e: MouseEvent) => {
  if (props.disableLink) return
  e.stopPropagation()

  if (hasStory.value && !props.hideStoryRing) {
    router.push(`/stories/${props.user.id}`)
  } else {
    router.push(`/profile/${props.user.id}`)
  }
}

const avatarSize = computed(() => {
  if (typeof props.size === 'number') return `${props.size}px`
  return props.size || '34px' // Domyślny zaktualizowany rozmiar
})

// Zmiana z ringClass na borderClass
const borderClass = computed(() => {
  if (hasStory.value && !props.hideStoryRing) {

    return 'border-[2px] border-[#0866ff] p-[2px]'
  }
  // Odwzorowanie "Zrzut ekranu 2026-07-21 o 12.58.52.png"
  // 1px delikatnej szarej ramki, przylegającej do zdjęcia.
  // Całość nadal zajmuje dokładnie ten sam rozmiar.
  return 'border border-gray-200'
})

const isError = ref(false)
const handleImageError = () => {
  isError.value = true
}

// Reset state when user or avatar changes
watch(
  () => props.user?.avatar,
  () => {
    isError.value = false
  },
)

const fallbackAvatar = computed(() => {
  const name = props.user?.name || 'Użytkownik'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=EBF4FF&color=1877F2&bold=true`
})


</script>

<template>
  <ProfilePopper :userId="user?.id" :name="user?.name" :disabled="disableLink">
    <div
      class="relative inline-flex items-center justify-center rounded-full select-none shrink-0 bg-white box-border"
      :class="[!disableLink ? 'cursor-pointer hover:opacity-90 transition-opacity' : '', borderClass]"
      :style="{ width: avatarSize, height: avatarSize }"
      @click="handleClick"
    >
      <img
        class="rounded-full w-full h-full object-cover"
        :src="fallbackAvatar"
        :alt="user?.name || 'User'"
        @error="handleImageError"
      />
    </div>
  </ProfilePopper>
</template>
