<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

import VideoImage from 'vue-material-design-icons/VideoImage.vue'
import Image from 'vue-material-design-icons/Image.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import Incognito from 'vue-material-design-icons/Incognito.vue'
import Poll from 'vue-material-design-icons/Poll.vue'

import BaseModal from '@/components/common/BaseModal.vue'
import CreateModal from './CreateModal.vue'
import { useCreatePostStore } from '@/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import { getUserById} from '@/data/users'

import type { Event } from '@/data/events'

// Definicja props
const props = defineProps<{
  targetId?: string
  targetType?: 'User' | 'Group' | 'Event'
  eventTarget?: Event
}>()

const createPostStore = useCreatePostStore()
const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)
const isGroupComputed = computed(() => props.targetType === 'Group')

const targetUser = computed(() => {
  if (props.targetId && props.targetType === 'User') {
    return getUserById(Number(props.targetId))
  }
  return null
})

const modalTargetId = computed(() => props.eventTarget?.id || props.targetId)
const modalTargetType = computed(() => (props.eventTarget ? 'Event' : props.targetType))

const isOpen = ref(false)
const image = computed(() => currentUser.value?.avatar || '')
const placeholder = computed(() => {
  if (props.eventTarget) {
    return `Napisz coś w wydarzeniu ${props.eventTarget.name}...`
  }
  if (isGroupComputed.value) {
    return t('home.whatsOnYourMind')
  }
  if (targetUser.value) {
    return t('post.writeSomethingTo', { name: targetUser.value.name })
  }
  return t('post.whatAreYouThinking', { name: currentUser.value?.name || 'Bartosz' })
})
const fileInput = ref<HTMLInputElement | null>(null)

const createModalRef = ref<InstanceType<typeof CreateModal> | null>(null)
const showBackButton = ref(false)
const modalTitle = computed(() => {
  if (targetUser.value) {
    return `${currentUser.value?.name} > ${targetUser.value.name}`
  }
  return t('post.createPost')
})

const handleGoBack = () => {
  createModalRef.value?.goBack()
}

const openCreatePost = () => {
  isOpen.value = true
}

const openCreatePostWithFeeling = () => {
  createPostStore.setInitialView('feeling')
  openCreatePost()
}


const openCreatePostPoll = () => {
  createPostStore.setInitialView('poll')
  openCreatePost()
}

const openAnonymousPost = () => {
  // Przykładowa logika - ustawienie flagi anonimowości
  // createPostStore.setAnonymous(true);
  openCreatePost()
}

const handleFileClick = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (file) {
    if (file.type.startsWith('video/')) {
      createPostStore.setPostVideoUrl(URL.createObjectURL(file))
    } else {
      const reader = new FileReader()
      reader.onload = (e) => {
        createPostStore.addSelectedImage({
          url: e.target?.result as string,
          altText: '',
        })
      }
      reader.readAsDataURL(file)
    }
    isOpen.value = true
  }

  if (target) {
    target.value = ''
  }
}

const closeCreatePost = () => {
  isOpen.value = false
}
</script>

<template>
  <div
    id="CreatePostBox"
    class="w-full bg-theme-bg-secondary rounded-lg px-3 mt-4 shadow-md dark:shadow-lg"
  >
    <div class="flex items-center py-3 border-b border-theme-border">
      <a class="mr-2">
        <img class="rounded-full ml-1 min-w-9 max-h-9" :src="image" />
      </a>
      <div
        @click="openCreatePost"
        class="flex items-center justify-start bg-[#F1F2F5] dark:bg-[#333334] hover:bg-theme-bg-hover p-2 rounded-full w-full cursor-pointer"
      >
        <div class="text-left pl-2 text-theme-text-secondary">{{ placeholder }}</div>
      </div>
    </div>

    <div class="flex items-center py-2">
      <template v-if="isGroupComputed">
        <button
          @click="openAnonymousPost"
          class="flex items-center justify-center hover:bg-theme-hover p-1 w-full rounded-lg cursor-pointer"
        >
          <Incognito :size="30" fillColor="#1877F2" />
          <div class="text-theme-text-secondary font-medium ml-2 text-sm sm:text-base">
            Post anonimowy
          </div>
        </button>

        <button
          @click="openCreatePostWithFeeling"
          class="flex items-center justify-center hover:bg-theme-hover p-1 w-full rounded-lg cursor-pointer"
        >
          <EmoticonOutline :size="30" fillColor="#F8B927" />
          <div class="text-theme-text-secondary font-medium ml-2 text-sm sm:text-base">
            {{ $t('post.addFeeling') }}
          </div>
        </button>

        <button
          @click="openCreatePostPoll"
          class="flex items-center justify-center hover:bg-theme-hover p-1 w-full rounded-lg cursor-pointer"
        >
          <Poll :size="30" fillColor="#FA6900" />
          <div class="text-theme-text-secondary font-medium ml-2 text-sm sm:text-base">Ankieta</div>
        </button>
      </template>

      <template v-else-if="!isGroupComputed">
        <RouterLink
          to="/live/produce"
          class="flex items-center justify-center hover:bg-theme-hover p-1 w-full rounded-lg cursor-pointer"
        >
          <VideoImage :size="35" fillColor="#F12848" />
          <div class="text-theme-text-secondary font-medium">{{ $t('post.addLive') }}</div>
        </RouterLink>
        <button
          @click="handleFileClick"
          class="flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
        >
          <Image :size="35" fillColor="#43BE62" />
          <div class="text-theme-text-secondary font-medium">{{ $t('post.addPhoto') }}</div>
        </button>
        <input
          ref="fileInput"
          type="file"
          accept="image/*,video/mp4"
          class="hidden"
          @change="handleFileSelect"
        />
        <button
          @click="openCreatePostWithFeeling"
          class="hidden md:flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
        >
          <EmoticonOutline :size="35" fillColor="#F8B927" />
          <div class="text-theme-text-secondary font-medium">{{ $t('post.addFeeling') }}</div>
        </button>
      </template>
    </div>
  </div>

  <BaseModal
    v-if="isOpen"
    :title="modalTitle"
    :back="showBackButton"
    @close="closeCreatePost"
    @back="handleGoBack"
  >
    <CreateModal
      ref="createModalRef"
      v-model:showBack="showBackButton"
      v-model:title="modalTitle"
      :target-id="modalTargetId"
      :target-type="modalTargetType"
      @close="closeCreatePost"
    />
  </BaseModal>
</template>
