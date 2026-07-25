<script setup lang="ts">
import { ref } from 'vue'
import type { Group } from '@/types/Group'
import GroupInfoSidebar from '@/components/groups/GroupInfoSidebar.vue'

defineProps<{
  groupDetails?: Group
  stickyTop?: number
}>()

const rightSectionRef = ref<HTMLDivElement | null>(null)
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
    <div class="lg:col-span-7 space-y-4">
      <div class="p-4 bg-theme-bg-secondary rounded-lg">
        <h1 class="text-xl font-bold">Group Information</h1>
        <p>This page will contain detailed information about the group.</p>
      </div>
    </div>
    <div
      ref="rightSectionRef"
      class="lg:col-span-5 space-y-4 sticky self-start"
      :style="{ top: `${stickyTop}px` }"
    >
      <GroupInfoSidebar :group-details="groupDetails" />
    </div>
  </div>
</template>
