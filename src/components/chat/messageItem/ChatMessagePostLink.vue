<script setup lang="ts">
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
}>()
</script>

<template>
  <div
    class="flex flex-col overflow-hidden rounded-2xl shadow-sm min-w-[280px] max-w-full border border-gray-100 bg-gray-200"
  >
    <div class="flex items-center px-4 py-3">
      <div
        class="w-10 h-10 rounded-full overflow-hidden mr-3 border border-gray-200 bg-black"
      >
        <img
          src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80"
          class="w-full h-full object-cover opacity-90"
          alt="Author"
        />
      </div>
      <div class="flex flex-col">
        <span class="font-bold text-gray-900 text-[15px] leading-tight"> Coding Tips </span>
      </div>
    </div>

    <div class="w-full relative bg-gray-800">
      <img
        src="https://images.unsplash.com/photo-1555066931-4365d14bab8c?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"
        class="w-full h-auto object-cover max-h-[250px]"
        alt="Code example"
      />
      <div
        class="absolute bottom-0 w-full bg-yellow-100/90 text-center py-1 text-xs font-bold text-gray-800 border-t border-yellow-200"
      >
        Takeaway: Unused Code Create Confusion.
      </div>
    </div>

    <div class="px-4 py-3 flex flex-col justify-between min-h-[80px]">
      <h3 class="font-bold text-gray-900 text-[17px] leading-tight mb-1">
        Golden Rules of Writing Good Code.
      </h3>

      <div class="flex items-center justify-between mt-auto pt-1">
        <div class="flex items-center text-gray-500 text-sm font-medium">
          <span>Facebook</span>
        </div>
        <span
          class="text-gray-400 font-bold text-xl leading-none pb-2 cursor-pointer hover:text-gray-600"
          >...</span
        >
      </div>
    </div>
  </div>
</template>
