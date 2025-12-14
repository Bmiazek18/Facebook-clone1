<template>
  <div class="min-h-screen bg-[#f0f2f5] mt-[50px] flex font-sans">

    <div class="w-[360px] bg-white shadow-lg h-screen fixed left-0 top-[50px] pt-4 px-2 flex flex-col overflow-y-auto z-10">

      <div class="flex justify-between items-center px-2 mb-4">
        <h1 class="text-[24px] font-bold text-[#050505]">Znajomi</h1>
        <div class="bg-[#e4e6eb] w-9 h-9 rounded-full flex items-center justify-center cursor-pointer hover:bg-[#d8dadf] transition-colors">
          <CogIcon :size="20" />
        </div>
      </div>

      <div class="flex flex-col gap-1">

        <div class="flex items-center justify-between px-2 py-2 bg-[#e4e6eb] rounded-lg cursor-pointer">
          <div class="flex items-center gap-3">
            <div class="bg-[#1877f2] rounded-full p-1.5 text-white">
              <AccountGroupIcon :size="20" />
            </div>
            <span class="text-[17px] font-medium text-[#050505]">Strona główna</span>
          </div>
        </div>

        <div v-for="(item, index) in menuItems" :key="index" class="flex items-center justify-between px-2 py-2 hover:bg-[#e4e6eb] rounded-lg cursor-pointer group transition-colors">
          <div class="flex items-center gap-3">
            <div class="bg-[#e4e6eb] rounded-full p-1.5 text-[#050505] group-hover:bg-white transition-colors">
              <component :is="item.icon" :size="20" />
            </div>
            <span class="text-[17px] font-medium text-[#050505]">{{ item.label }}</span>
          </div>
          <ChevronRightIcon :size="24" class="text-[#65676b]" />
        </div>

      </div>
    </div>

    <div class="flex-1 ml-[360px] p-8">

      <div class="max-w-[1400px] mx-auto">

        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold text-[#050505]">Zaproszenia do grona znajomych</h2>
          <a href="#" class="text-[#1877f2] hover:bg-[#f0f2f5] px-2 py-1 rounded text-[15px] font-medium">Pokaż wszystkie</a>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-6 gap-3">

           <FriendCard
            v-for="person in friendRequests"
            :key="person.id"
            :person="person"
            variant="request"
            @add="handleAddFriend"
            @remove="(id) => friendRequests = friendRequests.filter(p => p.id !== id)"
          />


        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
const handleAddFriend = (id: number) => {
  console.log('Wysłano zaproszenie do:', id);
  const person = suggestions.value.find(p => p.id === id);
  if (person) person.isFriend = true; // Zmienia stan przycisku na "Wysłano"
};
// Ikony
import CogIcon from 'vue-material-design-icons/Cog.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'; // Strona główna icon
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue';
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue';
import GiftIcon from 'vue-material-design-icons/Gift.vue';
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import FriendCard from '../components/PeopleYouMayKnowCard.vue'
// --- Menu Data ---
const menuItems = [
  { label: 'Zaproszenia do grona znajomych', icon: AccountPlusIcon },
  { label: 'Propozycje', icon: AccountPlusIcon }, // Używam podobnej ikony dla uproszczenia
  { label: 'Wszyscy znajomi', icon: AccountMultipleIcon },
  { label: 'Urodziny', icon: GiftIcon },
  { label: 'Listy niestandardowe', icon: FormatListBulletedIcon },
];

// --- Mock Data (Dane ze screenshota) ---
const friendRequests = ref([
  {
    id: 1,
    name: 'Patryk Marciniuk',
    mutual: 0,
    imageUrl: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 2,
    name: 'Hubert Rudnik',
    mutual: 78,
    imageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 3,
    name: 'Michał Kudlak',
    mutual: 97,
    imageUrl: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 4,
    name: 'Piotr Kosmala',
    mutual: 11,
    imageUrl: 'https://images.unsplash.com/photo-1557862921-37829c790f19?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 5,
    name: 'Mateusz Nowacki',
    mutual: 12,
    imageUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 6,
    name: 'Nela Gryczka',
    mutual: 2,
    imageUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 7,
    name: 'Maja Trochim',
    mutual: 7,
    imageUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 8,
    name: 'Wojciech Krasuski',
    mutual: 70,
    imageUrl: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 9,
    name: 'Kuba Bielecki',
    mutual: 36,
    imageUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 10,
    name: 'Krystian Celejewski',
    mutual: 26,
    imageUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 11,
    name: 'Julka Przeplatacka',
    mutual: 4,
    imageUrl: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 12,
    name: 'Bartosz Szczepaniak',
    mutual: 8,
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
]);
</script>

<style scoped>
/* Ukrycie paska przewijania w sidebarze dla estetyki (opcjonalne) */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
</style>
