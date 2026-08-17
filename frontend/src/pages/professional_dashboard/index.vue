<script setup lang="ts">
import StatsCard from '@/components/professional_dashboard/StatsCard.vue'
import ContentCard from '@/components/professional_dashboard/ContentCard.vue'
import MonetizationCard from '@/components/professional_dashboard/MonetizationCard.vue'
import ActivityCard from '@/components/professional_dashboard/ActivityCard.vue'
import PageStatusCard from '@/components/professional_dashboard/PageStatusCard.vue'
import FeedbackCard from '@/components/professional_dashboard/FeedbackCard.vue'

definePageMeta({
  layout: 'dashboard'
})
</script>

<template>
  <div class="w-full p-4 md:p-8 flex justify-center">
    <div class="w-full max-w-[1100px] flex flex-col lg:flex-row gap-4 md:gap-6 items-start">

      <!-- LEWA KOLUMNA (Szersza) -->
      <div class="flex-1 w-full flex flex-col gap-4 md:gap-6">
        <StatsCard />
        <ContentCard />
        <MonetizationCard />
        <ActivityCard />
      </div>

      <!-- PRAWA KOLUMNA (Węższa) -->
      <div class="w-full md:w-[360px] flex flex-col gap-4 shrink-0">
        <PageStatusCard />
        <FeedbackCard />
      </div>

    </div>
  </div>
</template>
