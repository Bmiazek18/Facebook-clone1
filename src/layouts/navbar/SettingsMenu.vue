<template>
  <div class="w-[360px] bg-white text-black flex flex-col max-h-[85vh] overflow-y-auto rounded-[12px] shadow-2xl font-sans">

    <!-- Nagłówek -->
    <div class="px-4 py-3.5 border-b border-gray-200">
      <h2 class="text-[17px] font-semibold leading-tight">Ustawienia czatu</h2>
      <p class="text-[14px] text-gray-500 mt-0.5">Dostosuj interfejs Messengera.</p>
    </div>

    <!-- View security alerts (Wysyła event do rodzica, aby otworzyć modal) -->
    <button
      type="button"
      @click="emit('open-alert')"
      class="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors border-b border-gray-200 focus:outline-none"
    >
      <div class="flex items-center gap-4">
        <ShieldLockOutlineIcon class="h-6 w-6 text-black shrink-0" />
        <span class="text-[15px] font-medium">View security alerts</span>
      </div>
      <span class="bg-[#E8F0FE] text-[#1A73E8] text-[12px] font-bold w-[22px] h-[22px] rounded-full flex items-center justify-center shrink-0">
        2
      </span>
    </button>

    <!-- Dźwięki połączenia przychodzącego -->
    <div class="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors">
      <div class="flex items-center gap-4">
        <PhoneInTalkOutlineIcon class="h-6 w-6 text-black shrink-0" />
        <span class="text-[15px] font-medium leading-snug text-left">Dźwięki połączenia<br/>przychodzącego</span>
      </div>
      <button
        type="button"
        role="switch"
        :aria-checked="incomingCallsSounds"
        @click="incomingCallsSounds = !incomingCallsSounds"
        :class="[
          'w-11 h-6 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none',
          incomingCallsSounds ? 'bg-[#1A73E8]' : 'bg-gray-300'
        ]"
      >
        <div
          :class="[
            'absolute top-[2px] w-5 h-5 bg-white rounded-full shadow-sm transition-transform duration-200',
            incomingCallsSounds ? 'translate-x-[22px]' : 'translate-x-[2px]'
          ]"
        ></div>
      </button>
    </div>

    <!-- Dźwięki wiadomości -->
    <div class="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors">
      <div class="flex items-center gap-4">
        <VolumeHighIcon class="h-6 w-6 text-black shrink-0" />
        <span class="text-[15px] font-medium">Dźwięki wiadomości</span>
      </div>
      <button
        type="button"
        role="switch"
        :aria-checked="messageSounds"
        @click="messageSounds = !messageSounds"
        :class="[
          'w-11 h-6 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none',
          messageSounds ? 'bg-[#1A73E8]' : 'bg-gray-300'
        ]"
      >
        <div
          :class="[
            'absolute top-[2px] w-5 h-5 bg-white rounded-full shadow-sm transition-transform duration-200',
            messageSounds ? 'translate-x-[22px]' : 'translate-x-[2px]'
          ]"
        ></div>
      </button>
    </div>

    <!-- Wyświetlaj nowe wiadomości... -->
    <div class="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors border-b border-gray-200">
      <div class="flex items-start gap-4 pr-3">
        <ForumOutlineIcon class="h-6 w-6 text-black shrink-0 mt-0.5" />
        <div class="flex flex-col text-left">
          <span class="text-[15px] font-medium leading-tight mb-0.5">Wyświetlaj nowe wiadomości w oknie podręcznym</span>
          <span class="text-[13px] text-gray-500 leading-snug">Automatycznie otwieraj nowe wiadomości.</span>
        </div>
      </div>
      <button
        type="button"
        role="switch"
        :aria-checked="popupMessages"
        @click="popupMessages = !popupMessages"
        :class="[
          'w-11 h-6 rounded-full relative shrink-0 transition-colors duration-200 focus:outline-none self-center',
          popupMessages ? 'bg-[#1A73E8]' : 'bg-gray-300'
        ]"
      >
        <div
          :class="[
            'absolute top-[2px] w-5 h-5 bg-white rounded-full shadow-sm transition-transform duration-200',
            popupMessages ? 'translate-x-[22px]' : 'translate-x-[2px]'
          ]"
        ></div>
      </button>
    </div>

    <!-- Prywatność i bezpieczeństwo -->
    <button type="button" class="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors border-b border-gray-200 focus:outline-none text-left">
      <div class="flex items-center gap-4">
        <ShieldLockOutlineIcon class="h-6 w-6 text-black shrink-0" />
        <span class="text-[15px] font-medium">Prywatność i bezpieczeństwo</span>
      </div>
      <ChevronRightIcon class="h-6 w-6 text-gray-400 shrink-0" />
    </button>

    <!-- Pozostałe opcje menu -->
    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left focus:outline-none">
      <AccountCircleOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Status aktywności: Wł.</span>
    </button>

    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left focus:outline-none">
      <MessageOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Inne</span>
    </button>

    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left focus:outline-none">
      <CloseBoxOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Zarchiwizowane czaty</span>
    </button>

    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left border-b border-gray-200 focus:outline-none">
      <SendClockOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Ustawienia dostarczania wiadomości</span>
    </button>

    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left focus:outline-none">
      <AccountCancelOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Ograniczone konta</span>
    </button>

    <button type="button" class="w-full flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors text-left focus:outline-none pb-4">
      <MinusCircleOutlineIcon class="h-6 w-6 text-black shrink-0" />
      <span class="text-[15px] font-medium">Ustawienia blokowania</span>
    </button>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Definicja emitów pozwala na wysłanie eventu do rodzica (poza VDropdown)
const emit = defineEmits(['open-alert'])

// Zaktualizowane ikony w celu lepszego dopasowania do designu ze zrzutu ekranu
import ShieldLockOutlineIcon from 'vue-material-design-icons/ShieldLockOutline.vue'
import PhoneInTalkOutlineIcon from 'vue-material-design-icons/PhoneInTalkOutline.vue'
import VolumeHighIcon from 'vue-material-design-icons/VolumeHigh.vue'
import ForumOutlineIcon from 'vue-material-design-icons/ForumOutline.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue'
import MessageOutlineIcon from 'vue-material-design-icons/MessageOutline.vue'
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue'
import SendClockOutlineIcon from 'vue-material-design-icons/SendClockOutline.vue'
import AccountCancelOutlineIcon from 'vue-material-design-icons/AccountCancelOutline.vue'
import MinusCircleOutlineIcon from 'vue-material-design-icons/MinusCircleOutline.vue'

const incomingCallsSounds = ref(true)
const messageSounds = ref(true)
const popupMessages = ref(true)
</script>
