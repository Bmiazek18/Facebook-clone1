<template>
  <div class="bg-white rounded-2xl shadow-sm p-5 max-w-sm font-sans border border-gray-100">
    <h2 class="text-[22px] font-bold text-gray-900 mb-5 tracking-tight">{{ $t('dashboard.statusStrony') }}</h2>

    <div class="flex items-center gap-4">

      <!-- Awatar -->
      <div class="relative w-[56px] h-[56px] shrink-0">
        <!-- Tło i ikona użytkownika -->
        <div class="w-full h-full bg-[#E4E6EB] border border-gray-200 rounded-full flex items-center justify-center overflow-hidden">
          <img :src="images" :alt="$t('chat.avatar')" class="w-full h-full object-cover" />
        </div>

        <!-- Zielona plakietka (Checkmark) -->
        <div class="absolute -bottom-1 -right-1 w-[22px] h-[22px] bg-[#31A24C] border-[3px] border-white rounded-full flex items-center justify-center shadow-sm">
          <svg viewBox="0 0 24 24" class="w-3.5 h-3.5 fill-white">
            <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
          </svg>
        </div>
      </div>

      <!-- Informacje (Imię i postęp) -->
      <div class="flex flex-col">
        <div class="font-semibold text-[17px] text-gray-900 leading-tight mb-0.5">{{ $t('dashboard.kont') }}</div>
        <div class="text-[15px] text-[#65676B] flex items-center">
          <span>{{ $t('dashboard.postepTygodniowy') }}</span>
          <span class="text-gray-900 font-semibold ml-1.5">0 %</span>
          <div class="w-[15px] h-[15px] rounded-full border-[2.5px] border-[#CED0D4] ml-2"></div>
        </div>
      </div>

    </div>
  </div>
</template>
<script setup lang="ts">
import images from '~/assets/images/default_avatar.png'
</script>
