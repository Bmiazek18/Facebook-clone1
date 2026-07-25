<template>
  <!-- Original content display -->
  <div
    v-if="post.content && (!isTranslated || isOriginalVisible)"
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
      <NuxtLink
        v-else-if="part.type === 'mention'"
        :to="`/profile/${part.userId}`"
        class="text-blue-500 hover:underline"
        :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
      >
        @{{ getUserById(part.userId)?.name }}
      </NuxtLink>
      <a
        v-else-if="part.type === 'link'"
        :href="part.url"
        target="_blank"
        class="text-blue-500 hover:underline"
        :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
      >
        {{ part.value }}
      </a>
      <span v-else :class="{ ' text-[30px]': (post.selectedCardBgId ?? 0) > 0 }">{{
        part.value
      }}</span>
    </template>
    <button
      v-if="showReadMore"
      @click="isExpanded = true"
      class="text-theme-text hover:underline font-semibold"
    >
      Czytaj więcej
    </button>
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
          >
            Oceń to tłumaczenie
          </button>
          <template #popper>
            <div class="p-4 text-center">Funkcja oceniania...</div>
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
import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

import TranslationRatingDropdown from './TranslationRatingDropdown.vue'

import { getUserById } from '@/utils/users'
import { processContent } from '@/utils/contentProcessor'
import type { Post } from '@/types/Post'

const TRANSLATE_TEXT_MUTATION = gql`
  mutation TranslateText($text: String!, $targetLanguage: String!) {
    translateText(text: $text, targetLanguage: $targetLanguage)
  }
`

interface CardBackground {
  id: number
  class: string
  textClass?: string
}

const props = defineProps<{
  post: Post
}>()

const isExpanded = ref(false)

// Translation state
const isTranslated = ref(false)
const translatedContent = ref<string>('')
const isTranslating = ref(false)
const translationError = ref(false)
const isOriginalVisible = ref(false)

// Rating state
const rating = ref(0)
const hoverRating = ref(0)

// Card backgrounds configuration - merged from both files
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  {
    id: 2,
    class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500',
    textClass: 'text-white',
  },
  {
    id: 3,
    class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900',
    textClass: 'text-white',
  },
  { id: 4, class: 'bg-gradient-to-r from-green-400 to-blue-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-br from-orange-400 to-pink-600', textClass: 'text-white' },
  {
    id: 6,
    class: 'bg-gradient-to-r from-purple-400 via-pink-500 to-red-500',
    textClass: 'text-white',
  },
  { id: 7, class: 'bg-gradient-to-br from-teal-400 to-blue-500', textClass: 'text-white' },
  {
    id: 8,
    class: 'bg-gradient-to-r from-yellow-400 via-red-500 to-pink-500',
    textClass: 'text-white',
  },
  {
    id: 9,
    class: 'bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500',
    textClass: 'text-white',
  },
  {
    id: 10,
    class: 'bg-gradient-to-r from-gray-700 via-gray-900 to-black',
    textClass: 'text-white',
  },
]

// Computed properties
const currentBackground = computed(() => {
  return cardBackgrounds.find((bg) => bg.id === props.post?.selectedCardBgId) ?? cardBackgrounds[0]
})

const needsTranslation = computed(() => {
  return props.post.detectedLanguage && props.post.detectedLanguage !== 'pl'
})

const processedOriginalContent = computed(() => {
  return processContent(props.post.content)
})

const processedTranslatedContent = computed(() => {
  return processContent(translatedContent.value)
})

const showReadMore = computed(() => {
  return props.post.content.length > 137 && !isExpanded.value
})

const contentToShow = computed(() => {
  if (showReadMore.value) {
    // Truncate the content
    const truncated = props.post.content.substring(0, 200)
    // We need to process the truncated content to get the parts
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

const { mutate: translateTextMutation } = useMutation(TRANSLATE_TEXT_MUTATION)

const translatePost = async () => {
  if (isTranslating.value || isTranslated.value) return

  try {
    isTranslating.value = true
    translationError.value = false

    const { data } = await translateTextMutation({
      text: props.post.content,
      targetLanguage: 'pl',
    })
    console.log('Otrzymane dane tłumaczenia:', data)
    if (data?.translateText) {
      translatedContent.value = data.translateText
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
