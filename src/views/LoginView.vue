<template>
  <div class="min-h-screen bg-[#f0f2f5] flex flex-col font-sans">

    <div class="flex-grow flex flex-col md:flex-row items-center justify-center w-full max-w-[980px] mx-auto px-4 pt-10 pb-10 md:pt-20 gap-x-0 md:gap-x-16">

      <div class="w-full md:w-[500px] mb-10 md:mb-0 flex flex-col items-center md:items-start text-center md:text-left">
        <h1 class="text-[#1877f2] text-6xl font-bold -ml-1 mb-2 tracking-tighter">facebook</h1>

        <h2 class="text-[28px] leading-8 font-normal text-[#1c1e21] mb-1">Recent logins</h2>
        <p class="text-[#606770] text-[15px] mb-5">Click your picture or add an account.</p>

        <div class="flex gap-5 flex-wrap justify-center md:justify-start">

          <div class="group relative w-[160px] cursor-pointer hover:shadow-lg transition-shadow duration-200 bg-white rounded-lg overflow-hidden border border-gray-200">
            <div class="absolute top-0 left-0 p-1">
              <div class="bg-black/50 hover:bg-black/70 rounded-full p-0.5 transition-colors cursor-pointer text-white" title="Remove account">
                <CloseIcon :size="14" />
              </div>
            </div>

            <div class="absolute top-1 right-1 bg-red-500 text-white text-xs font-bold px-1.5 py-0.5 rounded-full border-2 border-white">
              2
            </div>

            <div class="h-[160px] bg-gray-300 overflow-hidden">
               <img
                src="https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80"
                alt="Bartosz"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <div class="py-3 text-center bg-white">
              <span class="text-[#4b4f56] text-lg leading-5 block group-hover:text-[#1c1e21]">Bartosz</span>
            </div>
          </div>

          <div class="w-[160px] cursor-pointer hover:shadow-lg transition-shadow duration-200 bg-white rounded-lg overflow-hidden border border-gray-200 flex flex-col">
            <div class="h-[160px] bg-[#f5f6f7] flex items-center justify-center text-[#1877f2]">
              <div class="bg-[#1877f2] rounded-full p-2 text-white">
                <PlusIcon :size="24" />
              </div>
            </div>
            <div class="py-3 text-center bg-white text-[#1877f2]">
              <span class="text-md font-medium">Add Account</span>
            </div>
          </div>

        </div>
      </div>

      <div class="flex flex-col items-center">
        <div class="bg-white p-4 rounded-lg shadow-xl w-[396px] text-center">
          <form @submit.prevent="handleLogin" class="flex flex-col gap-3.5">
            <div>
              <input
                v-model="email"
                type="text"
                placeholder="Email address or phone number"
                class="w-full h-[52px] px-4 text-[17px] border border-[#dddfe2] rounded-md focus:outline-none focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2] placeholder-gray-500"
              />
            </div>
            <div>
              <input
                v-model="password"
                type="password"
                placeholder="Password"
                class="w-full h-[52px] px-4 text-[17px] border border-[#dddfe2] rounded-md focus:outline-none focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2] placeholder-gray-500"
              />
            </div>

            <button
              type="submit"
              class="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white text-[20px] font-bold py-2.5 rounded-md transition-colors leading-[48px] px-4"
            >
              Log in
            </button>

            <a href="#" class="text-[#1877f2] text-[14px] hover:underline mt-1 font-medium">
              Forgotten password?
            </a>

            <div class="border-b border-[#dadde1] my-2"></div>

            <div class="flex justify-center pt-2 pb-2">
              <button
                type="button"
                class="bg-[#42b72a] hover:bg-[#36a420] text-white text-[17px] font-bold py-2 px-4 rounded-md transition-colors leading-[48px] h-[48px]"
              >
                Create new account
              </button>
            </div>
          </form>
        </div>

        <div class="mt-7 text-[14px]">
          <a href="#" class="font-bold text-black hover:underline">Create a Page</a> for a celebrity, brand or business.
        </div>
      </div>
    </div>

    <footer class="bg-white w-full text-[#737373] text-xs pt-5 pb-5">
      <div class="max-w-[980px] mx-auto px-4">
        <div class="flex flex-wrap gap-x-2.5 gap-y-1 mb-2 items-center">
          <span v-for="(lang, index) in languages" :key="index">
            <a href="#" class="hover:underline" :class="{'text-[#737373]': index === 0, 'text-[#8a8d91]': index !== 0}">{{ lang }}</a>
          </span>
          <button class="bg-[#f5f6f7] border border-[#ccd0d5] px-2 hover:bg-[#ebedf0]">+</button>
        </div>

        <div class="border-b border-[#dddfe2] my-2"></div>

        <div class="flex flex-wrap gap-x-4 gap-y-1 mb-4 text-[#8a8d91]">
          <a href="#" v-for="(link, index) in footerLinks" :key="index" class="hover:underline">
            {{ link }}
          </a>
        </div>

        <div class="text-[#737373]">
          Meta © 2025
        </div>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
// Upewnij się, że masz zainstalowane: npm install vue-material-design-icons
import CloseIcon from 'vue-material-design-icons/Close.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';

// State
const email = ref('');
const password = ref('');

// Data for Footer
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

// Handlers
const handleLogin = () => {
  console.log('Logging in with:', email.value, password.value);
};
</script>

<style scoped>
/* Dodatkowe style, jeśli Tailwind nie wystarczy (zazwyczaj niepotrzebne przy tej skali) */
</style>
