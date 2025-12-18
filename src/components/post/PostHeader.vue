<script setup lang="ts">
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import ProfilePopper from '../ProfilePopper.vue'
import { useTheme } from '@/composables/useTheme'

defineProps<{
  authorName: string
  authorAvatar: string
  date?: string
}>()

defineEmits<{
  (e: 'menu'): void
  (e: 'close'): void
}>()

const { isDark } = useTheme()
</script>

<template>
  <div class="px-4 pt-3 pb-1">
    <div class="flex items-start">
      <button class="mr-2.5">
        <img
          class="rounded-full w-10 h-10 object-cover"
          :src="authorAvatar"
          :alt="authorName"
        >
      </button>

      <div class="flex-1 min-w-0 mt-0.5">
        <ProfilePopper :name="authorName" class="font-semibold text-theme-text text-[15px] hover:underline cursor-pointer leading-tight block" />
        <div class="flex items-center text-[13px] text-theme-text-secondary mt-0.5">
          <span class="hover:underline cursor-pointer">{{ date || '17 grudnia' }}</span>
          <span class="mx-1">·</span>
          <Earth :size="12" fillColor="#65676B" v-tooltip="'Publiczne'" />
        </div>
      </div>

      <div class="flex items-center -mr-2">
        <button @click="$emit('menu')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
        </button>
        <button @click="$emit('close')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <Close :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
        </button>
      </div>
    </div>
  </div>
</template>
