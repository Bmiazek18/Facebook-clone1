<script setup lang="ts">
import { computed } from 'vue'
import type { TextMessage } from '@/types/Message'

// Importy Emoji Mart
import { Emoji, EmojiIndex } from 'emoji-mart-vue-fast/src'
import data from 'emoji-mart-vue-fast/data/all.json'

const props = defineProps<{
  message: TextMessage
  bubbleRadiusClass?: string
  bubbleColor?: string
  textColor?: string
}>()

const emojiIndex = new EmojiIndex(data)

// Rozszerzony regex dla emoji
const EMOJI_REGEX =
  /(\ud83c[\udf00-\udfff]|\ud83d[\udc00-\ude4f]|\ud83d[\ude80-\udeff]|\ud83e[\udd00-\uddff]|[\u2600-\u27bf])/g

const style = computed(() => ({
  backgroundColor: props.bubbleColor,
  color: props.textColor,
}))

const tokens = computed(() => {
  const content = props.message.content || ''
  return content
    .split(EMOJI_REGEX)
    .map((part) => ({
      value: part,
      isEmoji: EMOJI_REGEX.test(part),
    }))
    .filter((token) => token.value !== '')
})

const isEmojiOnlyMessage = computed(() => {
  if (!props.message.content?.trim()) return false
  return tokens.value.every((token) => token.isEmoji || token.value.trim() === '')
})

const jumboSize = computed(() => {
  const state = props.message.iconSizeState
  if (state === 'small') return 45
  if (state === 'medium') return 60
  if (state === 'large') return 80
  return 48
})
</script>

<template>
  <div class="message-wrapper">
    <div v-if="isEmojiOnlyMessage" class="flex flex-wrap items-center leading-none">
      <template v-for="(token, index) in tokens" :key="'jumbo-' + index">
        <span
          v-if="token.isEmoji"
          class="emoji-container inline-flex relative align-middle justify-center items-center"
          :style="{ width: jumboSize + 'px', height: jumboSize + 'px' }"
        >
          <span class="native-emoji-text" :style="{ fontSize: jumboSize + 'px' }">{{
            token.value
          }}</span>

          <span class="graphic-emoji absolute pointer-events-none">
            <Emoji
              :data="emojiIndex"
              :emoji="token.value"
              :size="jumboSize"
              :native="false"
              set="facebook"
            />
          </span>
        </span>
      </template>
    </div>

    <div
      v-else
      class="relative px-4 py-2 text-[15px] shadow-sm break-words max-w-full"
      :class="[bubbleRadiusClass]"
      :style="style"
    >
      <div class="inline-block align-middle">
        <template v-for="(token, index) in tokens" :key="'text-' + index">
          <span
            v-if="token.isEmoji"
            class="emoji-container inline-flex relative align-middle justify-center items-center mx-[1px]"
            style="width: 20px; height: 20px"
          >
            <span class="native-emoji-text" style="font-size: 18px">{{ token.value }}</span>

            <span class="graphic-emoji absolute pointer-events-none">
              <Emoji
                :data="emojiIndex"
                :emoji="token.value"
                :size="18"
                :native="false"
                set="facebook"
              />
            </span>
          </span>

          <span v-else class="align-middle">{{ token.value }}</span>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Kontener pozwalający na naturalne zaznaczanie */
.message-wrapper {
  user-select: text;
  -webkit-user-select: text;
}

/* Niewidzialny tekst - to on trafia do schowka, gdy przeciągasz myszką! */
.native-emoji-text {
  color: transparent; /* Niewidoczny */
  z-index: 2; /* Na wierzchu, żeby złapać zaznaczenie myszką */
  user-select: text;
  line-height: 1;
}

/* Gdy użytkownik podświetli tekst, chcemy żeby podświetlenie było normalne, a tekst nadal przezroczysty */
.native-emoji-text::selection {
  color: transparent;
  background-color: rgba(
    59,
    130,
    246,
    0.4
  ); /* Typowy niebieski kolor zaznaczenia (Tailwind blue-500 z przezroczystością) */
}

/* Grafika emoji znajduje się POD niewidzialnym tekstem */
.graphic-emoji {
  z-index: 1;
  user-select: none; /* Wyłączamy z zaznaczania */
  -webkit-user-select: none;
  pointer-events: none; /* Wyłączamy kliknięcia w obrazek */
}

/* Fix dla emoji-mart by idealnie centrował */
:deep(.emoji-mart-emoji) {
  display: flex !important;
  align-items: center;
  justify-content: center;
}

.break-words {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
