<template>
  <aside class="w-[360px] flex flex-col bg-theme-bg-secondary border-r border-theme-border sticky top-14 h-[calc(100vh-56px)] overflow-y-auto z-20">
    <div class="p-4">
      <h1 class="text-[24px] font-bold mb-4 px-2">Grupy</h1>

      <div class="relative mb-6 px-2">
        <magnify-icon class="absolute left-5 top-2.5 text-theme-text-secondary" :size="20" />
        <input
          type="text"
          placeholder="Wyszukaj grupy"
          class="w-full bg-theme-bg-subtle rounded-full py-2.5 pl-11 pr-4 placeholder-theme-text-secondary focus:outline-none text-[15px]"
        />
      </div>

      <nav class="space-y-1">
        <a href="#" class="flex items-center gap-3 bg-theme-bg-subtle px-3 py-2 rounded-lg transition group">
          <div class="bg-theme-primary p-1.5 rounded-full text-white">
            <home-icon :size="20" />
          </div>
          <span class="font-semibold text-[15px]">Strona główna</span>
        </a>
        <a href="#" class="flex items-center gap-3 hover:bg-theme-hover px-3 py-2 rounded-lg transition group">
          <div class="bg-theme-bg-subtle group-hover:bg-theme-hover-strong p-1.5 rounded-full">
            <account-group-icon :size="20" />
          </div>
          <span class="font-medium text-[15px]">Twoje grupy</span>
          <chevron-down-icon class="ml-auto text-theme-text-secondary" :size="20" />
        </a>
        <a href="#" class="flex items-center gap-3 hover:bg-theme-hover px-3 py-2 rounded-lg transition group">
          <div class="bg-theme-bg-subtle group-hover:bg-theme-hover-strong p-1.5 rounded-full">
            <bell-icon :size="20" />
          </div>
          <span class="font-medium text-[15px]">Powiadomienia</span>
        </a>
      </nav>


      <router-link to="/add-group" class="mt-4 w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]">
        <plus-icon :size="20" /> Utwórz nową grupę
      </router-link>

    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import HomeIcon from 'vue-material-design-icons/Home.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
</script>
