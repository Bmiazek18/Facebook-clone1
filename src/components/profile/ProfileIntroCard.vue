<template>
  <div class="bg-theme-bg-secondary p-5 rounded-2xl">
    <!-- SEKCJA: Dane osobowe -->
    <div class="flex items-center justify-between">
      <h2 class="font-bold text-theme-text text-[20px]">Dane osobowe</h2>
      <button
        v-if="isOwner"
        @click="handleEdit('personal')"
        class="p-2 hover:bg-gray-100 rounded-full transition-colors cursor-pointer text-gray-500"
        title="Edytuj dane osobowe"
      >
        <PencilOutline :size="22" />
      </button>
    </div>

    <!-- Kontener na dane z odstępami -->
    <div class="text-[16px] font-medium text-theme-text">
      <!-- Lokalizacja -->
      <div
        @click="openModal('location')"
        class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer text-center"
      >
        <MapMarkerOutline :size="26" class="text-gray-800 shrink-0" />
        <span class="leading-snug"
          >Mieszka w: {{ profileUser?.location || 'Łuków, Siedlce, Poland' }}</span
        >
      </div>

      <!-- Data urodzenia -->
      <div
        @click="openModal('birthday')"
        class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer text-center"
      >
        <CakeVariantOutline :size="26" class="text-gray-800 shrink-0" />
        <span class="leading-snug">{{ formatBirthDate(profileUser?.birthDate) }}</span>
      </div>

      <!-- Status związku -->
      <div
        v-if="profileUser?.relationshipStatus"
        @click="openModal('relationship')"
        class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer text-center"
      >
        <HeartOutline :size="26" class="text-gray-800 shrink-0" />
        <span class="leading-snug">{{ profileUser.relationshipStatus }}</span>
      </div>
    </div>

    <!-- SEKCJA: Wykształcenie -->
    <div class="flex items-center justify-between pt-2 border-t border-gray-100">
      <h2 class="font-bold text-theme-text text-[20px]">Wykształcenie</h2>
      <button
        v-if="isOwner"
        @click="handleEdit('education')"
        class="p-2 hover:bg-gray-100 rounded-full transition-colors cursor-pointer text-gray-500"
        title="Edytuj wykształcenie"
      >
        <PencilOutline :size="22" />
      </button>
    </div>

    <div
      @click="openModal('education')"
      class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer text-center"
    >
      <div
        class="w-11 h-11 rounded-full bg-white flex items-center border border-gray-200 overflow-hidden shrink-0"
      >
        <img :src="schoolLogo" alt="School Logo" class="object-cover w-full h-full" />
      </div>
      <span class="font-medium text-[16px] text-theme-text leading-snug">
        {{
          profileUser?.school || 'Zespół Szkół nr 3 im. Władysława Stanisława Reymonta w Łukowie'
        }}
      </span>
    </div>
  </div>

  <!-- Główny modal do wyświetlania szczegółów -->
  <BaseModal v-if="isModalOpen" @close="closeModal">
    <!-- Renderujemy komponent z listą urodzin z poprzedniego kroku -->
    <BirthdayModal v-if="modalType === 'birthday'" @close="closeModal" />

    <!-- Miejsce na kolejne komponenty (np. LocationModal), póki co wyświetlamy placeholder -->
    <div v-else-if="modalType === 'location'" class="p-6 bg-white rounded-xl text-center">
      <h3 class="font-bold text-lg mb-2">Lokalizacja</h3>
      <p class="text-gray-600">
        Tutaj pojawi się komponent dla lokalizacji (np. mapa lub znajomi z okolicy).
      </p>
      <button @click="closeModal" class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg">
        Zamknij
      </button>
    </div>

    <div v-else class="p-6 bg-white rounded-xl text-center">
      <p class="text-gray-600">Szczegóły wkrótce...</p>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'nuxt/app'

// Import ikon
import MapMarkerOutline from 'vue-material-design-icons/MapMarkerOutline.vue'
import CakeVariantOutline from 'vue-material-design-icons/CakeVariantOutline.vue'
import HeartOutline from 'vue-material-design-icons/HeartOutline.vue'
import PencilOutline from 'vue-material-design-icons/PencilOutline.vue'

// Import Twoich komponentów modali
import BaseModal from '@/components/common/BaseModal.vue'
import BirthdayModal from './BirthdayModal.vue' // <-- Komponent z poprzedniego kroku

const props = defineProps<{
  profileUser: any
  isOwner: boolean
}>()

const router = useRouter()
const route = useRoute()

// Stan dla modala
const isModalOpen = ref(false)
const modalType = ref('')

// Funkcja otwierająca modal dla konkretnego wiersza
const openModal = (type: string) => {
  modalType.value = type
  isModalOpen.value = true
}

// Funkcja zamykająca modal
const closeModal = () => {
  isModalOpen.value = false
  modalType.value = ''
}

const schoolLogo = computed(() => {
  if (props.profileUser?.school === 'AWF Biała Podlaska' || !props.profileUser?.school) {
    return 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Logo_AWF_Warszawa.png/600px-Logo_AWF_Warszawa.png'
  }
  return 'https://via.placeholder.com/40'
})

const formatBirthDate = (dateStr?: string) => {
  if (!dateStr) return '23 lutego 2005'
  try {
    const dateObj = new Date(dateStr)
    if (isNaN(dateObj.getTime())) return dateStr
    return dateObj.toLocaleDateString('pl-PL', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch (e) {
    return dateStr
  }
}

const handleEdit = (section: 'personal' | 'education') => {
  if (!props.isOwner) return
  const userId = route.params.userId
  const basePath = userId ? `/profile/${userId}/info` : '/profile/info'

  if (section === 'personal') {
    router.push(`${basePath}/directory_personal_details`)
  } else {
    router.push(`${basePath}/directory_education`)
  }
}
</script>
