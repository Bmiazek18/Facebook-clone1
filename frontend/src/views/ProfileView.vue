<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, provide, watch } from 'vue'
import { useRoute, useRouter } from 'nuxt/app'
import { useI18n } from 'vue-i18n'
import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

// --- IMPORTY KOMPONENTÓW ---
import ImageWithGradient from '@/components/media/ImageWithGradient.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import EditProfileImgModal from '@/components/profile/EditProfileImgModal.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import SelectProfileImgModal from '~/components/profile/SelectProfileImgModal.vue'
// --- IMPORTY IKON (Vue Material Design Icons) ---
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import Domain from 'vue-material-design-icons/Domain.vue'
import Camera from 'vue-material-design-icons/Camera.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Message from 'vue-material-design-icons/Message.vue'
// Nowe ikony potrzebne do modalu informacyjnego
import CalendarMonthOutline from 'vue-material-design-icons/CalendarMonthOutline.vue'
import AccountCircleOutline from 'vue-material-design-icons/AccountCircleOutline.vue'

// --- DANE I STORES ---
import { useAuthStore } from '@/stores/auth'
import { useProfilePhotoPost } from '@/composables/feed/useProfilePhotoPost'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const auth = useAuthStore()
const { resolveProfilePhotoPost } = useProfilePhotoPost()

// --- KONFIGURACJA ZAKŁADEK ---
const tabs = [
  { key: '', label: 'profile.tabs.posts' },
  { key: 'info', label: 'profile.tabs.info' },
  { key: 'friends_all', label: 'profile.tabs.friends' },
  { key: 'photos', label: 'profile.tabs.photos' },
  { key: 'videos', label: 'profile.tabs.videos' },
]

// --- KONTROLA SEKCJI SUGEROWANYCH ZNAJOMYCH ---
const isSuggestionsOpen = ref(false)

const toggleSuggestions = () => {
  isSuggestionsOpen.value = !isSuggestionsOpen.value
}

const basePath = computed(() =>
  route.params.userId ? `/profile/${route.params.userId}` : '/profile',
)

const activeTab = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  if (segments.some((seg) => seg.includes('info'))) return 'info'
  if (segments.some((seg) => seg.includes('friends'))) return 'friends_all'
  if (segments.includes('photos')) return 'photos'
  if (segments.includes('videos')) return 'videos'

  return ''
})

function setActiveTab(tabKey: string) {
  router.push(`${basePath.value}/${tabKey}`)
}

// --- DANE UŻYTKOWNIKA ---
const userIdParam = computed(() => {
  const param = route.params.userId as string
  if (!param) return null
  if (/^\d+$/.test(param)) {
    return parseInt(param, 10)
  }
  return param
})

const graphqlProfileUser = ref<any>(null)

const profileUser = computed(() => {
  const idVal = userIdParam.value
  if (!idVal) {
    return graphqlProfileUser.value || auth.currentUser
  }
  return graphqlProfileUser.value
})

const isOwner = computed(() => {
  const pUser = profileUser.value
  const cUser = auth.currentUser
  if (!pUser || !cUser) return false
  return String(pUser.id) === String(cUser.id)
})

// --- STICKY HEADER LOGIC ---
const tabsContainerRef = ref<HTMLElement | null>(null)
const isTabsFixed = ref(false)

const handleScroll = () => {
  if (tabsContainerRef.value) {
    isTabsFixed.value = tabsContainerRef.value.getBoundingClientRect().top <= 56
  }
}

const fetchUserProfile = async () => {
  const idVal = userIdParam.value
  const fetchId = idVal ? String(idVal) : String(auth.currentUserId)

  try {
    const { client } = useApolloClient()
    const { data } = await client.query({
      query: gql`
        query GetUserProfile($userId: ID!) {
          getUserById(userId: $userId) {
            id
            firstName
            lastName
            avatarId
            avatar
            coverId
            cover
            city
            hometown
            education
            bio
            gender
            birthDate
            languages
            pronouns
            highSchool
            job
            company
            phone
            website
            relationshipStatus
            relationshipSince
            partnerName
            partnerAvatar
            bioDetails
            namePronunciation
            otherNames
            favoriteQuotes
            createdAt
            updatedAt
            note
          }
        }
      `,
      variables: { userId: fetchId },
      fetchPolicy: 'network-only',
    })

    const u = data?.getUserById
    if (u) {
      graphqlProfileUser.value = {
        ...u,
        name: [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik',
        avatar: u.avatar || '/default_avatar.png',
        cover: u.cover || 'https://picsum.photos/id/1018/1200/400',
        location: u.city || u.hometown || '',
        school: u.education || '',
      }
    }
  } catch (err) {
    console.error('Failed to fetch user profile:', err)
  }
}

provide('isOwner', isOwner)
provide('profileUser', profileUser)
provide('fetchUserProfile', fetchUserProfile)

useHead({
  title: computed(() => profileUser.value?.name ? `${profileUser.value.name} | Facebook` : 'Profil | Facebook')
})

onMounted(async () => {
  await fetchUserProfile()
  window.addEventListener('scroll', handleScroll)
  fetchProfileFriends()
})

watch(userIdParam, async () => {
  await fetchUserProfile()
  fetchProfileFriends()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// --- STAN MODALI ---
const isPickerOpen = ref(false)
const isCoverPickerOpen = ref(false)
const isInfoModalOpen = ref(false) // Stan kontrolujący modal bezpieczeństwa profilu
const autoTriggerCover = ref(false)

const formatDate = (dateStr?: string) => {
  if (!dateStr) return 'Brak danych'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleDateString('pl-PL', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch (e) {
    return dateStr
  }
}

const formatRelativeOrAbsoluteTime = (dateStr?: string) => {
  if (!dateStr) return 'Brak danych'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    const now = new Date()
    const diffMs = now.getTime() - d.getTime()
    const diffSec = Math.floor(diffMs / 1000)
    const diffMin = Math.floor(diffSec / 60)
    const diffHr = Math.floor(diffMin / 60)
    const diffDays = Math.floor(diffHr / 24)

    if (diffSec < 60) return 'przed chwilą'
    if (diffMin < 60) {
      if (diffMin === 1) return '1 minutę temu'
      if (diffMin >= 2 && diffMin <= 4) return `${diffMin} minuty temu`
      return `${diffMin} minut temu`
    }
    if (diffHr < 24) {
      if (diffHr === 1) return '1 godzinę temu'
      if (diffHr >= 2 && diffHr <= 4) return `${diffHr} godziny temu`
      return `${diffHr} godzin temu`
    }
    if (diffDays < 7) {
      if (diffDays === 1) return 'wczoraj'
      if (diffDays >= 2 && diffDays <= 4) return `${diffDays} dni temu`
      return `${diffDays} dni temu`
    }
    return d.toLocaleDateString('pl-PL', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch (e) {
    return dateStr
  }
}

const triggerCoverUpload = () => {
  isCoverPickerOpen.value = true
  autoTriggerCover.value = true
}

const openProfilePhoto = async (type: 'avatar' | 'cover') => {
  const user = profileUser.value
  if (!user) return
  const src = type === 'cover' ? user.cover : user.avatar
  if (!src) return

  try {
    const post = await resolveProfilePhotoPost({
      userId: user.id,
      kind: type,
      src,
    })

    if (post?.id) {
      const mediaSrc = post.media?.[0]?.src || src
      const fbid = String(mediaSrc).split('/').pop()?.split('?')[0] || '0'
      router.push({
        path: '/photo',
        query: {
          fbid,
          set: `a.${post.id}`,
        },
      })
      return
    }
  } catch (e) {
    console.warn('Nie znaleziono posta dla zdjęcia profilowego:', e)
  }

  // Fallback: otwórz galerię (photo.vue spróbuje jeszcze raz rozwiązać post)
  router.push({
    path: '/photo',
    query: {
      src,
      type,
      userId: String(user.id),
      name: user.name || '',
    },
  })
}

const friendsList = ref<any[]>([])
const fetchProfileFriends = async () => {
  const userId = String(userIdParam.value || auth.currentUserId)
  const currentUserId = String(auth.currentUser?.id || auth.currentUserId || '1')
  try {
    const apolloClient = useApolloClient().resolveClient()
    const { data } = await apolloClient.query({
      query: gql`
        query ProfileFriends($userId: ID!, $currentUserId: ID!) {
          getFriends(userId: $userId) {
            id
            firstName
            lastName
            avatar
            avatarId
            mutualFriendsCount(currentUserId: $currentUserId)
          }
        }
      `,
      variables: { userId, currentUserId },
      fetchPolicy: 'network-only',
    })
    friendsList.value = (data?.getFriends || []).map((friend: any) => ({
      id: friend.id,
      name: `${friend.firstName} ${friend.lastName}`,
      avatar: friend.avatar || (friend.avatarId ? `/api/users/avatar/${friend.avatarId}` : '/default-avatar.png'),
      mutualFriendsCount: friend.mutualFriendsCount,
    }))
  } catch (err) {
    console.warn('Failed to fetch profile friends:', err)
  }
}

const miniPhotosList = [101, 102, 103, 104, 105, 106, 107, 108, 109]
</script>

<template>
  <div v-if="profileUser" class="w-full min-h-screen pb-20 bg-theme-bg mt-[56px]">
    <!-- Sticky Header -->
    <div
      v-if="isTabsFixed"
      class="fixed top-[50px] left-0 right-0 h-[70px] bg-theme-bg-secondary shadow-theme-shadow border-b border-theme-border z-30 animate-slide-down flex items-center"
    >
      <div class="max-w-[1200px] flex items-center justify-between w-full mx-auto lg:px-0">
        <div class="flex items-center space-x-3">
          <UserAvatar :user="profileUser" :size="40" :hide-story-ring="true" :is-owner="isOwner" />
          <div class="text-[17px] text-theme-text leading-5">
            {{ profileUser.name }}
          </div>
        </div>
        <div class="flex items-center space-x-2">
          <button
            class="w-9 h-9 flex items-center justify-center bg-theme-bg hover:bg-theme-bg-hover rounded-sm transition-colors"
          >
            <DotsHorizontal :size="20" fillColor="currentColor" class="text-theme-text" />
          </button>
        </div>
      </div>
    </div>

    <div class="w-full bg-theme-bg-secondary shadow-theme-shadow relative group">
      <ImageWithGradient
        :image-url="profileUser.cover"
        class="rounded-b-xl"
        @upload-cover="triggerCoverUpload"
        @view-cover="openProfilePhoto('cover')"
      />

      <div class="max-w-[1250px] mx-auto relative">
        <div id="ProfileInfo" class="px-4 lg:px-8">
          <div class="flex flex-col lg:flex-row items-center relative">
            <!-- Avatar -->
            <div class="relative z-10 flex-shrink-0">
              <div class="relative group p-1 bg-theme-bg-secondary rounded-full">
                <UserAvatar
                  :user="profileUser"
                  :size="168"
                  :is-owner="isOwner"
                  :view-photo-src="profileUser.avatar"
                  view-photo-type="avatar"
                  class="relative block"
                />
                <button
                  v-if="isOwner"
                  @click.stop="isPickerOpen = true"
                  class="absolute bottom-4 right-4 bg-gray-200 hover:bg-gray-300 text-black p-2 rounded-full cursor-pointer transition-colors z-20"
                >
                  <Camera :size="22" fillColor="currentColor" class="text-theme-text" />
                </button>
              </div>
            </div>

            <!-- Detale profilu -->
            <div
              class="flex-1 flex flex-col items-center lg:items-start mt-2 lg:mt-0 lg:ml-6 min-w-0"
            >
              <!-- Zmiana @onClick na natywne @click i podpięcie isInfoModalOpen -->
              <h1
                @click="isInfoModalOpen = true"
                class="text-[32px] font-bold text-theme-text leading-tight text-center lg:text-left hover:bg-theme-bg-hover cursor-pointer rounded-lg transition-colors truncate px-2 py-0.5 -ml-2 inline-block"
              >
                {{ profileUser.name }}
              </h1>

              <div class="flex items-center text-[15px] font-medium text-theme-text">
                <span class="hover:underline cursor-pointer"
                  >{{ friendsList.length }}
                  {{ friendsList.length === 1 ? 'znajomy' : 'znajomych' }}</span
                >
                <template v-if="profileUser.mutualFriendsCount && !isOwner">
                  <span class="mx-1.5">•</span>
                  <span class="hover:underline cursor-pointer">{{
                    $t('profile.mutualFriendsCount', { count: profileUser.mutualFriendsCount })
                  }}</span>
                </template>
              </div>

              <!-- Lista awatarów znajomych dla profili innych osób -->
              <div
                v-if="!isOwner && friendsList.length > 0"
                class="flex items-center -space-x-2 overflow-hidden mt-2"
              >
                <img
                  v-for="friend in friendsList.slice(0, 8)"
                  :key="friend.id"
                  class="inline-block h-8 w-8 rounded-full ring-2 ring-theme-bg-secondary object-cover cursor-pointer hover:scale-110 transition-transform"
                  :src="friend.avatar"
                  :alt="friend.name"
                  :title="friend.name"
                  @click="router.push(`/profile/${friend.id}`)"
                />
              </div>

              <div
                class="flex flex-wrap items-center justify-center lg:justify-start gap-4 text-[15px] text-theme-text font-medium"
                v-if="profileUser.location || profileUser.school"
              >
                <div class="flex items-center" v-if="profileUser.location">
                  <MapMarker :size="18" class="text-theme-text-secondary opacity-70" />
                  <span>{{ profileUser.location.split(',')[0] }}</span>
                </div>
                <div class="flex items-center" v-if="profileUser.school">
                  <Domain :size="18" class="text-theme-text-secondary opacity-70" />
                  <span>{{ profileUser.school }}</span>
                </div>
              </div>
            </div>

            <!-- Przyciski akcji -->
            <div
              class="flex flex-col sm:flex-row items-center gap-3 mt-6 lg:mt-0 lg:mb-8 lg:self-center shrink-0"
            >
              <template v-if="isOwner">
                <button
                  @click="router.push('/stories/create')"
                  class="flex items-center px-4 py-2 bg-theme-primary hover:bg-theme-primary-hover text-white rounded-md font-semibold text-[15px] transition-colors"
                >
                  <Plus :size="20" class="mr-1.5" fillColor="#FFFFFF" />
                  {{ $t('profile.addToStory') }}
                </button>
                <button
                  @click="router.push(`${basePath}/info`)"
                  class="flex items-center px-4 py-2 bg-theme-bg hover:bg-theme-bg-hover text-theme-text rounded-md font-semibold text-[15px] transition-colors"
                >
                  <Pencil :size="18" class="mr-1.5 text-theme-text" fillColor="currentColor" />
                  {{ $t('profile.editProfile') }}
                </button>
              </template>
              <template v-else>
                <button
                  @click="router.push('/chat/' + String(profileUser.id))"
                  class="flex items-center px-3 py-1.75 bg-theme-primary text-white rounded-md font-semibold"
                >
                  <Message :size="20" class="mr-1.5" /> {{ $t('profile.sendMessage') }}
                </button>
              </template>

              <button
                @click="toggleSuggestions"
                class="flex items-center justify-center w-9 h-9 bg-theme-bg hover:bg-theme-bg-hover rounded-md transition-all shrink-0"
                :class="{
                  'bg-theme-primary/10 text-theme-primary hover:bg-theme-primary/20':
                    isSuggestionsOpen,
                }"
              >
                <ChevronDown
                  :size="24"
                  fillColor="currentColor"
                  class="text-theme-text transition-transform duration-300"
                  :class="{ 'rotate-180': isSuggestionsOpen }"
                />
              </button>
            </div>
          </div>
          <FriendsPeopleYouMayKnow v-if="isSuggestionsOpen" />
          <div class="h-px bg-gray-300 mt-6 lg:mt-4 mb-1 mx-auto opacity-70"></div>

          <!-- Zakładki -->
          <div ref="tabsContainerRef" class="flex flex-wrap items-center justify-start lg:gap-1">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              @click="setActiveTab(tab.key)"
              class="relative h-15 px-4 flex items-center justify-center cursor-pointer rounded-lg hover:bg-gray-100 transition-colors group"
            >
              <span
                class="text-[15px] font-semibold"
                :class="activeTab === tab.key ? 'text-[#1877F2]' : 'text-gray-600'"
              >
                {{ t(tab.label) }}
              </span>
              <div
                v-if="activeTab === tab.key"
                class="absolute bottom-0 left-0 w-full h-0.75 bg-[#1877F2] rounded-t-sm"
              ></div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-312.5 mx-auto md:px-0 px-2">
      <NuxtPage
        :friends-list="friendsList"
        :mini-photos-list="miniPhotosList"
        :user-name="profileUser.name"
        :user-image="profileUser.avatar"
      />
    </div>
  </div>

  <!-- MODAL 1: Zmiana zdjęcia profilowego -->
  <BaseModal
    v-if="isPickerOpen"
    @close="isPickerOpen = false"
    :title="$t('profile.editProfileImage')"
  >
    <SelectProfileImgModal @close="isPickerOpen = false" @updated="fetchUserProfile" />
  </BaseModal>

  <!-- MODAL 3: Zmiana zdjęcia w tle -->
  <BaseModal
    v-if="isCoverPickerOpen"
    @close="isCoverPickerOpen = false"
    title="Edytuj zdjęcie w tle"
  >
    <SelectProfileImgModal
      :is-cover="true"
      :auto-trigger="autoTriggerCover"
      @close="isCoverPickerOpen = false; autoTriggerCover = false"
      @updated="fetchUserProfile"
    />
  </BaseModal>

  <!-- MODAL 2: Informacje o bezpieczeństwie profilu (Aktywowany kliknięciem w H1) -->
  <BaseModal v-if="isInfoModalOpen" @close="isInfoModalOpen = false" :title="profileUser?.name || ''">
    <div class="p-3 w-[550px]  text-left">
      <!-- Główny opis informacyjny -->
      <p class="text-[#65676b] text-[15px] leading-[1.4] mb-5 tracking-normal">
        Aby zapewnić bezpieczeństwo Facebooka, wyświetlamy informacje o użytkownikach i ich
        profilach.
      </p>

      <!-- Lista z informacjami -->
      <div class="flex flex-col gap-4">
        <!-- Pozycja 1: Data dołączenia -->
        <div class="flex items-center gap-3.5 py-1">
          <div class="flex items-center justify-center text-[#050505] shrink-0">
            <CalendarMonthOutline :size="24" />
          </div>
          <span class="text-[#050505] text-[15px] font-normal leading-tight">
            Dołączenie do Facebooka: {{ formatDate(profileUser?.createdAt) }}
          </span>
        </div>

        <!-- Pozycja 2: Ostatnia aktualizacja -->
        <div class="flex items-center gap-3.5 py-1 mb-1">
          <div class="flex items-center justify-center text-[#050505] shrink-0">
            <AccountCircleOutline :size="24" />
          </div>
          <span class="text-[#050505] text-[15px] font-normal leading-tight">
            Zaktualizowano profil: {{ formatRelativeOrAbsoluteTime(profileUser?.updatedAt) }}
          </span>
        </div>
      </div>
    </div>
  </BaseModal>
</template>

<style scoped>
@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-100%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-down {
  animation: slide-down 0.3s ease-out;
}
</style>
