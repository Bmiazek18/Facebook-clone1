<template>
  <div class="w-[360px] bg-theme-bg-secondary text-theme-text flex flex-col max-h-[80vh] overflow-y-auto rounded-lg shadow-xl">

    <div class="p-4 pb-2 border-b border-theme-border">
      <h2 class="text-lg font-semibold leading-tight">Ustawienia czatu</h2>
      <p class="text-[13px] text-theme-text-secondary mt-0.5">Dostosuj interfejs Messengera.</p>
    </div>

    <div class="py-1 border-b border-theme-border">

      <div class="w-full flex items-center justify-between px-4 py-2 hover:bg-theme-hover transition-colors">
        <div class="flex items-center gap-3">
          <PhoneRingIcon class="h-5 w-5 text-theme-text" />
          <span class="text-[14px] font-medium leading-tight">Dźwięki połączenia<br/>przychodzącego</span>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="incomingCallsSounds"
          @click="incomingCallsSounds = !incomingCallsSounds"
          :class="[
            'w-10 h-5 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none',
            incomingCallsSounds ? 'bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'
          ]"
        >
          <div
            :class="[
              'absolute top-[2px] w-4 h-4 bg-white rounded-full shadow-sm transition-transform duration-200',
              incomingCallsSounds ? 'right-[2px]' : 'left-[2px]'
            ]"
          ></div>
        </button>
      </div>

      <div class="w-full flex items-center justify-between px-4 py-2 hover:bg-theme-hover transition-colors">
        <div class="flex items-center gap-3">
          <VolumeHighIcon class="h-5 w-5 text-theme-text" />
          <span class="text-[14px] font-medium">Dźwięki wiadomości</span>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="messageSounds"
          @click="messageSounds = !messageSounds"
          :class="[
            'w-10 h-5 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none',
            messageSounds ? 'bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'
          ]"
        >
          <div
            :class="[
              'absolute top-[2px] w-4 h-4 bg-white rounded-full shadow-sm transition-transform duration-200',
              messageSounds ? 'right-[2px]' : 'left-[2px]'
            ]"
          ></div>
        </button>
      </div>

      <div class="w-full flex items-center justify-between px-4 py-2 hover:bg-theme-hover transition-colors">
        <div class="flex items-center gap-3 pr-4">
          <ChatAlertIcon class="h-5 w-5 text-theme-text shrink-0 self-start mt-0.5" />
          <div class="flex flex-col">
            <span class="text-[14px] font-medium leading-tight">Wyświetlaj nowe wiadomości w oknie podręcznym</span>
            <span class="text-[12px] text-theme-text-secondary mt-0.5">Automatycznie otwieraj nowe wiadomości.</span>
          </div>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="popupMessages"
          @click="popupMessages = !popupMessages"
          :class="[
            'w-10 h-5 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none',
            popupMessages ? 'bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'
          ]"
        >
          <div
            :class="[
              'absolute top-[2px] w-4 h-4 bg-white rounded-full shadow-sm transition-transform duration-200',
              popupMessages ? 'right-[2px]' : 'left-[2px]'
            ]"
          ></div>
        </button>
      </div>
    </div>

    <div class="py-0.5 border-b border-theme-border">
      <button type="button" class="w-full flex items-center justify-between px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <div class="flex items-center gap-3">
          <ShieldOutlineIcon class="h-5 w-5 text-theme-text" />
          <span class="text-[14px] font-medium">Prywatność i bezpieczeństwo</span>
        </div>
        <ChevronRightIcon class="h-5 w-5 text-theme-text-secondary" />
      </button>
    </div>

    <div class="py-1 border-b border-theme-border flex flex-col">
      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <AccountCircleOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Status aktywności: Wł.</span>
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <MessageOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Inne</span>
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <ArchiveOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Zarchiwizowane czaty</span>
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <SendClockOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Ustawienia dostarczania wiadomości</span>
      </button>
    </div>

    <div class="py-1">
      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <AccountCancelOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Ograniczone konta</span>
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-2 hover:bg-theme-hover transition-colors text-left focus:outline-none">
        <MinusCircleOutlineIcon class="h-5 w-5 text-theme-text shrink-0" />
        <span class="text-[14px] font-medium">Ustawienia blokowania</span>
      </button>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import PhoneRingIcon from 'vue-material-design-icons/PhoneRing.vue'
import VolumeHighIcon from 'vue-material-design-icons/VolumeHigh.vue'
import ChatAlertIcon from 'vue-material-design-icons/ChatAlert.vue'
import ShieldOutlineIcon from 'vue-material-design-icons/ShieldOutline.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue'
import MessageOutlineIcon from 'vue-material-design-icons/MessageOutline.vue'
import ArchiveOutlineIcon from 'vue-material-design-icons/ArchiveOutline.vue'
import SendClockOutlineIcon from 'vue-material-design-icons/SendClockOutline.vue'
import AccountCancelOutlineIcon from 'vue-material-design-icons/AccountCancelOutline.vue'
import MinusCircleOutlineIcon from 'vue-material-design-icons/MinusCircleOutline.vue'

const incomingCallsSounds = ref(true)
const messageSounds = ref(true)
const popupMessages = ref(true)
</script>
