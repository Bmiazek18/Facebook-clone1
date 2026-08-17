<template>
  <div class="w-[548px] bg-white rounded-xl shadow-xl overflow-hidden border border-gray-100">
    <div class="p-6 pb-4">
      <h2 class="text-[17px] font-bold text-theme-text">Kto może zobaczyć Twoją relację?</h2>
      <p class="text-[15px] text-theme-text-secondary mt-1">
        Twoja relacja będzie widoczna przez 24 godziny na Facebooku i w Messengerze.
      </p>
    </div>

    <div class="px-2">
      <label
        v-for="option in privacyOptions"
        :key="option.id"
        @click.prevent="handleOptionChange(option.id)"
        :class="[
          'flex items-center justify-between p-4 rounded-xl cursor-pointer transition-all duration-200 select-none m-1',
          selectedOption === option.id ? 'bg-blue-50/50' : 'hover:bg-gray-50',
        ]"
      >
        <div class="flex items-center space-x-4">
          <div
            class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-gray-700 shrink-0"
          >
            <component :is="option.icon" :size="24" />
          </div>

          <div>
            <span class="block text-base font-semibold text-theme-text">
              {{ option.title }}
            </span>
            <span class="block text-sm text-theme-text-secondary mt-0.5">
              {{ option.description }}
            </span>
          </div>
        </div>

        <div class="relative flex items-center justify-center pr-2">
          <input
            type="radio"
            name="privacy"
            :value="option.id"
            :checked="selectedOption === option.id"
            class="sr-only"
          />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 transition-all duration-200 flex items-center justify-center',
              selectedOption === option.id ? 'border-black' : 'border-gray-400',
            ]"
          >
            <div
              v-if="selectedOption === option.id"
              class="w-2.5 h-2.5 rounded-full bg-black"
            ></div>
          </div>
        </div>
      </label>
    </div>

    <div class="p-4 bg-white flex justify-end space-x-3 border-t border-gray-100 mt-2">
      <button
        @click="handleCancel"
        class="px-5 py-2.5 rounded-lg text-sm font-semibold text-blue-600 hover:bg-blue-50 transition-colors duration-200"
      >
        Anuluj
      </button>
      <button
        @click="handleSave"
        class="px-6 py-2.5 rounded-lg text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-md hover:shadow-lg active:scale-95 transition-all duration-200"
      >
        Zapisz
      </button>
    </div>
  </div>

  <!-- Confirm Privacy Change Modal -->
  <BaseModal v-if="showConfirmModal" title="Zmienić prywatność relacji?" @close="cancelChange">
    <div class="p-3 w-[548px] text-theme-text text-[12px] bg-theme-bg-secondary rounded-xl">
      <p class="mb-6 text-[15px] text-theme-text">
        Wprowadzone zmiany będą dotyczyć wszystkich zdjęć i filmów aktualnie zawartych w relacji
        oraz dodanych do niej w przyszłości
      </p>
      <div class="flex justify-end gap-3">
        <button
          @click="cancelChange"
          class="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-black font-semibold rounded-lg transition text-sm cursor-pointer"
        >
          Anuluj
        </button>
        <button
          @click="confirmChange"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition text-sm cursor-pointer shadow-md"
        >
          Zapisz
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, markRaw } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

// Import konkretnych ikon z biblioteki vue-material-design-icons
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountCogIcon from 'vue-material-design-icons/AccountCog.vue'

const emit = defineEmits(['close'])

// Mapowanie danych z przypisaniem zaimportowanych ikon
const privacyOptions = [
  {
    id: 'public',
    title: 'Publiczna',
    description: 'Wszyscy użytkownicy Facebooka lub Messengera',
    icon: markRaw(EarthIcon), // Zabezpieczenie wydajnościowe Vue dla komponentów-obiektów
  },
  {
    id: 'friends',
    title: 'Znajomi',
    description: 'Tylko znajomi z Facebooka',
    icon: markRaw(AccountGroupIcon),
  },
  {
    id: 'custom',
    title: 'Ustawienie niestandardowe',
    description: 'Wiktoria Szerszeń',
    icon: markRaw(AccountCogIcon),
  },
]

const defaultOption = 'public'
const selectedOption = ref(defaultOption)
const showConfirmModal = ref(false)
const pendingOption = ref('')

const handleOptionChange = (optionId) => {
  if (optionId === selectedOption.value) return
  if (optionId !== defaultOption) {
    pendingOption.value = optionId
    showConfirmModal.value = true
  } else {
    selectedOption.value = optionId
  }
}

const confirmChange = () => {
  selectedOption.value = pendingOption.value
  showConfirmModal.value = false
}

const cancelChange = () => {
  showConfirmModal.value = false
}

const handleCancel = () => {
  emit('close')
}

const handleSave = () => {
  console.log('Zapisano:', selectedOption.value)
  emit('close')
}
</script>
