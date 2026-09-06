<script setup lang="ts">
defineProps<{
  recipientName: string
  recipientAvatar: string
  videoEnabled: boolean
  microphoneEnabled: boolean
}>()

const emit = defineEmits<{
  'open-add-user': []
  'toggle-video': []
  'toggle-mute': []
  'disconnect': []
}>()
</script>

<template>
  <div class="absolute inset-0 flex flex-col items-center justify-center z-30 bg-[#202124]">
    <!-- Top Left: Informacje o podłączonych urządzeniach -->
    <div class="absolute top-6 left-6 flex items-center gap-3">
      <div class="w-10 h-10 rounded-full bg-[#3C4043] flex items-center justify-center text-zinc-300">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
        </svg>
      </div>
      <div>
        <div class="flex items-center gap-1.5">
          <div class="w-2 h-2 rounded-full bg-green-500"></div>
          <span class="text-[13px] font-medium text-zinc-200 leading-tight">Podłączony mikrofon i głośnik: Domyślne</span>
        </div>
      </div>
    </div>

    <!-- Center: Odbiorca -->
    <div class="flex flex-col items-center justify-center mb-12">
      <div class="w-[120px] h-[120px] rounded-full overflow-hidden mb-5 bg-zinc-800 ring-4 ring-white/10 shadow-2xl">
        <img :src="recipientAvatar" class="w-full h-full object-cover" />
      </div>
      <h1 class="text-[28px] font-medium text-white mb-1.5">{{ recipientName }}</h1>
      <p class="text-[15px] text-zinc-400 tracking-wide flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-blue-500 animate-ping"></span>
        Dzwonienie...
      </p>
    </div>

    <!-- Bottom: Controls -->
    <div class="absolute bottom-8 flex items-center gap-4">
      <button
        @click="emit('open-add-user')"
        class="w-[50px] h-[50px] rounded-full bg-[#3C4043] hover:bg-[#4E5256] flex items-center justify-center transition-colors cursor-pointer text-zinc-300"
        title="Dodaj osobę do rozmowy"
      >
        <svg class="w-5 h-5 text-zinc-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
        </svg>
      </button>

      <button
        @click="emit('toggle-video')"
        class="w-[50px] h-[50px] rounded-full flex items-center justify-center transition-colors cursor-pointer text-white"
        :class="videoEnabled ? 'bg-[#3C4043]' : 'bg-[#EA4335]'"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
          <line v-show="!videoEnabled" x1="3" y1="3" x2="21" y2="21" stroke="currentColor" stroke-width="2" />
        </svg>
      </button>

      <button
        @click="emit('toggle-mute')"
        class="w-[50px] h-[50px] rounded-full flex items-center justify-center transition-colors cursor-pointer text-white"
        :class="microphoneEnabled ? 'bg-[#3C4043]' : 'bg-[#EA4335]'"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path v-if="microphoneEnabled" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
          <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
        </svg>
      </button>

      <button
        @click="emit('disconnect')"
        class="w-[60px] h-[40px] rounded-full bg-[#EA4335] hover:bg-[#D33426] flex items-center justify-center transition-colors px-4 cursor-pointer text-white"
        title="Rozłącz"
      >
        <svg class="w-[22px] h-[22px] rotate-[135deg]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.94.725l.548 2.2a1 1 0 01-.321.988l-1.305.98a10.582 10.582 0 004.872 4.872l.98-1.305a1 1 0 01.988-.321l2.2.548a1 1 0 01.725.94V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
        </svg>
      </button>
    </div>
  </div>
</template>
