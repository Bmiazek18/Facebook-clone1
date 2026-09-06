<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useStoryShareStore } from '@/stores/storyShare'
import type { PostData, ReelData } from '@/types/StoryElement'
import NavbarRight from '@/components/navbar/NavbarRight.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'

// Sub-views rendered conditionally
import StoryPicker from '@/components/create/createStory/StoryPicker.vue'
import StoryImageEditor from '@/components/create/createStory/StoryImageEditor.vue'
import StoryTextEditor from '@/components/create/createStory/StoryTextEditor.vue'

type StoryMode = 'picker' | 'image' | 'text'

const storyShareStore = useStoryShareStore()

const mode = ref<StoryMode>('picker')
const selectedImage = ref<{ url: string; altText: string } | null>(null)
const initialPost = ref<PostData | null>(null)
const initialReel = ref<ReelData | null>(null)

onMounted(() => {
  const pendingPost = storyShareStore.getPendingPost()
  const pendingReel = storyShareStore.getPendingReel()

  if (pendingPost) {
    initialPost.value = pendingPost
    mode.value = 'image'
  } else if (pendingReel) {
    initialReel.value = pendingReel
    mode.value = 'image'
  }
})

const onSelectImage = (imageUrl: string) => {
  selectedImage.value = { url: imageUrl, altText: '' }
  mode.value = 'image'
}

const onSelectText = () => {
  mode.value = 'text'
}

const showDiscardConfirm = ref(false)
const nextNavigationTarget = ref<string | null>(null)
const router = useRouter()

onBeforeRouteLeave((to, from, next) => {
  if (mode.value !== 'picker') {
    showDiscardConfirm.value = true
    nextNavigationTarget.value = to.fullPath
    next(false)
  } else {
    next()
  }
})

const confirmDiscard = () => {
  showDiscardConfirm.value = false
  selectedImage.value = null
  initialPost.value = null
  initialReel.value = null
  mode.value = 'picker'

  if (nextNavigationTarget.value) {
    const target = nextNavigationTarget.value
    nextNavigationTarget.value = null
    router.push(target)
  }
}

// Go back to picker
const onBack = () => {
  if (mode.value !== 'picker') {
    showDiscardConfirm.value = true
  } else {
    router.back()
  }
}
</script>

<template>
  <div class="h-screen w-full">
    <div class="absolute top-0 right-0 p-4 gap-3 z-30 hidden md:flex">
      <NavbarRight />
    </div>
    <StoryPicker
      v-if="mode === 'picker'"
      @select-image="onSelectImage"
      @select-text="onSelectText"
    />

    <!-- Image editor -->
    <StoryImageEditor
      v-else-if="mode === 'image'"
      :initial-image="selectedImage?.url"
      :initial-post="initialPost"
      :initial-reel="initialReel"
      @back="onBack"
    />

    <StoryTextEditor v-else-if="mode === 'text'" @back="onBack" />
  </div>

  <!-- Discard Confirmation Modal -->
  <BaseModal
    v-if="showDiscardConfirm"
    :title="$t('create.odrzucicRelacje')"
    @close="showDiscardConfirm = false"
  >
    <div class="p-6 text-theme-text max-w-md w-full bg-theme-bg-secondary rounded-lg">
      <p class="mb-6 text-[15px] text-theme-text-secondary leading-normal">{{ $t('create.czyNaPewnoChcesz') }}</p>

      <div class="flex justify-end items-center gap-3">
        <button
          @click="showDiscardConfirm = false"
          class="px-4 py-2.5 rounded-lg bg-gray-200 hover:bg-gray-300 text-black font-semibold transition text-[15px] cursor-pointer"
        >{{ $t('create.kontynuujEdycje') }}</button>

        <button
          @click="confirmDiscard"
          class="px-6 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold shadow-md transition text-[15px] cursor-pointer"
        >{{ $t('chat.odrzuc') }}</button>
      </div>
    </div>
  </BaseModal>
</template>
