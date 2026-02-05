<template>
  <div class="flex items-center gap-3 w-full font-sans bg-theme-bg text-theme-text">

    <div :class="['shrink-0 flex items-center justify-center', iconColor]">
      <component
        :is="icon"
        :size="24"
        fillColor="currentColor"
      />
    </div>

    <div class="text-[15px] font-normal grow leading-snug">
      {{ title }}
    </div>

    <button
      v-if="action"
      @click.stop="action.handler"
      class="text-[14px] font-semibold text-blue-600 hover:text-blue-700 hover:underline shrink-0 px-2 cursor-pointer"
    >
      {{ action.label }}
    </button>

    <button
      @click="$emit('close-toast')"
      class="flex items-center justify-center w-8 h-8 ml-1 text-gray-400 bg-transparent hover:bg-theme-hover rounded-full transition-colors shrink-0 cursor-pointer"
    >
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps({
  title: { type: String, required: true },
  icon: { type: Object, required: true }, // Komponent ikony
  iconColor: { type: String, required: true }, // Klasa Tailwind (np. text-red-500)
  action: { type: Object, required: false } // { label: String, handler: Function }
});

defineEmits(['close-toast']);
</script>
