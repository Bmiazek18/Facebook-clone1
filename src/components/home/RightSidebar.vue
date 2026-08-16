<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Magnify from 'vue-material-design-icons/Magnify.vue'
import CheckCircle from 'vue-material-design-icons/CheckCircle.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import { useTheme } from '@/composables/shared/useTheme'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import SponsoredAds from './SponsoredAds.vue'
import FriendRequestsWidget from './FriendRequestsWidget.vue'

const { isDark } = useTheme()
const chatStore = useChatStore()
const authStore = useAuthStore()

const props = defineProps<{
  friends: any[]
  birthdayUsers: any[]
}>()

const birthdayText = ref('')
const activeContacts = ref<any[]>([])

const updateBirthdayText = () => {
  const users = props.birthdayUsers || []
  if (users.length > 0) {
    const names = users.map((bu: any) =>
      bu.user ? `${bu.user.firstName || ''} ${bu.user.lastName || ''}`.trim() || `Użytkownik ${bu.userId}` : `Użytkownik ${bu.userId}`,
    )

    if (names.length === 1) {
      birthdayText.value = names[0] || ''
    } else if (names.length === 2) {
      birthdayText.value = `${names[0]} i ${names[1]}`
    } else {
      birthdayText.value = `${names.slice(0, -1).join(', ')} i ${names[names.length - 1]}`
    }
  } else {
    birthdayText.value = ''
  }
}

const fetchActiveStatuses = async () => {
  const list = props.friends || []
  if (list.length === 0) {
    activeContacts.value = []
    return
  }

  const contactUserIds = list.map((friend: any) => String(friend.id))

  try {
    const statusResponse = await fetch('http://localhost:8080/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `
          query GetActiveStatuses($userIds: [ID!]!) {
            getActiveStatuses(userIds: $userIds) {
              userId
              active
              lastActiveText
            }
          }
        `,
        variables: { userIds: contactUserIds },
      }),
    })

    const statusJson = await statusResponse.json()
    const statuses = statusJson.data?.getActiveStatuses || []
    const activeStatusMap = new Map<string, boolean>()
    statuses.forEach((s: any) => {
      activeStatusMap.set(String(s.userId), s.active)
    })

    activeContacts.value = list.map((u: any) => {
      const isActive = activeStatusMap.get(String(u.id)) === true
      const avatarUrl = u.avatar || '/default-avatar.png'

      return {
        id: isNaN(Number(u.id)) ? String(u.id) : Number(u.id),
        name: `${u.firstName} ${u.lastName}`,
        avatarUrl: avatarUrl,
        status: isActive,
      }
    })
  } catch (err) {
    console.error('Failed to fetch active statuses:', err)
    activeContacts.value = list.map((u: any) => ({
      id: isNaN(Number(u.id)) ? String(u.id) : Number(u.id),
      name: `${u.firstName} ${u.lastName}`,
      avatarUrl: u.avatar || '/default-avatar.png',
      status: false,
    }))
  }
}

const openChatBox = (id: string | number) => {
  chatStore.addMessageBox(id)
}

onMounted(() => {
  updateBirthdayText()
  fetchActiveStatuses()
})

watch(
  () => props.birthdayUsers,
  () => {
    updateBirthdayText()
  },
  { deep: true, immediate: true }
)

watch(
  () => props.friends,
  () => {
    fetchActiveStatuses()
  },
  { deep: true, immediate: true }
)
</script>

<template>
  <div
    class="max-w-[360px] min-w-[280px] ml-auto fixed top-[56px] overflow-hidden"
  >
    <HoverScrollbar maxHeight="100%">
      <div class="pr-2 pl-2 mt-2 pb-4 select-none flex flex-col gap-3">
        <SponsoredAds />
<FriendRequestsWidget/>
        <!-- Sekcja urodzin (pokazywana tylko, jeśli ktoś ma dzisiaj urodziny) -->
        <div v-if="birthdayText" class="pt-2 pb-4 border-b border-theme-border">
          <div class="text-[17px] font-semibold text-theme-text-secondary">
            {{ $t('home.birthday') }}
          </div>
          <div
            class="flex items-center gap-2 p-2 hover:bg-theme-hover rounded-lg cursor-pointer mt-1"
          >
            <span class="text-2xl">🎁</span>
            <p class="text-theme-text text-sm leading-snug">
              <span class="font-semibold">{{ birthdayText }}</span> {{ $t('home.birthdayHas') }}.
            </p>
          </div>
        </div>

        <!-- Sekcja kontaktów (tylko aktywni znajomi) -->
        <div class="pt-2">
          <div class="flex items-center justify-between pb-1">
            <span class="text-[17px] font-semibold text-theme-text-secondary antialiased">
              Kontakty
            </span>
            <div class="flex items-center gap-1">
              <div
                class="p-2 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full cursor-pointer transition-colors"
              >
                <Magnify :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
              </div>
              <div
                class="p-2 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full cursor-pointer transition-colors"
              >
                <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-0.5 mt-1">
            <div
              v-for="contact in activeContacts"
              :key="contact.id"
              @click="openChatBox(contact.id)"
              class="flex items-center p-2 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-800 cursor-pointer transition-colors group relative"
            >
              <div class="relative shrink-0">
                <img
                  class="w-[38px] h-[38px] rounded-full object-cover border border-gray-200 dark:border-gray-700 shadow-sm"
                  :src="contact.avatarUrl"
                  alt="Avatar"
                />
                <div
                  v-if="contact.status"
                  class="absolute bottom-0 right-0 w-3 h-3 bg-[#24832c] rounded-full border-2 border-white dark:border-[#18191A]"
                ></div>
              </div>

              <div class="flex items-center ml-3 overflow-hidden">
                <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] font-medium truncate">
                  {{ contact.name }}
                </span>
              </div>
            </div>

            <!-- Komunikat, gdy brak aktywnych znajomych -->
            <div
              v-if="activeContacts.length === 0"
              class="p-4 text-center text-sm text-theme-text-secondary italic"
            >
              Brak aktywnych znajomych online
            </div>
          </div>
        </div>
      </div>
    </HoverScrollbar>
  </div>
</template>

<style scoped></style>
