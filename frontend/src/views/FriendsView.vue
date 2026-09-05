<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex">
    <FriendsSidebar />
    <div class="flex-1 ml-[360px] p-8">
      <!-- Sekcja zaproszeń -->
      <div class="max-w-[1400px] mx-auto mb-8">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold text-theme-text">{{ t('friends.friendRequests') }}</h2>
          <span
            class="text-sm text-theme-text-secondary font-medium"
            v-if="friendRequests.length > 0"
          >
            Liczba zaproszeń: {{ friendRequests.length }}
          </span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
          <FriendCard
            v-for="person in friendRequests"
            :key="person.id"
            :person="person"
            variant="request"
            @confirm="handleConfirmRequest"
            @delete="handleDeleteRequest"
          />
        </div>
        <div
          v-if="!requestsLoading && friendRequests.length === 0"
          class="p-6 text-center text-theme-text-secondary bg-theme-bg-secondary rounded-xl border border-theme-border"
        >
          Brak oczekujących zaproszeń do grona znajomych
        </div>
      </div>

      <!-- Sekcja propozycji -->
      <div class="max-w-[1400px] mx-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold text-theme-text">
            Propozycje znajomych (Ludzie, których możesz znać)
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
          <FriendCard
            v-for="person in friendSuggestions"
            :key="person.id"
            :person="person"
            variant="suggestion"
            @add="handleAddFriend"
            @delete="handleDeleteSuggestion"
          />
        </div>
        <div
          v-if="!suggestionsLoading && friendSuggestions.length === 0"
          class="p-6 text-center text-theme-text-secondary bg-theme-bg-secondary rounded-xl border border-theme-border"
        >
          Brak nowych propozycji znajomych
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'

import FriendsSidebar from '../components/friends/FriendsSidebar.vue'
import FriendCard from '../components/friends/PeopleYouMayKnowCard.vue'

const { t } = useI18n()
const authStore = useAuthStore()

// Lokalne listy dla optymistycznych aktualizacji interfejsu (np. usuwanie kafelków)
const friendRequests = ref<any[]>([])
const friendSuggestions = ref<any[]>([])
const requestsLoading = ref(false)
const suggestionsLoading = ref(false)

const fetchRequests = async () => {
  if (!authStore.currentUserId || String(authStore.currentUserId) === '0') return
  requestsLoading.value = true
  try {
    const reqList = await usersApi.getFriendRequests(authStore.currentUserId)
    friendRequests.value = reqList.map((item: any) => {
      const u = item.user
      const avatarUrl = u?.avatar
        || `https://ui-avatars.com/api/?name=${encodeURIComponent(
            (u?.firstName || '') + ' ' + (u?.lastName || '')
          )}&background=EBF4FF&color=1877F2&bold=true`

      return {
        id: item.userId,
        name: u ? `${u.firstName} ${u.lastName}` : `User ${item.userId}`,
        commonFriends: item.mutualFriendsCount || 0,
        imageUrl: avatarUrl,
        isFriend: false,
      }
    })
  } catch (err) {
    console.error('Failed to fetch friend requests:', err)
  } finally {
    requestsLoading.value = false
  }
}

const fetchSuggestions = async () => {
  if (!authStore.currentUserId || String(authStore.currentUserId) === '0') return
  suggestionsLoading.value = true
  try {
    const sugList = await usersApi.getFriendSuggestions(authStore.currentUserId)
    friendSuggestions.value = sugList.map((item: any) => {
      const u = item.user
      const avatarUrl = u?.avatar
        || `https://ui-avatars.com/api/?name=${encodeURIComponent(
            (u?.firstName || '') + ' ' + (u?.lastName || '')
          )}&background=EBF4FF&color=1877F2&bold=true`

      return {
        id: item.userId,
        name: u ? `${u.firstName} ${u.lastName}` : `User ${item.userId}`,
        commonFriends: item.mutualFriendsCount || 0,
        imageUrl: avatarUrl,
        isFriend: false,
      }
    })
  } catch (err) {
    console.error('Failed to fetch friend suggestions:', err)
  } finally {
    suggestionsLoading.value = false
  }
}

onMounted(() => {
  fetchRequests()
  fetchSuggestions()
})

watch(() => authStore.currentUserId, () => {
  fetchRequests()
  fetchSuggestions()
})

const handleConfirmRequest = async (senderId: string | number) => {
  try {
    const res = await usersApi.acceptFriendRequest(senderId, authStore.currentUserId)

    if (res?.success) {
      // Usuwamy załadowany element lokalnie, żeby nie było mrugnięć i odświeżeń całej listy
      friendRequests.value = friendRequests.value.filter((r) => r.id !== senderId)
    }
  } catch (err) {
    console.error('Failed to accept friend request:', err)
  }
}

const handleAddFriend = async (receiverId: string | number) => {
  try {
    const res = await usersApi.sendFriendRequest(authStore.currentUserId, receiverId)

    if (res?.success) {
      // Usuwamy załadowany element lokalnie z rekomendacji po pomyślnym wysłaniu zaproszenia
      friendSuggestions.value = friendSuggestions.value.filter((s) => s.id !== receiverId)
    }
  } catch (err) {
    console.error('Failed to send friend request:', err)
  }
}

// Funkcje "czyszczące" (np. użytkownik klika 'Usuń zaproszenie' w UI, aby go nie widzieć na froncie)
const handleDeleteRequest = (id: string | number) => {
  friendRequests.value = friendRequests.value.filter((r) => r.id !== id)
}

const handleDeleteSuggestion = (id: string | number) => {
  friendSuggestions.value = friendSuggestions.value.filter((s) => s.id !== id)
}
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
