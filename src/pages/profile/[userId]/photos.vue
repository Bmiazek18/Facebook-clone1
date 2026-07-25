<script setup lang="ts">
import { ref } from 'vue'

definePageMeta({
  keepScroll: true,
})

// Ikony
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import EyeOutline from 'vue-material-design-icons/EyeOutline.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import Magnify from 'vue-material-design-icons/Magnify.vue'

// --- STANY AKTYWNYCH SUB-TABÓW ---
const activePhotosTab = ref<'yours' | 'albums'>('yours')
const activeReelsTab = ref<'yours' | 'saved'>('yours')

// --- PRZYKŁADOWE DANE ---
const photos = ref([
  { id: 1, url: 'https://picsum.photos/id/10/300/300', editable: true },
  { id: 2, url: 'https://picsum.photos/id/20/300/300', editable: true },
  { id: 3, url: 'https://picsum.photos/id/30/300/300', editable: true },
])

// Dane albumów na podstawie Twojego zrzutu ekranu
const albums = ref([
  { id: 1, title: 'Zdjęcia profilowe', count: 2, coverUrl: 'https://picsum.photos/id/64/300/300' },
  { id: 2, title: 'Zdjęcia w tle', count: 5, coverUrl: 'https://picsum.photos/id/29/300/300' },
])

const reels = ref([{ id: 1, url: 'https://picsum.photos/id/40/200/400', views: '26' }])

const pastEvents = ref([
  {
    id: 1,
    date: 'Sob, 30 maj',
    title: 'Rajd Elektronika 2026',
    desc: 'Pole namiotowe Małdyty "Dzika Gęś" · Morąg',
    organizer: 'WRS ETI',
    img: 'https://picsum.photos/id/50/150/100',
  },
  {
    id: 2,
    date: 'Pt, 29 maj',
    title: 'JUWENALIA TRÓJMIASTA 2026 ⭐️ FESTIWAL ⭐️ 29-31 MAJ ⭐️',
    desc: 'Wydarzenie Studenci Trójmiasto',
    organizer: 'Studenci Trójmiasto',
    img: 'https://picsum.photos/id/60/150/100',
  },
  {
    id: 3,
    date: 'Sob, 16 maj',
    title: 'Technikalia.26',
    desc: 'Camper Park Politechniki Gdańskiej, ul. Towarowa 40...',
    organizer: 'WRS ETI',
    img: 'https://picsum.photos/id/70/150/100',
  },
  {
    id: 4,
    date: 'Czw, 9 paź 2025',
    title: '🐴 Wielkie Otrzęsiny Studenckie 🌵|| EiA, ETI, WILIŚ, ZIE',
    desc: 'AK PG Kwadratowa · Gdańsk',
    organizer: 'WRS EiA',
    img: 'https://picsum.photos/id/80/150/100',
  },
])

const groups = ref([
  {
    id: 1,
    name: 'Studenci Miasta Gdańsk !!',
    type: 'Grupa publiczna',
    members: '24.6K członków',
    img: 'https://picsum.photos/id/100/100/100',
  },
  {
    id: 2,
    name: 'PG Kupię/Sprzedam Politechnika Gdańska',
    type: 'Grupa publiczna',
    members: '5.7K członków',
    img: 'https://picsum.photos/id/110/100/100',
  },
  {
    id: 3,
    name: 'Tanie loty królu złoty!',
    type: 'Grupa publiczna',
    members: '256.8K członków',
    img: 'https://picsum.photos/id/120/100/100',
  },
  {
    id: 4,
    name: 'Absurdalnie Tanie Loty',
    type: 'Grupa publiczna',
    members: '1.6M członków',
    img: 'https://picsum.photos/id/130/100/100',
  },
])
</script>

<template>
  <div class="bg-[#F0F2F5] p-4 space-y-4 antialiased   text-[#050505]">
    <div class="bg-white rounded-xl shadow-sm p-4">
      <div class="flex items-center justify-between mb-2">
        <h2 class="text-[20px] font-bold">Zdjęcia</h2>
        <div class="flex items-center gap-2">
          <button
            class="text-[#1877F2] hover:bg-blue-50 px-3 py-2 rounded-md font-medium text-[15px] transition-colors"
          >
            Dodaj zdjęcia/film
          </button>
          <button class="p-2 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full transition-colors">
            <DotsHorizontal :size="16" />
          </button>
        </div>
      </div>

      <div class="flex items-center justify-between border-b border-gray-200 mb-4">
        <div class="flex gap-1">
          <button
            @click="activePhotosTab = 'yours'"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative"
            :class="
              activePhotosTab === 'yours' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'
            "
          >
            Twoje zdjęcia
            <div
              v-if="activePhotosTab === 'yours'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>
          <button
            @click="activePhotosTab = 'albums'"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative"
            :class="
              activePhotosTab === 'albums' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'
            "
          >
            Albumy
            <div
              v-if="activePhotosTab === 'albums'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>
        </div>
        <button class="p-2 text-[#65676B] hover:bg-gray-100 rounded-full">
          <Magnify :size="20" />
        </button>
      </div>

      <div
        v-if="activePhotosTab === 'yours'"
        class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-1.5"
      >
        <div
          v-for="img in photos"
          :key="img.id"
          class="relative aspect-square group overflow-hidden rounded-md border border-gray-200"
        >
          <img
            :src="img.url"
            class="w-full h-full object-cover transition-transform duration-200 group-hover:scale-102"
          />
          <button
            v-if="img.editable"
            class="absolute top-2 right-2 p-1.5 bg-black/60 hover:bg-black/80 text-white rounded-full transition-colors shadow-sm"
          >
            <Pencil :size="16" />
          </button>
        </div>
      </div>

      <div
        v-if="activePhotosTab === 'albums'"
        class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4"
      >
        <NuxtLink to="/addAlbum" class="cursor-pointer group">
          <div
            class="aspect-square w-full bg-[#E4E6EB] hover:bg-[#D8DADF] transition rounded-xl flex items-center justify-center border border-gray-200"
          >
            <Plus :size="36" class="text-[#65676B]" />
          </div>
          <div class="mt-2 pl-1">
            <h4 class="text-[15px] font-semibold text-[#050505]">Utwórz album</h4>
          </div>
        </NuxtLink>

        <div v-for="album in albums" :key="album.id" class="cursor-pointer group">
          <div
            class="aspect-square w-full rounded-xl overflow-hidden border border-gray-200 hover:brightness-95 transition"
          >
            <img :src="album.coverUrl" class="w-full h-full object-cover" />
          </div>
          <div class="mt-2 pl-1">
            <h4 class="text-[15px] font-semibold text-[#050505] leading-tight truncate">
              {{ album.title }}
            </h4>
            <p class="text-[13px] text-[#65676B] mt-0.5">{{ album.count }} elementy</p>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm p-4">
      <div class="flex items-center justify-between mb-2">
        <h2 class="text-[20px] font-bold">Rolki</h2>
        <button
          class="text-[#1877F2] hover:bg-blue-50 px-3 py-2 rounded-md font-medium text-[15px] transition-colors"
        >
          Utwórz rolkę
        </button>
      </div>

      <div class="flex gap-1 border-b border-gray-200 mb-4">
        <button
          @click="activeReelsTab = 'yours'"
          class="px-4 py-3 text-[15px] font-semibold relative"
          :class="activeReelsTab === 'yours' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
        >
          Twoje rolki
          <div
            v-if="activeReelsTab === 'yours'"
            class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
          ></div>
        </button>
        <button
          @click="activeReelsTab = 'saved'"
          class="px-4 py-3 text-[15px] font-semibold relative"
          :class="activeReelsTab === 'saved' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
        >
          Zapisane rolki
          <div
            v-if="activeReelsTab === 'saved'"
            class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
          ></div>
        </button>
      </div>

      <div class="grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 gap-2">
        <div
          v-for="reel in reels"
          :key="reel.id"
          class="relative aspect-[9/16] w-full rounded-xl overflow-hidden group shadow-sm"
        >
          <img :src="reel.url" class="w-full h-full object-cover" />
          <div
            class="absolute bottom-2 left-2 flex items-center gap-1 text-white text-[13px] font-semibold drop-shadow-[0_1px_4px_rgba(0,0,0,0.8)]"
          >
            <EyeOutline :size="16" />
            <span>{{ reel.views }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm p-4">
      <div class="flex items-center justify-between mb-2">
        <h2 class="text-[20px] font-bold">Wydarzenia</h2>
        <div class="flex items-center gap-2">
          <button
            class="bg-[#E7F3FF] hover:bg-[#DBE7F2] text-[#1877F2] px-3 h-9 rounded-md font-semibold text-[14px] flex items-center gap-1.5 transition-colors"
          >
            <Plus :size="16" />
            Utwórz wydarzenie
          </button>
          <button class="p-2 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full transition-colors">
            <DotsHorizontal :size="16" />
          </button>
        </div>
      </div>

      <div class="border-b border-gray-200 mb-4">
        <div class="px-4 py-3 text-[15px] font-semibold text-[#1877F2] inline-block relative">
          Wydarzenia z przeszłości
          <div class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"></div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div
          v-for="event in pastEvents"
          :key="event.id"
          class="flex gap-3 p-2 rounded-xl border border-gray-100 hover:bg-gray-50 transition-colors cursor-pointer"
        >
          <img
            :src="event.img"
            class="w-[140px] h-[84px] object-cover rounded-lg border border-gray-100 shrink-0"
          />
          <div class="flex flex-col justify-center min-w-0">
            <span class="text-[12px] text-[#980202] font-medium uppercase tracking-wide">{{
              event.date
            }}</span>
            <h3 class="text-[15px] font-bold text-[#050505] leading-tight truncate mt-0.5">
              {{ event.title }}
            </h3>
            <span class="text-[13px] text-[#65676B] truncate mt-0.5">{{ event.desc }}</span>
            <span class="text-[12px] text-[#65676B] font-semibold mt-1"
              >Wydarzenie {{ event.organizer }}</span
            >
          </div>
        </div>
      </div>

      <button
        class="w-full mt-4 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold text-[15px] rounded-lg transition-colors text-center"
      >
        Zobacz wszystko
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm p-4">
      <div class="flex items-center justify-between mb-2">
        <h2 class="text-[20px] font-bold">Grupy</h2>
        <button class="p-2 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full transition-colors">
          <DotsHorizontal :size="16" />
        </button>
      </div>

      <div class="border-b border-gray-200 mb-4">
        <div class="px-4 py-3 text-[15px] font-semibold text-[#1877F2] inline-block relative">
          Publiczne
          <div class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"></div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="group in groups"
          :key="group.id"
          class="flex items-center gap-3 p-1 hover:bg-gray-50 rounded-xl transition-colors cursor-pointer"
        >
          <img
            :src="group.img"
            class="w-[60px] h-[60px] object-cover rounded-xl border border-gray-200/60 shrink-0"
          />
          <div class="flex flex-col min-w-0">
            <h3 class="text-[15px] font-bold text-[#050505] leading-snug truncate hover:underline">
              {{ group.name }}
            </h3>
            <div class="flex items-center gap-1 text-[13px] text-[#65676B] mt-0.5">
              <Earth :size="14" class="inline text-[#65676B]" />
              <span>{{ group.type }}</span>
              <span>·</span>
              <span>{{ group.members }}</span>
            </div>
          </div>
        </div>
      </div>

      <button
        class="w-full mt-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold text-[15px] rounded-lg transition-colors text-center"
      >
        Zobacz wszystko
      </button>
    </div>
  </div>
</template>
