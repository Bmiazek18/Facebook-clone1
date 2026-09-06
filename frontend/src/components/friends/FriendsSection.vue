<script setup lang="ts">
import FriendListItem from '@/components/friends/FriendListItem.vue'
import SearchInput from '@/components/common/SearchInput.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'
import { ref, inject, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from '#app'
import { usersApi } from '@/api/users'

const route = useRoute()
const router = useRouter()
const searchQuery = ref('')
const showFriendsModal = ref(false)
const profileUser: any = inject('profileUser', null)
const isOwner = inject('isOwner', false)

const props = withDefaults(
  defineProps<{
    friendsList?: {
      id?: string | number
      name: string
      mutual?: number
      isFriend?: boolean
      imageId?: number
      avatar?: string
    }[]
    isFullView?: boolean // false = Info Tab Preview, true = Full Friends Tab
    pageType?: string
  }>(),
  {
    friendsList: () => [],
    isFullView: false,
    pageType: '',
  }
)

const internalFriends = ref<any[]>([])
const isLoading = ref(false)

const targetUserId = computed(() => {
  return (profileUser?.value?.id || route.params.userId) as string
})

const effectiveFriendsList = computed(() => {
  if (props.friendsList && props.friendsList.length > 0) {
    return props.friendsList
  }
  return internalFriends.value
})

const currentPageType = computed(() => {
  if (props.pageType) return props.pageType
  const seg = route.path.replace(/^\//, '').split('/')
  const last = seg[seg.length - 1]
  if (
    [
      'friends_all',
      'friends_recent',
      'friends_birthdays',
      'friends_high_school',
      'friends_current_city',
      'following',
    ].includes(last)
  ) {
    return last
  }
  return 'friends_all'
})

const myCity = computed(() => {
  return (
    profileUser?.value?.city ||
    profileUser?.value?.location ||
    profileUser?.value?.hometown ||
    ''
  )
    .trim()
    .toLowerCase()
})

const myHighSchool = computed(() => {
  return (
    profileUser?.value?.highSchool ||
    profileUser?.value?.school ||
    profileUser?.value?.education ||
    ''
  )
    .trim()
    .toLowerCase()
})

const apiFilterType = computed(() => {
  if (currentPageType.value === 'friends_high_school') return 'HIGH_SCHOOL'
  if (currentPageType.value === 'friends_current_city') return 'CURRENT_CITY'
  if (currentPageType.value === 'friends_birthdays') return 'BIRTHDAYS'
  return 'ALL'
})

const filteredFriendsList = computed(() => {
  let list = effectiveFriendsList.value

  // Add subtitle formatting based on tab when in full view
  if (props.isFullView) {
    if (currentPageType.value === 'friends_birthdays') {
      list = list.map((f) => ({
        ...f,
        subtitle: f.birthDate ? `Urodziny: ${f.birthDate}` : '',
      }))
    } else if (currentPageType.value === 'friends_high_school') {
      list = list.map((f) => ({
        ...f,
        subtitle: `Szkoła: ${f.highSchool || f.school || 'Szkoła średnia'}`,
      }))
    } else if (currentPageType.value === 'friends_current_city') {
      list = list.map((f) => ({
        ...f,
        subtitle: `Mieszka w: ${f.city || f.location || f.hometown || ''}`,
      }))
    }
  }

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter((f) => f.name.toLowerCase().includes(q))
  }

  return list
})

const fetchFriends = async () => {
  if (!targetUserId.value) return
  isLoading.value = true
  try {
    const filter = props.isFullView ? apiFilterType.value : 'ALL'
    const list = await usersApi.getFriends(targetUserId.value, filter)
    if (list && Array.isArray(list) && list.length > 0) {
      internalFriends.value = list.map((f: any) => ({
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
      internalFriends.value = []
    }
  } catch (err) {
    console.error('Failed to fetch friends in FriendsSection:', err)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  if (!props.friendsList || props.friendsList.length === 0) {
    fetchFriends()
  }
})

watch([targetUserId, currentPageType], () => {
  if (!props.friendsList || props.friendsList.length === 0) {
    fetchFriends()
  }
})

const goToAllFriends = () => {
  const uid = targetUserId.value
  if (uid) {
    router.push(`/profile/${uid}/friends_all`)
  } else {
    router.push('/profile/friends_all')
  }
}

const getFriendsRoute = (type: string) => {
  const uid = targetUserId.value
  return uid ? `/profile/${uid}/${type}` : `/profile/${type}`
}

const settings = ref([
  {
    title: 'Lista znajomych',
    subtitle: 'Kto może zobaczyć Twoją listę znajomych',
    description:
      'Pamiętaj, że Twoi znajomi kontrolują widoczność swoich znajomości na własnych osiach czasu. Jeżeli użytkownicy zobaczą Twoją znajomość na innej osi czasu, będą mogli zobaczyć ją w Aktualnościach, wynikach wyszukiwania i innych miejscach na Facebooku. Jeśli wybierzesz ustawienie „Tylko ja”, tylko Ty będziesz mieć możliwość zobaczenia pełnej listy znajomych na swojej osi czasu. Inne osoby będą widzieć wyłącznie wspólnych znajomych.',
    privacyType: 'friends',
    privacyLabel: 'Znajomi',
  },
  {
    title: 'Obserwowanie',
    subtitle: 'Kto może zobaczyć osoby i strony, które obserwujesz?',
    description: 'Pamiętaj! Obserwowane przez Ciebie osoby mogą zobaczyć, że je obserwujesz.',
    privacyType: 'public',
    privacyLabel: 'Wszyscy',
  },
  {
    title: 'Obserwujący',
    subtitle: 'Kto może zobaczyć osoby Cię obserwujące na Twojej osi czasu?',
    description: '',
    privacyType: 'friends',
    privacyLabel: 'Włączone',
  },
])

const selectedSetting = ref<any>(null)

const handlePrivacyClick = (item: any) => {
  selectedSetting.value = item
}

const handleBack = () => {
  selectedSetting.value = null
}

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  if (selectedSetting.value) {
    selectedSetting.value.privacyType = payload.id
    selectedSetting.value = null
  }
}
</script>

<template>
  <div
    class="bg-theme-bg-secondary p-4 mt-4 rounded-lg shadow-lg"
    :class="{ 'shadow-lg border border-theme-border': !isFullView }"
  >
    <div class="flex justify-between font-semibold items-center mb-4 pb-3">
      <h2 class="text-theme-text text-xl">{{ $t('postFilter.privacyFriends') }}</h2>

      <!-- Dodatkowe elementy nagłówka dla FullView -->
      <div v-if="isFullView" class="flex items-center space-x-4">
        <div class="relative w-48">
          <SearchInput v-model="searchQuery" :placeholder="$t('common.search')" />
        </div>
        <template v-if="isOwner && isFullView">
          <a class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
            >{{ $t('friends.friendRequests') }}</a
          >
          <a class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
            >{{ $t('common.szukajZnajomych') }}</a
          >

          <button
            @click="showFriendsModal = true"
            class="bg-theme-bg-subtle hover:bg-theme-hover px-3 py-1.5 rounded-lg text-theme-text font-bold"
          >
            ...
          </button>
        </template>
      </div>

      <a
        v-if="!isFullView"
        @click="goToAllFriends"
        class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
        >{{ $t('friends.allFriends') }}</a
      >
    </div>

    <div
      class="flex space-x-6 mb-4 text-theme-text-secondary border-b border-theme-border overflow-x-auto whitespace-nowrap"
    >
      <!-- Wszyscy znajomi -->
      <NuxtLink
        :to="getFriendsRoute('friends_all')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('friends.allFriends') }}</NuxtLink>

      <!-- Niedawno dodani -->
      <NuxtLink
        v-if="isFullView"
        :to="getFriendsRoute('friends_recent')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('friends.niedawnoDodani') }}</NuxtLink>

      <!-- Urodziny -->
      <NuxtLink
        :to="getFriendsRoute('friends_birthdays')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('friends.birthdays') }}</NuxtLink>

      <!-- Szkoła średnia -->
      <NuxtLink
        :to="getFriendsRoute('friends_high_school')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('profile.info.highSchool') }}</NuxtLink>

      <!-- Aktualne miejsce zamieszkania -->
      <NuxtLink
        :to="getFriendsRoute('friends_current_city')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('friends.aktualneMiejsceZamieszkania') }}</NuxtLink>

      <!-- Obserwowani -->
      <NuxtLink
        :to="getFriendsRoute('following')"
        class="pb-3 text-[15px] cursor-pointer transition-all"
        active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
        inactive-class="hover:border-b-3 hover:border-b-theme-border"
      >{{ $t('profile.following') }}</NuxtLink>
    </div>

    <!-- Kontener listy znajomych -->
    <div v-if="filteredFriendsList.length > 0" class="flex flex-wrap -mx-2 mt-4">
      <FriendListItem
        v-for="(friend, index) in filteredFriendsList"
        :key="friend.id || index"
        :friend="friend"
      />
    </div>
    <div v-else-if="isLoading" class="p-8 text-center text-theme-text-secondary animate-pulse">{{ $t('friends.ladowanieListyZnajomych') }}</div>
    <div v-else class="p-8 text-center text-theme-text-secondary">{{ $t('friends.brakZnajomychDoWyswietlenia') }}</div>

    <button
      v-if="!isFullView && filteredFriendsList.length > 0"
      @click="goToAllFriends"
      class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong rounded-lg p-2 font-bold mt-4 text-theme-text cursor-pointer transition-colors"
    >{{ $t('friends.zobaczWszystko') }}</button>

    <BaseModal
      v-if="showFriendsModal"
      :title="$t('friends.edytujUstawieniaPrywatnosci')"
      @close="showFriendsModal = false"
      :back="!!selectedSetting"
      @back="handleBack"
    >
      <!-- Widok listy ustawień -->
      <div
        v-if="!selectedSetting"
        class="max-w-4xl mx-auto p-6 bg-theme-bg-secondary   antialiased"
      >
        <div class="divide-y divide-theme-border">
          <div
            v-for="(item, index) in settings"
            :key="index"
            class="flex flex-row justify-between items-start gap-6 py-6 first:pt-0 last:pb-0"
          >
            <div class="flex-1 space-y-1 pr-4">
              <h2 class="text-[20px] font-bold text-theme-text tracking-tight">
                {{ item.title }}
              </h2>
              <h3 class="text-[15px] font-normal text-theme-text leading-snug">
                {{ item.subtitle }}
              </h3>
              <p
                v-if="item.description"
                class="text-[14px] text-theme-text-secondary leading-relaxed pt-1"
              >
                {{ item.description }}
              </p>
            </div>

            <button
              @click="handlePrivacyClick(item)"
              class="flex items-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover active:scale-95 transition-all text-theme-text font-bold text-[15px] px-4 py-2 rounded-xl shrink-0 shadow-sm"
            >
              <span>{{ item.privacyLabel }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Widok selectora prywatności -->
      <div v-else class="max-w-2xl mx-auto p-6 bg-theme-bg-secondary   antialiased">
        <PrivacySelector
          :initial-privacy="selectedSetting.privacyType"
          @confirm="handlePrivacyConfirm"
          @back="handleBack"
        />
      </div>
    </BaseModal>
  </div>
</template>
