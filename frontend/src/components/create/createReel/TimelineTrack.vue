<template>
  <div>
    <div class="text-white text-xs mb-1 flex items-center gap-1">
      <component :is="config.icon" :size="14" />
      {{ config.label }}
    </div>
    <div ref="trackRef" class="relative h-12 bg-gray-700 rounded">
      <div
        v-for="item in items"
        :key="item.id"
        :style="{
          left: (item.startTime / totalDuration) * 100 + '%',
          width: ((item.endTime - item.startTime) / totalDuration) * 100 + '%',
        }"
        :class="['absolute top-1 bottom-1 bg-linear-to-r rounded group', config.gradientClass]"
        @click.stop="emit('select', item)"
      >
        <!-- Lewy uchwyt do rozszerzania -->
        <div
          :class="[
            'absolute left-0 top-0 bottom-0 w-2 cursor-ew-resize opacity-0 group-hover:opacity-100 transition-opacity rounded-l',
            config.handleClass,
            config.handleHoverClass,
          ]"
          @mousedown.stop="dragHandler?.startResize(item, 'left', $event)"
        ></div>

        <!-- Środek do przesuwania -->
        <div
          class="absolute inset-0 flex items-center justify-center text-white text-[10px] font-medium truncate px-1 cursor-move"
          @mousedown.stop="dragHandler?.startMove(item, $event)"
        >
          <slot name="content" :item="item">
            <component :is="config.icon" :size="12" />
          </slot>
        </div>

        <!-- Prawy uchwyt do rozszerzania -->
        <div
          :class="[
            'absolute right-0 top-0 bottom-0 w-2 cursor-ew-resize opacity-0 group-hover:opacity-100 transition-opacity rounded-r',
            config.handleClass,
            config.handleHoverClass,
          ]"
          @mousedown.stop="dragHandler?.startResize(item, 'right', $event)"
        ></div>
      </div>

      <div
        v-if="items.length === 0"
        class="absolute inset-0 flex items-center justify-center text-gray-500 text-xs pointer-events-none"
      >
        {{ config.emptyMessage }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends BaseTimelineItem">
import { ref, computed, toRef, type Component } from 'vue'
import type { BaseTimelineItem } from '@/types/video-editor.types'
import { useTimelineDrag } from '@/composables/media/useTimelineDrag'
import TextIcon from 'vue-material-design-icons/FormatText.vue'
import ImageIcon from 'vue-material-design-icons/Image.vue'
import VideoIcon from 'vue-material-design-icons/Video.vue'

type TrackType = 'text' | 'image' | 'pipVideo'

interface TrackConfig {
  label: string
  icon: Component
  gradientClass: string
  handleClass: string
  handleHoverClass: string
  emptyMessage: string
}

interface Props {
  type: TrackType
  items: T[]
  totalDuration: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  select: [item: T]
}>()

const trackRef = ref<HTMLElement | null>(null)

const config = computed<TrackConfig>(() => {
  const configs: Record<TrackType, TrackConfig> = {
    text: {
      label: 'Tekst',
      icon: TextIcon,
      gradientClass: 'from-purple-600 to-purple-500',
      handleClass: 'bg-purple-400',
      handleHoverClass: 'hover:bg-purple-300',
      emptyMessage: 'Kliknij aby dodać tekst',
    },
    image: {
      label: 'Obrazki',
      icon: ImageIcon,
      gradientClass: 'from-green-600 to-green-500',
      handleClass: 'bg-green-400',
      handleHoverClass: 'hover:bg-green-300',
      emptyMessage: 'Dodaj obrazek',
    },
    pipVideo: {
      label: 'Video PiP',
      icon: VideoIcon,
      gradientClass: 'from-orange-600 to-orange-500',
      handleClass: 'bg-orange-400',
      handleHoverClass: 'hover:bg-orange-300',
      emptyMessage: 'Dodaj video PiP',
    },
  }

  return configs[props.type]
})

// Użyj composable do obsługi drag & drop
const dragHandler = useTimelineDrag(
  trackRef,
  toRef(() => props.items),
  toRef(() => props.totalDuration),
  (item: T) => emit('select', item),
)
</script>
