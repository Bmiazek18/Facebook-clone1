<template>
  <div class="flex h-screen overflow-hidden bg-theme-bg text-theme-text">
    <FriendsSidebar />

    <main class="flex-1 h-full mt-14 overflow-y-auto relative ml-90">
      <div class="max-w-250 flex flex-col mx-auto px-4 py-8">
        <div class="space-y-5">
          <LazyEmojiPicker
            v-if="showEmojiPicker"
            :target="emojiPickerTarget"
            @picked="handleEmoji"
            @close="showEmojiPicker = false"
          />

          <!-- Dzisiejsze urodziny -->
          <div
            v-if="todayBirthdays.length > 0"
            class="bg-white rounded-lg shadow-sm p-4 border border-gray-100"
          >
            <h2 class="text-[20px] font-bold text-gray-900 mb-4">{{ t('birthday.today') }}</h2>

            <div v-for="user in todayBirthdays" :key="user.id" class="mb-4 last:mb-0">
              <div class="flex items-start">
                <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-3" />

                <div class="flex-1">
                  <div class="flex justify-between items-baseline mb-1">
                    <h3 class="text-[16px] font-semibold text-gray-900">{{ user.name }}</h3>
                    <span class="text-[13px] text-gray-500"
                      >{{ user.age }} {{ t('birthday.years') }}</span
                    >
                  </div>

                  <div class="flex items-center space-x-2 mb-2 mt-2">
                    <div class="flex-1 relative">
                      <input
                        type="text"
                        v-model="user.wishText"
                        :placeholder="`${t('birthday.happyBirthday')}, ${user.firstName}!`"
                        class="w-full bg-gray-100 rounded-full py-2.5 pl-4 pr-10 text-[15px] focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                      <button
                        @click="onEmojiClick(user, $event)"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
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
                      class="text-blue-500 hover:bg-blue-50 p-2 rounded-full transition-colors flex items-center justify-center"
                    >
                      <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                      </svg>
                    </button>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <button
                      @click="user.wishText = 'Stówka! 🌟🥂🎂'"
                      class="px-3 py-1.5 rounded-full border border-gray-200 text-[14px] text-gray-700 hover:bg-gray-50 transition-colors bg-white"
                    >
                      Stówka! 🌟🥂🎂
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Ostatni solenizanci -->
          <div class="bg-white rounded-lg shadow-sm p-5 border border-gray-100">
            <h2 class="text-[20px] font-bold text-gray-900 mb-6">Ostatni solenizanci</h2>

            <div
              v-for="user in recentBirthdays"
              :key="user.id"
              class="flex items-start mb-6 border-b border-gray-100 pb-6 last:mb-0 last:border-0 last:pb-0"
            >
              <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-4" />
              <div class="flex-1">
                <div class="flex justify-between items-start mb-1">
                  <div>
                    <h3 class="text-[16px] font-bold text-gray-900">{{ user.name }}</h3>
                    <p class="text-[13px] text-gray-500">{{ user.dateLabel }}</p>
                  </div>
                  <span v-if="user.age" class="text-[13px] text-gray-500">{{ user.age }} lat</span>
                </div>

                <div class="flex items-center space-x-3 mb-3 mt-3">
                  <div class="flex-1 relative">
                    <input
                      type="text"
                      v-model="user.wishText"
                      :placeholder="`Wszystkiego najlepszego z okazji urodzin, ${user.firstName}! 🥂💐☕`"
                      class="w-full bg-gray-100 rounded-full py-2.5 pl-4 pr-10 text-[15px] focus:outline-none"
                    />
                    <button
                      @click="onEmojiClick(user, $event)"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <path
                          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm3.5 8c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5-1.5-.67-1.5-1.5.67-1.5 1.5-1.5zm-7 0c.83 0 1.5.67 1.5 1.5S9.33 13 8.5 13 7 12.33 7 11.5 7.67 10 8.5 10zm3.5 9.5c-2.33 0-4.31-1.46-5.11-3.5h10.22c-.8 2.04-2.78 3.5-5.11 3.5z"
                        />
                      </svg>
                    </button>
                  </div>
                  <button
                    class="text-blue-600 hover:bg-blue-50 p-2 rounded-full transition-colors flex items-center justify-center"
                  >
                    <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                    </svg>
                  </button>
                </div>

                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="(reply, idx) in user.quickReplies"
                    :key="idx"
                    @click="user.wishText = reply"
                    class="px-4 py-1.5 rounded-full border border-gray-200 text-[14px] text-gray-700 hover:bg-gray-50 transition-colors bg-white max-w-full truncate"
                  >
                    {{ reply }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Nadchodzące urodziny -->
          <div class="bg-white rounded-lg shadow-sm p-5 border border-gray-100">
            <h2 class="text-[20px] font-bold text-gray-900 mb-6">Nadchodzące urodziny</h2>
            <div class="space-y-4">
              <div
                v-for="user in upcomingBirthdays"
                :key="user.id"
                class="flex items-center justify-between"
              >
                <div class="flex items-center">
                  <img :src="user.avatar" class="w-14 h-14 rounded-full object-cover mr-4" />
                  <div>
                    <h3 class="text-[16px] font-bold text-gray-900">{{ user.name }}</h3>
                    <p class="text-[13px] text-gray-500">
                      {{ user.dateLabel }}
                      <span v-if="user.age">· {{ user.age }} lat</span>
                    </p>
                  </div>
                </div>
                <button
                  class="bg-[#e4e6eb] hover:bg-[#d8dadf] text-black font-semibold py-2 px-4 rounded-lg text-[15px] flex items-center transition-colors"
                >
                  <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
                    <path
                      d="M12 2C6.477 2 2 6.14 2 11.25c0 2.91 1.5 5.51 3.84 7.23v3.13c0 .38.44.6.75.38l3.15-2.02c.73.2 1.48.31 2.26.31 5.523 0 10-4.14 10-9.25S17.523 2 12 2zm1.09 12.31l-2.5-2.67-4.8 2.67 5.27-5.59 2.5 2.67 4.8-2.67-5.27 5.59z"
                    />
                  </svg>
                  Wyślij wiadomość
                </button>
              </div>
            </div>
          </div>

          <!-- Miesiące -->
          <div
            v-for="(month, index) in monthlyGroups"
            :key="index"
            class="bg-white rounded-lg shadow-sm p-5 border border-gray-100"
          >
            <h2 class="text-[20px] font-bold text-gray-900 mb-1 capitalize">{{ month.name }}</h2>
            <p class="text-[15px] text-gray-500 mb-4">
              <span class="font-semibold text-gray-900">{{ month.highlightedNames }}</span>
              {{ t('birthday.and') }} {{ month.count }} {{ t('birthday.users') }}
            </p>
            <div class="flex flex-wrap gap-1">
              <div
                v-for="(avatar, i) in month.avatars"
                :key="i"
                class="w-[12%] sm:w-[10%] md:w-[8%] aspect-square p-0.5"
              >
                <img
                  :src="avatar"
                  class="w-full h-full rounded-full object-cover border border-gray-200 hover:brightness-90 cursor-pointer transition-all"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import FriendsSidebar from '@/components/friends/FriendsSidebar.vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'

const { t } = useI18n()

interface BirthdayUser {
  id: number
  name: string
  firstName: string
  age?: number | null
  wishText: string
  avatar: string
  dateLabel?: string
  quickReplies?: string[]
}

interface EmojiPicked {
  i: string
}

const showEmojiPicker = ref(false)
const emojiPickerTarget = ref<EventTarget | null>(null)
const activeUserForEmoji = ref<BirthdayUser | null>(null)

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

const todayBirthdays = ref<BirthdayUser[]>([
  {
    id: 1,
    name: 'Marcin Krasnodębski',
    firstName: 'Marcin',
    age: 21,
    wishText: '',
    avatar: 'https://ui-avatars.com/api/?name=MK&background=0D8ABC&color=fff',
  },
])

const recentBirthdays = ref<BirthdayUser[]>([
  {
    id: 2,
    name: 'Karol Jastrzębski',
    firstName: 'Karol',
    dateLabel: '5 lipca 1995',
    age: 31,
    wishText: '',
    avatar: 'https://ui-avatars.com/api/?name=KJ&background=random',
    quickReplies: [
      'Wszystkiego najlepszego z okazji urodzin! Życzę Ci miłego dnia! 🎉💐🎂',
      'Stówka! 🎈🎊☕',
    ],
  },
  {
    id: 3,
    name: 'Maja Fejtko',
    firstName: 'Maja',
    dateLabel: '6 lipca 2005',
    age: 21,
    wishText: '',
    avatar: 'https://ui-avatars.com/api/?name=MF&background=random',
    quickReplies: [
      'Oto kolejny niesamowity rok życia! 💐🎊',
      'Wszystkiego naj naj naj z okazji urodzin! 💐🥂🎂',
    ],
  },
  {
    id: 4,
    name: 'Magomed Soltmuradov',
    firstName: 'Magomed',
    dateLabel: '6 lipca 2006',
    age: 20,
    wishText: '',
    avatar: 'https://ui-avatars.com/api/?name=MS&background=random',
    quickReplies: [
      'Zasługujesz na to, by Cię uczcić. Wszystkiego najlepszego z okazji uro...',
      'Życzę Ci wszystkiego najlepszego z okazji urodzin! 🎁🌟',
    ],
  },
])

const upcomingBirthdays = ref([
  {
    id: 5,
    name: 'Julia Figas',
    dateLabel: '8 lipca 2005',
    age: 21,
    avatar: 'https://ui-avatars.com/api/?name=JF&background=random',
  },
])

const generateAvatars = (count: number) => {
  return Array.from({ length: count }, () => `https://i.pravatar.cc/150?u=${Math.random()}`)
}

const monthlyGroups = ref([
  {
    name: 'lutego',
    highlightedNames: 'Sebastian Pszkit, Kornel Piorunski',
    count: 32,
    avatars: generateAvatars(34),
  },
])
</script>
