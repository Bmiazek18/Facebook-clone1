<template>
  <div class="mt-2">
    <div
      v-for="contact in contacts"
      :key="contact.id"
      class="flex items-center justify-between p-2 hover:bg-[#F2F4F7] dark:hover:bg-[#3A3B3C] rounded-lg transition duration-150 ease-in-out cursor-pointer mx-2"
    >
      <div class="flex items-center space-x-3 w-full overflow-hidden">
        <div class="relative flex-shrink-0">
          <img
            :src="contact.avatarUrl"
            :alt="contact.name"
            class="w-10 h-10 rounded-full object-cover border border-gray-100 dark:border-gray-700"
            loading="lazy"
          />
        </div>

        <div class="flex flex-col truncate">
          <span class="text-[15px] font-medium text-[#050505] dark:text-[#E4E6EB] truncate">
            {{ contact.name }}
          </span>

          <div class="flex items-center text-[13px] text-[#65676B] dark:text-[#B0B3B8] truncate">
            <span
              v-if="contact.isNew"
              class="block w-2 h-2 bg-blue-500 rounded-full mr-2 flex-shrink-0"
            ></span>

            <span :class="{ 'font-medium text-blue-500': contact.isNew }" class="truncate">
              {{ contact.status }}
            </span>
          </div>
        </div>
      </div>

      <button
        @click.stop="removeContact(contact.id)"
        class="text-[#65676B] dark:text-[#B0B3B8] hover:bg-gray-200 dark:hover:bg-gray-600 p-2 rounded-full transition duration-150 ease-in-out flex-shrink-0 ml-2"
        aria-label="Usuń z historii"
      >
        <Close :size="20" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Close from 'vue-material-design-icons/Close.vue';

// Definicja typu
interface Contact {
  id: number;
  name: string;
  avatarUrl: string;
  status?: string;
  isNew?: boolean; // Czy pokazać niebieską kropkę i niebieski tekst
}

// Dane odwzorowujące Twój zrzut ekranu
const initialContacts: Contact[] = [
  {
    id: 1,
    name: 'Kolegium Sędziów BOZPN',
    avatarUrl: 'https://scontent-waw2-2.xx.fbcdn.net/v/t39.30808-1/326752317_547526970768789_7336691129596307379_n.jpg?stp=cp0_dst-jpg_p40x40&_nc_cat=109&ccb=1-7&_nc_sid=e99d92&_nc_ohc=8wTzE0zPz3MAX-x6Yx_&_nc_ht=scontent-waw2-2.xx&oh=00_AfBv55qXwB4q0XwB4q0XwB4q0XwB4q0XwB4q0XwB4q0Xw&oe=65676B',
    status: 'Odwiedzono stronę' // Domyślny tekst jeśli brak info
  },
  {
    id: 2,
    name: 'Soccersport Futsal Liga Łuków',
    avatarUrl: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/305775317_488243453315663_5613322137599723528_n.jpg?stp=cp0_dst-jpg_p40x40&_nc_cat=102&ccb=1-7&_nc_sid=e99d92&_nc_ohc=abcde12345&_nc_ht=scontent-waw2-1.xx&oh=00_AfD12345&oe=65676B',
    status: '1 nowa',
    isNew: true
  },
  {
    id: 3,
    name: 'Wiktoria Szerszeń',
    avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80',
    status: 'Odwiedzono profil'
  },
  {
    id: 4,
    name: 'Vanessa rojewska',
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80',
    status: 'Jeszcze 9 nowa',
    isNew: true
  },
  {
    id: 5,
    name: 'Bartosz Miazek',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80',
    status: 'Odwiedzono profil'
  },
  {
    id: 6,
    name: 'Adrian Kłosowski',
    avatarUrl: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80',
    status: 'Znajomy'
  },
  {
    id: 7,
    name: 'WRS ETI',
    avatarUrl: 'https://via.placeholder.com/150?text=WRS',
    status: '2 nowe',
    isNew: true
  },
  {
    id: 8,
    name: 'Filip Ścioch',
    avatarUrl: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?ixlib=rb-1.2.1&auto=format&fit=crop&w=100&q=80',
    status: 'Znajomy'
  },
];

const contacts = ref<Contact[]>(initialContacts);

const removeContact = (id: number): void => {
  contacts.value = contacts.value.filter(contact => contact.id !== id);
};
</script>
