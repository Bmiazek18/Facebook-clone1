<script setup>
import { ref } from 'vue'

const activeTab = ref('Zdjęcia')
const tabs = ['Zdjęcia', 'Filmy', 'Albumy']

// Dane dla sekcji Zdjęcia
const photos = ref([
  { id: 1, url: 'https://via.placeholder.com/300?text=Sędzia+1' },
  { id: 2, url: 'https://via.placeholder.com/300?text=Sędzia+2' },
  { id: 3, url: 'https://via.placeholder.com/300?text=Sprzęt' },
  { id: 4, url: 'https://via.placeholder.com/300?text=Koszulka' },
  { id: 5, url: 'https://via.placeholder.com/300?text=Tabela' },
  { id: 6, url: 'https://via.placeholder.com/300?text=Składki' },
])

// Dane dla sekcji Albumy
const albums = ref([
  {
    id: 1,
    title: '25 października 2018',
    count: 1,
    coverUrl: 'https://via.placeholder.com/400?text=Okładka+Albumu',
  },
])
</script>

<template>
  <div class="bg-theme-bg-secondary text-theme-text p-6 mt-4  ">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-xl font-bold">{{ $t('groups.media') }}</h1>
      <div class="flex space-x-6 text-sm">
        <NuxtLink
          to="/addAlbum"
          class="text-theme-primary hover:text-theme-primary-hover transition flex items-center font-medium"
        >
          <span class="text-xl mr-1">+</span>{{ $t('profile.utworzAlbum') }}</NuxtLink>
        <button class="text-theme-primary hover:text-theme-primary-hover transition font-medium">{{ $t('profile.dodajZdjeciaFilm') }}</button>
      </div>
    </div>

    <div class="flex space-x-8 mb-6 border-b border-theme-border">
      <button
        v-for="tab in tabs"
        :key="tab"
        @click="activeTab = tab"
        :class="[
          'pb-3 text-sm font-semibold transition-all relative px-1',
          activeTab === tab
            ? 'text-theme-primary'
            : 'text-theme-text-secondary hover:text-theme-text',
        ]"
      >
        {{ tab }}
        <div
          v-if="activeTab === tab"
          class="absolute bottom-0 left-0 right-0 h-[3px] bg-theme-primary rounded-t-full"
        ></div>
      </button>
    </div>

    <div
      v-if="activeTab === 'Zdjęcia'"
      class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-7 gap-1"
    >
      <div
        v-for="photo in photos"
        :key="photo.id"
        class="aspect-square overflow-hidden cursor-pointer"
      >
        <img
          :src="photo.url"
          class="w-full h-full object-cover transition duration-200 hover:brightness-125"
        />
      </div>
    </div>

    <div
      v-if="activeTab === 'Albumy'"
      class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-6"
    >
      <div v-for="album in albums" :key="album.id" class="group cursor-pointer text-left">
        <div class="aspect-square overflow-hidden rounded-md border border-theme-border mb-3">
          <img
            :src="album.coverUrl"
            class="w-full h-full object-cover transition duration-200 group-hover:brightness-125"
          />
        </div>
        <h3 class="text-[15px] font-bold text-theme-text leading-snug">{{ album.title }}</h3>
        <p class="text-[13px] text-theme-text-secondary">{{ $t('profile.albumCountZdjecie') }}</p>
      </div>
    </div>

    <div v-if="activeTab === 'Filmy'" class="py-10 text-center text-theme-text-secondary">{{ $t('profile.brakFilmowDoWyswietlenia') }}</div>
  </div>
</template>
