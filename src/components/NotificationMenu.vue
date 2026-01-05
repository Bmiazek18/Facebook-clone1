<template>
  <div class="w-full md:w-[360px] mx-auto bg-theme-bg-secondary max-h-[calc(100vh-4rem)] flex flex-col overflow-hidden shadow-2xl rounded-b-2xl">
    <header class="pt-2 pb-0 px-3 flex justify-between items-center shrink-0">
      <h1 class="text-2xl font-bold text-theme-text">{{ $t('notifications_page.title') }}</h1>
      <DotsHorizontalIcon class="h-6 w-6 text-theme-text-secondary cursor-pointer" />
    </header>

    <div class="flex px-3 pt-2 space-x-2">
      <button
        @click="activeTab = 'all'"
        :class="{'bg-blue-100 text-blue-700 font-semibold': activeTab === 'all', 'text-theme-text-secondary': activeTab !== 'all'}"
        class="py-1 px-3 rounded-full text-sm transition duration-150"
      >
        {{ $t('notifications_page.all') }}
      </button>
      <button
        @click="activeTab = 'unread'"
        :class="{'bg-blue-100 text-blue-700 font-semibold': activeTab === 'unread', 'text-gray-700': activeTab !== 'unread'}"
        class="py-1 px-3 rounded-full text-sm transition duration-150"
      >
        {{ $t('notifications_page.unread') }}
      </button>
    </div>

    <div class="p-3">
      <div class="flex justify-between items-center mb-3">
        <h2 class="text-lg font-semibold text-theme-text">{{ $t('notifications_page.earlier') }}</h2>
        <button class="text-blue-500 font-medium text-sm hover:underline">{{ $t('notifications_page.viewAll') }}</button>
      </div>

      <ul class="space-y-1">
        <li v-for="notification in filteredNotifications" :key="notification.id" class="flex items-start py-2 hover:bg-gray-50 rounded-lg cursor-pointer transition duration-100">
          <div class="relative shrink-0 mr-3">
            <img :src="notification.avatarUrl" alt="Awatar" class="h-15 w-15 rounded-full object-cover bg-gray-200" />
            <div>
               <component
                :is="notification.typeIcon"
                class="absolute bottom-0 right-0 p-1 h-6 w-6 flex content-center items-center rounded-full "
                :class="{'bg-blue-500 text-white': notification.unread, 'bg-gray-300 text-gray-700': !notification.unread}"
            />
            </div>

          </div>

          <div class="grow text-sm">
            <p class="text-theme-text leading-snug" :class="{'font-medium': notification.unread, 'font-normal': !notification.unread}">
              <span v-html="notification.message"></span>
            </p>
            <span class="text-xs text-theme-text-secondary">{{ notification.timeAgo }}</span>
          </div>

          <div v-if="notification.unread" class="w-3 h-3 bg-blue-500 rounded-full shrink-0 ml-3 mt-1.5"></div>
        </li>
      </ul>

      <div class="pt-4 mt-2  border-gray-100">
        <button class="w-full py-2 bg-gray-100 hover:bg-gray-200 rounded-lg text-sm font-semibold text-gray-800 transition duration-150">
          {{ $t('notifications_page.viewEarlier') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, computed, type DefineComponent } from 'vue';
import { useI18n } from 'vue-i18n';


import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';


useI18n()

interface Notification {
  id: number;
  avatarUrl: string;
  typeIcon: DefineComponent;
  message: string;
  timeAgo: string;
  unread: boolean;
}


const activeTab: Ref<'all' | 'unread'> = ref('all');

const rawNotifications: Notification[] = [
  {
    id: 1,
    avatarUrl: 'https://via.placeholder.com/100',
    typeIcon: BellIcon,
    message: 'Masz nową propozycję znajomości: <strong>Raul Salerno</strong>.',
    timeAgo: '19 godz.',
    unread: true,
  },
  {
    id: 2,
    avatarUrl: 'https://via.placeholder.com/100/400082?text=ST',
    typeIcon: AccountGroupIcon,
    message: 'Użytkownik <strong>Studenci Trójmiasto</strong> oznaczył Ciebie i innych użytkowników w poście w grupi...',
    timeAgo: '1 dzień',
    unread: true,
  },
  {
    id: 3,
    avatarUrl: 'https://via.placeholder.com/100/400082?text=ST',
    typeIcon: AccountGroupIcon,
    message: 'Użytkownik <strong>Studenci Trójmiasto</strong> oznaczył Ciebie i innych użytkowników w poście w grupi...',
    timeAgo: '2 dni',
    unread: true,
  },
  {
    id: 4,
    avatarUrl: 'https://via.placeholder.com/100/000000?text=KS',
    typeIcon: AccountGroupIcon,
    message: 'Teraz w grupie Kolegium Sędziów BOZPN: **„UWAGA! Przypominam że runda jesienna 2025r. trwa...”**',
    timeAgo: '3 dni',
    unread: false,
  },
  {
    id: 5,
    avatarUrl: 'https://via.placeholder.com/100/400082?text=ST',
    typeIcon: AccountGroupIcon,
    message: 'Użytkownik <strong>Studenci Trójmiasto</strong> oznaczył Ciebie i innych użytkowników w poście w grupi...',
    timeAgo: '1 tydz.',
    unread: true,
  },
  {
    id: 6,
    avatarUrl: 'https://via.placeholder.com/100/DC143C?text=PS', // Placeholder dla Pawła Staniszewskiego
    typeIcon: AccountGroupIcon,
    message: '<strong>Paweł Staniszewski</strong> oznaczył Ciebie i innych użytkowników w poście w grupie <strong>Reprezentacja</strong>...',
    timeAgo: '1 tydz.',
    unread: true,
  },
];

const notifications: Ref<Notification[]> = ref(rawNotifications);


const filteredNotifications = computed(() => {
  if (activeTab.value === 'unread') {
    return notifications.value.filter(n => n.unread);
  }
  return notifications.value;
});



</script>


