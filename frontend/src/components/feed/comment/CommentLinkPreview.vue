<script setup lang="ts">
import { ref } from 'vue'
import InformationOutline from 'vue-material-design-icons/InformationOutline.vue'
import type { LinkPreview } from '@/types/Post'
import BaseModal from '../../common/BaseModal.vue'
import LinkModal from '../modals/LinkModal.vue'
import { useLinkGuard } from '@/composables/shared/useLinkGuard'

const props = defineProps<{
  linkPreview: LinkPreview
}>()

const isModalOpen = ref(false)
const { isVerifying, verifyAndNavigate } = useLinkGuard()

const openLinkModal = () => {
  isModalOpen.value = true
}
const closeLinkModal = () => {
  isModalOpen.value = false
}
const handleLinkClick = async () => {
  if (isVerifying.value) return
  await verifyAndNavigate(props.linkPreview.url)
}
</script>

<template>
  <div
    @click="handleLinkClick"
    class="relative mt-1 border border-theme-secondary rounded-xl overflow-hidden cursor-pointer flex bg-theme-comment-bg hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
  >
    <!-- Loading Spinner Overlay -->
    <div
      v-if="isVerifying"
      class="absolute inset-0 bg-theme-comment-bg/95 backdrop-blur-[1px] flex items-center justify-center z-20 transition-all"
    >
      <div class="flex items-center space-x-2">
        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-theme-primary"></div>
        <span class="text-xs text-theme-text-secondary font-medium">{{ $t('feed.skanowanie') }}</span>
      </div>
    </div>

    <div v-if="linkPreview.image" class="w-24 h-24 flex-shrink-0 border-r border-theme-secondary">
      <img :src="linkPreview.image" class="w-full h-full object-cover" />
    </div>

    <div class="flex flex-col justify-center p-3 pr-10 overflow-hidden">
      <div
        class="text-[12px] uppercase text-theme-text-secondary font-semibold tracking-wider truncate"
      >
        {{ linkPreview.domain || new URL(linkPreview.url).hostname.toUpperCase() }}
      </div>
      <div class="text-[15px] text-theme-text leading-tight mt-1 line-clamp-2">
        {{ linkPreview.title }}
      </div>
    </div>

    <div
      class="absolute top-2 right-2 text-theme-text-secondary opacity-50 group-hover:opacity-100 transition-opacity z-10 p-1 hover:bg-black/10 dark:hover:bg-white/10 rounded-full"
      @click.stop="openLinkModal"
    >
      <InformationOutline :size="20" />
    </div>
  </div>
  <BaseModal
    :title="$t('birthday.informacjeOTejZawartosci')"
    v-if="isModalOpen"
    @close="closeLinkModal"
  >
    <LinkModal :target-url="linkPreview.url" />
  </BaseModal>
</template>
