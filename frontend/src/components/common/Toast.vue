<template>
  <!-- TRYB 1: Karta powiadomienia (Zrzut ekranu 2026-06-23 o 08.55.46.jpg) -->
  <div v-if="header || avatar" class="flex flex-col gap-2.5 w-full   text-theme-text">
    <!-- Górna belka: Tytuł + Przycisk zamknięcia -->
    <div class="flex items-center justify-between gap-2 w-full">
      <span v-if="header" class="text-[15px] font-semibold leading-none">
        {{ header }}
      </span>

      <button
        @click="$emit('close-toast')"
        class="flex items-center justify-center w-7 h-7 ml-auto text-gray-500 bg-gray-100 hover:bg-gray-200 dark:bg-gray-800 dark:hover:bg-gray-700 rounded-full transition-colors shrink-0 cursor-pointer"
      >
        <svg
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>

    <!-- Dolna sekcja: Awatar/Ikona + Treść + Kropka unread -->
    <div class="flex items-center gap-3 w-full">
      <!-- Awatar z opcjonalną małą ikoną w rogu (badge) -->
      <div v-if="avatar" class="relative shrink-0 self-center">
        <img :src="avatar" :alt="$t('chat.avatar')" class="w-11 h-11 rounded-full object-cover" />
        <div
          v-if="avatarBadgeIcon"
          :class="[
            'absolute -bottom-1 -right-1 flex items-center justify-center w-5 h-5 rounded-full ring-2 ring-white dark:ring-gray-900',
            avatarBadgeColor,
          ]"
        >
          <component :is="avatarBadgeIcon" :size="11" fillColor="currentColor" />
        </div>
      </div>

      <!-- Zwykła ikona (gdyby w trybie karty nie podano awatara) -->
      <div v-else-if="icon" :class="['shrink-0 flex items-center justify-center', iconColor]">
        <component :is="icon" :size="24" fillColor="currentColor" />
      </div>

      <!-- Tekst główny + Czas / Akcja -->
      <div class="grow flex flex-col gap-1 min-w-0">
        <div class="text-[14px] font-normal leading-snug break-words">
          {{ title }}
        </div>

        <div class="flex items-center gap-3 mt-0.5">
          <span v-if="time" class="text-[13px] font-semibold text-blue-600">
            {{ time }}
          </span>
          <button
            v-if="action"
            @click.stop="action.handler"
            class="text-[13px] font-semibold text-blue-600 hover:underline cursor-pointer"
          >
            {{ action.label }}
          </button>
        </div>
      </div>

      <!-- Niebieska kropka "nieprzeczytane" -->
      <div v-if="unread" class="shrink-0 self-center pl-1">
        <span class="block w-2.5 h-2.5 rounded-full bg-blue-600"></span>
      </div>
    </div>
  </div>

  <!-- TRYB 2: Twój dotychczasowy, klasyczny Toast (bez zmian) -->
  <div v-else class="flex items-center gap-3 w-full   text-theme-text">
    <div v-if="icon" :class="['shrink-0 flex items-center justify-center', iconColor]">
      <component :is="icon" :size="24" fillColor="currentColor" />
    </div>

    <div class="text-[15px] font-normal grow leading-snug">
      {{ title }}
    </div>

    <button
      v-if="action"
      @click.stop="action.handler"
      class="text-[14px] font-semibold text-blue-600 hover:text-blue-700 hover:underline shrink-0 px-2 cursor-pointer"
    >
      {{ action.label }}
    </button>

    <button
      @click="$emit('close-toast')"
      class="flex items-center justify-center w-8 h-8 ml-1 text-gray-400 bg-transparent hover:bg-theme-hover rounded-full transition-colors shrink-0 cursor-pointer"
    >
      <svg
        width="18"
        height="18"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps({
  // Dotychczasowe
  title: { type: String, required: true },
  icon: { type: Object, required: false, default: null },
  iconColor: { type: String, required: false, default: '' },
  action: { type: Object, required: false },

  // Nowe (dla układu powiadomienia)
  header: { type: String, default: '' },
  time: { type: String, default: '' },
  avatar: { type: String, default: '' },
  avatarBadgeIcon: { type: Object, default: null },
  avatarBadgeColor: { type: String, default: 'bg-red-500 text-white' },
  unread: { type: Boolean, default: false },
})

defineEmits(['close-toast'])
</script>
