<script setup lang="ts">
import BriefcaseIcon from 'vue-material-design-icons/Briefcase.vue'
import SchoolIcon from 'vue-material-design-icons/School.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
</script>

<template>
  <VMenu placement="top-start">
    <div class="font-extrabold text-[15px] text-theme-text">Bartosz Miazek</div>

    <template #popper>
      <div class="bg-theme-bg-secondary rounded-lg shadow-xl max-w-sm mx-auto overflow-hidden">

        <div class="flex justify-end p-2 pb-0">
          <button
            class="text-gray-500 hover:text-gray-700 p-1 transition focus:outline-none"
            aria-label="Zamknij kartę"
          >
            <CloseIcon :size="24" class="h-6 w-6" />
          </button>
        </div>

        <div class="px-4 pb-4 pt-0">
          <div class="flex items-start space-x-4">

            <div class="flex-shrink-0">
              <img
                src="https://picsum.photos/id/400/80/80"
                alt="Zdjęcie profilowe"
                class="h-20 w-20 rounded-full object-cover  -mt-7"
              />
            </div>

            <div class="flex-grow">
              <h2 class="text-xl font-bold text-theme-text">Anna Kowalska</h2>

              <div class="mt-3 flex items-start space-x-3">
                <span class="text-theme-text pt-0.5 flex-shrink-0">
                  <BriefcaseIcon :size="20" class="h-5 w-5" />
                </span>
                <p class="text-theme-text text-sm leading-snug">
                  <span class="font-semibold">Biegły Rewident</span> i stanowisko w:
                  <span class="font-normal">Audyt Finansowy, Sprawozdania i Zrównoważony Rozwój</span>
                </p>
              </div>

              <div class="mt-4 flex items-start space-x-3">
                <span class="text-theme-text pt-0.5 flex-shrink-0">
                  <SchoolIcon :size="20" class="h-5 w-5" />
                </span>
                <p class="text-theme-text text-sm leading-snug">
                  Studiowała "Rachunkowość i Kontrola Finansowa" na:
                  <span class="font-semibold">Uniwersytet Ekonomiczny w Krakowie</span>
                </p>
              </div>
            </div>
          </div>

          <div class="mt-6 border-t border-gray-200 pt-4 flex justify-between space-x-2 font-[15px]">

            <button
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 text-sm font-semibold rounded-md bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              <SendIcon :size="20" />
              <span>Wyślij wiadomość</span>
            </button>

            <button
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition"
            >
              <AccountPlusIcon :size="20" />
              <span>Dodaj znajomego</span>
            </button>

            <button
              class="p-2 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition"
            >
              <DotsHorizontalIcon :size="20" />
            </button>
          </div>

        </div>
      </div>
    </template>
  </VMenu>
</template>
