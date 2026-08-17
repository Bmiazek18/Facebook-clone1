<template>
  <VDropdown :distance="10" placement="bottom-start" theme="dropdown">
    <button
      class="mr-1.5 flex items-center justify-center text-[#1877F2] hover:bg-blue-50 rounded-full p-1 -ml-1 transition-colors"
    >
      <Cog :size="16" />
    </button>

    <template #popper>
      <div class="w-[320px] py-2 text-[#050505] dark:text-[#E4E6EB] text-[15px]">
        <!-- Rating section -->
        <div
          class="flex flex-col items-center justify-center p-2 pb-3 border-b border-gray-200 dark:border-gray-700"
        >
          <span class="mb-2 font-medium">Oceń to tłumaczenie</span>

          <div class="flex gap-1 mb-2" @mouseleave="localHoverRating = 0">
            <button
              v-for="i in 5"
              :key="i"
              @click="setRating(i)"
              @mouseenter="localHoverRating = i"
              class="transition-transform hover:scale-110 focus:outline-none"
            >
              <component
                :is="isStarFilled(i) ? Star : StarOutline"
                :size="32"
                class="text-[#1877F2] transition-colors duration-200"
              />
            </button>
          </div>

          <span class="text-[13px] text-gray-500">
            {{ rating > 0 ? 'Dziękujemy za ocenę!' : 'Kliknij gwiazdkę, aby ocenić' }}
          </span>
        </div>

        <!-- Options menu -->
        <div class="mt-2">
          <button
            class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors"
          >
            <Close :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
            <div class="flex flex-col">
              <span class="font-medium leading-tight"
                >Nigdy nie tłumacz z języka: {{ detectedLanguage }}</span
              >
              <span class="text-[13px] text-gray-500 mt-0.5"
                >Tłumaczenie z języka: {{ detectedLanguage }} na polski</span
              >
            </div>
          </button>

          <button
            class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors"
          >
            <MinusCircle :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
            <div class="flex flex-col">
              <span class="font-medium leading-tight"
                >Post nie był w języku: {{ detectedLanguage }}</span
              >
              <span class="text-[13px] text-gray-500 mt-0.5">Zgłoś błąd</span>
            </div>
          </button>

          <button
            class="w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-start gap-3 transition-colors"
          >
            <Cog :size="24" class="text-[#050505] dark:text-[#E4E6EB] mt-0.5" />
            <div class="flex flex-col justify-center h-full">
              <span class="font-medium mt-1">Ustawienia języka</span>
            </div>
          </button>
        </div>
      </div>
    </template>
  </VDropdown>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Dropdown as VDropdown } from 'floating-vue'

import Cog from 'vue-material-design-icons/Cog.vue'
import Star from 'vue-material-design-icons/Star.vue'
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import Close from 'vue-material-design-icons/Close.vue'
import MinusCircle from 'vue-material-design-icons/MinusCircle.vue'

interface Props {
  detectedLanguage?: string
  rating: number
  hoverRating: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:rating': [value: number]
  'update:hoverRating': [value: number]
}>()

// Local state for hover rating to avoid prop mutation
const localHoverRating = ref(0)

const isStarFilled = (index: number) => {
  const activeRating = localHoverRating.value > 0 ? localHoverRating.value : props.rating
  return index <= activeRating
}

const setRating = (value: number) => {
  emit('update:rating', value)
}
</script>
