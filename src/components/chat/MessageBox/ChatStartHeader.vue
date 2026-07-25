<!-- @/components/chat/ChatStartHeader.vue -->
<script setup lang="ts">
defineProps<{
  name: string
  subtitle?: string
  avatarUrl?: string
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center p-6 text-center select-none mb-6">
    <!-- Awatar -->
    <div class="mb-4">
      <img
        :src="avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150'"
        :alt="name"
        class="w-24 h-24 rounded-full object-cover shadow-sm"
      />
    </div>

    <!-- Nazwa rozmówcy / grupy -->
    <h2 class="text-[22px] font-bold text-theme-text tracking-tight leading-snug mb-1">
      {{ name }}
    </h2>

    <!-- Podtekst -->
    <p v-if="subtitle" class="text-[14px] text-gray-500 mb-10">
      {{ subtitle }}
    </p>

    <!-- Szyfrowanie -->
    <div class="max-w-[620px] text-[13px] text-gray-500浏览 leading-normal px-4">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
        class="w-3.5 h-3.5 inline-block align-text-bottom mr-1 text-theme-text opacity-80 -mt-0.5"
      >
        <path fill-rule="evenodd" d="M12 1.5a5.25 5.25 0 00-5.25 5.25v3a3 3 0 00-3 3v6.75a3 3 0 003 3h10.5a3 3 0 003-3v-6.75a3 3 0 00-3-3v-3c0-2.9-2.35-5.25-5.25-5.25zm3.75 8.25v-3a3.75 3.75 0 10-7.5 0v3h7.5z" clip-rule="evenodd" />
      </svg>
      Wiadomości i połączenia są zabezpieczone pełnym szyfrowaniem. Tylko uczestnicy tego czatu mogą je odczytywać, odsłuchiwać i udostępniać.
      <a href="#" class="text-[#0064e0] font-semibold hover:underline ml-1" @click.prevent>
        Dowiedz się więcej
      </a>
    </div>
  </div>
</template>
