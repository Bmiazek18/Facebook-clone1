<script setup>
import { ref } from 'vue'

// Importy ikon
import ShieldStarIcon from 'vue-material-design-icons/ShieldStar.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

// Dane ról (id musi odpowiadać temu, co chcesz mieć w URL)
const roles = ref([
  {
    id: 'admin',
    name: 'Administrator',
    memberCount: '1 członek'
  },
  {
    id: 'moderator',
    name: 'Moderator',
    memberCount: '0 członków'
  }
])
const route = useRoute() // Nuxt 3 auto-importuje useRoute()
</script>

<template>
  <!-- Główne tło strony -->
  <div class="min-h-screen bg-[#18191a] text-[#e4e6eb] font-sans p-6 sm:p-10 flex flex-col items-center selection:bg-blue-600">

    <!-- Kontener zawartości -->
    <div class="w-full max-w-3xl">

      <!-- Główny nagłówek strony -->
      <h1 class="text-2xl font-bold mb-6 px-2">{{ $t('groups.roleSpolecznosciowe') }}</h1>

      <!-- Karta: Standardowe role -->
      <div class="bg-[#242526] rounded-xl p-4 shadow-sm border border-[#3e4042]">

        <!-- Nagłówek i opis wewnątrz karty -->
        <div class="mb-3 px-2">
          <h2 class="text-[17px] font-bold leading-snug">{{ $t('groups.standardoweRole') }}</h2>
          <p class="text-[15px] text-[#b0b3b8] mt-0.5">{{ $t('groups.majaDomyslnieUstawioneObowiazki') }}</p>
        </div>

        <!-- Lista ról -->
        <div class="space-y-1 mt-4">
          <!-- Zmiana z <button> na <NuxtLink> -->
          <NuxtLink
            v-for="role in roles"
            :key="role.id"

            :to="`${route.path}/${role.id}`"
            class="w-full flex items-center justify-between p-2 rounded-lg hover:bg-[#3a3b3c] transition-colors cursor-pointer group text-left block"
          >
            <!-- Lewa strona: Ikona i Tekst -->
            <div class="flex items-center gap-3">
              <!-- Okrągłe tło ikony -->
              <div class="w-10 h-10 rounded-full bg-[#3a3b3c] group-hover:bg-[#4e4f50] transition-colors flex items-center justify-center shrink-0 text-[#e4e6eb]">
                <ShieldStarIcon :size="20" />
              </div>

              <!-- Informacje o roli -->
              <div class="flex flex-col">
                <span class="text-[17px] font-medium leading-tight text-[#e4e6eb]">
                  {{ role.name }}
                </span>
                <span class="text-[13px] text-[#b0b3b8] mt-0.5">
                  {{ role.memberCount }}
                </span>
              </div>
            </div>

            <!-- Prawa strona: Strzałka -->
            <div class="pr-2">
              <ChevronRightIcon :size="24" class="text-[#b0b3b8]" />
            </div>
          </NuxtLink>
        </div>

      </div>

    </div>
  </div>
</template>
