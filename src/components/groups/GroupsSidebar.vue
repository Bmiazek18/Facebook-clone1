<template>
  <div class="p-4 pb-10 w-full bg-theme-bg text-theme-text">

    <div class="w-full bg-theme-bg-secondary rounded-lg shadow-sm p-4">

      <div class="flex justify-between items-center mb-3">
        <h1 class="text-2xl font-bold text-theme-text">Grupy</h1>
        <button class="p-2 bg-theme-bg-tertiary hover:bg-theme-hover rounded-full transition-colors">
          <svg class="w-5 h-5 text-theme-text" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>

      <div class="relative mb-4">
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <input
          type="text"
          placeholder="Szukaj grup"
          class="w-full bg-theme-bg-tertiary hover:bg-theme-hover transition-colors text-theme-text rounded-full py-2 pl-10 pr-4 focus:outline-none focus:ring-0 placeholder-gray-500"
        >
      </div>

      <nav class="space-y-1 mb-4">
        <a href="#" class="flex items-center space-x-3 px-2 py-2 rounded-lg bg-blue-50 hover:bg-blue-100 transition-colors">
          <div class="bg-blue-500 rounded-full p-1.5 text-white shrink-0">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path d="M2 5a2 2 0 012-2h12a2 2 0 012 2v10a2 2 0 01-2 2H4a2 2 0 01-2-2V5zm3.293 1.293a1 1 0 011.414 0l3 3a1 1 0 010 1.414l-3 3a1 1 0 01-1.414-1.414L7.586 10 5.293 7.707a1 1 0 010-1.414zM11 12a1 1 0 100 2h3a1 1 0 100-2h-3zM11 7a1 1 0 100 2h3a1 1 0 100-2h-3z" /></svg>
          </div>
          <span class="font-semibold text-[15px] text-theme-text">Twoje Aktualności</span>
        </a>

        <a href="#" class="flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors">
          <div class="bg-theme-bg-tertiary rounded-full p-1.5 text-theme-text shrink-0">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0 1 1 0 002 0zm-5.937.21a1 1 0 011.414 0l1.983 1.984a1 1 0 11-1.414 1.414L4.063 7.624a1 1 0 010-1.414zM10 16a6 6 0 100-12 6 6 0 000 12z" clip-rule="evenodd" /><path d="M12 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
          </div>
          <span class="font-semibold text-[15px] text-theme-text">Odkryj</span>
        </a>

        <a href="#" class="flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors">
          <div class="bg-theme-bg-tertiary rounded-full p-1.5 text-theme-text shrink-0">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z" /></svg>
          </div>
          <span class="font-semibold text-[15px] text-theme-text">Twoje grupy</span>
        </a>
      </nav>

      <router-link to="/add-group" class="w-full flex items-center justify-center space-x-2 bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-md transition-colors mb-4">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        <span>Utwórz nową grupę</span>
      </router-link>

      <div class="border-t border-theme-border mb-4 mx-2"></div>

      <div class="flex justify-between items-center px-2 mb-2">
        <h2 class="text-[17px] font-semibold text-theme-text leading-snug">Grupy, do których należysz</h2>
        <a href="#" class="text-[13px] text-blue-600 hover:underline shrink-0">Wyświetl wszystkie</a>
      </div>

      <div class="space-y-1">
        <a
          v-for="(group, index) in groups"
          :key="index"
          href="#"
          class="flex items-center space-x-3 p-2 rounded-lg hover:bg-theme-hover transition-colors group"
        >
          <img
            :src="group.image"
            alt="Group Icon"
            class="w-12 h-12 rounded-xl object-cover border border-theme-border shrink-0"
          >

          <div class="flex-1 min-w-0">
            <h3 class="text-[15px] font-semibold text-theme-text truncate group-hover:text-theme-text">
              {{ group.name }}
            </h3>
            <p class="text-[13px] text-gray-500 truncate">
              Ostatnia aktywność {{ group.lastActive }}
            </p>
          </div>
        </a>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const groups = ref([
  {
    name: 'Kolegium Sędziów BOZPN',
    lastActive: '2 dni temu',
    image: 'https://ui-avatars.com/api/?name=KS&background=3F6212&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Absurdalnie Tanie Loty',
    lastActive: '37 min temu',
    image: 'https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Reprezentacja Polski Diabetyków w Futsalu',
    lastActive: 'tydzień temu',
    image: 'https://ui-avatars.com/api/?name=PL&background=DC2626&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Praca dla początkujących programistów: Staż, Junior',
    lastActive: '6 dni temu',
    image: 'https://ui-avatars.com/api/?name=IT&background=F3F4F6&color=111&size=128&font-size=0.4'
  },
  {
    name: 'Piłkarski Świat',
    lastActive: '46 min temu',
    image: 'https://ui-avatars.com/api/?name=PS&background=E5E7EB&color=111&size=128&font-size=0.4'
  },
  {
    name: 'Grupa Piłkarzyki',
    lastActive: '22 godz. temu',
    image: 'https://ui-avatars.com/api/?name=GP&background=1F2937&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'BILETY // POLSKA SIATKÓWKA // INFORMACJE',
    lastActive: '5 dni temu',
    image: 'https://ui-avatars.com/api/?name=VP&background=1E3A8A&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'EA SPORTS FC 26 - POLSKA',
    lastActive: '5 min temu',
    image: 'https://ui-avatars.com/api/?name=EA&background=450a0a&color=fff&size=128&font-size=0.4'
  },
]);
</script>
