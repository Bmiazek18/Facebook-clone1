<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['reconnect', 'close'])
const selectedRating = ref(0)
</script>

<template>
  <div
    class="w-full h-screen bg-[#0a0a0a] text-white flex flex-col items-center justify-center   select-none"
  >
    <div class="flex flex-col items-center justify-center flex-grow text-center px-4">
      <h2 class="text-xl font-medium tracking-wide mb-4">Jak oceniasz jakość połączenia?</h2>

      <div class="flex gap-2 mb-8">
        <svg
          v-for="i in 5"
          :key="i"
          @click="selectedRating = i"
          xmlns="http://www.w3.org/2000/svg"
          class="h-8 w-8 cursor-pointer transition-colors"
          :class="i <= selectedRating ? 'text-yellow-500' : 'text-zinc-600 hover:text-zinc-400'"
          fill="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
          />
        </svg>
      </div>

      <button
        @click="emit('close')"
        class="px-6 py-2.5 bg-zinc-800 hover:bg-zinc-700 active:bg-zinc-600 text-sm font-medium rounded-lg transition-colors"
      >
        Nie teraz
      </button>
    </div>

    <div class="pb-12 flex gap-12 justify-center items-center">
      <div class="flex flex-col items-center gap-2">
        <button
          @click="emit('reconnect')"
          class="w-14 h-14 bg-green-500 hover:bg-green-600 active:bg-green-700 rounded-full flex items-center justify-center text-white shadow-lg transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
            />
          </svg>
        </button>
        <span class="text-xs text-zinc-400">Połącz ponownie</span>
      </div>

      <div class="flex flex-col items-center gap-2">
        <button
          @click="emit('close')"
          class="w-14 h-14 bg-zinc-800 hover:bg-zinc-700 active:bg-zinc-600 rounded-full flex items-center justify-center text-zinc-300 shadow-lg transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        <span class="text-xs text-zinc-400">Zamknij</span>
      </div>
    </div>
  </div>
</template>
