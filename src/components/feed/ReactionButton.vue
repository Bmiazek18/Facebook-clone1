<template>
  <div
    class="reaction-wrapper relative flex justify-center select-none"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    :class="display === 'full' ? 'w-full' : 'w-fit'"
  >
    <button
      :class="[
        'flex items-center justify-center gap-2 rounded transition-colors cursor-pointer text-theme-text-secondary font-semibold w-full',
        {
          'hover:bg-theme-hover': display !== 'compact',
          'h-9 text-[15px]': display === 'full',
          'text-[12px] hover:underline': display === 'compact',
        }
      ]"
      @click="toggleReaction('like')"
    >
      <template v-if="display === 'full'">
        <template v-if="userReaction">
          <span class="text-xl leading-none">{{ getReactionEmoji(userReaction) }}</span>
          <span :class="getReactionTextColor(userReaction)">{{ getReactionLabel(userReaction) }}</span>
        </template>
        <template v-else>
          <ThumbUpOutline :size="20" class="text-gray-500 dark:text-gray-400" />
          <span class="text-theme-text-secondary" >{{ $t('actions.like') }}</span>
        </template>
      </template>

      <template v-else> <template v-if="userReaction">
          <span :class="getReactionTextColor(userReaction)">
            {{ getReactionLabel(userReaction) }}
          </span>
        </template>
        <template v-else>
          <span class="text-theme-text-secondary">{{ $t('actions.like') }}</span>
        </template>
      </template>
    </button>


    <div
      class="absolute left-0 flex gap-1 px-2 py-1.5 rounded-full shadow-xl bg-theme-bg-secondary border border-gray-200 dark:border-gray-700 transition-all duration-200 z-50"
      :class="[
        display === 'full' ? 'bottom-10' : 'bottom-6',
        isVisible ? 'opacity-100 pointer-events-auto translate-y-0' : 'opacity-0 pointer-events-none translate-y-2'
      ]"
    >
      <div
        v-for="(reaction, index) in reactions"
        :key="reaction.name"
        class="flex flex-col items-center"
        @click="selectReaction(reaction.name as ReactionType)"
      >
        <div
          class="transition-all duration-300 ease-[cubic-bezier(0.34,1.56,0.64,1)]"
          :style="{
            transitionDelay: isVisible ? `${index * 40}ms` : '0ms',
            transform: isVisible ? 'scale(1) translateY(0)' : 'scale(0.3) translateY(20px)',
            opacity: isVisible ? 1 : 0
          }"
        >
          <div
            class="w-10 h-10 flex items-center justify-center cursor-pointer transition-transform duration-200 hover:scale-125 hover:-translate-y-2"
            v-tooltip="reaction.label"
          >
            <img :src="reaction.src" :alt="reaction.name" class="w-9 h-9 object-contain" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTimeoutFn } from '@vueuse/core'
import ThumbUpOutline from 'vue-material-design-icons/ThumbUpOutline.vue'
import type { ReactionType } from '@/types/Post'

const props = defineProps({
  display: {
    type: String,
    default: 'full'
  },
  userReaction: {
    type: String as () => ReactionType | null,
    default: null
  }
})

const emit = defineEmits(['react'])

const { t } = useI18n()

// Konfiguracja: emoji do przycisku, src (GIF) do listy
const reactionConfigs = [
  { name: 'like', labelKey: 'reaction.like', emoji: '👍', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif", color: 'text-blue-500' },
  { name: 'love', labelKey: 'reaction.love', emoji: '❤️', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif", color: 'text-red-500' },
  { name: 'haha', labelKey: 'reaction.haha', emoji: '😆', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif", color: 'text-yellow-500' },
  { name: 'wow', labelKey: 'reaction.wow', emoji: '😮', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif", color: 'text-yellow-500' },
  { name: 'sad', labelKey: 'reaction.sad', emoji: '😢', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif", color: 'text-blue-400' },
  { name: 'angry', labelKey: 'reaction.angry', emoji: '😡', src: "https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif", color: 'text-orange-600' }
]

const reactions = computed(() =>
  reactionConfigs.map(config => ({
    ...config,
    label: t(config.labelKey)
  }))
)

const isVisible = ref(false)

// Pobiera statyczne emoji dla przycisku
const getReactionEmoji = (name: string) => {
  return reactionConfigs.find(r => r.name === name)?.emoji || ''
}

const getReactionLabel = (name: string) => {
  const config = reactionConfigs.find(r => r.name === name)
  return config ? t(config.labelKey) : ''
}

const getReactionTextColor = (name: string) => {
  return reactionConfigs.find(r => r.name === name)?.color || 'text-blue-500'
}

const selectReaction = (reactionName: ReactionType) => {
  emit('react', reactionName)
  isVisible.value = false
}

const toggleReaction = (defaultReaction: ReactionType) => {
  if (props.userReaction === defaultReaction) {
    emit('react', null) // odkliknięcie
  } else {
    emit('react', defaultReaction)
  }
}

const { start: startHideTimer, stop: stopHideTimer } = useTimeoutFn(() => {
  isVisible.value = false
}, 500, { immediate: false })

const { start: startShowTimer, stop: stopShowTimer } = useTimeoutFn(() => {
  isVisible.value = true
}, 500, { immediate: false })

const handleMouseEnter = () => {
  stopHideTimer()
  startShowTimer()
}

const handleMouseLeave = () => {
  stopShowTimer()
  if (isVisible.value) {
    startHideTimer()
  }
}
</script>
