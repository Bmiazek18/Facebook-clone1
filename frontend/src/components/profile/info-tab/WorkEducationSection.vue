<script setup lang="ts">
import { ref, reactive, inject, computed, watch } from 'vue'

// Importy ikon (Vue Material Design Icons)
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import HomeOutline from 'vue-material-design-icons/HomeOutline.vue'
import CakeVariant from 'vue-material-design-icons/CakeVariant.vue'
import HeartOutline from 'vue-material-design-icons/HeartOutline.vue'
import PineTree from 'vue-material-design-icons/PineTree.vue'
import CircleMultipleOutline from 'vue-material-design-icons/CircleMultipleOutline.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import Translate from 'vue-material-design-icons/Translate.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue'
import Lock from 'vue-material-design-icons/Lock.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import DeleteOutline from 'vue-material-design-icons/DeleteOutline.vue'

const isOwner = inject('isOwner', true)

import BaseModal from '@/components/common/BaseModal.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'

// --- PRYWATNOŚĆ ---
const showPrivacyModal = ref(false)
const activePrivacyItem = ref<
  'location' | 'birthDate' | 'birthYear' | 'gender' | 'pronouns' | null
>(null)

const locationPrivacy = ref('public')
const birthDatePrivacy = ref('friends')
const birthYearPrivacy = ref('friends')
const genderPrivacy = ref('only_me')
const pronounsPrivacy = ref('public')

const activePrivacyValue = computed(() => {
  if (activePrivacyItem.value === 'location') return locationPrivacy.value
  if (activePrivacyItem.value === 'birthDate') return birthDatePrivacy.value
  if (activePrivacyItem.value === 'birthYear') return birthYearPrivacy.value
  if (activePrivacyItem.value === 'gender') return genderPrivacy.value
  if (activePrivacyItem.value === 'pronouns') return pronounsPrivacy.value
  return 'public'
})

const openPrivacySelector = (
  item: 'location' | 'birthDate' | 'birthYear' | 'gender' | 'pronouns',
) => {
  activePrivacyItem.value = item
  showPrivacyModal.value = true
}

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  if (activePrivacyItem.value === 'location') {
    locationPrivacy.value = payload.id
  } else if (activePrivacyItem.value === 'birthDate') {
    birthDatePrivacy.value = payload.id
  } else if (activePrivacyItem.value === 'birthYear') {
    birthYearPrivacy.value = payload.id
  } else if (activePrivacyItem.value === 'gender') {
    genderPrivacy.value = payload.id
  } else if (activePrivacyItem.value === 'pronouns') {
    pronounsPrivacy.value = payload.id
  }
  showPrivacyModal.value = false
  activePrivacyItem.value = null
}

const getPrivacyLabel = (privacy: string) => {
  switch (privacy) {
    case 'public':
      return 'Publiczne'
    case 'friends':
      return 'Znajomi'
    case 'only_me':
      return 'Tylko ja'
    case 'close_friends':
      return 'Bliscy znajomi'
    case 'friends_except':
      return 'Nie wyświetlaj...'
    case 'specific_friends':
      return 'Wyświetlaj tylko...'
    default:
      return 'Publiczne'
  }
}

const getPrivacyIcon = (privacy: string) => {
  switch (privacy) {
    case 'public':
      return Earth
    case 'friends':
      return AccountMultiple
    case 'only_me':
      return Lock
    case 'close_friends':
      return AccountMultiple
    case 'friends_except':
      return AccountMultiple
    case 'specific_friends':
      return AccountMultiple
    default:
      return Earth
  }
}

// --- STANY EDYCJI ---
const isEditingGender = ref(false)
const isEditingLocation = ref(false)
const isLocationFocused = ref(false)

// --- FORMULARZE ---
const genderForm = reactive({
  value: 'Mężczyzna',
  showOnProfile: false,
})

const locationForm = reactive({
  value: 'Łuków, Polska',
})

// Kopia wartości przed edycją (do anulowania)
const initialLocationValue = ref('')

// --- STAN API LOKALIZACJI ---
interface LocationSuggestion {
  id: string | number
  title: string
  country: string
  meta: string
}

const locationSuggestions = ref<LocationSuggestion[]>([])
const isLoadingLocations = ref(false)
let debounceTimeout: ReturnType<typeof setTimeout> | null = null

// --- FUNKCJA FETCHUJĄCA Z PRAWDZIWEGO API (OpenStreetMap Nominatim) ---
const fetchLocationsFromAPI = async (query: string) => {
  if (!query || query.trim().length < 3) {
    locationSuggestions.value = []
    return
  }

  isLoadingLocations.value = true
  try {
    // Parametry zapytania dostosowane pod format adresowy i język polski
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&addressdetails=1&limit=5&accept-language=pl`

    const response = await fetch(url, {
      headers: {
        'User-Agent': 'VueFacebookCloneApp/1.0', // Dobra praktyka przy korzystaniu z darmowego Nominatim
      },
    })
    const data = await response.json()

    // Mapowanie odpowiedzi z API na nasz wewnętrzny format
    locationSuggestions.value = data.map((item: any) => {
      const city =
        item.address.city ||
        item.address.town ||
        item.address.village ||
        item.address.municipality ||
        item.display_name.split(',')[0]
      const state = item.address.state || ''
      const country = item.address.country || ''

      // Tworzymy czytelny tytuł (np. "Łuków, Województwo lubelskie")
      const title = state ? `${city}, ${state}` : city

      return {
        id: item.place_id,
        title: title,
        country: country,
        meta: item.display_name, // Pełna ścieżka jako tekst pomocniczy
      }
    })
  } catch (error) {
    console.error('Błąd podczas pobierania lokalizacji:', error)
  } finally {
    isLoadingLocations.value = false
  }
}

// Watcher z mechanizmem debounce (odczekaj 400ms przed strzałem do API)
watch(
  () => locationForm.value,
  (newQuery) => {
    // Nie szukaj, jeśli input stracił focus (np. po kliknięciu w pozycję z listy)
    if (!isLocationFocused.value) return

    if (debounceTimeout) clearTimeout(debounceTimeout)

    debounceTimeout = setTimeout(() => {
      fetchLocationsFromAPI(newQuery)
    }, 400)
  },
)

// Sprawdza, czy wartość się zmieniła, aby odblokować przycisk Zapisz
const isLocationChanged = computed(() => {
  return locationForm.value !== initialLocationValue.value
})

// --- FUNKCJE OBSŁUGI ---
const startEditingLocation = () => {
  initialLocationValue.value = locationForm.value
  isEditingLocation.value = true
}

const cancelEditingLocation = () => {
  locationForm.value = initialLocationValue.value
  isEditingLocation.value = false
}

const saveLocation = () => {
  if (!isLocationChanged.value) return
  isEditingLocation.value = false
}

const deleteLocation = () => {
  locationForm.value = ''
  locationSuggestions.value = []
}

const selectLocation = (locTitle: string) => {
  locationForm.value = locTitle
  // Czyszczenie podpowiedzi po wyborze, by dropdown zniknął
  locationSuggestions.value = []
  isLocationFocused.value = false
}

const handleLocationBlur = () => {
  setTimeout(() => {
    isLocationFocused.value = false
  }, 200) // Zwiększone do 200ms na wypadek wolniejszego kliknięcia mobilnego
}
</script>

<template>
  <div class="max-w-[850px] mx-auto text-theme-text antialiased">
    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Lokalizacja</h3>

      <div v-if="isEditingLocation" class="mt-2">
        <button
          type="button"
          @click="openPrivacySelector('location')"
          class="inline-flex items-center gap-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary px-3 py-1.5 rounded-md font-semibold text-[15px] text-theme-text mb-4 transition-colors cursor-pointer"
        >
          <component :is="getPrivacyIcon(locationPrivacy)" :size="16" class="text-theme-text-secondary" />
          <span>{{ getPrivacyLabel(locationPrivacy) }}</span>
        </button>

        <div class="relative">
          <div
            class="relative border-[1px] rounded-lg bg-white transition-colors"
            :class="
              isLocationFocused
                ? 'border-[#1877F2] ring-1 ring-[#1877F2]'
                : 'border-theme-border hover:border-[#8A8D91]'
            "
          >
            <span
              class="absolute top-2 left-3 text-[13px] font-normal transition-colors cursor-text"
              :class="isLocationFocused ? 'text-[#1877F2]' : 'text-theme-text-secondary'"
            >
              Aktualna miejscowość
            </span>
            <input
              v-model="locationForm.value"
              @focus="isLocationFocused = true"
              @blur="handleLocationBlur"
              placeholder="Wpisz min. 3 znaki miejscowości..."
              class="w-full pt-6 pb-2 px-3 bg-transparent outline-none text-[15px] text-theme-text"
            />

            <div v-if="isLoadingLocations" class="absolute right-3 top-1/2 -translate-y-1/2">
              <div
                class="animate-spin rounded-full h-4 w-4 border-2 border-[#1877F2] border-t-transparent"
              ></div>
            </div>
          </div>

          <div
            v-if="isLocationFocused && locationSuggestions.length > 0"
            class="absolute left-0 right-0 top-full mt-1 bg-white rounded-lg shadow-[0_2px_12px_rgba(0,0,0,0.2)] z-50 overflow-hidden border border-theme-border max-h-[300px] overflow-y-auto"
          >
            <div
              v-for="loc in locationSuggestions"
              :key="loc.id"
              @mousedown.prevent="selectLocation(loc.title)"
              class="flex items-start gap-3 p-3 hover:bg-theme-hover cursor-pointer"
            >
              <div
                class="w-10 h-10 rounded-lg bg-theme-bg-tertiary flex items-center justify-center shrink-0"
              >
                <MapMarker :size="24" class="text-white" />
              </div>
              <div class="flex flex-col justify-center min-h-[40px] w-full overflow-hidden">
                <span class="text-[15px] font-semibold text-theme-text leading-tight truncate">{{
                  loc.title
                }}</span>
                <span class="text-[13px] text-theme-text-secondary leading-tight mt-0.5">{{
                  loc.country
                }}</span>
                <span
                  v-if="loc.meta"
                  class="text-[11px] text-theme-text-secondary leading-tight mt-0.5 truncate block w-full"
                  >{{ loc.meta }}</span
                >
              </div>
            </div>
          </div>

          <div
            v-if="
              isLocationFocused &&
              locationSuggestions.length === 0 &&
              locationForm.value.trim().length >= 3 &&
              !isLoadingLocations
            "
            class="absolute left-0 right-0 top-full mt-1 bg-white rounded-lg shadow-[0_2px_12px_rgba(0,0,0,0.2)] z-50 p-4 border border-theme-border text-center text-theme-text-secondary text-[14px]"
          >
            Nie znaleziono pasujących miejsc dla tej frazy
          </div>
        </div>

        <div class="flex justify-between items-center pt-4 border-t border-theme-border mt-4">
          <button
            @click="deleteLocation"
            type="button"
            class="flex items-center gap-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary px-4 py-1.5 rounded-md font-semibold text-[15px] text-theme-text transition-colors"
          >
            <DeleteOutline :size="20" />
            Usuń
          </button>

          <div class="flex gap-2">
            <button
              @click="cancelEditingLocation"
              type="button"
              class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
            >
              Anuluj
            </button>
            <button
              @click="saveLocation"
              :disabled="!isLocationChanged"
              :class="
                isLocationChanged
                  ? 'bg-[#1877F2] hover:bg-[#166FE5] text-white cursor-pointer'
                  : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'
              "
              class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
            >
              Zapisz
            </button>
          </div>
        </div>
      </div>

      <div v-else class="flex justify-between items-start group">
        <div class="flex gap-4">
          <div
            class="w-10 h-10 rounded-full bg-theme-bg-tertiary flex items-center justify-center shrink-0"
          >
            <MapMarker :size="24" class="text-[#1877F2]" />
          </div>
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] font-medium text-[#1877F2] hover:underline cursor-pointer">
              {{ locationForm.value || 'Dodaj aktualne miejsce zamieszkania' }}
            </span>
            <span class="text-[13px] text-theme-text-secondary">Aktualne miejsce zamieszkania</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('location')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            title="Zmień ustawienia prywatności"
          >
            <component :is="getPrivacyIcon(locationPrivacy)" :size="16" class="text-theme-text-secondary" />
          </button>
          <button
            @click="startEditingLocation"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Miejscowość rodzinna</h3>
      <div class="flex items-center gap-4 cursor-pointer group">
        <HomeOutline :size="28" class="text-theme-text-secondary" />
        <span class="text-[15px] font-medium text-theme-text-secondary group-hover:underline"
          >Miejscowość rodzinna</span
        >
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Data urodzenia</h3>
      <div class="flex justify-between items-start group">
        <div class="flex gap-4 w-full max-w-[300px]">
          <CakeVariant :size="28" class="text-theme-text shrink-0" />
          <div class="flex flex-col gap-4 w-full mt-1">
            <div class="flex justify-between items-center w-full">
              <div class="flex flex-col">
                <span class="text-[15px] text-theme-text">23 lutego</span>
                <span class="text-[13px] text-theme-text-secondary">Data urodzenia</span>
              </div>
              <button
                @click="openPrivacySelector('birthDate')"
                class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
                title="Zmień ustawienia prywatności"
              >
                <component
                  :is="getPrivacyIcon(birthDatePrivacy)"
                  :size="16"
                  class="text-theme-text-secondary"
                />
              </button>
            </div>
            <div class="flex justify-between items-center w-full">
              <div class="flex flex-col">
                <span class="text-[15px] text-theme-text">2005</span>
                <span class="text-[13px] text-theme-text-secondary">Rok urodzenia</span>
              </div>
              <button
                @click="openPrivacySelector('birthYear')"
                class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
                title="Zmień ustawienia prywatności"
              >
                <component
                  :is="getPrivacyIcon(birthYearPrivacy)"
                  :size="16"
                  class="text-theme-text-secondary"
                />
              </button>
            </div>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-start">
          <button class="p-2 hover:bg-theme-hover rounded-full transition-colors">
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Status</h3>
      <div class="flex items-center gap-4 cursor-pointer group">
        <HeartOutline :size="28" class="text-theme-text-secondary" />
        <span class="text-[15px] font-medium text-theme-text-secondary group-hover:underline"
          >Status związku</span
        >
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Członkowie rodziny</h3>
      <div class="flex items-center gap-4 cursor-pointer group">
        <PineTree :size="28" class="text-theme-text-secondary" />
        <span class="text-[15px] font-medium text-theme-text-secondary group-hover:underline">Rodzina</span>
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Płeć</h3>
      <div v-if="isEditingGender" class="mt-2">
        <div
          class="relative bg-theme-bg-tertiary hover:bg-theme-bg-tertiary transition-colors rounded-md px-3 py-2.5 flex justify-between items-center cursor-pointer mb-4"
        >
          <span class="text-[15px] text-theme-text">{{ genderForm.value }}</span>
          <ChevronDown :size="24" class="text-theme-text" />
        </div>
        <label class="flex items-center gap-3 cursor-pointer mb-6">
          <input
            type="checkbox"
            v-model="genderForm.showOnProfile"
            class="w-5 h-5 rounded text-[#1877F2] border-gray-400 focus:ring-[#1877F2]"
          />
          <span class="text-[15px] text-theme-text font-medium">Wyświetl w moim profilu</span>
        </label>
        <div class="flex justify-end space-x-2 pt-4 border-t border-theme-border">
          <button
            @click="isEditingGender = false"
            class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
          >
            Anuluj
          </button>
          <button
            class="px-4 py-1.5 bg-theme-bg-tertiary text-[#BCC0C4] font-semibold rounded-md text-[15px] cursor-not-allowed"
          >
            Zapisz
          </button>
        </div>
      </div>
      <div v-else class="flex justify-between items-center group">
        <div class="flex gap-4">
          <CircleMultipleOutline :size="28" class="text-theme-text" />
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] text-theme-text">{{ genderForm.value }}</span>
            <span class="text-[13px] text-theme-text-secondary">Płeć</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('gender')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            title="Zmień ustawienia prywatności"
          >
            <component :is="getPrivacyIcon(genderPrivacy)" :size="16" class="text-[#BCC0C4]" />
          </button>
          <button
            @click="isEditingGender = true"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">Zaimki</h3>
      <div class="flex justify-between items-center group">
        <div class="flex gap-4">
          <MessageOutline :size="28" class="text-theme-text" />
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] text-theme-text">on/jego</span>
            <span class="text-[13px] text-theme-text-secondary">Zaimki systemowe</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('pronouns')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            title="Zmień ustawienia prywatności"
          >
            <component :is="getPrivacyIcon(pronounsPrivacy)" :size="16" class="text-[#BCC0C4]" />
          </button>
          <button class="p-2 hover:bg-theme-hover rounded-full transition-colors">
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <div class="mb-2">
      <h3 class="font-bold text-[17px] mb-4">Języki</h3>
      <div class="flex items-center gap-4 cursor-pointer group">
        <Translate :size="28" class="text-theme-text-secondary" />
        <span class="text-[15px] font-medium text-theme-text-secondary group-hover:underline">Języki</span>
      </div>
    </div>

    <!-- Modal Prywatności -->
    <BaseModal
      v-if="showPrivacyModal"
      title="Wybierz grupę odbiorców"
      @close="showPrivacyModal = false"
    >
      <PrivacySelector
        :initial-privacy="activePrivacyValue"
        @confirm="handlePrivacyConfirm"
        @back="showPrivacyModal = false"
      />
    </BaseModal>
  </div>
</template>
