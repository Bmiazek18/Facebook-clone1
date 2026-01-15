<template>
  <!-- Original content display -->
  <div v-if="post.content && (!isTranslated || isOriginalVisible)"
       class="px-4 py-1 text-[15px] leading-normal whitespace-pre-line"
       :class="{
         [((currentBackground as CardBackground).class ?? '')]: (post?.selectedCardBgId ?? 0) !== 0,
         [((currentBackground as CardBackground).textClass ?? 'text-theme-text')]: (post?.selectedCardBgId ?? 0) !== 0,
         'p-4 h-[383px] flex items-center justify-center text-center': (post?.selectedCardBgId ?? 0) !== 0,
         'text-xl': (post?.selectedCardBgId ?? 0) !== 0 && post?.content.length <= 80,
         'text-base': (post?.selectedCardBgId ?? 0) !== 0 && post?.content.length > 80,
         'text-theme-text': (post?.selectedCardBgId ?? 0) === 0,
         'pb-3': !isTranslated
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

  <!-- Translation section -->
  <div v-if="isTranslated || needsTranslation"
       :class="isTranslated ? 'ml-4 pl-3 border-l-[3px] border-[#dddfe2] dark:border-gray-600 pr-4 mt-1' : 'px-4'">

    <!-- Translated content display -->
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

    <!-- Translation controls -->
    <div v-if="needsTranslation" class="pb-3 pt-0" :class="!isTranslated ? 'px-4' : ''">

      <!-- Translation options when translated -->
      <div v-if="isTranslated" class="flex items-center text-[13px] font-semibold leading-4">

        <VDropdown :distance="10" placement="bottom-start" theme="dropdown">
          <button class="mr-1.5 flex items-center justify-center text-[#1877F2] hover:bg-blue-50 rounded-full p-1 -ml-1 transition-colors">
            <Cog :size="16" />
          </button>

          <template #popper>
            <div class="w-[320px] py-2 text-[#050505] dark:text-[#E4E6EB] text-[15px]">

              <!-- Rating section -->
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

              <!-- Options menu -->
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

      <!-- Translation trigger button -->
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
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import axios from 'axios'
import { Dropdown as VDropdown } from 'floating-vue'

import Cog from 'vue-material-design-icons/Cog.vue'
import Star from 'vue-material-design-icons/Star.vue'
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import Close from 'vue-material-design-icons/Close.vue'
import MinusCircle from 'vue-material-design-icons/MinusCircle.vue'

import { getUserById } from '@/data/users'
import { processContent } from '@/utils/contentProcessor'
import type { Post } from '@/types/Post'

interface CardBackground { 
  id: number; 
  class: string; 
  textClass?: string 
}

const props = defineProps<{
  post: Post
}>()

// Translation state
const isTranslated = ref(false)
const translatedContent = ref<string>('')
const isTranslating = ref(false)
const translationError = ref(false)
const isOriginalVisible = ref(false)

// Rating state
const rating = ref(0)
const hoverRating = ref(0)

// Card backgrounds configuration
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
]

// Computed properties
const currentBackground = computed(() => {
  return cardBackgrounds.find(bg => bg.id === props.post?.selectedCardBgId) ?? cardBackgrounds[0]
})

const needsTranslation = computed(() => {
  return props.post.detectedLanguage && props.post.detectedLanguage !== 'pl'
})

const processedOriginalContent = computed(() => {
  return processContent(props.post.content);
})

const processedTranslatedContent = computed(() => {
  return processContent(translatedContent.value);
})

// Rating functions
const isStarFilled = (index: number) => {
  const activeRating = hoverRating.value > 0 ? hoverRating.value : rating.value
  return index <= activeRating
}

const setRating = (value: number) => {
  rating.value = value
}

// Translation functions
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
    isOriginalVisible.value = false
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