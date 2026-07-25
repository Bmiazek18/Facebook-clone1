<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { useAuthStore } from '@/stores/auth'

// Import ikon z vue-material-design-icons zgodnie z makietą
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue'
import HomeVariantIcon from 'vue-material-design-icons/HomeVariant.vue'
import MessageTextIcon from 'vue-material-design-icons/MessageText.vue'
import AccountCheckIcon from 'vue-material-design-icons/AccountCheck.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'

import { getUserById } from '@/utils/users'

interface Props {
  userId?: number | string
  name?: string
  mention?: boolean
  comment?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userId: undefined,
  name: '',
  mention: false,
  comment: false,
  disabled: false,
})

const router = useRouter()
const authStore = useAuthStore()
const isInteracted = ref(false)
const fetchedUser = ref<any>(null)
const isLoading = ref(false)

const user = computed(() => {
  if (fetchedUser.value) return fetchedUser.value
  if (!props.userId) return null
  const id = typeof props.userId === 'string' ? parseInt(props.userId) : props.userId
  return getUserById(id)
})

const displayName = computed(() => props.name || user.value?.name || 'Użytkownik')

const handleViewProfile = () => {
  if (props.userId) {
    router.push(`/profile/${props.userId}`)
  }
}

const onShow = async () => {
  isInteracted.value = true
  if (!props.userId) return

  const id = typeof props.userId === 'string' ? parseInt(props.userId) : props.userId
  const mockUser = getUserById(id)
  if (mockUser) {
    fetchedUser.value = mockUser
    return
  }

  if (fetchedUser.value) return

  isLoading.value = true
  try {
    const { client } = useApolloClient()
    const { data } = await client.query({
      query: gql`
        query GetUserByIdForPopper($userId: ID!) {
          getUserById(userId: $userId) {
            id
            firstName
            lastName
            avatarId
            city
            hometown
          }
        }
      `,
      variables: {
        userId: String(props.userId),
      },
    })

    const u = data?.getUserById
    if (u) {
      fetchedUser.value = {
        id: u.id,
        name: [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik',
        avatar: u.avatarId ? `http://localhost:8080/api/users/avatar/${u.avatarId}` : 'http://localhost:8080/api/users/avatar/default-avatar.svg',
        location: u.city || u.hometown || 'Łuków',
        mutualFriendsCount: 0,
      }
    }
  } catch (err) {
    console.error('Failed to fetch user for Popper:', err)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <VMenu
    placement="top-start"
    :delay="{ show: 500, hide: 300 }"
    :distance="12"
    :skidding="0"
    container="body"
    :disabled="disabled || !userId || userId === 0 || userId === '0'"
    @show="onShow"
  >
    <slot>
      <div
        @click="handleViewProfile"
        :class="[
          'cursor-pointer hover:underline inline-block leading-5 w-fit',
          comment
            ? 'text-[15px] font-medium text-[#050505]'
            : mention
              ? 'text-[13px]'
              : 'text-[17px] font-semibold text-[#050505]',
        ]"
      >
        {{ displayName }}
      </div>
    </slot>

    <template #popper="{ hide }">
      <div
        v-if="isInteracted && user"
        class="relative bg-white rounded-xl shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] w-[400px] p-4 text-[#050505] border border-gray-200/50"
      >
        <button
          @click="hide"
          class="absolute top-3 right-3 p-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors rounded-full text-[#050505]"
        >
          <CloseIcon :size="20" />
        </button>

        <div class="flex gap-4 items-start">
          <div class="shrink-0 w-[96px] h-[96px]">
            <img
              :src="user.avatar || 'https://picsum.photos/id/400/96/96'"
              :alt="user.name"
              class="h-[96px] w-[96px] rounded-full object-cover"
            />
          </div>

          <div class="grow min-w-0 pr-6">
            <h2
              @click="handleViewProfile"
              class="text-[20px] font-bold text-[#050505] leading-6 hover:underline cursor-pointer truncate"
            >
              {{ user.name }}
            </h2>

            <div class="mt-1 flex items-start gap-2 text-[15px] text-[#050505] leading-5">
              <AccountMultipleIcon :size="20" class="text-[#65676B] shrink-0 mt-0.5" />
              <div>
                <span class="font-normal text-[#65676B]"
                  >{{ user.mutualFriendsCount || 0 }} wspólnych znajomych</span
                >
              </div>
            </div>

            <div
              v-if="user.location || true"
              class="mt-2 flex items-center gap-2 text-[15px] text-[#050505]"
            >
              <HomeVariantIcon :size="20" class="text-[#65676B] shrink-0" />
              <span>
                <span class="text-[#65676B]">Mieszka w:</span>
                <span class="font-semibold ml-1">{{ user.location || 'Łuków' }}</span>
              </span>
            </div>
          </div>
        </div>

        <div class="mt-4 flex items-center gap-2">
          <!-- Zmieniono h-9 na h-[34px] -->
          <button
            class="flex items-center justify-center gap-2 px-3 h-[34px] text-[15px] font-semibold rounded-md bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] transition active:scale-[0.98]"
          >
            <AccountCheckIcon :size="16" />
            <span>Znajomi</span>
          </button>

          <!-- Zmieniono h-9 na h-[34px] -->
          <button
            @click="router.push('/chat/' + String(props.userId))"
            class="flex-1 flex items-center justify-center gap-2 px-4 h-[34px] text-[15px] font-semibold rounded-md bg-[#1877F2] hover:bg-[#1771E6] text-white transition active:scale-[0.98]"
          >
            <MessageTextIcon :size="16" />
            <span>Wyślij wiadomość</span>
          </button>

          <!-- Zmieniono h-9 na h-[34px] -->
          <button
            class="flex items-center justify-center px-3 h-[34px] rounded-md bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] transition active:scale-[0.98]"
          >
            <DotsHorizontalIcon :size="20" />
          </button>
        </div>
      </div>
    </template>
  </VMenu>
</template>

<style scoped>
.v-popper__popper {
  pointer-events: auto !important;
}

</style>
