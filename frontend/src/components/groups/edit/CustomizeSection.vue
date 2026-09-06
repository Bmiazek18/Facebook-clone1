<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <h2 class="text-[20px] font-semibold text-[#050505] dark:text-[#e4e6eb] mb-2">{{ $t('groups.dostosujGrupe') }}</h2>

    <div class="flex flex-col">
      <template v-for="item in items" :key="item.id">
        <!-- REGULARNY PRZYCISK (Gdy element nie jest edytowany inline) -->
        <button
          v-if="activeEditId !== item.id"
          @click="toggleEdit(item.id)"
          :class="[
            'flex items-center justify-between py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0 hover:bg-gray-100 dark:hover:bg-[#3a3b3c]/50 transition-colors -mx-4 px-4 group text-left cursor-pointer',
            activeEditId && activeEditId !== item.id ? 'opacity-40 pointer-events-none' : ''
          ]"
        >
          <div class="flex flex-col pr-4">
            <span class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] leading-snug">
              {{ item.label }}
            </span>
            <span v-if="item.subLabel" class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5 leading-tight">
              {{ item.subLabel }}
            </span>
          </div>

          <div class="shrink-0 flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] group-hover:text-[#050505] dark:group-hover:text-[#e4e6eb] leading-snug">
            <PencilIcon v-if="item.action === 'edit'" :size="20" />
            <ChevronDownIcon v-else-if="item.action === 'dropdown'" :size="24" />
            <div v-else-if="item.action === 'colorPicker'" class="bg-gray-100 dark:bg-[#3a3b3c] rounded-md px-3 py-1.5 flex items-center justify-center border border-gray-300 dark:border-[#4e4f50]">
              <div class="w-5 h-5 rounded-full border border-gray-300" :style="{ backgroundColor: item.color }"></div>
            </div>
          </div>
        </button>

        <!-- FORMULARZ EDYCJI: ADRES INTERNETOWY (Inline) -->
        <div
          v-else-if="item.id === 'web_address' && activeEditId === 'web_address'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-3">
            <!-- Input z prefiksem -->
            <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-3 focus-within:border-[#1877f2] dark:focus-within:border-[#4599ff] focus-within:ring-1 focus-within:ring-[#1877f2] dark:focus-within:ring-[#4599ff] transition-all flex items-center">
              <span class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] select-none">{{ $t('groups.wwwFacebookComGroups') }}</span>
              <input
                v-model="webAddressSlug"
                type="text"
                class="flex-1 bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] font-normal focus:outline-none"
              />
            </div>

            <!-- Informacja na szarym tle -->
            <div class="bg-[#f0f2f5] dark:bg-[#3a3b3c]/60 p-4 rounded-xl text-[14px] text-[#65676b] dark:text-[#b0b3b8] leading-relaxed">{{ $t('groups.kiedyGrupaBedzieLiczyc') }}</div>
          </div>

          <!-- Przyciski: Anuluj / Zapisz -->
          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >{{ $t('common.cancel') }}</button>

            <button
              type="button"
              @click="saveWebAddress"
              :disabled="webAddressSlug === initialWebAddressSlug || !webAddressSlug.trim()"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                webAddressSlug !== initialWebAddressSlug && webAddressSlug.trim()
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >{{ $t('createLive.save') }}</button>
          </div>
        </div>
      </template>
    </div>

    <!-- MODAL WYBORU KOLORU (BaseModal + GroupColorPickerModal) -->
    <BaseModal v-if="isColorModalOpen" @close="isColorModalOpen = false"  :title="$t('create.kolor')">
      <GroupColorPickerModal
        :initial-color="selectedGroupColor"
        @close="isColorModalOpen = false"

        @save="handleSaveColor"
      />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

// Importy ikon
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'

// Importy Modalij (dostosuj ścieżki do swojego projektu, np. z ~/components)
import BaseModal from '~/components/common/BaseModal.vue'
import GroupColorPickerModal from '../modals/GroupColorPickerModal.vue'

const props = defineProps<{
  activeEditId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:activeEditId', id: string | null): void
}>()

// Stan dla formularza adresu internetowego
const webAddressSlug = ref('1142185798240696')
const initialWebAddressSlug = ref('1142185798240696')

// Stan dla Modala wyboru koloru
const isColorModalOpen = ref(false)
const selectedGroupColor = ref('#ffffff')

// Reaktywna tablica elementów
const items = reactive([
  { id: 'web_address', label: 'Adres internetowy', subLabel: 'www.facebook.com/groups/1142185798240696/', action: 'edit' },
  { id: 'group_color', label: 'Kolor grupy', subLabel: '', action: 'colorPicker', color: '#ffffff' },
  { id: 'badges', label: 'Odznaki', subLabel: '7 odznak', action: 'edit' },
])

const toggleEdit = (id: string) => {
  // Jeśli kliknięto Kolor grupy, otwieramy Modal zamiast edycji inline
  if (id === 'group_color') {
    isColorModalOpen.value = true
    return
  }

  if (props.activeEditId === id) {
    cancelEdit()
  } else {
    emit('update:activeEditId', id)
  }
}

const cancelEdit = () => {
  webAddressSlug.value = initialWebAddressSlug.value
  emit('update:activeEditId', null)
}

const saveWebAddress = () => {
  initialWebAddressSlug.value = webAddressSlug.value
  const item = items.find(i => i.id === 'web_address')
  if (item) {
    item.subLabel = `www.facebook.com/groups/${webAddressSlug.value}/`
  }
  emit('update:activeEditId', null)
}

// Obsługa zapisu koloru z Modala
const handleSaveColor = (newColor: string) => {
  selectedGroupColor.value = newColor
  const item = items.find(i => i.id === 'group_color')
  if (item) {
    item.color = newColor
  }
  isColorModalOpen.value = false
}
</script>
