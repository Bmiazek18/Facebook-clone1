<script setup lang="ts">
import { ref, computed } from 'vue'
import type { TextMessage } from '@/types/Message'
import { processContent, type ProcessedContent } from '@/utils/contentProcessor'
import { useUserCache } from '@/composables/shared/useUserCache'
import UserMentionActionModal from '@/components/chat/messageItem/modals/UserMentionActionModal.vue'

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
const { getUserById, getOrFetchUser } = useUserCache()

const selectedMentionUserId = ref<string | null>(null)
const showMentionModal = ref(false)

// Kompleksowy regex dla emoji
const EMOJI_REGEX = /([\u2700-\u27BF]|[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF])/g

const style = computed(() => ({
  backgroundColor: props.bubbleColor,
  color: props.textColor,
}))

const isEmojiOnlyMessage = computed(() => {
  const content = props.message.content || ''
  if (!content.trim() || content.includes('[@')) return false
  const rawTokens = content
    .split(EMOJI_REGEX)
    .filter((part) => part !== '')
  const isAllEmoji = rawTokens.every((part) => EMOJI_REGEX.test(part) || part.trim() === '')
  if (!isAllEmoji) return false

  const cleanContent = content.replace(/\s+/g, '')
  const segmenter = new Intl.Segmenter(undefined, { granularity: 'grapheme' })
  const emojiCount = Array.from(segmenter.segment(cleanContent)).length
  return emojiCount <= 4
})

const jumboSize = computed(() => {
  const state = props.message.iconSizeState
  if (state === 'large') return 65
  if (state === 'medium') return 50
  if (state === 'small') return 40
  return 30
})

const processedParts = computed<ProcessedContent[]>(() => {
  return processContent(props.message.content || '')
})

function splitByEmoji(text: string) {
  return text
    .split(EMOJI_REGEX)
    .map((part) => ({
      value: part,
      isEmoji: EMOJI_REGEX.test(part),
    }))
    .filter((token) => token.value !== '')
}

function getMentionDisplayName(userId: string): string {
  if (userId === 'all' || userId === 'wszyscy') return '@wszyscy'
  const user = getUserById(userId)
  if (user?.name) return `@${user.name}`
  // trigger background fetch if not cached
  getOrFetchUser(userId)
  return `@${userId}`
}

function handleMentionClick(userId?: string) {
  if (!userId || userId === 'all' || userId === 'wszyscy') return
  selectedMentionUserId.value = userId
  showMentionModal.value = true
}
</script>

<template>
  <div class="message-wrapper">
    <!-- Wiadomość zawierająca same duże emotikony (Jumbo Emoji) -->
    <div v-if="isEmojiOnlyMessage" class="flex flex-wrap items-center leading-none">
      <template v-for="(part, pIndex) in processedParts" :key="'jumbo-p-' + pIndex">
        <template v-for="(token, index) in splitByEmoji(part.value)" :key="'jumbo-' + index">
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
      </template>
    </div>

    <!-- Standardowy dymek wiadomości z tekstem, linkami i oznaczeniami -->
    <div
      v-else
      class="relative px-4 py-2 text-[15px] shadow-sm break-words max-w-full"
      :class="[bubbleRadiusClass]"
      :style="style"
    >
      <div class="inline-block align-middle leading-relaxed">
        <template v-for="(part, pIndex) in processedParts" :key="'part-' + pIndex">
          <!-- Wzmianka / Oznaczenie użytkownika -->
          <span
            v-if="part.type === 'mention'"
            @click.stop="handleMentionClick(part.userId)"
            class="inline-flex items-center font-semibold bg-black/10 dark:bg-white/20 hover:bg-blue-500/20 active:scale-95 rounded px-1.5 py-0.5 mx-0.5 transition-all cursor-pointer select-none text-inherit"
            :title="$t('chat.kliknijAbyWybracAkcje')"
          >
            {{ getMentionDisplayName(part.userId || '') }}
          </span>

          <!-- Link URL -->
          <a
            v-else-if="part.type === 'link'"
            :href="part.url"
            target="_blank"
            rel="noopener noreferrer"
            class="underline hover:opacity-80 transition-opacity font-medium"
          >
            {{ part.value }}
          </a>

          <!-- Tekst i emoji -->
          <template v-else>
            <template v-for="(token, index) in splitByEmoji(part.value)" :key="'text-' + pIndex + '-' + index">
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

              <span v-else class="align-middle whitespace-pre-wrap">{{ token.value }}</span>
            </template>
          </template>
        </template>
      </div>
    </div>
  </div>

  <!-- Modal akcji dla oznaczonego użytkownika -->
  <UserMentionActionModal
    v-if="selectedMentionUserId"
    :show="showMentionModal"
    :user-id="selectedMentionUserId"
    @close="showMentionModal = false"
  />
</template>

<style scoped>
.message-wrapper {
  user-select: text;
  -webkit-user-select: text;
}

.native-emoji-text {
  color: transparent;
  z-index: 2;
  user-select: text;
  line-height: 1;
}

.native-emoji-text::selection {
  color: transparent;
  background-color: rgba(59, 130, 246, 0.4);
}

.graphic-emoji {
  z-index: 1;
  user-select: none;
  -webkit-user-select: none;
  pointer-events: none;
}

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
