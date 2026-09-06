<script setup lang="ts">
defineProps<{
  microphoneEnabled: boolean
  videoEnabled: boolean
  isSharingScreen: boolean
  filterMenuOpen: boolean
  currentFilter: 'none' | 'blur' | 'image'
}>()

const emit = defineEmits<{
  'toggle-mute': []
  'toggle-video': []
  'toggle-screenshare': []
  'open-add-user': []
  'toggle-filter-menu': []
  'change-filter': [type: 'none' | 'blur' | 'image']
  'disconnect': []
}>()
</script>

<template>
  <div class="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex items-center gap-4 z-20">
    <!-- Mic toggle -->
    <button
      @click="emit('toggle-mute')"
      :class="[
        'w-12 h-12 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md',
        microphoneEnabled ? 'bg-white/20 hover:bg-white/30 text-white' : 'bg-red-500 hover:bg-red-600 text-white',
      ]"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path v-if="microphoneEnabled" stroke-linecap="round" stroke-linejoin="round" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
        <path v-else stroke-linecap="round" stroke-linejoin="round" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
      </svg>
    </button>

    <!-- Camera toggle -->
    <button
      @click="emit('toggle-video')"
      :class="[
        'w-12 h-12 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md',
        videoEnabled ? 'bg-white/20 hover:bg-white/30 text-white' : 'bg-red-500 hover:bg-red-600 text-white',
      ]"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
        <line v-show="!videoEnabled" x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2" />
      </svg>
    </button>

    <!-- Screen share toggle -->
    <button
      @click="emit('toggle-screenshare')"
      :class="[
        'w-12 h-12 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md',
        isSharingScreen ? 'bg-green-500 hover:bg-green-600 text-white' : 'bg-white/20 hover:bg-white/30 text-white',
      ]"
      title="Udostępnij ekran"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
      </svg>
    </button>

    <!-- Add participant -->
    <button
      @click="emit('open-add-user')"
      class="w-12 h-12 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md text-white"
      title="Dodaj osobę do rozmowy"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
      </svg>
    </button>

    <!-- Virtual background / Effects dropdown -->
    <div class="relative">
      <button
        @click="emit('toggle-filter-menu')"
        class="w-12 h-12 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md text-white"
        title="Efekty i filtry"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
        </svg>
      </button>

      <div
        v-if="filterMenuOpen"
        class="absolute bottom-16 left-1/2 transform -translate-x-1/2 bg-zinc-900 border border-zinc-800 rounded-xl p-2 flex flex-col gap-1 shadow-2xl w-44 z-30"
      >
        <button
          @click="emit('change-filter', 'none')"
          :class="[
            'text-xs text-left px-3 py-2 rounded-lg transition-colors cursor-pointer',
            currentFilter === 'none' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
          ]"
        >
          Brak filtra
        </button>
        <button
          @click="emit('change-filter', 'blur')"
          :class="[
            'text-xs text-left px-3 py-2 rounded-lg transition-colors cursor-pointer',
            currentFilter === 'blur' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
          ]"
        >
          Rozmycie postaci
        </button>
        <button
          @click="emit('change-filter', 'image')"
          :class="[
            'text-xs text-left px-3 py-2 rounded-lg transition-colors cursor-pointer',
            currentFilter === 'image' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
          ]"
        >
          Wgraj własne tło...
        </button>
      </div>
    </div>

    <!-- Hang up -->
    <button
      @click="emit('disconnect')"
      class="w-12 h-12 bg-red-600 hover:bg-red-700 rounded-full flex items-center justify-center transition-colors shadow-lg cursor-pointer text-white"
      title="Zakończ połączenie"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 rotate-[135deg]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.94.725l.548 2.2a1 1 0 01-.321.988l-1.305.98a10.582 10.582 0 004.872 4.872l.98-1.305a1 1 0 01.988-.321l2.2.548a1 1 0 01.725.94V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
      </svg>
    </button>
  </div>
</template>
