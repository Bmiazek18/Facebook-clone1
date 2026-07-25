<script setup lang="ts">
defineProps<{
  src?: string
  alt?: string
}>()
</script>

<template>
  <img
    :src="src"
    :alt="alt"
    class="w-10.5 h-10.5 rounded-full object-cover cursor-pointer hover:brightness-85 transition-all duration-100"
  />
</template>
