<script setup lang="ts">
import BaseModal from './BaseModal.vue';
import LockIcon from 'vue-material-design-icons/Lock.vue';
import PhoneOffIcon from 'vue-material-design-icons/PhoneOff.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';

// Definicja typów dla propsów
interface Props {
  isOpen: boolean;
  callerName: string;
  callerAvatar: string;
}

// Props z wartościami domyślnymi
withDefaults(defineProps<Props>(), {
  isOpen: false,
  callerName: 'Nieznany numer',
  callerAvatar: 'https://via.placeholder.com/150', // Placeholder
});

// Emity do obsługi akcji w rodzicu
const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'reject'): void;
  (e: 'accept'): void;
}>();

// Funkcje pomocnicze
const handleClose = () => emit('close');
const handleReject = () => emit('reject');
const handleAccept = () => emit('accept');
</script>

<template>
  <BaseModal v-if="isOpen" title="Połączenie przychodzące" @close="handleClose">
    <div class="w-[300px] flex flex-col items-center p-4">
      <div class="mb-4 relative">
        <img
          :src="callerAvatar"
          alt="Avatar"
          class="w-[72px] h-[72px] rounded-full object-cover shadow-sm"
        />
      </div>

      <h2 class="text-2xl font-extrabold text-theme-text text-center leading-tight">
        {{ callerName }}
      </h2>

      <p class="text-xl font-bold text-theme-text text-center mb-2">
        dzwoni do Ciebie
      </p>

      <div class="flex items-center gap-1.5 text-xs text-gray-500 font-medium mb-8">
        <LockIcon :size="16" fillColor="currentColor" />
        Pełne szyfrowanie
      </div>

      <div class="flex items-center justify-between w-full px-4 mb-2">
        <div class="flex flex-col items-center gap-2">
          <button
            @click="handleReject"
            class="w-14 h-14 rounded-full bg-red-600 flex items-center justify-center text-white shadow-lg hover:bg-red-700 active:scale-95 transition-all duration-200"
          >
            <PhoneOffIcon :size="28" fillColor="white" />
          </button>
          <span class="text-xs font-semibold text-theme-text">Odrzuć</span>
        </div>

        <div class="flex flex-col items-center gap-2">
          <button
            @click="handleAccept"
            class="w-14 h-14 rounded-full bg-green-600 flex items-center justify-center text-white shadow-lg hover:bg-green-700 active:scale-95 transition-all duration-200"
          >
            <PhoneIcon :size="28" fillColor="white" />
          </button>
          <span class="text-xs font-semibold text-theme-text">Zaakceptuj</span>
        </div>
      </div>
    </div>
  </BaseModal>
</template>
