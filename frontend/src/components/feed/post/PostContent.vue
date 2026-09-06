<template>
  <!-- Original content display -->
  <div
    v-if="post.content && (!isTranslated || isOriginalVisible) && post.targetType !== 'GroupCreated'"
    class="px-4 py-1 text-[15px] leading-normal whitespace-pre-line"
    :class="{
      [(currentBackground as CardBackground).class ?? '']: (post?.selectedCardBgId ?? 0) !== 0,
      [(currentBackground as CardBackground).textClass ?? 'text-theme-text']:
        (post?.selectedCardBgId ?? 0) !== 0,
      'p-4 h-95.75 flex items-center justify-center text-center':
        (post?.selectedCardBgId ?? 0) !== 0,
      'text-xl': (post?.selectedCardBgId ?? 0) !== 0 && post?.content.length <= 80,
      'text-base': (post?.selectedCardBgId ?? 0) !== 0 && post?.content.length > 80,
      'text-theme-text': (post?.selectedCardBgId ?? 0) === 0,
      'pb-3': !isTranslated && !needsTranslation,
    }"
  >
    <template v-for="(part, index) in contentToShow" :key="index">
      <NuxtLink
        v-if="part.type === 'hashtag'"
        :to="`/hashtag/${part.hashtag}`"
        class="text-blue-500 hover:underline"
        :class="{ 'text-white ': (post.selectedCardBgId ?? 0) > 0 }"
      >
        {{ part.value }}
      </NuxtLink>
      <ProfilePopper
        mention
        v-else-if="part.type === 'mention'"
        :user-id="part.userId"
        class="text-blue-500 hover:underline inline-flex"
        :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
      />
      <a
        v-else-if="part.type === 'link'"
        :href="part.url"
        target="_blank"
        class="text-blue-500 hover:underline"
        :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
        @click="handleLinkClick(part.url)"
      >
        {{ part.value }}
      </a>
      <span v-else :class="{ ' text-[30px]': (post.selectedCardBgId ?? 0) > 0 }">{{
        part.value
      }}</span>
    </template>
    <button
      v-if="showReadMore"
      @click="handleExpandText"
      class="text-theme-text hover:underline font-semibold"
    >{{ $t('feed.czytajWiecej') }}</button>
  </div>

  <!-- Translation section -->
  <div
    v-if="isTranslated || needsTranslation"
    :class="
      isTranslated
        ? 'ml-4 pl-3 border-l-[3px] border-[#dddfe2] dark:border-gray-600 pr-4 mt-1'
        : 'px-4'
    "
  >
    <!-- Translated content display -->
    <div
      v-if="isTranslated"
      class="py-1 pb-1 text-[15px] leading-normal whitespace-pre-line text-theme-text"
    >
      <template v-for="(part, index) in processedTranslatedContent" :key="index">
        <NuxtLink
          v-if="part.type === 'hashtag'"
          :to="`/hashtag/${part.hashtag}`"
          class="text-blue-500 hover:underline"
        >
          {{ part.value }}
        </NuxtLink>
        <NuxtLink
          v-else-if="part.type === 'mention'"
          :to="`/profile/${part.userId}`"
          class="text-blue-500 hover:underline"
        >
          @{{ getUserById(part.userId)?.name }}
        </NuxtLink>
        <a
          v-else-if="part.type === 'link'"
          :href="part.url"
          target="_blank"
          class="text-blue-500 hover:underline"
        >
          {{ part.value }}
        </a>
        <span v-else>{{ part.value }}</span>
      </template>
    </div>

    <!-- Translation controls -->
    <div v-if="needsTranslation" class="pb-3 pt-0" :class="!isTranslated ? 'px-4' : ''">
      <!-- Translation options when translated -->
      <div v-if="isTranslated" class="flex items-center text-[13px] font-semibold leading-4">
        <TranslationRatingDropdown
          :detected-language="post.detectedLanguage"
          :rating="rating"
          :hover-rating="hoverRating"
          @update:rating="rating = $event"
          @update:hover-rating="hoverRating = $event"
        />

        <button
          @click="showOriginal"
          class="text-[#1877F2] hover:underline cursor-pointer bg-transparent border-none p-0"
        >
          {{ isOriginalVisible ? 'Ukryj oryginalny tekst' : 'Zobacz oryginalny tekst' }}
        </button>

        <span class="text-[#65676B] dark:text-[#B0B3B8] px-1">·</span>

        <VDropdown :distance="10" placement="bottom-start">
          <button
            class="text-[#1877F2] hover:underline cursor-pointer bg-transparent border-none p-0"
          >{{ $t('feed.ocenToTlumaczenie') }}</button>
          <template #popper>
            <div class="p-4 text-center">{{ $t('feed.funkcjaOceniania') }}</div>
          </template>
        </VDropdown>
      </div>

      <!-- Translation trigger button -->
      <div v-else>
        <button
          @click="translatePost"
          class="text-[13px] font-semibold text-[#1877F2] hover:underline bg-transparent border-none p-0"
          :disabled="isTranslating"
        >
          {{ isTranslating ? 'Tłumaczenie...' : 'Zobacz tłumaczenie' }}
        </button>
        <span v-if="translationError" class="text-xs text-red-500 ml-2"> (Błąd) </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Dropdown as VDropdown } from 'floating-vue'
import { feedApi } from '@/api/feed'

import TranslationRatingDropdown from './TranslationRatingDropdown.vue'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'

import { getUserById } from '@/utils/users'
import { processContent } from '@/utils/contentProcessor'
import type { Post } from '@/types/Post'
import { useImpressionTracker } from '@/composables/analytics/useImpressionTracker'

const { trackExpandText, trackLinkClick } = useImpressionTracker()

const handleExpandText = () => {
  isExpanded.value = true
  if (props.post?.id) {
    const authorId = props.post.author?.id || (props.post as any).authorId
    trackExpandText(String(props.post.id), authorId)
  }
}

const handleLinkClick = (url: string) => {
  if (props.post?.id) {
    const authorId = props.post.author?.id || (props.post as any).authorId
    trackLinkClick(String(props.post.id), authorId, url)
  }
}

interface CardBackground {
  id: number
  class: string
  textClass?: string
}

const props = withDefaults(
  defineProps<{
    post?: Post
    isShared?: boolean
  }>(),
  {
    post: () => ({} as any),
    isShared: false,
  }
)

const isExpanded = ref(false)
const isTranslated = ref(false)
const translatedContent = ref('')
const isTranslating = ref(false)
const translationError = ref(false)
const isOriginalVisible = ref(true)

// Rating state
const rating = ref(0)
const hoverRating = ref(0)

// Card backgrounds definition
const cardBackgrounds: CardBackground[] = [
  {
    id: 1,
    class: 'bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-700',
    textClass: 'text-white',
  },
  {
    id: 2,
    class: 'bg-gradient-to-r from-emerald-500 via-teal-600 to-cyan-700',
    textClass: 'text-white',
  },
  {
    id: 3,
    class: 'bg-gradient-to-r from-rose-500 via-pink-600 to-purple-600',
    textClass: 'text-white',
  },
  {
    id: 4,
    class: 'bg-gradient-to-r from-amber-400 via-orange-500 to-red-500',
    textClass: 'text-white',
  },
  {
    id: 5,
    class: 'bg-gradient-to-r from-fuchsia-600 via-pink-500 to-rose-500',
    textClass: 'text-white',
  },
  {
    id: 6,
    class: 'bg-gradient-to-r from-blue-700 via-blue-800 to-indigo-900',
    textClass: 'text-white',
  },
  {
    id: 7,
    class: 'bg-gradient-to-r from-violet-600 via-purple-700 to-indigo-800',
    textClass: 'text-white',
  },
  {
    id: 8,
    class: 'bg-gradient-to-r from-gray-700 via-gray-900 to-black',
    textClass: 'text-white',
  },
]

// Computed properties
const currentBackground = computed(() => {
  return cardBackgrounds.find((bg) => bg.id === props.post?.selectedCardBgId) ?? cardBackgrounds[0]
})

const needsTranslation = computed(() => {
  return props.post?.detectedLanguage && props.post.detectedLanguage !== 'pl'
})

const processedOriginalContent = computed(() => {
  return processContent(props.post?.content || '')
})

const processedTranslatedContent = computed(() => {
  return processContent(translatedContent.value)
})

const showReadMore = computed(() => {
  return (props.post?.content?.length || 0) > 137 && !isExpanded.value
})

const contentToShow = computed(() => {
  if (showReadMore.value) {
    const truncated = (props.post?.content || '').substring(0, 200)
    const processed = processContent(truncated)
    // Add ellipsis at the end
    if (processed.length > 0) {
      const lastPart = processed[processed.length - 1]
      if (lastPart.type === 'text') {
        lastPart.value += '...'
      } else {
        processed.push({ type: 'text', value: '...' })
      }
    }
    return processed
  }
  return processedOriginalContent.value
})

const translatePost = async () => {
  if (isTranslating.value || isTranslated.value) return

  try {
    isTranslating.value = true
    translationError.value = false

    const translated = await feedApi.translateText(props.post?.content || '', 'pl')
    if (translated) {
      translatedContent.value = translated
      isTranslated.value = true
      isOriginalVisible.value = false
    } else {
      translationError.value = true
    }
  } catch (error) {
    console.error('Błąd tłumaczenia:', error)
    translationError.value = true
  } finally {
    isTranslating.value = false
  }
}

const showOriginal = () => {
  isOriginalVisible.value = !isOriginalVisible.value
}
</script>
