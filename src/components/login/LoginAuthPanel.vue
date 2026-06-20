<template>
  <section class="border-t lg:border-t-0 lg:border-l-2 border-[#dfe2e5] flex flex-col justify-center items-center p-6 lg:p-10 bg-white relative">

    <div v-if="!showLoginForm" class="w-full max-w-[380px] flex flex-col mt-8 lg:mt-0">
      <div class="flex items-center justify-between w-full mb-8">
        <h2 class="text-[22px] font-medium text-slate-900 tracking-tight">Zaloguj się do Facebooka</h2>
        <button @click="showSettingsModal = true" class="text-black hover:bg-slate-100 p-2 rounded-full transition-colors" aria-label="Ustawienia">
          <CogIcon :size="24" />
        </button>
      </div>

      <div class="flex flex-col gap-1 mb-8">
        <button
          v-for="profile in profiles"
          :key="profile.id"
          class="flex items-center justify-between w-full p-2.5 hover:bg-slate-50 rounded-lg transition-colors group"
        >
          <div class="flex items-center gap-4">
            <img :src="profile.avatar" :alt="profile.name" class="w-12 h-12 rounded-full object-cover border border-slate-200" />
            <span class="text-[15px] font-medium text-slate-900">{{ profile.name }}</span>
          </div>
          <ChevronRightIcon :size="24" class="text-slate-400 group-hover:text-slate-600 transition-colors" />
        </button>
      </div>

      <div class="w-full flex flex-col gap-4">
        <button @click="showLoginForm = true" class="w-full bg-white hover:bg-slate-50 text-slate-800 font-medium py-2.5 px-4 border border-slate-300 rounded-full transition-colors text-[14px]">
          Użyj innego profilu
        </button>

        <RouterLink to="/register" class="w-full block">
          <button class="w-full bg-white hover:bg-slate-50 text-[#1877F2] font-medium py-2.5 px-4 border border-[#1877F2] rounded-full transition-colors text-[14px]">
            Utwórz nowe konto
          </button>
        </RouterLink>
      </div>

      <div class="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
        <MetaIcon :size="20" class="text-[#1877F2] mr-1" />
        <span>Meta</span>
      </div>
    </div>

    <div v-else class="w-full max-w-[380px] flex flex-col mt-8 lg:mt-0">
      <div class="flex items-center w-full mb-8 relative">
        <button @click="showLoginForm = false" class="absolute -left-3 text-black hover:bg-slate-100 p-2 rounded-full transition-colors">
          <ChevronLeftIcon :size="32" />
        </button>
        <h2 class="text-[22px] font-medium text-slate-900 ml-12 tracking-tight">Zaloguj się do Facebooka</h2>
      </div>

      <form @submit.prevent="handleLogin" class="w-full flex flex-col space-y-4">

        <div v-if="errorMessage" class="p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-200 text-left">
          {{ errorMessage }}
        </div>

        <CustomInput
          ref="emailInput"
          id="email"
          type="text"
          label="Adres e-mail lub numer telefonu komórkowego"
          v-model="email"
          :disable-focus-color="true"
        />

        <CustomInput
          id="password"
          type="password"
          label="Hasło"
          v-model="password"
          :disable-focus-color="true"
        />

        <AuthPrimaryButton type="submit" :disabled="isLoading">
          {{ isLoading ? 'Logowanie...' : 'Zaloguj się' }}
        </AuthPrimaryButton>
      </form>

      <div class="w-full mt-5 text-center">
        <a href="#" class="text-[15px] text-[#1877F2] font-medium hover:underline">Nie pamiętasz hasła?</a>
      </div>

      <div class="w-full mt-10">
        <RouterLink to="/register" class="w-full block">
          <button class="w-full bg-white hover:bg-slate-50 text-[#1877F2] font-medium py-2.5 px-4 border border-[#1877F2] rounded-full transition-colors text-[14px]">Utwórz nowe konto</button>
        </RouterLink>
      </div>

      <div class="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
        <MetaIcon :size="20" class="text-[#1877F2] mr-1" />
        <span>Meta</span>
      </div>
    </div>

    <LoginSettingsModal v-model="showSettingsModal" :profiles="profiles" />
  </section>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router'; // Import routera
import MetaIcon from 'vue-material-design-icons/Infinity.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import CustomInput from '@/components/common/CustomInput.vue';
import AuthPrimaryButton from '@/components/auth/AuthPrimaryButton.vue';
import LoginSettingsModal from './LoginSettingsModal.vue';

interface UserProfile {
  id: number;
  name: string;
  avatar: string;
}

defineProps<{
  profiles: UserProfile[];
}>();

const router = useRouter(); // Inicjalizacja routera

const showLoginForm = ref(false);
const showSettingsModal = ref(false);
const email = ref('');
const password = ref('');
const emailInput = ref<InstanceType<typeof CustomInput> | null>(null);

// Stany dla formularza
const isLoading = ref(false);
const errorMessage = ref('');

// Funkcja odpowiedzialna za logowanie
const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Wprowadź e-mail/telefon oraz hasło.';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const response = await fetch('https://pantry-cozily-slander.ngrok-free.dev/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },

      body: JSON.stringify({
        emailOrPhone: email.value,
        password: password.value
      })
    });

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || 'Nieprawidłowe dane logowania.');
    }

    // Zapisujemy otrzymany token JWT (zależnie od tego, gdzie przechowujesz tokeny. Tu przykład z localStorage)
    localStorage.setItem('jwt_token', data);

    // Po udanym zalogowaniu przenosimy użytkownika na stronę główną
    router.push('/');

  } catch (error: any) {
    errorMessage.value = error.message;
  } finally {
    isLoading.value = false;
  }
};

watch(showLoginForm, (newValue) => {
  if (newValue) {
    nextTick(() => {
      emailInput.value?.inputRef?.focus();
    });
  }
});
</script>
