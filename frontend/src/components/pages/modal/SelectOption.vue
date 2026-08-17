<template>
  <div class="max-w-[550px] mx-auto bg-white  p-4 font-sans text-gray-900 shadow-sm ">
    <h2 class="text-xl font-bold mb-5">Która opcja jest dla Ciebie najlepsza?</h2>

    <div class="space-y-4">
      <!-- Opcja 1: Strona publiczna -->
      <label
        class="relative flex items-start p-4 border rounded-xl cursor-pointer transition-all duration-200"
        :class="selectedOption === 'public' ? 'border-blue-500 bg-white ring-1 ring-blue-500' : 'border-gray-300 hover:bg-gray-50'"
      >
        <input
          type="radio"
          name="account_type"
          value="public"
          v-model="selectedOption"
          class="sr-only"
        />

        <!-- Ikona Flagi -->
        <div
          class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center mr-4 transition-colors duration-200"
          :class="selectedOption === 'public' ? 'bg-blue-100 text-blue-600' : 'bg-gray-200 text-gray-700'"
        >
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
            <path d="M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6h-9.6z"/>
          </svg>
        </div>

        <div class="flex-grow pr-10">
          <h3 class="text-base font-bold">Strona publiczna</h3>
          <p class="text-gray-500 text-sm mt-0.5">Rozwijaj się jako firma, twórca lub organizacja.</p>
          <ul class="list-disc list-inside text-sm text-gray-500 mt-2 space-y-1">
            <li>Uzyskaj dostęp do zaawansowanych narzędzi profesjonalnych</li>
            <li>Przyznaj dostęp innym osobom</li>
          </ul>
        </div>

        <!-- Custom Radio Button -->
        <div class="absolute right-4 top-5 flex items-center justify-center w-6 h-6 rounded-full border-2"
             :class="selectedOption === 'public' ? 'border-black' : 'border-gray-400'">
          <div v-if="selectedOption === 'public'" class="w-3 h-3 bg-black rounded-full"></div>
        </div>
      </label>

      <!-- Opcja 2: Ulepsz swój istniejący profil -->
      <label
        class="relative flex items-start p-4 border rounded-xl cursor-pointer transition-all duration-200"
        :class="selectedOption === 'personal' ? 'border-blue-500 bg-white ring-1 ring-blue-500' : 'border-gray-300 hover:bg-gray-50'"
      >
        <input
          type="radio"
          name="account_type"
          value="personal"
          v-model="selectedOption"
          class="sr-only"
        />

        <!-- Ikona Użytkownika -->
        <div
          class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center mr-4 transition-colors duration-200"
          :class="selectedOption === 'personal' ? 'bg-blue-100 text-blue-600' : 'bg-gray-200 text-gray-600'"
        >
          <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </div>

        <div class="flex-grow pr-10">
          <h3 class="text-base font-bold">Ulepsz swój istniejący profil</h3>
          <p class="text-gray-500 text-sm mt-0.5">
            Dodaj narzędzia profesjonalne do swojego profilu głównego, aby dalej powiększać obecną grupę odbiorców.
          </p>
        </div>

        <!-- Custom Radio Button -->
        <div class="absolute right-4 top-5 flex items-center justify-center w-6 h-6 rounded-full border-2"
             :class="selectedOption === 'personal' ? 'border-black' : 'border-gray-400'">
          <div v-if="selectedOption === 'personal'" class="w-3 h-3 bg-black rounded-full"></div>
        </div>
      </label>
    </div>

    <!-- Przyciski akcji -->
    <div class="flex gap-4 mt-8">
      <button
        class="flex-1 py-3 rounded-lg font-semibold text-base transition-colors duration-200"
        :class="selectedOption ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
        :disabled="!selectedOption"
        @click="handleNextClick"
      >
        Dalej
      </button>
      <button
        class="flex-1 py-3 bg-gray-200 text-black font-semibold text-base rounded-lg hover:bg-gray-300 transition-colors duration-200"
      >
        Porównaj opcje
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const handleNextClick = () => {
  if (selectedOption.value) {

    navigateTo('/pages/creation')
  }
}


const selectedOption = ref(null)
</script>
