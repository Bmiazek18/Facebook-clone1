<template>
  <div class="p-6 flex flex-col gap-4 w-full max-w-[1200px] mx-auto">
    <StatsCard/>

    <!-- GÓRNY RZĄD (Dwie kolumny) -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 min-h-[300px]">

      <!-- Karta 1: Wyświetlenia według typu zawartości -->
      <div class="bg-white rounded-xl border border-[#CED0D4] p-5 flex flex-col shadow-sm">
        <h2 class="text-[17px] font-semibold text-[#050505]">{{ $t('dashboard.wyswietleniaWedlugTypuZawartosci') }}</h2>

        <!-- Legenda -->
        <div class="flex items-center gap-4 mt-4 text-[13px] text-[#65676B]">
          <div class="flex items-center gap-1.5">
            <div class="w-3 h-3 rounded-full bg-[#1877F2]"></div>
            <span>{{ $t('dashboard.obserwatorzy') }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="w-3 h-3 rounded-full bg-[#002D62]"></div>
            <span>{{ $t('dashboard.osobyNiebedaceObserwatorami') }}</span>
          </div>
        </div>

        <!-- Wykresy Paskowe dla każdego typu zawartości -->
        <div class="mt-6 flex flex-col gap-4">
          <!-- Tekst -->
          <div class="flex flex-col gap-1.5">
            <div class="flex justify-between text-[14px] text-[#050505]">
              <span>{{ $t('ui.text') }}</span>
              <span class="font-semibold">{{ $t('dashboard.textpercent') }}</span>
            </div>
            <div class="h-3 bg-[#E5E5E5] rounded-full overflow-hidden flex">
              <div class="h-full bg-[#002D62] transition-all duration-500" :style="{ width: `${textPercent}%` }"></div>
            </div>
          </div>

          <!-- Zdjęcia -->
          <div class="flex flex-col gap-1.5">
            <div class="flex justify-between text-[14px] text-[#050505]">
              <span>{{ $t('profile.tabs.photos') }}</span>
              <span class="font-semibold">{{ $t('dashboard.photopercent') }}</span>
            </div>
            <div class="h-3 bg-[#E5E5E5] rounded-full overflow-hidden flex">
              <div class="h-full bg-[#1877F2] transition-all duration-500" :style="{ width: `${photoPercent}%` }"></div>
            </div>
          </div>

          <!-- Wideo / Rolki -->
          <div class="flex flex-col gap-1.5">
            <div class="flex justify-between text-[14px] text-[#050505]">
              <span>{{ $t('dashboard.wideoIRolki') }}</span>
              <span class="font-semibold">{{ $t('dashboard.videopercent') }}</span>
            </div>
            <div class="h-3 bg-[#E5E5E5] rounded-full overflow-hidden flex">
              <div class="h-full bg-[#00A400] transition-all duration-500" :style="{ width: `${videoPercent}%` }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Karta 2: Wyświetlenia z podziałem na osoby... -->
      <div class="bg-white rounded-xl border border-[#CED0D4] p-5 flex flex-col shadow-sm">
        <h2 class="text-[17px] font-semibold text-[#050505]">{{ $t('dashboard.wyswietleniaZPodzialemNa') }}</h2>

        <!-- Zawartość karty z wykresem kołowym i legendą -->
        <div class="flex-1 flex flex-col items-center justify-center mt-6">

          <!-- Wykres kołowy (SVG Donut Chart) -->
          <div class="relative w-32 h-32">
            <svg viewBox="0 0 36 36" class="w-full h-full transform -rotate-90">
              <!-- Tło wykresu (szare) -->
              <path
                class="text-[#F0F2F5]"
                stroke-width="4"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <!-- Wypełnienie: Osoby niebędące obserwatorami -->
              <path
                class="text-[#1877F2] transition-all duration-700"
                :stroke-dasharray="`${nonFollowerPercent}, 100`"
                stroke-linecap="butt"
                stroke-width="4"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <!-- Wypełnienie: Obserwatorzy -->
              <path
                v-if="followerPercent > 0"
                class="text-[#002D62] transition-all duration-700"
                :stroke-dasharray="`${followerPercent}, 100`"
                :stroke-dashoffset="`-${nonFollowerPercent}`"
                stroke-linecap="butt"
                stroke-width="4"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
          </div>

          <!-- Legenda z procentami pod wykresem -->
          <div class="flex justify-center gap-10 mt-6 w-full">

            <!-- Osoby niebędące obserwatorami (Z lewej) -->
            <div class="flex flex-col items-center text-center">
              <span class="text-[17px] font-semibold text-[#050505]">{{ $t('dashboard.nonfollowerpercent') }}</span>
              <div class="flex gap-1.5 mt-1 items-start justify-center">
                <span class="text-[13px] text-[#65676B] max-w-[120px] leading-tight text-right">{{ $t('dashboard.osobyNiebedaceObserwatorami') }}</span>
                <div class="w-2.5 h-2.5 rounded-full bg-[#1877F2] mt-0.5 shrink-0"></div>
              </div>
            </div>

            <!-- Obserwatorzy (Z prawej) -->
            <div class="flex flex-col items-center text-center">
              <span class="text-[17px] font-semibold text-[#050505]">{{ $t('dashboard.followerpercent') }}</span>
              <div class="flex gap-1.5 mt-1 items-start justify-center">
                <div class="w-2.5 h-2.5 rounded-full bg-[#002D62] mt-0.5 shrink-0"></div>
                <span class="text-[13px] text-[#65676B] leading-tight text-left">{{ $t('dashboard.obserwatorzy') }}</span>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>

    <!-- DOLNY RZĄD: Ostatnie podsumowanie -->
    <div class="bg-white rounded-xl border border-[#CED0D4] p-5 flex flex-col shadow-sm min-h-[140px]">
      <h2 class="text-[17px] font-semibold text-[#050505]">{{ $t('dashboard.odwiedzinyIZaangazowanieProfilu') }}</h2>
      <div class="flex items-center gap-8 mt-4">
        <div>
          <span class="text-[24px] font-bold text-[#050505]">{{ insights?.totalProfileVisits ?? 0 }}</span>
          <p class="text-[13px] text-[#65676B]">{{ $t('dashboard.odwiedzinyProfilu') }}</p>
        </div>
        <div>
          <span class="text-[24px] font-bold text-[#050505]">{{ (insights?.totalReactions ?? 0) + (insights?.totalComments ?? 0) }}</span>
          <p class="text-[13px] text-[#65676B]">{{ $t('dashboard.laczneInterakcje') }}</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import StatsCard from '~/components/professional_dashboard/StatsCard.vue'
import { usePageAnalytics } from '@/composables/analytics/usePageAnalytics'

definePageMeta({
  layout: 'dashboard'
})

const { insights } = usePageAnalytics()

const nonFollowerPercent = computed(() => {
  return Math.round(insights.value?.nonFollowerViewsPercent ?? 100)
})

const followerPercent = computed(() => {
  return Math.round(insights.value?.followerViewsPercent ?? 0)
})

const textPercent = computed(() => {
  const byType = insights.value?.viewsByContentType
  if (!byType) return 100
  const total = (byType.text || 0) + (byType.photo || 0) + (byType.video || 0)
  if (total === 0) return 100
  return Math.round(((byType.text || 0) / total) * 100)
})

const photoPercent = computed(() => {
  const byType = insights.value?.viewsByContentType
  if (!byType) return 0
  const total = (byType.text || 0) + (byType.photo || 0) + (byType.video || 0)
  if (total === 0) return 0
  return Math.round(((byType.photo || 0) / total) * 100)
})

const videoPercent = computed(() => {
  const byType = insights.value?.viewsByContentType
  if (!byType) return 0
  const total = (byType.text || 0) + (byType.photo || 0) + (byType.video || 0)
  if (total === 0) return 0
  return Math.round(((byType.video || 0) / total) * 100)
})
</script>
