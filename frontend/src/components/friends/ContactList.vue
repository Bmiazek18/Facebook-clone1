<template>
  <div class="bg-theme-bg-secondary rounded-2xl w-full max-w-[500px] pb-2 select-none antialiased">
    <!-- Lista elementów z mocka -->
    <div class="space-y-0.5">
      <div
        v-for="contact in formattedContacts"
        :key="contact.id"
        @click="handleSelect(contact.id)"
        class="flex items-center justify-between px-4 py-1.5 hover:bg-theme-bg-hover transition duration-150 ease-in-out cursor-pointer group"
      >
        <div class="flex items-center space-x-3 min-w-0 flex-1">
          <!-- LEWA STRONA: Ikona zegarka dla stron/grup lub główny awatar dla osób -->
          <div
            v-if="contact.isPage || contact.isGroup"
            class="w-9 h-9 rounded-full bg-theme-bg-tertiary flex items-center justify-center shrink-0 text-theme-text-secondary"
          >
            <svg
              class="w-5 h-5 text-theme-text-secondary"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>

          <!-- Główny awatar (okrągły) dla użytkowników -->
          <div v-else class="relative shrink-0">
            <img
              :src="contact.avatarUrl"
              :alt="contact.name"
              class="w-9 h-9 rounded-full object-cover border border-theme-border"
              loading="lazy"
            />
          </div>

          <!-- ŚRODEK: Nazwa oraz dynamiczny podtytuł -->
          <div class="flex flex-col min-w-0 flex-1 justify-center">
            <span
              class="text-[15px] font-semibold text-theme-text leading-snug text-wrap group-hover:underline"
            >
              {{ contact.name }}
            </span>

            <div
              v-if="contact.subtitle"
              class="flex items-center gap-1.5 text-[13px] font-normal mt-0.5"
            >
              <span
                v-if="contact.isNew"
                class="w-2 h-2 rounded-full bg-[#1877F2] inline-block shrink-0"
              ></span>
              <span class="text-theme-text-secondary">
                {{ contact.subtitle }}
              </span>
            </div>
          </div>

          <!-- PRAWA STRONA: Mały kwadratowy awatar dla stron/grup -->
          <div
            v-if="(contact.isPage || contact.isGroup) && contact.avatarUrl"
            class="relative shrink-0 mr-1"
          >
            <img
              :src="contact.avatarUrl"
              :alt="contact.name"
              class="w-9 h-9 object-cover border border-theme-border rounded-xl"
              loading="lazy"
            />
          </div>
        </div>

        <!-- Przycisk usuwania (X) -->
        <button
          @click.stop="handleRemove(contact.id)"
          class="p-1.5 text-theme-text-secondary hover:bg-theme-bg-tertiary rounded-full transition ml-1 shrink-0"
          title="Usuń"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'nuxt/app'
import { useAuthStore } from '@/stores/auth'
import { getApolloClient } from '@/utils/apollo'
import { GET_FRIENDS, GET_FRIEND_SUGGESTIONS } from '@/graphql/friends'
import { SEARCH_USERS } from '@/graphql/search'

const props = withDefaults(
  defineProps<{
    searchQuery?: string
  }>(),
  {
    searchQuery: '',
  }
)

const router = useRouter()
const authStore = useAuthStore()

const contacts = ref<any[]>([])

const fetchContacts = async () => {
  const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce')
  const apolloClient = getApolloClient()
  
  try {
    // 1. Fetch real friends from Neo4j (via getFriends query)
    const { data } = await apolloClient.query({
      query: GET_FRIENDS,
      variables: { userId: currentUserId },
      fetchPolicy: 'network-only'
    })
    
    if (data?.getFriends && data.getFriends.length > 0) {
      contacts.value = data.getFriends
      return
    }
  } catch (err) {
    console.error('Failed to fetch friends:', err)
  }

  // 2. Fallback: If no friends yet, load suggestions (people you may know)
  try {
    const { data: suggestionData } = await apolloClient.query({
      query: GET_FRIEND_SUGGESTIONS,
      variables: { currentUserId },
      fetchPolicy: 'network-only'
    })
    
    if (suggestionData?.getFriendSuggestions) {
      contacts.value = suggestionData.getFriendSuggestions
        .filter((s: any) => s.user)
        .map((s: any) => s.user)
    }
  } catch (err) {
    console.error('Failed to fetch friend suggestions:', err)
  }
}

onMounted(() => {
  fetchContacts()
})

// Watch for user changes so contacts are re-fetched when switching accounts
watch(
  () => authStore.currentUserId,
  () => {
    fetchContacts()
  }
)

const searchResults = ref<any[]>([])
const isSearching = ref(false)

const fetchSearchResults = async (query: string) => {
  if (!query.trim()) {
    searchResults.value = []
    return
  }
  isSearching.value = true
  try {
    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce')
    const { data } = await apolloClient.query({
      query: SEARCH_USERS,
      variables: {
        query,
        currentUserId,
      }
    })
    searchResults.value = data?.searchUsers || []
  } catch (err) {
    console.error('Failed to search users:', err)
  } finally {
    isSearching.value = false
  }
}

watch(
  () => props.searchQuery,
  (newQuery) => {
    if (newQuery && newQuery.trim()) {
      fetchSearchResults(newQuery)
    } else {
      searchResults.value = []
    }
  }
)

const formattedContacts = computed(() => {
  const isQueryEmpty = !(props.searchQuery || '').trim()
  const listToMap = isQueryEmpty ? contacts.value : searchResults.value

  return listToMap.map((user) => {
    let subtitle = ''
    let isNew = false

    if (user.newPostsCount && user.newPostsCount > 0) {
      const lastDigit = user.newPostsCount % 10
      const lastTwoDigits = user.newPostsCount % 100

      if (user.newPostsCount === 1) {
        subtitle = '1 nowy post'
      } else if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 10 || lastTwoDigits >= 20)) {
        subtitle = `${user.newPostsCount} nowe posty`
      } else {
        subtitle = `${user.newPostsCount} nowych postów`
      }
      isNew = true
    } else {
      subtitle = isQueryEmpty ? 'Aktywny(a)' : ''
    }

    const avatarUrl = user.avatar || '/default-avatar.png'

    return {
      id: user.id,
      name: `${user.firstName} ${user.lastName}`.trim(),
      avatarUrl: avatarUrl,
      subtitle: subtitle,
      isNew: isNew,
      isPage: false,
      isGroup: false,
    }
  })
})

const handleSelect = (id: string | number) => {
  router.push(`/profile/${id}`)
}

const handleRemove = (id: string | number) => {
  contacts.value = contacts.value.filter((item) => String(item.id) !== String(id))
}
</script>
