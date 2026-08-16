<template>
  <div
    class="reaction-wrapper relative flex justify-center select-none"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    :class="display === 'full' ? 'w-full' : 'w-fit'"
  >
    <div
      v-if="hasDarkBackground"
      class="p-2 md:p-3  rounded-full transition-colors cursor-pointer"
      @click="toggleReaction('like')"
    >
      <component
        :is="isLiked || userReaction === 'like' ? ThumbUpIcon : ThumbUpOutline"
        :size="iconSize"
        :fillColor="isLiked || userReaction === 'like' ? '#1b74e4' : 'white'"
      />
    </div>

    <button
      v-else
      :class="[
        'flex items-center justify-center gap-2 rounded transition-colors cursor-pointer font-semibold w-full',
        {
          'hover:bg-theme-hover': display !== 'compact',
          'h-9 text-[15px]': display === 'full',
          'text-[12px] hover:underline': display === 'compact',
          'text-theme-text-secondary': !userReaction,
        },
      ]"
      @click="toggleReaction('like')"
    >
      <template v-if="display === 'full'">
        <div v-if="userReaction" class="flex items-center gap-2">
          <div class="animate-pop" :key="userReaction">
            <span v-if="userReaction === 'like'" class="flex items-center">
              <ThumbUpIcon :size="20" class="text-blue-500" />
            </span>
            <span v-else-if="userReaction === 'love'" class="flex items-center">
              <HeartCircleIcon :size="22" class="text-[#F3425F]" />
            </span>
            <span v-else class="text-xl leading-none flex items-center">
              {{ getReactionEmoji(userReaction) }}
            </span>
          </div>

          <span :style="{ color: getReactionColorHex(userReaction) }">
            {{ getReactionLabel(userReaction) }}
          </span>
        </div>

        <template v-else>
          <ThumbUpOutline :size="iconSize" class="text-gray-500 dark:text-gray-400" />
          <span>{{ $t('actions.like') }}</span>
        </template>
      </template>

      <template v-else>
        <template v-if="userReaction">
          <span :style="{ color: getReactionColorHex(userReaction) }">
            {{ getReactionLabel(userReaction) }}
          </span>
        </template>
        <template v-else>
          <span class="text-theme-text-secondary">{{ $t('actions.like') }}</span>
        </template>
      </template>
    </button>

    <div
      class="absolute left-0 flex gap-1 px-2 py-1.5 rounded-full shadow-xl transition-all duration-200 z-50"
      :class="[
        hasDarkBackground ? 'bg-[#252729]' : 'bg-theme-bg-secondary',
        display === 'full' ? 'bottom-10' : 'bottom-6',
        isVisible
          ? 'opacity-100 pointer-events-auto translate-y-0'
          : 'opacity-0 pointer-events-none translate-y-2',
      ]"
    >
      <div
        v-for="(reaction, index) in reactions"
        :key="reaction.name"
        class="flex flex-col items-center hover:scale-125 w-[40px] transition-transform duration-200 cursor-pointer"
        @click="selectReaction(reaction.name)"
      >
        <div
          class="transition-all duration-300"
          :style="{
            transitionDelay: isVisible ? `${index * 40}ms` : '0ms',
            transform: isVisible ? 'scale(1) translateY(0)' : 'scale(0.3) translateY(20px)',
            opacity: isVisible ? 1 : 0,
          }"
        >
          <LottieIcon v-if="reaction.name === 'like'" :animationData="LIKE_JSON" :size="35" />
          <LottieIcon v-else-if="reaction.name === 'love'" :animationData="LOVE_JSON" :size="35" />
          <img v-else :src="reaction.src" :alt="reaction.name" class="w-10 h-10 object-contain" />
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
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue'
import HeartCircleIcon from 'vue-material-design-icons/HeartCircle.vue'

import LottieIcon from './LottieIcon.vue'
import LIKE_JSON from '@/assets/animations/like.json'
import LOVE_JSON from '@/assets/animations/love.json'

const props = defineProps({
  display: {
    type: String,
    default: 'full',
  },
  userReaction: {
    type: String as () => ReactionType | null,
    default: null,
  },
  hasDarkBackground: {
    type: Boolean,
    default: false,
  },
  iconSize: {
    type: Number,
    default: 20,
  },
  isLiked: {
    type: Boolean,
    default: false,
  },
  alwaysDarkPopup: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['react'])

const { t } = useI18n()

// Konfiguracja kolorów przeszła na bezpośrednie wartości HEX
const reactionConfigs = [
  {
    name: 'like',
    labelKey: 'reaction.like',
    emoji: '👍',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif',
    color: '#1b74e4',
  }, // Niebieski FB
  {
    name: 'love',
    labelKey: 'reaction.love',
    emoji: '❤️',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif',
    color: '#f33e58',
  }, // Malinowy FB
  {
    name: 'haha',
    labelKey: 'reaction.haha',
    emoji: '😆',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif',
    color: '#f59e0b',
  }, // Żółty
  {
    name: 'wow',
    labelKey: 'reaction.wow',
    emoji: '😮',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif',
    color: '#f59e0b',
  }, // Żółty
  {
    name: 'sad',
    labelKey: 'reaction.sad',
    emoji: '😢',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif',
    color: '#60a5fa',
  }, // Jasnoniebieski
  {
    name: 'angry',
    labelKey: 'reaction.angry',
    emoji: '😡',
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif',
    color: '#ea580c',
  }, // Pomarańczowo-czerwony
]

const reactions = computed(() =>
  reactionConfigs.map((config) => ({
    ...config,
    label: t(config.labelKey),
  })),
)

const isVisible = ref(false)

const getReactionEmoji = (name: string) => {
  return reactionConfigs.find((r) => r.name === name)?.emoji || ''
}

const getReactionLabel = (name: string) => {
  const config = reactionConfigs.find((r) => r.name === name)
  return config ? t(config.labelKey) : ''
}

// Funkcja zwracająca czysty HEX, który Tailwind na pewno zinterpretuje poprawnie w :style
const getReactionColorHex = (name: string) => {
  return reactionConfigs.find((r) => r.name === name)?.color || '#1b74e4'
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

const { start: startHideTimer, stop: stopHideTimer } = useTimeoutFn(
  () => {
    isVisible.value = false
  },
  500,
  { immediate: false },
)

const { start: startShowTimer, stop: stopShowTimer } = useTimeoutFn(
  () => {
    isVisible.value = true
  },
  500,
  { immediate: false },
)

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

<style scoped>
/* Definicja animacji sprężystego powiększenia (pop) */
@keyframes pop-animation {
  0% {
    transform: scale(1);
  }
  50% {
    rotate: -7deg;
    transform: scale(1.25);
  }
  100% {
    transform: scale(1);
  }
}

/* Klasa, która odpala animację */
.animate-pop {
  animation: pop-animation 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
  /* cubic-bezier dodaje efekt "sprężyny" przy powiększaniu */
}
</style>
