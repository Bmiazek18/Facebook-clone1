<script setup lang="ts">
import { defineProps } from 'vue'
import type { Event } from '@/types/Event'

import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue'
import ReplyIcon from 'vue-material-design-icons/Reply.vue'

const props = defineProps<{
  event: Event
}>()

const router = useRouter()

const navigateToEvent = (eventId: string) => {
  router.push(`/event/${eventId}`)
}
</script>

<template>
  <div
    class="group bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md overflow-hidden flex flex-col transition-all duration-200 cursor-pointer"
    @click="navigateToEvent(props.event.id)"
  >
    <div class="relative aspect-[1.91/1] w-full overflow-hidden">
      <img
        :src="props.event.images[0]"
        class="w-full h-full object-cover transition-transform duration-500"
        v-if="props.event.images?.length"
      />
      <button
        class="absolute top-2 right-2 bg-black/60 hover:bg-black/80 text-white p-1.5 rounded-full transition z-10"
      >
        <DotsHorizontalIcon :size="20" />
      </button>
    </div>

    <div class="p-4 flex flex-1 flex-col hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
      <p class="text-[13px] font-medium text-gray-900 dark:text-gray-100 mb-0.5">
        {{ props.event.startDate }} o {{ props.event.startTime }}
      </p>

      <h3
        v-tooltip="props.event.name"
        class="text-[17px] font-bold leading-[1.2] mb-1 text-gray-900 dark:text-gray-100 line-clamp-2 hover:underline cursor-pointer"
      >
        {{ props.event.name }}
      </h3>

      <p class="text-gray-500 dark:text-gray-400 text-[14px] font-normal mb-1 truncate">
        {{ props.event.location }}
      </p>

      <p
        class="text-gray-500 dark:text-gray-400 truncate text-[13px] mb-4"
        v-tooltip="'16 osób zainteresowanych · 24 osoby weźmie udział'"
      >
        16 osób zainteresowanych · 24 osoby weźmie udział
      </p>

      <div class="flex gap-2 mt-auto">
        <button
          class="flex-grow flex items-center justify-center gap-2 py-2 bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-gray-800 dark:text-gray-200 font-semibold rounded-lg transition text-[15px]"
        >
          <StarOutlineIcon :size="20" />
          <span>Zainteresowany(a)</span>
        </button>

        <button
          class="w-12 h-10 flex items-center justify-center bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-gray-800 dark:text-gray-200 rounded-lg transition"
        >
          <ReplyIcon :size="20" class="rotate-0 scale-x-[-1]" />
        </button>
      </div>
    </div>
  </div>
</template>
