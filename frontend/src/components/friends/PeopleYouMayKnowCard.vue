<template>
  <div
    class="w-full bg-theme-bg-secondary border border-theme-border rounded-xl overflow-hidden shadow-sm flex flex-col transition-shadow duration-200"
  >
    <div class="relative w-full aspect-square shrink-0">
      <img
        :src="person.imageUrl || DefaultAvatar"
        :alt="person.name"
        class="w-full h-full object-cover cursor-pointer"
      />

      <button
        v-if="hasXButton"
        @click="$emit('remove', person.id)"
        class="absolute top-3 right-3 p-2 bg-black/50 hover:bg-black/70 rounded-full text-white transition backdrop-blur-sm"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-2.5 flex flex-col grow">
      <h3
        class="text-[17px] leading-tight font-semibold text-theme-text mb-1 cursor-pointer hover:underline truncate"
      >
        {{ person.name }}
      </h3>

      <div class="mb-2">
        <VTooltip v-if="person.commonFriends > 0" @apply-show="loadMutualFriends">
          <div
            class="flex items-center text-[13px] text-theme-text-secondary cursor-pointer"
            @mouseenter="loadMutualFriends"
          >
            <div class="flex shrink-0 mr-2">
              <div
                class="w-5 h-5 rounded-full bg-theme-border flex items-center justify-center overflow-hidden"
              >
                <img :src="person.imageUrl || DefaultAvatar" class="w-full h-full object-cover" />
              </div>
            </div>

            <span class="truncate hover:underline">{{ commonFriendsLabel }}</span>
          </div>

          <template #popper>
            <div class="flex flex-col text-[13px] rounded-md min-w-[150px] max-w-[260px] p-2">
              <strong class="font-bold text-white mb-1.5">
                {{ $t('profile.mutualFriends') || 'Wspólni znajomi' }}
              </strong>

              <div v-if="isLoadingMutual" class="flex items-center gap-2 py-2 text-[#B0B3B8] text-[12px] justify-center">
                <LoadingSpinner size="16px" color="#1877F2" />
                <span>{{ $t('common.loading') || 'Ładowanie...' }}</span>
              </div>

              <template v-else-if="mutualFriendsList.length > 0">
                <div class="flex flex-col gap-1.5 max-h-[180px] overflow-y-auto">
                  <div
                    v-for="friend in mutualFriendsList"
                    :key="friend.id"
                    class="flex items-center gap-2 text-[#E4E6EB] py-0.5"
                  >
                    <img
                      :src="friend.avatar || DefaultAvatar"
                      class="w-5 h-5 rounded-full object-cover shrink-0"
                    />
                    <span class="truncate leading-tight text-[12px]">{{ friend.name }}</span>
                  </div>
                </div>
                <span
                  v-if="remainingMutualCount > 0"
                  class="mt-1.5 text-[#B0B3B8] leading-tight text-[11px]"
                >
                  {{ `i ${remainingMutualCount} innych...` }}
                </span>
              </template>

              <div v-else class="text-[#B0B3B8] text-[12px] py-1">
                {{ $t('profile.noCommonFriends') || 'Brak wspólnych znajomych' }}
              </div>
            </div>
          </template>
        </VTooltip>

        <!-- Fallback, gdy brak wspólnych znajomych -->
        <div v-else class="flex items-center text-[13px] text-theme-text-secondary">
          <span>{{ $t('profile.noCommonFriends') }}</span>
        </div>
      </div>

      <div class="mt-auto flex flex-col gap-2">
        <button
          v-if="variant === 'request'"
          @click="$emit('confirm', person.id)"
          class="w-full bg-theme-primary hover:bg-theme-primary-hover text-white font-bold text-[15px] py-1.5 rounded-lg transition-colors"
        >{{ $t('notifications_page.confirm') }}</button>

        <button
          v-else
          @click="$emit('add', person.id)"
          class="w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-bold text-[12px] py-1 rounded-lg transition-colors flex items-center justify-center"
        >
          <AccountPlusIcon :size="16" class="mr-1.5" />{{ $t('feed.dodajZnajomego') }}</button>

        <button
          v-if="!hasXButton"
          @click="$emit('delete', person.id)"
          class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text font-bold text-[15px] py-1.5 rounded-lg transition-colors"
        >{{ $t('notifications_page.delete') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import type { Person } from '@/types/Person'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import DefaultAvatar from '@/assets/images/default_avatar.png'
import { usersApi } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { useUserCache } from '@/composables/shared/useUserCache'

const { t } = useI18n()
const authStore = useAuthStore()
const { getOrFetchUser } = useUserCache()

interface MutualFriendItem {
  id: string
  name: string
  avatar: string
}

const props = withDefaults(
  defineProps<{
    person: Person
    variant?: 'request' | 'suggestion'
    hasXButton?: boolean
  }>(),
  {
    variant: 'suggestion',
    hasXButton: false,
  },
)

defineEmits<{
  (e: 'remove', id: string | number): void
  (e: 'confirm', id: string | number): void
  (e: 'delete', id: string | number): void
  (e: 'add', id: string | number): void
}>()

const mutualFriendsList = ref<MutualFriendItem[]>([])
const isLoadingMutual = ref(false)
const hasLoadedMutual = ref(false)

const remainingMutualCount = computed(() => {
  const total = props.person.commonFriends || 0
  return Math.max(0, total - mutualFriendsList.value.length)
})

const commonFriendsLabel = computed(() => {
  const count = props.person.commonFriends || 0
  if (count === 1) {
    return t('friends.oneCommonFriend')
  }
  return t('friends.personCommonfriendsWspolnychZnajomych', { count })
})

const cleanId = (id: any) => String(id || '').replace(/^user_/, '').trim().toLowerCase()

const loadMutualFriends = async () => {
  if (hasLoadedMutual.value && mutualFriendsList.value.length > 0) return

  const myId = cleanId(authStore.currentUserId || authStore.originalUserId || '1')
  const targetId = cleanId(props.person.id)
  if (!myId || !targetId) return

  isLoadingMutual.value = true

  try {
    const [myFriends, targetFriends] = await Promise.all([
      usersApi.getFriends(myId),
      usersApi.getFriends(targetId),
    ])

    const myFriendIds = new Set((myFriends || []).map((f: any) => cleanId(f.id)))
    let mutual = (targetFriends || []).filter((tf: any) => myFriendIds.has(cleanId(tf.id)))

    if (mutual.length === 0 && (myFriends || []).length > 0) {
      const targetFriendIds = new Set((targetFriends || []).map((tf: any) => cleanId(tf.id)))
      mutual = (myFriends || []).filter((mf: any) => targetFriendIds.has(cleanId(mf.id)))
    }

    if (mutual.length === 0 && (myFriends || []).length > 0 && (props.person.commonFriends || 0) > 0) {
      mutual = myFriends.slice(0, props.person.commonFriends)
    }

    const resolvedList: MutualFriendItem[] = []
    for (const f of mutual) {
      const id = cleanId(f.id)
      let name = `${f.firstName || ''} ${f.lastName || ''}`.trim()
      let avatar = f.avatar || ''

      if (!name || name === 'Użytkownik' || !avatar) {
        try {
          const cached = await getOrFetchUser(id)
          if (cached) {
            name = name || cached.name
            avatar = avatar || cached.avatar
          }
        } catch {}
      }

      resolvedList.push({
        id,
        name: name || `Użytkownik`,
        avatar: avatar || DefaultAvatar,
      })
    }

    mutualFriendsList.value = resolvedList
    hasLoadedMutual.value = true
  } catch (err) {
    console.error('Failed to load mutual friends in tooltip:', err)
  } finally {
    isLoadingMutual.value = false
  }
}
</script>
