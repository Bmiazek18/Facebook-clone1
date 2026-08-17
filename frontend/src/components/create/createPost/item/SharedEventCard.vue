<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import StarIcon from 'vue-material-design-icons/Star.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'

defineProps<{
  sharedEvent: {
    title?: string
    name?: string
    date?: string
    images?: string[]
    locationName?: string
    location?: string
  } | null
}>()

const { t } = useI18n()
</script>

<template>
  <div
    v-if="sharedEvent"
    class="mb-4 border border-theme-border rounded-lg overflow-hidden cursor-pointer group hover:opacity-95 transition-opacity"
  >
    <div
      class="relative w-full aspect-[1.91/1] bg-theme-bg-tertiary border-b border-theme-border"
    >
      <img
        v-if="sharedEvent.images?.[0]"
        :src="sharedEvent.images[0]"
        class="w-full h-full object-cover"
      />
      <div
        v-else
        class="w-full h-full bg-linear-to-br from-theme-primary to-purple-600 flex items-center justify-center text-white font-bold text-2xl"
      >
        {{ sharedEvent.date ? sharedEvent.date.split(' ')[0] : 'EVENT' }}
      </div>
    </div>
    <div class="p-3 bg-theme-bg flex items-center justify-between gap-3">
      <div class="flex-1 min-w-0 flex flex-col justify-center">
        <div
          class="text-theme-danger text-[13px] font-semibold mb-0.5 uppercase tracking-wide leading-none"
        >
          {{ sharedEvent.date || t('post.eventDateFallback') }}
        </div>
        <h3 class="font-bold text-[17px] text-theme-text leading-tight mb-0.5 truncate">
          {{ sharedEvent.title || sharedEvent.name }}
        </h3>
        <div class="text-[13px] text-theme-text-secondary truncate leading-tight">
          {{ sharedEvent.locationName || sharedEvent.location || 'Lokalizacja nieznana' }}
        </div>
      </div>
      <button
        class="shrink-0 flex items-center gap-1.5 bg-theme-blue-light hover:bg-theme-blue-light-hover text-theme-primary px-3 py-1.5 rounded-md font-semibold text-[15px] transition-colors border border-transparent"
      >
        <StarIcon :size="18" />
        <span>{{ t('post.interesujeSie') }}</span>
        <ChevronDownIcon :size="16" class="ml-0.5" />
      </button>
    </div>
  </div>
</template>
