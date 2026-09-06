<script setup lang="ts">
import { ref, reactive, inject, computed, watch } from 'vue'
import { usersApi } from '@/api/users'

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

import BaseModal from '@/components/common/BaseModal.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'

const props = defineProps<{
  profileUser: any
}>()

const isOwner = inject('isOwner', ref(false))
const fetchUserProfile = inject<() => Promise<void>>('fetchUserProfile')

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
const isEditingPronouns = ref(false)
const isEditingHometown = ref(false)

// --- FORMULARZE ---
const genderForm = reactive({
  value: props.profileUser?.gender || 'Mężczyzna',
  showOnProfile: true,
})

const locationForm = reactive({
  value: props.profileUser?.location || props.profileUser?.city || '',
})

const pronounsForm = ref(props.profileUser?.pronouns || 'on/jego')
const hometownForm = ref(props.profileUser?.hometown || '')

// Kopia wartości przed edycją (do anulowania)
const initialLocationValue = ref('')
const initialGenderValue = ref('')
const initialPronounsValue = ref('')
const initialHometownValue = ref('')

watch(() => props.profileUser, (newUser) => {
  if (newUser) {
    locationForm.value = newUser.location || newUser.city || ''
    genderForm.value = newUser.gender || 'Mężczyzna'
    pronounsForm.value = newUser.pronouns || 'on/jego'
    hometownForm.value = newUser.hometown || ''
  }
}, { immediate: true })

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
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&addressdetails=1&limit=5&accept-language=pl`

    const response = await fetch(url, {
      headers: {
        'User-Agent': 'VueFacebookCloneApp/1.0',
      },
    })
    const data = await response.json()

    locationSuggestions.value = data.map((item: any) => {
      const city =
        item.address.city ||
        item.address.town ||
        item.address.village ||
        item.address.municipality ||
        item.display_name.split(',')[0]
      const state = item.address.state || ''
      const country = item.address.country || ''

      const title = state ? `${city}, ${state}` : city

      return {
        id: item.place_id,
        title: title,
        country: country,
        meta: item.display_name,
      }
    })
  } catch (error) {
    console.error('Błąd podczas pobierania lokalizacji:', error)
  } finally {
    isLoadingLocations.value = false
  }
}

watch(
  () => locationForm.value,
  (newQuery) => {
    if (!isLocationFocused.value) return
    if (debounceTimeout) clearTimeout(debounceTimeout)
    debounceTimeout = setTimeout(() => {
      fetchLocationsFromAPI(newQuery)
    }, 400)
  },
)

const isLocationChanged = computed(() => {
  return locationForm.value !== initialLocationValue.value
})

const isGenderChanged = computed(() => {
  return genderForm.value !== initialGenderValue.value
})

const isPronounsChanged = computed(() => {
  return pronounsForm.value !== initialPronounsValue.value
})

const isHometownChanged = computed(() => {
  return hometownForm.value !== initialHometownValue.value
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

const saveLocation = async () => {
  if (!isLocationChanged.value) return
  try {
    await usersApi.updateProfile(props.profileUser.id, {
      location: locationForm.value
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditingLocation.value = false
  } catch (e) {
    console.error('Failed to update location:', e)
  }
}

const deleteLocation = async () => {
  try {
    await usersApi.updateProfile(props.profileUser.id, {
      location: ''
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    locationForm.value = ''
    locationSuggestions.value = []
  } catch (e) {
    console.error('Failed to delete location:', e)
  }
}

const selectLocation = (locTitle: string) => {
  locationForm.value = locTitle
  locationSuggestions.value = []
  isLocationFocused.value = false
}

const handleLocationBlur = () => {
  setTimeout(() => {
    isLocationFocused.value = false
  }, 200)
}

const startEditingGender = () => {
  initialGenderValue.value = genderForm.value
  isEditingGender.value = true
}

const saveGender = async () => {
  try {
    await usersApi.updateProfile(props.profileUser.id, {
      gender: genderForm.value
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditingGender.value = false
  } catch (e) {
    console.error('Failed to update gender:', e)
  }
}

const startEditingPronouns = () => {
  initialPronounsValue.value = pronounsForm.value
  isEditingPronouns.value = true
}

const savePronouns = async () => {
  try {
    await usersApi.updateProfile(props.profileUser.id, {
      pronouns: pronounsForm.value
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditingPronouns.value = false
  } catch (e) {
    console.error('Failed to update pronouns:', e)
  }
}

const startEditingHometown = () => {
  initialHometownValue.value = hometownForm.value
  isEditingHometown.value = true
}

const saveHometown = async () => {
  try {
    await usersApi.updateProfile(props.profileUser.id, {
      hometown: hometownForm.value
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditingHometown.value = false
  } catch (e) {
    console.error('Failed to update hometown:', e)
  }
}
</script>

<template>
  <div class="max-w-[850px] mx-auto text-theme-text antialiased">
    <!-- LOKALIZACJA -->
    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">{{ $t('post.location') }}</h3>

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
            >{{ $t('profile.aktualnaMiejscowosc') }}</span>
            <input
              v-model="locationForm.value"
              @focus="isLocationFocused = true"
              @blur="handleLocationBlur"
              :placeholder="$t('profile.wpiszMin3Znaki')"
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
          >{{ $t('profile.nieZnalezionoPasujacychMiejsc') }}</div>
        </div>

        <div class="flex justify-between items-center pt-4 border-t border-theme-border mt-4">
          <button
            @click="deleteLocation"
            type="button"
            class="flex items-center gap-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary px-4 py-1.5 rounded-md font-semibold text-[15px] text-theme-text transition-colors"
          >
            <DeleteOutline :size="20" />{{ $t('notifications_page.delete') }}</button>

          <div class="flex gap-2">
            <button
              @click="cancelEditingLocation"
              type="button"
              class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
            >{{ $t('common.cancel') }}</button>
            <button
              @click="saveLocation"
              :disabled="!isLocationChanged"
              :class="
                isLocationChanged
                  ? 'bg-[#1877F2] hover:bg-[#166FE5] text-white cursor-pointer'
                  : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'
              "
              class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
            >{{ $t('createLive.save') }}</button>
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
            <span class="text-[13px] text-theme-text-secondary">{{ $t('friends.aktualneMiejsceZamieszkania') }}</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('location')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            :title="$t('profile.zmienUstawieniaPrywatnosci')"
          >
            <component :is="getPrivacyIcon(locationPrivacy)" :size="16" class="text-theme-text-secondary" />
          </button>
          <button
            @click="startEditingLocation"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <!-- MIEJSCOWOŚĆ RODZINNA -->
    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">{{ $t('profile.miejscowoscRodzinna') }}</h3>
      <div v-if="isEditingHometown" class="mt-2">
        <input
          v-model="hometownForm"
          :placeholder="$t('profile.wpiszMiejscowoscRodzinna')"
          class="w-full border border-theme-border rounded-md p-2 text-[15px] text-theme-text outline-none focus:border-[#1877F2]"
        />
        <div class="flex justify-end space-x-2 pt-4 border-t border-theme-border mt-4">
          <button
            @click="isEditingHometown = false"
            class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
          >{{ $t('common.cancel') }}</button>
          <button
            @click="saveHometown"
            :disabled="!isHometownChanged"
            class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
            :class="isHometownChanged ? 'bg-[#1877F2] hover:bg-[#166FE5] text-white' : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'"
          >{{ $t('createLive.save') }}</button>
        </div>
      </div>
      <div v-else class="flex justify-between items-center group">
        <div class="flex gap-4">
          <HomeOutline :size="28" class="text-theme-text-secondary" />
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] font-medium text-[#1877F2] hover:underline cursor-pointer">
              {{ hometownForm || 'Dodaj miejscowość rodzinną' }}
            </span>
            <span class="text-[13px] text-theme-text-secondary">{{ $t('profile.miejscowoscRodzinna') }}</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="startEditingHometown"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <!-- PŁEĆ -->
    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">{{ $t('auth.register.gender') }}</h3>
      <div v-if="isEditingGender" class="mt-2">
        <div
          class="relative bg-theme-bg-tertiary hover:bg-theme-bg-tertiary transition-colors rounded-md px-3 py-2.5 flex justify-between items-center cursor-pointer mb-4"
        >
          <select
            v-model="genderForm.value"
            class="w-full bg-transparent outline-none appearance-none cursor-pointer text-[15px] text-theme-text pr-8 font-medium"
          >
            <option value="Mężczyzna">{{ $t('profile.mezczyzna') }}</option>
            <option value="Kobieta">{{ $t('profile.kobieta') }}</option>
            <option value="Inna">{{ $t('profile.inna') }}</option>
          </select>
          <ChevronDown :size="24" class="text-theme-text absolute right-3 pointer-events-none" />
        </div>
        <div class="flex justify-end space-x-2 pt-4 border-t border-theme-border">
          <button
            @click="isEditingGender = false"
            class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
          >{{ $t('common.cancel') }}</button>
          <button
            @click="saveGender"
            :disabled="!isGenderChanged"
            class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
            :class="isGenderChanged ? 'bg-[#1877F2] hover:bg-[#166FE5] text-white' : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'"
          >{{ $t('createLive.save') }}</button>
        </div>
      </div>
      <div v-else class="flex justify-between items-center group">
        <div class="flex gap-4">
          <CircleMultipleOutline :size="28" class="text-theme-text" />
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] text-theme-text">{{ genderForm.value }}</span>
            <span class="text-[13px] text-theme-text-secondary">{{ $t('auth.register.gender') }}</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('gender')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            :title="$t('profile.zmienUstawieniaPrywatnosci')"
          >
            <component :is="getPrivacyIcon(genderPrivacy)" :size="16" class="text-[#BCC0C4]" />
          </button>
          <button
            @click="startEditingGender"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <!-- ZAIMKI -->
    <div class="mb-8">
      <h3 class="font-bold text-[17px] mb-4">{{ $t('profile.info.pronouns') }}</h3>
      <div v-if="isEditingPronouns" class="mt-2">
        <input
          v-model="pronounsForm"
          :placeholder="$t('profile.zaimkiNpOnJego')"
          class="w-full border border-theme-border rounded-md p-2 text-[15px] text-theme-text outline-none focus:border-[#1877F2]"
        />
        <div class="flex justify-end space-x-2 pt-4 border-t border-theme-border mt-4">
          <button
            @click="isEditingPronouns = false"
            class="px-4 py-1.5 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
          >{{ $t('common.cancel') }}</button>
          <button
            @click="savePronouns"
            :disabled="!isPronounsChanged"
            class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
            :class="isPronounsChanged ? 'bg-[#1877F2] hover:bg-[#166FE5] text-white' : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'"
          >{{ $t('createLive.save') }}</button>
        </div>
      </div>
      <div v-else class="flex justify-between items-center group">
        <div class="flex gap-4">
          <MessageOutline :size="28" class="text-theme-text" />
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] text-theme-text">{{ pronounsForm || 'on/jego' }}</span>
            <span class="text-[13px] text-theme-text-secondary">{{ $t('profile.zaimkiSystemowe') }}</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="openPrivacySelector('pronouns')"
            class="p-1.5 hover:bg-theme-bg-tertiary rounded-full transition-colors cursor-pointer"
            :title="$t('profile.zmienUstawieniaPrywatnosci')"
          >
            <component :is="getPrivacyIcon(pronounsPrivacy)" :size="16" class="text-[#BCC0C4]" />
          </button>
          <button
            @click="startEditingPronouns"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Prywatności -->
    <BaseModal
      v-if="showPrivacyModal"
      :title="$t('profile.wybierzGrupeOdbiorcow')"
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
