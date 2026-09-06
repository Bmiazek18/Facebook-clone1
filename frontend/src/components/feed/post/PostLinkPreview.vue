<script setup lang="ts">
import { ref } from 'vue'
import type { LinkPreviewData } from '@/types/Post'
import WebIcon from 'vue-material-design-icons/Web.vue'
import InformationIcon from 'vue-material-design-icons/Information.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import LinkModal from '@/components/feed/modals/LinkModal.vue'
import { useLinkGuard } from '@/composables/shared/useLinkGuard'

const props = defineProps<{
  linkPreview: LinkPreviewData
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
  <div class="relative mb-3 group select-none">
    <div
      @click="handleLinkClick"
      class="block bg-theme-bg-tertiary overflow-hidden hover:bg-theme-bg-hover transition-colors cursor-pointer no-underline shadow-sm relative"
    >
      <!-- Loading Overlay -->
      <div
        v-if="isVerifying"
        class="absolute inset-0 bg-theme-bg-secondary/90 backdrop-blur-[4px] flex flex-col items-center justify-center z-20 transition-all duration-300"
      >
        <div class="flex flex-col items-center p-4 text-center">
          <div class="relative flex items-center justify-center w-12 h-12 rounded-full bg-theme-primary/10 text-theme-primary animate-pulse mb-3">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
            </svg>
          </div>
          <span class="text-[15px] font-semibold text-theme-text">{{ $t('feed.weryfikowanieBezpieczenstwa') }}</span>
          <span class="text-xs text-theme-text-secondary mt-1">{{ $t('feed.przekierowywaniePrzezSystemLinkguard') }}</span>
        </div>
      </div>

      <!-- Preview Image -->
      <div
        v-if="linkPreview.image"
        class="w-full aspect-[1.91/1] overflow-hidden bg-theme-bg-tertiary relative"
      >
        <img :src="linkPreview.image" class="w-full h-full object-cover" :alt="$t('create.linkPreview')" />

        <button
          @click.stop="openLinkModal"
          class="absolute bottom-2 right-2 bg-black/60 hover:bg-black/80 text-white/90 rounded-full w-6 h-6 flex items-center justify-center backdrop-blur-[2px] z-10 transition-colors pointer-events-auto"
          :title="$t('feed.informacjeOTejWitrynie')"
        >
          <InformationIcon :size="14" />
        </button>
      </div>

      <!-- Preview Text Info -->
      <div class="p-4 flex flex-col justify-center">
        <div
          class="text-[13px] text-theme-text-secondary uppercase font-medium tracking-wide mb-1.5 flex items-center justify-between truncate opacity-80"
        >
          <div class="flex items-center">
            <WebIcon :size="14" class="mr-1.5" v-if="!linkPreview.image" />
            {{ linkPreview.domain }}
          </div>
          <button
            v-if="!linkPreview.image"
            @click.stop="openLinkModal"
            class="p-1 hover:bg-black/10 dark:hover:bg-white/10 rounded-full transition-colors z-10 pointer-events-auto"
            :title="$t('feed.informacjeOTejWitrynie')"
          >
            <InformationIcon :size="16" />
          </button>
        </div>

        <div class="font-bold text-theme-text text-[17px] leading-6 mb-1 line-clamp-2">
          {{ linkPreview.title }}
        </div>

        <div class="text-theme-text-secondary text-sm leading-snug line-clamp-1 opacity-90">
          {{ linkPreview.description }}
        </div>
      </div>
    </div>
  </div>

  <BaseModal
    :title="$t('birthday.informacjeOTejZawartosci')"
    v-if="isModalOpen"
    @close="closeLinkModal"
  >
    <LinkModal :targetUrl="linkPreview.url" />
  </BaseModal>
</template>

