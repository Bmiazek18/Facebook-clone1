<script setup lang="ts">
import { computed } from 'vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import ViewDashboard from 'vue-material-design-icons/ViewDashboard.vue'
import TagMultiple from 'vue-material-design-icons/TagMultiple.vue'
import Bell from 'vue-material-design-icons/Bell.vue'
import ChartBar from 'vue-material-design-icons/ChartBar.vue'

const sidebarItems = computed(() => [
  { icon: ViewDashboard, text: 'Pulpit sprzedawcy', route: '/marketplace/you/dashboard' },
  { icon: TagMultiple, text: 'Twoje ogłoszenia', route: '/marketplace/you/listings' },
  { icon: Bell, text: 'Powiadomienia', route: '/marketplace/you/notifications' },
  { icon: ChartBar, text: 'Statystyki', route: '/marketplace/you/insights' },
  { text: 'Profil w Marketplace', route: '/marketplace/you/profile', avatar: 'https://i.pravatar.cc/150?img=11' },
])
</script>

<template>
  <Sidebar
    title="Sprzedaż"
    backRoute="/marketplace"
    :showSearch="false"
    :items="sidebarItems"
    :createButton="{
      icon: Plus,
      text: 'Utwórz nowe ogłoszenie',
      route: '/marketplace/create/item',
    }"
  />
</template>
