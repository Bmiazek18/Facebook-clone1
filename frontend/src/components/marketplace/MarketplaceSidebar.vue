<script setup lang="ts">
import { computed } from 'vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import ViewDashboard from 'vue-material-design-icons/ViewDashboard.vue'
import TagMultiple from 'vue-material-design-icons/TagMultiple.vue'
import Bell from 'vue-material-design-icons/Bell.vue'
import ChartBar from 'vue-material-design-icons/ChartBar.vue'
import { useAuthStore } from '~/stores/auth'
import BaseModal from '../common/BaseModal.vue'
import SellerModal from './modals/SellerModal.vue'
import ProfileModal from './modals/ProfileModal.vue'
const authStore = useAuthStore()
const isProfileModalOpen = ref(false)

const openProfileModal = () => {
  isProfileModalOpen.value = true
}
const sidebarItems = computed(() => [
  { icon: ViewDashboard, text: 'Pulpit sprzedawcy', route: '/marketplace/you/dashboard' },
  { icon: TagMultiple, text: 'Twoje ogłoszenia', route: '/marketplace/you/listings' },
  { icon: Bell, text: 'Powiadomienia', route: '/marketplace/you/notifications' },
  { icon: ChartBar, text: 'Statystyki', route: '/marketplace/you/insights' },
  {
    text: 'Profil w Marketplace',
    avatar: authStore.currentUser?.avatar || '',
    // Usuwamy "route", a zamiast niego dajemy "action"
    action: openProfileModal
  },
])
</script>

<template>
  <Sidebar
    :title="$t('marketplace.sprzedaz')"
    backRoute="/marketplace"
    :showSearch="false"
    :items="sidebarItems"
    :createButton="{
      icon: Plus,
      text: 'Utwórz nowe ogłoszenie',
      route: '/marketplace/create/item',
    }"
  />
  <BaseModal v-if="isProfileModalOpen" @close="isProfileModalOpen = false" :title="$t('marketplace.profilWMarketplace')">
    <ProfileModal/>
  </BaseModal>
</template>
