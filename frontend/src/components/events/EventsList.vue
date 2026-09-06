<template>
  <div
    class="w-full px-6 mx-auto bg-white rounded-2xl shadow-[0_1px_2px_rgba(0,0,0,0.1)] border border-theme-border p-4  "
  >
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-[17px] font-bold text-gray-900 m-0">{{ $t('events.twojeWydarzenia') }}</h2>
      <a href="#" class="text-[#0064d1] no-underline text-[15px] hover:underline">{{ $t('events.seeAll') }}</a>
    </div>

    <ul class="list-none m-0 border border-theme-border rounded-lg">
      <li
        v-for="(event, index) in events"
        :key="event.id"
        class="flex gap-3 pt-2 px-2 cursor-pointer hover:bg-theme-hover rounded-lg transition-colors"
      >
        <div class="shrink-0 pt-1 pb-2">
          <img
            :src="event.image"
            :alt="event.title"
            class="w-[168px] h-[108px] rounded-lg object-cover border border-black/10"
          />
        </div>

        <div
          :class="[
            'grow flex gap-4 pt-2',
            index !== events.length - 1 ? 'pb-2 border-b border-[#ced0d4]' : '',
          ]"
        >
          <div class="grow flex flex-col justify-center">
            <span class="text-[13px] font-semibold text-theme-text mb-0.5">{{ event.date }}</span>
            <h3 v-tooltip="event.title" class="text-[17px] font-semibold text-theme-text">
              {{ event.title }}
            </h3>
            <span class="text-[14px] h-[21px] font-medium text-theme-text-secondary m-0 mb-1">{{
              event.location
            }}</span>

            <div class="flex items-center text-[12px] text-gray-500 gap-1.5 mt-1">
              <template v-if="event.invitedBy">
                <img :src="event.inviterAvatar" :alt="$t('chat.avatar')" class="w-4 h-4 rounded-full" />
                <span class="text-gray-700">{{ $t('events.eventInvitedbyZaprosilCie') }}</span>
              </template>
              <template v-else-if="event.friendAttending">
                <img :src="event.friendAvatar" :alt="$t('chat.avatar')" class="w-4 h-4 rounded-full" />
                <span class="text-gray-700">{{ $t('events.eventFriendattendingWezmieUdzial') }}</span>
              </template>
              <template v-else>
                <span>{{ event.stats }}</span>
              </template>
            </div>
          </div>

          <div class="flex items-start gap-2">
            <button
              @click="toggleInterest(index)"
              :class="[
                'flex items-center justify-center gap-1.5 border-none rounded-md px-3 h-9 text-[15px] font-semibold cursor-pointer transition-colors',
                event.isInterested
                  ? 'bg-[#ebf5ff] hover:bg-[#e1f0ff] text-[#0064d1]'
                  : 'bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505]',
              ]"
            >
              <component
                :is="event.isInterested ? StarIcon : StarOutlineIcon"
                class="w-4 h-4 flex items-center justify-center"
              />
              {{ event.isInterested ? 'Interesuję się' : 'Zainteresowany(a)' }}
              <ChevronDownIcon
                v-if="event.isInterested"
                class="w-3 h-3 ml-1 flex items-center justify-center"
              />
            </button>

            <EventShareDropdown :event="event" />

            <VDropdown :distance="0" placement="bottom-end">
              <button
                class="flex items-center justify-center bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] border-none rounded-md px-2.5 h-9 cursor-pointer transition-colors"
              >
                <DotsHorizontalIcon class="w-5 h-5 flex items-center justify-center" />
              </button>

              <template #popper="{ hide }">
                <ul
                  class="flex flex-col p-2 min-w-[320px] m-0 list-none bg-white rounded-xl shadow-lg border border-gray-100"
                >
                  <li v-for="option in moreOptions" :key="option.id">
                    <button
                      @click="
                        option.action(event);
                        hide();
                      "
                      class="flex items-center gap-3.5 p-2.5 hover:bg-[#f2f2f2] rounded-lg text-left transition-colors w-full cursor-pointer border-none bg-transparent"
                    >
                      <component :is="option.icon" class="w-6 h-6 text-[#050505]" />
                      <span class="text-[15px] font-medium text-[#050505]">{{ option.label }}</span>
                    </button>
                  </li>
                </ul>
              </template>
            </VDropdown>
          </div>
        </div>
      </li>
    </ul>
  </div>
  <BaseModal v-if="isOpen" @close="isOpen = false" :title="'Zaproś osoby'">
    <InviteModal />
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, shallowRef } from 'vue'

import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

// Ikony główne
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue'
import StarIcon from 'vue-material-design-icons/Star.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import EventShareDropdown from './EventShareDropdown.vue'

// Ikony do Dropdowna "3 kropki" (Więcej opcji)
import EmailOutlineIcon from 'vue-material-design-icons/EmailOutline.vue'
import ShareOutlineIcon from 'vue-material-design-icons/ShareOutline.vue'
import BookmarkOutlineIcon from 'vue-material-design-icons/BookmarkOutline.vue'
import CogOutlineIcon from 'vue-material-design-icons/CogOutline.vue'
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue'
import TrayArrowDownIcon from 'vue-material-design-icons/TrayArrowDown.vue'
import AlertBoxOutlineIcon from 'vue-material-design-icons/AlertBoxOutline.vue'
import BaseModal from '../common/BaseModal.vue'
import InviteModal from './InviteModal.vue'

// ==========================================
// DEDYKOWANE FUNKCJE DLA AKCJI W DROPDOWNACH
// ==========================================
const isOpen = ref(false)
// Akcje z menu "Więcej opcji (3 kropki)"
const inviteToEvent = (event: any) => {
  console.log('Zapraszanie do wydarzenia:', event.id)
  isOpen.value = true
}
const shareEvent = (event: any) => console.log('Udostępnianie wydarzenia:', event.id)
const saveEvent = (event: any) => console.log('Zapisywanie wydarzenia:', event.id)
const notificationSettings = (event: any) => console.log('Ustawienia powiadomień dla:', event.id)
const unfollowEvent = (event: any) => console.log('Przestano obserwować:', event.id)
const addToCalendar = (event: any) => console.log('Dodawanie do kalendarza:', event.id)
const reportEvent = (event: any) => console.log('Zgłaszanie wydarzenia:', event.id)

// ==========================================
// KONFIGURACJA LIST ROZWIJANYCH
// ==========================================

// Lista opcji dla przycisku "Więcej (3 kropki)"
const moreOptions = shallowRef([
  { id: 'invite', label: 'Zaproś', icon: EmailOutlineIcon, action: inviteToEvent },
  { id: 'share', label: 'Udostępnij', icon: ShareOutlineIcon, action: shareEvent },
  { id: 'save', label: 'Zapisz', icon: BookmarkOutlineIcon, action: saveEvent },
  {
    id: 'settings',
    label: 'Ustawienia powiadomień',
    icon: CogOutlineIcon,
    action: notificationSettings,
  },
  {
    id: 'unfollow',
    label: 'Przestań obserwować wydarzenie',
    icon: CloseBoxOutlineIcon,
    action: unfollowEvent,
  },
  {
    id: 'add_calendar',
    label: 'Dodaj do kalendarza',
    icon: TrayArrowDownIcon,
    action: addToCalendar,
  },
  { id: 'report', label: 'Zgłoś wydarzenie', icon: AlertBoxOutlineIcon, action: reportEvent },
])

// ==========================================
// DANE
// ==========================================

const events = ref([
  {
    id: 1,
    image: 'https://picsum.photos/seed/moon/120/120',
    date: 'Pt, 20 mar o 18:00',
    title: 'Ekstremalna Droga Krzyżowa Łuków - Wola Gułowska 2026',
    location: 'parafia Podwyższenia Krzyża Świętego w Łukowie',
    invitedBy: 'Łukasz Błańczak',
    inviterAvatar: 'https://i.pravatar.cc/150?u=lukasz',
    friendAttending: null,
    friendAvatar: null,
    stats: null,
    isInterested: false,
  },
  {
    id: 2,
    image: 'https://picsum.photos/seed/tech/120/120',
    date: 'Sob, 16 maj o 15:00',
    title: 'Technikalia.26',
    location: 'Camper Park Politechniki Gdańskiej, ul. Towarowa 40, 80-218 Gdańsk',
    invitedBy: null,
    inviterAvatar: null,
    friendAttending: 'Bartek',
    friendAvatar: 'https://i.pravatar.cc/150?u=bartek',
    stats: null,
    isInterested: true,
  },
  {
    id: 3,
    image: 'https://picsum.photos/seed/juwenalia/120/120',
    date: 'Pt, 28 maj – 31 maj',
    title: 'JUWENALIA TRÓJMIASTA 2026 ☆ FESTIWAL ☆ 29-31 MAJ ☆',
    location: null,
    invitedBy: null,
    inviterAvatar: null,
    friendAttending: null,
    friendAvatar: null,
    stats: '85 osób zainteresowanych · 21 osób weźmie udział',
    isInterested: true,
  },
])

const toggleInterest = (index: number) => {
  events.value[index].isInterested = !events.value[index].isInterested
}
</script>
