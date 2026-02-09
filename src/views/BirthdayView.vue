<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex font-sans">
    <FriendsSidebar />
    <div class="max-w-[800px] mx-auto py-6 px-4 space-y-5">
      <LazyEmojiPicker
        v-if="showEmojiPicker"
        :target="emojiPickerTarget"
        @picked="handleEmoji"
        @close="showEmojiPicker = false"
      />
      <div v-if="todayBirthdays.length > 0" class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <h2 class="text-[17px] font-bold text-theme-text mb-4">{{ t('birthday.today') }}</h2>

        <div v-for="user in todayBirthdays" :key="user.id" class="mb-4 last:mb-0">
          <div class="flex items-start">
            <img :src="user.avatar" class="w-12 h-12 rounded-full object-cover mr-3" />

            <div class="flex-1">
              <div class="flex justify-between items-baseline mb-1">
                <h3 class="text-[16px] font-semibold text-theme-text">{{ user.name }}</h3>
                <span class="text-[13px] text-theme-text-secondary">{{ user.age }} {{ t('birthday.years') }}</span>
              </div>

              <div class="flex items-center space-x-2 mb-2">
                <div class="flex-1 relative">
                  <input
                    type="text"
                    v-model="user.wishText"
                    :placeholder="`${t('birthday.happyBirthday')}, ${user.firstName}!`"
                    class="bg-theme-bg-tertiary rounded-full py-2 pl-4 pr-10 text-[15px] focus:outline-none focus:ring-1 focus:ring-blue-500"
                  />
                  <button
                    @click="onEmojiClick(user, $event)"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-theme-text-secondary hover:text-gray-700"
                  >
                    <svg
                      class="w-5 h-5"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                  </button>
                </div>
                <button class="text-blue-500 hover:bg-blue-50 p-2 rounded-full transition-colors">
                  <svg class="w-5 h-5 transform rotate-90" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"
                    />
                  </svg>
                </button>
              </div>

              <div class="flex flex-wrap gap-2">
                <button
                  @click="user.wishText = 'Stówka! 🌟🥂🎂'"
                  class="px-3 py-1.5 rounded-full border border-gray-300 text-[13px] font-medium text-gray-700 hover:bg-gray-100 transition-colors"
                >
                  Stówka! 🌟🥂🎂
                </button>
                <button
                  @click="user.wishText = 'Wszystkiego naj naj naj z okazji urodzin! 💐🎂'"
                  class="px-3 py-1.5 rounded-full border border-gray-300 text-[13px] font-medium text-gray-700 hover:bg-gray-100 transition-colors"
                >
                  Wszystkiego naj naj naj z okazji urodzin! 💐🎂
                </button>
                <button
                  @click="user.wishText = 'Wszystkiego najlepszego z okazji urodzin, dużo szczęścia! 🎂🥂👯‍♀️'"
                  class="px-3 py-1.5 rounded-full border border-gray-300 text-[13px] font-medium text-gray-700 hover:bg-gray-100 transition-colors"
                >
                  Wszystkiego najlepszego z okazji urodzin, dużo szczęścia! 🎂🥂👯‍♀️
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <h2 class="text-[17px] font-bold text-theme-text mb-4">{{ t('birthday.recent') }}</h2>

        <div v-for="user in recentBirthdays" :key="user.id" class="flex items-start mb-6 last:mb-0">
          <img :src="user.avatar" class="w-12 h-12 rounded-full object-cover mr-3" />
          <div class="flex-1">
            <div class="mb-1">
              <h3 class="text-[16px] font-semibold text-theme-text">{{ user.name }}</h3>
              <p class="text-[13px] text-theme-text-secondary">{{ user.dateLabel }}</p>
            </div>

            <div class="flex items-center space-x-2 mb-2">
              <div class="flex-1 relative">
                <input
                  type="text"
                  v-model="user.wishText"
                  :placeholder="`${t('birthday.happyBirthday')}, ${user.firstName}!`"
                  class="bg-theme-bg-tertiary rounded-full py-2 pl-4 pr-10 text-[15px] focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
                <button
                  @click="onEmojiClick(user, $event)"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-theme-text-secondary hover:text-gray-700"
                >
                  <svg
                    class="w-5 h-5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                </button>
              </div>
              <button class="text-blue-500 hover:bg-blue-50 p-2 rounded-full transition-colors">
                <svg class="w-5 h-5 transform rotate-90" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"
                  />
                </svg>
              </button>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                @click="user.wishText = 'Stówka! 🎁☕️👯‍♀️'"
                class="px-3 py-1.5 rounded-full border border-gray-300 text-[13px] font-medium text-gray-700 hover:bg-gray-100 transition-colors"
              >
                Stówka! 🎁☕️👯‍♀️
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <h2 class="text-[17px] font-bold text-theme-text mb-4">{{ t('birthday.upcoming') }}</h2>
        <div class="space-y-4">
          <div
            v-for="user in upcomingBirthdays"
            :key="user.id"
            class="flex items-center justify-between"
          >
            <div class="flex items-center">
              <img :src="user.avatar" class="w-12 h-12 rounded-full object-cover mr-3" />
              <div>
                <h3 class="text-[16px] font-semibold text-theme-text">{{ user.name }}</h3>
                <p class="text-[13px] text-theme-text-secondary">
                  {{ user.dateLabel }}
                  <span v-if="user.age">· {{ user.age }} {{ t('birthday.years') }}</span>
                </p>
              </div>
            </div>
            <button
              class="bg-theme-bg-secondary hover:bg-theme-bg-hover text-theme-text font-semibold py-1.5 px-3 rounded-md text-[15px] flex items-center transition-colors"
            >
              <svg class="w-4 h-4 mr-1.5" viewBox="0 0 28 28" fill="currentColor">
                <path
                  d="M14 2C7.373 2 2 6.972 2 13.105c0 3.5 1.76 6.625 4.512 8.705.234.177.382.477.332.793l-.44 2.802a.855.855 0 001.192.89l3.167-1.393a1.056 1.056 0 01.767-.058c.8.22 1.648.342 2.53.342 6.627 0 12-4.972 12-11.105C26 6.972 20.627 2 14 2zm1.26 13.88l-2.688-2.868a.792.792 0 00-1.144 0L7.56 17.514c-.382.408-1.002.062-.777-.432l2.94-6.446a.792.792 0 000-.658l2.688 2.868a.792.792 0 001.144 0l3.868-4.302c.382-.408 1.002-.062.777.432l-2.94 6.446a.792.792 0 000 .658z"
                />
              </svg>
              {{ t('birthday.sendMessage') }}
            </button>
          </div>
        </div>
      </div>

      <div
        v-for="(month, index) in monthlyGroups"
        :key="index"
        class="bg-theme-bg-secondary rounded-lg shadow-sm p-4"
      >
        <h2 class="text-[17px] font-bold text-theme-text mb-1 capitalize">{{ month.name }}</h2>
        <p class="text-[15px] text-theme-text-secondary mb-4">
          <span class="font-semibold">{{ month.highlightedNames }}</span>
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
              class="w-full h-full rounded-full object-cover border border-theme-border hover:brightness-90 cursor-pointer transition-all"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FriendsSidebar from '@/components/friends/FriendsSidebar.vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue';

const { t } = useI18n();

interface BirthdayUser {
  id: number;
  name: string;
  firstName: string;
  age?: number | null;
  wishText: string;
  avatar: string;
  dateLabel?: string;
}

interface EmojiPicked {
  i: string;
  // Add other properties if needed based on the actual emoji object structure
}

const showEmojiPicker = ref(false);
const emojiPickerTarget = ref<EventTarget | null>(null);
const activeUserForEmoji = ref<BirthdayUser | null>(null);

const onEmojiClick = (user: BirthdayUser, event: MouseEvent) => {
  activeUserForEmoji.value = user;
  showEmojiPicker.value = true;
  emojiPickerTarget.value = event.currentTarget;
};

const handleEmoji = (emoji: EmojiPicked) => {
  if (activeUserForEmoji.value) {
    activeUserForEmoji.value.wishText += emoji.i;
  }
};

// --- MOCK DATA ---
const todayBirthdays = ref([
  {
    id: 1,
    name: 'Marcin Krasnodębski',
    firstName: 'Marcin',
    age: 21,
    wishText: '', // DODANO POLE STANU DLA INPUTA
    avatar: 'https://ui-avatars.com/api/?name=MK&background=random'
  }
]);

const recentBirthdays = ref([
  {
    id: 2,
    name: 'Kinga Włoczewska',
    firstName: 'Kinga',
    dateLabel: '25 stycznia',
    wishText: '', // DODANO POLE STANU DLA INPUTA
    avatar: 'https://ui-avatars.com/api/?name=KW&background=random'
  }
]);

const upcomingBirthdays = ref([
  { id: 3, name: 'Albert Świerczewski', dateLabel: '27 stycznia 2005', age: 21, avatar: 'https://ui-avatars.com/api/?name=AS&background=random' },
  { id: 4, name: 'Alan Va', dateLabel: '28 stycznia', age: null, avatar: 'https://ui-avatars.com/api/?name=AV&background=random' },
  { id: 5, name: 'Igor Grzelak', dateLabel: '28 stycznia 2000', age: 26, avatar: 'https://ui-avatars.com/api/?name=IG&background=random' },
  { id: 6, name: 'Filip Okuń', dateLabel: '29 stycznia 2009', age: 17, avatar: 'https://ui-avatars.com/api/?name=FO&background=random' },
  { id: 7, name: 'Czarek Kozakowski', dateLabel: '29 stycznia 2007', age: 19, avatar: 'https://ui-avatars.com/api/?name=CK&background=random' },
  { id: 8, name: 'Igor Stefaniak', dateLabel: '29 stycznia 2004', age: 22, avatar: 'https://ui-avatars.com/api/?name=IS&background=random' },
  { id: 9, name: 'Iga Wiąckiewicz', dateLabel: '29 stycznia 2006', age: 20, avatar: 'https://ui-avatars.com/api/?name=IW&background=random' },
]);

// Helper do generowania dużej ilości awatarów dla miesięcy
const generateAvatars = (count: number) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  return Array.from({ length: count }, (_, i) => `https://i.pravatar.cc/150?u=${Math.random()}`);
};

const monthlyGroups = ref([
  {
    name: 'lutego',
    highlightedNames: 'Sebastian Pszkit, Kornel Piorunski',
    count: 32,
    avatars: generateAvatars(34)
  },
  {
    name: 'marca',
    highlightedNames: 'Mikołaj Sidoruk, Zuzia Pieniak',
    count: 45,
    avatars: generateAvatars(47)
  },
  {
    name: 'kwietnia',
    highlightedNames: 'Rafał Iwańczuk, Dawid Okun',
    count: 43,
    avatars: generateAvatars(45)
  }
]);
</script>
