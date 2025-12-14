<template>
  <div class="min-h-screen bg-[#f0f2f5] flex flex-col font-sans items-center">

    <div class="pt-20 pb-6">
      <h1 class="text-[#1877f2] text-6xl font-bold tracking-tighter cursor-pointer">
        facebook
      </h1>
    </div>

    <div class="bg-white rounded-lg shadow-xl w-[396px] p-4 text-center">

      <div class="flex justify-center mb-3 mt-2">
        <img
          src="https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
          alt="Profile"
          class="w-[100px] h-[100px] rounded-full object-cover border border-gray-200"
        />
      </div>

      <h2 class="text-[18px] text-[#1c1e21] font-medium mb-1">
        Log in as Bartosz Miazek
      </h2>

      <div class="mb-5">
        <a href="#" class="text-[#1877f2] text-[14px] hover:underline cursor-pointer">
          Not you?
        </a>
      </div>

      <form @submit.prevent="handleLogin" class="flex flex-col gap-3.5">
        <div class="relative group">
          <input
            v-model="password"
            type="password"
            placeholder="Password"
            class="w-full h-[52px] px-4 text-[17px] border border-[#dddfe2] rounded-md focus:outline-none focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2] placeholder-gray-500"
          />
        </div>

        <button
          type="submit"
          class="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white text-[20px] font-bold py-2.5 rounded-md transition-colors leading-[48px] px-4 shadow-sm"
        >
          Log in
        </button>
      </form>

      <div class="mt-4 mb-5">
        <a href="#" class="text-[#1877f2] text-[14px] hover:underline font-medium">
          Forgotten account?
        </a>
      </div>

      <div class="flex items-center justify-between my-2">
        <div class="h-[1px] bg-[#dadde1] w-full"></div>
        <span class="px-3 text-[#96999e] text-[13px]">or</span>
        <div class="h-[1px] bg-[#dadde1] w-full"></div>
      </div>

      <div class="flex justify-center pt-2 pb-2">
        <button
          type="button"
          class="bg-[#42b72a] hover:bg-[#36a420] text-white text-[17px] font-bold py-2 px-4 rounded-md transition-colors leading-[48px] h-[48px]"
        >
          Create new account
        </button>
      </div>

    </div>

    <footer class="bg-white w-full text-[#737373] text-xs pt-5 pb-5 mt-auto">
      <div class="max-w-[980px] mx-auto px-4">
        <div class="flex flex-wrap gap-x-2.5 gap-y-1 mb-2 items-center justify-center md:justify-start">
          <span v-for="(lang, index) in languages" :key="index">
            <a href="#" class="hover:underline" :class="{'text-[#737373]': index === 0, 'text-[#8a8d91]': index !== 0}">{{ lang }}</a>
          </span>
          <button class="bg-[#f5f6f7] border border-[#ccd0d5] px-2 hover:bg-[#ebedf0]">+</button>
        </div>

        <div class="border-b border-[#dddfe2] my-2"></div>

        <div class="flex flex-wrap gap-x-4 gap-y-1 mb-4 text-[#8a8d91] justify-center md:justify-start">
          <a href="#" v-for="(link, index) in footerLinks" :key="index" class="hover:underline">
            {{ link }}
          </a>
        </div>

        <div class="text-[#737373] text-center md:text-left">
          Meta © 2025
        </div>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// State
const password = ref('');

// Handlers
const handleLogin = () => {
  if (!password.value) {
    alert('Please enter your password');
    return;
  }
  console.log('Logging in user Bartosz with password:', password.value);
};

// Footer Data (Konsystentne z poprzednimi ekranami)
const languages = [
  'English (UK)', 'Polski', 'ślůnsko godka', 'Русский', 'Deutsch',
  'Français (France)', 'Italiano', 'Українська', 'Español (España)', 'Português (Brasil)', 'العربية'
];

const footerLinks = [
  'Sign Up', 'Log in', 'Messenger', 'Facebook Lite', 'Video', 'Places', 'Games',
  'Marketplace', 'Meta Pay', 'Meta Store', 'Meta Quest', 'Ray-Ban Meta', 'Meta AI',
  'Instagram', 'Threads', 'Fundraisers', 'Services', 'Voting Information Centre',
  'Privacy Policy', 'Privacy Centre', 'Groups', 'About', 'Create ad', 'Create Page',
  'Developers', 'Careers', 'Cookies', 'AdChoices', 'Terms', 'Help', 'Contact uploading and non-users'
];
</script>

<style scoped>
/* Dodatkowe style nie są wymagane, Tailwind pokrywa całość */
</style>
