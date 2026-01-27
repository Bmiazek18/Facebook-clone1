<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex font-sans">
    <FriendsSidebar />
    <div class="flex-1 ml-[360px] p-8">
      <div class="max-w-[1400px] mx-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold text-theme-text">{{ t('friends.friendRequests') }}</h2>
          <a href="#" class="text-[#1877f2] hover:bg-theme-bg-tertiary px-2 py-1 rounded text-[15px] font-medium">{{ t('friends.showAll') }}</a>
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
import { useI18n } from 'vue-i18n';
import FriendsSidebar from '../components/friends/FriendsSidebar.vue';
import FriendCard from '../components/friends/PeopleYouMayKnowCard.vue';

const { t } = useI18n();

const handleAddFriend = (id: number) => {
  console.log(t('friends.sentFriendRequestTo'), id);
  const person = friendRequests.value.find((p: { id: number }) => p.id === id);
  if (person) person.isFriend = true;
};

// --- Mock Data (Dane ze screenshota) ---
const friendRequests = ref([
  {
    id: 1,
    name: 'Patryk Marciniuk',
    commonFriends: 0,
    imageUrl: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 2,
    name: 'Hubert Rudnik',
    commonFriends: 78,
    imageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 3,
    name: 'Michał Kudlak',
    commonFriends: 97,
    imageUrl: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 4,
    name: 'Piotr Kosmala',
    commonFriends: 11,
    imageUrl: 'https://images.unsplash.com/photo-1557862921-37829c790f19?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 5,
    name: 'Mateusz Nowacki',
    commonFriends: 12,
    imageUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 6,
    name: 'Nela Gryczka',
    commonFriends: 2,
    imageUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 7,
    name: 'Maja Trochim',
    commonFriends: 7,
    imageUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 8,
    name: 'Wojciech Krasuski',
    commonFriends: 70,
    imageUrl: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 9,
    name: 'Kuba Bielecki',
    commonFriends: 36,
    imageUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 10,
    name: 'Krystian Celejewski',
    commonFriends: 26,
    imageUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 11,
    name: 'Julka Przeplatacka',
    commonFriends: 4,
    imageUrl: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
    isFriend: false
  },
  {
    id: 12,
    name: 'Bartosz Szczepaniak',
    commonFriends: 8,
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
