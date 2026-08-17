<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useUserCache } from '@/composables/shared/useUserCache'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import AccountCircleOutline from 'vue-material-design-icons/AccountCircleOutline.vue'
import Close from 'vue-material-design-icons/Close.vue'

interface Props {
  show: boolean
  userId: string | number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const chatStore = useChatStore()
const { getOrFetchUser } = useUserCache()

const userData = ref<{ id: string; name: string; avatar: string } | null>(null)
const loading = ref(false)

watch(
  () => [props.show, props.userId],
  async ([isShown, uId]) => {
    if (isShown && uId) {
      loading.value = true
      try {
        const u = await getOrFetchUser(String(uId))
        if (u) {
          userData.value = u
        }
      } catch (e) {
        console.error('Failed to fetch mentioned user:', e)
      } finally {
        loading.value = false
      }
    }
  },
  { immediate: true }
)

function handleSendMessage() {
  if (props.userId) {
    chatStore.addMessageBox(String(props.userId).replace(/^user_/, ''))
  }
  emit('close')
}

function handleViewProfile() {
  if (props.userId) {
    router.push(`/profile/${String(props.userId).replace(/^user_/, '')}`)
  }
  emit('close')
}
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm antialiased animate-in fade-in duration-150"
    @click.self="emit('close')"
  >
    <div
      class="bg-white dark:bg-[#242526] rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden border border-gray-200 dark:border-gray-700 p-5 flex flex-col items-center text-center space-y-4 animate-in zoom-in-95 duration-150"
    >
      <!-- Close button -->
      <div class="w-full flex justify-end">
        <button
          @click="emit('close')"
          class="p-1.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full transition-colors"
        >
          <Close :size="18" />
        </button>
      </div>

      <!-- User avatar and info -->
      <div class="flex flex-col items-center space-y-2">
        <div class="relative w-20 h-20 rounded-full overflow-hidden border-2 border-blue-500 shadow-md bg-gray-100 dark:bg-gray-800">
          <img
            :src="userData?.avatar || '/default-avatar.png'"
            class="w-full h-full object-cover"
            alt="Avatar"
          />
        </div>

        <div>
          <h3 class="text-lg font-bold text-gray-900 dark:text-white">
            {{ userData?.name || 'Użytkownik' }}
          </h3>
          <p class="text-xs text-gray-500 dark:text-gray-400">
            Wybierz akcję dla oznaczonego użytkownika
          </p>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="w-full space-y-2 pt-2">
        <button
          @click="handleSendMessage"
          type="button"
          class="w-full py-2.5 px-4 bg-[#1877F2] hover:bg-[#166FE5] text-white font-semibold rounded-xl text-sm transition-all flex items-center justify-center gap-2 shadow-sm active:scale-[0.98]"
        >
          <MessageOutline :size="18" />
          <span>Wyślij wiadomość</span>
        </button>

        <button
          @click="handleViewProfile"
          type="button"
          class="w-full py-2.5 px-4 bg-gray-100 dark:bg-gray-700/80 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 font-semibold rounded-xl text-sm transition-all flex items-center justify-center gap-2 active:scale-[0.98]"
        >
          <AccountCircleOutline :size="18" />
          <span>Wejdź na profil</span>
        </button>
      </div>
    </div>
  </div>
</template>
