<template>
  <!-- Usunięto sztywną szerokość w-[375px], by modal mógł się dostosować,
       ale możesz dodać class="w-[500px]" w zależności od potrzeb -->
  <div class=" max-w-lg w-[500px] bg-white rounded-lg shadow-xl flex flex-col">
    <!-- Nagłówek i zakładki -->
    <div class="flex items-center justify-between px-2 pt-2 border-b border-gray-200">
      <div
        v-if="totalReactions > 0"
        class="flex overflow-x-auto whitespace-nowrap scrollbar-hide"
      >
       <button
  v-if="Object.keys(reactionSummary).length > 1"
  @click="selectTab(null)"
  :class="[
    'px-4 py-3 text-[15px] font-semibold border-b-[3px] transition-colors duration-200 focus:outline-none',
    selectedReaction === null
      ? 'border-blue-600 text-blue-600'
      : 'border-transparent text-gray-600 hover:bg-gray-100 rounded-t-lg',
  ]"
>
  Wszystkie
</button>

        <button
          v-for="(count, emoji) in reactionSummary"
          :key="emoji"
          @click="selectTab(emoji as string)"
          :class="[
            'flex items-center px-4 py-3 text-[15px] font-semibold border-b-[3px] transition-colors duration-200 focus:outline-none',
            selectedReaction === emoji
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-gray-600 hover:bg-gray-100 rounded-t-lg',
          ]"
        >
          <span class="text-xl mr-1.5">{{ emoji }}</span>
          <span>{{ count }}</span>
        </button>
      </div>

      <div v-else class="py-3 px-4 text-gray-500 font-medium">Brak reakcji</div>

      <!-- Przycisk zamykania -->
      <button
        @click="$emit('close')"
        class="w-9 h-9 mr-2 mb-1 shrink-0 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 transition-colors focus:outline-none"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Lista użytkowników -->
    <HoverScrollbar max-height="400px" v-if="totalReactions > 0" class="py-2">
      <div
        v-for="reaction in filteredReactions"
        :key="`${reaction.userId}-${reaction.emoji}`"
        class="flex items-center justify-between px-4 py-2 hover:bg-gray-50 transition-colors"
      >
        <div class="flex items-center space-x-3 overflow-hidden">

          <!-- Awatar + Plakietka reakcji -->
          <div class="relative w-10 h-10 shrink-0 cursor-pointer">
            <div class="w-full h-full rounded-full bg-gray-200 flex items-center justify-center overflow-hidden border border-gray-100">
              <img
                v-if="reaction.avatarUrl"
                :src="reaction.avatarUrl"
                :alt="reaction.userName"
                class="w-full h-full object-cover"
              />
              <span v-else class="text-sm font-bold text-gray-500 uppercase">
                {{ reaction.userName.charAt(0) }}
              </span>
            </div>

            <!-- Mała ikona reakcji na awatarze -->
            <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-white rounded-full flex items-center justify-center border border-white shadow-sm overflow-hidden text-[12px]">
              {{ reaction.emoji }}
            </div>
          </div>

          <!-- Dane użytkownika -->
          <div class="flex flex-col truncate">
            <ProfilePopper
              :userId="reaction.userId"
              :name="reaction.userName"
comment
            />
            <span v-if="reaction.mutualFriends !== undefined" class="text-[13px] text-gray-500 truncate">
              {{ reaction.mutualFriends }} {{ getMutualFriendsLabel(reaction.mutualFriends) }}
            </span>
          </div>
        </div>

        <!-- Przycisk Akcji -->
        <button
          class="shrink-0 ml-3 flex items-center cursor-pointer gap-1.5 px-3 py-1.5 bg-gray-200 hover:bg-gray-300 rounded-md transition-colors"
        >
          <!-- Ikona 'Dodaj znajomego' -->
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-900" viewBox="0 0 20 20" fill="currentColor">
            <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
          </svg>
          <span class="text-[15px] font-semibold text-gray-900">Dodaj znajomego</span>
        </button>
      </div>
    </HoverScrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import { getUserById } from '@/utils/users'
import { usePostReactions } from '@/composables/feed/usePostReactions'

defineEmits(['close'])

// --- Typy ---
interface ReactionUser {
  userId: number
  userName: string
  emoji: string
  avatarUrl?: string
  mutualFriends?: number // Dodano obsługę wspólnych znajomych
}

const props = defineProps<{
  reactions: Partial<Record<string, number[]>>
  reactionDetails?: any[]
}>()

const { reactionIcons } = usePostReactions()

const reactionsList = computed<ReactionUser[]>(() => {
  const list: ReactionUser[] = []

  if (Array.isArray(props.reactionDetails) && props.reactionDetails.length > 0) {
    props.reactionDetails.forEach((r: any) => {
      const type = r.reactionType.toLowerCase()
      const emoji = reactionIcons[type]?.emoji || '👍'

      if (Array.isArray(r.users)) {
        r.users.forEach((u: any) => {
          if (u) {
            list.push({
              userId: Number(u.id),
              userName: [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik',
              emoji,
              avatarUrl: u.avatar || '/default-avatar.png',
              mutualFriends: u.mutualFriendsCount || 0
            })
          }
        })
      } else if (Array.isArray(r.userIds)) {
        r.userIds.forEach((id: any) => {
          const user = getUserById(Number(id))
          if (user) {
            list.push({
              userId: user.id,
              userName: user.name,
              emoji,
              avatarUrl: user.avatar,
              mutualFriends: user.mutualFriendsCount || 0
            })
          }
        })
      }
    })
    return list
  }

  if (!props.reactions) return list

  for (const [type, userIds] of Object.entries(props.reactions)) {
    if (userIds) {
      userIds.forEach((userId) => {
        const user = getUserById(userId)
        if (user) {
          list.push({
            userId: user.id,
            userName: user.name,
            emoji: reactionIcons[type]?.emoji || '👍',
            avatarUrl: user.avatar,
            mutualFriends: user.mutualFriendsCount || 0
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
/* Ukrycie domyślnego paska przewijania w zakładkach, zachowując możliwość scrollowania */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
