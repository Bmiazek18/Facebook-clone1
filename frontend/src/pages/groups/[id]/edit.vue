<script setup lang="ts">
import { ref } from 'vue'
import ConfigureSection from '@/components/groups/edit/ConfigureSection.vue'
import CustomizeSection from '@/components/groups/edit/CustomizeSection.vue'
import MembershipSection from '@/components/groups/edit/MembershipSection.vue'
import DiscussionSection from '@/components/groups/edit/DiscussionSection.vue'
import FeaturesSection from '@/components/groups/edit/FeaturesSection.vue'

const activeEditId = ref<string | null>(null)
</script>

<template>
  <div class="min-h-screen pb-8 px-4 sm:px-6 flex justify-center selection:bg-blue-600">
    <div class="w-full max-w-[500px] space-y-4 pt-6">
      <ConfigureSection v-model:activeEditId="activeEditId" />
      <CustomizeSection v-model:activeEditId="activeEditId" />
      <MembershipSection v-model:activeEditId="activeEditId" />
      <DiscussionSection v-model:activeEditId="activeEditId" />
      <FeaturesSection v-model:activeEditId="activeEditId" />
    </div>
  </div>
</template>
