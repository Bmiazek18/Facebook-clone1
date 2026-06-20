<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ShieldCheckOutlineIcon from 'vue-material-design-icons/ShieldCheckOutline.vue';
import AuthPrimaryButton from '@/components/auth/AuthPrimaryButton.vue';

const email = ref('Ładowanie...');
const verificationCode = ref('');
const isSubmitting = ref(false);
const isResending = ref(false); // Nowa zmienna dla przycisku "Kod nie dotarł"
const errorMessage = ref('');
const successMessage = ref('');

onMounted(async () => {
  try {
    // Odpytujemy backend o dane rejestracji przypisane do ciasteczka visitorId
    const response = await fetch('http://localhost:8080/api/auth/verify-info', {
      method: 'GET',
      credentials: 'include' // Automatycznie wysyła ciasteczko visitorId
    });

    if (!response.ok) {
      // Jeśli backend zwróci błąd (np. brak tokenu w Redis dla tego urządzenia)
      // Przekierowujemy na stronę logowania (tutaj dodaj logikę routera w przyszłości)
      errorMessage.value = 'Sesja wygasła. Wróć do logowania.';
      return;
    }

    // Backend zwraca przypisany e-mail/telefon z Redisa
    email.value = await response.text();

  } catch (error) {
    console.error('Błąd pobierania danych:', error);
    errorMessage.value = 'Nie udało się połączyć z serwerem.';
  }
});

const handleContinue = async () => {
  if (verificationCode.value.length !== 5) {
    errorMessage.value = 'Wprowadź poprawny 5-cyfrowy kod.';
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const url = `http://localhost:8080/api/auth/verify?code=${encodeURIComponent(verificationCode.value)}`;

    const response = await fetch(url, {
      method: 'POST',
      credentials: 'include', // Nakazuje przeglądarce automatycznie dołączyć ciasteczko visitorId
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || 'Coś poszło nie tak podczas weryfikacji.');
    }

    successMessage.value = data;
    // Opcjonalnie: router.push('/dashboard') po udanej weryfikacji

  } catch (error: any) {
    errorMessage.value = error.message;
  } finally {
    isSubmitting.value = false;
  }
};

const handleResendCode = async () => {
  isResending.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    // Strzał do endpointu Spring Boota, który korzysta z Resilience4j
    const response = await fetch('http://localhost:8080/api/auth/resend-code', {
      method: 'POST',
      credentials: 'include', // Niezbędne, aby Spring odebrał ciasteczko z visitorId
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const data = await response.text();

    if (!response.ok) {
      // Łapiemy błąd np. "Kod weryfikacyjny można wysłać ponownie raz na 5 minut."
      throw new Error(data || 'Nie udało się wysłać kodu ponownie.');
    }

    // Sukces z Spring Boota
    successMessage.value = data;

  } catch (error: any) {
    // Wyświetlamy tekst błędu z Resilience4j na czerwono
    errorMessage.value = error.message;
  } finally {
    isResending.value = false;
  }
};
</script>

<template>
  <div class="flex flex-col min-h-screen bg-white font-sans text-[#1c1e21]">
    <main class="grow flex items-center justify-center px-4">
      <div class="w-full max-w-125 text-center">

        <div class="mb-4 flex justify-center text-blue-600">
          <ShieldCheckOutlineIcon :size="48" />
        </div>

        <h1 class="text-[24px] font-bold tracking-tight mb-3">
          Wprowadź kod potwierdzający
        </h1>

        <p class="text-[15px] leading-normal text-[#606770] mb-6">
          Aby potwierdzić konto, wprowadź 5-cyfrowy kod, który wysłaliśmy tutaj:
          <span class="font-semibold text-black">{{ email }}</span>.
        </p>

        <form @submit.prevent="handleContinue" class="space-y-3">

          <div v-if="errorMessage" class="p-3 text-sm text-red-600 bg-red-50 rounded-xl border border-red-200 text-left">
            {{ errorMessage }}
          </div>

          <div v-if="successMessage" class="p-3 text-sm text-green-600 bg-green-50 rounded-xl border border-green-200 text-left">
            {{ successMessage }}
          </div>

          <div>
            <input
              v-model="verificationCode"
              type="text"
              maxlength="5"
              placeholder="Kod potwierdzający"
              class="w-full px-4 py-3.5 border border-[#ccd0d5] rounded-xl text-[16px] placeholder-[#8d949e] focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition duration-150 text-center tracking-widest font-medium"
            />
          </div>

          <AuthPrimaryButton
            type="submit"
            :disabled="verificationCode.length !== 5 || isSubmitting"
          >
            {{ isSubmitting ? 'Sprawdzanie...' : 'Kontynuuj' }}
          </AuthPrimaryButton>

          <button
            type="button"
            @click="handleResendCode"
            :disabled="isResending"
            class="w-full bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#4b4f56] font-semibold py-3 px-4 rounded-xl text-[16px] transition duration-150 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isResending ? 'Wysyłanie...' : 'Kod nie dotarł' }}
          </button>
        </form>
      </div>
    </main>

    <footer class="w-full max-w-245 mx-auto px-4 py-6 text-[12px] text-[#737373] border-t border-[#dddfe2]">
      <div class="flex flex-wrap gap-x-3 gap-y-1 mb-3">
        <span class="cursor-pointer hover:underline text-[#737373]">Polski</span>
        <a href="#" class="hover:underline text-[#385898]">English (US)</a>
        <a href="#" class="hover:underline text-[#385898]">ślōnshō gōdka</a>
        <a href="#" class="hover:underline text-[#385898]">Русский</a>
        <a href="#" class="hover:underline text-[#385898]">Deutsch</a>
        <a href="#" class="hover:underline text-[#385898]">Français (France)</a>
        <a href="#" class="hover:underline text-[#385898]">Italiano</a>
        <a href="#" class="hover:underline text-[#385898]">Więcej języków...</a>
      </div>

      <div class="border-b border-[#e5e5e5] my-2"></div>

      <div class="flex flex-wrap gap-x-4 gap-y-1 mb-4">
        <a href="#" class="hover:underline text-[#737373]">Informacje</a>
        <a href="#" class="hover:underline text-[#737373]">Zasady ochrony prywatności</a>
        <a href="#" class="hover:underline text-[#737373]">Pliki cookie</a>
        <a href="#" class="hover:underline text-[#737373]">Opcje wyświetlania reklam</a>
        <a href="#" class="hover:underline text-[#737373]">Regulamin</a>
        <a href="#" class="hover:underline text-[#737373]">Pomoc</a>
      </div>

      <div class="text-[#737373]">
        Meta © 2026
      </div>
    </footer>
  </div>
</template>
