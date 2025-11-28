<template>
  <div class="max-w-[360px] mx-auto bg-theme-bg-secondary  h-full ">
    <header class="p-4 flex justify-between items-center sticky top-0 bg-theme-bg-secondary z-10">
      <div class="flex items-center space-x-2">
        <h1 class="text-2xl font-bold text-theme-text">{{ $t('header.title') }}</h1>
        <LanguageSwitcher />
      </div>
      <div class="flex space-x-3 text-theme-text-secondary">
        <DotsHorizontalIcon class="h-6 w-6 cursor-pointer" />
        <ArrowExpandIcon class="h-6 w-6 cursor-pointer" />
        <PencilOutlineIcon class="h-6 w-6 cursor-pointer" />
      </div>
    </header>

    <div class="px-4 pb-3 sticky top-[72px] bg-theme-bg-secondary z-10">
      <div class="flex items-center bg-theme-bg rounded-full p-2">
        <MagnifyIcon class="h-5 w-5 text-theme-text-secondary mx-2 shrink-0" />
        <input
          type="text"
          :placeholder="$t('common.search')"
          class="w-full bg-theme-bg border-none p-0 focus:ring-0 placeholder-theme-text-secondary text-sm"
        />
      </div>
    </div>

    <div class="flex px-4 pb-3 space-x-2 sticky top-[120px] bg-theme-bg-secondary z-10">
      <button
        @click="activeTab = 'all'"
        :class="{'bg-blue-500 text-white font-semibold': activeTab === 'all', 'bg-gray-200 text-gray-800': activeTab !== 'all'}"
        class="py-1 px-3 rounded-full text-sm transition duration-150"
      >
        {{ $t('chat.allChats') }}
      </button>
      <button
        @click="activeTab = 'unread'"
        :class="{'bg-blue-500 text-white font-semibold': activeTab === 'unread', 'bg-gray-200 text-gray-800': activeTab !== 'unread'}"
        class="py-1 px-3 rounded-full text-sm transition duration-150"
      >
        {{ $t('chat.unread') }}
      </button>
      <DotsHorizontalIcon class="h-5 w-5 text-gray-500 self-center ml-auto cursor-pointer" />
    </div>

    <div class="px-4 pb-2">
      <a href="#" class="flex items-center py-2 hover:bg-theme-hover rounded-lg transition duration-100">
        <div class="shrink-0 w-12 h-12 rounded-full flex items-center justify-center bg-theme-bg mr-3">
          <ChatOutlineIcon class="h-6 w-6 text-theme-text" />
        </div>
        <div class="grow">
          <p class="font-normal text-theme-text">{{ $t('chat.newMessage') }}</p>
          <p class="text-sm text-theme-text-secondary">{{ $t('chat.from') }} Alan Va</p>
        </div>
        <ChevronRightIcon class="h-6 w-6 text-theme-text-secondary ml-auto" />
      </a>
    </div>

    <ul class="px-4 space-y-1">
      <li v-for="chat in filteredChats" :key="chat.id">
        <button
          @click="handleClick(chat.id)"
          class="w-full group flex items-center py-2 px-1 hover:bg-theme-hover rounded-lg cursor-pointer transition duration-100 text-left"
        >
          <div class="relative shrink-0  mr-3">
            <img :src="chat.avatarUrl" alt="Awatar" class="h-12 w-12 rounded-full object-cover bg-theme-bg" />
            <span v-if="chat.isActive" class="absolute bottom-0 right-0 block h-3 w-3 rounded-full ring-2 ring-white bg-green-500"></span>

          </div>

          <div class="grow min-w-0">
            <p class="text-theme-text truncate" :class="{'font-bold': chat.unread}">
              {{ chat.name }}
            </p>
            <p class="text-sm truncate" :class="{'font-bold text-theme-text': chat.unread, 'text-theme-text-secondary': !chat.unread}">
              <span v-html="chat.lastMessage"></span> · {{ chat.timeAgo }}
            </p>
          </div>

          <div class="shrink-0 ml-3 relative flex items-center space-x-1">

            <div v-if="chat.extraAvatars" class="flex -space-x-1 overflow-hidden">
                <img v-for="(avatar, index) in chat.extraAvatars" :key="index" :src="avatar" class="inline-block h-5 w-5 rounded-full ring-2 ring-white bg-gray-300" />
            </div>
            <div v-if="chat.unread" class="w-2 h-2 bg-blue-500  rounded-full shrink-0"></div>

            <HandRightIcon v-if="chat.isPinch" class="h-5 w-5 text-theme-text-secondary" />
            <VDropdown :distance="30" @show="() => setDropdownOpen(chat.id, true)" @hide="() => setDropdownOpen(chat.id, false)">
              <div :class="['group-hover:flex hover:bg-theme-hover  absolute right-3 top-1/2 -translate-y-1/2 shadow-md border bg-theme-bg border-gray-300 items-center justify-center w-9 h-9 rounded-full', openDropdowns[chat.id] ? 'flex' : 'hidden']">
                <DotsHorizontalIcon class="cursor-pointer" />
              </div>
              <template #popper>
                <ContexMenu/>
              </template>
            </VDropdown>
          </div>
        </button>
      </li>
    </ul>

    <div class="px-4 pt-3 pb-4">
        <a href="#" class="block w-full text-center py-2 text-blue-500 font-semibold hover:underline">
            {{ $t('chat.openMessenger') }}
        </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, computed } from 'vue';

// 1. IMPORT KOMPONENTÓW IKON
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ArrowExpandIcon from 'vue-material-design-icons/ArrowExpand.vue';
import PencilOutlineIcon from 'vue-material-design-icons/PencilOutline.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import ChatOutlineIcon from 'vue-material-design-icons/ChatOutline.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ContexMenu from './contexMenu.vue';
import LanguageSwitcher from './LanguageSwitcher.vue';


// 2. INTERFEJSY TYPÓW DANYCH
interface Chat {
  id: number;
  name: string;
  avatarUrl: string;
  lastMessage: string;
  timeAgo: string;
  unread: boolean;
  isActive: boolean;
  isPinch?: boolean; // Ikona "szczypnięcia" / reakcji ręki

  extraAvatars?: string[]; // Mini-awatary dla konwersacji grupowych
}

// 3. ZARZĄDZANIE STANEM
const activeTab: Ref<'all' | 'unread'> = ref('all');

const rawChats: Chat[] = [
  // 1. Carbonara
  {
    id: 1,
    name: 'Carbonara 🤠',
    avatarUrl: 'https://via.placeholder.com/100/108390?text=C',
    lastMessage: 'Użytkownik Carbonara 🤠 wysłał ...',
    timeAgo: '21 min',
    unread: false,
    isActive: false,
    isPinch: true,
  },
  // 2. Łuków24
  {
    id: 2,
    name: 'Łuków24',
    avatarUrl: 'https://via.placeholder.com/100/DC143C?text=24',
    lastMessage: 'Użytkownik Łuków24 wysłał ...',
    timeAgo: '49 min',
    unread: false,
    isActive: false,
  },
  // 3. Pati Kochanska
  {
    id: 3,
    name: 'Pati Kochanska',
    avatarUrl: 'https://via.placeholder.com/100/A0A0A0?text=P',
    lastMessage: 'jeszcze w zime',
    timeAgo: '5 godz.',
    unread: false,
    isActive: true,
  },
  // 4. Grupa 7 (casual)
  {
    id: 4,
    name: 'Grupa 7 (casual)',
    avatarUrl: 'https://via.placeholder.com/100/400082?text=G',
    lastMessage: 'Paweł: chyba tak',
    timeAgo: '6 godz.',
    unread: false,
    isActive: false,
  },
  // 5. Koalicja 2 Grudnia
  {
    id: 5,
    name: 'Koalicja 2 Grudnia',
    avatarUrl: 'https://via.placeholder.com/100/000000?text=K',
    lastMessage: 'Ty: Aż tak za lukowe...',
    timeAgo: '9 godz.',
    unread: false,
    isActive: false,
    extraAvatars: [
        'https://via.placeholder.com/100/E3E6EA?text=A',
        'https://via.placeholder.com/100/E3E6EA?text=B'
    ],
  },
  // 6. Infa 2025
  {
    id: 6,
    name: 'Infa 2025',
    avatarUrl: 'https://via.placeholder.com/100/A0A0A0?text=I',
    lastMessage: 'Natalia: Okej',
    timeAgo: '13 godz.',
    unread: false,
    isActive: false,

  },
  // 7. Milf Hunters
  {
    id: 7,
    name: 'Milf Hunters',
    avatarUrl: 'https://via.placeholder.com/100/000000?text=M',
    lastMessage: 'Mateusz: Piłkarzami z przypadp...',
    timeAgo: '2 dni',
    unread: true,
    isActive: true,
  },
  // 8. Legia Futsal
  {
    id: 8,
    name: 'Legia Futsal',
    avatarUrl: 'https://via.placeholder.com/100/004E22?text=L',
    lastMessage: 'Bramka Luci Prioriego nomi...',
    timeAgo: '2 dni',
    unread: true,
    isActive: false,
  },
  // 9. Mateusz Bieniek
  {
    id: 9,
    name: 'Mateusz Bieniek',
    avatarUrl: 'https://via.placeholder.com/100/108390?text=MB',
    lastMessage: 'Nie dam rady',
    timeAgo: '2 dni',
    unread: false,
    isActive: false,
  },
  // 10. Zgrupowanie Reprezentacja Se...
  {
    id: 10,
    name: 'Zgrupowanie Reprezentacja Se...',
    avatarUrl: 'https://via.placeholder.com/100/FFFFFF',
    lastMessage: 'Michał: Nie, tym razem to nie ...',
    timeAgo: '3 dni',
    unread: true,
    isActive: true,

  },
  // 11. Wioletta Miazek
  {
    id: 11,
    name: 'Wioletta Miazek',
    avatarUrl: 'https://via.placeholder.com/100/F0F0F0?text=WM',
    lastMessage: '🙌 3 dni',
    timeAgo: '3 dni',
    unread: true,
    isActive: false,
  },
  // 12. Adam Zarzycki
  {
    id: 12,
    name: 'Adam Zarzycki',
    avatarUrl: 'https://via.placeholder.com/100/000000?text=AZ',
    lastMessage: 'Ty: Gdzie ty jesteś?',
    timeAgo: '3 dni',
    unread: true,
    isActive: false,
  },
  // 13. WC UPOSiF
  {
    id: 13,
    name: 'WC UPOSiF',
    avatarUrl: 'https://via.placeholder.com/100/A0A0A0?text=W',
    lastMessage: '...',
    timeAgo: '3 dni',
    unread: false,
    isActive: false,
  },
];

const chats: Ref<Chat[]> = ref(rawChats);

// track which dropdown is open per chat id so the trigger stays visible when popper is active
const openDropdowns = ref<Record<number, boolean>>({});
const setDropdownOpen = (id: number, value: boolean) => {
  openDropdowns.value[id] = value;
};

// 4. LOGIKA FILTROWANIA (Computed Property)
const filteredChats = computed(() => {
  if (activeTab.value === 'unread') {
    return chats.value.filter(n => n.unread);
  }
  // Pomijamy sekcję "Inne" w głównej liście (jest renderowana oddzielnie)
  return chats.value;
});

// 5. OBSŁUGA KLIKNIĘĆ
const handleClick = (chatId: number): void => {
  console.log(`Przechodzę do czatu ID: ${chatId}`);
  // Tutaj dodasz logikę routingu do konwersacji
};

</script>

<style scoped>
/* Możesz dodać style niestandardowe, jeśli zajdzie taka potrzeba */
</style>
