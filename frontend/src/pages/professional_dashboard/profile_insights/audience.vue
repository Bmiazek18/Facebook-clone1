<template>
  <div class="min-h-screen bg-[#F0F2F5] p-6 font-sans">
    <div class="max-w-7xl mx-auto space-y-4">

      <!-- WIERZCH 1 i 2 (Siatka 3-kolumnowa) -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">

        <!-- Karta 1.1: Liczba obserwatorów netto -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[220px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 leading-snug">Liczba obserwatorów netto według typu zawartości</h2>
            <p class="text-[13px] text-gray-500 mt-1">Ostatnie 28 dni</p>
          </div>
          <div class="flex-1 flex flex-col items-center justify-center mt-3">
            <span class="text-[28px] font-bold text-gray-900">{{ insights?.netFollowers ?? 0 }}</span>
            <span class="text-[13px] text-green-600 font-medium mt-0.5">+{{ insights?.followersGrowthPercent ?? 0 }}% z poprzednich 28 dni</span>
          </div>
        </div>

        <!-- Karta 1.2: Jak użytkownicy znajdują materiały -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[220px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 leading-snug flex items-center gap-1">
              Jak użytkownicy znajdują Twoje materiały
              <InfoIcon :size="16" class="text-gray-400" />
            </h2>
            <div class="flex items-center gap-2 mt-3">
              <button class="px-3 py-1.5 bg-blue-50 text-blue-700 font-semibold rounded-full text-[13px]">Ruch</button>
              <button class="px-3 py-1.5 text-gray-700 font-semibold hover:bg-gray-50 rounded-full text-[13px]">Źródło</button>
            </div>
          </div>
          <div class="flex-1 flex flex-col justify-center gap-2 mt-3 text-[13px]">
            <div class="flex justify-between">
              <span class="text-gray-600">Aktualności (Feed)</span>
              <span class="font-semibold text-gray-900">72%</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Wyszukiwarka</span>
              <span class="font-semibold text-gray-900">18%</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Profil bezpośrednio</span>
              <span class="font-semibold text-gray-900">10%</span>
            </div>
          </div>
        </div>

        <!-- Karta 1.3: Odwiedziny (Wykres) -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[220px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 flex items-center gap-1">
              {{ insights?.totalProfileVisits ?? 0 }} Odwiedziny
              <InfoIcon :size="16" class="text-gray-400" />
            </h2>
            <p class="text-[13px] mt-1">
              <span class="text-green-600 font-medium">+{{ insights?.viewsGrowthPercent ?? 0 }}%</span>
              <span class="text-gray-500 ml-1">z poprzednich 28 dni</span>
            </p>
          </div>
          <div class="mt-auto pt-6">
            <div class="w-full border-b-[2px] border-blue-500 mb-2"></div>
            <div class="flex justify-between text-[11px] text-gray-500 px-1">
              <span v-for="point in (insights?.timeline || []).slice(-5)" :key="point.date">
                {{ point.date.split('-').slice(1).join('/') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Karta 2.1: Wiek i płeć -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[160px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 flex items-center gap-1">
              Wiek i płeć
              <InfoIcon :size="16" class="text-gray-400" />
            </h2>
            <p class="text-[13px] text-gray-500 mt-1">Ostatnie 28 dni</p>
          </div>
          <div class="flex-1 flex flex-col justify-center gap-2 mt-3 text-[13px]">
            <div class="flex justify-between">
              <span class="text-gray-600">Kobiety:</span>
              <span class="font-semibold text-gray-900">{{ insights?.audience?.womenPercent ?? 54 }}%</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Mężczyźni:</span>
              <span class="font-semibold text-gray-900">{{ insights?.audience?.menPercent ?? 44 }}%</span>
            </div>
          </div>
        </div>

        <!-- Karta 2.2: Kraj -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[160px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 flex items-center gap-1">
              Kraj
              <InfoIcon :size="16" class="text-gray-400" />
            </h2>
            <p class="text-[13px] text-gray-500 mt-1">Główne lokalizacje</p>
          </div>
          <div class="flex-1 flex flex-col justify-center gap-1.5 mt-2 text-[13px]">
            <div v-for="c in (insights?.audience?.topCountries || [])" :key="c.name" class="flex justify-between">
              <span class="text-gray-600">{{ c.name }}</span>
              <span class="font-semibold text-gray-900">{{ c.percent }}%</span>
            </div>
          </div>
        </div>

        <!-- Karta 2.3: Miasta -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[160px]">
          <div>
            <h2 class="text-[15px] font-bold text-gray-900 flex items-center gap-1">
              Miasta
              <InfoIcon :size="16" class="text-gray-400" />
            </h2>
            <p class="text-[13px] text-gray-500 mt-1">Główne miasta</p>
          </div>
          <div class="flex-1 flex flex-col justify-center gap-1.5 mt-2 text-[13px]">
            <div v-for="city in (insights?.audience?.topCities || [])" :key="city.name" class="flex justify-between">
              <span class="text-gray-600">{{ city.name }}</span>
              <span class="font-semibold text-gray-900">{{ city.percent }}%</span>
            </div>
          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import InfoIcon from 'vue-material-design-icons/Information.vue'
import { usePageAnalytics } from '@/composables/analytics/usePageAnalytics'

definePageMeta({
  layout: 'dashboard'
})

const { insights } = usePageAnalytics()
</script>
