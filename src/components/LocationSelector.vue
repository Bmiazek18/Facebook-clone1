<script setup lang="ts">
import { ref, onMounted } from 'vue';

// --- TYPY ---
type LocationResult = {
  title: string;
  subtitle: string;
  type: 'city' | 'attraction' | 'park' | 'place';
  lat: string;
  lon: string;
};

const emit = defineEmits<{
  (e: 'confirm', location: LocationResult): void;
  (e: 'back'): void;
}>();

// --- STAN ---
const searchQuery = ref('');
const suggestions = ref<LocationResult[]>([]);

// searchedCity: Przechowuje nagłówek znalezionego miasta
const searchedCity = ref<LocationResult | null>(null);

// initLocationData: Dane dla obecnej lokalizacji GPS
const currentCity = ref<LocationResult | null>(null);
const currentAttractions = ref<LocationResult[]>([]);

const loading = ref(false);
const startLoading = ref(true); // Loading dla GPS na start
const selectedLocation = ref<LocationResult | null>(null);
let debounceTimeout: ReturnType<typeof setTimeout>;

// --- PARSOWANIE DANYCH ---
const parsePlace = (item: any, forceType?: LocationResult['type']): LocationResult => {
  const p = item.address || {};
  let title = item.name;
  if (!title) title = item.display_name ? item.display_name.split(',')[0] : 'Nieznane miejsce';

  // Budujemy podtytuł (Województwo, Kraj)
  const subtitleParts = [p.state, p.country].filter(Boolean);
  const subtitle = subtitleParts.join(', ');

  return {
    title,
    subtitle,
    type: forceType || 'place',
    lat: item.lat,
    lon: item.lon
  };
};

// --- API: ATRAKCJE Z WIKIPEDII ---
const fetchAttractions = async (lat: string, lon: string) => {
  try {
    const params = new URLSearchParams({
      action: 'query',
      list: 'geosearch',
      gscoord: `${lat}|${lon}`,
      gsradius: '10000', // Zwiększyłem do 10km dla lepszych wyników
      gslimit: '10',     // Więcej wyników
      format: 'json',
      origin: '*'
    });

    const url = `https://pl.wikipedia.org/w/api.php?${params.toString()}`;
    const res = await fetch(url);
    const data = await res.json();

    if (!data.query || !data.query.geosearch) return [];

    return data.query.geosearch.map((item: any) => ({
      title: item.title,
      subtitle: 'Ciekawe miejsce (Wikipedia)',
      type: 'attraction',
      lat: item.lat.toString(),
      lon: item.lon.toString()
    }));
  } catch (e) {
    console.error("Błąd Wiki:", e);
    return [];
  }
};

// --- API: WYSZUKIWANIE MIAST ---
const searchLocations = async () => {
  if (searchQuery.value.length < 3) return;

  loading.value = true;
  suggestions.value = [];
  searchedCity.value = null;

  try {
    // 1. Pobieramy wyniki z Nominatim
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(searchQuery.value)}&format=jsonv2&addressdetails=1&limit=10&countrycodes=pl&accept-language=pl`;
    const res = await fetch(url);
    const results = await res.json();

    // 2. FILTROWANIE: Bierzemy tylko miasta, miasteczka i wsie
    const cityResults = results.filter((r: any) =>
       r.addresstype === 'city' ||
       r.addresstype === 'town' ||
       r.addresstype === 'village'
    );

    if (cityResults.length > 0) {
      // Bierzemy najlepszy wynik (pierwsze miasto)
      const topCity = cityResults[0];
      const parsedCity = parsePlace(topCity, 'city');

      searchedCity.value = parsedCity;

      // Jeśli użytkownik nic nie wybrał, a znaleźliśmy miasto -> zaznacz je wstępnie
      if (!selectedLocation.value) {
          selectedLocation.value = parsedCity;
      }

      // 3. Pobieramy atrakcje z Wiki dla TEGO miasta
      const attractions = await fetchAttractions(topCity.lat, topCity.lon);
      suggestions.value = attractions;

    } else {
      // Brak miast
      suggestions.value = [];
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// --- INIT (GPS) ---
const initLocation = () => {
  if (!navigator.geolocation) {
    startLoading.value = false;
    return;
  }

  navigator.geolocation.getCurrentPosition(async (pos) => {
    try {
      const { latitude, longitude } = pos.coords;

      // Reverse geocoding - jak nazywa się moje miasto?
      const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}&zoom=10&accept-language=pl`;
      const res = await fetch(url);
      const data = await res.json();

      const cityName = data.address.city || data.address.town || data.address.village;

      if (cityName) {
        const myLocation: LocationResult = {
          title: cityName,
          subtitle: 'Twoja obecna lokalizacja',
          type: 'city',
          lat: data.lat,
          lon: data.lon
        };

        currentCity.value = myLocation;

        // --- KLUCZOWA ZMIANA: Ustawiamy Twoją lokalizację jako wybraną na starcie ---
        if (!selectedLocation.value && !searchQuery.value) {
            selectedLocation.value = myLocation;
        }

        // Pobierz atrakcje wokół Ciebie
        currentAttractions.value = await fetchAttractions(data.lat, data.lon);
      }
    } catch (e) {
      console.error('GPS Error', e);
    } finally {
      startLoading.value = false;
    }
  }, () => startLoading.value = false);
};

// --- UI HANDLERS ---
const onInput = () => {
  // Resetujemy wybór przy wpisywaniu, żeby user musiał kliknąć nowe
  selectedLocation.value = null;
  searchedCity.value = null;

  clearTimeout(debounceTimeout);
  if (!searchQuery.value) {
    suggestions.value = [];
    return;
  }
  debounceTimeout = setTimeout(searchLocations, 800); // Nieco dłuższy debounce dla oszczędności API
};

const selectLocation = (loc: LocationResult) => {
  selectedLocation.value = loc;
  // Opcjonalnie: wpisz nazwę do inputa
  // searchQuery.value = loc.title;
};

const confirm = () => {
  if (selectedLocation.value) emit('confirm', selectedLocation.value);
};

onMounted(initLocation);

// --- HELPERY CSS ---
const getIconClass = (type: string) => {
  switch (type) {
    case 'city': return 'bg-blue-600 text-white shadow-md ring-2 ring-blue-100';
    case 'attraction': return 'bg-purple-100 text-purple-600';
    default: return 'bg-gray-200 text-gray-500';
  }
};
</script>

<template>
  <div class="bg-white h-full w-full max-w-md mx-auto flex flex-col font-sans text-gray-800 relative">

    <div class="px-4 py-3 flex items-center mb-1 relative">
      <button @click="$emit('back')" class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition absolute left-4 z-10">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
      </button>
      <h2 class="text-xl font-bold w-full text-center">Wybierz miasto</h2>
    </div>

    <div class="px-4 mb-4">
      <div class="relative bg-gray-100 rounded-xl flex items-center shadow-inner transition-all focus-within:ring-2 focus-within:ring-blue-500 focus-within:bg-white">
        <div class="pl-3"><svg class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg></div>
        <input
          v-model="searchQuery"
          @input="onInput"
          type="text"
          class="w-full py-3 px-3 bg-transparent border-none focus:ring-0 text-gray-700 placeholder-gray-500 outline-none"
          placeholder="Wpisz nazwę miasta..."
        />
        <button v-if="searchQuery" @click="searchQuery = ''; suggestions = []; searchedCity = null;" class="pr-3 text-gray-400 hover:text-gray-600">
          <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-4 custom-scrollbar pb-20">

      <div v-if="loading" class="py-12 flex flex-col items-center text-blue-500 opacity-80">
        <svg class="animate-spin h-8 w-8 mb-2" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
        <span class="text-xs font-medium uppercase tracking-wide">Szukam miasta...</span>
      </div>

      <div v-if="!searchQuery && !loading">

         <div v-if="startLoading" class="space-y-4 opacity-50 pt-2 animate-pulse">
            <div class="h-20 bg-gray-200 rounded-xl"></div>
            <div class="h-8 bg-gray-200 rounded w-1/3"></div>
            <div class="h-12 bg-gray-100 rounded-xl"></div>
         </div>

         <div v-else-if="currentCity">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Twoja lokalizacja</h3>

            <div @click="selectLocation(currentCity!)"
                 :class="['flex items-center gap-4 p-4 rounded-xl cursor-pointer border mb-6 transition group relative overflow-hidden',
                 selectedLocation?.title === currentCity.title ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500' : 'bg-white border-gray-200 hover:border-blue-300 hover:shadow-md']">

              <div class="w-12 h-12 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center z-10">
                 <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
              </div>

              <div class="z-10">
                <span class="font-bold text-lg text-gray-900 block">{{ currentCity.title }}</span>
                <span class="text-sm text-gray-500">Użyj mojej lokalizacji</span>
              </div>

              <div v-if="selectedLocation?.title === currentCity.title" class="ml-auto text-blue-600 z-10">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
              </div>
            </div>

            <div v-if="currentAttractions.length > 0">
               <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3 flex items-center gap-2">
                 <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-8a2 2 0 012-2h14a2 2 0 012 2v8M9 10a2 2 0 012-2h2a2 2 0 012 2" /></svg>
                 Ciekawe miejsca w pobliżu
               </h3>
               <ul class="space-y-2">
                 <li v-for="(loc, index) in currentAttractions" :key="index" @click="selectLocation(loc)"
                     :class="['flex items-center gap-4 cursor-pointer p-3 rounded-xl border transition',
                     selectedLocation?.title === loc.title ? 'bg-blue-50 border-blue-400 shadow-sm' : 'bg-white border-transparent hover:bg-gray-50 hover:border-gray-200']">
                   <div class="w-8 h-8 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center text-xs font-bold">
                      {{ index + 1 }}
                   </div>
                   <div class="flex flex-col flex-1">
                     <span class="font-bold text-gray-900 leading-tight">{{ loc.title }}</span>
                     <span class="text-xs text-gray-500">Atrakcja</span>
                   </div>
                   <div v-if="selectedLocation?.title === loc.title" class="text-blue-600">
                     <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
                   </div>
                 </li>
               </ul>
            </div>
         </div>
      </div>

      <div v-else-if="!loading && (searchedCity || suggestions.length > 0)">

         <div v-if="searchedCity" class="mb-6">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Znalezione miasto</h3>
            <div @click="selectLocation(searchedCity!)"
                 :class="['flex items-center gap-4 p-4 rounded-xl cursor-pointer border transition shadow-sm',
                 selectedLocation?.title === searchedCity.title ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500' : 'bg-blue-600 text-white border-blue-600 hover:bg-blue-700']">

              <div :class="['w-12 h-12 rounded-full flex items-center justify-center', selectedLocation?.title === searchedCity.title ? 'bg-blue-600 text-white' : 'bg-white text-blue-600']">
                 <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
              </div>

              <div>
                <span :class="['font-bold text-lg block', selectedLocation?.title === searchedCity.title ? 'text-gray-900' : 'text-white']">{{ searchedCity.title }}</span>
                <span :class="['text-sm', selectedLocation?.title === searchedCity.title ? 'text-blue-600' : 'text-blue-100']">{{ searchedCity.subtitle }}</span>
              </div>

              <div v-if="selectedLocation?.title === searchedCity.title" class="ml-auto text-blue-600">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
              </div>
            </div>
         </div>

         <div v-if="suggestions.length > 0">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Warto zobaczyć w: {{ searchedCity?.title }}</h3>
            <ul class="space-y-2">
              <li v-for="(loc, index) in suggestions" :key="'s'+index" @click="selectLocation(loc)"
                  :class="['flex items-center gap-4 cursor-pointer p-3 rounded-xl border transition',
                  selectedLocation?.title === loc.title ? 'bg-blue-50 border-blue-400' : 'bg-white border-transparent hover:bg-gray-50 hover:border-gray-200']">

                <div class="flex-shrink-0 w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-8a2 2 0 012-2h14a2 2 0 012 2v8M9 10a2 2 0 012-2h2a2 2 0 012 2" /></svg>
                </div>

                <div class="flex flex-col flex-1 overflow-hidden">
                  <span class="font-bold text-gray-900 truncate">{{ loc.title }}</span>
                  <span class="text-sm text-gray-500 truncate">Atrakcja turystyczna</span>
                </div>

                <div v-if="selectedLocation?.title === loc.title" class="text-blue-600">
                   <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
                </div>
              </li>
            </ul>
         </div>
      </div>

      <div v-else-if="!loading && searchQuery" class="text-center py-10 text-gray-400">
          <p class="font-medium">Nie znaleziono takiego miasta.</p>
          <p class="text-sm mt-1">Sprawdź pisownię.</p>
      </div>

    </div>

    <div class="p-4 bg-white border-t border-gray-100 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)] z-20 sticky bottom-0">
        <button
          @click="confirm"
          :disabled="!selectedLocation"
          :class="['w-full py-3.5 rounded-xl font-bold text-lg transition shadow-lg flex items-center justify-center gap-2',
            selectedLocation ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-blue-200 transform active:scale-95' : 'bg-gray-200 text-gray-400 cursor-not-allowed']">

          <span v-if="selectedLocation">Wybierz: {{ selectedLocation.title }}</span>
          <span v-else>Wybierz lokalizację</span>

          <svg v-if="selectedLocation" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
        </button>
    </div>

  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0,0,0,0.1);
  border-radius: 20px;
}
</style>
