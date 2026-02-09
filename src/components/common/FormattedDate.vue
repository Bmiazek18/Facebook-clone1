<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import {
  format,
  isToday,
  isYesterday,
  formatDistanceToNow,
  differenceInMinutes,
  differenceInHours,
  differenceInDays,
  differenceInWeeks,
  differenceInMonths,
  differenceInYears
} from 'date-fns';
import { pl } from 'date-fns/locale';

const props = withDefaults(defineProps<{
  date: string | number | Date;
  short?: boolean;
}>(), {
  short: false
});

const now = ref(new Date());
let timer: ReturnType<typeof setTimeout> | ReturnType<typeof setInterval> | null = null;

const startPreciseTimer = () => {
  const dateObj = new Date(props.date);
  if (isNaN(dateObj.getTime())) return;

  if (differenceInMinutes(new Date(), dateObj) >= 60) return;

  const syncToMinute = () => {
    const currentNow = new Date();
    const msUntilNextMinute = (60 - currentNow.getSeconds()) * 1000 - currentNow.getMilliseconds();

    timer = setTimeout(() => {
      now.value = new Date();

      if (differenceInMinutes(now.value, dateObj) >= 60) return;

      timer = setInterval(() => {
        const loopNow = new Date();
        now.value = loopNow;

        if (differenceInMinutes(loopNow, dateObj) >= 60) {
          if (timer) clearInterval(timer);
          timer = null;
        }
      }, 60000);

    }, msUntilNextMinute + 50);
  };

  syncToMinute();
};

onMounted(() => {
  startPreciseTimer();
});

onUnmounted(() => {
  if (timer) {
    clearTimeout(timer);
    clearInterval(timer as any);
  }
});

const formattedDate = computed(() => {
  const currentTime = now.value;
  const dateObj = new Date(props.date);

  if (isNaN(dateObj.getTime())) return props.date.toString();

  // --- TRYB KRÓTKI (SHORT) ---
  if (props.short) {
    const diffMin = differenceInMinutes(currentTime, dateObj);
    if (diffMin < 1) return 'teraz';
    if (diffMin < 60) return `${diffMin} min`;

    const diffHours = differenceInHours(currentTime, dateObj);
    if (diffHours < 24) return `${diffHours} godz.`;

    const diffDays = differenceInDays(currentTime, dateObj);
    if (diffDays < 7) return `${diffDays} dni`;

    const diffWeeks = differenceInWeeks(currentTime, dateObj);
    if (diffWeeks < 5) return `${diffWeeks} tyg.`;

    const diffMonths = differenceInMonths(currentTime, dateObj);
    if (diffMonths < 12) return `${diffMonths} mies.`;

    const diffYears = differenceInYears(currentTime, dateObj);
    return `${diffYears} lat`;
  }

  // --- TRYB STANDARDOWY ---
  const diff = differenceInMinutes(currentTime, dateObj);

  if (diff < 60) {
    return formatDistanceToNow(dateObj, { addSuffix: true, locale: pl });
  }

  if (isToday(dateObj)) {
    return `Dzisiaj o ${format(dateObj, 'HH:mm')}`;
  }

  if (isYesterday(dateObj)) {
    return `Wczoraj o ${format(dateObj, 'HH:mm')}`;
  }

  if (dateObj.getFullYear() === currentTime.getFullYear()) {
    return format(dateObj, "d MMMM 'o' HH:mm", { locale: pl });
  }

  return format(dateObj, "d MMMM yyyy", { locale: pl });
});

const fullFormattedDate = computed(() => {
  const dateObj = new Date(props.date);
  if (isNaN(dateObj.getTime())) return props.date.toString();
  return format(dateObj, "EEEE, d MMMM yyyy 'o' HH:mm", { locale: pl });
});
</script>

<template>
  <span class="hover:underline cursor-pointer text-theme-text-secondary z-[30]" v-tooltip="fullFormattedDate">
    {{ formattedDate }}
  </span>
</template>
