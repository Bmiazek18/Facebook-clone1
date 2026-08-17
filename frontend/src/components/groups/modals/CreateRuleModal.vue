<script setup lang="ts">
import { ref } from 'vue'
import CustomInput from '~/components/common/CustomInput.vue';
import CustomTextarea from '~/components/common/CustomTextarea.vue';

// Definicja emitowanych zdarzeń
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'create', rule: { title: string; description: string }): void
}>()

// Definicja przykładowych reguł z domyślnym opisem
interface ExampleRule {
  id: string
  title: string
  description: string
}

const exampleRules: ExampleRule[] = [
  {
    id: 'kindness',
    title: 'Bądź miły i uprzejmy',
    description: 'Razem tworzymy serdeczną atmosferę. Traktujemy się z szacunkiem. Urozmaicone dialogi to normalna rzecz, ale pamiętajmy o uprzejmości.'
  },
  {
    id: 'hate_speech',
    title: 'Zakaz nękania i propagowani...',
    description: 'Upewnijmy się, że wszyscy czują się bezpiecznie. Nękanie dowolnego rodzaju nie jest tolerowane, a nienawistne komentarze na temat rasy, religii, kultury, orientacji seksualnej, płci czy tożsamości nie będą akceptowane.'
  },
  {
    id: 'no_spam',
    title: 'Zakaz promocji i spamu',
    description: 'Daj grupie więcej, niż z niej bierzesz. Autopromocja, spam i istotne linki są niedozwolone w tej społeczności.'
  },
  {
    id: 'privacy',
    title: 'Szanuj prywatność innych osób',
    description: 'Bycie częścią grupy wymaga wzajemnego zaufania. Autentyczne, ekspresyjne dyskusje sprawiają, że grupy są świetne, ale mogą też być wrażliwe i prywatne.'
  }
]

// Wybrana aktywna pigułka
const selectedExampleId = ref<string>('kindness')

// Formularz (Tytuł i Opis)
const title = ref<string>("")
const description = ref<string>("")

// Wybór przykładowej reguły
const selectExample = (example: ExampleRule) => {
  selectedExampleId.value = example.id
  title.value = example.title.endsWith('...')
    ? (example.id === 'hate_speech' ? 'Zakaz nękania i propagowania nienawiści' : example.title)
    : example.title
  description.value = example.description
}

// Obsługa zatwierdzenia formularza
const handleSubmit = () => {
  if (!title.value.trim()) return
  emit('create', {
    title: title.value,
    description: description.value
  })
}
</script>

<template>
  <!-- Kontener modala / karty -->
  <div class="bg-white dark:bg-[#242526] text-[#050505] dark:text-[#e4e6eb] p-6  w-full max-w-2xl ">

    <!-- Sekcja 1: Przykładowe reguły -->
    <div class="mb-6">
      <h3 class="text-[17px] font-bold mb-3">
        Przykładowe reguły
      </h3>

      <!-- Pigułki / Chipsy -->
      <div class="flex flex-wrap gap-2">
        <button
          v-for="rule in exampleRules"
          :key="rule.id"
          type="button"
          @click="selectExample(rule)"
          :class="[
            'px-4 py-2 rounded-full text-[15px] font-semibold transition-colors cursor-pointer border',
            selectedExampleId === rule.id
              ? 'bg-[#e7f3ff] dark:bg-[#263951] text-[#1877f2] dark:text-[#4599ff] border-transparent'
              : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] border-transparent hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50]'
          ]"
        >
          {{ rule.title }}
        </button>
      </div>
    </div>

    <!-- Sekcja 2: Formularz -->
    <div>
      <h3 class="text-[17px] font-bold mb-3">
        Utwórz własną regułę
      </h3>

      <div class="space-y-4">

        <!-- Field 1: Tytuł -->

          <CustomInput
            v-model="title"
            type="text"
            class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] font-normal focus:outline-none mt-0.5"
            label="Tytuł"
          />



          <CustomTextarea
            v-model="description"
            rows="4"
            class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] font-normal focus:outline-none resize-none mt-0.5 leading-relaxed"
            label="Opis"
          />
        </div>


    </div>

    <!-- Sekcja 3: Przyciski akcji (Dół) -->
    <div class="flex items-center justify-end gap-3 mt-6">
      <button
        type="button"
        @click="emit('close')"
        class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-[#e7f3ff]/50 dark:hover:bg-[#3a3b3c] transition-colors"
      >
        Anuluj
      </button>

      <button
        type="button"
        @click="handleSubmit"
        :disabled="!title.trim()"
        class="px-6 py-2 rounded-lg text-[15px] font-semibold text-white bg-[#1877f2] hover:bg-[#166fe5] active:bg-[#1465d2] disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm"
      >
        Utwórz
      </button>
    </div>

  </div>
</template>
