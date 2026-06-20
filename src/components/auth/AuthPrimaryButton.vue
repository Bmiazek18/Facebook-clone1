<template>
  <button
    :type="type"
    :disabled="disabled"
    :class="baseClasses"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
defineOptions({ inheritAttrs: false });

defineProps<{
  type?: 'button' | 'submit' | 'reset';
  disabled?: boolean;
}>();

// Default, shared auth button classes (all auth buttons identical)
const baseClasses = 'w-full font-medium py-3 px-4 rounded-full text-[15px] bg-primary hover:bg-primary-600 active:bg-primary-700 text-white transition-colors focus:outline-none focus:ring-4 focus:ring-primary/30 disabled:bg-primary-300 disabled:cursor-not-allowed';
</script>
