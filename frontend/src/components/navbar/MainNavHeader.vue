<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'nuxt/app'
import { onClickOutside } from '@vueuse/core'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'

// Ikony
import SearchInput from '@/components/common/SearchInput.vue'
import Home from 'vue-material-design-icons/Home.vue'
import TelevisionPlay from 'vue-material-design-icons/TelevisionPlay.vue'
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import StarBoxOutlineIcon from 'vue-material-design-icons/StarBoxOutline.vue'
import NavbarRight from '@/components/navbar/NavbarRight.vue'

type ActiveMenuType = 'profile' | 'notifications' | 'message' | null

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = ref<ActiveMenuType>(null)
const isSearchFocused = ref(false)
const searchInput = ref('')
const navLeft = ref(null)
const menuTarget = ref(null)

// Meilisearch search results
const searchResults = ref<any[]>([])
const isSearching = ref(false)

// History searches loaded from backend & local storage
const recentSearches = ref<any[]>([])

const getStorageKey = () => {
  const userId = authStore.currentUserId ? String(authStore.currentUserId) : 'default'
  return `facebook_search_history_${userId}`
}

const loadLocalHistory = (): any[] => {
  if (typeof window === 'undefined') return []
  try {
    const raw = localStorage.getItem(getStorageKey())
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

const saveLocalHistory = (items: any[]) => {
  if (typeof window === 'undefined') return
  try {
    localStorage.setItem(getStorageKey(), JSON.stringify(items.slice(0, 15)))
  } catch (e) {
    console.error('Failed to save search history locally:', e)
  }
}

const fetchSearchHistory = async () => {
  const localItems = loadLocalHistory()
  if (localItems.length > 0 && recentSearches.value.length === 0) {
    recentSearches.value = localItems.slice(0, 10)
  }

  try {
    const currentUserId = authStore.currentUserId ? String(authStore.currentUserId) : undefined
    const history = await usersApi.getSearchHistory(currentUserId)

    if (history && history.length > 0) {
      const backendItems = history.map((user: any) => ({
        id: String(user.id),
        name: `${user.firstName || ''} ${user.lastName || ''}`.trim() || `User ${user.id}`,
        avatar: user.avatar || '/default-avatar.png',
        newPostsCount: user.newPostsCount || 0,
        type: 'user',
      }))

      const queryItems = localItems.filter(item => item.type === 'query')
      const mergedMap = new Map<string, any>()
      
      for (const item of [...backendItems, ...queryItems]) {
        if (!mergedMap.has(item.id)) {
          mergedMap.set(item.id, item)
        }
      }
      recentSearches.value = Array.from(mergedMap.values()).slice(0, 10)
      saveLocalHistory(recentSearches.value)
    } else if (localItems.length > 0) {
      recentSearches.value = localItems.slice(0, 10)
    }
  } catch (error) {
    console.error('Failed to fetch search history from backend, using local:', error)
    if (localItems.length > 0) {
      recentSearches.value = localItems.slice(0, 10)
    }
  }
}

const removeFromRecent = async (id: string) => {
  try {
    const currentUserId = authStore.currentUserId ? String(authStore.currentUserId) : undefined
    recentSearches.value = recentSearches.value.filter(item => item.id !== id)
    saveLocalHistory(recentSearches.value)

    if (!id.startsWith('query_')) {
      await usersApi.deleteSearchHistoryItem(id, currentUserId)
    }
  } catch (error) {
    console.error('Failed to delete search history item:', error)
  }
}

const clearAllRecent = async () => {
  try {
    const itemsToDelete = [...recentSearches.value]
    recentSearches.value = []
    saveLocalHistory([])
    const currentUserId = authStore.currentUserId ? String(authStore.currentUserId) : undefined
    
    for (const item of itemsToDelete) {
      if (!item.id.startsWith('query_')) {
        await usersApi.deleteSearchHistoryItem(item.id, currentUserId).catch(() => {})
      }
    }
  } catch (error) {
    console.error('Failed to clear search history:', error)
  }
}

onMounted(() => {
  fetchSearchHistory()
})

watch(isSearchFocused, (focused) => {
  if (focused) {
    fetchSearchHistory()
  }
})

watch(() => authStore.currentUserId, () => {
  fetchSearchHistory()
})

onClickOutside(navLeft, () => {
  isSearchFocused.value = false
})

onClickOutside(menuTarget, () => {
  activeMenu.value = null
})

const performLiveSearch = async () => {
  const query = searchInput.value.trim()
  if (!query) {
    searchResults.value = []
    return
  }

  isSearching.value = true
  try {
    const currentUserId = authStore.currentUserId ? String(authStore.currentUserId) : undefined
    const users = await usersApi.searchUsers(query, currentUserId)

    if (users) {
      searchResults.value = users.map((user: any) => ({
        id: String(user.id),
        name: `${user.firstName || ''} ${user.lastName || ''}`.trim() || `User ${user.id}`,
        avatar: user.avatar || '/default-avatar.png',
        newPostsCount: user.newPostsCount || 0,
        type: 'user',
      }))
    } else {
      searchResults.value = []
    }
  } catch (error) {
    console.error('Failed to perform live search:', error)
    searchResults.value = []
  } finally {
    isSearching.value = false
  }
}

let searchDebounceTimeout: any = null

watch(searchInput, (newValue) => {
  if (searchDebounceTimeout) {
    clearTimeout(searchDebounceTimeout)
  }

  if (!newValue.trim()) {
    searchResults.value = []
    isSearching.value = false
    return
  }

  isSearching.value = true

  searchDebounceTimeout = setTimeout(() => {
    performLiveSearch()
  }, 300)
})

const saveQueryToHistory = (query: string) => {
  const cleanQ = query.trim()
  if (!cleanQ) return
  const queryItem = {
    id: `query_${cleanQ.toLowerCase()}`,
    query: cleanQ,
    name: cleanQ,
    type: 'query'
  }
  recentSearches.value = [queryItem, ...recentSearches.value.filter(item => item.id !== queryItem.id)].slice(0, 10)
  saveLocalHistory(recentSearches.value)
}

const handleSearchSubmit = () => {
  const query = searchInput.value.trim()
  if (query) {
    saveQueryToHistory(query)
    router.push({ path: '/search', query: { q: query } })
    isSearchFocused.value = false
    searchInput.value = ''
  }
}

const goToProfile = (user: { id: string, name: string, avatar: string, newPostsCount?: number }) => {
  const userItem = {
    id: String(user.id),
    name: user.name,
    avatar: user.avatar || '/default-avatar.png',
    newPostsCount: user.newPostsCount || 0,
    type: 'user'
  }
  
  recentSearches.value = [userItem, ...recentSearches.value.filter(item => item.id !== String(user.id))].slice(0, 10)
  saveLocalHistory(recentSearches.value)

  try {
    const currentUserId = authStore.currentUserId ? String(authStore.currentUserId) : undefined
    usersApi.recordSearch(user.id, currentUserId)
      .then(() => {
        fetchSearchHistory()
      })
      .catch(err => console.error('Failed to record search:', err))
  } catch (error) {
    console.error('Failed to record search:', error)
  }

  router.push('/profile/' + user.id)
  isSearchFocused.value = false
  searchInput.value = ''
}

const goToRecentItem = (item: any) => {
  if (item.type === 'query' || item.query) {
    const query = item.query || item.name
    saveQueryToHistory(query)
    router.push({ path: '/search', query: { q: query } })
    isSearchFocused.value = false
    searchInput.value = ''
  } else {
    goToProfile(item)
  }
}
</script>

<template>
  <div
    id="MainNav"
    class="fixed z-50 w-full flex items-center justify-between top-0 h-14 bg-theme-bg-secondary shadow-md px-4 mb-[50px]"
  >
    <div
      id="NavLeft"
      ref="navLeft"
      class="flex items-center justify-start w-[260px] relative h-full"
    >
      <!-- Dropdown wyszukiwania -->
      <div
        v-if="isSearchFocused"
        class="absolute -top-2 -left-4 w-[330px] bg-theme-bg-secondary rounded-b-xl shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] z-10 pt-[65px] pb-3 border-t-0 max-h-[500px] overflow-y-auto"
      >
        <!-- Stan z wpisaną frazą wyszukiwania -->
        <div v-if="searchInput.trim()">
          <div v-if="isSearching" class="px-4 py-3 text-center text-[15px] text-[#65676B] dark:text-[#B0B3B8] flex items-center justify-center gap-2">
            <div class="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            <span>{{ $t('header.wyszukiwanie') }}</span>
          </div>
          
          <div v-if="searchResults.length > 0" class="flex flex-col">
            <div
              v-for="user in searchResults"
              :key="user.id"
              class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 dark:hover:bg-[#3A3B3C] cursor-pointer transition-colors"
              @click="goToProfile(user)"
            >
              <div class="w-9 h-9 rounded-full overflow-hidden bg-zinc-800 shrink-0">
                <img :src="user.avatar" class="w-full h-full object-cover" />
              </div>
              <span class="text-[15px] font-semibold text-gray-900 dark:text-gray-200 truncate">{{ user.name }}</span>
              <!-- Blue dot if user posted since last search -->
              <span v-if="user.newPostsCount > 0" class="w-2.5 h-2.5 rounded-full bg-blue-500 shrink-0 ml-auto" :title="$t('header.nowePostyOdOstatniego')"></span>
            </div>
          </div>

          <div v-else-if="!isSearching" class="px-4 py-2 text-center text-[14px] text-[#65676B] dark:text-[#B0B3B8]">
            {{ $t('header.nieZnalezionoPasujacychOsob') }}
          </div>

          <!-- Przycisk "Szukaj [fraza]" na dole -->
          <div
            @click="handleSearchSubmit"
            class="flex items-center gap-3 px-4 py-2.5 hover:bg-gray-100 dark:hover:bg-[#3A3B3C] cursor-pointer transition-colors border-t border-gray-100 dark:border-zinc-700/60 my-1"
          >
            <div class="w-9 h-9 rounded-full bg-[#1877f2] text-white flex items-center justify-center shrink-0">
              <MagnifyIcon :size="20" />
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-[15px] font-medium text-[#1877f2] dark:text-blue-400 truncate">
                {{ $t('header.szukaj') || 'Szukaj' }}: <strong class="font-semibold">{{ searchInput.trim() }}</strong>
              </span>
            </div>
          </div>

          <!-- Historia wyszukiwania na dole sekcji wyszukiwania -->
          <div v-if="recentSearches.length > 0" class="mt-2 pt-2 border-t border-gray-200 dark:border-zinc-700">
            <div class="flex justify-between items-center px-4 py-1.5">
              <span class="text-[14px] font-bold text-gray-700 dark:text-gray-300">{{ $t('search.recent') }}</span>
              <button
                @click="clearAllRecent"
                class="text-[12px] font-medium text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/30 px-2 py-0.5 rounded transition"
              >{{ $t('header.wyczyscWszystko') }}</button>
            </div>
            <div class="flex flex-col">
              <div
                v-for="item in recentSearches.slice(0, 5)"
                :key="item.id"
                class="flex items-center justify-between px-4 py-2 hover:bg-gray-100 dark:hover:bg-[#3A3B3C] group cursor-pointer transition-colors"
                @click="goToRecentItem(item)"
              >
                <div class="flex items-center gap-3 min-w-0">
                  <div v-if="item.type === 'query'" class="w-9 h-9 rounded-full bg-gray-200 dark:bg-zinc-700 flex items-center justify-center shrink-0 text-gray-600 dark:text-gray-300">
                    <MagnifyIcon :size="20" />
                  </div>
                  <div v-else class="w-9 h-9 rounded-full overflow-hidden bg-zinc-800 shrink-0">
                    <img :src="item.avatar" class="w-full h-full object-cover" />
                  </div>
                  <span class="text-[14px] font-medium text-gray-950 dark:text-gray-200 truncate">{{ item.name }}</span>
                  <span v-if="item.newPostsCount > 0" class="w-2 h-2 rounded-full bg-blue-500 shrink-0" :title="$t('header.nowePostyOdOstatniego')"></span>
                </div>
                <button
                  @click.stop="removeFromRecent(item.id)"
                  class="p-1 rounded-full hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-400 hover:text-gray-600 dark:hover:text-zinc-200 transition opacity-0 group-hover:opacity-100"
                  :title="$t('header.usunZHistorii')"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Stan domyślny (pusty input) -->
        <div v-else>
          <div v-if="recentSearches.length > 0">
            <div class="flex justify-between items-center px-4 py-2">
              <span class="text-[16px] font-bold text-gray-800 dark:text-gray-200">{{ $t('search.recent') }}</span>
              <button
                @click="clearAllRecent"
                class="text-[13px] font-medium text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/30 px-2 py-1 rounded transition"
              >{{ $t('header.wyczyscWszystko') }}</button>
            </div>
            <div class="flex flex-col mt-1">
              <div
                v-for="item in recentSearches"
                :key="item.id"
                class="flex items-center justify-between px-4 py-2 hover:bg-gray-100 dark:hover:bg-[#3A3B3C] group cursor-pointer transition-colors"
                @click="goToRecentItem(item)"
              >
                <div class="flex items-center gap-3 min-w-0">
                  <div v-if="item.type === 'query'" class="w-9 h-9 rounded-full bg-gray-200 dark:bg-zinc-700 flex items-center justify-center shrink-0 text-gray-600 dark:text-gray-300">
                    <MagnifyIcon :size="20" />
                  </div>
                  <div v-else class="w-9 h-9 rounded-full overflow-hidden bg-zinc-800 shrink-0">
                    <img :src="item.avatar" class="w-full h-full object-cover" />
                  </div>
                  <span class="text-[15px] font-medium text-gray-950 dark:text-gray-200 truncate">{{ item.name }}</span>
                  <!-- Blue dot if user posted since last search -->
                  <span v-if="item.newPostsCount > 0" class="w-2.5 h-2.5 rounded-full bg-blue-500 shrink-0" :title="$t('header.nowePostyOdOstatniego')"></span>
                </div>
                <button
                  @click.stop="removeFromRecent(item.id)"
                  class="p-1 rounded-full hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-400 hover:text-gray-600 dark:hover:text-zinc-200 transition opacity-0 group-hover:opacity-100"
                  :title="$t('header.usunZHistorii')"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
          <div v-else class="px-4 py-3 text-center text-[15px] text-[#65676B] dark:text-[#B0B3B8]">{{ $t('header.wpiszImieLubNazwisko') }}</div>
        </div>
      </div>

      <div class="z-20 flex items-center w-full">
        <Transition name="slide-fade" mode="out-in">
          <NuxtLink v-if="!isSearchFocused" to="/" class="mr-2 min-w-10 bg-white rounded-full">
            <img class="w-10" src="@/assets/images/FacebookLogoCircle.png" />
          </NuxtLink>

          <div
            v-else
            class="mr-2 p-2 rounded-full hover:bg-gray-100 dark:hover:bg-[#3A3B3C] cursor-pointer text-[#64676B] dark:text-gray-300 transition-colors"
            @click="isSearchFocused = false"
          >
            <ArrowLeft :size="24" />
          </div>
        </Transition>

        <div :class="isSearchFocused ? 'w-full' : 'w-[40px]'" class="flex relative xl:w-full">
          <SearchInput
            v-model="searchInput"
            :is-focused="isSearchFocused"
            @update:isFocused="isSearchFocused = $event"
            @enter="handleSearchSubmit"
          />
        </div>
      </div>
    </div>

    <div
      id="NavCenter"
      class="hidden md:flex items-center justify-center w-8/12 max-w-[700px] h-full"
    >
      <!-- Home -->
      <NuxtLink
        to="/"
        v-tooltip="'Strona główna'"
        class="flex items-center justify-center w-full px-1 h-full border-b-[3px] transition-colors"
        :class="route.path === '/' ? 'border-blue-500' : 'border-transparent'"
      >
        <div class="flex items-center justify-center w-full h-[calc(100%-3px)] hover:bg-theme-hover rounded-lg">
          <Home :size="27" :fillColor="route.path === '/' ? '#1A73E3' : '#64676B'" />
        </div>
      </NuxtLink>

      <!-- Reels -->
      <NuxtLink
        to="/reel"
        v-tooltip="'Rolki'"
        class="flex items-center justify-center w-full px-1 h-full border-b-[3px] transition-colors"
        :class="route.path === '/reel' ? 'border-blue-500' : 'border-transparent'"
      >
        <div class="flex items-center justify-center w-full h-[calc(100%-3px)] hover:bg-theme-hover rounded-lg">
          <TelevisionPlay :size="27" :fillColor="route.path === '/reel' ? '#1A73E3' : '#64676B'" />
        </div>
      </NuxtLink>

      <!-- Marketplace -->
      <NuxtLink
        to="/marketplace"
        v-tooltip="'Marketplace'"
        class="flex items-center justify-center w-full px-1 h-full border-b-[3px] transition-colors"
        :class="route.path.includes('marketplace') ? 'border-blue-500' : 'border-transparent'"
      >
        <div class="flex items-center justify-center w-full h-[calc(100%-3px)] hover:bg-theme-hover rounded-lg">
          <StorefrontOutline
            :size="27"
            :fillColor="route.path.includes('marketplace') ? '#1A73E3' : '#64676B'"
          />
        </div>
      </NuxtLink>

      <!-- Groups -->
      <NuxtLink
        to="/groups"
        v-tooltip="'Grupy'"
        class="flex items-center justify-center w-full px-1 h-full border-b-[3px] transition-colors"
        :class="route.path === '/groups' ? 'border-blue-500' : 'border-transparent'"
      >
        <div class="flex items-center justify-center w-full h-[calc(100%-3px)] hover:bg-theme-hover rounded-lg">
          <span
            class="rounded-full border-2 p-1 flex items-center justify-center"
            :class="route.path === '/groups' ? 'border-blue-500' : 'dark:border-gray-400 border-[#64676B]'"
          >
            <AccountGroup :size="20" :fillColor="route.path === '/groups' ? '#1A73E3' : '#64676B'" />
          </span>
        </div>
      </NuxtLink>

      <!-- Event / Saved -->
      <NuxtLink
        to="/events"
        v-tooltip="'Wydarzenia'"
        class="flex items-center justify-center w-full px-1 h-full border-b-[3px] transition-colors"
        :class="route.path.startsWith('/events') ? 'border-blue-500' : 'border-transparent'"
      >
        <div class="flex items-center justify-center w-full h-[calc(100%-3px)] hover:bg-theme-hover rounded-lg">
          <StarBoxOutlineIcon
            :size="30"
            :fillColor="route.path.startsWith('/events') ? '#1A73E3' : '#64676B'"
          />
        </div>
      </NuxtLink>


    </div>

    <NavbarRight />
  </div>
</template>

<style scoped>
.slide-fade-enter-active {
  transition: all 0.2s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(-5px);
  opacity: 0;
}
</style>
