<template>
  <div class="relative inline-block text-left font-sans text-[#0f0f0f]" ref="dropdownRef">

    <!-- Zamknięty widok: Przycisk -->
    <button
      @click="toggleMenu"
      class="inline-flex items-center gap-3 bg-[#e8ebed] hover:bg-[#dce0e3] font-medium text-[15px] px-4 py-2 rounded-lg transition-colors cursor-pointer border-none"
    >
      <span>{{ selectedOptionLabel }}</span>
      <svg class="w-3 h-3 fill-current ml-1" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
        <path d="M4 6h8l-4 5.333L4 6z"/>
      </svg>
    </button>

    <!-- Otwarty widok: Menu rozwijane -->
    <div
      v-if="isOpen"
      class="absolute left-0 mt-2 w-[320px] bg-white rounded-lg shadow-lg py-2 z-50 border border-gray-100"
    >
      <div
        v-for="option in options"
        :key="option.value"
        @click="handleSelect(option)"
        class="flex justify-between items-center px-6 py-3 cursor-pointer hover:bg-[#f2f2f2] transition-colors text-[15px]"
      >
        <span>{{ option.label }}</span>

        <!-- Ikonka: Radio -->
        <span
          v-if="option.type === 'radio'"
          :class="[
            'w-5 h-5 rounded-full box-border',
            props.modelValue === option.value
              ? 'border-[6px] border-[#065fd4]'
              : 'border-2 border-[#717171]'
          ]"
        ></span>

        <!-- Ikonka: Strzałka -->
        <span
          v-else-if="option.type === 'arrow'"
          class="text-[#717171] text-xl font-bold leading-none mb-1"
        >
          &rsaquo;
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: 'last_28_days'
  }
})

const emit = defineEmits(['update:modelValue'])

const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const formatDate = (date: Date) => {
  const months = ['sty', 'lut', 'mar', 'kwi', 'maj', 'cze', 'lip', 'sie', 'wrz', 'paź', 'lis', 'gru']
  return `${date.getDate()} ${months[date.getMonth()]}`
}

const options = computed(() => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const formattedYesterday = formatDate(yesterday)

  const getRangeLabel = (days: number) => {
    const startDate = new Date(yesterday)
    startDate.setDate(yesterday.getDate() - (days - 1))
    return `${formatDate(startDate)} – ${formattedYesterday}`
  }

  const getThisYearLabel = () => {
    const firstDayOfYear = new Date(today.getFullYear(), 0, 1)
    return `${formatDate(firstDayOfYear)} – ${formattedYesterday}`
  }

  return [
    { value: 'today', label: 'Dziś', type: 'radio' },
    { value: 'last_7_days', label: `Ostatnie 7 dni: ${getRangeLabel(7)}`, type: 'radio' },
    { value: 'last_14_days', label: `Ostatnie 14 dni: ${getRangeLabel(14)}`, type: 'radio' },
    { value: 'last_28_days', label: `Ostatnie 28 dni: ${getRangeLabel(28)}`, type: 'radio' },
    { value: 'last_60_days', label: `Ostatnie 60 dni: ${getRangeLabel(60)}`, type: 'radio' },
    { value: 'last_90_days', label: `Ostatnie 90 dni: ${getRangeLabel(90)}`, type: 'radio' },
    { value: 'this_year', label: `W tym roku: ${getThisYearLabel()}`, type: 'radio' },
    { value: 'monthly', label: 'Co miesiąc', type: 'arrow' },
    { value: 'custom', label: 'Niestandardowy', type: 'arrow' },
  ]
})

const selectedOptionLabel = computed(() => {
  const foundOption = options.value.find(opt => opt.value === props.modelValue)
  return foundOption ? foundOption.label : 'Wybierz zakres'
})

const toggleMenu = () => {
  isOpen.value = !isOpen.value
}

const handleSelect = (option: any) => {
  if (option.type === 'radio') {
    emit('update:modelValue', option.value) // Wysłanie nowej wartości do ojca
    isOpen.value = false
  } else {
    console.log('Otwórz dodatkowy widok/modal dla:', option.label)
  }
}

const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', handleClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', handleClickOutside))
</script>
