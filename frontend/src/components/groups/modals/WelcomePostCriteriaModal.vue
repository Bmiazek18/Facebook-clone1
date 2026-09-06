<script setup lang="ts">
import { ref, watch, markRaw } from 'vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import '@/assets/animations/slideTransition.css'

// Importy ikon
import LightbulbOutlineIcon from 'vue-material-design-icons/LightbulbOutline.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import MenuDownIcon from 'vue-material-design-icons/MenuDown.vue'
import CalendarBlankIcon from 'vue-material-design-icons/CalendarBlank.vue'
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue'

// Stan formularza - typ wyzwalacza
const triggerType = ref<'count' | 'schedule'>('count')

// Stan dla "Liczba nowych członków"
const memberCount = ref(1)

// Stan dla "Harmonogram"
const repeatOption = ref('Raz w tygodniu')
const startDate = ref('16 sie 2026')
const startTime = ref('23:00')

// --- Integracja useSlideTransition ---
const {
  wrapperRef,
  currentView,
  transitionName,
  navigateTo,
  navigateBack,
  onEnter,
  onAfterEnter,
} = useSlideTransition('count')

const isTransitioningHeight = ref(false)

const onEnterWithTransition = (el: Element) => {
  isTransitioningHeight.value = true
  onEnter(el)
}

const onAfterEnterWithTransition = () => {
  isTransitioningHeight.value = false
  onAfterEnter()
}

// Obserwacja triggerType i animowanie przejścia
watch(triggerType, (newType) => {
  if (newType === 'schedule') {
    navigateTo('schedule')
  } else {
    navigateBack()
  }
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

// Symulacja akcji zapisu
const handleSave = () => {
  console.log('Zapisywanie do Asystenta administratora...', {
    triggerType: triggerType.value,
    memberCount: triggerType.value === 'count' ? memberCount.value : null,
    schedule: triggerType.value === 'schedule' ? {
      repeatOption: repeatOption.value,
      startDate: startDate.value,
      startTime: startTime.value
    } : null
  })
  emit('close')
}
</script>

<template>
  <div class="flex flex-col w-[550px] max-w-full mx-auto bg-white dark:bg-[#242526] font-sans text-[#050505] dark:text-[#e4e6eb] shadow-lg rounded-xl overflow-hidden border border-gray-200 dark:border-[#3e4042]">

    <!-- Główna zawartość z przewijaniem -->
    <div class="p-4 sm:p-6 overflow-y-auto max-h-[85vh]">

      <!-- Nagłówek -->
      <h1 class="text-[22px] sm:text-[24px] font-bold leading-tight mb-5">{{ $t('groups.dostosujTenPostPowitalny') }}</h1>

      <!-- Baner informacyjny -->
      <div class="flex items-start gap-3 bg-[#f0f2f5] dark:bg-[#3a3b3c] p-4 rounded-xl mb-6">
        <LightbulbOutlineIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] shrink-0 mt-0.5" />
        <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] leading-snug">{{ $t('groups.asystentAdministratoraAutomatycznieOznaczy') }}</p>
      </div>

      <!-- Podgląd posta -->
      <div class="mb-8">
        <p class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] mb-2 ml-1">{{ $t('groups.podgladPosta') }}</p>

        <div class="border border-gray-300 dark:border-[#3e4042] rounded-xl overflow-hidden bg-white dark:bg-[#242526] shadow-sm">

          <!-- Nagłówek posta (Autor) -->
          <div class="flex items-center gap-2.5 p-4 pb-2">
            <!-- Awatar -->
            <div class="w-10 h-10 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] flex items-center justify-center shrink-0 overflow-hidden">
              <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full mt-2">
                <circle cx="18" cy="11" r="6" fill="#8c939d" />
                <path d="M7 31C7 24 12 21 18 21C24 21 29 24 29 31L7 31Z" fill="#8c939d" />
              </svg>
            </div>

            <div class="flex flex-col">
              <span class="text-[15px] font-bold leading-tight">{{ $t('groups.testTestowy') }}</span>
              <div class="flex items-center gap-1 mt-0.5">
                <span class="bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff] text-[12px] font-semibold px-1.5 py-0.5 rounded leading-none">{{ $t('feed.administrator') }}</span>
                <span class="text-[#65676b] dark:text-[#b0b3b8] text-[12px]">·</span>
                <LockIcon :size="12" class="text-[#65676b] dark:text-[#b0b3b8]" />
              </div>
            </div>
          </div>

          <!-- Treść posta -->
          <div class="p-4 pt-1">
            <p class="text-[15px] leading-snug">{{ $t('groups.powitajmyNowychCzlonkowNaszej') }}</p>
          </div>

          <!-- Ilustracja posta (Zastępczy Gradient) -->
          <div class="w-full h-[180px] sm:h-[220px] bg-gradient-to-r from-[#9b51e0] via-[#f2994a] to-[#f2c94c] flex items-center justify-center relative overflow-hidden">
             <div class="absolute inset-0 opacity-30 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjEiIGZpbGw9IiNmZmYiLz48L3N2Zz4=')]"></div>
          </div>

          <!-- Przycisk Edycji w podglądzie -->
          <div class="p-4">
            <button class="w-full bg-[#f0f2f5] hover:bg-[#e4e6eb] dark:bg-[#3a3b3c] dark:hover:bg-[#4e4f50] text-[#1877f2] dark:text-[#4599ff] font-semibold text-[15px] py-2 rounded-lg flex items-center justify-center gap-2 transition-colors cursor-pointer">
              <PencilIcon :size="18" />{{ $t('post.editPost') }}</button>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- SEKCJA: WYBÓR KRYTERIUM PUBLIKACJI         -->
      <!-- ========================================== -->
      <div class="mb-6 border-t border-gray-200 dark:border-[#3e4042] pt-6">
        <h3 class="text-[17px] font-bold mb-4">{{ $t('groups.kryteriumPublikacji') }}</h3>

        <div class="space-y-3">
          <label class="flex items-center gap-3 cursor-pointer group">
            <input
              type="radio"
              value="count"
              v-model="triggerType"
              class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
            />
            <span class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] group-hover:opacity-80 transition-opacity">{{ $t('groups.okreslonaLiczbaDolaczen') }}</span>
          </label>

          <label class="flex items-center gap-3 cursor-pointer group">
            <input
              type="radio"
              value="schedule"
              v-model="triggerType"
              class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
            />
            <span class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] group-hover:opacity-80 transition-opacity">{{ $t('groups.cyklicznyHarmonogram') }}</span>
          </label>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- DYNAMICZNE POLA FORMULARZA (Z przejścia)   -->
      <!-- ========================================== -->
      <div
        class="relative w-full overflow-hidden"
        :class="{ 'transition-[height] duration-300 ease-in-out': isTransitioningHeight }"
        ref="wrapperRef"
      >
        <Transition
          :name="transitionName"
          @enter="onEnterWithTransition"
          @after-enter="onAfterEnterWithTransition"
        >
          <!-- WARIANT 'count' (Liczba dołączeń) -->
          <div v-if="currentView === 'count'" key="count" class="view-container mb-2">
            <h3 class="text-[17px] font-bold mb-1">{{ $t('groups.liczbaNowychCzlonkow') }}</h3>
            <p class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-snug mb-3">{{ $t('groups.wprowadzLiczbeCzlonkowKtorzy') }}</p>

            <input
              v-model="memberCount"
              type="number"
              min="1"
              class="w-full border border-gray-300 dark:border-[#525355] rounded-xl px-4 py-3.5 text-[17px] font-normal focus:outline-none focus:ring-1 focus:ring-[#1877f2] focus:border-[#1877f2] dark:bg-[#242526] transition-all"
            />
          </div>

          <!-- WARIANT 'schedule' (Harmonogram) -->
          <div v-else-if="currentView === 'schedule'" key="schedule" class="view-container mb-2 space-y-6">
            <!-- Powtórz -->
            <div>
              <h3 class="text-[17px] font-bold mb-3">{{ $t('groups.powtorz') }}</h3>
              <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-4 py-2 flex items-center justify-between cursor-pointer focus-within:ring-1 focus-within:ring-[#1877f2] focus-within:border-[#1877f2]">
                <div class="flex flex-col w-full">
                  <label class="text-[12px] text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.powtorz') }}</label>
                  <select v-model="repeatOption" class="w-full appearance-none bg-transparent text-[17px] focus:outline-none cursor-pointer pb-1 dark:text-[#e4e6eb]">
                    <option class="dark:bg-[#242526]">{{ $t('events.codziennie') }}</option>
                    <option class="dark:bg-[#242526]">{{ $t('groups.razWTygodniu') }}</option>
                    <option class="dark:bg-[#242526]">{{ $t('groups.razWMiesiacu') }}</option>
                  </select>
                </div>
                <MenuDownIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] pointer-events-none" />
              </div>
            </div>

            <!-- Opublikuj pierwszy post -->
            <div>
              <h3 class="text-[17px] font-bold mb-3">{{ $t('groups.opublikujPierwszyPost') }}</h3>

              <div class="grid grid-cols-2 gap-3">
                <!-- Data -->
                <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-2 flex items-center gap-3 cursor-text focus-within:ring-1 focus-within:ring-[#1877f2] focus-within:border-[#1877f2]">
                  <CalendarBlankIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] shrink-0" />
                  <div class="flex flex-col w-full">
                    <label class="text-[12px] text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.data') }}</label>
                    <input v-model="startDate" type="text" class="w-full bg-transparent text-[15px] focus:outline-none pb-0.5 dark:text-[#e4e6eb]" />
                  </div>
                </div>

                <!-- Godzina -->
                <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-2 flex items-center gap-3 cursor-text focus-within:ring-1 focus-within:ring-[#1877f2] focus-within:border-[#1877f2]">
                  <ClockOutlineIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] shrink-0" />
                  <div class="flex flex-col w-full">
                    <label class="text-[12px] text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.godzina') }}</label>
                    <input v-model="startTime" type="text" class="w-full bg-transparent text-[15px] focus:outline-none pb-0.5 dark:text-[#e4e6eb]" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>

    </div>

    <!-- Pasek akcji na dole -->
    <div class="border-t border-gray-200 dark:border-[#3e4042] p-4 bg-white dark:bg-[#242526]">
      <button
        @click="handleSave"
        class="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white font-semibold text-[15px] py-2.5 rounded-lg transition-colors cursor-pointer"
      >{{ $t('groups.dodajDoAsystentaAdministratora') }}</button>
    </div>

  </div>
</template>

<style scoped>
.view-container {
  width: 100%;
}
</style>
