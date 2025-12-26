<script setup lang="ts">
import { ref, type DefineComponent } from 'vue';

// Import ikon (vue-material-design-icons)
import HomeIcon from 'vue-material-design-icons/Home.vue';
import VideoIcon from 'vue-material-design-icons/Video.vue';
import CalendarIcon from 'vue-material-design-icons/Calendar.vue';
import MessageAlertIcon from 'vue-material-design-icons/MessageAlert.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import CalendarBlankIcon from 'vue-material-design-icons/CalendarBlank.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import MonitorDashboardIcon from 'vue-material-design-icons/MonitorDashboard.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';

// Typy danych dla kart
interface FeatureList {
  icon: DefineComponent;
  text: string;
}

interface ActionCard {
  title: string;
  iconColor: string; // klasa tailwind np. bg-red-600
  mainIcon: DefineComponent;
  description: string; // Tytuł nad listą
  features: FeatureList[];
  buttonText: string;
  link: string;
}

// Dane symulujące stan użytkownika
const userName = ref('Bartosz');
const currentTab = ref<'live' | 'scheduled'>('live');

// Konfiguracja kart akcji
const actionCards: ActionCard[] = [
  {
    title: 'Rozpocznij transmisję na żywo',
    iconColor: 'bg-red-600',
    mainIcon: VideoIcon,
    description: 'Rozpocznij transmisję na żywo',
    features: [
      { icon: AccountGroupIcon, text: 'Rozpocznij transmisję na żywo samodzielnie lub wspólnie z innymi' },
      { icon: MonitorDashboardIcon, text: 'Wybierz, gdzie chcesz opublikować transmisję wideo na żywo' },
      { icon: CogIcon, text: 'Odkryj dodatkowe narzędzia, które pomogą Ci zwiększyć aktywność odbiorców' },
    ],
    buttonText: 'Rozpocznij transmisję na żywo',
    link: '/live/produce/create-live'
  },
  {
    title: 'Utwórz wydarzenie z transmisją wideo na żywo',
    iconColor: 'bg-blue-600',
    mainIcon: CalendarIcon,
    description: 'Utwórz wydarzenie z transmisją wideo na żywo',
    features: [
      { icon: CalendarBlankIcon, text: 'Utwórz wydarzenie z wyprzedzeniem i udostępnij je swoim odbiorcom' },
      { icon: AccountGroupIcon, text: 'Widzowie mogą reagować na Twoje wydarzenie' },
      { icon: BellIcon, text: 'Ty i Twoi widzowie otrzymacie przypomnienie przed rozpoczęciem transmisji na żywo' },
    ],
    buttonText: 'Utwórz wydarzenie',
    link: '/live/produce/create-event'
  }
];
</script>

<template>
  <div class="flex min-h-screen bg-[#F0F2F5] font-sans text-gray-900">

    <aside class="w-80 bg-white border-r border-gray-200 flex flex-col fixed h-full z-10">
      <div class="p-4">
        <h1 class="text-xl font-bold mb-6 leading-tight">Utwórz transmisję wideo na żywo</h1>

        <button class="flex items-center space-x-3 w-full p-2 bg-gray-100 rounded-lg mb-6 hover:bg-gray-200 transition">
          <div class="bg-blue-600 rounded-full p-1">
            <HomeIcon class="text-white" :size="20" />
          </div>
          <span class="font-semibold text-sm">Strona główna</span>
        </button>

        <div class="flex items-center space-x-3 mb-4 px-2">
          <img
            src="https://i.pravatar.cc/150?u=bartosz"
            alt="Avatar"
            class="w-10 h-10 rounded-full border border-gray-300"
          />
          <div class="flex flex-col">
            <span class="font-bold text-sm">{{ userName }} Miazek</span>
            <span class="text-xs text-gray-500">Organizator — Twój profil</span>
          </div>
        </div>

        <div class="relative border border-blue-500 rounded-lg p-3 bg-blue-50 cursor-pointer hover:bg-blue-100 transition">
          <div class="absolute top-2 right-2">
            <ChevronDownIcon :size="20" />
          </div>
          <p class="text-xs text-blue-600 mb-1">Wskaż docelową lokalizację posta</p>
          <p class="font-semibold text-sm">Opublikuj w profilu</p>
        </div>
      </div>

      <div class="mt-auto p-4 border-t border-gray-100">
        <button class="flex items-center space-x-3 text-gray-700 hover:bg-gray-100 w-full p-2 rounded-lg transition">
          <div class="bg-gray-300 rounded-full p-1">
            <MessageAlertIcon :size="18" class="text-gray-700"/>
          </div>
          <span class="font-medium">Przekaż opinię</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 ml-80 p-8 flex flex-col items-center">

      <div class="text-center mb-8 mt-4">
        <h2 class="text-2xl font-bold mb-1">Witaj ponownie, {{ userName }}!</h2>
        <p class="text-gray-500">Określ, jak chcesz poprowadzić transmisję na żywo.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full max-w-5xl mb-8">
        <div
          v-for="(card, index) in actionCards"

          :key="index"
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col h-full"
        >
          <div class="mb-4">
            <div :class="`inline-flex items-center justify-center w-12 h-12 rounded-full text-white ${card.iconColor}`">
              <component :is="card.mainIcon" :size="24" />
            </div>
          </div>

          <h3 class="font-bold text-lg mb-6">{{ card.description }}</h3>

          <ul class="space-y-5 mb-8 grow">
            <li v-for="(feature, fIndex) in card.features" :key="fIndex" class="flex items-start">
              <div class="mr-3 mt-0.5 text-gray-400">
                <component :is="feature.icon" :size="20" />
              </div>
              <span class="text-sm text-gray-600 leading-snug">{{ feature.text }}</span>
            </li>
          </ul>

          <RouterLink :to="card.link" class="w-full bg-[#E4F0FF] text-blue-700 font-semibold py-2.5 rounded-lg hover:bg-blue-100 transition active:scale-[0.98]">
            {{ card.buttonText }}
          </RouterLink>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 w-full max-w-5xl p-6 min-h-[300px]">

        <div class="flex space-x-2 mb-12">
          <button
            @click="currentTab = 'live'"
            :class="[
              'px-4 py-2 rounded-full text-sm font-semibold transition',
              currentTab === 'live' ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            Na żywo teraz
          </button>
          <button
            @click="currentTab = 'scheduled'"
            :class="[
              'px-4 py-2 rounded-full text-sm font-semibold transition',
              currentTab === 'scheduled' ? 'bg-blue-100 text-blue-700' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            Zaplanowane transmisje
          </button>
        </div>

        <div class="flex flex-col items-center justify-center text-center py-8">
          <div class="bg-gray-200 rounded-full p-4 mb-4">
            <VideoOutlineIcon :size="32" class="text-gray-500" />
          </div>
          <h4 class="text-lg font-medium text-black mb-1">Obecnie nie transmitujesz na żywo.</h4>
          <p class="text-sm text-gray-500">Szukasz transmisji? Upewnij się, że wybrałeś odpowiednią stronę.</p>
        </div>

      </div>

    </main>
  </div>
</template>

<style scoped>
/* Opcjonalne poprawki dla ikon, jeśli nie są wyrównane idealnie w linii tekstu */
.material-design-icon {
  display: inline-flex;
  align-self: center;
  position: relative;
  top: 0.125em;
}
.material-design-icon > .material-design-icon__svg {
    height: 1em;
    width: 1em;
}
</style>
