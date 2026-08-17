<script setup lang="ts">
import { ref } from 'vue'
import Cog from 'vue-material-design-icons/Cog.vue'
import TextModeOptions from './TextModeOptions.vue'
import ImageModeOptions from './ImageModeOptions.vue'
import { useAuthStore } from '@/stores/auth'
import Close from 'vue-material-design-icons/Close.vue'
// Dodajemy import ikony Facebooka
import Facebook from 'vue-material-design-icons/Facebook.vue'
import AppCloseHeader from '@/layouts/AppCloseHeader.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PrivacyModal from './PrivacyModal.vue'

const auth = useAuthStore()
const router = useRouter()
const showSettingsModal = ref(false)

// --- TYPY ---
interface FontStyle {
  id: string
  label: string
  class: string
}

defineProps<{
  isMusicModalOpen: boolean
  isImageSelected: boolean
  hasMusic?: boolean
  mode?: 'image' | 'text'
  selectedBackgroundId?: number
  selectedFontId?: string
}>()

const emit = defineEmits<{
  (e: 'add-text'): void
  (e: 'add-image'): void
  (e: 'toggle-music'): void
  (e: 'add-link'): void
  (e: 'back'): void
  (e: 'save-alt-text', text: string): void
  (e: 'export-story'): void
  (e: 'select-background', id: number): void
  (e: 'select-font', font: FontStyle): void
}>()

const goBack = () => {
  router.back()
}
</script>

<template>
  <aside
    class="w-full lg:w-[360px] bg-theme-bg-secondary h-full flex flex-col relative z-20 shadow-xl shrink-0  "
  >
    <div class="flex-shrink-0 bg-theme-bg-secondary pb-2">
      <AppCloseHeader />

      <div class="flex items-center justify-between px-4 pb-4 pt-2">
        <h1 class="text-2xl font-bold text-theme-text tracking-tight">Twoja relacja</h1>
        <button
          @click="showSettingsModal = true"
          class="bg-gray-200/50 p-2 rounded-full cursor-pointer hover:bg-gray-200 transition-colors flex items-center justify-center"
        >
          <Cog :size="24" class="text-theme-text" />
        </button>
      </div>

      <div class="flex items-center gap-3 px-4 pb-4">
        <div class="w-14 h-14 rounded-full overflow-hidden border border-gray-200 shrink-0">
          <img
            :src="auth.currentUser?.avatar || 'https://via.placeholder.com/150'"
            class="w-full h-full object-cover"
            alt="User Avatar"
          />
        </div>
        <span class="text-[17px] font-semibold text-theme-text">
          {{ auth.currentUser?.name || 'Użytkownik' }}
        </span>
      </div>

      <div class="border-b border-gray-200 mx-0"></div>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar bg-theme-bg-secondary">
      <TextModeOptions
        v-if="mode === 'text'"
        :is-music-modal-open="isMusicModalOpen"
        :selected-background-id="selectedBackgroundId"
        :selected-font-id="selectedFontId"
        @select-background="(id) => emit('select-background', id)"
        @select-font="(font) => emit('select-font', font)"
        @toggle-music="emit('toggle-music')"
      />

      <ImageModeOptions
        v-if="mode === 'image'"
        :is-music-modal-open="isMusicModalOpen"
        :is-image-selected="isImageSelected"
        :has-music="hasMusic"
        @add-text="emit('add-text')"
        @toggle-music="emit('toggle-music')"
        @add-link="emit('add-link')"
        @save-alt-text="(text) => emit('save-alt-text', text)"
      />
    </div>

    <div
      v-if="mode !== undefined"
      class="p-4 flex gap-3 bg-theme-bg-secondary shadow-sm z-30 border-t border-gray-100"
    >
      <button
        @click="emit('back')"
        class="w-[40%] py-2.5 rounded-lg bg-gray-200 text-black font-semibold hover:bg-gray-300 transition-colors text-[15px]"
      >
        Odrzuć
      </button>

      <button
        @click="emit('export-story')"
        class="w-[60%] py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 transition-colors shadow-sm text-[15px]"
      >
        Udostępnij w relacji
      </button>
    </div>
  </aside>

  <BaseModal
    v-if="showSettingsModal"
    title="Ustawienia prywatności relacji"
    @close="showSettingsModal = false"
  >
    <PrivacyModal @close="showSettingsModal = false" />
  </BaseModal>
</template>

<style scoped>
/* Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #94a3b8;
}

/* Animacja wjazdu z lewej */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.slide-in {
  opacity: 0; /* Domyślnie niewidoczne przed animacją */
  animation: slideInLeft 0.5s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}
</style>
