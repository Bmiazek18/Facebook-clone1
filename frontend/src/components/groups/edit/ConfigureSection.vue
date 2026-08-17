<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <h2 class="text-[20px] font-semibold text-[#050505] dark:text-[#e4e6eb] mb-2">Skonfiguruj grupę</h2>

    <div class="flex flex-col">
      <template v-for="item in items" :key="item.id">

        <!-- REGULARNY PRZYCISK (Gdy nie jest edytowany) -->
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
          </div>
        </button>

        <!-- 1. NAZWA I OPIS (Inline Form) -->
        <div
          v-else-if="item.id === 'name_desc' && activeEditId === 'name_desc'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[15px] font-semibold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-3">
            <!-- Pole: Nazwa -->
            <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-2 focus-within:border-[#1877f2] dark:focus-within:border-[#4599ff] focus-within:ring-1 focus-within:ring-[#1877f2] dark:focus-within:ring-[#4599ff] transition-all">
              <label class="block text-[12px] text-[#65676b] dark:text-[#b0b3b8] font-medium leading-tight">
                Nazwa
              </label>
              <input
                v-model="formName"
                type="text"
                class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] font-semibold focus:outline-none mt-0.5"
              />
            </div>

            <!-- Pole: Opis -->
            <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-2 focus-within:border-[#1877f2] dark:focus-within:border-[#4599ff] focus-within:ring-1 focus-within:ring-[#1877f2] dark:focus-within:ring-[#4599ff] transition-all">
              <textarea
                v-model="formDescription"
                rows="4"
                placeholder="Opis"
                class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] placeholder-[#65676b] dark:placeholder-[#b0b3b8] font-normal focus:outline-none resize-none leading-relaxed pt-1"
              ></textarea>
            </div>
          </div>

          <!-- Przyciski: Anuluj / Zapisz -->
          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveEdit('name_desc')"
              :disabled="formName === initialName && formDescription === initialDescription"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                (formName !== initialName || formDescription !== initialDescription)
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 2. WPROWADZENIE DLA NOWEGO CZŁONKA GRUPY -->
        <div
          v-else-if="item.id === 'onboarding' && activeEditId === 'onboarding'"
          class="py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <div class="flex items-center justify-between mb-2 cursor-pointer" @click="cancelEdit">
            <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb]">
              {{ item.label }}
            </h3>
            <ChevronUpIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8]" />
          </div>

          <p class="text-[15px] text-[#050505] dark:text-[#e4e6eb] leading-snug mb-4">
            Utwórz wiadomość, którą zobaczą nowi członkowie odwiedzający Twoją grupę po raz pierwszy.
          </p>

          <button
            type="button"
            class="w-full bg-[#e7f3ff] hover:bg-[#d8eaff] dark:bg-[#252f3d] dark:hover:bg-[#2d3a4d] text-[#1877f2] dark:text-[#4599ff] font-semibold text-[15px] py-2.5 rounded-xl transition-colors"
          >
            Rozpocznij
          </button>
        </div>

        <!-- 3. PRYWATNOŚĆ (Informacja Inline) -->
        <div
          v-else-if="item.id === 'privacy' && activeEditId === 'privacy'"
          class="py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <div class="flex items-center justify-between mb-4 cursor-pointer" @click="cancelEdit">
            <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb]">
              {{ item.label }}
            </h3>
            <ChevronUpIcon :size="24" class="text-[#65676b] dark:text-[#b0b3b8]" />
          </div>

          <div class="flex items-start gap-3 mb-4">
            <div class="w-10 h-10 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] flex items-center justify-center shrink-0 text-[#050505] dark:text-[#e4e6eb]">
              <LockIcon :size="20" />
            </div>
            <div class="flex-1 min-w-0 pt-0.5">
              <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Prywatna</div>
              <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.
              </div>
            </div>
          </div>

          <p class="text-[15px] text-[#050505] dark:text-[#e4e6eb] leading-relaxed">
            W tej chwili nie można zmienić grupy na publiczną. Przy użyciu ustawień <span class="font-semibold">Ukryj grupę</span> i <span class="font-semibold">Kto może dołączyć do grupy</span> możesz zarządzać listą osób, które mogą wyszukać grupę i do niej dołączyć.
          </p>
        </div>

        <!-- 4. UKRYJ GRUPĘ (Radio Options Inline) -->
        <div
          v-else-if="item.id === 'hide_group' && activeEditId === 'hide_group'"
          class="py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-4">
            <label class="flex items-start gap-3 cursor-pointer group">
              <div class="w-10 h-10 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] flex items-center justify-center shrink-0 text-[#050505] dark:text-[#e4e6eb]">
                <EyeOutlineIcon :size="20" />
              </div>
              <div class="flex-1 min-w-0 pt-0.5">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Widoczna</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8]">Każdy może znaleźć tę grupę.</div>
              </div>
              <input
                type="radio"
                name="hide_group_radio"
                value="visible"
                v-model="hideGroupValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-2 cursor-pointer"
              />
            </label>

            <label class="flex items-start gap-3 cursor-pointer group">
              <div class="w-10 h-10 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] flex items-center justify-center shrink-0 text-[#050505] dark:text-[#e4e6eb]">
                <EyeOffOutlineIcon :size="20" />
              </div>
              <div class="flex-1 min-w-0 pt-0.5">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Ukryta</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8]">Tylko członkowie mogą znaleźć tę grupę.</div>
              </div>
              <input
                type="radio"
                name="hide_group_radio"
                value="hidden"
                v-model="hideGroupValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-2 cursor-pointer"
              />
            </label>
          </div>

          <!-- Przyciski: Anuluj / Zapisz -->
          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveEdit('hide_group')"
              :disabled="hideGroupValue === initialHideGroupValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                hideGroupValue !== initialHideGroupValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 5. LOKALIZACJA (Inline Form + Map) -->
        <div
          v-else-if="item.id === 'location' && activeEditId === 'location'"
          class="py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="relative w-full h-44 rounded-xl overflow-hidden bg-[#87ceeb] mb-3 border border-gray-200 dark:border-[#525355]">
            <svg viewBox="0 0 800 400" class="w-full h-full object-cover">
              <rect width="800" height="400" fill="#a4d7f5" />
              <path d="M150,80 Q180,50 250,70 T230,180 T120,200 Z" fill="#b1e095" />
              <path d="M220,220 Q280,210 320,280 T260,380 T190,300 Z" fill="#b1e095" />
              <path d="M400,60 Q520,40 600,80 T580,180 T420,160 Z" fill="#edf4aa" />
              <path d="M420,190 Q500,180 520,280 T440,360 Z" fill="#b1e095" />
              <path d="M620,240 Q720,230 750,320 T630,360 Z" fill="#b1e095" />
            </svg>
            <button class="absolute right-2 bottom-2 bg-white/80 dark:bg-black/60 rounded-full p-1 text-[#050505] dark:text-[#e4e6eb]">
              <InformationOutlineIcon :size="18" />
            </button>
          </div>

          <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-3 focus-within:border-[#1877f2] dark:focus-within:border-[#4599ff] focus-within:ring-1 focus-within:ring-[#1877f2] dark:focus-within:ring-[#4599ff] transition-all">
            <input
              v-model="locationValue"
              type="text"
              placeholder="Lokalizacje tej grupy"
              class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] placeholder-[#65676b] dark:placeholder-[#b0b3b8] font-normal focus:outline-none"
            />
          </div>

          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveEdit('location')"
              :disabled="locationValue === initialLocationValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                locationValue !== initialLocationValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Importy ikon
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'
import EyeOutlineIcon from 'vue-material-design-icons/EyeOutline.vue'
import EyeOffOutlineIcon from 'vue-material-design-icons/EyeOffOutline.vue'
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'

const props = defineProps<{
  activeEditId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:activeEditId', id: string | null): void
}>()

// --- STAN FORMULARZY ---
const formName = ref('test')
const formDescription = ref('')
const initialName = ref('test')
const initialDescription = ref('')

const hideGroupValue = ref('hidden') // 'visible' | 'hidden'
const initialHideGroupValue = ref('hidden')

const locationValue = ref('')
const initialLocationValue = ref('')

const toggleEdit = (id: string) => {
  if (props.activeEditId === id) {
    cancelEdit()
  } else {
    emit('update:activeEditId', id)
  }
}

const cancelEdit = () => {
  formName.value = initialName.value
  formDescription.value = initialDescription.value
  hideGroupValue.value = initialHideGroupValue.value
  locationValue.value = initialLocationValue.value

  emit('update:activeEditId', null)
}

const saveEdit = (id: string) => {
  if (id === 'name_desc') {
    initialName.value = formName.value
    initialDescription.value = formDescription.value
  } else if (id === 'hide_group') {
    initialHideGroupValue.value = hideGroupValue.value
  } else if (id === 'location') {
    initialLocationValue.value = locationValue.value
  }
  emit('update:activeEditId', null)
}

const items = [
  { id: 'name_desc', label: 'Nazwa i opis', subLabel: '', action: 'edit' },
  { id: 'onboarding', label: 'Wprowadzenie dla nowego członka grupy', subLabel: 'Wył.', action: 'dropdown' },
  { id: 'privacy', label: 'Prywatność', subLabel: 'Prywatna', action: 'dropdown' },
  { id: 'hide_group', label: 'Ukryj grupę', subLabel: 'Ukryta', action: 'edit' },
  { id: 'invite_link', label: 'Zaproś przy użyciu linku', subLabel: 'Tylko administratorzy', action: 'edit' },
  { id: 'location', label: 'Lokalizacja', subLabel: '', action: 'edit' },
]
</script>
