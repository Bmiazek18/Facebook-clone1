<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCreatePostStore } from '@/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import { useContentEditable } from '@/composables/ui/useContentEditable'
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import StoryTextCard from './StoryTextCard.vue'

defineProps<{
  hasSharedPost: boolean
}>()

const emit = defineEmits<{
  (e: 'detectUrl', url: string): void
}>()

const { t } = useI18n()
const createPostStore = useCreatePostStore()
const authStore = useAuthStore()

const currentUser = computed(() => authStore.currentUser)
const isAnonymous = computed(() => !!createPostStore.postData.isAnonymous)

const displayName = computed(() => {
  return isAnonymous.value
    ? t('post.anonymousUser') || 'Anonim'
    : currentUser.value?.name || `${(currentUser.value as any)?.firstName || ''} ${(currentUser.value as any)?.lastName || ''}`.trim()
})

const hasLocation = computed(() => !!createPostStore.postData.location)

const postContent = computed({
  get: () => createPostStore.postData.content,
  set: (v) => { createPostStore.postData.content = v }
})

const selectedImages = computed(() => createPostStore.postData.images)
const selectedCardBgId = computed(() => createPostStore.postData.cardBgId)
const selectedGif = computed(() => createPostStore.postData.gif)
const initialView = computed(() => createPostStore.uiState.initialView)

// --- LOGIKA CONTENT EDITABLE ---
const contentEditableDiv = ref<HTMLDivElement | null>(null)
const {
  onContentInput: baseOnContentInput,
  matchingUsers,
  showUserDropdown,
  selectUser: selectUserFromComposable,
  addEmoji: addEmojiFromComposable,
  renderContentEditable,
} = useContentEditable(contentEditableDiv, postContent)

// Wywołujemy render przy montowaniu, na wypadek gdyby treść była już w sklepie
onMounted(() => {
  renderContentEditable()
})

let linkCheckTimeout: ReturnType<typeof setTimeout> | null = null

const onContentInput = () => {
  baseOnContentInput()

  if (linkCheckTimeout) clearTimeout(linkCheckTimeout)

  linkCheckTimeout = setTimeout(() => {
    const text = contentEditableDiv.value?.innerText || ''
    const urlMatch = text.match(/(https?:\/\/[^\s]+)/g)

    if (urlMatch && urlMatch.length > 0) {
      emit('detectUrl', urlMatch[0])
    }
  }, 500)
}

const showTextCard = ref(selectedCardBgId.value !== 0)
const toggleTextCard = () => {
  if (!showTextCard.value && selectedCardBgId.value === 0) createPostStore.postData.cardBgId = 1
  showTextCard.value = !showTextCard.value
}
const selectCardBackground = (id: number) => {
  createPostStore.postData.cardBgId = id
  if (id === 0) showTextCard.value = false
}

// --- BACKGROUNDY KART ---
const cardBackgrounds = [
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
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
]
const currentBackground = computed(
  () => (cardBackgrounds.find((bg) => bg.id === selectedCardBgId.value) ?? cardBackgrounds[0]) as typeof cardBackgrounds[0],
)
</script>

<template>
  <div class="relative w-full mb-2">
    <div v-if="!showTextCard" class="relative w-full z-10">
      <div
        class="relative w-full transition-all duration-300"
        :class="
          selectedCardBgId !== 0
            ? [
                currentBackground.class,
                currentBackground.textClass,
                'rounded-lg p-4 min-h-[12rem] flex items-center justify-center text-center',
              ]
            : ''
        "
      >
        <div
          ref="contentEditableDiv"
          contenteditable="true"
          @input="onContentInput"
          class="w-full border-none resize-none caret-current focus:ring-0 focus:outline-none p-0 pt-2 cursor-text whitespace-pre-wrap bg-transparent text-theme-text"
          :class="{
            'h-[150px] text-[24px] leading-[28px]':
              selectedImages.length === 0 && !selectedGif && initialView !== 'poll',
            'text-base': postContent.length > 80,
            'min-h-[31px] pb-2': selectedCardBgId === 0,
            'text-[15px] leading-[19px]':
              selectedImages.length != 0 || selectedGif || initialView == 'poll',
          }"
        ></div>

        <div
          v-if="!postContent.trim() && selectedCardBgId === 0"
          class="absolute top-2 left-0 text-[#65686c] pointer-events-none"
          :class="{
            'text-[24px] leading-[28px]':
              selectedImages.length === 0 && !selectedGif && initialView !== 'poll',
            'text-[15px] leading-[19px]':
              selectedImages.length != 0 || selectedGif || initialView == 'poll',
          }"
        >
          {{
            hasSharedPost
              ? t('post.saySomething')
              : hasLocation
                ? t('post.whatAreYouThinking', { name: displayName })
                : t('post.whatsUp')
          }}
        </div>
      </div>

      <div
        class="absolute bottom-0 left-0 text-[#fe5b70] cursor-pointer"
        :title="t('post.textStyling')"
      >
        <div class="flex items-center justify-center p-1" @click="toggleTextCard">
          <div
            v-if="initialView !== 'poll' && selectedImages.length === 0 && !selectedGif"
            class="w-8 h-8 rounded-xl bg-gradient-to-tr..."
          >
            <span class="text-white font-bold text-md tracking-tighter select-none">Aa</span>
          </div>
        </div>
      </div>

      <div
        class="absolute bottom-2 right-0 text-theme-text-secondary cursor-pointer"
        :title="t('post.addEmoji')"
      >
        <VDropdown placement="top-end" :distance="10" :autoHide="true">
          <emoticon-happy-icon
            :size="24"
            class="cursor-pointer hover:text-theme-text-secondary-hover transition"
          />
          <template #popper>
            <div class="emoji-popper-content">
              <LazyEmojiPicker @select="addEmojiFromComposable" />
            </div>
          </template>
        </VDropdown>
      </div>
    </div>

    <VDropdown
      :shown="showUserDropdown"
      placement="bottom-start"
      class="absolute bottom-0 left-0 w-full h-0 pointer-events-none"
    >
      <div class="w-full h-0"></div>
      <template #popper>
        <div class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto">
          <ul>
            <li
              v-for="user in matchingUsers"
              :key="user.id"
              class="px-4 py-2 cursor-pointer hover:bg-theme-bg-hover flex items-center gap-2 text-theme-text"
              @mousedown.prevent="selectUserFromComposable(user)"
            >
              <div class="w-8 h-8 bg-theme-bg-tertiary rounded-full flex-shrink-0">
                <img
                  v-if="user.avatar"
                  :src="user.avatar"
                  class="w-full h-full object-cover rounded-full"
                />
              </div>
              <span class="font-medium text-sm">{{ user.name }}</span>
            </li>
          </ul>
        </div>
      </template>
    </VDropdown>

    <StoryTextCard
      v-if="showTextCard"
      v-model="postContent"
      :bgId="selectedCardBgId"
      :backgrounds="cardBackgrounds"
      @update:bgId="selectCardBackground"
      @close="showTextCard = false"
    />
  </div>
</template>

<style scoped>
.emoji-popper-content {
  overflow: hidden;
}
:deep(.v-popper) {
  width: 100%;
}
</style>
