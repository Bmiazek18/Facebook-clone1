<template>
  <button
    :type="type"
    :disabled="disabled"
    :class="[commonClasses, variantClasses[variant]]"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset'
    disabled?: boolean
    variant?: 'primary' | 'secondary'
  }>(),
  {
    type: 'button',
    disabled: false,
    variant: 'primary',
  },
)

const commonClasses =
  'w-full font-medium py-3 px-4 rounded-full text-[15px] transition-colors cursor-pointer focus:outline-none focus:ring-4 disabled:cursor-not-allowed disabled:opacity-60'

const variantClasses = {
  primary:
    'bg-primary hover:bg-primary-600 active:bg-primary-700 text-white focus:ring-primary/30 disabled:bg-primary-300',
  secondary:
    'bg-white hover:bg-gray-50 active:bg-gray-100 border border-[#ccd0d5] text-[#1c1e21] focus:ring-gray-200 disabled:bg-gray-50',
}
</script>
