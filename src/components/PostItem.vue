<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import Cog from 'vue-material-design-icons/Cog.vue'
import Heart from 'vue-material-design-icons/Heart.vue'

import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import PlayCircleOutline from 'vue-material-design-icons/PlayCircleOutline.vue'
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import VolumeMute from 'vue-material-design-icons/VolumeMute.vue'
import Play from 'vue-material-design-icons/Play.vue'
import Pause from 'vue-material-design-icons/Pause.vue'

import PostImageGallery from './PostImageGallery.vue'
import PlayerVideo from './PlayerVideo.vue'
import { usePostReactions } from '@/composables/usePostReactions'
import { useVideoAutoplay } from '@/composables/useVideoAutoplay'
import BaseModal from './BaseModal.vue'
import PostModal from './PostModal.vue'
import ShareAsPostModal from './ShareAsPostModal.vue'
import PostHeader from './post/PostHeader.vue'
import PostActions from './post/PostActions.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore } from '@/stores/posts'
import { useEventsStore } from '@/stores/events'
import { useReelsStore } from '@/stores/reels'

import { getUserById } from '@/data/users'
import { processContent } from '@/utils/contentProcessor'

useI18n()

import type { Post } from '@/types/Post';
import ShareAsMessageModal from './ShareAsMessageModal.vue'


const props = defineProps<{
  post: Post
  isShared?: boolean
}>()

const emit = defineEmits<{
  (e: 'delete', postId: string): void
}>()

const router = useRouter()
const storyShareStore = useStoryShareStore()
const postsStore = usePostsStore()
const eventsStore = useEventsStore()
const reelsStore = useReelsStore()

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)

// Reel controls
const isReelMuted = ref(true)
const isReelPaused = ref(false)
const reelVideoRef = ref<HTMLVideoElement | null>(null)

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}


import Star from 'vue-material-design-icons/Star.vue' // <--- NOWE

// ... (reszta kodu)

const rating = ref(0)       // Zapisana ocena (po kliknięciu)
const hoverRating = ref(0)  // Tymczasowa ocena (przy najechaniu)

// Funkcja określająca, czy gwiazdka o indeksie 'index' ma być pełna
const isStarFilled = (index: number) => {
  // Jeśli najeżdżamy, użyj wartości hover, w przeciwnym razie użyj zapisanej oceny
  const activeRating = hoverRating.value > 0 ? hoverRating.value : rating.value
  return index <= activeRating
}

// Obsługa kliknięcia
const setRating = (value: number) => {
  rating.value = value
  // Tu możesz dodać wysyłanie oceny do API
  console.log(`Oceniono tłumaczenie na: ${value}`)
}
const isInterested = ref(false)

const toggleInterest = () => {
  isInterested.value = !isInterested.value
  // Tutaj możesz dodać wywołanie API, np.: eventsStore.toggleInterest(eventId)
}

interface CardBackground { id: number; class: string; textClass?: string }
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
];

const currentBackground = computed(() => {
  return cardBackgrounds.find(bg => bg.id === props.post?.selectedCardBgId) ?? cardBackgrounds[0]
})

const postData = computed<Post>(() => {
  return {
    id: String(props.post?.id || Date.now()),
    authorName: props.post?.authorName,
    authorAvatar: props.post?.authorAvatar,
    content: props.post?.content,
    imageUrl: props.post?.imageUrl,
    images: props.post?.images,
    videoUrl: props.post?.videoUrl,
    authorId: props.post?.authorId ?? 0,
    date: props.post?.date ?? '',
    likesCount: props.post?.likesCount ?? 0,
    commentsCount: props.post?.commentsCount ?? 0,
    sharesCount: props.post?.sharesCount ?? 0,
    timestamp: props.post?.timestamp ?? Date.now(),
    taggedUsers: props.post?.taggedUsers ?? [],
    location: props.post?.location,
    gif: props.post?.gif,
    isLiked: props.post?.isLiked ?? false,
    likedType: props.post?.likedType ?? null,
    reactionCount: props.post?.reactionCount ?? 0,
    commentCount: props.post?.commentCount ?? 0,
    comments: props.post?.comments ?? [],
    selectedCardBgId: props.post?.selectedCardBgId ?? 0,
    privacy: props.post?.privacy ?? '',
    feeling: props.post?.feeling,
    activity: props.post?.activity,
    sharedFromId: props.post?.sharedFromId,
    sharedEventId: props.post?.sharedEventId,
    createdEvent: props.post?.createdEvent
  }
})

const { t } = useI18n()

// Stan tłumaczenia
const isTranslated = ref(false)
const translatedContent = ref<string>('')
const isTranslating = ref(false)
const translationError = ref(false)

// Sprawdź czy post wymaga tłumaczenia (język inny niż polski)
const needsTranslation = computed(() => {
  return props.post.detectedLanguage && props.post.detectedLanguage !== 'pl'
})



// Treść do wyświetlenia (oryginał lub tłumaczenie)
const displayContent = computed(() => {
  return isTranslated.value ? translatedContent.value : props.post.content
})

const processedContent = computed(() => {
  return processContent(displayContent.value);
});
// 1. Dodaj nową zmienną stanu
const isOriginalVisible = ref(false)

// 2. Zaktualizuj funkcję translatePost (żeby ukrywała oryginał przy nowym tłumaczeniu)
const translatePost = async () => {
  if (isTranslating.value || isTranslated.value) return

  try {
    isTranslating.value = true
    translationError.value = false

    const { data } = await axios.post('http://127.0.0.1:8000/translate', {
      text: props.post.content,
      sourceLanguage: props.post.detectedLanguage,
      targetLanguage: 'pl'
    })
    console.log('Otrzymane dane tłumaczenia:', data)
    translatedContent.value = data.translatedText
    isTranslated.value = true
    isOriginalVisible.value = false // <--- WAŻNE: Ukryj oryginał po przetłumaczeniu
  } catch (error) {
    console.error('Błąd tłumaczenia:', error)
    translationError.value = true
  } finally {
    isTranslating.value = false
  }
}

// 3. Zmień funkcję showOriginal (teraz działa jak przełącznik widoczności)
const showOriginal = () => {
  isOriginalVisible.value = !isOriginalVisible.value
}

// 4. Dodaj osobne computed properties dla treści (zamiast jednego displayContent)
// (Możesz usunąć stare 'displayContent' i 'processedContent' jeśli ich nie używasz gdzie indziej)
const processedOriginalContent = computed(() => {
  return processContent(props.post.content);
});

const processedTranslatedContent = computed(() => {
  return processContent(translatedContent.value);
});

const shareToStory = () => {
  storyShareStore.setPostToShare(postData.value)
  router.push('/stories/create')
}

const shareAsMyPost = () => {
  isShareAsPostModalOpen.value = true
}

const handleShareAsPost = (comment: string) => {

  postsStore.sharePost(postToShare.value, comment)
  isShareAsPostModalOpen.value = false
  router.push('/profile')
}

const handleDelete = () => {
  emit('delete', props.post.id)
}
const isShareAsMessageModalOpen = ref(false);
const shareToMessage = () => {
  isShareAsMessageModalOpen.value = true;
};
// Handlers for PostSettingPopper actions
const handleDeletePost = (postId: string) => {
    postsStore.removePost(postId);
}

const handleEditPost = (postId: number) => {
  console.log('Edit Post:', postId);
  // Implement actual edit logic here (e.g., open an edit modal)
};

const handleHidePost = (postId: number) => {
  console.log('Hide Post:', postId);
  // Implement actual hide logic here
};

const originalPost = computed(() => {
  if (props.post.sharedFromId) {
    return postsStore.getPostById(props.post.sharedFromId);
  }
  return undefined;
});

const sharedEvent = computed(() => {
  if (props.post.sharedEventId) {
    return eventsStore.getEventById(props.post.sharedEventId);
  }
  return undefined;
});

const sharedReel = computed(() => {
  if (props.post.sharedReelId) {
    return reelsStore.getReelById(props.post.sharedReelId);
  }
  return undefined;
});

// Reel control functions
const toggleReelMute = () => {
  isReelMuted.value = !isReelMuted.value;
  if (reelVideoRef.value) {
    reelVideoRef.value.muted = isReelMuted.value;
  }
};

const toggleReelPause = () => {
  if (!reelVideoRef.value) return;

  if (isReelPaused.value) {
    reelVideoRef.value.play();
    isReelPaused.value = false;
  } else {
    reelVideoRef.value.pause();
    isReelPaused.value = true;
  }
};

const postToShare = computed(() => {
  return originalPost.value || props.post;
});

// Reactions - using composable
const { userReaction, likesCount, handleReaction } = usePostReactions(24)


// Konfiguracja wyglądu reakcji
const getReactionConfig = (type: string) => {
  switch (type) {
    // 1. IKONY Z TŁEM (Like, Love)
    case 'like':
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]', // Niebieskie tło
        color: '#FFFFFF'
      }
    case 'love':
      return {
        mode: 'icon',
        component: Heart,
        wrapperClass: 'bg-[#F3425F]', // Czerwone tło
        color: '#FFFFFF'
      }

    // 2. ZWYKŁE EMOTKI (Haha, Wow, Sad, Angry)
    // Tło jest 'bg-white', aby przykryć ikonę pod spodem, ale dla oka to "sama emotka"
    case 'haha':
      return { mode: 'emoji', char: '😆', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'wow':
      return { mode: 'emoji', char: '😮', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'sad':
      return { mode: 'emoji', char: '😢', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'angry':
      return { mode: 'emoji', char: '😡', wrapperClass: 'bg-white dark:bg-[#242526]' }

    // Domyślny fallback
    default:
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
  }
}
// Video autoplay - using composable
const videoContainerRef = ref<HTMLElement | null>(null)
const videoRef = ref<InstanceType<typeof PlayerVideo> | null>(null)
useVideoAutoplay(videoContainerRef)
</script>

<template>
  <div class="w-full bg-theme-bg-secondary rounded-lg"
       :class="isShared ? 'border border-theme-border' : 'my-4 shadow-sm dark:shadow-lg'">

    <template v-if="!isShared">
    <PostHeader
      :author-name="post.authorName"
      :author-avatar="post.authorAvatar"
      :author-id="post.authorId"
      :tagged-users="post.taggedUsers"
      :date="post.date"
      :privacy="post.privacy"
      :post-id="post.id"
      :feeling="post.feeling"
      :activity="post.activity"
      :is-shared="isShared"
      :created-event="post.createdEvent"
      @delete-post="handleDeletePost"
      @edit-post="handleEditPost"
      @hide-post="handleHidePost"
    />
<div v-if="post.content && (!isTranslated || isOriginalVisible)"
         class="px-4 py-1 text-[15px] leading-normal whitespace-pre-line"
         :class="{
           [((currentBackground as CardBackground).class ?? '')]: (props.post?.selectedCardBgId ?? 0) !== 0,
           [((currentBackground as CardBackground).textClass ?? 'text-theme-text')]: (props.post?.selectedCardBgId ?? 0) !== 0,
           'p-4 h-[383px] flex items-center justify-center text-center': (props.post?.selectedCardBgId ?? 0) !== 0,
           'text-xl': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length <= 80,
           'text-base': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length > 80,
           'text-theme-text': (props.post?.selectedCardBgId ?? 0) === 0,
           'pb-3': !isTranslated // Padding standardowy gdy nie ma tłumaczenia
         }"
    >
      <template v-for="(part, index) in processedOriginalContent" :key="index">
        <router-link
          v-if="part.type === 'hashtag'"
          :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
          class="text-blue-500 hover:underline"
          :class="{ 'text-white ': (post.selectedCardBgId ?? 0) > 0 }"
        >
          {{ part.value }}
        </router-link>
        <router-link
          v-else-if="part.type === 'mention'"
          :to="{ name: 'userProfile', params: { userId: part.userId } }"
          class="text-blue-500 hover:underline"
          :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
        >
          @{{ getUserById(parseInt(part.userId || ''))?.name }}
        </router-link>
        <span v-else :class="{ ' text-[30px]': (post.selectedCardBgId ?? 0) > 0 }">{{ part.value }}</span>
      </template>
    </div>

    <div v-if="isTranslated || needsTranslation"
         :class="isTranslated ? 'ml-4 pl-3 border-l-[3px] border-[#dddfe2] dark:border-gray-600 pr-4 mt-1' : 'px-4'">

      <div v-if="isTranslated" class="py-1 pb-1 text-[15px] leading-normal whitespace-pre-line text-theme-text">
        <template v-for="(part, index) in processedTranslatedContent" :key="index">
             <router-link
              v-if="part.type === 'hashtag'"
              :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
              class="text-blue-500 hover:underline"
            >
              {{ part.value }}
            </router-link>
            <router-link
              v-else-if="part.type === 'mention'"
              :to="{ name: 'userProfile', params: { userId: part.userId } }"
              class="text-blue-500 hover:underline"
            >
              @{{ getUserById(parseInt(part.userId || ''))?.name }}
            </router-link>
            <span v-else>{{ part.value }}</span>
        </template>
      </div>

    <div v-if="needsTranslation" class="pb-3 pt-0" :class="!isTranslated ? 'px-4' : ''">

        <div v-if="isTranslated" class="flex items-center text-[13px] font-semibold leading-4">

          <VDropdown :distance="10" placement="bottom-start" theme="dropdown">
            <button class="mr-1.5 flex items-center justify-center text-[#1877F2] hover:bg-blue-50 rounded-full p-1 -ml-1 transition-colors">
              <Cog :size="16" />
            </button>

            <template #popper>
              <div class="w-[320px] py-2 text-[#050505] dark:text-[#E4E6EB] text-[15px]">

              <div class="flex flex-col items-center justify-center p-2 pb-3 border-b border-gray-200 dark:border-gray-700">
                  <span class="mb-2 font-medium">Oceń to tłumaczenie</span>

                  <div class="flex gap-1 mb-2" @mouseleave="hoverRating = 0">
                    <button
                      v-for="i in 5"
                      :key="i"
                      @click="setRating(i)"
                      @mouseenter="hoverRating = i"
                      class="transition-transform hover:scale-110 focus:outline-none"
                    >
                       <component
                         :is="isStarFilled(i) ? Star : StarOutline"
                         :size="32"
                         class="text-[#1877F2] transition-colors duration-200"
                       />
                    </button>
                  </div>

                  <span class="text-[13px] text-gray-500">
                    {{ rating > 0 ? 'Dziękujemy za ocenę!' : 'Kliknij gwiazdkę, aby ocenić' }}
                  </span>
                </div>

                <div class="mt-2">
                  <button class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors">
                    <Close :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
                    <div class="flex flex-col">
                      <span class="font-medium leading-tight">Nigdy nie tłumacz z języka: {{ post.detectedLanguage }}</span>
                      <span class="text-[13px] text-gray-500 mt-0.5">Tłumaczenie z języka: {{ post.detectedLanguage }} na polski</span>
                    </div>
                  </button>

                  <button class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors">
                    <MinusCircle :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
                    <div class="flex flex-col">
                      <span class="font-medium leading-tight">Post nie był w języku: {{ post.detectedLanguage }}</span>
                      <span class="text-[13px] text-gray-500 mt-0.5">Zgłoś błąd</span>
                    </div>
                  </button>

                  <button class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors">
                    <Cog :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
                    <div class="flex flex-col justify-center h-full">
                      <span class="font-medium mt-1">Ustawienia języka</span>
                    </div>
                  </button>
                </div>

              </div>
            </template>
          </VDropdown>
          <button @click="showOriginal" class="text-[#1877F2] hover:underline cursor-pointer bg-transparent border-none p-0">
            {{ isOriginalVisible ? 'Ukryj oryginalny tekst' : 'Zobacz oryginalny tekst' }}
          </button>

          <span class="text-[#65676B] dark:text-[#B0B3B8] px-1">·</span>

          <VDropdown :distance="10" placement="bottom-start">
             <button class="text-[#1877F2] hover:underline cursor-pointer bg-transparent border-none p-0">
              Oceń to tłumaczenie
            </button>
             <template #popper>
               <div class="p-4 text-center">Funkcja oceniania...</div>
             </template>
          </VDropdown>

        </div>

        <div v-else>
           <button
              @click="translatePost"
              class="text-[13px] font-semibold text-[#1877F2] hover:underline bg-transparent border-none p-0"
              :disabled="isTranslating"
            >
            {{ isTranslating ? 'Tłumaczenie...' : 'Zobacz tłumaczenie' }}
          </button>
           <span v-if="translationError" class="text-xs text-red-500 ml-2">
            (Błąd)
         </span>
        </div>
      </div>
    </div>

    <div v-if="!post.sharedFromId">
      <!-- Video -->
      <div v-if="post.videoUrl" ref="videoContainerRef" class="w-full">
        <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="post.videoUrl" />
      </div>

      <!-- Images -->
      <PostImageGallery v-else-if="post.images && post.images.length > 0" :images="post.images" :post-id="post.id ?? ''" />
    </div>
    </template>
    <template v-else>
        <div v-if="!post.sharedFromId">
            <!-- Video -->
            <div v-if="post.videoUrl" ref="videoContainerRef" class="w-full">
                <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="post.videoUrl" />
            </div>

            <!-- Images -->
            <PostImageGallery v-else-if="post.images && post.images.length > 0" :images="post.images" :post-id="post.id ?? ''" />
        </div>
    <PostHeader
      :author-name="post.authorName"
      :author-avatar="post.authorAvatar"
      :author-id="post.authorId"
      :tagged-users="post.taggedUsers"
      :date="post.date"
      :privacy="post.privacy"
      :post-id="post.id"
      :feeling="post.feeling"
      :activity="post.activity"
      :is-shared="isShared"
      @delete-post="handleDeletePost"
      @edit-post="handleEditPost"
      @hide-post="handleHidePost"
    />
        <div v-if="post.content" class="px-4 py-1 pb-3 text-[15px] leading-normal whitespace-pre-line"
             :class="{
           [((currentBackground as CardBackground).class ?? '')]: (props.post?.selectedCardBgId ?? 0) !== 0, // Apply background if ID is set
           [((currentBackground as CardBackground).textClass ?? 'text-theme-text')]: (props.post?.selectedCardBgId ?? 0) !== 0, // Apply text color if ID is set
           'p-4 h-[383px] flex items-center justify-center text-center': (props.post?.selectedCardBgId ?? 0) !== 0, // Enforce height and centering if ID is set
           'text-xl': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length <= 80,
           'text-base': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length > 80,
           'text-theme-text': (props.post?.selectedCardBgId ?? 0) === 0, // Default text color if no card background
             }"
        >
          <template v-for="(part, index) in processedContent" :key="index">
            <router-link
              v-if="part.type === 'hashtag'"
              :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
              class="text-blue-500 hover:underline"
              :class="{ 'text-white ': (post.selectedCardBgId ?? 0) > 0 }"
            >
              {{ part.value }}
            </router-link>
            <router-link
              v-else-if="part.type === 'mention'"
              :to="{ name: 'userProfile', params: { userId: part.userId } }"
              class="text-blue-500 hover:underline"
              :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
            >
              @{{ getUserById(parseInt(part.userId || ''))?.name }}
            </router-link>
            <span v-else :class="{ ' text-[30px]': (post.selectedCardBgId ?? 0) > 0 }">{{ part.value }}</span>
          </template>
        </div>
    </template>

    <div v-if="post.sharedFromId && originalPost" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
        <PostItem :post="originalPost" :is-shared="true" />
    </div>
<div v-if="post.sharedReelId && sharedReel"
         class=" mb-3 mt-2 relative w-auto h-[800px]  bg-gradient-to-b from-[#5c6b55] to-[#2e3b2b] rounded-xl overflow-hidden shadow-lg border border-gray-700/30 group cursor-pointer"
         @click="router.push(`/reel/${sharedReel.id}`)">

        <div class="absolute inset-0 w-full h-full">
             <video
              ref="reelVideoRef"
              class="w-[70%] mx-auto h-full object-cover block"
              :src="sharedReel.videoSrc"
              :poster="sharedReel.poster"
              autoplay
              loop
              playsinline
              :muted="isReelMuted"
            ></video>
            <div class="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/60 pointer-events-none"></div>
        </div>

        <div class="absolute top-4 left-4 flex items-center gap-3 z-20 pointer-events-none">
          <div class="w-10 h-10 rounded-full border-2 border-white/20 overflow-hidden bg-gray-300">
             <img :src="sharedReel.user.avatar" alt="Avatar" class="w-full h-full object-cover" />
          </div>

          <div class="flex flex-col text-white drop-shadow-md">
            <span class="font-bold text-sm leading-tight flex items-center gap-1">
              {{ sharedReel.user.name }} <span class="text-gray-300 font-normal text-xs opacity-90">• Obserwuj</span>
            </span>
            <div class="flex items-center gap-1 text-xs text-gray-200 opacity-80 mt-0.5">

              <earth :size="12" class="text-gray-200"/>
            </div>
          </div>
        </div>

        <!-- Control buttons -->
        <div class="absolute top-4 right-4 z-20 flex gap-2">
          <button
            @click.stop="toggleReelMute"
            class="p-2 bg-black/20 backdrop-blur-sm rounded-full text-white hover:bg-black/40 transition-colors"
          >
            <VolumeHigh v-if="!isReelMuted" :size="20" />
            <VolumeMute v-else :size="20" />
          </button>


        </div>


<div class="absolute bottom-6 left-4 right-16 z-20">
          <div class="flex items-center bg-black/30 backdrop-blur-md self-start px-3 py-2 rounded-full max-w-[220px] text-white border border-white/10">

            <div class="shrink-0 mr-2 flex items-center justify-center">
               <music-note :size="16" class="animate-pulse-slow" />
            </div>

            <div class="overflow-hidden w-full relative h-[16px] flex items-center">
               <div class="w-full overflow-hidden mask-[linear-gradient(90deg,transparent_0%,white_10%,white_90%,transparent_100%)]">

                  <div class="animate-marquee whitespace-nowrap text-xs font-medium tracking-wide">
                     {{ sharedReel.user.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {{ sharedReel.user.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  </div>

               </div>
            </div>

          </div>
        </div>

    </div>
<div v-if="post.sharedEventId && sharedEvent"
         class=" mb-4 mt-2  overflow-hidden cursor-pointer  bg-[#f0f2f5] border-b border-[#dadde1] transition-colors group"
         @click="router.push(`/event/${sharedEvent.id}`)">

      <div class="relative w-full aspect-[1.91/1] bg-gray-100 dark:bg-[#3A3B3C]">
        <img
          v-if="sharedEvent.images && sharedEvent.images[0]"
          :src="sharedEvent.images[0]"
          alt="Event cover"
          class="w-full h-full object-cover"
        />
      </div>

      <div class="p-3 flex justify-between items-center">
        <div class="flex-1 min-w-0 pr-3">
          <div class="text-[#F02849] text-[13px] font-semibold uppercase mb-0.5 tracking-wide leading-none">
            {{ sharedEvent.date || 'SOB, 16 MAJ O 15:00' }}
          </div>
          <h3 class="text-[#050505] dark:text-[#E4E6EB] font-bold text-[17px] leading-snug truncate mb-0.5">
            {{ sharedEvent.title || sharedEvent.name }}
          </h3>
          <div class="text-[#65676B] dark:text-[#B0B3B8] text-[13px] truncate">
             {{ sharedEvent.locationName || sharedEvent.location }}
          </div>
          </div>

       <button
          @click.stop="toggleInterest"
          class="shrink-0 h-9 px-3 rounded-[6px] text-[15px] font-semibold flex items-center justify-center gap-1.5 transition-colors z-10 border border-transparent"
          :class="[
            isInterested
              ? 'bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBEBFF]'
              : 'bg-[#E4E6EB] dark:bg-[#3A3B3C] text-[#050505] dark:text-[#E4E6EB] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50]'
          ]"
        >
          <component
            :is="isInterested ? Star : StarOutline"
            :size="20"
          />

          <span class="hidden sm:inline">
            {{ isInterested ? 'Interesuję się' : 'Zainteresowany(a)' }}
          </span>

          <ChevronDown
            v-if="isInterested"
            :size="20"
          />
        </button>
      </div>
    </div>

    <div v-if="!isShared" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
      <div class="flex items-center justify-between text-theme-text-secondary text-[15px]">

        <div class="flex items-center gap-1.5 cursor-pointer group" v-if="likesCount > 0">

          <div class="flex items-center relative pl-1">

            <div
              v-if="likesCount > 1 || (likesCount === 1 && !userReaction)"
              class="relative z-10 rounded-full w-[20px] h-[20px] flex items-center justify-center bg-[#1877F2]"
            >
               <ThumbUp :size="12" fillColor="#FFFFFF" />
            </div>

            <div
              v-if="userReaction"
              class="relative z-0 rounded-full w-5 h-5 flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
              :class="[
                getReactionConfig(userReaction).wrapperClass,
                likesCount > 1 ? '-ml-1' : '' // Przesunięcie w lewo
              ]"
            >
              <component
                v-if="getReactionConfig(userReaction).mode === 'icon'"
                :is="getReactionConfig(userReaction).component"
                :size="10"
                :fillColor="getReactionConfig(userReaction).color"
              />

              <span
                v-else
                class="text-[20px] leading-none  select-none"
              >
                {{ getReactionConfig(userReaction).char }}
              </span>
            </div>
          </div>

          <span class="text-theme-text-secondary group-hover:underline leading-snug ml-0.5">
            <template v-if="userReaction">
              <span v-if="likesCount === 1">{{ t('post.likedByYou') }}</span>
              <span v-else-if="likesCount === 2">{{ t('post.likedByYouAnd', { name: 'Anna Kowalska' }) }}</span>
              <span v-else>{{ t('post.likedByYouAndOthers', { name: 'Anna Kowalska', count: likesCount - 2 }) }}</span>
            </template>

            <template v-else>
              <span>{{ t('post.likedBy', { name: 'Anna Kowalska' }) }}</span>
              <span v-if="likesCount > 1"> {{ t('post.andOthers', { count: likesCount - 1 }) }}</span>
            </template>
          </span>
        </div>

        <div v-else></div>

        <div class="flex items-center gap-3 text-theme-text-secondary">
          <span class="hover:underline cursor-pointer">{{ t('post.commentsCount', { count: post.commentsCount }) }}</span>
          <span class="hover:underline cursor-pointer">{{ t('post.sharesCount', { count: post.sharesCount }) }}</span>
        </div>
      </div>
    </div>

    <PostActions v-if="!isShared"
      @react="handleReaction"
      @comment="toggleModal"
      @share-as-post="shareAsMyPost"
      @share-to-story="shareToStory"
      @share-to-message="shareToMessage"
    />

    <BaseModal v-if="isModalOpen" @close="toggleModal">
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>
    <BaseModal :title="t('post.sendTo')" v-if="isShareAsMessageModalOpen" @close="isShareAsMessageModalOpen = false">
      <ShareAsMessageModal  />
    </BaseModal>

    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postToShare" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"/>
  </div>
</template>
<style scoped>
  .animate-marquee {
  display: inline-block;
  white-space: nowrap;
  animation: scroll-left 12s linear infinite;
  padding-left: 100%; /* Start z prawej strony */
}

@keyframes scroll-left {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}
</style>
