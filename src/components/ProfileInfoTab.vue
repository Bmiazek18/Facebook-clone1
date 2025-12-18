<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { useRoute } from 'vue-router'
  import FriendsSection from './FriendsSection.vue'
  import OverviewSection from './ProfileInfoTab/OverviewSection.vue'
  import WorkEducationSection from './ProfileInfoTab/WorkEducationSection.vue'
  import PlacesSection from './ProfileInfoTab/PlacesSection.vue'
  import ContactBasicSection from './ProfileInfoTab/ContactBasicSection.vue'
  import FamilySection from './ProfileInfoTab/FamilySection.vue'
  import DetailsSection from './ProfileInfoTab/DetailsSection.vue'
  import EventsSection from './ProfileInfoTab/EventsSection.vue'
  import { getUserById } from '@/data/users'

  const route = useRoute()

  // 1. Pobranie ID z URL
  const userIdParam = computed(() => {
    const id = route.params.userId
    return id ? parseInt(id as string, 10) : null
  })

  // 2. Pobranie użytkownika (bez fallbacku do hardcoded default user)
  const profileUser = computed(() => {
    if (userIdParam.value) {
      return getUserById(userIdParam.value)
    }
    return null
  })

  // 3. Obsługa zakładek (nawigacja po lewej stronie)
  const activeTab = ref('overview')

  const menuItems = [
    { id: 'overview', label: 'Przegląd' },
    { id: 'work_edu', label: 'Praca i wykształcenie' },
    { id: 'places', label: 'Wcześniejsze miejsca zamieszkania' },
    { id: 'contact_basic', label: 'Dane kontaktowe i podstawowe informacje' },
    { id: 'family', label: 'Rodzina i związki' },
    { id: 'details', label: 'Informacje szczegółowe' }, // Skrócona nazwa dla czytelności
    { id: 'events', label: 'Wydarzenia z życia' },
  ]

  // Lista znajomych (pozostawiona bez zmian)
  const friendsList = ref([
    { name: 'Natalia Wójcik', mutual: 71, isFriend: true, imageId: 35 },
    { name: 'Kacper Szymański', mutual: 10, isFriend: false, imageId: 36 },
    { name: 'Monika Zawadzka', mutual: 211, isFriend: true, imageId: 37 },
    { name: 'Michał Kowalczyk', mutual: 15, isFriend: false, imageId: 38 },
    { name: 'Ewa Lipińska', mutual: 45, isFriend: true, imageId: 39 },
    { name: 'Marek Pająk', mutual: 8, isFriend: false, imageId: 40 },
    { name: 'Piotr Zieliński', mutual: 99, isFriend: true, imageId: 20 },
    { name: 'Katarzyna Nowak', mutual: 56, isFriend: true, imageId: 21 },
    { name: 'Tomasz Dąbrowski', mutual: 139, isFriend: true, imageId: 22 },
    { name: 'Anna Kozłowska', mutual: 34, isFriend: false, imageId: 23 },
    { name: 'Rafał Woźniak', mutual: 157, isFriend: true, imageId: 24 },
    { name: 'Joanna Błaszczyk', mutual: 142, isFriend: true, imageId: 25 },
    { name: 'Łukasz Cichy', mutual: 144, isFriend: true, imageId: 26 },
    { name: 'Zuzanna Górska', mutual: 114, isFriend: true, imageId: 27 },
    { name: 'Maciej Kamiński', mutual: 52, isFriend: false, imageId: 28 },
    { name: 'Kinga Bartosiewicz', mutual: 38, isFriend: false, imageId: 29 },
    { name: 'Adam Wróbel', mutual: 46, isFriend: false, imageId: 30 },
    { name: 'Justyna Jurek', mutual: 128, isFriend: true, imageId: 31 },
    { name: 'Robert Kubiak', mutual: 89, isFriend: false, imageId: 32 },
    { name: 'Karolina Sęk', mutual: 80, isFriend: false, imageId: 33 },
  ])
</script>

<template>
    <div class="w-full mt-4 overflow-auto">
        <div v-if="profileUser" class="flex bg-theme-bg-secondary p-4 rounded-lg shadow-lg min-h-[400px]">

            <div class="w-1/3 md:w-1/4 border-r border-gray-200 pr-4">
                <h2 class="text-xl font-bold mb-4 text-theme-text ml-2">Informacje</h2>
                <ul class="space-y-1 text-theme-text-secondary">
                    <li
                        v-for="item in menuItems"
                        :key="item.id"
                        @click="activeTab = item.id"
                        class="p-2 rounded-lg cursor-pointer transition-colors"
                        :class="activeTab === item.id
                            ? 'bg-blue-50 text-blue-600 font-bold'
                            : 'hover:bg-gray-100'"
                    >
                        {{ item.id === 'details' ? `Informacje szczegółowe o użytkowniku ${profileUser.name.split(' ')[0]}` : item.label }}
                    </li>
                </ul>
            </div>

            <div class="w-2/3 md:w-3/4 pl-6 text-theme-text-secondary">
                <OverviewSection v-if="activeTab === 'overview'" :profile-user="profileUser" />
                <WorkEducationSection v-else-if="activeTab === 'work_edu'" :profile-user="profileUser" />
                <PlacesSection v-else-if="activeTab === 'places'" :profile-user="profileUser" />
                <ContactBasicSection v-else-if="activeTab === 'contact_basic'" :profile-user="profileUser" />
                <FamilySection v-else-if="activeTab === 'family'" :profile-user="profileUser" />
                <DetailsSection v-else-if="activeTab === 'details'" :profile-user="profileUser" />
                <EventsSection v-else-if="activeTab === 'events'" :profile-user="profileUser" />
            </div>
        </div>

        <div v-else class="p-8 text-center text-gray-500 bg-theme-bg-secondary rounded-lg shadow-lg">
            Nie znaleziono użytkownika lub brak ID w adresie URL.
        </div>

        <FriendsSection
            :friends-list="friendsList"
            :is-full-view="false"
            class="mt-4 border-none shadow-none p-0 bg-transparent"
        />
    </div>
</template>
