<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex">
    <FriendsSidebar />
    <div class="flex-1 ml-[360px] p-8">
      <!-- WIDOK FILTROWANEJ LISTY ZNAJOMYCH (gdy przekazano filterType) -->
      <template v-if="filterType">
        <div class="max-w-[1400px] mx-auto">
          <!-- Nagłówek i wyszukiwarka -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h1 class="text-[24px] font-bold text-theme-text">{{ pageTitle }}</h1>
              <p v-if="!isLoadingFriends" class="text-sm text-theme-text-secondary mt-0.5">
                {{ $t('friends.liczbaZnajomych', { count: filteredFriends.length }) }}
              </p>
            </div>
            <div class="w-full sm:w-72">
              <SearchInput v-model="searchQuery" :placeholder="$t('common.search')" />
            </div>
          </div>

          <!-- Stan ładowania -->
          <div v-if="isLoadingFriends" class="flex justify-center items-center py-20">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[#1877f2]"></div>
          </div>

          <!-- Brak znajomych w tej sekcji -->
          <div
            v-else-if="filteredFriends.length === 0"
            class="p-12 text-center text-theme-text-secondary bg-theme-bg-secondary rounded-xl border border-theme-border shadow-sm"
          >
            <p class="text-[16px] font-semibold mb-1">{{ $t('friends.brakZnajomychDoWyswietlenia') }}</p>
            <p class="text-[14px]">{{ emptyStateMessage }}</p>
          </div>

          <!-- Siatka znajomych -->
          <div v-else class="flex flex-wrap -mx-2">
            <FriendListItem
              v-for="friend in filteredFriends"
              :key="friend.id"
              :friend="friend"
            />
          </div>
        </div>
      </template>

      <!-- DOMYŚLNY WIDOK STRONY GŁÓWNEJ ZNAJOMYCH (zaproszenia + propozycje) -->
      <template v-else-if="friendsStore.isLoaded">
        <!-- Sekcja zaproszeń -->
        <div class="max-w-[1400px] mx-auto mb-8">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-[20px] font-bold text-theme-text">{{ t('friends.friendRequests') }}</h2>
            <span
              class="text-sm text-theme-text-secondary font-medium"
              v-if="friendsStore.friendRequests.length > 0"
            >{{ $t('friends.liczbaZaproszenFriendrequestsLength', { count: friendsStore.friendRequests.length }) }}</span>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useFriendsStore } from '@/stores/friends'
import { usersApi } from '@/api/users'

import FriendsSidebar from '@/components/friends/FriendsSidebar.vue'
import FriendCard from '@/components/friends/PeopleYouMayKnowCard.vue'
import FriendListItem from '@/components/friends/FriendListItem.vue'
import SearchInput from '@/components/common/SearchInput.vue'

const props = withDefaults(
  defineProps<{
    title?: string
    filterType?: 'ALL' | 'RECENT' | 'BIRTHDAYS' | 'HIGH_SCHOOL' | 'CURRENT_CITY' | 'FOLLOWING'
  }>(),
  {
    title: '',
    filterType: undefined,
  }
)

const { t } = useI18n()
const authStore = useAuthStore()
const friendsStore = useFriendsStore()

const searchQuery = ref('')
const rawFriends = ref<any[]>([])
const isLoadingFriends = ref(false)

const pageTitle = computed(() => {
  if (props.title) return props.title
  switch (props.filterType) {
    case 'RECENT': return t('friends.niedawnoDodani')
    case 'BIRTHDAYS': return t('friends.birthdays')
    case 'HIGH_SCHOOL': return t('friends.highSchool')
    case 'CURRENT_CITY': return t('friends.aktualneMiejsceZamieszkania')
    case 'FOLLOWING': return t('friends.following')
    case 'ALL': return t('friends.allFriends')
    default: return t('friends.allFriends')
  }
})

const emptyStateMessage = computed(() => {
  switch (props.filterType) {
    case 'HIGH_SCHOOL': return 'Brak znajomych ze szkoły średniej.'
    case 'CURRENT_CITY': return 'Brak znajomych z Twojego aktualnego miejsca zamieszkania.'
    case 'BIRTHDAYS': return 'Brak informacji o urodzinach Twoich znajomych.'
    case 'RECENT': return 'Brak niedawno dodanych znajomych.'
    case 'FOLLOWING': return 'Nie obserwujesz jeszcze żadnych znajomych.'
    default: return 'Nie masz jeszcze żadnych znajomych.'
  }
})

const myCity = computed(() => {
  const user = authStore.currentUser as any
  return (user?.city || user?.location || user?.hometown || '').trim().toLowerCase()
})

const myHighSchool = computed(() => {
  const user = authStore.currentUser as any
  return (user?.highSchool || user?.school || user?.education || '').trim().toLowerCase()
})

const fetchFilteredFriends = async () => {
  if (!props.filterType || !authStore.currentUserId) return
  isLoadingFriends.value = true
  try {
    const list = await usersApi.getFriends(String(authStore.currentUserId), props.filterType)
    if (list && Array.isArray(list)) {
      rawFriends.value = list.map((f: any) => ({
        id: f.id,
        name: [f.firstName, f.lastName].filter(Boolean).join(' ') || 'Użytkownik',
        avatar: f.avatar || '',
        birthDate: f.birthDate || '',
        city: f.city || '',
        location: f.location || '',
        hometown: f.hometown || '',
        school: f.school || '',
        highSchool: f.highSchool || '',
        work: f.work || f.job || '',
        isFriend: true,
        mutual: f.mutualCount ?? 0,
        imageId: 35,
      }))
    } else {
      rawFriends.value = []
    }
  } catch (err) {
    console.error('Failed to fetch filtered friends:', err)
  } finally {
    isLoadingFriends.value = false
  }
}

const filteredFriends = computed(() => {
  let list = [...rawFriends.value]

  if (props.filterType === 'BIRTHDAYS') {
    const withBirthdays = list.filter((f) => f.birthDate && f.birthDate.trim().length > 0)
    list = (withBirthdays.length > 0 ? withBirthdays : list).map((f) => ({
      ...f,
      subtitle: f.birthDate ? `Urodziny: ${f.birthDate}` : 'Urodziny',
    }))
  } else if (props.filterType === 'HIGH_SCHOOL') {
    const target = myHighSchool.value
    const matchSchool = list.filter((f) => {
      const hs = (f.highSchool || f.school || '').toLowerCase()
      return target ? hs.includes(target) : hs.length > 0
    })
    list = (matchSchool.length > 0 ? matchSchool : list.filter(f => (f.highSchool || f.school))).map((f) => ({
      ...f,
      subtitle: `Szkoła: ${f.highSchool || f.school || 'Szkoła średnia'}`,
    }))
  } else if (props.filterType === 'CURRENT_CITY') {
    const target = myCity.value
    const matchCity = list.filter((f) => {
      const c = (f.city || f.location || f.hometown || '').toLowerCase()
      return target ? c.includes(target) : c.length > 0
    })
    list = (matchCity.length > 0 ? matchCity : list.filter(f => (f.city || f.location || f.hometown))).map((f) => ({
      ...f,
      subtitle: `Mieszka w: ${f.city || f.location || f.hometown || ''}`,
    }))
  } else if (props.filterType === 'RECENT') {
    list = list.slice().reverse().map((f) => ({
      ...f,
      subtitle: 'Dodano niedawno',
    }))
  } else if (props.filterType === 'FOLLOWING') {
    list = list.map((f) => ({
      ...f,
      subtitle: 'Obserwowany',
    }))
  }

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter((f) => f.name.toLowerCase().includes(q))
  }

  return list
})

onMounted(() => {
  if (props.filterType) {
    fetchFilteredFriends()
  } else {
    friendsStore.fetchFriendsData()
  }
})

watch(() => [props.filterType, authStore.currentUserId], () => {
  if (props.filterType) {
    fetchFilteredFriends()
  } else {
    friendsStore.fetchFriendsData(true)
  }
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
