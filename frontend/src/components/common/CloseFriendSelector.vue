<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import { storeToRefs } from 'pinia'

import CloseIcon from 'vue-material-design-icons/Close.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

import { getAllUsers } from '@/utils/users'
import type { User } from '@/utils/users'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'

const emit = defineEmits<{
  (e: 'back'): void
}>()

const createPostStore = useCreatePostStore()
const { closeFriends } = storeToRefs(createPostStore)

const allUsers = ref<User[]>(getAllUsers())
const selectedUsers = ref<User[]>([...closeFriends.value])
const searchQuery = ref('')
const isInputFocused = ref(false) // <-- NOWY STAN DO ŚLEDZENIA FOCUSU

// Scrollbar karuzeli
const carouselRef = ref<HTMLElement | null>(null)
const showLeftArrow = ref(false)
const showRightArrow = ref(false)

const isSelected = (user: User) => selectedUsers.value.some((u) => u.id === user.id)

const filteredUsers = computed(() => {
  if (!searchQuery.value) return allUsers.value
  return allUsers.value.filter((u) =>
    u.name.toLowerCase().includes(searchQuery.value.toLowerCase()),
  )
})

const toggleUser = (user: User) => {
  if (isSelected(user)) {
    selectedUsers.value = selectedUsers.value.filter((u) => u.id !== user.id)
  } else {
    selectedUsers.value.push(user)
  }
}

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter((u) => u.id !== user.id)
}

const removeAll = () => {
  selectedUsers.value = []
}

// LOGIKA STRZAŁEK
const checkScroll = () => {
  if (!carouselRef.value) return
  const { scrollLeft, scrollWidth, clientWidth } = carouselRef.value

  showLeftArrow.value = scrollLeft > 10
  showRightArrow.value = scrollLeft < scrollWidth - clientWidth - 10
}

const scrollCarousel = (direction: 'left' | 'right') => {
  if (!carouselRef.value) return
  const scrollAmount = 220
  carouselRef.value.scrollBy({
    left: direction === 'left' ? -scrollAmount : scrollAmount,
    behavior: 'smooth',
  })
}

onMounted(() => checkScroll())
watch(selectedUsers, () => nextTick(() => checkScroll()), { deep: true })

const saveSelection = () => {
  createPostStore.setCloseFriends(selectedUsers.value)
  emit('back')
}
</script>

<template>
  <div class="flex flex-col h-full w-full select-none bg-white   text-[#050505]">
    <!-- SEKCJA INFORMACYJNA I WYSZUKIWARKA -->
    <div class="px-4 pt-3 pb-3 border-b border-gray-200">
      <p class="text-[#65676b] text-[14px] mb-3 leading-snug">{{ $t('common.zmianyDotyczaWszystkichMaterialow') }}<a href="#" class="text-[#1877f2] font-semibold hover:underline cursor-pointer"
          >{{ $t('auth.register.learnMore') }}</a
        >
      </p>

      <div class="relative flex items-center">
        <div class="absolute left-3.5 flex items-center pointer-events-none text-[#65676b]">
          <MagnifyIcon :size="20" />
        </div>
        <input
          v-model="searchQuery"
          @focus="isInputFocused = true"
          @blur="isInputFocused = false"
          type="text"
          :placeholder="$t('common.szukajZnajomych')"
          class="w-full bg-[#f0f2f5] text-[#050505] placeholder-[#65676b] rounded-full py-2 pl-10 pr-4 focus:outline-none text-[15px]"
        />
      </div>
    </div>

    <!-- GŁÓWNY KONTENER PRZEWIJANY -->
    <HoverScrollbar class="flex-1 overflow-y-auto fb-custom-scrollbar">
      <!-- POZIOMA KARUZELA WYBRANYCH ZNAJOMYCH (Znika gdy focus na input) -->
      <div
        v-if="selectedUsers.length > 0 && !isInputFocused"
        class="px-4 py-4 border-b border-gray-200"
      >
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-[17px] font-bold text-[#050505]">{{ $t('common.proponowaniZnajomiSelectedusersLength') }}</h3>
          <button
            type="button"
            @click="removeAll"
            class="text-[15px] text-[#1877f2] hover:underline font-normal cursor-pointer"
          >{{ $t('common.usunWszystkich') }}</button>
        </div>

        <div class="relative group">
          <!-- Lewa strzałka -->
          <button
            v-show="showLeftArrow"
            @click="scrollCarousel('left')"
            type="button"
            class="absolute left-0 top-7 -translate-y-1/2 -translate-x-1.5 z-10 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-[0_1px_8px_rgba(0,0,0,0.2)] border border-gray-100 text-[#050505] hover:bg-gray-50 transition-all cursor-pointer active:scale-95"
          >
            <ChevronLeftIcon :size="22" />
          </button>

          <!-- Przewijana lista -->
          <div
            ref="carouselRef"
            @scroll="checkScroll"
            class="flex gap-3 overflow-x-auto pb-2 pt-1 fb-horizontal-scrollbar scroll-smooth"
          >
            <div
              v-for="user in selectedUsers"
              :key="user.id"
              class="flex flex-col items-center w-[72px] shrink-0"
            >
              <div class="relative mb-1.5">
                <img
                  :src="user.avatar"
                  :alt="user.name"
                  class="w-14 h-14 rounded-full object-cover border border-gray-200 shadow-sm shrink-0"
                />
                <button
                  type="button"
                  @click="removeUser(user)"
                  class="absolute -top-1 -right-1 w-5 h-5 bg-white rounded-full flex items-center justify-center shadow-md border border-gray-200 text-gray-700 hover:bg-gray-100 transition-colors cursor-pointer"
                >
                  <CloseIcon :size="12" />
                </button>
              </div>
              <span
                class="text-[12px] font-medium text-[#050505] text-center leading-tight line-clamp-2 px-0.5"
              >
                {{ user.name }}
              </span>
            </div>
          </div>

          <!-- Prawa strzałka -->
          <button
            v-show="showRightArrow"
            @click="scrollCarousel('right')"
            type="button"
            class="absolute right-0 top-7 -translate-y-1/2 translate-x-1.5 z-10 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-[0_1px_8px_rgba(0,0,0,0.2)] border border-gray-100 text-[#050505] hover:bg-gray-50 transition-all cursor-pointer active:scale-95"
          >
            <ChevronRightIcon :size="22" />
          </button>
        </div>
      </div>

      <!-- PIONOWA LISTA WSZYSTKICH ZNAJOMYCH -->
      <div class="px-2 py-4">
        <h3 class="text-[17px] font-bold text-[#050505] px-2 mb-2">{{ $t('friends.allFriends') }}</h3>
        <div class="flex flex-col">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            @click="toggleUser(user)"
            class="flex items-center px-2 py-2.5 rounded-lg cursor-pointer hover:bg-black/5 active:bg-black/10 transition-colors"
          >
            <img
              :src="user.avatar"
              :alt="user.name"
              class="w-11 h-11 rounded-full object-cover mr-3.5 border border-gray-100 shrink-0"
            />

            <span class="flex-1 text-[16px] text-[#050505] font-normal tracking-tight">
              {{ user.name }}
            </span>

            <div
              class="w-6 h-6 rounded-[6px] flex items-center justify-center transition-all duration-150 ml-2 shrink-0"
              :class="
                isSelected(user)
                  ? 'bg-[#1877f2] border-none text-white shadow-sm'
                  : 'border-[2px] border-[#bcc0c4] bg-white'
              "
            >
              <CheckIcon v-if="isSelected(user)" :size="16" />
            </div>
          </div>
        </div>
      </div>
    </HoverScrollbar>

    <!-- STOPKA -->
    <div class="p-2 border-t border-gray-200 bg-white shrink-0">
      <button
        type="button"
        class="w-full py-2.5 bg-[#1877f2] hover:bg-[#166fe5] active:scale-[0.99] text-white font-semibold text-[15px] rounded-lg transition-all shadow-sm flex items-center justify-center cursor-pointer"
        @click="saveSelection"
      >{{ $t('createLive.save') }}</button>
    </div>
  </div>
</template>

<style scoped>
.fb-custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.fb-custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.fb-custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.fb-custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8a8d91;
}

.fb-horizontal-scrollbar::-webkit-scrollbar {
  height: 0px;
}
</style>
