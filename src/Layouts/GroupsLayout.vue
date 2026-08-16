<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'

// Komponenty wewnętrzne (zakładam, że istnieją w Twoim projekcie)
import ImageWithGradient from '@/components/media/ImageWithGradient.vue'

// Store
import { useGroupsStore } from '@/stores/groups'
import { useAuthStore } from '@/stores/auth'
import type { Group as GroupType } from '@/types/Group'

// --- IKONY (vue-material-design-icons) ---
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue'
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import InformationIcon from 'vue-material-design-icons/Information.vue'

import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue' // Ważne: dodano
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue' // Ważne: dodano
import PlusIcon from 'vue-material-design-icons/Plus.vue'

import { useStickySidebar } from '@/composables/ui/useStickySidebar'
import { useI18n } from 'vue-i18n'
import GroupsSidebar from '@/components/groups/GroupsSidebar.vue'
import GroupAdminSidebar from '@/components/groups/GroupAdminSidebar.vue'

const { t } = useI18n()

// --- LOGIKA "FACEBOOK DUAL STICKY" (Prawy pasek) ---
const rightSectionRef = ref<HTMLDivElement | null>(null)
const { stickyTop } = useStickySidebar(rightSectionRef, 125, 16)
// --- STICKY HEADER LOGIC ---
const tabsContainerRef = ref<HTMLElement | null>(null)
const isTabsFixed = ref(false)

const handleScroll = () => {
  if (tabsContainerRef.value) {
    // Jeśli element zakładek dotyka górnego paska nawigacji (ok. 56px)
    const rect = tabsContainerRef.value.getBoundingClientRect()
    isTabsFixed.value = rect.top <= 56
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// --- DANE (Store) ---
const route = useRoute()
const groupsStore = useGroupsStore()
const authStore = useAuthStore()

const membershipRole = ref('')

const fetchMembership = async () => {
  const groupId = route.params.id as string
  if (groupId && authStore.currentUserId) {
    membershipRole.value = await groupsStore.getGroupMembership(groupId, authStore.currentUserId)
  } else {
    membershipRole.value = ''
  }
}

watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      await groupsStore.loadGroupDetails(newId as string)
      await fetchMembership()
    }
  },
  { immediate: true }
)

const groupDetails = computed<GroupType | undefined>(() => {
  const id = route.params.id as string
  return groupsStore.getGroupById(id)
})

useHead({
  title: computed(() => groupDetails.value?.name ? `${groupDetails.value.name} | Facebook` : 'Grupy | Facebook')
})

const isPrivateAndNotMember = computed(() => {
  const isPrivate = groupDetails.value?.privacy === 'private'
  const isMember = membershipRole.value === 'MEMBER' || membershipRole.value === 'ADMIN'
  return isPrivate && !isMember
})

const handleJoin = async () => {
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.joinGroup(groupId)
    if (success) {
      await fetchMembership()
    }
  }
}

const handleLeave = async () => {
  const groupId = route.params.id as string
  if (groupId) {
    if (membershipRole.value === 'ADMIN' && (groupDetails.value?.members || 0) > 1) {
      const allMembers = await groupsStore.fetchGroupMembers(groupId)
      const adminCount = allMembers.filter(m => m.role.toUpperCase() === 'ADMIN').length
      if (adminCount <= 1) {
        alert('Jesteś jedynym administratorem. Mianuj nowego administratora przed opuszczeniem grupy.')
        return
      }
    }
    const success = await groupsStore.leaveGroup(groupId)
    if (success) {
      await fetchMembership()
    }
  }
}

// --- UI LOGIC ---
const isDescriptionExpanded = ref(false)
const truncatedDescription = computed(() => {
  const description = groupDetails.value?.description || ''
  const maxLength = 200
  if (description.length > maxLength && !isDescriptionExpanded.value) {
    return description.substring(0, maxLength) + '...'
  }
  return description
})

const navLinks = [
  { path: '', text: t('groups.discussion') },
  { path: '/info', text: t('groups.info') },
  { path: '/members', text: t('groups.members') },
  { path: '/events', text: t('groups.events') },
  { path: '/media', text: t('groups.media') },
  { path: '/files', text: t('groups.files') },
]

const displayLinks = computed(() => {
  return navLinks
})

const isStandaloneAdminRoute = computed(() => {
  const paths = ['/admin_assistant', '/member-requests', '/admin_activities', '/edit']
  return paths.some(path => route.path.includes(path))
})
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-[#f0f2f5] dark:bg-theme-bg text-theme-text">
    <GroupAdminSidebar v-if="membershipRole === 'ADMIN'" />
    <GroupsSidebar v-else />

    <main class="flex-1 h-full mt-[56px] overflow-y-auto relative pb-10">

      <!-- Nagłówek Grupy (Tło, Tytuł, Przyciski, Zakładki) -->
      <div v-if="!isStandaloneAdminRoute" class="w-full bg-white dark:bg-theme-bg-secondary shadow-sm">
        <div class="max-w-[1200px] mx-auto px-0 md:px-4 lg:px-8">

          <!-- Zdjęcie w tle (Cover Photo) -->
          <div class="w-full h-[250px] md:h-[350px] rounded-b-xl overflow-hidden relative">
            <ImageWithGradient
              :image-url="groupDetails?.images"
              :initial-width="1250"
              :initial-height="350"
              class="w-full h-full object-cover"
            />
          </div>

          <!-- Pływający pasek zakładek (Sticky Header) -->
          <div
            v-if="isTabsFixed"
            class="fixed top-[50px] left-0 right-0 h-[60px] bg-white dark:bg-theme-bg-secondary shadow-sm border-b border-theme-border z-30 animate-slide-down flex items-center"
          >
            <div class="max-w-[1200px] flex items-center justify-between w-full mx-auto px-4 lg:px-8">
              <div class="flex items-center space-x-3">
                <img :src="groupDetails?.image" class="h-10 w-10 rounded-md object-cover" />
                <div class="text-[17px] text-theme-text font-bold leading-5">
                  {{ groupDetails?.name }}
                </div>
              </div>
              <div class="flex items-center gap-2 w-full md:w-auto">
                <button
                  v-if="membershipRole === 'MEMBER' || membershipRole === 'ADMIN'"
                  class="bg-[#0866FF] hover:bg-[#0052CC] text-white h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5"
                >
                  <PlusIcon :size="18" /> {{ t('groups.invite') }}
                </button>
                <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text">
                  <ShareVariantIcon :size="18" /> {{ t('common.share') }}
                </button>
                <button
                  class="h-9 w-12 rounded-md font-semibold transition flex items-center justify-center bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text"
                >
                  <ChevronDownIcon :size="20" />
                </button>
              </div>
            </div>
          </div>

          <!-- Informacje o grupie (Tytuł, Avatary, Akcje) -->
          <div class="px-4 md:px-0 pt-5 pb-0 relative">
            <div class="mb-4">
              <h1 class="text-[28px] md:text-[32px] font-bold leading-tight text-theme-text">
                {{ groupDetails?.name || t('groups.groupName') }}
              </h1>

              <div class="text-[15px] mt-1 font-semibold flex items-center gap-1.5 text-theme-text-secondary">
                <span class="flex items-center gap-1">
                  <!-- Możesz podmienić AccountGroupIcon na LockIcon jeśli wolisz wygląd kłódki -->
                  <AccountGroupIcon v-if="groupDetails?.privacy !== 'public'" :size="16" />
                  <EarthIcon v-else :size="16" />
                  <span>{{ groupDetails?.privacy === 'public' ? t('groups.public') : t('groups.private') }}</span>
                </span>
                <span class="text-[12px] align-middle">•</span>
                <span class="font-semibold hover:underline cursor-pointer">
                  {{ groupDetails?.members || '0' }} {{ t('groups.members') }}
                </span>
              </div>
            </div>

            <!-- Avatary i Przyciski (Wyrównane do środka) -->
            <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-4">

              <!-- Rząd Avatarów -->
              <div class="flex items-center -space-x-2 overflow-hidden py-1 pl-1 w-full md:w-auto">
                <img
                  v-for="i in 8"
                  :key="i"
                  :src="`https://i.pravatar.cc/150?img=${i + 10}`"
                  alt="Member"
                  class="w-[36px] h-[36px] rounded-full border-2 border-white dark:border-theme-bg-secondary cursor-pointer hover:z-10 relative object-cover shadow-sm"
                />
              </div>

              <!-- Główne Przyciski -->
              <div class="flex items-center flex-wrap gap-2 w-full md:w-auto">
                <button
                  v-if="membershipRole === 'MEMBER' || membershipRole === 'ADMIN'"
                  class="bg-[#E41E5D] hover:bg-[#C21A4F] text-white h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5"
                >
                  <PlusIcon :size="18" /> {{ t('groups.invite') }}
                </button>

                <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text">
                  <ShareVariantIcon :size="18" /> {{ t('common.share') }}
                </button>

                <!-- Status przycisk dołączenia -->
                <button
                  v-if="membershipRole === ''"
                  @click="handleJoin"
                  class="bg-[#0866FF] hover:bg-[#0052CC] text-white h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5"
                >
                  <PlusIcon :size="18" /> Dołącz do grupy
                </button>
                <button
                  v-else-if="membershipRole === 'PENDING'"
                  @click="handleLeave"
                  class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-amber-500 hover:bg-amber-600 text-white"
                  title="Kliknij, aby wycofać prośbę"
                >
                  <span class="w-2 h-2 rounded-full bg-white animate-pulse"></span> Oczekiwanie...
                </button>
                <button
                  v-else
                  @click="handleLeave"
                  class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text"
                  title="Kliknij, aby opuścić grupę"
                >
                  <AccountGroupIcon :size="18" /> <!-- Ikona Dołączono -->
                  {{ membershipRole === 'ADMIN' ? 'Admin' : 'Dołączono' }}
                  <ChevronDownIcon :size="18" />
                </button>

                <button class="h-9 w-12 rounded-md font-semibold transition flex items-center justify-center bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text">
                  <ChevronDownIcon :size="20" />
                </button>
              </div>
            </div>

            <!-- Zakładki Nawigacji -->
            <div
              ref="tabsContainerRef"
              class="border-t flex items-center justify-between h-[60px] border-theme-border"
            >
              <div class="flex h-full overflow-x-auto no-scrollbar">
                <NuxtLink
                  v-for="link in displayLinks"
                  :key="link.path"
                  :to="`/groups/${route.params.id}${link.path}`"
                  class="px-4 h-full font-semibold text-[15px] flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-gray-100 dark:hover:bg-theme-hover rounded-md my-1"
                  exact-active-class="text-[#0866FF] border-b-[3px] border-[#0866FF] rounded-none my-0"
                >
                  {{ link.text }}
                </NuxtLink>
              </div>

              <!-- Ikony akcji po prawej stronie zakładek -->
              <div class="flex items-center gap-1 pl-2 shrink-0">
                <button class="p-2 rounded-full transition w-9 h-9 flex items-center justify-center bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text">
                  <MagnifyIcon :size="20" />
                </button>
                <button class="p-2 rounded-full transition w-9 h-9 flex items-center justify-center bg-gray-100 dark:bg-theme-bg-subtle hover:bg-gray-200 dark:hover:bg-theme-hover-strong text-theme-text">
                  <DotsHorizontalIcon :size="20" />
                </button>
              </div>
            </div>

          </div>
        </div>
      </div>

      <!-- Treść grupy (Prywatna informacja lub zawartość z NuxtPage) -->
      <div class="w-full max-w-[1200px] mx-auto px-4 lg:px-8 mt-4">
        <div v-if="isPrivateAndNotMember" class="bg-white dark:bg-theme-bg-secondary border border-theme-border rounded-xl p-8 text-center max-w-xl mx-auto shadow-sm mt-10">
          <div class="w-16 h-16 bg-gray-100 dark:bg-zinc-800 rounded-full flex items-center justify-center mx-auto mb-4 text-theme-text-secondary">
            <!-- Ikona kłódki -->
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <h2 class="text-xl font-bold text-theme-text mb-2">Ta grupa jest prywatna</h2>
          <p class="text-theme-text-secondary text-[15px] mb-6">
            Dołącz do tej grupy, aby zobaczyć jej zawartość, publikować posty i brać udział w dyskusjach.
          </p>
          <button
            v-if="membershipRole === ''"
            @click="handleJoin"
            class="bg-[#0866FF] hover:bg-[#0052CC] text-white px-6 py-2.5 rounded-lg font-semibold text-[15px] transition"
          >
            Dołącz do grupy
          </button>
          <div v-else-if="membershipRole === 'PENDING'" class="text-amber-500 font-semibold flex items-center justify-center gap-1.5">
            <span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span>
            Oczekiwanie na zatwierdzenie przez administratora
          </div>
        </div>
        <NuxtPage v-else v-slot="{ Component }">
          <component :is="Component" :group-details="groupDetails" :sticky-top="stickyTop" />
        </NuxtPage>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Ukrywanie scrollbara w zakładkach, ale zachowanie funkcjonalności */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Stylizacja paska przewijania dla całej strony (opcjonalnie) */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f0f2f5;
}
::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #999;
}
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
