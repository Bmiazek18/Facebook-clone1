<template>
  <div v-if="people.length > 0" class="py-4 pl-4 bg-theme-bg-secondary rounded-none lg:rounded-lg shadow-md mx-0 lg:mx-auto">
    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center text-theme-text">
        <AccountGroupIcon :size="24" class="mr-2" fillColor="var(--color-text-secondary)" />
        <h2 class="text-[15px] font-semibold">{{ $t('home.peopleYouMayKnow') }}</h2>
      </div>

    </div>

    <div class="relative">
      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 left-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-hover transition duration-150 z-10"
        style="margin-left: 24px"
      >
        <ChevronLeftIcon :size="24" fillColor="var(--color-text-secondary)" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-scroll pb-2 h-72 scrollbar-hide overflow-y-hidden"
      >
        <Card
          v-for="person in people"
          :key="person.id"
          :person="person"
          hasXButton
          class="max-w-[180px] shrink-0 mr-3"
          @add="handleAddFriend"
          @delete="removeCard"
        />

        <div
          class="flex flex-col items-center justify-center p-4 cursor-pointer w-49 h-72 border border-theme-border rounded-lg shadow-sm hover:shadow-md transition duration-200 bg-theme-bg-subtle shrink-0 mr-3"
        >
          <PlusCircleIcon
            :size="32"
            class="text-theme-primary mb-2"
            fillColor="var(--color-primary)"
          />
          <span class="text-theme-primary font-semibold text-sm">{{ $t('home.showAll') }}</span>
        </div>
        <div class="shrink-0" style="width: 0.75rem"></div>
      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 right-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-hover transition duration-150 z-10"
        style="margin-right: 24px"
      >
        <ChevronRightIcon :size="24" fillColor="var(--color-text-secondary)" />
      </button>

      <div class="text-center mt-2">
        <button
          class="text-theme-primary font-medium py-1 px-4 rounded hover:bg-theme-primary-subtle transition duration-150"
        >
          {{ $t('home.showAll') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/friends/PeopleYouMayKnowCard.vue'
import type { Person } from '@/types/Person'
import { useCarousel } from '@/composables/media/useCarousel'
import { usersApi } from '@/api/users'
import { useAuthStore } from '@/stores/auth'

// i18n
useI18n()

// --- IMPORT IKON ---
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import PlusCircleIcon from 'vue-material-design-icons/PlusCircle.vue'

const authStore = useAuthStore()

// --- UŻYCIE COMPOSABLE ---
const { carouselRef, isStart, isEnd, scrollLeft, scrollRight, checkScrollState } = useCarousel(2)

const people = ref<Person[]>([])

const fetchSuggestions = async () => {
  try {
    const suggestions = await usersApi.getFriendSuggestions(authStore.currentUserId || '1')

    people.value = (suggestions || []).map((s: any) => {
      const u = s.user
      return {
        id: s.userId,
        name: u ? [u.firstName, u.lastName].filter(Boolean).join(' ') : 'Użytkownik',
        imageUrl: u?.avatar || '/default-avatar.png',
        commonFriends: s.mutualFriendsCount || 0,
        isFriend: false,
      }
    })

    nextTick(() => {
      checkScrollState()
    })
  } catch (err) {
    console.error('Failed to fetch friend suggestions:', err)
  }
}

onMounted(() => {
  fetchSuggestions()
})

// --- METODY KOMPONENTU ---

const handleAddFriend = async (id: string | number) => {
  try {
    await usersApi.sendFriendRequest(authStore.currentUserId || '1', id)
    removeCard(id)
  } catch (err) {
    console.error('Failed to send friend request:', err)
  }
}

const removeCard = (id: string | number) => {
  people.value = people.value.filter((p: Person) => String(p.id) !== String(id))
  nextTick(() => {
    checkScrollState()
  })
}
</script>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
