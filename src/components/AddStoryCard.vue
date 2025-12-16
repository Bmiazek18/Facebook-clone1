<template>
  <div class="
     w-[113px] h-[200px] mt-3 rounded-xl shadow-sm overflow-hidden flex flex-col relative group transition duration-300 ease-in-out cursor-pointer flex-shrink-0
    ">

    <div class="absolute inset-x-0 top-0 h-[70%] z-0">
      <img
        src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=dst-jpg_s320x320_tt6&amp;_nc_cat=104&amp;ccb=1-7&amp;_nc_sid=e99d92&amp;_nc_ohc=HjlBiaeY_e0Q7kNvwEN3-iG&amp;_nc_oc=AdnWBetTrKBcEcnqPCk9tA9-PRqtuwgxGOfQFcyXlxTP07KnY41OKQ00nb6SmFwqeF1Mk2Gq_RhtH3HLWf9qFDxD&amp;_nc_zt=24&amp;_nc_ht=scontent-waw2-1.xx&amp;_nc_gid=ptyBC2FNhs8UFm_JvSpjUw&amp;oh=00_AfhnQR53dqp1aOvigdhHIN8a2MjP5PP7mY0okqiTDnkcig&amp;oe=691C0CAC"
        alt="Osoby na stacji metra"
        class="
          w-full h-full object-cover
          transition duration-300 ease-in-out
          group-hover:scale-103
          filter brightness-90 contrast-100
          group-hover:brightness-85
          group-hover:contrast-105
        "
      >
      </div>

    <div class="absolute inset-x-0 bottom-0 h-[30%] bg-theme-bg-secondary pt-4 flex flex-col items-center justify-start z-50
    group-hover:bg-gray-200 transition-colors duration-300 ease-in-out">
      <button
        class="
          w-8
          h-8
          bg-blue-600
          text-white
          rounded-full
          flex
          items-center
          justify-center
          shadow-md

          transition-colors
          mt-[-30px]

          ring-4 ring-theme-bg-secondary
          p-0
        "
        aria-label="Utwórz relację"
        @click="createStory"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="3"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
      </button>

      <p class="text-theme-text text-center text-xs font-semibold leading-tight pt-1">
        Utwórz<br />relację
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

// Funkcja, która zostanie wywołana po kliknięciu przycisku
const createStory = () => {
  router.push('/createReel');
};
</script>
