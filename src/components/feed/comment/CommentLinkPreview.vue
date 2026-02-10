<script setup lang="ts">
import {  ref } from 'vue'
import InformationOutline from 'vue-material-design-icons/InformationOutline.vue'
import type { LinkPreview } from '@/types/Post'
import BaseModal from '../../common/BaseModal.vue';
import LinkModal from '../LinkModal.vue';

defineProps<{
  linkPreview: LinkPreview
}>()

const isModalOpen= ref(false)
const openLinkModal = ()=> {
  isModalOpen.value = true
}
const closeLinkModal = ()=> {
  isModalOpen.value = false
}
</script>

<template>
  <div
    class="relative mt-1 border border-theme-secondary rounded-xl overflow-hidden cursor-pointer flex bg-theme-comment-bg hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
  >
    <div v-if="linkPreview.image" class="w-24 h-24 flex-shrink-0 border-r border-theme-secondary">
      <img :src="linkPreview.image" class="w-full h-full object-cover" />
    </div>

    <div class="flex flex-col justify-center p-3 pr-10 overflow-hidden">
      <div class="text-[12px] uppercase text-theme-text-secondary font-semibold tracking-wider truncate">
        {{ linkPreview.domain || new URL(linkPreview.url).hostname.toUpperCase() }}
      </div>
      <div class="text-[15px] text-theme-text leading-tight mt-1 line-clamp-2">
        {{ linkPreview.title }}
      </div>
    </div>

    <div
      class="absolute top-2 right-2 text-theme-text-secondary opacity-50 group-hover:opacity-100 transition-opacity"
      @click="openLinkModal"
    >
      <InformationOutline :size="20" />
    </div>
  </div>
  <BaseModal title="Informacje o tej zawartości
" v-if="isModalOpen" @close="closeLinkModal">
  <LinkModal :target-url="linkPreview.url"/>
  </BaseModal>
</template>

