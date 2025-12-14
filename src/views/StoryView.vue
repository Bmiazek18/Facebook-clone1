<script setup lang="ts">
import { ref } from 'vue';

// Import ikon
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue';
import Close from 'vue-material-design-icons/Close.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import Play from 'vue-material-design-icons/Play.vue';
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue';
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue';
import Heart from 'vue-material-design-icons/Heart.vue';
import EmoticonExcited from 'vue-material-design-icons/EmoticonExcited.vue';
import EmoticonCry from 'vue-material-design-icons/EmoticonCry.vue';
import EmoticonAngry from 'vue-material-design-icons/EmoticonAngry.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';

// --- Typy danych ---
interface StoryUser {
  id: number;
  name: string;
  time: string;
  avatarColor: string;
  isUnseen: boolean;
  isActive?: boolean;
}

// --- Dane ---
const stories = ref<StoryUser[]>([
  { id: 1, name: 'AK PG Kwadratowa', time: '15 nowe • 6 godz.', avatarColor: 'bg-blue-600', isUnseen: true, isActive: true },
  { id: 2, name: 'Wib Wycieczki', time: '11 nowe • 4 godz.', avatarColor: 'bg-green-500', isUnseen: true },
  { id: 3, name: 'Warszawa Polak', time: '3 nowe • 4 min', avatarColor: 'bg-red-500', isUnseen: true },
  { id: 4, name: 'Beskid Liga Biznesowa', time: '18 nowe • 1 godz.', avatarColor: 'bg-yellow-500', isUnseen: true },
  { id: 5, name: 'MajorFootball', time: '7 nowe • 22 godz.', avatarColor: 'bg-purple-500', isUnseen: true },
]);

const currentStory = {
  user: 'AK PG Kwadratowa',
  time: '16 godz.',
  music: 'Lady Gaga • Bloody Mary',
  content: 'Dziś też możecie wpadać na dolną salę do @chudy_the_lighter',
  imagePlaceholder: 'bg-gradient-to-b from-blue-500 via-blue-900 to-black',
};

const messageInput = ref('');
</script>

<template>
  <div class="flex h-screen w-full bg-black overflow-hidden font-sans">

    <aside class="w-[360px] flex-shrink-0 bg-white border-r border-gray-300 flex flex-col h-full z-10 hidden md:flex">
      <div class="p-4 border-b border-gray-200">
        <h1 class="text-2xl font-bold text-black">Relacje</h1>
        <div class="flex gap-4 mt-2 text-sm font-medium text-blue-500 cursor-pointer">
          <span>Archiwum</span>
          <span>Ustawienia</span>
        </div>
      </div>
      <div class="p-3 hover:bg-gray-100 cursor-pointer flex items-center gap-3">
        <div class="relative">
          <div class="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xl">
            +
          </div>
        </div>
        <div>
          <p class="font-semibold text-black">Utwórz relację</p>
          <p class="text-xs text-gray-500">Udostępnij zdjęcie lub napisz...</p>
        </div>
      </div>
      <div class="px-4 py-2 text-base font-bold text-gray-900 mt-2">
        Wszystkie relacje
      </div>
      <div class="flex-1 overflow-y-auto custom-scrollbar">
        <div
          v-for="story in stories"
          :key="story.id"
          class="flex items-center gap-3 p-3 cursor-pointer transition-colors"
          :class="story.isActive ? 'bg-gray-100' : 'hover:bg-gray-50'"
        >
          <div class="relative p-[2px] rounded-full" :class="story.isUnseen ? 'bg-blue-600' : 'bg-gray-300'">
            <div class="border-2 border-white rounded-full">
               <div :class="`w-12 h-12 rounded-full ${story.avatarColor}`"></div>
            </div>
          </div>
          <div class="flex flex-col">
            <span class="font-semibold text-sm text-gray-900 leading-tight">{{ story.name }}</span>
            <span class="text-xs text-blue-600 font-medium mt-0.5">{{ story.time }}</span>
          </div>
        </div>
      </div>
    </aside>


    <main class="flex-1 relative flex flex-col items-center justify-center bg-black">

      <div class="absolute top-4 left-4 z-30">
        <div class="w-10 h-10 bg-gray-800 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-700 transition">
          <Close class="text-white" :size="24" />
        </div>
      </div>
      <div class="absolute top-4 right-4 z-30 flex gap-2">
          <div class="w-10 h-10 bg-gray-800 rounded-full flex items-center justify-center hover:bg-gray-700 transition">
            <DotsHorizontal class="text-white" />
          </div>
      </div>

      <div class="flex flex-col items-center justify-center w-full h-full max-h-screen py-4">

        <div class="relative flex items-center justify-center">

            <button class="absolute -left-16 z-20 w-12 h-12 bg-gray-700 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition shadow-lg">
              <ChevronLeft :size="36" />
            </button>

            <div class="relative aspect-9/16 h-[90vh] bg-gray-900 rounded-lg overflow-hidden shadow-2xl flex flex-col border border-gray-800">

              <div :class="`absolute inset-0 z-0 ${currentStory.imagePlaceholder}`">
                 <div class="absolute bottom-0 w-full h-2/3 bg-gradient-to-t from-black/60 to-transparent"></div>
              </div>

              <div class="absolute top-2 left-2 right-2 flex gap-1 z-10">
                <div class="h-1 flex-1 bg-white/30 rounded-full overflow-hidden">
                   <div class="h-full w-full bg-white"></div>
                </div>
                 <div class="h-1 flex-1 bg-white/30 rounded-full"></div>
              </div>

              <div class="absolute top-6 left-4 right-4 flex justify-between items-start z-10 text-white">
                 <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-blue-600 rounded-full border border-gray-400"></div>
                    <div class="flex flex-col justify-center">
                       <div class="flex items-center gap-2">
                         <span class="font-semibold text-sm hover:underline cursor-pointer">{{ currentStory.user }}</span>
                         <span class="text-gray-400 text-xs">{{ currentStory.time }}</span>
                       </div>
                       <div class="flex items-center gap-1 mt-0.5 text-xs font-medium text-white/90">
                          <MusicNote :size="12" />
                          <span>{{ currentStory.music }}</span>
                       </div>
                    </div>
                 </div>
                 <div class="flex items-center gap-4">
                    <Play :size="24" class="cursor-pointer hover:opacity-80"/>
                    <VolumeHigh :size="24" class="cursor-pointer hover:opacity-80"/>
                    <DotsHorizontal :size="24" class="cursor-pointer hover:opacity-80"/>
                 </div>
              </div>

              <div class="absolute top-1/3 left-0 right-0 px-6 z-10 text-center">
                 <div class="bg-black/30 backdrop-blur-sm p-4 rounded-xl inline-block">
                   <p class="text-white text-xl font-medium leading-relaxed">
                     Dziś też możecie wpadać <br> na dolną salę do <br>
                     <span class="font-bold underline cursor-pointer">@chudy_the_lighter</span>
                   </p>
                 </div>
                 <div class="absolute -top-8 right-4 transform rotate-12 shadow-lg">
                    <div class="bg-blue-500/20 backdrop-blur-md p-2 rounded-lg border border-white/40 w-16 h-16 flex items-center justify-center">
                       <div class="w-8 h-8 border-2 border-white rounded-sm"></div>
                    </div>
                 </div>
              </div>
            </div> <button class="absolute -right-16 z-20 w-12 h-12 bg-gray-700 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition shadow-lg">
              <ChevronRight :size="36" />
            </button>
        </div>


        <div class="w-full max-w-[600px] mt-4 flex items-center gap-3 px-4">
            <div class="flex-1 relative">
               <input
                 v-model="messageInput"
                 type="text"
                 placeholder="Odpowiedz..."
                 class="w-full bg-gray-800 text-white border border-gray-700 rounded-full py-3 pl-5 pr-12 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 placeholder-gray-400 transition"
               />
               <EmoticonOutline class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer hover:text-white" :size="24"/>
            </div>

            <div class="flex items-center gap-2">
                <div class="cursor-pointer text-blue-500 hover:scale-110 transition p-2 bg-gray-800/50 rounded-full"><ThumbUp :size="28"/></div>
                <div class="cursor-pointer text-red-500 hover:scale-110 transition p-2 bg-gray-800/50 rounded-full"><Heart :size="28"/></div>
                <div class="cursor-pointer text-yellow-400 hover:scale-110 transition p-2 bg-gray-800/50 rounded-full"><EmoticonExcited :size="28"/></div>
                <div class="cursor-pointer text-orange-400 hover:scale-110 transition p-2 bg-gray-800/50 rounded-full"><EmoticonCry :size="28"/></div>
                <div class="cursor-pointer text-red-600 hover:scale-110 transition p-2 bg-gray-800/50 rounded-full"><EmoticonAngry :size="28"/></div>
            </div>
        </div>

      </div>
    </main>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(209, 213, 219, 0.5);
  border-radius: 20px;
}
</style>
