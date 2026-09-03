<template>
  <div class="flex min-h-screen overflow-hidden bg-[#141414]">
    <AppSidebar />
    <main :class="[
      'h-screen flex-1 overflow-y-auto transition-all duration-300 ease-in-out',
      isExpanded ? 'ml-72' : 'ml-16'
    ]">
      <div class="flex-1 bg-[#141414] h-full">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/meta-ai/AppSidebar.vue'
import { useSidebar } from '@/composables/useSidebar'

const { isExpanded } = useSidebar()
</script>
