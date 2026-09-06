<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupsStore } from '@/stores/groups'
import { useUserCache } from '@/composables/shared/useUserCache'

// Importy ikon
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import TuneIcon from 'vue-material-design-icons/Tune.vue'

// Import niestandardowego Dropdownu
import CustomDropdownButton from '@/components/common/CustomDropdownButton.vue'
import type { DropdownOption } from '@/components/common/CustomDropdownButton.vue'

const route = useRoute()
const groupsStore = useGroupsStore()
const { getOrFetchUser } = useUserCache()

interface PendingUser {
  id: string
  name: string
  avatar: string
}

const pendingUsers = ref<PendingUser[]>([])
const isLoading = ref(true)
const searchQuery = ref('')

// --- Stan aktywnych filtrów (v-model strings) ---
const activeFilters = ref({
  age: null as string | null,
  fbJoinDate: null as string | null,
  gender: null as string | null,
  more: null as string | null,
})

const hasActiveFilters = computed(() => {
  return Object.values(activeFilters.value).some(val => val !== null) || searchQuery.value.length > 0
})

const clearAllFilters = () => {
  activeFilters.value = {
    age: null,
    fbJoinDate: null,
    gender: null,
    more: null
  }
  searchQuery.value = ''
}

// --- Definicje opcji dla filtrów ---
const ageOptions: DropdownOption[] = [
  { label: 'Mniej niż tydzień', value: 'lt_1w' },
  { label: 'Mniej niż miesiąc', value: 'lt_1m' },
  { label: 'Mniej niż 3 miesiące', value: 'lt_3m' },
  { label: 'Ponad 3 miesiące temu', value: 'gt_3m' },
  { label: 'Ponad 6 miesięcy temu', value: 'gt_6m' },
  { label: 'Ponad rok temu', value: 'gt_1y' },
]

const fbJoinDateOptions: DropdownOption[] = [
  { label: 'Mniej niż 3 miesiące temu', value: 'lt_3m' },
  { label: 'Mniej niż 6 miesięcy temu', value: 'lt_6m' },
  { label: 'Ponad rok temu', value: 'gt_1y' },
  { label: 'Ponad 2 lata temu', value: 'gt_2y' },
]

const genderOptions: DropdownOption[] = [
  { label: 'Kobieta', value: 'female' },
  { label: 'Mężczyzna', value: 'male' },
  { label: 'Niestandardowa', value: 'custom' },
  { label: 'Nieznana', value: 'unknown' },
]

const moreFiltersOptions: DropdownOption[] = [
  {
    label: 'Zdjęcie profilowe',
    value: 'profile_pic',
    subOptions: [
      { label: 'Ma zdjęcie profilowe', value: 'has_pic' },
      { label: 'Brak zdjęcia profilowego', value: 'no_pic' },
    ]
  },
  {
    label: 'Zaproszono',
    value: 'invited',
    subOptions: [
      { label: 'Zaproszenie od członka grupy', value: 'invited_member' },
      { label: 'Prośba o dołączenie', value: 'invited_request' },
    ]
  },
  {
    label: 'Znajomi',
    value: 'friends',
    subOptions: [
      { label: 'Znajomi w grupie', value: 'friends_in_group' },
      { label: 'Wspólni znajomi', value: 'friends_mutual' },
    ]
  },
  { label: 'Lokalizacja', value: 'location' },
  { label: 'Typ profilu', value: 'profile_type' },
  { label: 'Grupy', value: 'groups' },
]

// --- Akcje ---
const loadRequests = async () => {
  isLoading.value = true
  const groupId = route.params.id as string
  if (groupId) {
    const userIds: string[] = await groupsStore.getPendingRequests(groupId)
    const users: PendingUser[] = []
    for (const id of userIds) {
      const u = await getOrFetchUser(id)
      users.push({ id: u.id, name: u.name, avatar: u.avatar })
    }
    pendingUsers.value = users
  }
  isLoading.value = false
}

const handleApprove = async (userId: string) => {
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.approveGroupRequest(groupId, userId)
    if (success) {
      pendingUsers.value = pendingUsers.value.filter(u => u.id !== userId)
    }
  }
}

const handleReject = async (userId: string) => {
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.rejectGroupRequest(groupId, userId)
    if (success) {
      pendingUsers.value = pendingUsers.value.filter(u => u.id !== userId)
    }
  }
}

// Filtrowanie listy
const filteredUsers = computed(() => {
  let result = pendingUsers.value

  if (searchQuery.value) {
    result = result.filter(u => u.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
  }

  // Wymuszenie stanu pustego ze zrzutów ekranu, gdy aktywny jest dowolny filtr
  if (activeFilters.value.age || activeFilters.value.fbJoinDate || activeFilters.value.gender || activeFilters.value.more) {
    result = []
  }

  return result
})

onMounted(() => {
  loadRequests()
})
</script>

<template>
  <div class="min-h-screen bg-[#f0f2f5] dark:bg-theme-bg text-[#050505] dark:text-theme-text font-sans flex flex-col relative selection:bg-blue-600">

    <!-- Górna sekcja z filtrami (Header) -->
    <div class="bg-white dark:bg-theme-bg-secondary border-b border-gray-200 dark:border-theme-border px-4 py-4 sm:px-8 shadow-sm z-10 sticky top-0">
      <div class="max-w-6xl mx-auto">

        <!-- Pasek tytułowy -->
        <div class="flex justify-between items-center mb-4">
          <h1 class="text-[20px] font-bold leading-tight flex items-center">{{ $t('groups.prosbyODolaczenie') }}<template v-if="hasActiveFilters && filteredUsers.length === 0">
              <span class="text-[#65676b] dark:text-[#b0b3b8] font-normal ml-1">{{ $t('groups.brakDopasowan') }}</span>
            </template>
            <template v-else-if="!isLoading && pendingUsers.length > 0">
              <span class="text-[#65676b] dark:text-[#b0b3b8] font-normal ml-1">
                · {{ filteredUsers.length }}
              </span>
            </template>
          </h1>

          <button class="bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] transition-colors rounded-lg px-3 py-1.5 flex items-center gap-1.5 text-[#050505] dark:text-[#e4e6eb] cursor-pointer">
            <TuneIcon :size="18" />
            <ChevronDownIcon :size="20" class="-mr-1" />
          </button>
        </div>

        <!-- Wyszukiwarka i sortowanie -->
        <div class="flex flex-col sm:flex-row gap-3 mb-3">
          <div class="relative flex-1">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <MagnifyIcon :size="20" class="text-[#65676b] dark:text-[#b0b3b8]" />
            </div>
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="$t('groups.szukajWedlugImieniaI')"
              class="w-full bg-[#f0f2f5] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] placeholder-[#65676b] dark:placeholder-[#b0b3b8] rounded-full pl-10 pr-4 py-2 text-[15px] focus:outline-none focus:ring-1 focus:ring-[#1877f2] transition-shadow"
            />
          </div>

          <button class="bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] transition-colors rounded-lg px-4 py-2 flex items-center justify-between gap-2 min-w-[180px] text-[#050505] dark:text-[#e4e6eb] cursor-pointer">
            <span class="text-[15px] font-semibold">{{ $t('groups.odNajnowszych') }}</span>
            <ChevronDownIcon :size="20" class="-mr-1" />
          </button>
        </div>

        <!-- Rząd przycisków filtrów -->
        <div class="flex flex-wrap items-center gap-2">

          <button
            @click="clearAllFilters"
            :disabled="!hasActiveFilters"
            :class="[
              'px-3.5 py-1.5 rounded-lg text-[15px] font-semibold transition-colors',
              hasActiveFilters
                ? 'bg-[#1877f2] hover:bg-[#166fe5] text-white shadow-sm cursor-pointer'
                : 'bg-transparent text-[#bcc0c4] dark:text-[#65676b] cursor-not-allowed'
            ]"
          >{{ $t('groups.wyczyscFiltry') }}</button>

          <CustomDropdownButton
            :label="$t('groups.wiekProsby')"
            :options="ageOptions"
            v-model="activeFilters.age"
          />

          <CustomDropdownButton
            :label="$t('groups.dataDolaczeniaDoFacebooka')"
            :options="fbJoinDateOptions"
            v-model="activeFilters.fbJoinDate"
          />

          <CustomDropdownButton
            :label="$t('auth.register.gender')"
            :options="genderOptions"
            v-model="activeFilters.gender"
          />

          <CustomDropdownButton
            :label="$t('groups.wiecejFiltrow')"
            dropdown-title="Więcej filtrów"
            :has-chevron="false"
            :badge-mode="true"
            :options="moreFiltersOptions"
            v-model="activeFilters.more"
          />

        </div>

      </div>
    </div>

    <!-- Spinner ładowania -->
    <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center py-12">
      <div class="w-8 h-8 border-4 border-[#1877f2] border-t-transparent rounded-full animate-spin"></div>
      <p class="mt-4 text-[#65676b] dark:text-[#b0b3b8] text-[15px]">{{ $t('groups.ladowanieProsbODolaczenie') }}</p>
    </div>

    <div v-else class="flex-1 overflow-y-auto">

      <!-- Pusty stan -->
      <div v-if="filteredUsers.length === 0" class="h-full flex flex-col items-center justify-center p-8 mt-12">
        <svg width="120" height="120" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-4">
          <rect x="30" y="30" width="16" height="20" rx="3" fill="#699bf7" />
          <path d="M38 35C38 35 34 42 38 45C42 42 38 35 38 35Z" fill="#e4e6eb" opacity="0.8" />
          <path d="M22 65C22 55 26 50 38 50C50 50 54 55 54 65L54 75L22 75L22 65Z" fill="#b0b3b8" />
          <circle cx="62" cy="48" r="10" fill="#4599ff" />
          <path d="M52 48C52 42 57 38 62 38C67 38 72 42 72 48C72 48 52 48 52 48Z" fill="#b0b3b8" />
          <path d="M42 75C42 62 48 55 62 55C76 55 82 62 82 75L82 75L42 75L42 75Z" fill="#75777a" />
        </svg>

        <h2 class="text-[20px] font-bold text-[#65676b]">{{ $t('groups.brakProsbODolaczenie') }}</h2>
      </div>

      <!-- Lista próśb -->
      <div v-else class="space-y-3 max-w-3xl mx-auto w-full px-4 sm:px-8 mt-6 pb-12">
        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="bg-white dark:bg-theme-bg-secondary rounded-xl p-4 flex items-center justify-between border border-gray-200 dark:border-transparent shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="flex items-center space-x-3">
            <img :src="user.avatar || 'https://i.pravatar.cc/150'" class="w-12 h-12 rounded-full object-cover border border-gray-100 dark:border-[#3e4042]" />
            <div>
              <span class="font-bold text-[16px] text-[#050505] dark:text-theme-text hover:underline cursor-pointer">
                {{ user.name }}
              </span>
              <p class="text-[13px] text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.chceDolaczycDoGrupy') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button
              @click="handleApprove(user.id)"
              class="bg-[#1877f2] hover:bg-[#166fe5] text-white px-5 py-2 rounded-lg text-[15px] font-semibold transition cursor-pointer"
            >{{ $t('groups.zatwierdz') }}</button>
            <button
              @click="handleReject(user.id)"
              class="bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] text-[#050505] dark:text-[#e4e6eb] px-5 py-2 rounded-lg text-[15px] font-semibold transition cursor-pointer"
            >{{ $t('chat.odrzuc') }}</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
