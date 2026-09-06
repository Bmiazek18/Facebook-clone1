<script setup lang="ts">
import { ref, computed, inject } from 'vue'
import { useRoute } from 'vue-router'
import CreateBox from '@/components/create/createPost/CreateBox.vue'
import ProfileMiniGallery from '@/components/profile/ProfileMiniGallery.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import PostFilter from './PostFilter.vue'
import { usePostsStore } from '@/composables/feed/useAppState'
import BirthdayPostFeed from '@/components/BirthdayPostFeed.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PostModal from '@/components/feed/post/PostModal.vue'
import { getUserById } from '@/utils/users'

import ProfileIntroCard from './ProfileIntroCard.vue'
import ProfileGridItem from './ProfileGridItem.vue'
import { useStickySidebar } from '@/composables/ui/useStickySidebar'

const isOwner = inject('isOwner', ref(false))
const profileUser = inject<any>('profileUser')

const props = defineProps({
  friendsList: {
    type: Array,
    required: true,
  },
  miniPhotosList: {
    type: Array,
    required: true,
  },
  userName: {
    type: String,
    required: true,
  },
  userImage: {
    type: String,
    required: true,
  },
})

const photoItems = computed(() => {
  return ((props.miniPhotosList as number[]) || []).slice(0, 9).map((id) => ({
    id,
    imageUrl: `https://picsum.photos/id/${id}/200/200`,
  }))
})

const friendItems = computed(() => {
  return ((props.friendsList as any[]) || []).slice(0, 9).map((friend) => ({
    id: friend.id,
    imageUrl: friend.avatar,
    name: friend.name,
    mutualFriendsCount: friend.mutualFriendsCount,
  }))
})

const route = useRoute()
const postsStore = usePostsStore()
const activeView = ref('list')

const HEADER_OFFSET = 110
const BOTTOM_OFFSET = 16
const leftSectionRef = ref<HTMLElement | null>(null)
const { stickyTop } = useStickySidebar(leftSectionRef, HEADER_OFFSET, BOTTOM_OFFSET)

const targetId = computed(() => route.params.userId as string)
const effectiveTargetId = computed(() => (route.params.userId as string) || '1')

const selectedPost = ref<any | null>(null)
const selectedGroupPosts = ref<any[] | null>(null)
const selectedGroupTitle = ref<string>('')

const openPostModal = (post: any) => {
  selectedPost.value = post
}

const openGroupModal = (postsList: any[], title: string) => {
  selectedGroupPosts.value = postsList
  selectedGroupTitle.value = title
}

const groupedPostsByMonth = computed(() => {
  const userPosts = postsStore.posts.filter(
    (p) =>
      p.authorId === parseInt(effectiveTargetId.value) ||
      (p.targetType === 'User' && p.targetId === effectiveTargetId.value),
  )
  const grouped = userPosts.reduce(
    (acc, post) => {
      const date = new Date(post.date)
      const monthYear = date.toLocaleString('pl-PL', { month: 'long', year: 'numeric' })
      if (!acc[monthYear]) {
        acc[monthYear] = []
      }
      acc[monthYear].push(post)
      return acc
    },
    {} as Record<string, typeof userPosts>,
  )
  return grouped
})

const gridItemsByMonth = computed(() => {
  const result: Record<string, any[]> = {}

  for (const [monthYear, posts] of Object.entries(groupedPostsByMonth.value)) {
    const items: any[] = []
    const wallPostsByDate: Record<string, any[]> = {}

    // 1. Grupuj posty urodzinowe w danym miesiącu
    const birthdayPosts = posts.filter((p) => p.isBirthday)
    if (birthdayPosts.length > 0) {
      const avatars = birthdayPosts
        .map(
          (p) =>
            p.authorAvatar ||
            getUserById(p.authorId)?.avatar ||
            `https://picsum.photos/seed/${p.authorId}/150/150`,
        )
        .slice(0, 4)

      items.push({
        type: 'birthday',
        count: birthdayPosts.length,
        date: birthdayPosts[0].date,
        avatars: avatars,
        posts: birthdayPosts,
      })
    }

    // 2. Filtruj nieurodzinowe posty
    const nonBirthdayPosts = posts.filter((p) => !p.isBirthday)
    nonBirthdayPosts.forEach((post) => {
      // Sprawdzamy, czy to post kogoś innego na tablicy użytkownika
      if (post.authorId !== parseInt(effectiveTargetId.value) && post.targetType === 'User') {
        // Grupujemy po dacie (bez uwzględniania godzin)
        const dateObj = new Date(post.date)
        const dateString = `${dateObj.getFullYear()}-${dateObj.getMonth()}-${dateObj.getDate()}`

        if (!wallPostsByDate[dateString]) {
          wallPostsByDate[dateString] = []
        }
        wallPostsByDate[dateString].push(post)
      } else {
        // Własne posty lub posty ze zdjęciami traktujemy jako pojedyncze
        items.push({ type: 'single', data: post })
      }
    })

    // Tworzenie kart dla zgrupowanych postów z tablicy
    for (const [dateString, dayPosts] of Object.entries(wallPostsByDate)) {
      if (dayPosts.length === 1) {
        // Jeśli tylko jedna osoba napisała, zostawiamy jako pojedynczy post
        items.push({ type: 'single', data: dayPosts[0] })
      } else {
        // Pobieramy unikalne awatary autorów (maksymalnie 4 do podglądu)
        const avatars = dayPosts
          .map(
            (p) =>
              p.authorAvatar ||
              getUserById(p.authorId)?.avatar ||
              `https://picsum.photos/seed/${p.authorId || Math.random()}/150/150`,
          )
          .slice(0, 4)

        items.push({
          type: 'aggregated',
          count: dayPosts.length,
          date: dayPosts[0].date, // bierzemy datę z pierwszego posta
          avatars: avatars,
          posts: dayPosts,
        })
      }
    }

    // Sortowanie wszystkich elementów po dacie (od najnowszych)
    items.sort((a, b) => {
      const dateA = new Date(a.type === 'single' ? a.data.date : a.date).getTime()
      const dateB = new Date(b.type === 'single' ? b.data.date : b.date).getTime()
      return dateB - dateA
    })

    result[monthYear] = items
  }

  return result
})

const sortedGridMonths = computed(() => {
  const keys = Object.keys(gridItemsByMonth.value)
  keys.sort((a, b) => {
    const itemsA = gridItemsByMonth.value[a]
    const itemsB = gridItemsByMonth.value[b]
    if (!itemsA || itemsA.length === 0) return 1
    if (!itemsB || itemsB.length === 0) return -1

    const dateA = new Date(
      itemsA[0].type === 'single' ? itemsA[0].data.date : itemsA[0].date,
    ).getTime()
    const dateB = new Date(
      itemsB[0].type === 'single' ? itemsB[0].data.date : itemsB[0].date,
    ).getTime()
    return dateB - dateA
  })

  return keys.map((key) => ({
    monthYear: key,
    items: gridItemsByMonth.value[key],
  }))
})

const hasBirthdayPosts = computed(() => {
  return postsStore.posts.some(
    (p) =>
      p.isBirthday &&
      (p.targetId === effectiveTargetId.value || p.authorId === parseInt(effectiveTargetId.value)),
  )
})

const filteredListPosts = computed(() => {
  return postsStore.posts.filter(
    (p) =>
      !p.isBirthday &&
      (p.authorId === parseInt(effectiveTargetId.value) ||
        (p.targetType === 'User' && p.targetId === effectiveTargetId.value)),
  )
})

const handleViewChanged = (view: string) => {
  activeView.value = view
}

const handleDeletePost = (postId: number) => {
  postsStore.deletePost(postId)
}
</script>

<template>
  <div
    class="flex flex-col md:flex-row w-full justify-between items-start relative max-w-[1250px] mx-auto px-4"
  >
    <div
      id="LeftSection"
      ref="leftSectionRef"
      class="w-full md:w-[40%] mt-4 md:sticky md:z-10 self-start"
      :style="{ top: `${stickyTop}px` }"
    >
      <ProfileIntroCard :profile-user="profileUser" :is-owner="isOwner" />

      <ProfileMiniGallery
        :title="$t('postFilter.privacyFriends')"
        :subtitle="`${friendsList?.length || 0} znajomych`"
        action-text="Pokaż wszystkich znajomych"
        :items="friendItems"
      />
      <ProfileMiniGallery :title="$t('profile.tabs.photos')" action-text="Zobacz wszystkie" :items="photoItems" />

      <div class="mt-4 text-[13px] text-gray-500 px-2 pb-4">{{ $t('profile.prywatnoscRegulaminReklamaPliki') }}</div>
    </div>

    <div id="ContentSection" class="w-full md:w-[58%] min-h-screen pb-20">
      <CreateBox :target-id="targetId" target-type="User" />
      <PostFilter @view-changed="handleViewChanged" :is-owner="isOwner" />

      <template v-if="activeView === 'list'">
        <PostItem
          v-for="post in filteredListPosts"
          :key="post.id"
          class="mt-4"
          :post="post"
          @delete="handleDeletePost"
        />
        <BirthdayPostFeed v-if="hasBirthdayPosts" />
      </template>

      <template v-else-if="activeView === 'grid'">
        <div class="space-y-6 mt-4">
          <div
            v-for="{ monthYear, items } in sortedGridMonths"
            :key="monthYear"
            class="bg-theme-bg-secondary p-4 rounded-xl shadow-sm border border-theme-border"
          >
            <h3 class="text-theme-text text-xl font-bold mb-4">{{ monthYear }}</h3>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <div v-for="(item, index) in items" :key="index">
                <ProfileGridItem
                  :item="item"
                  :user-name="userName"
                  :user-image="userImage"
                  :is-owner="isOwner"
                  @click-single="openPostModal"
                  @click-group="openGroupModal"
                />
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Modale do podglądu postów -->
      <BaseModal
        v-if="selectedPost"
        @close="selectedPost = null"
        :title="`Post ${getUserById(selectedPost.authorId)?.name || ''}`"
      >
        <PostModal :post="selectedPost" />
      </BaseModal>

      <BaseModal
        v-if="selectedGroupPosts"
        @close="selectedGroupPosts = null"
        :title="selectedGroupTitle"
      >
        <div class="space-y-4 max-h-[80vh] overflow-y-auto p-4 bg-theme-bg">
          <PostItem
            v-for="post in selectedGroupPosts"
            :key="post.id"
            :post="post"
            :is-in-modal="true"
          />
        </div>
      </BaseModal>
    </div>
  </div>
</template>
