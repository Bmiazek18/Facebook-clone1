<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ImageTagType } from '@/types/Post'

const props = withDefaults(
  defineProps<{
    tag: ImageTagType
    /** Show label even without hover (e.g. when parent photo is hovered) */
    forceShow?: boolean
  }>(),
  { forceShow: false },
)

const isHovered = ref(false)

const showLabel = computed(() => props.forceShow || isHovered.value)

const displayName = computed(() => {
  if (props.tag.user) {
    const u = props.tag.user as any
    if (u.name) return u.name
    return [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik'
  }
  return props.tag.name || 'Użytkownik'
})

const profilePath = computed(() => {
  const id = props.tag.userId || props.tag.user?.id
  return id ? `/profile/${id}` : undefined
})
</script>

<template>
  <component
    :is="profilePath ? 'NuxtLink' : 'div'"
    :to="profilePath"
    class="block w-10 h-10 border-2 -translate-x-1/2 -translate-y-1/2 transition-colors duration-150 z-20 pointer-events-auto"
    :class="{
      'border-white shadow-[0_0_0_1px_rgba(0,0,0,0.35)]': showLabel,
      'border-transparent': !showLabel,
    }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
    @click.stop
  >
    <div
      v-if="showLabel"
      class="absolute left-1/2 bottom-[calc(100%+8px)] -translate-x-1/2 flex flex-col items-center filter drop-shadow-md whitespace-nowrap pointer-events-none"
    >
      <div class="bg-black/80 text-white px-2.5 py-1 rounded text-[13px] font-semibold">
        {{ displayName }}
      </div>
      <div
        class="w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-t-[6px] border-t-black/80"
      ></div>
    </div>
  </component>
</template>
