<template>
  <div class="min-h-screen bg-white font-sans flex flex-col justify-between overflow-hidden">
    <main class="grow grid grid-cols-1 lg:grid-cols-[1fr_550px] w-full  mx-auto relative">

      <div class="absolute top-6 left-6 lg:top-10 lg:left-10 z-10">
        <svg viewBox="0 0 36 36" class="w-10 h-10 fill-[#1877F2]" xmlns="http://www.w3.org/2000/svg">
          <path d="M20.181 35.87C29.094 34.791 36 27.202 36 18c0-9.941-8.059-18-18-18S0 8.059 0 18c0 8.442 5.811 15.526 13.652 17.471L14 34v-8.954h-2.923v-4.144H14v-2.828c0-4.11 2.366-6.155 5.894-6.155 1.516 0 2.871.189 3.328.283v3.743l-2.079.001c-1.895 0-2.391 1.054-2.391 2.314v2.641h4.298l-.666 4.144h-3.632v9.825z"/>
        </svg>
      </div>

      <div class="flex flex-row h-full relative p-8 lg:p-12 lg:pl-20">


        <div class="mt-auto mb-8">
          <h1 class="text-4xl lg:text-[52px] font-bold text-slate-900 tracking-tight leading-[1.1]">
            Przeglądaj,<br />to, co <span class="text-[#1877F2]">lubisz</span>.
          </h1>
        </div>
        <div class="flex-grow flex items-center justify-center mt-12">
          <img :src="logoUrl" alt="Hero" class="h-[900px] object-contain" />
        </div>
      </div>

      <LoginAuthPanel :profiles="profiles" />

    </main>

    <footer class="bg-white border-t-2 border-[#dfe2e5] py-6 px-4 text-center text-[12px] text-slate-500 w-full shrink-0">
      <div class="max-w-5xl mx-auto space-y-3">
        <div class="flex flex-wrap justify-center gap-x-4 gap-y-1 text-slate-600">
          <a href="#" class="hover:underline font-medium">Polski</a>
          <a href="#" class="hover:underline">English (US)</a>
          <a href="#" class="hover:underline">Ślōnski godka</a>
          <a href="#" class="hover:underline">Русский</a>
          <a href="#" class="hover:underline">Deutsch</a>
          <a href="#" class="hover:underline">Français (France)</a>
          <a href="#" class="hover:underline">Italiano</a>
          <a href="#" class="hover:underline text-[#1877F2]">Więcej języków...</a>
        </div>
        <hr class="border-[#dfe2e5] max-w-4xl mx-auto my-3" />
        <div class="flex flex-wrap justify-center gap-x-4 gap-y-2 text-[11px]">
          <a href="#" class="hover:underline">Zarejestruj się</a>
          <a href="#" class="hover:underline">Zaloguj się</a>
          <a href="#" class="hover:underline">Messenger</a>
          <a href="#" class="hover:underline">Facebook Lite</a>
          <a href="#" class="hover:underline">Film</a>
          <a href="#" class="hover:underline">Meta Pay</a>
          <a href="#" class="hover:underline">Sklep Meta</a>
          <a href="#" class="hover:underline">Meta Quest</a>
          <a href="#" class="hover:underline">Ray-Ban Meta</a>
          <a href="#" class="hover:underline">Meta AI</a>
          <a href="#" class="hover:underline">Instagram</a>
          <a href="#" class="hover:underline">Threads</a>
        </div>
        <div class="text-slate-400 pt-2 text-[11px]">Meta © 2026</div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import LoginAuthPanel from '@/components/login/LoginAuthPanel.vue';
import logoUrl from '@/assets/images/HeroImage.webp';

interface UserProfile {
  id: number;
  name: string;
  avatar: string;
}

// Symulacja dwóch profili w cookies
const profiles = ref<UserProfile[]>([
  {
    id: 1,
    name: 'Bartosz Miazek',
    avatar: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=100&auto=format&fit=crop&q=60'
  },
  {
    id: 2,
    name: 'Test Testowy',
    // Możesz wrzucić tu standardowy awatar jako domyślny fallback
    avatar: 'https://images.unsplash.com/photo-1640951613773-54706e06851d?w=100&auto=format&fit=crop&q=60'
  }
]);
</script>
