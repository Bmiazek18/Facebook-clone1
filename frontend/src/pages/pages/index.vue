<template>
  <div class="min-h-screen bg-[#F0F2F5] p-6 font-sans flex gap-6">

    <Sidebar
      :title="$t('home.pages')"
      :items="sidebarItems"
      :createButton="createPageBtn"
    />

    <div class="max-w-[1080px] mx-auto w-full flex-1">
      <component :is="activeComponent" />
    </div>
  </div>
  <BaseModal v-if="isModalOpen" @close="isModalOpen = false">
    <SelectOption />
  </BaseModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

// Importujemy ikony dla sidebara
import CompassIcon from 'vue-material-design-icons/Compass.vue'
import FolderMultipleIcon from 'vue-material-design-icons/FolderMultiple.vue'
import AccountMultiplePlusIcon from 'vue-material-design-icons/AccountMultiplePlus.vue'
// Dodajemy ikonę "Plusa" dla przycisku tworzenia
import PlusIcon from 'vue-material-design-icons/Plus.vue'

// Importujemy nasze wydzielone widoki i komponenty
import PagesDiscover from '~/components/pages/Discover.vue'
import PagesLiked from '~/components/pages/Liked.vue'
import PagesInvites from '~/components/pages/Invites.vue'
import Sidebar from '~/components/common/Sidebar.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import SelectOption from '~/components/pages/modal/SelectOption.vue'

const route = useRoute()
const currentView = computed(() => route.query.category || route.query.tab || 'top')
const isModalOpen = ref(false) // Stan modala dla wyboru opcji
// Mapowanie wartości z URL na konkretny komponent
const activeComponent = computed(() => {
  switch (currentView.value) {
    case 'liked':
      return PagesLiked
    case 'invites':
      return PagesInvites
    case 'top':
    default:
      return PagesDiscover
  }
})

// Konfiguracja elementów sidebara
const sidebarItems = computed(() => [
  {
    text: 'Odkryj',
    icon: CompassIcon,
    route: '?category=top',
    active: currentView.value === 'top'
  },
  {
    text: 'Followed Pages',
    icon: FolderMultipleIcon,
    route: '?category=liked',
    active: currentView.value === 'liked'
  },
  {
    text: 'Zaproszenia',
    icon: AccountMultiplePlusIcon,
    route: '?category=invites',
    active: currentView.value === 'invites',
    secondaryText: '21 nowych',
    showNotificationDot: true
  }
])

// Konfiguracja przycisku "Utwórz stronę"
const createPageBtn = {
  text: 'Utwórz nową stronę',
  icon: PlusIcon,
  // Możesz użyć 'route', aby po kliknięciu przenieść użytkownika pod konkretny adres:
  // route: '/pages/create',

  // ALBO użyć 'action', aby wywołać funkcję (np. otworzyć modal):
  action: () => {
    isModalOpen.value = true
  }
}
</script>
