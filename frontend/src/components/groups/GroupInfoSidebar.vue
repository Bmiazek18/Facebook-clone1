<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Group } from '@/types/Group'

// --- ICONS ---
import InformationIcon from 'vue-material-design-icons/Information.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'
import GroupSetupWidget from './GroupSetupWidget.vue'

const { t } = useI18n()

const props = defineProps<{
  groupDetails?: Group
}>()

const isDescriptionExpanded = ref(false)

const truncatedDescription = computed(() => {
  const description = props.groupDetails?.description || ''
  const maxLength = 200
  if (description.length > maxLength && !isDescriptionExpanded.value) {
    return description.substring(0, maxLength) + '...'
  }
  return description
})
</script>

<template>
  <GroupSetupWidget/>
  <div class="bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm p-4 w-full">
    <!-- Nagłówek -->
    <h2 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-3">
      {{ t('groups.information') }}
    </h2>

    <!-- Linia oddzielająca -->
    <hr class="border-[#CED0D4] dark:border-[#3E4042] mb-4" />

    <!-- Opis bez ikony obok -->
    <div class="text-[15px] text-[#050505] dark:text-[#E4E6EB] leading-relaxed mb-5">
      {{ truncatedDescription }}
      <span
        v-if="(groupDetails?.description || '').length > 200"
        @click="isDescriptionExpanded = !isDescriptionExpanded"
        class="font-semibold cursor-pointer hover:underline ml-1 text-[#050505] dark:text-[#E4E6EB]"
      >
        {{ isDescriptionExpanded ? t('groups.hide') : t('groups.seeMore') }}
      </span>
    </div>

    <!-- Lista z ikonami -->
    <div class="space-y-4">

      <!-- Prywatność -->
      <div class="flex items-start gap-3">
        <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
          <!-- Kłódka (Prywatna) lub Ziemia (Publiczna) -->
          <svg v-if="groupDetails?.privacy !== 'public'" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17 9V7c0-2.76-2.24-5-5-5S7 4.24 7 7v2H6a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V11a2 2 0 00-2-2h-1zm-5 8c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3-8H9V7c0-1.66 1.34-3 3-3s3 1.34 3 3v2z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="currentColor">
             <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
          </svg>
        </div>
        <div class="flex flex-col">
          <span class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-snug">
            {{ groupDetails?.privacy === 'public' ? t('groups.public') : t('groups.private') }}
          </span>
          <span class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug mt-1">
            {{ groupDetails?.privacy === 'public' ? t('groups.publicDescription') : t('groups.privateDescription') }}
          </span>
        </div>
      </div>

      <!-- Widoczność -->
      <div class="flex items-start gap-3">
        <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
          <!-- Ikona Oka -->
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
          </svg>
        </div>
        <div class="flex flex-col">
          <span class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-snug">
            {{ t('groups.visibility') }}
          </span>
          <span class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug mt-1">
            {{ t('groups.visibleDescription') }}
          </span>
        </div>
      </div>

      <!-- Historia -->
      <div class="flex items-start gap-3">
        <div class="w-6 h-6 flex-shrink-0 text-[#8C939D] dark:text-[#B0B3B8] mt-0.5">
          <!-- Ikona Zegara -->
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2a10 10 0 100 20 10 10 0 000-20zm1 10.41l3.3 1.98-1 1.66-4.3-2.5V7h2v5.41z"/>
          </svg>
        </div>
        <div class="flex flex-col">
          <span class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-snug">{{ $t('groups.historia') }}</span>
          <span class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug mt-1">{{ $t('groups.dataUtworzeniaGrupy8') }}<span class="font-bold text-[#050505] dark:text-[#E4E6EB] cursor-pointer hover:underline ml-0.5">{{ $t('groups.wyswietlWiecej') }}</span>
          </span>
        </div>
      </div>

    </div>
  </div>
</template>
