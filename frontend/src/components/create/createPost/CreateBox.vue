<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

import VideoImage from 'vue-material-design-icons/VideoImage.vue'
import Image from 'vue-material-design-icons/Image.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import Incognito from 'vue-material-design-icons/Incognito.vue'
import Flag from 'vue-material-design-icons/Flag.vue' // Nowa ikona do wydarzenia z życia
import Poll from 'vue-material-design-icons/Poll.vue'

import BaseModal from '@/components/common/BaseModal.vue'
import CreateModal from './CreateModal.vue'
import { useCreatePostStore } from '@/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import { getUserById } from '@/utils/users'

import type { Event } from '@/types/Event'
import AvatarImage from '~/components/common/AvatarImage.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
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
const isGroup2 = computed(() => props.targetType === 'Group' && props.targetId === '2')

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
const modalTitle = ref('')

const handleGoBack = () => {
  createModalRef.value?.goBack()
}

const openCreatePost = () => {
  isOpen.value = true
}

const openCreateLifeEvent = () => {
  createPostStore.uiState.initialView = 'lifeEvent'
  isOpen.value = true
}

const openCreatePostWithFeeling = () => {
  createPostStore.uiState.initialView = 'feeling'
  openCreatePost()
}

const openCreatePostPoll = () => {
  createPostStore.postData.poll = {
    question: '',
    options: [{ text: '' }, { text: '' }]
  }
  createPostStore.uiState.initialView = 'poll'
  openCreatePost()
}

const openAnonymousPost = () => {
  openCreatePost()
}

const handleFileClick = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: any) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (file) {
    if (file.type.startsWith('image/') || file.type.startsWith('video/')) {
      createPostStore.uploadVideoInChunks(file)
    }
    isOpen.value = true
  }

  if (target) {
    target.value = ''
  }
}

const closeCreatePost = () => {
  isOpen.value = false
  createPostStore.reset()
}
</script>

<template>
  <div
    id="CreatePostBox"
    class="w-full bg-theme-bg-secondary rounded-lg px-4 py-3 mt-4 shadow-sm relative z-10 mx-auto max-w-[calc(100%-8px)] sm:max-w-full"
  >
    <!-- STAN 1: Główna tablica (Home) -->
    <div v-if="!targetId && !eventTarget && !isGroupComputed" class="flex items-center gap-2">
      <a class="flex-shrink-0">
        <AvatarImage :src="image" />
      </a>

      <div
        @click="openCreatePost"
        class="flex-grow bg-[#F0F2F5] dark:bg-[#3A3B3C] hover:bg-theme-bg-hover transition-colors py-2 px-3 rounded-full cursor-pointer"
      >
        <div class="text-theme-text-secondary text-[17px] truncate">
          {{ placeholder }}
        </div>
      </div>

      <div class="flex items-center gap-1 sm:gap-2 flex-shrink-0">
        <NuxtLink
          to="/live/produce"
          class="p-0.5 hover:bg-gray-100 dark:hover:bg-white/10 rounded-[5px] transition"
          v-tooltip="$t('post.video')"
        >
          <VideoImage :size="34" fillColor="#F12848" />
        </NuxtLink>

        <button
          @click="handleFileClick"
          class="p-0.5 hover:bg-gray-100 dark:hover:bg-white/10 rounded-[5px] transition"
          v-tooltip="$t('post.image')"
        >
          <Image :size="28" fillColor="#43BE62" />
        </button>

        <button
          @click="openCreatePostWithFeeling"
          class="p-0.5 hover:bg-gray-100 dark:hover:bg-white/10 rounded-[5px] transition"
          v-tooltip="$t('post.feeling')"
        >
          <EmoticonOutline :size="28" fillColor="#F8B927" />
        </button>
      </div>
    </div>

    <!-- STAN 2: Grupy, Wydarzenia, Profile innych użytkowników (Wygląd ze zrzutu ekranu) -->
    <div v-else class="flex flex-col">
      <!-- Górna sekcja: Awatar + Szeroki zaokrąglony input box -->
      <div class="flex items-center gap-2 pb-3 border-b border-theme-border">
        <a class="flex-shrink-0">
          <AvatarImage :src="image" />
        </a>
        <div
          @click="openCreatePost"
          class="flex-grow bg-[#F0F2F5] dark:bg-[#3A3B3C] hover:bg-theme-bg-hover transition-colors py-2.5 px-4 rounded-full cursor-pointer"
        >
          <div class="text-left text-theme-text-secondary text-[16px] sm:text-[17px] truncate">
            {{ placeholder }}
          </div>
        </div>
      </div>

      <!-- Dolna sekcja: Trzy równomiernie rozłożone przyciski akcji -->
      <div class="grid grid-cols-3 gap-1 pt-2.5">
        <!-- 1. Transmisja wideo na żywo / Ankieta (tylko dla grupy 2) -->
        <button
          v-if="isGroupComputed"
          @click="openCreatePostPoll"
          class="flex items-center justify-center gap-2 hover:bg-theme-hover py-2 rounded-lg transition-colors text-theme-text-secondary font-semibold text-[14px] sm:text-[15px] cursor-pointer w-full"
        >
          <Poll :size="24" fillColor="#0866FF" />
          <span class="truncate">{{ $t('create.ankieta') }}</span>
        </button>
        <NuxtLink
          v-else
          to="/live/produce"
          class="flex items-center justify-center gap-2 hover:bg-theme-hover py-2 rounded-lg transition-colors text-theme-text-secondary font-semibold text-[14px] sm:text-[15px] cursor-pointer"
        >
          <VideoImage :size="24" fillColor="#F12848" />
          <span class="truncate">{{ $t('createEvent.liveVideo') }}</span>
        </NuxtLink>

        <!-- 2. Zdjęcie/film -->
        <button
          @click="handleFileClick"
          class="flex items-center justify-center gap-2 hover:bg-theme-hover py-2 rounded-lg transition-colors text-theme-text-secondary font-semibold text-[14px] sm:text-[15px] cursor-pointer"
        >
          <Image :size="24" fillColor="#43BE62" />
          <span class="truncate">{{ $t('post.photoVideo') }}</span>
        </button>

        <!-- 3. Dynamiczny przycisk: Post anonimowy (dla grup) LUB Wydarzenie z życia (dla reszty) -->
        <button
          v-if="isGroupComputed"
          @click="openAnonymousPost"
          class="flex items-center justify-center gap-2 hover:bg-theme-hover py-2 rounded-lg transition-colors text-theme-text-secondary font-semibold text-[14px] sm:text-[15px] cursor-pointer"
        >
          <Incognito :size="24" fillColor="#1877F2" />
          <span class="truncate">{{ $t('post.anonymousPost') }}</span>
        </button>

        <button
          v-else
          @click="openCreateLifeEvent"
          class="flex items-center justify-center gap-2 hover:bg-theme-hover py-2 rounded-lg transition-colors text-theme-text-secondary font-semibold text-[14px] sm:text-[15px] cursor-pointer"
        >
          <Flag :size="24" fillColor="#1877F2" />
          <span class="truncate">{{ $t('create.wydarzenieZZycia') }}</span>
        </button>
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*,video/mp4"
      class="hidden"
      @change="handleFileSelect"
    />
  </div>

  <BaseModal
    v-if="isOpen"
    :title="t('post.createPost')"
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
