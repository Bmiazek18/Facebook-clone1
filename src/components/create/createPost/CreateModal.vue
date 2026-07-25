<script setup lang="ts">
import { computed, watch, onMounted, onBeforeUnmount, type Component, ref, nextTick } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import PostCreator from './tabs/PostCreator.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'
import TagUsers from './tabs/TagUsers.vue'
import LocationSelector from './tabs/LocationSelector.vue'
import GifSelector from './tabs/GifSelector.vue'
import ImageEditor from './tabs/ImageEditor.vue'
import VideoEditor from './tabs/VideoEditor.vue'
import FeelingModal from './tabs/FeelingModal.vue'
import LifeEventCreator from './tabs/LifeEventCreator.vue'
import '@/assets/animations/slideTransition.css'
import { useI18n } from 'vue-i18n'

// --- Typy ---
import type { PostData } from '@/types/StoryElement'

type ViewKey =
  | 'creator'
  | 'privacy'
  | 'tagUsers'
  | 'location'
  | 'gifSelector'
  | 'imageEditor'
  | 'videoEditor'
  | 'feeling'
  | 'lifeEvent'

const props = defineProps<{
  sharedPost?: PostData | null
  sharedEventId?: string
  targetId?: string
  targetType?: 'User' | 'Group' | 'Event' | 'event'
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'publish', content: string): void
  (e: 'update:showBack', value: boolean): void
  (e: 'update:title', value: string): void
}>()

const { t } = useI18n()
const createPostStore = useCreatePostStore()

// --- Konfiguracja Widoków ---
const VIEW_CONFIG: Record<
  ViewKey,
  { component: Component; titleKey: string; widthClass?: string }
> = {
  creator: {
    component: PostCreator,
    titleKey: 'post.createPost',
    widthClass: 'w-full sm:w-125 p-2 sm:p-4',
  },
  privacy: {
    component: PrivacySelector,
    titleKey: 'post.selectPrivacy',
    widthClass: 'w-full sm:w-125 p-2 sm:p-3',
  },
  tagUsers: {
    component: TagUsers,
    titleKey: 'post.tagUsers',
    widthClass: 'w-full sm:w-125 p-2 sm:p-4',
  },
  location: {
    component: LocationSelector,
    titleKey: 'post.addLocation',
    widthClass: 'w-full sm:w-125 p-2 sm:p-4',
  },
  gifSelector: {
    component: GifSelector,
    titleKey: 'post.selectGif',
    widthClass: 'w-full sm:w-125 ',
  },
  imageEditor: {
    component: ImageEditor,
    titleKey: 'post.editImage',
    widthClass: 'w-full lg:w-[80vw]',
  },
  videoEditor: {
    component: VideoEditor,
    titleKey: 'post.editVideo',
    widthClass: 'w-full lg:w-500 p-2 sm:p-4',
  },
  feeling: {
    component: FeelingModal,
    titleKey: 'post.feelingActivity',
    widthClass: 'w-full sm:w-125 p-1',
  },
  lifeEvent: {
    component: LifeEventCreator,
    titleKey: 'profile.addLifeEvent',
    widthClass: 'w-full sm:w-125 p-2 sm:p-4',
  },
}

// --- Konfiguracja Widoku Początkowego ---
const initialViewName = (
  createPostStore.uiState.initialView && VIEW_CONFIG[createPostStore.uiState.initialView as ViewKey]
    ? createPostStore.uiState.initialView
    : 'creator'
) as ViewKey

createPostStore.setInitialView(initialViewName)

const currentView = computed(() => createPostStore.currentView as ViewKey)
const transitionName = computed(() => createPostStore.transitionName)
const navigateTo = createPostStore.navigateTo
const navigateBack = createPostStore.navigateBack

const wrapperRef = ref<HTMLElement | null>(null)

const baseOnEnter = (el: Element) => {
  const element = el as HTMLElement
  nextTick(() => {
    if (wrapperRef.value) {
      wrapperRef.value.style.height = `${element.offsetHeight}px`
    }
  })
}

const onAfterEnter = () => {
  if (wrapperRef.value) {
    wrapperRef.value.style.height = ''
  }
}

// --- RESIZE OBSERVER (FIX WYSOKOŚCI) ---
const resizeObserver = ref<ResizeObserver | null>(null)
const isTransitioning = ref(false)

const onEnterWithObserver = (el: Element) => {
  isTransitioning.value = true
  // 1. Najpierw ustawiamy wysokość startową (tak jak wcześniej)
  baseOnEnter(el)

  // 2. Czyścimy stary observer
  if (resizeObserver.value) resizeObserver.value.disconnect()

  // 3. Tworzymy nowy observer dla aktywnego widoku
  resizeObserver.value = new ResizeObserver((entries) => {
    for (const entry of entries) {
      if (wrapperRef.value) {
        // Aktualizujemy wysokość wrappera, gdy zmieni się wysokość dziecka (np. załadowanie obrazka)
        wrapperRef.value.style.height = `${entry.contentRect.height}px`
      }
    }
  })

  // 4. Zaczynamy obserwować element, który właśnie wchodzi
  resizeObserver.value.observe(el)
}

const onAfterEnterWithObserver = () => {
  isTransitioning.value = false
  onAfterEnter()
}

// Sprzątanie observera przy odmontowaniu komponentu
onBeforeUnmount(() => {
  if (resizeObserver.value) resizeObserver.value.disconnect()
})

// --- Computed & Watchers ---
const activeViewConfig = computed(
  () => VIEW_CONFIG[currentView.value as ViewKey] || VIEW_CONFIG.creator,
)
const activeComponent = computed(() => activeViewConfig.value.component)

const dynamicProps = computed(() => {
  if (currentView.value === 'creator') {
    return {
      sharedPost: props.sharedPost,
      sharedEventId: props.sharedEventId,
    }
  }
  return {}
})

watch(
  () => currentView.value,
  (newView) => {
    emit('update:showBack', newView !== 'creator')
    const config = VIEW_CONFIG[newView as ViewKey]
    if (config) emit('update:title', t(config.titleKey))
  },
  { immediate: true },
)

watch(
  () => props.sharedPost,
  (newVal) => {
    createPostStore.postData.sharedPost = newVal || null
  },
  { immediate: true },
)

// --- Nawigacja ---
const activeComponentRef = ref<any>(null)

const goBack = () => {
  if (activeComponentRef.value && typeof activeComponentRef.value.goBack === 'function') {
    const handled = activeComponentRef.value.goBack()
    if (handled) return
  }
  if (currentView.value === 'imageEditor') createPostStore.setImageToEdit(null)
  if (currentView.value === 'videoEditor') createPostStore.uiState.videoToEdit = null
  navigateBack()
}

defineExpose({ goBack })

// --- Handlery ---
const handleClose = () => {
  createPostStore.reset()
  emit('close')
}

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  createPostStore.postData.privacy = payload.id
  if (payload.setDefault) {
    localStorage.setItem('fc_default_privacy', payload.id)
  }
  navigateBack()
}

// --- Init ---
onMounted(() => {
  if (props.targetId && props.targetType) {
    createPostStore.setTarget(props.targetId, props.targetType)
  }
  const savedPrivacy = localStorage.getItem('fc_default_privacy')
  if (savedPrivacy) createPostStore.postData.privacy = savedPrivacy
})
</script>

<template>
  <div
    :class="[
      activeViewConfig.widthClass,
      ' mx-auto rounded-xl relative overflow-hidden',
      { 'transition-[width] duration-300 ease-in-out': isTransitioning },
    ]"
  >
    <div
      class="relative"
      :class="{ 'transition-[height] duration-300 ease-in-out': isTransitioning }"
      ref="wrapperRef"
      style="min-height: 100px"
    >
      <Transition
        :name="transitionName"
        @enter="onEnterWithObserver"
        @after-enter="onAfterEnterWithObserver"
      >
        <component
          ref="activeComponentRef"
          :is="activeComponent"
          :key="currentView"
          class="view-container bg-theme-bg-secondary"
          v-bind="dynamicProps"
          @publish="
            (content) => {
              emit('publish', content)
              createPostStore.reset()
            }
          "
          @close="handleClose"
          @back="goBack"
          @confirm="handlePrivacyConfirm"
        />
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  width: 100%;
  top: 0;
  left: 0;
}

/* Animacje Slide */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition:
    transform 0.3s cubic-bezier(0.25, 1, 0.5, 1),
    opacity 0.3s ease;
  position: absolute;
  width: 100%;
}
.slide-left-enter-from {
  transform: translateX(100%);
}
.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}
.slide-right-enter-from {
  transform: translateX(-100%);
}
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
