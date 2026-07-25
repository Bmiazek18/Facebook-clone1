<script setup lang="ts">
import FriendListItem from '@/components/friends/FriendListItem.vue'
import SearchInput from '@/components/common/SearchInput.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'
import { ref, inject } from 'vue'

const searchQuery = ref('')
const showFriendsModal = ref(false)
const isOwner = inject('isOwner', false)

const settings = ref([
  {
    title: 'Lista znajomych',
    subtitle: 'Kto może zobaczyć Twoją listę znajomych',
    description:
      'Pamiętaj, że Twoi znajomi kontrolują widoczność swoich znajomości na własnych osiach czasu. Jeżeli użytkownicy zobaczą Twoją znajomość na innej osi czasu, będą mogli zobaczyć ją w Aktualnościach, wynikach wyszukiwania i innych miejscach na Facebooku. Jeśli wybierzesz ustawienie „Tylko ja”, tylko Ty będziesz mieć możliwość zobaczenia pełnej listy znajomych na swojej osi czasu. Inne osoby będą widzieć wyłącznie wspólnych znajomych.',
    privacyType: 'friends',
    privacyLabel: 'Znajomi',
  },
  {
    title: 'Obserwowanie',
    subtitle: 'Kto może zobaczyć osoby i strony, które obserwujesz?',
    description: 'Pamiętaj! Obserwowane przez Ciebie osoby mogą zobaczyć, że je obserwujesz.',
    privacyType: 'public',
    privacyLabel: 'Wszyscy',
  },
  {
    title: 'Obserwujący',
    subtitle: 'Kto może zobaczyć osoby Cię obserwujące na Twojej osi czasu?',
    description: '',
    privacyType: 'friends',
    privacyLabel: 'Włączone',
  },
])

const selectedSetting = ref<any>(null)

const handlePrivacyClick = (item: any) => {
  selectedSetting.value = item
}

const handleBack = () => {
  selectedSetting.value = null
}

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  if (selectedSetting.value) {
    selectedSetting.value.privacyType = payload.id
    selectedSetting.value = null
  }
}

const privacyOptions = ref([
  { label: 'Wszyscy', value: 'public' },
  { label: 'Znajomi', value: 'friends' },
  { label: 'Tylko ja', value: 'private' },
])

defineProps<{
  friendsList: {
    name: string
    mutual: number
    isFriend: boolean
    imageId: number
  }[]
  isFullView: boolean // false = Info Tab Preview, true = Full Friends Tab
}>()
</script>

<template>
  <div
    class="bg-theme-bg-secondary p-4 mt-4 rounded-lg shadow-lg"
    :class="{ 'shadow-lg border border-theme-border': !isFullView }"
  >
    <div class="flex justify-between font-semibold items-center mb-4 pb-3">
      <h2 class="text-theme-text text-xl">Znajomi</h2>

      <!-- Dodatkowe elementy nagłówka dla FullView -->
      <div v-if="isFullView" class="flex items-center space-x-4">
        <div class="relative w-48">
          <SearchInput v-model="searchQuery" placeholder="Szukaj" />
        </div>
        <template v-if="isOwner && isFullView">
          <a class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
            >Zaproszenia do grona znajomych</a
          >
          <a class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
            >Szukaj znajomych</a
          >

          <button
            @click="showFriendsModal = true"
            class="bg-theme-bg-subtle hover:bg-theme-hover px-3 py-1.5 rounded-lg text-theme-text font-bold"
          >
            ...
          </button>
        </template>
      </div>

      <a
        v-if="!isFullView"
        class="text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer"
        >Wszyscy znajomi</a
      >
    </div>

    <div
      class="flex space-x-6 mb-4 text-theme-text-secondary border-b border-theme-border overflow-x-auto whitespace-nowrap"
    >
      <div
        class="flex space-x-6 mb-4 text-theme-text-secondary border-b border-theme-border overflow-x-auto whitespace-nowrap"
      >
        <!-- Wszyscy znajomi (np. główny widok znajomych lub podstrona) -->
        <NuxtLink
          to="friends_all"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Wszyscy znajomi
        </NuxtLink>

        <!-- Niedawno dodani -->
        <NuxtLink
          v-if="isFullView"
          to="friends_recent"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Niedawno dodani
        </NuxtLink>

        <!-- Urodziny -->
        <NuxtLink
          to="friends_birthdays"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Urodziny
        </NuxtLink>

        <!-- Szkoła średnia -->
        <NuxtLink
          to="friends_high_school"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Szkoła średnia
        </NuxtLink>

        <!-- Aktualne miejsce zamieszkania -->
        <NuxtLink
          to="friends_current_city"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Aktualne miejsce zamieszkania
        </NuxtLink>

        <!-- Obserwowani (kieruje do /settings/following) -->
        <NuxtLink
          to="following"
          class="pb-3 text-[15px] cursor-pointer transition-all"
          active-class="border-b-3 border-b-theme-primary text-theme-primary font-medium"
          inactive-class="hover:border-b-3 hover:border-b-theme-border"
        >
          Obserwowani
        </NuxtLink>
      </div>
    </div>

    <!-- Nienaruszony, oryginalny kontener listy -->
    <div class="flex flex-wrap -mx-2 mt-4">
      <FriendListItem v-for="(friend, index) in friendsList" :key="index" :friend="friend" />
    </div>

    <button
      v-if="!isFullView"
      class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong rounded-lg p-2 font-bold mt-4 text-theme-text"
    >
      Zobacz wszystko
    </button>

    <BaseModal
      v-if="showFriendsModal"
      title="Edytuj ustawienia prywatności"
      @close="showFriendsModal = false"
      :back="!!selectedSetting"
      @back="handleBack"
    >
      <!-- Widok listy ustawień -->
      <div
        v-if="!selectedSetting"
        class="max-w-4xl mx-auto p-6 bg-theme-bg-secondary   antialiased"
      >
        <div class="divide-y divide-theme-border">
          <div
            v-for="(item, index) in settings"
            :key="index"
            class="flex flex-row justify-between items-start gap-6 py-6 first:pt-0 last:pb-0"
          >
            <div class="flex-1 space-y-1 pr-4">
              <h2 class="text-[20px] font-bold text-theme-text tracking-tight">
                {{ item.title }}
              </h2>
              <h3 class="text-[15px] font-normal text-theme-text leading-snug">
                {{ item.subtitle }}
              </h3>
              <p
                v-if="item.description"
                class="text-[14px] text-theme-text-secondary leading-relaxed pt-1"
              >
                {{ item.description }}
              </p>
            </div>

            <button
              @click="handlePrivacyClick(item)"
              class="flex items-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover active:scale-95 transition-all text-theme-text font-bold text-[15px] px-4 py-2 rounded-xl shrink-0 shadow-sm"
            >
              <span>{{ item.privacyLabel }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Widok selectora prywatności -->
      <div v-else class="max-w-2xl mx-auto p-6 bg-theme-bg-secondary   antialiased">
        <PrivacySelector
          :initial-privacy="selectedSetting.privacyType"
          @confirm="handlePrivacyConfirm"
          @back="handleBack"
        />
      </div>
    </BaseModal>
  </div>
</template>
