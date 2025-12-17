<template>
  <div class="reaction-wrapper flex justify-center " @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
   <button
      class="flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px] w-full"
      @click="toggleReaction('like')"
   >
      <template v-if="selectedReaction">
        <img :src="getReactionSrc(selectedReaction)" :alt="selectedReaction" width="20" height="20">
        <span :class="getReactionTextColor(selectedReaction)">{{ getReactionLabel(selectedReaction) }}</span>
      </template>
      <template v-else>
        <ThumbUpOutline fillColor="#65676C" :size="20" class="text-gray-500 dark:text-gray-400"/>
        <span>{{ $t('actions.like') }}</span>
      </template>
   </button>


    <!-- Reaction box -->
    <div class="reaction-box bg-theme-bg-secondary" :class="{ visible: isVisible }">
      <div
        v-for="reaction in reactions"
        :key="reaction.name"
        class="reaction-item"
        @click="selectReaction(reaction.name)"
      >
        <div class="icon-wrapper">
          <div class="reaction-icon" :class="reaction.name" v-tooltip="reaction.label">
            <img :src="reaction.src" alt="😄" width="37" height="37">
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

const { t } = useI18n()

const emit = defineEmits<{
  (e: 'react', reaction: string | null): void
}>()

const reactionConfigs = [
  { name: 'like', labelKey: 'reaction.like', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif", color: 'text-blue-500' },
  { name: 'love', labelKey: 'reaction.love', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif", color: 'text-red-500' },
  { name: 'haha', labelKey: 'reaction.haha', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif", color: 'text-yellow-500' },
  { name: 'wow', labelKey: 'reaction.wow', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif", color: 'text-yellow-500' },
  { name: 'sad', labelKey: 'reaction.sad', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif", color: 'text-yellow-500' },
  { name: 'angry', labelKey: 'reaction.angry', src:"https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif", color: 'text-orange-500' }
]

const reactions = computed(() =>
  reactionConfigs.map(config => ({
    ...config,
    label: t(config.labelKey)
  }))
)

const selectedReaction = ref<string | null>(null)
const isVisible = ref(false)

const getReactionSrc = (name: string) => {
  return reactionConfigs.find(r => r.name === name)?.src || ''
}

const getReactionLabel = (name: string) => {
  const config = reactionConfigs.find(r => r.name === name)
  return config ? t(config.labelKey) : ''
}

const getReactionTextColor = (name: string) => {
  return reactionConfigs.find(r => r.name === name)?.color || 'text-blue-500'
}

const selectReaction = (reactionName: string) => {
  if (selectedReaction.value === reactionName) {
    selectedReaction.value = null
    emit('react', null)
  } else {
    selectedReaction.value = reactionName
    emit('react', reactionName)
  }
  isVisible.value = false
}

const toggleReaction = (defaultReaction: string) => {
  if (selectedReaction.value) {
    selectedReaction.value = null
    emit('react', null)
  } else {
    selectedReaction.value = defaultReaction
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

<style>
/* Wrapper */
.reaction-wrapper {
  position: relative;

  font-family: sans-serif;
  user-select: none;
  width: 100%;
}

/* Button */
.like-btn {
  width: 60px;
  height: 30px;

  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  z-index: 10;
}

/* Reaction box */
.reaction-box {
  position: absolute;
  bottom: 40px;
  left: 0;
  display: flex;
  gap: 6px;
  padding: 6px 10px;

  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);

  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
  z-index: 5;
}

.reaction-box.visible {
  opacity: 1;
  pointer-events: auto;
}

/* Reaction item */
.reaction-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Icon wrapper - kontroluje pojawianie się */
.icon-wrapper {
  opacity: 0;
  transform: scale(0.3) translateY(10px);
  transition: transform 0.35s cubic-bezier(0.34,1.56,0.64,1), opacity 0.35s ease;
}

/* Sekwencyjne pojawianie się */
.reaction-box.visible .reaction-item:nth-child(1) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0s; }
.reaction-box.visible .reaction-item:nth-child(2) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0.06s; }
.reaction-box.visible .reaction-item:nth-child(3) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0.12s; }
.reaction-box.visible .reaction-item:nth-child(4) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0.18s; }
.reaction-box.visible .reaction-item:nth-child(5) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0.24s; }
.reaction-box.visible .reaction-item:nth-child(6) .icon-wrapper { opacity: 1; transform: scale(1) translateY(0); transition-delay: 0.3s; }

/* Ikona - kontroluje hover float */
.reaction-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-items: center;
  transition: transform 0.35s cubic-bezier(0.34,1.56,0.64,1);
}

/* Hover float */
.reaction-icon:hover {
  transform: scale(1.25) translateY(-8px);
}

/* Label */
.reaction-label {
  font-size: 10px;
  color: #555;
  margin-top: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.reaction-box.visible .reaction-label {
  opacity: 1;
  transition-delay: 0.3s;
}


</style>
