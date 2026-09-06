<script setup lang="ts">
import { ref } from 'vue'
import type { Group } from '@/types/Group'
import GroupInfoSidebar from '@/components/groups/GroupInfoSidebar.vue'
import MemberInfo from '~/components/groups/MemberInfo.vue';
import GroupRulesList from '~/components/groups/GroupRulesList.vue';
import ActivityInfo from '~/components/groups/ActivityInfo.vue';

defineProps<{
  groupDetails?: Group
  stickyTop?: number
}>()

const rightSectionRef = ref<HTMLDivElement | null>(null)
</script>

<template>
  <div class="w-full max-w-[680px] mx-auto flex flex-col gap-3">
    <GroupInfoSidebar :group-details="groupDetails" />
    <MemberInfo :group-details="groupDetails" />
    <ActivityInfo :group-details="groupDetails" />
    <GroupRulesList />
  </div>
</template>
