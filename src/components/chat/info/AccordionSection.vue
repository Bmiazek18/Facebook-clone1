<!-- components/common/AccordionSection.vue -->
<template>
  <div class="mt-1">
    <button
      @click="isOpen = !isOpen"
      class="w-full px-4 py-3 flex justify-between items-center hover:bg-theme-bg-hover cursor-pointer transition-colors outline-none"
    >
      <span class="font-semibold text-[14px] text-theme-text">{{ title }}</span>
      <ChevronUpIcon
        :size="20"
        class="text-theme-text transition-transform duration-300"
        :class="{ 'rotate-180': !isOpen }"
      />
    </button>
    <div
      class="grid transition-[grid-template-rows,opacity] duration-300"
      :class="isOpen ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'"
    >
      <div class="overflow-hidden min-h-0">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'

const props = withDefaults(
  defineProps<{
    title: string
    defaultOpen?: boolean
  }>(),
  {
    defaultOpen: false,
  },
)

const isOpen = ref(props.defaultOpen)
</script>
