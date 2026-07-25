<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'nuxt/app'
import { useAuthStore } from '@/stores/auth'
import { usePostsStore } from '@/composables/feed/useAppState'
import { getUserById } from '@/utils/users'

// Import components
import PostItem from '@/components/feed/post/PostItem.vue'
import type { Post } from '@/types/Post'
// Import icons
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import NewspaperVariant from 'vue-material-design-icons/NewspaperVariant.vue'
import MoviePlay from 'vue-material-design-icons/MoviePlay.vue'
import Storefront from 'vue-material-design-icons/Storefront.vue'
import Flag from 'vue-material-design-icons/Flag.vue'
import CalendarMonth from 'vue-material-design-icons/CalendarMonth.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

const postsStore = usePostsStore()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Search state
const searchQuery = ref('')
const activeFilter = ref<'all' | 'people' | 'posts' | 'reels'>('all')
const searchResults = ref<any[]>([])
const isSearching = ref(false)

// Filter toggles state
const filters = ref({
  latestPosts: false,
  seenPosts: false,
})

const performSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) {
    searchResults.value = []
    return
  }

  isSearching.value = true
  try {
    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1')
    const response = await fetch('http://localhost:8080/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        query: `
          query SearchUsers($query: String!, $currentUserId: ID!) {
            searchUsers(query: $query, currentUserId: $currentUserId) {
              id
              firstName
              lastName
              avatarId
              inHistory
              newPostsCount
              mutualFriendsCount(currentUserId: $currentUserId)
            }
          }
        `,
        variables: {
          query,
          currentUserId,
        },
      }),
    })

    const result = await response.json()
    if (result.data?.searchUsers) {
      searchResults.value = result.data.searchUsers.map((user: any) => ({
        id: user.id,
        name: `${user.firstName} ${user.lastName}`,
        avatar: `http://localhost:8080/api/users/avatar/${user.avatarId || 'default-avatar.svg'}`,
        inHistory: user.inHistory,
        newPostsCount: user.newPostsCount,
        mutualFriendsCount: user.mutualFriendsCount,
      }))
    }
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    isSearching.value = false
  }
}

onMounted(() => {
  if (route.query.q) {
    searchQuery.value = String(route.query.q)
    performSearch()
  }
})

watch(
  () => route.query.q,
  (newQ) => {
    if (newQ !== undefined) {
      searchQuery.value = String(newQ)
      performSearch()
    }
  },
)

// Computed results
const filteredPeople = computed(() => {
  return searchResults.value
})

const filteredPosts = computed(() => {
  const allPosts = postsStore?.posts || []
  if (!searchQuery.value.trim()) return allPosts
  const query = searchQuery.value.toLowerCase()
  return allPosts.filter((post: Post) => {
    const authorName = getUserById(post.authorId)?.name || ''
    return authorName.toLowerCase().includes(query) || post.content.toLowerCase().includes(query)
  })
})
</script>

<template>
  <div class="flex h-screen bg-[#F0F2F5] overflow-hidden">
    <aside
      class="w-[360px] flex-shrink-0 bg-white shadow-sm flex flex-col h-full overflow-y-auto border-r border-gray-200 z-20"
    >
      <div class="px-4 pt-5 pb-2">
        <h1 class="text-2xl font-bold text-gray-900 mb-4">Wyniki wyszukiwania</h1>
        <div class="h-[1px] bg-gray-300 w-full mb-4"></div>
        <h2 class="text-[17px] font-semibold text-gray-900 mb-2">Filtry</h2>
      </div>

      <div class="px-2 pb-10">
        <button
          @click="activeFilter = 'all'"
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg transition mb-1"
          :class="activeFilter === 'all' ? 'bg-[#EBF5FF]' : 'hover:bg-gray-100'"
        >
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center"
            :class="
              activeFilter === 'all' ? 'bg-[#1877F2] text-white' : 'bg-gray-200 text-gray-900'
            "
          >
            <NewspaperVariant :size="20" />
          </div>
          <span
            class="font-medium text-[15px]"
            :class="activeFilter === 'all' ? 'text-[#1877F2]' : 'text-gray-900'"
            >Wszystkie</span
          >
        </button>

        <div v-if="activeFilter === 'all'" class="pl-14 pr-2 py-2 space-y-4 mb-2">
          <div class="flex items-center justify-between">
            <span class="text-[15px] text-gray-900">Najnowsze posty</span>
            <button
              @click="filters.latestPosts = !filters.latestPosts"
              class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none"
              :class="filters.latestPosts ? 'bg-[#1877F2]' : 'bg-gray-300'"
            >
              <span
                class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform ml-1"
                :class="filters.latestPosts ? 'translate-x-5' : 'translate-x-0'"
              />
            </button>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-[15px] text-gray-900">Wyświetlone przez Ciebie</span>
            <button
              @click="filters.seenPosts = !filters.seenPosts"
              class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none"
              :class="filters.seenPosts ? 'bg-[#1877F2]' : 'bg-gray-300'"
            >
              <span
                class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform ml-1"
                :class="filters.seenPosts ? 'translate-x-5' : 'translate-x-0'"
              />
            </button>
          </div>

          <div class="pt-2 space-y-4">
            <div class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] text-gray-900">Data zamieszczenia</span>
              <ChevronDown :size="20" class="text-gray-500 bg-gray-200 rounded-full p-0.5" />
            </div>
          </div>
        </div>

        <button
          @click="activeFilter = 'people'"
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg transition mb-1"
          :class="activeFilter === 'people' ? 'bg-[#EBF5FF]' : 'hover:bg-gray-100'"
        >
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center transition"
            :class="
              activeFilter === 'people' ? 'bg-[#1877F2] text-white' : 'bg-gray-200 text-gray-900'
            "
          >
            <AccountGroup :size="20" />
          </div>
          <span
            class="font-medium text-[15px]"
            :class="activeFilter === 'people' ? 'text-[#1877F2]' : 'text-gray-900'"
            >Osoby</span
          >
        </button>

        <div v-if="activeFilter === 'people'" class="pl-14 pr-2 space-y-1 mb-2">
          <div
            v-for="label in ['Znajomi', 'Miejscowość', 'Wykształcenie', 'Miejsce pracy']"
            :key="label"
            class="flex items-center justify-between py-2 cursor-pointer group hover:bg-gray-100 px-2 rounded-md -ml-2"
          >
            <span class="text-[15px] text-gray-900">{{ label }}</span>
            <ChevronDown :size="20" class="text-gray-500" />
          </div>
        </div>

        <button
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-gray-100 transition mb-1"
        >
          <div
            class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-gray-900"
          >
            <MoviePlay :size="20" />
          </div>
          <span class="font-medium text-[15px] text-gray-900">Rolki</span>
        </button>
        <button
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-gray-100 transition mb-1"
        >
          <div
            class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-gray-900"
          >
            <Storefront :size="20" />
          </div>
          <span class="font-medium text-[15px] text-gray-900">Marketplace</span>
        </button>
        <button
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-gray-100 transition mb-1"
        >
          <div
            class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-gray-900"
          >
            <Flag :size="20" />
          </div>
          <span class="font-medium text-[15px] text-gray-900">Strony</span>
        </button>
        <button
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-gray-100 transition mb-1"
        >
          <div
            class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-gray-900"
          >
            <AccountGroup :size="20" />
          </div>
          <span class="font-medium text-[15px] text-gray-900">Grupy</span>
        </button>
        <button
          class="w-full flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-gray-100 transition mb-1"
        >
          <div
            class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-gray-900"
          >
            <CalendarMonth :size="20" />
          </div>
          <span class="font-medium text-[15px] text-gray-900">Wydarzenia</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto flex flex-col relative">
      <div class="w-full max-w-[750px] mx-auto py-6 px-4">
        <div v-if="activeFilter === 'all'" class="space-y-4">
          <div
            v-if="filteredPeople.length > 0"
            class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
          >
            <div class="px-4 py-3">
              <h2 class="text-[20px] font-bold text-gray-900">Osoby</h2>
            </div>
            <div>
              <div
                v-for="person in filteredPeople.slice(0, 3)"
                :key="person.id"
                class="flex items-center justify-between p-4 hover:bg-gray-50 transition gap-4"
              >
                <div class="flex items-start gap-4 overflow-hidden">
                  <img
                    :src="person.avatar"
                    class="w-[60px] h-[60px] rounded-full object-cover border border-gray-200 flex-shrink-0"
                  />
                  <div class="flex flex-col min-w-0 pt-1">
                    <h3
                      class="font-semibold text-[17px] text-gray-900 leading-snug cursor-pointer hover:underline mb-1"
                      @click="router.push('/profile/' + person.id)"
                    >
                      {{ person.name }}
                    </h3>
                    <div
                      class="flex items-center gap-2"
                      v-if="
                        person.mutualFriendsCount !== undefined && person.mutualFriendsCount > 0
                      "
                    >
                      <span class="text-[13px] text-gray-500">
                        {{ person.mutualFriendsCount }} wspólnych znajomych
                      </span>
                    </div>
                    <div
                      v-if="person.inHistory"
                      class="text-[13px] text-blue-600 font-medium mt-0.5"
                    >
                      {{ person.newPostsCount }} nowych postów od ostatniego wyszukiwania
                    </div>
                  </div>
                </div>

                <button
                  class="flex-shrink-0 bg-[#EBF5FF] text-[#0064D1] px-3 py-1.5 rounded-md font-semibold text-[15px] hover:bg-[#D4E9FF] transition"
                >
                  Dodaj znajomego
                </button>
              </div>
            </div>
            <button
              @click="activeFilter = 'people'"
              class="w-full py-2.5 bg-white hover:bg-gray-50 text-[15px] font-semibold text-gray-700 border-t border-gray-100"
            >
              Zobacz wszystkie
            </button>
          </div>

          <PostItem v-for="post in filteredPosts" :key="post.id" :post="post" />
        </div>

        <div v-if="activeFilter === 'people'" class="space-y-3">
          <div
            v-for="person in filteredPeople"
            :key="person.id"
            class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 flex items-center justify-between gap-4"
          >
            <div class="flex items-start gap-4 overflow-hidden">
              <img
                :src="person.avatar"
                class="w-[60px] h-[60px] rounded-full object-cover border border-gray-200 flex-shrink-0"
                alt=""
              />
              <div class="flex flex-col min-w-0 pt-1">
                <h3
                  class="font-semibold text-[17px] text-gray-900 leading-snug cursor-pointer hover:underline mb-1"
                  @click="router.push('/profile/' + person.id)"
                >
                  {{ person.name }}
                </h3>
                <div
                  class="flex items-center gap-2"
                  v-if="person.mutualFriendsCount !== undefined && person.mutualFriendsCount > 0"
                >
                  <span class="text-[13px] text-gray-500">
                    {{ person.mutualFriendsCount }} wspólnych znajomych
                  </span>
                </div>
                <div v-if="person.inHistory" class="text-[13px] text-blue-600 font-medium mt-0.5">
                  {{ person.newPostsCount }} nowych postów od ostatniego wyszukiwania
                </div>
              </div>
            </div>

            <button
              class="flex-shrink-0 bg-[#EBF5FF] text-[#0064D1] px-3 py-1.5 rounded-md font-semibold text-[15px] hover:bg-[#D4E9FF] transition"
            >
              Dodaj znajomego
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
aside::-webkit-scrollbar {
  width: 8px;
}
aside::-webkit-scrollbar-track {
  background: transparent;
}
aside::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 20px;
  border: 3px solid transparent;
  background-clip: content-box;
}
aside:hover::-webkit-scrollbar-thumb {
  background-color: #a0a4a8;
}
</style>
