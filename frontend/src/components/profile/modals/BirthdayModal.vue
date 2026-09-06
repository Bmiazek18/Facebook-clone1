<template>
  <div
    class="max-w-[450px] w-full bg-white rounded-2xl shadow-xl border border-gray-200 overflow-hidden relative  "
  >
    <button
      @click="$emit('close')"
      class="absolute top-4 right-4 w-9 h-9 bg-gray-200 hover:bg-gray-300 transition-colors rounded-full flex items-center justify-center text-gray-700"
      :title="$t('common.close')"
    >
      <Close :size="20" />
    </button>

    <div class="px-5 pt-12 pb-5">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-[22px] font-bold text-black leading-tight">{{ $t('auth.register.birthdate') }}</h2>
        <button class="text-blue-600 font-semibold text-[15px] hover:underline pr-8">{{ $t('common.edit') }}</button>
      </div>

      <div class="flex items-start gap-4">
        <CakeVariantOutline :size="28" class="text-black shrink-0 mt-0.5" />

        <div class="flex flex-col">
          <span class="text-[18px] font-bold text-black leading-snug">{{ $t('profile.23Lutego2005') }}</span>
          <div class="flex items-center gap-1.5 text-gray-500 mt-0.5">
            <AccountMultiple :size="16" />
            <span class="text-[14px]">{{ $t('profile.znajomiZnajomych') }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="w-full border-t border-gray-200"></div>

    <div class="px-5 py-5">
      <h3 class="text-[20px] font-bold text-black mb-5">{{ $t('profile.osobyObchodzaceUrodzinyW') }}</h3>

      <div class="flex flex-col space-y-4">
        <div
          v-for="(person, index) in birthdayList"
          :key="index"
          class="flex items-center gap-4 cursor-pointer hover:bg-gray-50 p-1 -ml-1 rounded-xl transition-colors"
        >
          <div
            class="w-12 h-12 rounded-full overflow-hidden shrink-0 flex items-center justify-center border border-gray-100"
            :class="person.bgColor || 'bg-gray-200'"
          >
            <img
              v-if="person.avatar"
              :src="person.avatar"
              :alt="person.name"
              class="w-full h-full object-cover"
            />
            <Account v-else-if="!person.bgColor" :size="32" class="text-gray-500 mt-2" />
          </div>

          <div class="flex flex-col">
            <span class="text-[16px] font-semibold text-black leading-snug">
              {{ person.name }}
            </span>
            <span class="text-[14px] text-gray-500">
              {{ person.date }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Import ikon
import Close from 'vue-material-design-icons/Close.vue'
import CakeVariantOutline from 'vue-material-design-icons/CakeVariantOutline.vue'
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue'
import Account from 'vue-material-design-icons/Account.vue'

defineEmits(['close'])

// Przykładowe dane symulujące listę z obrazka
const birthdayList = [
  {
    name: 'Andrzej Prokop',
    date: '6 lutego 1989',
    avatar: 'https://via.placeholder.com/150/0000FF/808080?Text=AP', // Zastępcze zdjęcie (błyskawice)
  },
  {
    name: 'Bartosz Paszkiewicz',
    date: '10 lutego 2001',
    avatar: null, // Wymusi domyślną szarą ikonę użytkownika
  },
  {
    name: 'Jakub Świerk',
    date: '12 lutego 2005',
    avatar: null,
    bgColor: 'bg-light-green-400', // Symulacja zielonego tła awatara
  },
  {
    name: 'Ame Dybciak',
    date: '13 lutego',
    avatar: 'https://via.placeholder.com/150/FFB6C1/000000?Text=AD',
  },
  {
    name: 'Aleksander Żmuda',
    date: '14 lutego 2005',
    avatar: 'https://via.placeholder.com/150/8B4513/FFFFFF?Text=AZ',
  },
  {
    name: 'Amelia Chrzanowska',
    date: '22 lutego 2006',
    avatar: 'https://via.placeholder.com/150/FFC0CB/000000?Text=AC',
  },
]
</script>

<style scoped>
/* Dodatkowy kolor dla "zielonego" awatara, jeśli tailwind go domyślnie nie skompiluje z dynamicznej zmiennej */
.bg-light-green-400 {
  background-color: #90ee90;
}
</style>
