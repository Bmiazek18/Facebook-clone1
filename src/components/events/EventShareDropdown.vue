<template>
  <VDropdown :distance="0" placement="bottom">
    <button class="flex items-center justify-center gap-1.5 bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] border-none rounded-md px-3 h-9 text-[15px] font-semibold cursor-pointer transition-colors">
      <ShareVariantIcon class="w-4 h-4 flex items-center justify-center" />
      {{ notTitle ? 'Udostępni' : '' }}
    </button>

    <template #popper="{ hide }">
      <ul class="flex flex-col p-2 min-w-[320px] m-0 list-none bg-white rounded-xl shadow-lg border border-white/70">
        <li v-for="option in shareOptions" :key="option.id">
          <button
            @click="option.action(event); hide()"
            class="flex items-center gap-3.5 p-2 hover:bg-[#f2f2f2] rounded-lg text-left transition-colors w-full cursor-pointer border-none bg-transparent"
          >
            <component :is="option.icon" class="w-5 h-5 text-[#050505]" />
            <span class="text-[15px] text-[#050505]">{{ option.label }}</span>
          </button>
        </li>
      </ul>
    </template>
  </VDropdown>
</template>

<script setup lang="ts">
import { shallowRef } from 'vue'
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import SquareEditOutlineIcon from 'vue-material-design-icons/SquareEditOutline.vue'
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue'
import AccountGroupOutlineIcon from 'vue-material-design-icons/AccountGroupOutline.vue'
import AccountMultipleOutlineIcon from 'vue-material-design-icons/AccountMultipleOutline.vue'

defineProps({
  event: {
    type: Object,
    required: true,
  },
})

// Akcje z menu "Udostępnij"
const copyLink = (event: any) => console.log('Skopiowano link do wydarzenia:', event.id)
const shareToFeed = (event: any) => console.log('Udostępnianie w aktualnościach:', event.id)
const sendInMessenger = (event: any) => console.log('Wysyłanie w Messengerze:', event.id)
const shareInGroup = (event: any) => console.log('Udostępnianie w grupie:', event.id)
const shareToFriendProfile = (event: any) => console.log('Udostępnianie na profilu znajomego:', event.id)

const shareOptions = shallowRef([
  { id: 'copy_link', label: 'Kopiuj link do wydarzenia', icon: LinkVariantIcon, action: copyLink },
  { id: 'share_feed', label: 'Udostępnij w Aktualnościach', icon: SquareEditOutlineIcon, action: shareToFeed },
  { id: 'send_messenger', label: 'Wyślij w Messengerze', icon: FacebookMessengerIcon, action: sendInMessenger },
  { id: 'share_group', label: 'Udostępnij w grupie', icon: AccountGroupOutlineIcon, action: shareInGroup },
  { id: 'share_friend', label: 'Udostępnij w profilu znajomego', icon: AccountMultipleOutlineIcon, action: shareToFriendProfile },
])
</script>
