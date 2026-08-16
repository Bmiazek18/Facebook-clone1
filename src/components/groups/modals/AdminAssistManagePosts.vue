<script setup lang="ts">
import { ref, computed } from 'vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import '@/assets/animations/slideTransition.css'

// Importy ikon
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import MessageAlertIcon from 'vue-material-design-icons/MessageAlert.vue'
import LightbulbOutlineIcon from 'vue-material-design-icons/LightbulbOutline.vue'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', data: any): void
}>()

// --- Definicje kryteriów (Widok Listy) ---
const criteriaList = [
  { id: 'reported_post', label: 'Zgłoszony post', icon: MessageAlertIcon },
  { id: 'reported_anon_post', label: 'Zgłoszony anonimowy post', icon: MessageAlertIcon },
]

// --- Stan formularza ---
const activeCriterion = ref<string | null>(null)
const selectedCount = ref(3)
const linkWithAction = ref(false)

// --- Integracja useSlideTransition ---
const {
  wrapperRef,
  currentView,
  transitionName,
  navigateTo,
  navigateBack,
  onEnter,
  onAfterEnter,
} = useSlideTransition('list')

const isTransitioningHeight = ref(false)

const onEnterWithTransition = (el: Element) => {
  isTransitioningHeight.value = true
  onEnter(el)
}

const onAfterEnterWithTransition = () => {
  isTransitioningHeight.value = false
  onAfterEnter()
}

// --- Obsługa nawigacji ---
const handleSelectCriterion = (id: string) => {
  activeCriterion.value = id
  selectedCount.value = 3 // Domyślna wartość ze zrzutów ekranu
  linkWithAction.value = false // Reset checkboxa
  navigateTo('detail')
}

const handleBack = () => {
  navigateBack()
  setTimeout(() => {
    activeCriterion.value = null
  }, 300)
}

const handleSave = () => {
  emit('save', {
    type: activeCriterion.value,
    count: selectedCount.value,
    linkWithAction: linkWithAction.value
  })
  emit('close')
}

// --- Mapowanie danych dla widoków szczegółowych ---
const detailConfig = computed(() => {
  if (activeCriterion.value === 'reported_post') {
    return {
      tipText: 'Zgłoszone posty mogą być nie na temat lub naruszać reguły grupy.',
      rulePrefix: 'Post został zgłoszony przynajmniej',
      ruleSuffix: 'razy',
      options: [1, 2, 3, 4, 5]
    }
  }

  if (activeCriterion.value === 'reported_anon_post') {
    return {
      tipText: 'Zgłoszone posty od anonimowych autorów mogą być nie na temat lub naruszać zasady grupy.',
      rulePrefix: 'Anonimowy post został zgłoszony co najmniej',
      ruleSuffix: 'raz(y)',
      options: [1, 2, 3, 4, 5]
    }
  }

  return null
})
</script>

<template>
  <div class="flex flex-col w-[550px] max-w-full mx-auto bg-white dark:bg-[#242526] font-sans text-[#050505] dark:text-[#e4e6eb] shadow-lg rounded-xl overflow-hidden border border-gray-200 dark:border-[#3e4042]">

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
        <!-- ========================================== -->
        <!-- WIDOK 1: LISTA (Wybór kryterium)             -->
        <!-- ========================================== -->
        <div v-if="currentView === 'list'" key="list" class="view-container">
          <div class="p-4 sm:p-6 pb-8">

            <h1 class="text-[20px] font-bold leading-tight mb-1">
              Zarządzaj opublikowanymi postami
            </h1>
            <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] mb-6 leading-snug">
              Rozpocznij od jednego kryterium, a potem dodaj ich więcej.
            </p>

            <h2 class="text-[14px] font-normal text-[#050505] dark:text-[#b0b3b8] uppercase tracking-wide mb-3">
              KRYTERIA DOTYCZĄCE POSTÓW
            </h2>

            <div class="flex flex-col gap-1">
              <button
                v-for="item in criteriaList"
                :key="item.id"
                @click="handleSelectCriterion(item.id)"
                class="flex items-center justify-between p-3 -mx-3 rounded-xl hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors group cursor-pointer text-left"
              >
                <!-- Lewa strona: Ikona w kółku + Etykieta -->
                <div class="flex items-center gap-3.5">
                  <div class="w-11 h-11 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] flex items-center justify-center shrink-0 text-[#1c1e21] dark:text-[#e4e6eb]">
                    <component :is="item.icon" :size="22" />
                  </div>
                  <span class="text-[17px] font-bold leading-tight">
                    {{ item.label }}
                  </span>
                </div>

                <!-- Prawa strona: Strzałka -->
                <ChevronRightIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] group-hover:text-[#050505] dark:group-hover:text-[#e4e6eb] transition-colors shrink-0 ml-4" />
              </button>
            </div>

          </div>
        </div>

        <!-- ========================================== -->
        <!-- WIDOK 2: SZCZEGÓŁY (Dostosuj kryteria)       -->
        <!-- ========================================== -->
        <div v-else-if="currentView === 'detail' && detailConfig" key="detail" class="view-container flex flex-col max-h-[85vh]">

          <!-- Przewijana zawartość szczegółów -->
          <div class="p-4 sm:p-6 pb-2 overflow-y-auto flex-1">

            <!-- Nagłówek (Przycisk Wstecz + Tytuł sekcji) -->
            <div class="flex items-center gap-3 mb-6 -ml-2">
              <button
                @click="handleBack"
                class="w-9 h-9 rounded-full hover:bg-gray-100 dark:hover:bg-[#3a3b3c] flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] transition-colors cursor-pointer shrink-0"
              >
                <ArrowLeftIcon :size="24" />
              </button>
            </div>

            <h1 class="text-[24px] font-bold leading-tight mb-4">
              Dostosuj kryteria do potrzeb grupy
            </h1>

            <!-- Baner z podpowiedzią (Żarówka) -->
            <div class="flex items-start gap-3 bg-[#f7f8fa] dark:bg-[#3a3b3c]/50 p-4 rounded-xl mb-8 border border-gray-100 dark:border-transparent">
              <LightbulbOutlineIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8] shrink-0 mt-0.5" />
              <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] leading-relaxed">
                {{ detailConfig.tipText }}
              </p>
            </div>

            <!-- Konfiguracja Reguły -->
            <div class="mb-8">
              <h2 class="text-[20px] font-bold mb-4">
                Przenieś opublikowany post do weryfikacji, jeśli
              </h2>

              <!-- Złożone zdanie z wyróżnioną wartością -->
              <p class="text-[16px] text-[#050505] dark:text-[#e4e6eb] mb-4">
                {{ detailConfig.rulePrefix }}
                <span class="inline-flex items-center justify-center bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff] font-bold px-2 py-0.5 rounded mx-1">
                  {{ selectedCount }}
                </span>
                {{ detailConfig.ruleSuffix }}
              </p>

              <!-- Siatka Pigułek Wyboru (Radio Buttons jako przyciski) -->
              <div class="flex flex-wrap gap-2 sm:gap-3">
                <button
                  v-for="option in detailConfig.options"
                  :key="option"
                  @click="selectedCount = option"
                  :class="[
                    'flex-1 min-w-[60px] py-2.5 rounded-lg text-[15px] font-bold transition-colors cursor-pointer',
                    selectedCount === option
                      ? 'bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff]'
                      : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50]'
                  ]"
                >
                  {{ option }}
                </button>
              </div>
            </div>

            <!-- Ustawienia opcjonalne (Checkbox) -->
            <div>
              <h3 class="text-[17px] font-bold mb-4">Ustawienia opcjonalne</h3>
              <label class="flex items-center justify-between cursor-pointer group">
                <span class="text-[16px] font-medium text-[#050505] dark:text-[#e4e6eb]">
                  Połącz te kryteria z innym działaniem
                </span>
                <input
                  type="checkbox"
                  v-model="linkWithAction"
                  class="w-6 h-6 rounded border-gray-400 text-[#1877f2] focus:ring-[#1877f2] bg-white cursor-pointer ml-4 shrink-0"
                />
              </label>
            </div>

          </div>

          <!-- Pasek akcji na dole -->
          <div class="border-t border-gray-200 dark:border-[#3e4042] p-4 bg-white dark:bg-[#242526] shrink-0 mt-4">
            <button
              @click="handleSave"
              class="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white font-semibold text-[15px] py-2.5 rounded-lg transition-colors cursor-pointer"
            >
              Dodaj do Asystenta administratora
            </button>
          </div>
        </div>

      </Transition>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  width: 100%;
}
</style>
