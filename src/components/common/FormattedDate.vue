<script setup lang="ts">
import { computed } from 'vue'
import {
  format,
  isToday,
  isYesterday,
  differenceInMinutes,
  differenceInHours,
  differenceInDays,
  differenceInWeeks,
  differenceInMonths,
  differenceInYears,
} from 'date-fns'
import { pl } from 'date-fns/locale'
import { useNow } from '@/composables/shared/useTime'

const props = withDefaults(
  defineProps<{
    date?: string | number | Date | null
    short?: boolean
  }>(),
  {
    date: null,
    short: false,
  },
)

const now = useNow()

const formattedDate = computed(() => {
  if (!props.date) return ''

  const currentTime = now.value
  const dateObj = new Date(props.date)

  if (isNaN(dateObj.getTime())) return String(props.date)

  // --- TRYB KRÓTKI (SHORT) ---
  if (props.short) {
    const diffMin = differenceInMinutes(currentTime, dateObj)
    if (diffMin < 1) return 'teraz'
    if (diffMin < 60) return `${diffMin} min`

    const diffHours = differenceInHours(currentTime, dateObj)
    if (diffHours < 24) return `${diffHours} godz.`

    const diffDays = differenceInDays(currentTime, dateObj)
    if (diffDays < 7) return `${diffDays} dni`

    const diffWeeks = differenceInWeeks(currentTime, dateObj)
    if (diffWeeks < 5) return `${diffWeeks} tyg.`

    const diffMonths = differenceInMonths(currentTime, dateObj)
    if (diffMonths < 12) return `${diffMonths} mies.`

    const diffYears = differenceInYears(currentTime, dateObj)
    return `${diffYears} lat`
  }

  // --- TRYB STANDARDOWY (STYL FB - CZYSZCZONE Z "OKOŁO") ---
  if (isToday(dateObj)) {
    const diffMin = Math.max(0, differenceInMinutes(currentTime, dateObj))
    if (diffMin < 1) return 'Przed chwilą'
    if (diffMin < 60) return `${diffMin} min`

    const diffHours = differenceInHours(currentTime, dateObj)
    return `${diffHours} godz.`
  }

  if (isYesterday(dateObj)) {
    return `Wczoraj o ${format(dateObj, 'HH:mm')}`
  }

  if (dateObj.getFullYear() === currentTime.getFullYear()) {
    return format(dateObj, "d MMMM 'o' HH:mm", { locale: pl })
  }

  return format(dateObj, 'd MMMM yyyy', { locale: pl })
})

const fullFormattedDate = computed(() => {
  if (!props.date) return ''

  const dateObj = new Date(props.date)
  if (isNaN(dateObj.getTime())) return String(props.date)

  return format(dateObj, "EEEE, d MMMM yyyy 'o' HH:mm", { locale: pl })
})
</script>

<template>
  <span
    v-if="props.date"
    class="hover:underline cursor-pointer text-theme-text-secondary z-[30]"
    v-tooltip="fullFormattedDate"
  >
    {{ formattedDate }}
  </span>
</template>
