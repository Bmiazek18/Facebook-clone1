<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex">
    <FriendsSidebar />
    <div class="flex-1 ml-[360px] p-8">
      <!-- Zawartość wyświetla się tylko gdy dane są załadowane (brak napisów/kart w trakcie wczytywania) -->
      <template v-if="friendsStore.isLoaded">
        <!-- Sekcja zaproszeń -->
        <div class="max-w-[1400px] mx-auto mb-8">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-[20px] font-bold text-theme-text">{{ t('friends.friendRequests') }}</h2>
            <span
              class="text-sm text-theme-text-secondary font-medium"
              v-if="friendsStore.friendRequests.length > 0"
            >{{ $t('friends.liczbaZaproszenFriendrequestsLength') }}</span>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
            <FriendCard
              v-for="person in friendsStore.friendRequests"
              :key="person.id"
              :person="person"
              variant="request"
              @confirm="friendsStore.confirmRequest"
              @delete="friendsStore.deleteRequest"
            />
          </div>
          <div
            v-if="friendsStore.friendRequests.length === 0"
            class="p-6 text-center text-theme-text-secondary bg-theme-bg-secondary rounded-xl border border-theme-border"
          >{{ $t('friends.brakOczekujacychZaproszenDo') }}</div>
        </div>

        <!-- Sekcja propozycji -->
        <div class="max-w-[1400px] mx-auto">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-[20px] font-bold text-theme-text">{{ $t('friends.propozycjeZnajomychLudzieKtorych') }}</h2>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
            <FriendCard
              v-for="person in friendsStore.friendSuggestions"
              :key="person.id"
              :person="person"
              variant="suggestion"
              @add="friendsStore.addSuggestion"
              @delete="friendsStore.deleteSuggestion"
            />
          </div>
          <div
            v-if="friendsStore.friendSuggestions.length === 0"
            class="p-6 text-center text-theme-text-secondary bg-theme-bg-secondary rounded-xl border border-theme-border"
          >{{ $t('friends.brakNowychPropozycjiZnajomych') }}</div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useFriendsStore } from '@/stores/friends'

import FriendsSidebar from '@/components/friends/FriendsSidebar.vue'
import FriendCard from '@/components/friends/PeopleYouMayKnowCard.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const friendsStore = useFriendsStore()

onMounted(() => {
  friendsStore.fetchFriendsData()
})

watch(() => authStore.currentUserId, () => {
  friendsStore.fetchFriendsData(true)
})
</script>

<style scoped>
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
