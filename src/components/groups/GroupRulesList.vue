<script setup>
import { ref } from 'vue'

// Lista zasad wczytywana do widoku
const rules = ref([
  {
    title: "Obrażanie innych członków grupy:",
    description: "Zachowania, które mają na celu wywołanie kłótni, bądź obrażanie innych członków grupy karane będą banem z możliwością odwołania."
  },
  {
    title: "Post niezwiązany z tematyką grupy:",
    description: 'Posty typu "co do zmiany", bądź zupełnie nie związane z tematyką grupy będą usuwane, a w szczególnych przypadkach banowane.'
  },
  {
    title: "Promowanie się na grupie:",
    description: "Promowanie na grupie jest kategorycznie zakazane - jakiekolwiek promo wstawione bez uzgodnienia z administratorem skutkować będzie banem permanentnym."
  },
  {
    title: "Fake konta na grupie:",
    description: "Jeżeli posiadasz fake konto na grupie, to musisz liczyć się z tym, że administrator ma prawo je zablokować."
  },
  {
    title: "Łamanie polityki facebooka:",
    description: "Wstawianie zdjęć o treści pornograficznej, obrażanie innych wyznań, bądź narodowości, a także innych zachowań zabronionych przez Facebooka jest kategorycznie zakazane I będzie karane banem."
  },
  {
    title: "Tworzenie fake newsów:",
    description: "Tworzenie postów z fałszywymi informacjami nt. transferów, stanu zdrowia zawodników etc. będą karane banem."
  },
  {
    title: "Beznadziejny post:",
    description: "Jeśli spamujesz postami, nie mają one żadnego sensu bądź są wymuszeniem reakcji i komentarzy to musisz liczyć się z tym, że możesz otrzymać bana."
  }
])
</script>

<template>
  <div class=" bg-transparent flex justify-center items-start">

    <!-- Karta główna widżetu -->
    <div class="w-full  bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm p-4">

      <!-- Nagłówek -->
      <h2 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-4">
        Zasady grupy ustalone przez administratorów
      </h2>

      <!-- Linia oddzielająca -->
      <hr class="border-[#CED0D4] dark:border-[#3E4042] mb-5" />

      <!-- Lista zasad z v-for -->
      <div class="flex flex-col space-y-5">
        <div
          v-for="(rule, index) in rules"
          :key="index"
          class="flex items-start gap-4"
        >
          <!-- Numer zasady -->
          <div class="w-4 text-[15px] font-semibold text-[#65676B] dark:text-[#B0B3B8] pt-0.5">
            {{ index + 1 }}
          </div>

          <!-- Zawartość zasady -->
          <div class="flex-1 flex flex-col">

            <!-- Tytuł i strzałka rozwijania -->
            <div class="flex justify-between items-start mb-1.5">
              <h3 class="text-[15px] font-semibold text-[#050505] dark:text-[#E4E6EB] leading-snug">
                {{ rule.title }}
              </h3>
              <button class="text-[#65676B] dark:text-[#B0B3B8] hover:bg-gray-100 dark:hover:bg-[#3A3B3C] p-1 rounded-full transition-colors -mt-1 ml-2 flex-shrink-0">
                <svg class="w-6 h-6 fill-currentColor" viewBox="0 0 24 24">
                  <path d="M18.53 15.53a.75.75 0 0 1-1.06 0L12 10.06l-5.47 5.47a.75.75 0 0 1-1.06-1.06l6-6a.75.75 0 0 1 1.06 0l6 6a.75.75 0 0 1 0 1.06z"></path>
                </svg>
              </button>
            </div>

            <!-- Opis i opcje -->
            <div class="flex justify-between items-end">
              <p class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-relaxed pr-4">
                {{ rule.description }}
              </p>
              <button class="text-[#65676B] dark:text-[#B0B3B8] hover:bg-gray-100 dark:hover:bg-[#3A3B3C] p-1 rounded-full transition-colors mb-1 ml-2 flex-shrink-0">
                <svg class="w-5 h-5 fill-currentColor" viewBox="0 0 24 24">
                  <path d="M6 12a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm8 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm8 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0z"></path>
                </svg>
              </button>
            </div>

          </div>
        </div>
      </div>

    </div>
  </div>
</template>
