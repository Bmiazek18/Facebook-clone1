<template>
  <div class="min-h-screen bg-white flex flex-col font-sans">

    <main class="grow flex flex-col items-center pt-12.5 px-4">
      <div class="w-full max-w-150">

        <button class="mb-6 -ml-2 p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
          </svg>
        </button>

        <h1 class="text-[28px] font-semibold text-[#1c1e21] mb-2">
          Znajdź swoje konto
        </h1>
        <p class="text-[17px] text-[#1c1e21] mb-6">
          Wprowadź numer telefonu komórkowego lub adres e-mail.
        </p>

        <div class="mb-4">
          <CustomInput
            id="recovery-input"
            v-model="searchQuery"
            label="Numer telefonu komórkowego lub adres e-mail"
            variant="new"
          />
        </div>

        <AuthPrimaryButton type="button" @click="handleContinue">Kontynuuj</AuthPrimaryButton>

      </div>
    </main>

    <footer class="w-full max-w-250 mx-auto pb-10 pt-4 px-4 text-[#737373] text-[13px] border-t border-[#ccd0d5]">

      <ul class="flex flex-wrap gap-x-4 gap-y-2 mb-3">
        <li><a href="#" class="hover:underline">Polski</a></li>
        <li><a href="#" class="hover:underline">English (US)</a></li>
        <li><a href="#" class="hover:underline">ślōnskŏ gōdka</a></li>
        <li><a href="#" class="hover:underline">Русский</a></li>
        <li><a href="#" class="hover:underline">Deutsch</a></li>
        <li><a href="#" class="hover:underline">Français (France)</a></li>
        <li><a href="#" class="hover:underline">Italiano</a></li>
        <li><a href="#" class="hover:underline font-semibold bg-[#f5f6f7] px-2 py-1 rounded border border-[#ccd0d5]">Więcej języków...</a></li>
      </ul>

      <div class="w-full h-px bg-[#ccd0d5] my-3"></div>

      <ul class="flex flex-wrap gap-x-4 gap-y-2 mb-4">
        <li><a href="#" class="hover:underline font-semibold">Zarejestruj się</a></li>
        <li><a href="#" class="hover:underline font-semibold">Zaloguj się</a></li>
        <li><a href="#" class="hover:underline font-semibold">Messenger</a></li>
        <li><a href="#" class="hover:underline font-semibold">Facebook Lite</a></li>
        <li><a href="#" class="hover:underline font-semibold">Film</a></li>
        <li><a href="#" class="hover:underline font-semibold">Meta Pay</a></li>
        <li><a href="#" class="hover:underline font-semibold">Sklep Meta</a></li>
        <li><a href="#" class="hover:underline font-semibold">Meta Quest</a></li>
        <li><a href="#" class="hover:underline font-semibold">Ray-Ban Meta</a></li>
        <li><a href="#" class="hover:underline font-semibold">Meta AI</a></li>
        <li><a href="#" class="hover:underline font-semibold">Instagram</a></li>
        <li><a href="#" class="hover:underline font-semibold">Threads</a></li>
        <li><a href="#" class="hover:underline font-semibold">Zasady ochrony prywatności</a></li>
        <li><a href="#" class="hover:underline font-semibold">Centrum ochrony prywatności</a></li>
        <li><a href="#" class="hover:underline font-semibold">Ustawienia plików cookie</a></li>
        <li><a href="#" class="hover:underline font-semibold">Informacje</a></li>
        <li><a href="#" class="hover:underline font-semibold">Utwórz reklamę</a></li>
        <li><a href="#" class="hover:underline font-semibold">Utwórz stronę</a></li>
        <li><a href="#" class="hover:underline font-semibold">Twórcy aplikacji</a></li>
        <li><a href="#" class="hover:underline font-semibold">Praca</a></li>
        <li><a href="#" class="hover:underline font-semibold">Pliki cookie</a></li>
        <li><a href="#" class="hover:underline font-semibold">Opcje wyświetlania reklam</a></li>
        <li><a href="#" class="hover:underline font-semibold">Regulamin</a></li>
        <li><a href="#" class="hover:underline font-semibold">Pomoc</a></li>
        <li><a href="#" class="hover:underline font-semibold">Przesyłanie listy kontaktów i osób niebędących użytkownikami</a></li>
      </ul>

      <div class="mt-4">
        Meta © 2026
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import CustomInput from '@/components/common/CustomInput.vue'; // Upewnij się, że ścieżka jest poprawna
import AuthPrimaryButton from '@/components/auth/AuthPrimaryButton.vue';

const searchQuery = ref('');

const handleContinue = () => {
  // Tutaj możesz dodać logikę walidacji i wysyłania formularza
  console.log('Szukam konta dla:', searchQuery.value);
};
</script>
