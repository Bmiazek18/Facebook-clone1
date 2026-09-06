<script setup lang="ts">
import { ref, computed } from 'vue'

// Importy ikon z vue-material-design-icons
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue'
import PencilOutlineIcon from 'vue-material-design-icons/PencilOutline.vue'
import SquareEditOutlineIcon from 'vue-material-design-icons/SquareEditOutline.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'

// Interfejs pojedynczego kroku
interface SetupStep {
  id: string
  title: string
  icon: any
  completed: boolean
}

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'step-click', step: SetupStep): void
}>()

// Definicja kroków z listy
const steps = ref<SetupStep[]>([
  { id: 'invite', title: 'Zaproś ludzi do dołączenia', icon: AccountPlusIcon, completed: false },
  { id: 'cover', title: 'Dodaj zdjęcie w tle', icon: ImageOutlineIcon, completed: false },
  { id: 'description', title: 'Dodaj opis', icon: PencilOutlineIcon, completed: false },
  { id: 'post', title: 'Utwórz post', icon: SquareEditOutlineIcon, completed: false }
])

// Wyliczanie ukończonych kroków
const completedCount = computed(() => steps.value.filter(s => s.completed).length)
const totalSteps = computed(() => steps.value.length)

// Kliknięcie w dany krok
const handleStepClick = (step: SetupStep) => {
  step.completed = !step.completed // Przełączanie ukończenia do testów
  emit('step-click', step)
}
</script>

<template>
  <div class="bg-white dark:bg-[#242526] text-[#050505] dark:text-[#e4e6eb] font-sans rounded-2xl p-2 shadow-md border border-gray-200 dark:border-[#3e4042] w-full max-w-lg select-none">

    <!-- Nagłówek z przyciskiem zamknięcia -->
    <div class="flex items-start p-1 justify-between mb-1">
      <div>
        <h2 class="text-[20px] font-bold leading-tight">{{ $t('groups.ukonczKonfigurowanieGrupy') }}</h2>
        <div class="text-[15px] font-bold mt-1">
          <span>{{ $t('groups.ukonczono') }}</span>
          <span class="text-[#2e7d32] dark:text-[#45a049]">{{ completedCount }} z {{ totalSteps }}</span>
          <span>{{ $t('groups.krokow') }}</span>
        </div>
      </div>

      <button
        type="button"
        @click="emit('close')"
        class="text-[#65676b] dark:text-[#b0b3b8] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] p-1.5 rounded-full transition-colors"
        :title="$t('common.close')"
      >
        <CloseIcon :size="20" />
      </button>
    </div>

    <!-- Subtitle / Opis -->
    <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] leading-snug mb-5">{{ $t('groups.kontynuujDodawanieKluczowychInformacji') }}</p>

    <!-- Linia oddzielająca (subtelna) -->
    <hr class="border-gray-200 dark:border-[#3e4042] mb-4" />

    <!-- Lista kroków -->
    <div class="space-y-2">
      <button
        v-for="step in steps"
        :key="step.id"
        type="button"
        @click="handleStepClick(step)"
        class="w-full flex items-center gap-4 p-2.5 rounded-xl hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors text-left group cursor-pointer"
      >
        <!-- Okrągła ikona -->
        <div
          class="w-12 h-12 rounded-full flex items-center justify-center shrink-0 transition-colors"
          :class="[
            step.completed
              ? 'bg-[#2e7d32] text-white'
              : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] group-hover:bg-[#d8dadf] dark:group-hover:bg-[#4e4f50]'
          ]"
        >
          <CheckIcon v-if="step.completed" :size="22" />
          <component v-else :is="step.icon" :size="22" />
        </div>

        <!-- Tytuł kroku -->
        <span
          class="text-[17px] font-semibold leading-snug"
          :class="{ 'line-through text-[#65676b] dark:text-[#b0b3b8]': step.completed }"
        >
          {{ step.title }}
        </span>
      </button>
    </div>

  </div>
</template>
