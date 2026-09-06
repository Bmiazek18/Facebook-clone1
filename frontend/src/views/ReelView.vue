<template>
  <div class="flex h-[calc(100vh-56px)] mt-[54px] bg-black overflow-hidden relative w-full">

    <!-- LEWA SEKCJA (SIDEBAR) - Dokładnie 220px szerokości -->
    <aside class="hidden md:flex flex-col w-[220px] h-full pt-[8px] bg-black  shrink-0 select-none">
      <h1 class="text-[#e2e5e9] text-2xl font-bold px-[16px] py-[8px] tracking-tight">{{ $t('feed.rolki') }}</h1>

      <nav class="flex flex-col gap-1 px-[8px]">
        <button
          @click="currentTab = 'for-you'"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg font-semibold text-[15px] transition-colors duration-200 w-full text-left',
            currentTab === 'for-you' ? 'bg-[#242526] text-white' : 'text-[#b0b3b8] hover:bg-[#1c1e21] hover:text-white'
          ]"
        >
          <svg class="w-5 h-5 shrink-0" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
          <span>{{ $t('feed.dlaCiebie') }}</span>
        </button>

        <button
          @click="currentTab = 'following'"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg font-semibold text-[15px] transition-colors duration-200 w-full text-left',
            currentTab === 'following' ? 'bg-[#242526] text-white' : 'text-[#b0b3b8] hover:bg-[#1c1e21] hover:text-white'
          ]"
        >
          <svg class="w-5 h-5 shrink-0" viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
          </svg>
          <span>{{ $t('pages.obserwujesz') }}</span>
        </button>

        <button
          @click="currentTab = 'profile'"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg font-semibold text-[15px] transition-colors duration-200 w-full text-left',
            currentTab === 'profile' ? 'bg-[#242526] text-white' : 'text-[#b0b3b8] hover:bg-[#1c1e21] hover:text-white'
          ]"
        >
          <svg class="w-5 h-5 shrink-0" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
          </svg>
          <span>{{ $t('chat.profil') }}</span>
        </button>
      </nav>
    </aside>

    <!-- ŚRODKOWA SEKCJA (KONTENER Z ROLKAMI) - Idealnie wyśrodkowany w elastycznej przestrzeni -->
    <div
      ref="scrollContainer"
      class="flex-1 h-full overflow-y-scroll snap-y snap-mandatory scrollbar-hide flex flex-col items-center z-10 transition-all duration-300"
    >
      <ReelItem
        v-for="(reel, index) in reels"
        :key="reel.id"
        :reel="reel"
        :data-id="reel.id"
        :ref="(el) => setReelRef(el, index)"
        :isActive="activeReelId === reel.id"
        :isMuted="isGlobalMuted"
        :globalVolume="globalVolume"
        :isCommentsOpen="isCommentsOpen"
        @toggle-comments="isCommentsOpen = !isCommentsOpen"
        @update-mute="isGlobalMuted = $event"
        @update-volume="globalVolume = $event"
        @open-share="handleOpenShare"
      />
    </div>

    <!-- SEKCJA PO PRAWEJ ZE STRZAŁKAMI - Dokładnie 72px szerokości -->
    <div class="hidden sm:flex flex-col justify-center items-center gap-3 w-[72px] h-full shrink-0 bg-black z-30 select-none">
      <button
        @click="goToReel('up')"
        :disabled="activeIndex <= 0"
        class="w-14 h-14 bg-[#18181c]/60 hover:bg-[#242526]/80 text-white disabled:opacity-20 border-[3px] border-[#555] hover:border-[#777] rounded-full flex items-center justify-center transition-all duration-200 active:scale-95"
      >
        <ChevronUpIcon :size="32" />
      </button>
      <button
        @click="goToReel('down')"
        :disabled="!reels || activeIndex >= reels.length - 1"
        class="w-14 h-14 bg-[#18181c]/60 hover:bg-[#242526]/80 text-white disabled:opacity-20 border-[3px] border-[#555] hover:border-[#777] rounded-full flex items-center justify-center transition-all duration-200 active:scale-95"
      >
        <ChevronDownIcon :size="32" />
      </button>
    </div>

    <!-- SKRAJNA PRAWA SEKCJA (PANEL KOMENTARZY) - Płynna animacja szerokości -->
    <div
      :class="[
        'h-full z-20 bg-black transition-all duration-300 ease-in-out overflow-hidden shrink-0',
        isCommentsOpen && currentReel
          ? 'w-full sm:w-[350px] md:w-[400px] lg:w-[460px] border-l border-[#1f1f1f]'
          : 'w-0 border-l-0'
      ]"
    >
      <div class="w-[350px] md:w-[400px] lg:w-[460px] h-full">
        <ReelInfoPanel
          v-if="currentReel"
          :reel="currentReel"
          class="w-full h-full"
        />
      </div>
    </div>

    <!-- MODAL UDOSTĘPNIANIA -->
    <BaseModal v-if="showShareModal" :title="shareModalTitle" @close="showShareModal = false">
      <StoryShareModal :reel="selectedReelToShare" @close="showShareModal = false" />
    </BaseModal>
  </div>
</template>



<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>


<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, provide, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { feedApi } from '@/api/feed'
import { processPostsIntoReels } from '@/utils/reels'
import ReelItem from '../components/feed/reel/ReelItem.vue'
import ReelInfoPanel from '@/components/feed/reel/ReelInfoPanel.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import StoryShareModal from '@/components/feed/stories/StoryShareModal.vue'
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'

const authStore = useAuthStore()

const currentTab = ref('for-you')
const feedData = ref<any[]>([])

const fetchReelsFeed = async () => {
  try {
    const feed = await feedApi.getFeed(authStore.currentUserId)
    feedData.value = feed || []
  } catch (err) {
    console.error('Failed to fetch reels feed:', err)
  }
}

onMounted(() => {
  fetchReelsFeed()
})

watch(() => authStore.currentUserId, () => {
  fetchReelsFeed()
})

const allPosts = computed(() => {
  const feed = feedData.value ?? []
  return feed.map((post: any) => {
    let formattedReactions: Record<string, number[]> = {}
    if (Array.isArray(post.reactions)) {
      post.reactions.forEach((r: any) => {
        formattedReactions[r.reactionType.toLowerCase()] = r.userIds.map(Number)
      })
    } else if (post.reactions) {
      formattedReactions = post.reactions
    }
    return {
      ...post,
      reactions: formattedReactions
    }
  })
})

provide('allPosts', allPosts)

const reels = computed(() => {
  return processPostsIntoReels(allPosts.value, String(authStore.currentUserId))
})

const activeReelId = ref<string | null>(null)
const isGlobalMuted = ref(true)
const globalVolume = ref(1)
const isCommentsOpen = ref(false)
const showShareModal = ref(false)
const shareModalTitle = ref('Udostępnij')
const selectedReelToShare = ref(null)
const reelRefs = ref<any[]>([])

const activeIndex = computed(() => reels.value.findIndex((r) => r.id === activeReelId.value))
const currentReel = computed(() => reels.value[activeIndex.value] || reels.value[0])

let observer: IntersectionObserver | null = null

const setReelRef = (el: any, index: number) => {
  if (el) reelRefs.value[index] = el.$el || el
}

const handleOpenShare = (reel: any) => {
  selectedReelToShare.value = reel
  showShareModal.value = true
}

const goToReel = (direction: 'up' | 'down') => {
  const newIndex = direction === 'up' ? activeIndex.value - 1 : activeIndex.value + 1
  if (newIndex >= 0 && newIndex < reels.value.length) {
    reelRefs.value[newIndex]?.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) activeReelId.value = entry.target.getAttribute('data-id')
      })
    },
    { threshold: 0.6 },
  )

  nextTick(() => {
    reelRefs.value.forEach((el) => el && observer?.observe(el))
  })
})

onUnmounted(() => observer?.disconnect())
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.animate-slide-in {
  animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
