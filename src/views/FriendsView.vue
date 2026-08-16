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
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useQuery, useMutation } from '@vue/apollo-composable'
import gql from 'graphql-tag'

import FriendsSidebar from '../components/friends/FriendsSidebar.vue'
import FriendCard from '../components/friends/PeopleYouMayKnowCard.vue'

const { t } = useI18n()
const authStore = useAuthStore()

// Lokalne listy dla optymistycznych aktualizacji interfejsu (np. usuwanie kafelków)
const friendRequests = ref<any[]>([])
const friendSuggestions = ref<any[]>([])

// ==========================================
// 1. Definicje zapytań i mutacji GraphQL
// ==========================================
const GET_FRIEND_REQUESTS = gql`
  query GetFriendRequests($currentUserId: ID!) {
    getFriendRequests(currentUserId: $currentUserId) {
      userId
      mutualFriendsCount
      user {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

const GET_FRIEND_SUGGESTIONS = gql`
  query GetFriendSuggestions($currentUserId: ID!) {
    getFriendSuggestions(currentUserId: $currentUserId) {
      userId
      mutualFriendsCount
      user {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

const ACCEPT_FRIEND_REQUEST = gql`
  mutation AcceptFriendRequest($senderId: ID!, $receiverId: ID!) {
    acceptFriendRequest(senderId: $senderId, receiverId: $receiverId) {
      success
      message
    }
  }
`

const SEND_FRIEND_REQUEST = gql`
  mutation SendFriendRequest($senderId: ID!, $receiverId: ID!) {
    sendFriendRequest(senderId: $senderId, receiverId: $receiverId) {
      success
      message
    }
  }
`

// ==========================================
// 2. Pobieranie danych (Queries)
// ==========================================

// Reaktywne zmienne dla zapytań
const queryVariables = computed(() => ({
  currentUserId: String(authStore.currentUserId),
}))

// Reaktywne opcje sterujące włączaniem zapytania (tylko, gdy mamy ID)
const queryOptions = computed(() => ({
  enabled: !!authStore.currentUserId && String(authStore.currentUserId) !== '0',
  fetchPolicy: 'cache-and-network' as const,
}))

// A) Pobieranie zaproszeń
const { onResult: onRequestsResult, loading: requestsLoading } = useQuery(
  GET_FRIEND_REQUESTS,
  queryVariables,
  queryOptions
)

onRequestsResult((res) => {
  const reqList = res.data?.getFriendRequests || []
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
})

// B) Pobieranie propozycji znajomych
const { onResult: onSuggestionsResult, loading: suggestionsLoading } = useQuery(
  GET_FRIEND_SUGGESTIONS,
  queryVariables,
  queryOptions
)

onSuggestionsResult((res) => {
  const sugList = res.data?.getFriendSuggestions || []
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
})

// ==========================================
// 3. Obsługa akcji (Mutations)
// ==========================================
const { mutate: acceptFriendRequest } = useMutation(ACCEPT_FRIEND_REQUEST)
const { mutate: sendFriendRequest } = useMutation(SEND_FRIEND_REQUEST)

const handleConfirmRequest = async (senderId: string | number) => {
  try {
    const res = await acceptFriendRequest({
      senderId: String(senderId),
      receiverId: String(authStore.currentUserId),
    })

    if (res?.data?.acceptFriendRequest?.success) {
      // Usuwamy załadowany element lokalnie, żeby nie było mrugnięć i odświeżeń całej listy
      friendRequests.value = friendRequests.value.filter((r) => r.id !== senderId)
    }
  } catch (err) {
    console.error('Failed to accept friend request:', err)
  }
}

const handleAddFriend = async (receiverId: string | number) => {
  try {
    const res = await sendFriendRequest({
      senderId: String(authStore.currentUserId),
      receiverId: String(receiverId),
    })

    if (res?.data?.sendFriendRequest?.success) {
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
