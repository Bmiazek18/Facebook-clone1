<template>
  <div
    class="w-[500px] mx-auto bg-theme-bg-secondary rounded-xl shadow-lg border border-theme-border   overflow-hidden"
  >
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <!-- Main View -->
        <div
          v-if="currentView === 'main'"
          key="main"
          class="view-container bg-theme-bg-secondary"
          data-view="main"
        >
          <div class="p-4 flex items-start gap-3">
            <img
              :src="postsStore.currentUser.avatar"
              class="w-10 h-10 rounded-full"
              alt="Profile"
            />
            <div class="flex-1">
              <h3 class="font-bold text-theme-text">{{ postsStore.currentUser.name }}</h3>
              <div class="flex gap-2 mt-1 text-[13px] font-semibold text-theme-text-secondary">
                <button class="bg-theme-bg-tertiary px-3 py-1 rounded-md">Aktualności</button>
                <button
                  @click="openPrivacySelector"
                  class="bg-theme-bg-tertiary px-3 py-1 rounded-md flex items-center gap-1 hover:bg-theme-hover transition-colors"
                >
                  <component :is="selectedPrivacy.icon" :size="14" />
                  {{ selectedPrivacy.label }}
                  <ChevronDownIcon :size="16" />
                </button>
              </div>
            </div>
          </div>

          <div class="px-4 py-2 relative">
            <textarea
              v-model="textareaContent"
              placeholder="Napisz coś o tym..."
              class="w-full h-20 outline-none resize-none text-[17px] pr-10 bg-transparent text-theme-text"
            ></textarea>

            <div class="absolute bottom-2 right-4 text-theme-text-secondary cursor-pointer">
              <VDropdown
                placement="top"
                :distance="10"
                :skidding="0"
                :triggers="['click']"
                :autoHide="true"
              >
                <EmoticonOutlineIcon :size="24" />

                <template #popper>
                  <div class="emoji-popper-content">
                    <LazyEmojiPicker @select="handleEmojiSelect" />
                  </div>
                </template>
              </VDropdown>
            </div>
          </div>

          <div class="px-4 pb-4">
            <button
              @click="handleShareNow"
              class="w-full bg-theme-primary text-white font-bold py-2 rounded-lg hover:bg-theme-primary-hover transition-colors"
            >
              Udostępnij teraz
            </button>
          </div>

          <hr class="border-theme-border" />

          <div class="p-4">
            <h4 class="font-bold mb-4 text-theme-text">Wyślij w Messengerze</h4>
            <div class="relative group">
              <!-- Left arrow -->
              <button
                v-if="!isStart"
                @click="scrollLeft"
                class="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-10 h-10 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border flex items-center justify-center hover:bg-theme-hover transition-colors"
              >
                <ChevronLeftIcon :size="24" fillColor="#4B5563" />
              </button>

              <!-- Carousel -->
              <div ref="carouselRef" class="flex gap-4 overflow-x-auto no-scrollbar scroll-smooth">
                <div
                  v-for="contact in contacts"
                  :key="contact.id"
                  class="flex flex-col items-center min-w-[72px] cursor-pointer group/contact"
                >
                  <div class="relative">
                    <img
                      :src="contact.avatar"
                      class="w-14 h-14 rounded-full border-2 border-theme-border group-hover/contact:border-theme-primary transition-colors"
                    />
                  </div>
                  <span class="text-[11px] mt-2 text-center line-clamp-1 w-full text-theme-text">{{
                    contact.name
                  }}</span>
                </div>
              </div>

              <!-- Right arrow -->
              <button
                v-if="!isEnd"
                @click="scrollRight"
                class="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-10 h-10 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border flex items-center justify-center hover:bg-theme-hover transition-colors"
              >
                <ChevronRightIcon :size="24" fillColor="#4B5563" />
              </button>
            </div>
          </div>

          <hr class="border-theme-border" />

          <div class="p-4">
            <h4 class="font-bold mb-4 text-theme-text">Udostępnij</h4>
            <div class="flex gap-3 overflow-x-auto no-scrollbar">
              <button
                v-for="action in shareActions"
                :key="action.label"
                @click="handleShareAction(action.id)"
                class="flex flex-col items-center gap-1 px-[5px] shrink-0 cursor-pointer group"
              >
                <div
                  class="w-12 h-12 bg-theme-bg-tertiary group-hover:bg-theme-hover rounded-full flex items-center justify-center text-theme-text-secondary transition-colors"
                >
                  <component :is="action.icon" :size="24" />
                </div>
                <span class="text-[11px] font-medium text-theme-text text-center">{{
                  action.label
                }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Privacy Selector View -->
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-theme-bg-secondary"
          data-view="privacy"
          @back="handlePrivacyBack"
          @confirm="handlePrivacyConfirm"
        />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useCarousel } from '@/composables/media/useCarousel'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import { getAllUsers } from '@/utils/users'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import { usePostsStore } from '@/composables/feed/useAppState'
import type { Reel } from '@/types/Reel'
import type { Post } from '@/types/Post'
import '@/assets/animations/slideTransition.css'

// Import poszczególnych ikon jako komponenty
import LockIcon from 'vue-material-design-icons/Lock.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import EmoticonOutlineIcon from 'vue-material-design-icons/EmoticonOutline.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue'
import WhatsappIcon from 'vue-material-design-icons/Whatsapp.vue'
import BookOpenVariantIcon from 'vue-material-design-icons/BookOpenVariant.vue'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'

const props = defineProps<{
  reel?: Reel | null
  marketplaceItem?: {
    id: string
    title: string
    price: string
    location: string
    images: string[]
    description: string
  } | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:showBack', value: boolean): void
  (e: 'update:title', value: string): void
}>()

const postsStore = usePostsStore()
const router = useRouter()

const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition()

// Initialize view to 'main'
currentView.value = 'main'

const showBack = computed(() => currentView.value !== 'main')
watch(showBack, (newValue) => {
  emit('update:showBack', newValue)
})

const viewTitles: Record<string, string> = {
  main: 'Udostępnij',
  privacy: 'Wybierz prywatność',
}

const currentTitle = computed(() => viewTitles[currentView.value] || '')
watch(currentTitle, (newTitle) => {
  emit('update:title', newTitle)
})

const showEmojiPicker = ref(false)
const textareaContent = ref('')
const selectedPrivacy = ref({ icon: LockIcon, label: 'Tylko ja' })

// Get all users from data
const allUsers = getAllUsers()
const contacts = ref(allUsers.slice(0, 10)) // First 10 users

const shareActions = [
  { id: 'messenger', label: 'Messenger', icon: FacebookMessengerIcon },
  { id: 'whatsapp', label: 'WhatsApp', icon: WhatsappIcon },
  { id: 'story', label: 'Twoja relacja', icon: BookOpenVariantIcon },
  { id: 'copy_link', label: 'Kopiuj link', icon: LinkVariantIcon },
  { id: 'group', label: 'Grupa', icon: AccountGroupIcon },
  { id: 'profile', label: 'Profil', icon: AccountCircleOutlineIcon },
]

const { carouselRef, isStart, isEnd, scrollLeft, scrollRight } = useCarousel(4)

const handleShareAction = async (actionId: string) => {
  if (actionId === 'copy_link' && props.reel) {
    const reelUrl = `${window.location.origin}/reel/${props.reel.id}`

    try {
      await navigator.clipboard.writeText(reelUrl)
      // Możesz tutaj dodać toast/notification o skopiowaniu
      console.log('Link skopiowany:', reelUrl)
    } catch (err) {
      console.error('Nie udało się skopiować linku:', err)
    }
  }
  // Możesz dodać inne akcje dla pozostałych buttonów
}

const handleEmojiSelect = (emoji: { native: string }) => {
  textareaContent.value += emoji.native
  showEmojiPicker.value = false
}

const openPrivacySelector = () => {
  previousView.value = currentView.value
  currentView.value = 'privacy'
}

const handlePrivacyBack = () => {
  previousView.value = currentView.value
  currentView.value = 'main'
}

const goBack = () => {
  handlePrivacyBack()
}
defineExpose({ goBack })

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  // Map privacy ID to icon and label
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const privacyMap: Record<string, { icon: any; label: string }> = {
    only_me: { icon: LockIcon, label: 'Tylko ja' },
    public: { icon: EarthIcon, label: 'Publiczne' },
    friends: { icon: AccountGroupIcon, label: 'Znajomi' },
    friends_except: { icon: AccountMultipleMinusIcon, label: 'Znajomi z wyjątkiem...' },
    specific_friends: { icon: AccountStarIcon, label: 'Konkretni znajomi' },
  }

  selectedPrivacy.value = privacyMap[payload.id] || { icon: LockIcon, label: 'Tylko ja' }
  previousView.value = currentView.value
  currentView.value = 'main'
}

const handleShareNow = () => {
  // Obsługa udostępniania Reel
  if (props.reel) {
    const newPost: Post = {
      id: `post_${Date.now()}`,
      authorId: postsStore.currentUser.id,
      content: textareaContent.value,
      date: new Date().toISOString(),
      timestamp: Date.now(),
      media: {
        images: [],
        videoUrl: props.reel.videoSrc,
      },
      context: {
        privacy: selectedPrivacy.value.label === 'Tylko ja' ? 'only_me' : 'public',
      },
      stats: {
        comments: 0,
        shares: 0,
      },
      reactions: {},
      sharedContent: {
        type: 'reel',
        originalId: props.reel.id,
      },
    }
    postsStore.addPost(newPost)
    emit('close')
    router.push('/')
    return
  }

  // Obsługa udostępniania produktu z Marketplace
  if (props.marketplaceItem) {
    const newPost: Post = {
      id: `marketplace_${Date.now()}`,
      authorId: postsStore.currentUser.id,
      content: textareaContent.value,
      date: new Date().toISOString(),
      timestamp: Date.now(),
      media: {
        images:
          props.marketplaceItem.images.length > 0
            ? props.marketplaceItem.images.map((img: string) => ({
                src: img,
                altText: props.marketplaceItem!.title,
              }))
            : [],
      },
      context: {
        privacy: selectedPrivacy.value.label === 'Tylko ja' ? 'only_me' : 'public',
        location: {
          title: props.marketplaceItem.location,
          subtitle: '', // Default subtitle
          type: 'place', // Assuming 'place' is a valid PostLocationType
          lat: null,
          lon: null,
        },
      },
      stats: {
        comments: 0,
        shares: 0,
      },
      reactions: {},
      // sharedContent will be undefined as 'marketplace' is not a valid type
      // Custom marketplaceData field (handled as any in PostItem)
      marketplaceData: {
        title: props.marketplaceItem.title,
        price: props.marketplaceItem.price,
        location: props.marketplaceItem.location,
        itemId: props.marketplaceItem.id,
        description: props.marketplaceItem.description,
      },
    } as Post
    postsStore.addPost(newPost)
    emit('close')
    router.push('/')
    return
  }
}
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
