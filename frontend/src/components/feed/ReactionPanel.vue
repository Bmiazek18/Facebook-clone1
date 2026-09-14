<template>
  <div class="max-w-lg w-[500px] bg-white dark:bg-[#242526] text-theme-text rounded-lg shadow-xl flex flex-col">
    <!-- Nagłówek i zakładki -->
    <div class="flex items-center justify-between px-2 pt-2 border-b border-gray-200 dark:border-gray-700">
      <div
        v-if="totalReactions > 0"
        class="flex overflow-x-auto whitespace-nowrap scrollbar-hide"
      >
        <button
          v-if="Object.keys(reactionSummary).length > 1"
          @click="selectTab(null)"
          :class="[
            'px-4 py-3 text-[15px] font-semibold border-b-[3px] transition-colors duration-200 focus:outline-none cursor-pointer',
            selectedReaction === null
              ? 'border-blue-600 text-blue-600 dark:text-blue-400'
              : 'border-transparent text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700/50 rounded-t-lg',
          ]"
        >
          {{ $t('postFilter.privacyAll') }}
        </button>

        <button
          v-for="(count, emoji) in reactionSummary"
          :key="emoji"
          @click="selectTab(emoji as string)"
          :class="[
            'flex items-center px-4 py-3 text-[15px] font-semibold border-b-[3px] transition-colors duration-200 focus:outline-none cursor-pointer',
            selectedReaction === emoji
              ? 'border-blue-600 text-blue-600 dark:text-blue-400'
              : 'border-transparent text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700/50 rounded-t-lg',
          ]"
        >
          <span class="text-xl mr-1.5">{{ emoji }}</span>
          <span>{{ count }}</span>
        </button>
      </div>

      <div v-else class="py-3 px-4 text-gray-500 font-medium">{{ $t('chat.brakReakcji') }}</div>

      <!-- Przycisk zamykania -->
      <button
        @click="$emit('close')"
        class="w-9 h-9 mr-2 mb-1 shrink-0 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors focus:outline-none cursor-pointer"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600 dark:text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Lista użytkowników -->
    <HoverScrollbar max-height="400px" v-if="totalReactions > 0" class="py-2">
      <div
        v-for="reaction in filteredReactions"
        :key="`${reaction.userId}-${reaction.emoji}`"
        class="flex items-center justify-between px-4 py-2 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors"
      >
        <div class="flex items-center space-x-3 overflow-hidden">
          <!-- Awatar + Plakietka reakcji -->
          <div class="relative w-10 h-10 shrink-0 cursor-pointer">
            <div class="w-full h-full rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center overflow-hidden border border-gray-100 dark:border-gray-600">
              <img
                v-if="reaction.avatarUrl && reaction.avatarUrl !== '/default-avatar.png'"
                :src="reaction.avatarUrl"
                :alt="reaction.userName"
                class="w-full h-full object-cover"
              />
              <span v-else class="text-sm font-bold text-gray-500 dark:text-gray-300 uppercase">
                {{ (reaction.userName || 'U').charAt(0) }}
              </span>
            </div>

            <!-- Mała ikona reakcji na awatarze -->
            <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-white dark:bg-[#242526] rounded-full flex items-center justify-center border border-white dark:border-gray-700 shadow-sm overflow-hidden text-[12px]">
              {{ reaction.emoji }}
            </div>
          </div>

          <!-- Dane użytkownika -->
          <div class="flex flex-col truncate">
            <ProfilePopper
              :userId="reaction.userId"
              :name="reaction.userName"
            />
            <span v-if="reaction.mutualFriends !== undefined && reaction.mutualFriends > 0" class="text-[13px] text-gray-500 truncate">
              {{ reaction.mutualFriends }} {{ getMutualFriendsLabel(reaction.mutualFriends) }}
            </span>
          </div>
        </div>

        <!-- Przycisk Akcji -->
        <button
          class="shrink-0 ml-3 flex items-center cursor-pointer gap-1.5 px-3 py-1.5 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-md transition-colors text-theme-text"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-900 dark:text-gray-100" viewBox="0 0 20 20" fill="currentColor">
            <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
          </svg>
          <span class="text-[15px] font-semibold text-gray-900 dark:text-gray-100">{{ $t('feed.dodajZnajomego') }}</span>
        </button>
      </div>
    </HoverScrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'
import { getUserById } from '@/utils/users'
import { usePostReactions } from '@/composables/feed/usePostReactions'

defineEmits(['close'])

// --- Typy ---
interface ReactionUser {
  userId: string | number
  userName: string
  emoji: string
  avatarUrl?: string
  mutualFriends?: number
}

const props = defineProps<{
  reactions?: Partial<Record<string, (string | number)[]>>
  reactionDetails?: any[]
}>()

const { reactionIcons } = usePostReactions()

const reactionsList = computed<ReactionUser[]>(() => {
  const list: ReactionUser[] = []

  if (Array.isArray(props.reactionDetails) && props.reactionDetails.length > 0) {
    props.reactionDetails.forEach((r: any) => {
      const type = (r.reactionType || '').toLowerCase()
      const emoji = reactionIcons[type]?.emoji || '👍'

      if (Array.isArray(r.users) && r.users.length > 0) {
        r.users.forEach((u: any) => {
          if (u) {
            list.push({
              userId: String(u.id),
              userName: [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik',
              emoji,
              avatarUrl: u.avatar || '/default-avatar.png',
              mutualFriends: u.mutualFriendsCount || 0
            })
          }
        })
      } else if (Array.isArray(r.userIds)) {
        r.userIds.forEach((id: any) => {
          const user = getUserById(String(id))
          if (user) {
            list.push({
              userId: String(user.id),
              userName: user.name,
              emoji,
              avatarUrl: user.avatar || '/default-avatar.png',
              mutualFriends: user.mutualFriendsCount || 0
            })
          } else {
            list.push({
              userId: String(id),
              userName: 'Użytkownik',
              emoji,
              avatarUrl: '/default-avatar.png',
              mutualFriends: 0
            })
          }
        })
      }
    })
    return list
  }

  if (!props.reactions) return list

  for (const [type, userIds] of Object.entries(props.reactions)) {
    if (userIds && Array.isArray(userIds)) {
      userIds.forEach((userId) => {
        const user = getUserById(String(userId))
        if (user) {
          list.push({
            userId: String(user.id),
            userName: user.name,
            emoji: reactionIcons[type?.toLowerCase()]?.emoji || '👍',
            avatarUrl: user.avatar || '/default-avatar.png',
            mutualFriends: user.mutualFriendsCount || 0
          })
        } else {
          list.push({
            userId: String(userId),
            userName: 'Użytkownik',
            emoji: reactionIcons[type?.toLowerCase()]?.emoji || '👍',
            avatarUrl: '/default-avatar.png',
            mutualFriends: 0
          })
        }
      })
    }
  }
  return list
})

const selectedReaction = ref<string | null>(null)
const totalReactions = computed(() => reactionsList.value.length)

const reactionSummary = computed(() => {
  return reactionsList.value.reduce(
    (acc, reaction) => {
      acc[reaction.emoji] = (acc[reaction.emoji] || 0) + 1
      return acc
    },
    {} as Record<string, number>,
  )
})

const filteredReactions = computed(() => {
  let filtered = reactionsList.value

  if (selectedReaction.value !== null) {
    filtered = reactionsList.value.filter((r) => r.emoji === selectedReaction.value)
  }

  return [...filtered].sort((a, b) => a.userName.localeCompare(b.userName, 'pl'))
})

const selectTab = (emoji: string | null) => {
  selectedReaction.value = emoji
}

// Funkcja pomocnicza do poprawnej odmiany słowa "znajomy"
const getMutualFriendsLabel = (count: number): string => {
  if (count === 1) return 'wspólny znajomy'
  return 'wspólnych znajomych'
}
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
