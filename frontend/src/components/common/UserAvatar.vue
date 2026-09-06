<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStoriesStore } from '@/composables/feed/useAppState'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'
import BaseModal from './BaseModal.vue';
import NotesModal from '@/components/profile/modals/NotesModal.vue';

const props = defineProps<{
  user: {
    id: number | string
    name?: string
    avatar?: string
    note?: string
  }
  size?: number | string
  disableLink?: boolean
  hideStoryRing?: boolean
  isOwner?: boolean
  /** Gdy ustawione — klik (bez story) otwiera /photo z tym src */
  viewPhotoSrc?: string
  viewPhotoType?: 'avatar' | 'cover' | 'photo'
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
    return
  }

  if (props.viewPhotoSrc) {
    router.push({
      path: '/photo',
      query: {
        src: props.viewPhotoSrc,
        type: props.viewPhotoType || 'avatar',
        userId: String(props.user.id),
        name: props.user.name || '',
      },
    })
    return
  }

  router.push(`/profile/${props.user.id}`)
}

const avatarSize = computed(() => {
  if (typeof props.size === 'number') return `${props.size}px`
  return props.size || '34px'
})

// NAJPROSTSZA RAMKA: Dodajemy obrys za pomocą 'ring' bezpośrednio w class obrazka
const avatarRingClass = computed(() => {
  if (hasStory.value && !props.hideStoryRing) {
    return 'ring-2 ring-[#0866ff] ring-offset-2'
  }
  return 'ring-1 ring-gray-300'
})

const isError = ref(false)
const handleImageError = () => {
  isError.value = true
}

watch(
  () => props.user?.avatar,
  () => {
    isError.value = false
  },
)

const isModalOpen = ref(false)
const toggleModal = () => {
  isModalOpen.value = !isModalOpen.value
}
</script>

<template>
  <ProfilePopper :userId="user?.id" :name="user?.name" :disabled="disableLink">
    <div class="relative inline-flex flex-col items-center">

      <!-- Dymek z notatką -->
      <div
        v-if="user?.note || isOwner"
        @click="isOwner ? toggleModal() : null"
        class="absolute z-20 bottom-full cursor-pointer -mb-2 left-1/2 transform -translate-x-1/2 ml-[-50px]"
      >
        <div class="relative bg-white shadow-[0_2px_8px_rgba(0,0,0,0.15)] rounded-[30px] px-4 py-3 border border-gray-100">
          <span class="text-[#65676B] text-[12px] whitespace-nowrap">{{ user?.note || 'Napisz, co myślisz...' }}</span>

          <div class="absolute -bottom-1.5 right-[30px] w-4.5 h-4.5 bg-white rounded-full z-10"></div>
          <div class="absolute -bottom-4.5 right-[25px] w-2 h-2 bg-white rounded-full shadow-[0_1px_3px_rgba(0,0,0,0.15)]"></div>
        </div>
      </div>

      <!-- Kontener Awatara -->
      <div
        class="relative inline-flex items-center justify-center rounded-full select-none shrink-0 bg-white box-border z-10"
        :class="{ 'cursor-pointer hover:opacity-90 transition-opacity': !disableLink }"
        :style="{ width: avatarSize, height: avatarSize }"
        @click="handleClick"
      >
        <img
          class="rounded-full w-full h-full object-cover"
          :class="avatarRingClass"
          :src="(!isError && user?.avatar) ? user.avatar : '/default-avatar.png'"
          :alt="user?.name || 'User'"
          @error="handleImageError"
        />
      </div>
    </div>
  </ProfilePopper>

  <BaseModal v-if="isModalOpen" @close="toggleModal" :title="$t('common.nowaNotatka')">
    <NotesModal :userId="user?.id" :currentNote="user?.note" @close="toggleModal" />
  </BaseModal>
</template>
