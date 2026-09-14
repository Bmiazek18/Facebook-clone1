<template>
  <div class="flex h-screen overflow-hidden bg-theme-bg text-theme-text">
    <FriendsSidebar />

    <main class="flex-1 h-full mt-14 overflow-y-auto relative ml-[360px]">
      <div class="max-w-[1000px] flex flex-col mx-auto px-4 py-8">
        <!-- Loading state -->
        <div v-if="isLoading" class="flex justify-center items-center py-20">
          <LoadingSpinner :size="36" color="#1877F2" />
        </div>

        <div v-else class="space-y-5">
          <LazyEmojiPicker
            v-if="showEmojiPicker"
            :target="emojiPickerTarget"
            @picked="handleEmoji"
            @close="showEmojiPicker = false"
          />

          <!-- Dzisiejsze urodziny -->
          <div
            v-if="todayBirthdays.length > 0"
            class="bg-theme-bg-secondary rounded-lg shadow-sm p-4 border border-theme-border"
          >
            <h2 class="text-[20px] font-bold text-theme-text mb-4">{{ t('birthday.today') }}</h2>

            <div v-for="user in todayBirthdays" :key="user.id" class="mb-4 last:mb-0">
              <div class="flex items-start">
                <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-3" />

                <div class="flex-1">
                  <div class="flex justify-between items-baseline mb-1">
                    <h3 class="text-[16px] font-semibold text-theme-text">{{ user.name }}</h3>
                    <span v-if="user.age" class="text-[13px] text-theme-text-secondary">
                      {{ user.age }} {{ t('birthday.years') }}
                    </span>
                  </div>

                  <div class="flex items-center space-x-2 mb-2 mt-2">
                    <div class="flex-1 relative">
                      <input
                        type="text"
                        v-model="user.wishText"
                        :placeholder="`${t('birthday.happyBirthday')}, ${user.firstName}!`"
                        class="w-full bg-theme-bg-tertiary rounded-full py-2.5 pl-4 pr-10 text-[15px] text-theme-text focus:outline-none focus:ring-1 focus:ring-blue-500 border border-theme-border"
                        @keyup.enter="sendWish(user)"
                      />
                      <button
                        @click="onEmojiClick(user, $event)"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-theme-text-secondary hover:text-theme-text"
                      >
                        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                          />
                        </svg>
                      </button>
                    </div>
                    <button
                      @click="sendWish(user)"
                      class="text-blue-500 hover:bg-blue-500/10 p-2 rounded-full transition-colors flex items-center justify-center"
                    >
                      <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                      </svg>
                    </button>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="(reply, idx) in defaultQuickReplies"
                      :key="idx"
                      @click="user.wishText = reply"
                      class="px-3 py-1.5 rounded-full border border-theme-border text-[14px] text-theme-text hover:bg-theme-hover transition-colors bg-theme-bg-tertiary"
                    >
                      {{ reply }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Ostatni solenizanci -->
          <div
            v-if="recentBirthdays.length > 0"
            class="bg-theme-bg-secondary rounded-lg shadow-sm p-5 border border-theme-border"
          >
            <h2 class="text-[20px] font-bold text-theme-text mb-6">{{ $t('birthday.ostatniSolenizanci') }}</h2>

            <div
              v-for="user in recentBirthdays"
              :key="user.id"
              class="flex items-start mb-6 border-b border-theme-border pb-6 last:mb-0 last:border-0 last:pb-0"
            >
              <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-4" />
              <div class="flex-1">
                <div class="flex justify-between items-start mb-1">
                  <div>
                    <h3 class="text-[16px] font-bold text-theme-text">{{ user.name }}</h3>
                    <p class="text-[13px] text-theme-text-secondary">{{ user.dateLabel }}</p>
                  </div>
                  <span v-if="user.age" class="text-[13px] text-theme-text-secondary">{{ user.age }} lat</span>
                </div>

                <div class="flex items-center space-x-3 mb-3 mt-3">
                  <div class="flex-1 relative">
                    <input
                      type="text"
                      v-model="user.wishText"
                      :placeholder="`Wszystkiego najlepszego z okazji urodzin, ${user.firstName}! 🥂💐☕`"
                      class="w-full bg-theme-bg-tertiary rounded-full py-2.5 pl-4 pr-10 text-[15px] text-theme-text focus:outline-none border border-theme-border"
                      @keyup.enter="sendWish(user)"
                    />
                    <button
                      @click="onEmojiClick(user, $event)"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-theme-text-secondary hover:text-theme-text"
                    >
                      <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <path
                          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm3.5 8c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5-1.5-.67-1.5-1.5.67-1.5 1.5-1.5zm-7 0c.83 0 1.5.67 1.5 1.5S9.33 13 8.5 13 7 12.33 7 11.5 7.67 10 8.5 10zm3.5 9.5c-2.33 0-4.31-1.46-5.11-3.5h10.22c-.8 2.04-2.78 3.5-5.11 3.5z"
                        />
                      </svg>
                    </button>
                  </div>
                  <button
                    @click="sendWish(user)"
                    class="text-blue-600 hover:bg-blue-500/10 p-2 rounded-full transition-colors flex items-center justify-center"
                  >
                    <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                    </svg>
                  </button>
                </div>

                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="(reply, idx) in defaultQuickReplies"
                    :key="idx"
                    @click="user.wishText = reply"
                    class="px-4 py-1.5 rounded-full border border-theme-border text-[14px] text-theme-text hover:bg-theme-hover transition-colors bg-theme-bg-tertiary max-w-full truncate"
                  >
                    {{ reply }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Nadchodzące urodziny -->
          <div
            v-if="upcomingBirthdays.length > 0"
            class="bg-theme-bg-secondary rounded-lg shadow-sm p-5 border border-theme-border"
          >
            <h2 class="text-[20px] font-bold text-theme-text mb-6">{{ $t('birthday.nadchodzaceUrodziny') }}</h2>
            <div class="space-y-4">
              <div
                v-for="user in upcomingBirthdays"
                :key="user.id"
                class="flex items-center justify-between"
              >
                <div class="flex items-center">
                  <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-4" />
                  <div>
                    <h3 class="text-[16px] font-bold text-theme-text">{{ user.name }}</h3>
                    <p class="text-[13px] text-theme-text-secondary">
                      {{ user.dateLabel }}
                      <span v-if="user.age">· {{ user.age }} lat</span>
                    </p>
                  </div>
                </div>
                <button
                  @click="navigateToMessages(user)"
                  class="bg-theme-bg-tertiary hover:bg-theme-hover text-theme-text font-semibold py-2 px-4 rounded-lg text-[15px] flex items-center transition-colors border border-theme-border"
                >
                  <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
                    <path
                      d="M12 2C6.477 2 2 6.14 2 11.25c0 2.91 1.5 5.51 3.84 7.23v3.13c0 .38.44.6.75.38l3.15-2.02c.73.2 1.48.31 2.26.31 5.523 0 10-4.14 10-9.25S17.523 2 12 2zm1.09 12.31l-2.5-2.67-4.8 2.67 5.27-5.59 2.5 2.67 4.8-2.67-5.27 5.59z"
                    />
                  </svg>{{ $t('profile.sendMessage') }}
                </button>
              </div>
            </div>
          </div>

          <!-- Miesiące -->
          <div
            v-for="(month, index) in monthlyGroups"
            :key="index"
            class="bg-theme-bg-secondary rounded-lg shadow-sm p-5 border border-theme-border"
          >
            <h2 class="text-[20px] font-bold text-theme-text mb-1 capitalize">{{ month.name }}</h2>
            <p class="text-[15px] text-theme-text-secondary mb-4">
              <span class="font-semibold text-theme-text">{{ month.highlightedNames }}</span>
              <template v-if="month.remainingCount > 0">
                {{ t('birthday.and') }} {{ month.remainingCount }} {{ t('birthday.users') }}
              </template>
            </p>
            <div class="flex flex-wrap gap-2">
              <div
                v-for="(u, i) in month.users"
                :key="i"
                class="w-12 h-12 rounded-full overflow-hidden border border-theme-border cursor-pointer hover:opacity-80 transition-opacity"
                :title="u.name"
              >
                <img
                  :src="u.avatar"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>
          </div>

          <!-- Pusta lista, gdy brak urodzin w ogóle -->
          <div
            v-if="!isLoading && totalBirthdaysCount === 0"
            class="bg-theme-bg-secondary rounded-lg shadow-sm p-12 text-center border border-theme-border"
          >
            <p class="text-theme-text-secondary text-[16px]">
              Brak informacji o urodzinach znajomych.
            </p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import FriendsSidebar from '@/components/friends/FriendsSidebar.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { usersApi } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { useNotify } from '@/composables/shared/useNotify'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const authStore = useAuthStore()
const notify = useNotify()
const router = useRouter()

interface BirthdayUser {
  id: string | number
  name: string
  firstName: string
  age?: number | null
  wishText: string
  avatar: string
  dateLabel?: string
  birthDate?: string
  daysDiff?: number
}

interface MonthGroup {
  name: string
  monthIndex: number
  highlightedNames: string
  remainingCount: number
  users: BirthdayUser[]
}

interface EmojiPicked {
  i: string
}

const isLoading = ref(true)
const showEmojiPicker = ref(false)
const emojiPickerTarget = ref<EventTarget | null>(null)
const activeUserForEmoji = ref<BirthdayUser | null>(null)

const todayBirthdays = ref<BirthdayUser[]>([])
const recentBirthdays = ref<BirthdayUser[]>([])
const upcomingBirthdays = ref<BirthdayUser[]>([])
const monthlyGroups = ref<MonthGroup[]>([])

const totalBirthdaysCount = computed(() => {
  return todayBirthdays.value.length + recentBirthdays.value.length + upcomingBirthdays.value.length + monthlyGroups.value.length
})

const defaultQuickReplies = [
  'Wszystkiego najlepszego z okazji urodzin! Życzę Ci miłego dnia! 🎉💐🎂',
  'Stówka! 🎈🎊☕',
  'Wszystkiego naj naj naj! 🥂🎂🎁',
]

const monthNamesPolish = [
  'styczeń', 'luty', 'marzec', 'kwiecień', 'maj', 'czerwiec',
  'lipiec', 'sierpień', 'wrzesień', 'październik', 'listopad', 'grudzień'
]

const monthGenitivePolish = [
  'stycznia', 'lutego', 'marca', 'kwietnia', 'maja', 'czerwca',
  'lipca', 'sierpnia', 'września', 'października', 'listopada', 'grudnia'
]

const onEmojiClick = (user: BirthdayUser, event: MouseEvent) => {
  activeUserForEmoji.value = user
  showEmojiPicker.value = true
  emojiPickerTarget.value = event.currentTarget
}

const handleEmoji = (emoji: EmojiPicked) => {
  if (activeUserForEmoji.value) {
    activeUserForEmoji.value.wishText += emoji.i
  }
}

const sendWish = (user: BirthdayUser) => {
  if (!user.wishText.trim()) return
  notify.success(`Wysłano życzenia do ${user.firstName}!`)
  user.wishText = ''
}

const navigateToMessages = (user: BirthdayUser) => {
  router.push(`/messages?userId=${user.id}`)
}

const parseDateParts = (dateStr?: string) => {
  if (!dateStr) return null
  // Formats: "YYYY-MM-DD" or "-MM-DD" or "MM-DD"
  const parts = dateStr.split('-').filter(Boolean)
  if (parts.length === 3) {
    const year = parseInt(parts[0], 10)
    const month = parseInt(parts[1], 10) - 1
    const day = parseInt(parts[2], 10)
    return { year, month, day }
  } else if (parts.length === 2) {
    const month = parseInt(parts[0], 10) - 1
    const day = parseInt(parts[1], 10)
    return { year: null, month, day }
  }
  return null
}

const fetchBirthdays = async () => {
  if (!authStore.currentUserId || String(authStore.currentUserId) === '0') {
    isLoading.value = false
    return
  }

  isLoading.value = true
  try {
    const friends = await usersApi.getFriends(authStore.currentUserId)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const currentYear = today.getFullYear()

    const todayList: BirthdayUser[] = []
    const recentList: BirthdayUser[] = []
    const upcomingList: BirthdayUser[] = []
    const monthMap = new Map<number, BirthdayUser[]>()

    for (let m = 0; m < 12; m++) {
      monthMap.set(m, [])
    }

    for (const f of friends || []) {
      const parsed = parseDateParts(f.birthDate)
      if (!parsed) continue

      const { year, month, day } = parsed
      const age = year ? currentYear - year : null
      const dateLabel = year
        ? `${day} ${monthGenitivePolish[month]} ${year}`
        : `${day} ${monthGenitivePolish[month]}`

      const fullName = `${f.firstName || ''} ${f.lastName || ''}`.trim() || `User ${f.id}`
      const avatarUrl = f.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`

      const birthdayUser: BirthdayUser = {
        id: f.id,
        name: fullName,
        firstName: f.firstName || 'Znajomy',
        age,
        wishText: '',
        avatar: avatarUrl,
        dateLabel,
        birthDate: f.birthDate,
      }

      // Add to month group
      monthMap.get(month)?.push(birthdayUser)

      // Calculate days difference relative to current year birthday
      const bdayThisYear = new Date(currentYear, month, day)
      bdayThisYear.setHours(0, 0, 0, 0)

      const msPerDay = 1000 * 60 * 60 * 24
      const diffDays = Math.round((bdayThisYear.getTime() - today.getTime()) / msPerDay)

      if (diffDays === 0) {
        todayList.push(birthdayUser)
      } else if (diffDays < 0 && diffDays >= -30) {
        birthdayUser.daysDiff = diffDays
        recentList.push(birthdayUser)
      } else if (diffDays > 0 && diffDays <= 30) {
        birthdayUser.daysDiff = diffDays
        upcomingList.push(birthdayUser)
      } else if (diffDays < -30 && month === 11 && today.getMonth() === 0) {
        // Near year boundary (e.g. birthday in late Dec, today in early Jan)
        const bdayLastYear = new Date(currentYear - 1, month, day)
        const diff = Math.round((bdayLastYear.getTime() - today.getTime()) / msPerDay)
        if (diff >= -30) {
          birthdayUser.daysDiff = diff
          recentList.push(birthdayUser)
        }
      } else if (diffDays > 30 && month === 0 && today.getMonth() === 11) {
        // Near year boundary (e.g. birthday in early Jan, today in late Dec)
        const bdayNextYear = new Date(currentYear + 1, month, day)
        const diff = Math.round((bdayNextYear.getTime() - today.getTime()) / msPerDay)
        if (diff <= 30) {
          birthdayUser.daysDiff = diff
          upcomingList.push(birthdayUser)
        }
      }
    }

    // Sort recent (most recent first) and upcoming (soonest first)
    recentList.sort((a, b) => (b.daysDiff || 0) - (a.daysDiff || 0))
    upcomingList.sort((a, b) => (a.daysDiff || 0) - (b.daysDiff || 0))

    todayBirthdays.value = todayList
    recentBirthdays.value = recentList
    upcomingBirthdays.value = upcomingList

    // Build monthly groups
    const groups: MonthGroup[] = []
    for (let m = 0; m < 12; m++) {
      const usersInMonth = monthMap.get(m) || []
      if (usersInMonth.length > 0) {
        const highlighted = usersInMonth.slice(0, 2).map((u) => u.name).join(', ')
        groups.push({
          name: monthNamesPolish[m],
          monthIndex: m,
          highlightedNames: highlighted,
          remainingCount: Math.max(0, usersInMonth.length - 2),
          users: usersInMonth,
        })
      }
    }

    monthlyGroups.value = groups
  } catch (err) {
    console.error('Failed to fetch friend birthdays:', err)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchBirthdays()
})

watch(() => authStore.currentUserId, () => {
  fetchBirthdays()
})
</script>

<style scoped>
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
</style>
