<template>
  <div class="bg-white dark:bg-[#3e4042] w-full ">
    <header class="flex justify-between items-center p-4 border-b dark:border-gray-700">
      <h1 class="text-xl font-semibold text-gray-800 dark:text-gray-100">Ostatnie</h1>
      <button class="text-blue-600 font-medium">Edytuj</button>
    </header>

    <div class="space-y-0.5 pb-2">
      <div
        v-for="contact in contacts"
        :key="contact.id"
        class="flex items-center justify-between p-2 hover:bg-gray-200 dark:hover:bg-[#4e5052] transition duration-150 ease-in-out cursor-pointer"
      >
        <div class="flex items-center space-x-3">
          <div class="relative flex-shrink-0">
            <img
              :src="contact.avatarUrl"
              :alt="contact.name"
              class="w-12 h-12 rounded-full object-cover shadow-sm"
              loading="lazy"
            />

            <div
              v-if="contact.newCount && contact.newCount > 0"
              class="absolute bottom-0 right-0 w-3 h-3 bg-blue-600 rounded-full border-2 border-white dark:border-[#3e4042]"
            ></div>
          </div>

          <div class="flex flex-col">
            <span class="text-base font-normal text-gray-800 dark:text-gray-100">{{ contact.name }}</span>
            <span
              v-if="contact.status"
              class="text-sm text-gray-500 dark:text-gray-400"
            >
              {{ contact.status }}
            </span>
          </div>
        </div>

        <button
          @click.stop="removeContact(contact.id)"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 p-2 rounded-full transition duration-150 ease-in-out"
          aria-label="Usuń kontakt"
        >
          <CloseBoxOutlineIcon :size="24" :fillColor="isDark ? '#e4e6eb' : '#65676b'" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue';
// Zakładam, że masz dostęp do Theme Composables, aby dostosować kolor ikon
import { useTheme } from '@/composables/useTheme';

// 1. Definicja Typu (Model Danych)
interface Contact {
  id: number;
  name: string;
  avatarUrl: string;
  status?: 'Znajomy' | 'Inne' | string;
  newCount?: number;
}

const { isDark } = useTheme();

// 2. Wzorcowe Dane (na podstawie obrazka)
// Zastąp "..." faktycznymi URL-ami lub użyj ścieżek do lokalnych zasobów
const initialContacts: Contact[] = [
  { id: 1, name: 'Kolegium Sędziów BOZPN', avatarUrl: 'https://via.placeholder.com/150/007AFF/FFFFFF?text=KS', status: 'Inne' },
  { id: 2, name: 'soccersport futsal liga łuków', avatarUrl: 'https://via.placeholder.com/150/FF9500/FFFFFF?text=F', newCount: 0 },
  { id: 3, name: 'Marcin Chwedoruk', avatarUrl: 'https://via.placeholder.com/150/34C759/FFFFFF?text=M', newCount: 0 },
  { id: 4, name: 'ŁKS Orlęta Łuków', avatarUrl: 'https://via.placeholder.com/150/FF3B30/FFFFFF?text=O', newCount: 0 },
  { id: 5, name: 'LKS Dwernicki Stoczek Łukowski', avatarUrl: 'https://via.placeholder.com/150/5AC8FA/FFFFFF?text=D', newCount: 2 },
  { id: 6, name: 'Wiktoria Szerszeń', avatarUrl: 'https://via.placeholder.com/150/A2845E/FFFFFF?text=W', status: 'Znajomy', newCount: 0 },
  { id: 7, name: 'GKS Armaty Stoczek Łukowski', avatarUrl: 'https://via.placeholder.com/150/C69C6E/FFFFFF?text=G', newCount: 0 },
  { id: 8, name: 'Ar-Tig Huta Dąbrowa', avatarUrl: 'https://via.placeholder.com/150/7F00FF/FFFFFF?text=A', newCount: 2 },
];

const contacts = ref<Contact[]>(initialContacts);

// 3. Logika Usuwania
const removeContact = (id: number): void => {
  contacts.value = contacts.value.filter(contact => contact.id !== id);
};
</script>

<style scoped>
/* Brak dodatkowych stylów, całość oparta na Tailwind */
</style>
