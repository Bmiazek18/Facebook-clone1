<template>
  <div
    class="w-[900px] max-w-full bg-white rounded-lg shadow-lg flex flex-col h-[85vh] overflow-hidden border border-gray-200  "
  >
    <div class="p-3 px-4 border-b border-gray-200 bg-white">
      <div class="flex items-center bg-[#f0f2f5] rounded-full px-3 py-2">
        <MagnifyIcon class="text-gray-500 mr-2" :size="20" />
        <input
          type="text"
          placeholder="Wyszukaj osoby..."
          v-model="searchQuery"
          class="bg-transparent border-none outline-none text-[15px] w-full text-black"
        />
      </div>
    </div>

    <div class="flex flex-1 overflow-hidden">
      <div class="w-1/3 border-r border-gray-200 overflow-y-auto pb-5 custom-scrollbar">
        <div class="border-b border-gray-200">
          <div
            @click="isFriendsOpen = !isFriendsOpen"
            class="flex items-center p-3 px-4 font-semibold text-sm text-black cursor-pointer select-none hover:bg-gray-50 transition-colors"
          >
            <AccountMultipleIcon class="mr-3 text-black" :size="20" />
            <span>Znajomi z Facebooka</span>
            <ChevronDownIcon
              class="ml-auto text-gray-500 transition-transform duration-200"
              :class="{ 'rotate-180': !isFriendsOpen }"
              :size="20"
            />
          </div>
          <div v-show="isFriendsOpen" class="px-2 pb-2">
            <div
              class="px-9 py-2 rounded-md text-sm cursor-pointer mb-0.5 bg-[#e7f3ff] text-[#1877f2] font-medium"
            >
              Proponowani
            </div>
            <div
              class="px-9 py-2 rounded-md text-sm text-gray-600 cursor-pointer mb-0.5 hover:bg-gray-100"
            >
              Wszyscy znajomi
            </div>
          </div>
        </div>

        <div class="border-b border-gray-200">
          <div
            @click="isEventsOpen = !isEventsOpen"
            class="flex items-center p-3 px-4 font-semibold text-sm text-black cursor-pointer select-none hover:bg-gray-50 transition-colors"
          >
            <CalendarIcon class="mr-3 text-black shrink-0" :size="20" />
            <span class="leading-tight">Wydarzenia, w których wziąłeś udział</span>
            <ChevronDownIcon
              class="ml-auto text-gray-500 transition-transform duration-200 shrink-0"
              :class="{ 'rotate-180': !isEventsOpen }"
              :size="20"
            />
          </div>
          <div v-show="isEventsOpen" class="px-2 pb-2">
            <div
              class="px-9 py-2 rounded-md text-sm text-gray-600 cursor-pointer mb-0.5 hover:bg-gray-100 leading-snug"
            >
              Wielkie Otrzęsiny Studenckie
            </div>
          </div>
        </div>

        <div class="border-b border-gray-200">
          <div
            @click="isMyEventsOpen = !isMyEventsOpen"
            class="flex items-center p-3 px-4 font-semibold text-sm text-black cursor-pointer select-none hover:bg-gray-50 transition-colors"
          >
            <CalendarStarIcon class="mr-3 text-black shrink-0" :size="20" />
            <span class="leading-tight">Zorganizowane przez Ciebie wydarzenia</span>
            <ChevronDownIcon
              class="ml-auto text-gray-500 transition-transform duration-200 shrink-0"
              :class="{ 'rotate-180': !isMyEventsOpen }"
              :size="20"
            />
          </div>
          <div v-show="isMyEventsOpen" class="px-2 pb-2">
            <div class="px-9 py-2 rounded-md text-sm text-gray-500 mb-0.5 cursor-default">
              Nie znaleziono wydarzeń.
            </div>
          </div>
        </div>

        <div>
          <div
            @click="isGroupsOpen = !isGroupsOpen"
            class="flex items-center p-3 px-4 font-semibold text-sm text-black cursor-pointer select-none hover:bg-gray-50 transition-colors"
          >
            <AccountGroupIcon class="mr-3 text-black shrink-0" :size="20" />
            <span>Twoje grupy</span>
            <ChevronDownIcon
              class="ml-auto text-gray-500 transition-transform duration-200 shrink-0"
              :class="{ 'rotate-180': !isGroupsOpen }"
              :size="20"
            />
          </div>
          <div v-show="isGroupsOpen" class="px-2 pb-2">
            <div
              class="px-9 py-2 rounded-md text-sm text-gray-600 cursor-pointer mb-0.5 hover:bg-gray-100"
            >
              Kolegium Sędziów BOZPN
            </div>
          </div>
        </div>
      </div>

      <div class="w-1/3 flex flex-col border-r border-gray-200">
        <div class="flex justify-between items-center p-4">
          <h3 class="m-0 text-base font-semibold text-black">Proponowani</h3>
          <button
            class="bg-transparent border-none text-[#1877f2] font-semibold text-sm cursor-pointer px-2 py-1 rounded hover:bg-gray-100 transition-colors"
            @click="toggleSelectAll"
          >
            {{ isAllSelected ? 'Odznacz wszystkich' : 'Wybierz wszystkich' }}
          </button>
        </div>

        <div class="flex-1 overflow-y-auto px-2 pb-2 custom-scrollbar">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            class="flex items-center p-2 rounded-md cursor-pointer hover:bg-gray-100 transition-colors"
            @click="toggleUser(user.id)"
          >
            <div
              class="w-10 h-10 rounded-full overflow-hidden mr-3 shrink-0 bg-[#e4e6eb] flex items-center justify-center"
            >
              <img
                v-if="user.avatar"
                :src="user.avatar"
                alt="Avatar"
                class="w-full h-full object-cover"
              />
              <AccountIcon v-else class="text-gray-400" :size="28" />
            </div>
            <div
              class="flex-1 text-[15px] truncate pr-2 text-black"
              :class="selectedIds.includes(user.id) ? 'font-semibold' : 'font-medium'"
            >
              {{ user.name }}
            </div>

            <div
              class="w-5 h-5 shrink-0 rounded-full border flex items-center justify-center transition-colors"
              :class="
                selectedIds.includes(user.id) ? 'bg-[#1877f2] border-[#1877f2]' : 'border-gray-400'
              "
            >
              <CheckIcon v-if="selectedIds.includes(user.id)" class="text-white" :size="14" />
            </div>
          </div>
        </div>
      </div>

      <div class="w-1/3 bg-[#f0f2f5] flex flex-col">
        <div class="p-4 pb-2">
          <p class="text-xs font-semibold text-gray-500 m-0 tracking-wide uppercase">
            POZOSTAŁO {{ 500 - selectedIds.length }} ZAPROSZEŃ
          </p>
        </div>

        <div class="flex-1 overflow-y-auto px-2 custom-scrollbar">
          <div
            v-for="user in selectedUsers"
            :key="'selected-' + user.id"
            class="flex items-center p-2 rounded-md hover:bg-gray-200 transition-colors group"
          >
            <div
              class="w-8 h-8 rounded-full overflow-hidden mr-3 shrink-0 bg-[#e4e6eb] flex items-center justify-center"
            >
              <img
                v-if="user.avatar"
                :src="user.avatar"
                alt="Avatar"
                class="w-full h-full object-cover"
              />
              <AccountIcon v-else class="text-gray-400" :size="20" />
            </div>

            <div class="flex-1 text-[14px] font-medium text-black truncate pr-2">
              {{ user.name }}
            </div>

            <button
              @click="toggleUser(user.id)"
              class="w-6 h-6 flex items-center justify-center rounded-full text-gray-500 hover:bg-gray-300 transition-colors cursor-pointer border-none bg-transparent"
            >
              <CloseIcon :size="16" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="p-4 border-t border-gray-200 flex justify-end gap-3 bg-white">
      <button
        class="bg-transparent border-none text-[#1877f2] font-semibold text-[15px] cursor-pointer px-3 py-2 rounded-md hover:bg-gray-100 transition-colors"
      >
        Anuluj
      </button>
      <button
        class="bg-[#1877f2] text-white border-none font-semibold text-[15px] px-6 py-2 rounded-md cursor-pointer disabled:bg-[#e4e6eb] disabled:text-[#bcc0c4] disabled:cursor-not-allowed transition-colors"
        :disabled="selectedIds.length === 0"
      >
        Wyślij zaproszenia
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// Import ikon
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue'
import CalendarIcon from 'vue-material-design-icons/Calendar.vue'
import CalendarStarIcon from 'vue-material-design-icons/CalendarStar.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import AccountIcon from 'vue-material-design-icons/Account.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue' // <-- Nowa ikona

// Stan akordeonu (według drugiego screena tylko pierwsza zakładka jest domyślnie rozwinięta)
const isFriendsOpen = ref(true)
const isEventsOpen = ref(false)
const isMyEventsOpen = ref(false)
const isGroupsOpen = ref(false)

const searchQuery = ref('')

const users = ref([
  { id: 1, name: 'Przemek Krasucki', avatar: 'https://i.pravatar.cc/150?u=1' },
  { id: 2, name: 'Mikołaj Niedziela', avatar: 'https://i.pravatar.cc/150?u=2' },
  { id: 3, name: 'Mateusz Piszcz', avatar: 'https://i.pravatar.cc/150?u=3' },
  { id: 4, name: 'Hubert Sujka', avatar: 'https://i.pravatar.cc/150?u=4' },
  { id: 5, name: 'Magda Chłopecka', avatar: 'https://i.pravatar.cc/150?u=5' },
  { id: 6, name: 'Wojtek Piotrowski', avatar: 'https://i.pravatar.cc/150?u=6' },
  { id: 7, name: 'Bartek Łada', avatar: null },
  { id: 8, name: 'Jakub Grabowski', avatar: 'https://i.pravatar.cc/150?u=8' },
  { id: 9, name: 'Nikodem Majzner', avatar: 'https://i.pravatar.cc/150?u=9' },
  { id: 10, name: 'Zuzanna Jóźwik', avatar: null },
  { id: 11, name: 'Jacek Ejsmont', avatar: 'https://i.pravatar.cc/150?u=11' },
  { id: 12, name: 'Weronika Salamończyk', avatar: 'https://i.pravatar.cc/150?u=12' },
  { id: 13, name: 'Bartłomiej Gawroński', avatar: null },
  { id: 14, name: 'Maks Muranowski', avatar: 'https://i.pravatar.cc/150?u=14' },
  { id: 15, name: 'Szymon Trochowski', avatar: 'https://i.pravatar.cc/150?u=15' },
])

const selectedIds = ref<number[]>([])

// Filtrowanie z lewej strony
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  return users.value.filter((u) => u.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

// Zmienna obliczeniowa pobierająca pełne obiekty zaznaczonych użytkowników do prawego panelu
const selectedUsers = computed(() => {
  return users.value.filter((u) => selectedIds.value.includes(u.id))
})

const isAllSelected = computed(() => {
  return (
    filteredUsers.value.length > 0 &&
    filteredUsers.value.every((u) => selectedIds.value.includes(u.id))
  )
})

const toggleUser = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    const visibleIds = filteredUsers.value.map((u) => u.id)
    selectedIds.value = selectedIds.value.filter((id) => !visibleIds.includes(id))
  } else {
    filteredUsers.value.forEach((u) => {
      if (!selectedIds.value.includes(u.id)) {
        selectedIds.value.push(u.id)
      }
    })
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a0a3a8;
}
</style>
