<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex  ">
    <FriendsSidebar />
    <div class="flex-1 ml-[360px] p-8">
      <div class="max-w-[1400px] mx-auto">
        <!-- Profile simulator input card -->
        <div class="bg-theme-bg-secondary border border-theme-border rounded-xl p-5 mb-6 shadow-sm">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h2 class="text-[18px] font-bold text-theme-text mb-1">{{ $t('friends.symulacjaProfiluUzytkownika') }}</h2>
              <p class="text-[13px] text-theme-text-secondary">{{ $t('friends.zmienIdPonizejAby') }}</p>
            </div>
            <div class="flex items-center gap-3">
              <label for="userIdInput" class="text-[14px] font-semibold text-theme-text shrink-0">{{ $t('friends.twojeId') }}</label>
              <input
                id="userIdInput"
                type="text"
                v-model="currentUserId"
                @change="onUserIdChange"
                class="w-48 bg-theme-bg-tertiary border border-theme-border rounded-lg px-2.5 py-1.5 text-theme-text text-center focus:outline-none focus:ring-2 focus:ring-[#1877f2] font-semibold"
              />
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold text-theme-text">{{ t('friends.suggestions') }}</h2>
        </div>

        <div v-if="isLoading" class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[#1877f2]"></div>
        </div>

        <div
          v-else-if="people.length === 0"
          class="bg-theme-bg-secondary border border-theme-border rounded-xl p-12 text-center shadow-sm"
        >
          <p class="text-theme-text-secondary text-[16px] mb-2">{{ $t('friends.brakPropozycjiNowychZnajomych') }}</p>
          <p class="text-theme-text-secondary text-[14px]">{{ $t('friends.wszyscyUzytkownicySaJuz') }}</p>
        </div>

        <div
          v-else
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-6 gap-3"
        >
          <FriendCard
            v-for="person in people"
            :key="person.id"
            :person="person"
            variant="suggestion"
            @add="handleAddFriend"
            @remove="(id) => (people = people.filter((p) => p.id !== id))"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import FriendsSidebar from '../components/friends/FriendsSidebar.vue'
import FriendCard from '../components/friends/PeopleYouMayKnowCard.vue'
import { useNotify } from '@/composables/shared/useNotify'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'
import type { Person } from '@/types/Person'

const { t } = useI18n()
const notify = useNotify()
const authStore = useAuthStore()
const config = useRuntimeConfig()

const currentUserId = ref<string | number>(authStore.currentUserId || '1')
const isLoading = ref(false)
const people = ref<Person[]>([])

const onUserIdChange = () => {
  authStore.setCurrentUser(currentUserId.value)
  if (typeof window !== 'undefined') {
    window.location.reload()
  }
}

const fetchSuggestions = async () => {
  isLoading.value = true
  try {
    const suggestions = await usersApi.getFriendSuggestions(currentUserId.value)
    if (!suggestions || suggestions.length === 0) {
      people.value = []
    } else {
      people.value = suggestions.map((s: any) => {
        const u = s.user || {}
        const firstName = u.firstName || ''
        const lastName = u.lastName || ''
        const fullName = `${firstName} ${lastName}`.trim() || `User ${s.userId}`
        const avatarUrl = u.avatarId
          ? `${config.public.storageUrl}/avatars/${u.avatarId}`
          : `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`

        return {
          id: s.userId,
          name: fullName,
          imageUrl: avatarUrl,
          commonFriends: s.mutualFriendsCount || 0,
          isFriend: false,
        }
      })
    }
  } catch (err: any) {
    console.error('Failed to fetch friend suggestions:', err)
    people.value = []
  } finally {
    isLoading.value = false
  }
}

const handleAddFriend = async (id: string | number) => {
  try {
    const res = await usersApi.sendFriendRequest(currentUserId.value, id)
    if (!res?.success) {
      throw new Error(res?.message || 'Failed to send friend request')
    }

    notify.success('Wysłano zaproszenie do grona znajomych!')
    people.value = people.value.filter((p) => p.id !== id)
  } catch (err: any) {
    console.error('Failed to add friend:', err)
    notify.error(`Błąd: ${err.message || 'Nie udało się dodać znajomego'}`)
  }
}

onMounted(() => {
  fetchSuggestions()
})
</script>

<style scoped>
/* Scrollbar styling for FriendsSidebar */
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
