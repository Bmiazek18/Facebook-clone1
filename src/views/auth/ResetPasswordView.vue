<template>
  <div class="min-h-screen bg-white font-sans text-[#1c1e21] flex flex-col">


    <div class="flex-1 flex justify-center pt-8 px-4">
      <div class="w-full max-w-[500px]">

        <div class="mb-5">
          <button
            type="button"
            @click="goBack"
            class="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors focus:outline-none mb-2 -ml-2"
          >
            <ChevronLeftIcon :size="28" />
          </button>
          <h1 class="text-[24px] font-bold">Wybierz sposób logowania</h1>
        </div>

        <div class="border border-[#ced0d4] rounded-xl p-4 flex items-center mb-6">
          <div class="relative mr-4 shrink-0">
            <img
              src="https://i.pravatar.cc/150?img=11"
              alt="Avatar"
              class="w-14 h-14 rounded-full object-cover"
            />
            <div class="absolute -bottom-1 -right-1 bg-white rounded-full p-[2px]">
              <div class="bg-[#1877f2] text-white rounded-full w-5 h-5 flex items-center justify-center">
                <FacebookIcon :size="14" />
              </div>
            </div>
          </div>
          <div>
            <div class="font-semibold text-[17px] leading-tight mb-0.5">Bartosz Miazek</div>
            <div class="text-[#606770] text-[15px] leading-tight">Facebook</div>
          </div>
        </div>

        <div class="border border-[#ced0d4] rounded-xl mb-6 overflow-hidden">

          <label class="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 transition-colors">
            <div>
              <div class="font-medium text-[16px]">Uzyskaj kod przez SMS</div>
              <div class="text-[#606770] text-[14px] mt-0.5">+48517419808</div>
            </div>
            <div
              class="w-6 h-6 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors"
              :class="selectedMethod === 'sms' ? 'border-[#1877f2]' : 'border-[#8d949e]'"
            >
              <div v-if="selectedMethod === 'sms'" class="w-3 h-3 bg-[#1877f2] rounded-full"></div>
            </div>
            <input type="radio" v-model="selectedMethod" value="sms" class="hidden" />
          </label>

          <hr class="border-[#ced0d4] mx-4" />

          <label class="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 transition-colors">
            <div>
              <div class="font-medium text-[16px]">Kontynuuj przy użyciu hasła</div>
              <div class="text-[#606770] text-[14px] mt-0.5">Użyj hasła, aby kontynuować</div>
            </div>
            <div
              class="w-6 h-6 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors"
              :class="selectedMethod === 'password' ? 'border-[#1877f2]' : 'border-[#8d949e]'"
            >
              <div v-if="selectedMethod === 'password'" class="w-3 h-3 bg-[#1877f2] rounded-full"></div>
            </div>
            <input type="radio" v-model="selectedMethod" value="password" class="hidden" />
          </label>
        </div>

        <div class="text-center mb-8">
          <a href="#" class="text-[#1877f2] hover:underline font-medium text-[14px]">
            Nie masz już do nich dostępu?
          </a>
        </div>

        <div class="flex flex-col gap-3">
          <AuthPrimaryButton
            @click="onContinue"

          >
            Kontynuuj
          </AuthPrimaryButton>

          <button
            @click="onNotYou"
            class="w-full bg-white border border-[#ced0d4] text-[#1c1e21] font-semibold text-[16px] rounded-full py-3 hover:bg-gray-50 transition-colors focus:outline-none"
          >
            To nie Ty?
          </button>
        </div>

      </div>
    </div>

    <div class="mt-auto pt-10 pb-6 px-4 flex flex-col items-center">
      <div class="flex flex-wrap justify-center gap-x-4 gap-y-2 text-[13px] text-[#606770] max-w-[800px] mb-4">
        <span class="text-[#1c1e21] font-semibold">Polski</span>
        <a href="#" class="hover:underline">English (US)</a>
        <a href="#" class="hover:underline">ślōnskŏ gŏdka</a>
        <a href="#" class="hover:underline">Русский</a>
        <a href="#" class="hover:underline">Deutsch</a>
        <a href="#" class="hover:underline">Français (France)</a>
        <a href="#" class="hover:underline">Italiano</a>
        <a href="#" class="hover:underline font-semibold">Więcej języków...</a>
      </div>
      <div class="w-full max-w-[800px] border-t border-gray-200 pt-4 flex flex-wrap justify-center gap-x-4 gap-y-2 text-[12px] text-[#606770]">
        <a href="#" class="hover:underline">Zarejestruj się</a>
        <a href="#" class="hover:underline">Zaloguj się</a>
        <a href="#" class="hover:underline">Messenger</a>
        <a href="#" class="hover:underline">Facebook Lite</a>
        <a href="#" class="hover:underline">Film</a>
        <a href="#" class="hover:underline">Meta Pay</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AuthPrimaryButton from '@/components/auth/AuthPrimaryButton.vue';
import { ref } from 'vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import FacebookIcon from 'vue-material-design-icons/Facebook.vue';
// Jeśli używasz vue-router:
// import { useRouter } from 'vue-router';

// Stan dla wybranej metody logowania (domyślnie wybrany SMS wg screena)
const selectedMethod = ref<'sms' | 'password'>('sms');

// const router = useRouter();

const goBack = () => {
  // router.back();
  console.log('Powrót');
};

const onContinue = () => {
  console.log('Wybrano metodę:', selectedMethod.value);
};

const onNotYou = () => {
  console.log('To nie ja - akcja');
};
</script>
