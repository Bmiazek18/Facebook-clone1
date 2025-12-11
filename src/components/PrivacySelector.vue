<script setup lang="ts">
import { ref } from 'vue';

// --- Import Ikony z vue-material-design-icons ---
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import CheckboxMarkedIcon from 'vue-material-design-icons/CheckboxMarked.vue';
import CheckboxBlankOutlineIcon from 'vue-material-design-icons/CheckboxBlankOutline.vue';

// Definicja emitowanych zdarzeń dla nawigacji
const emit = defineEmits<{
    (e: 'navigate', viewName: string): void;
    (e: 'back'): void;
}>();

// --- Definicje Opcji Prywatności ---
interface PrivacyOption {
  id: string;
  label: string;
  description: string;
  iconComponent: any;
}

const privacyOptions: PrivacyOption[] = [
  { id: 'public', label: 'Publiczne', description: 'Każdy na Facebooku i poza nim', iconComponent: EarthIcon },
  { id: 'friends', label: 'Znajomi', description: 'Twoi znajomi na Facebooku', iconComponent: AccountGroupIcon },
  { id: 'friends_except', label: 'Znajomi z wyjątkiem...', description: 'Nie pokazuj niektórym znajomym', iconComponent: AccountMultipleMinusIcon },
  { id: 'specific_friends', label: 'Konkretni znajomi', description: 'Pokazuj tylko niektórym znajomym', iconComponent: AccountStarIcon },
  // Dodajemy więcej elementów, aby wymusić przewijanie
  { id: 'custom_1', label: 'Opcja testowa 1', description: 'Testowy opis', iconComponent: AccountStarIcon },
  { id: 'custom_2', label: 'Opcja testowa 2', description: 'Testowy opis', iconComponent: AccountStarIcon },
  { id: 'custom_3', label: 'Opcja testowa 3', description: 'Testowy opis', iconComponent: AccountStarIcon },
];

// --- Stan Komponentu ---
const selectedPrivacy = ref('friends');
const setDefault = ref(false);

// --- Funkcje ---
const selectOption = (id: string) => {
  selectedPrivacy.value = id;
};

const handleDone = () => {
    console.log('Ustawiono prywatność:', selectedPrivacy.value);
    emit('back'); // Wrócenie do widoku tworzenia posta
};

const handleCancel = () => {
    emit('back'); // Emitowanie zdarzenia powrotu
};

</script>

<template>
  <div class="privacy-selector-modal flex flex-col p-0 h-[500px]">

    <div class="p-0 pb-4 border-b border-gray-200 flex-shrink-0">
      <h2 class="text-xl font-bold text-gray-800">Kto może zobaczyć Twój post?</h2>
      <p class="text-gray-600 text-sm mt-1">
        Post pojawi się w Aktualnościach, w Twoim profilu oraz w wynikach wyszukiwania.
      </p>
      <p class="text-gray-600 text-sm mt-2">
        Domyślne ustawienie grupy odbiorców to <b>Tylko ja</b> ale możesz je zmienić w odniesieniu do tego konkretnego posta.
      </p>
    </div>

    <div class="flex-grow overflow-y-auto pt-2">
      <div
        v-for="option in privacyOptions"
        :key="option.id"
        class="flex items-center p-3 m-0 rounded-lg cursor-pointer transition-colors"
        :class="{
            'bg-blue-50 hover:bg-blue-100': selectedPrivacy === option.id,
            'hover:bg-gray-100': selectedPrivacy !== option.id
        }"
        @click="selectOption(option.id)"
      >
        <component :is="option.iconComponent" :size="30" class="text-gray-600 mr-4 flex-shrink-0" />

        <div class="flex-grow min-w-0">
          <p class="font-semibold text-gray-800">{{ option.label }}</p>
          <p class="text-gray-500 text-sm truncate">{{ option.description }}</p>
        </div>

        <div class="ml-4 flex-shrink-0">
          <div v-if="selectedPrivacy === option.id">
            <svg class="h-6 w-6 text-blue-500 fill-current" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5" fill="none"/><circle cx="12" cy="12" r="6" fill="currentColor"/></svg>
          </div>
          <div v-else>
            <svg class="h-6 w-6 text-gray-400 fill-current" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
          </div>
        </div>
      </div>
    </div>

    <div class="pt-4 mt-2 border-t border-gray-200 flex-shrink-0">
        <div class="flex items-center mb-4 cursor-pointer" @click="setDefault = !setDefault">
            <component
                :is="setDefault ? CheckboxMarkedIcon : CheckboxBlankOutlineIcon"
                :size="24"
                class="mr-3"
                :class="{'text-blue-500': setDefault, 'text-gray-400': !setDefault}"
            />
            <span class="text-gray-700 text-sm">Ustaw jako domyślną grupę odbiorców.</span>
        </div>

        <div class="flex justify-end space-x-3">
            <button
                class="px-4 py-2 text-gray-600 font-semibold rounded-lg hover:bg-gray-100 transition-colors"
                @click="handleCancel"
            >
                Anuluj
            </button>
            <button
                class="px-6 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                @click="handleDone"
            >
                Done
            </button>
        </div>
    </div>
  </div>
</template>
