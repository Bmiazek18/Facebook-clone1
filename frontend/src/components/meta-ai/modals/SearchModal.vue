<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { onClickOutside, onKeyStroke, useScrollLock } from '@vueuse/core'

interface ChatItem {
  id: string | number
  title: string
  date: string
}

interface Props {
  modelValue: boolean
  chats?: ChatItem[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  chats: () => [
    { id: 1, title: 'prosty kod hello world', date: '4 wrz' },
    { id: 2, title: 'Przegląd statutu szkoły', date: '2 wrz' },
    { id: 3, title: 'Szybkie pozdrowienie', date: '2 wrz' }
  ]
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', chat: ChatItem): void
  (e: 'new-chat'): void
}>()

const searchQuery = ref('')
const inputRef = ref<HTMLInputElement | null>(null)
const modalCardRef = ref<HTMLDivElement | null>(null)

// Blokowanie przewijania strony w tle przez VueUse
const isLocked = useScrollLock(document?.body)

function close() {
  emit('update:modelValue', false)
  searchQuery.value = ''
}

// VueUse: Zamknięcie po kliknięciu poza obszar okna modalnego
onClickOutside(modalCardRef, () => {
  if (props.modelValue) {
    close()
  }
})

// VueUse: Zamknięcie klawiszem Escape
onKeyStroke('Escape', () => {
  if (props.modelValue) {
    close()
  }
})

const filteredChats = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return props.chats
  return props.chats.filter(chat =>
    chat.title.toLowerCase().includes(query)
  )
})

function handleSelect(chat: ChatItem) {
  emit('select', chat)
  close()
}

function handleNewChat() {
  emit('new-chat')
  close()
}

watch(() => props.modelValue, async (isOpen) => {
  isLocked.value = isOpen
  if (isOpen) {
    await nextTick()
    inputRef.value?.focus()
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-white/70 backdrop-blur-md"
      >
        <div
          ref="modalCardRef"
          class="w-full max-w-lg overflow-hidden bg-white border border-gray-100 rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.1)] transition-all"
          role="dialog"
          aria-modal="true"
        >
          <div class="relative flex items-center px-6 pt-6 pb-4">
            <input
              ref="inputRef"
              v-model="searchQuery"
              type="text"
              :placeholder="$t('metaAi.wyszukajCzaty')"
              class="w-full text-base bg-transparent border-none outline-none pr-8 text-neutral-800 placeholder-neutral-400 focus:ring-0"
            />
            <button
              type="button"
              @click="close"
              class="absolute right-6 p-1.5 text-neutral-400 hover:text-neutral-600 rounded-full hover:bg-neutral-100 transition-colors"
              :aria-label="$t('common.close')"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div class="h-px bg-neutral-100 mx-6"></div>

          <div class="p-4 space-y-4 max-h-[60vh] overflow-y-auto">
            <button
              type="button"
              @click="handleNewChat"
              class="flex items-center w-full gap-3 px-4 py-3 text-sm font-medium text-neutral-700 bg-neutral-100/70 hover:bg-neutral-100 rounded-2xl transition-colors text-left"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-neutral-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <span>{{ $t('metaAi.nowyCzat') }}</span>
            </button>

            <div>
              <p class="px-4 py-1 text-xs font-normal text-neutral-400">{{ $t('emojiPicker.recent') }}</p>

              <div class="mt-1 space-y-0.5">
                <button
                  v-for="chat in filteredChats"
                  :key="chat.id"
                  type="button"
                  @click="handleSelect(chat)"
                  class="flex items-center justify-between w-full px-4 py-2.5 text-sm text-neutral-800 rounded-xl hover:bg-neutral-50 transition-colors text-left group"
                >
                  <span class="truncate pr-4 font-normal text-[13px] text-neutral-700 group-hover:text-neutral-900">
                    {{ chat.title }}
                  </span>
                  <span class="text-xs text-neutral-400 shrink-0 font-light">
                    {{ chat.date }}
                  </span>
                </button>

                <div
                  v-if="filteredChats.length === 0"
                  class="px-4 py-6 text-center text-sm text-neutral-400"
                >{{ $t('metaAi.nieZnalezionoCzatow') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
