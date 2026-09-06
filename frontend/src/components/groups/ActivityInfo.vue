<template>
  <div class=" bg-transparent flex justify-center items-start">

    <!-- Karta główna widżetu -->
    <div class="w-full  bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm p-4">

      <!-- Nagłówek -->
      <h2 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-3">{{ $t('emojiPicker.categories.activity') }}</h2>

      <!-- Delikatna linia oddzielająca -->
      <hr class="border-[#CED0D4] dark:border-[#3E4042] mb-4" />

      <!-- Kontener z listą -->
      <div class="space-y-5">

        <!-- Row 1: Posty -->
        <div class="flex items-start gap-3">
          <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
            <svg viewBox="0 0 24 24" fill="currentColor" class="w-full h-full">
              <path d="M14.5 9h-7A2.5 2.5 0 0 0 5 11.5v3.62l-2.02 1.34A1 1 0 0 0 3.5 18h1.02a2.5 2.5 0 0 0 2.48 2h7.5A2.5 2.5 0 0 0 17 17.5v-6A2.5 2.5 0 0 0 14.5 9z"/>
              <path opacity="0.6" d="M19 4h-7A2.5 2.5 0 0 0 9.5 6.5v1.2a4 4 0 0 1 5.4 5.3h4.1a2.5 2.5 0 0 0 2.5-2.5v-6A2.5 2.5 0 0 0 19 4z"/>
            </svg>
          </div>
          <div class="flex flex-col">
            <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] leading-snug">
              {{ groupDetails?.newPostsToday ? groupDetails.newPostsToday + ' nowych postów dzisiaj' : 'Dzisiaj brak nowych postów' }}
            </span>
            <span class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug mt-0.5">{{ $t('groups.groupdetailsNewpostsmonth0W') }}</span>
          </div>
        </div>

        <!-- Row 2: Liczba członków -->
        <div class="flex items-start gap-3">
          <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
            <svg viewBox="0 0 24 24" fill="currentColor" class="w-full h-full">
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
            </svg>
          </div>
          <div class="flex flex-col">
            <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] leading-snug">{{ $t('groups.lacznieGroupdetailsMembers0') }}</span>
            <span class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug mt-0.5">{{ $t('groups.groupdetailsNewmembersweekBrakNowych') }}</span>
          </div>
        </div>

        <!-- Row 3: Data utworzenia -->
        <div class="flex items-start gap-3">
          <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
            <svg viewBox="0 0 24 24" fill="currentColor" class="w-full h-full">
              <path d="M12.5 11c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm-6 0c1.38 0 2.5-1.12 2.5-2.5S7.88 6 6.5 6 4 7.12 4 8.5 5.12 11 6.5 11zm12 0c1.38 0 2.5-1.12 2.5-2.5S19.88 6 18.5 6 16 7.12 16 8.5 17.12 11 18.5 11zm-6 2c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"/>
              <path opacity="0.6" d="M6 13.5c-.88 0-1.72.16-2.5.45V17H.5v-2.5c0-2.33 4.67-3.5 7-3.5.58 0 1.15.06 1.7.15A4.54 4.54 0 0 0 6 13.5zm12 0c0 .87-.24 1.68-.66 2.37H22.5V18h-3c-.15 0-.29-.02-.43-.05A7.83 7.83 0 0 1 20 17v-2.5c0-2.33-4.67-3.5-7-3.5a10.84 10.84 0 0 0-1.7.15c.98.54 1.7 1.48 1.7 2.35z"/>
            </svg>
          </div>
          <div class="flex flex-col">
            <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] leading-snug">{{ $t('groups.utworzono') }}</span>
            <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] leading-snug mt-0.5">
              {{ getFormattedCreatedAge(groupDetails?.createdAge) }}
            </span>
          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import type { Group } from '@/types/Group'
import { formatTimeAgo } from '@/utils/timeFormatter'

defineProps<{
  groupDetails?: Group
}>()

const getFormattedCreatedAge = (createdAge: string | undefined) => {
  if (!createdAge) return 'niedawno'
  
  if (/^\d+$/.test(createdAge)) {
    return formatTimeAgo(parseInt(createdAge))
  }
  
  const parsed = Date.parse(createdAge)
  if (!isNaN(parsed)) {
    return formatTimeAgo(parsed)
  }
  
  return createdAge
}
</script>
