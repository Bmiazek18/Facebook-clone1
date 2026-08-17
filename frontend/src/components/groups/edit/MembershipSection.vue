<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <h2 class="text-[20px] font-semibold text-[#050505] dark:text-[#e4e6eb] mb-2">Zarządzaj członkostwem</h2>

    <div class="flex flex-col">
      <template v-for="item in items" :key="item.id">
        <!-- REGULARNY PRZYCISK (Gdy element nie jest edytowany) -->
        <!-- REGULARNY PRZYCISK (Gdy element nie jest edytowany) -->
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

        <!-- 1. KTO MOŻE DOŁĄCZYĆ DO GRUPY (Inline Radio) -->
        <div
          v-else-if="item.id === 'who_join' && activeEditId === 'who_join'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-4">
            <!-- Opcja 1: Profile i strony -->
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Profile i strony</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Zezwalaj na wysyłanie próśb o dołączenie do grupy stronom i profilom
                </div>
              </div>
              <input
                type="radio"
                name="who_join_radio"
                value="profiles_and_pages"
                v-model="whoJoinValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <!-- Opcja 2: Tylko profile -->
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Tylko profile</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Zezwalaj na wysyłanie próśb o dołączenie do grupy tylko profilom
                </div>
              </div>
              <input
                type="radio"
                name="who_join_radio"
                value="only_profiles"
                v-model="whoJoinValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
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
              @click="saveWhoJoin"
              :disabled="whoJoinValue === initialWhoJoinValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                whoJoinValue !== initialWhoJoinValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 2. KTO MOŻE ZATWIERDZAĆ PROŚBY O DOŁĄCZENIE (Inline Radio) -->
        <div
          v-else-if="item.id === 'who_approve' && activeEditId === 'who_approve'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-4">
            <!-- Opcja 1: Każdy członek grupy -->
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Każdy członek grupy</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Członkowie mogą tylko zatwierdzać prośby od znajomych.
                </div>
              </div>
              <input
                type="radio"
                name="who_approve_radio"
                value="any_member"
                v-model="whoApproveValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <!-- Opcja 2: Tylko administratorzy i moderatorzy -->
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Tylko administratorzy i moderatorzy</div>
              </div>
              <input
                type="radio"
                name="who_approve_radio"
                value="admins_mods_only"
                v-model="whoApproveValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
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
              @click="saveWhoApprove"
              :disabled="whoApproveValue === initialWhoApproveValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                whoApproveValue !== initialWhoApproveValue
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

const props = defineProps<{
  activeEditId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:activeEditId', id: string | null): void
}>()

// Stan dla 'Kto może dołączyć do grupy'
const whoJoinValue = ref<'only_profiles' | 'profiles_and_pages'>('only_profiles')
const initialWhoJoinValue = ref<'only_profiles' | 'profiles_and_pages'>('only_profiles')

// Stan dla 'Kto może zatwierdzać prośby o dołączenie'
const whoApproveValue = ref<'any_member' | 'admins_mods_only'>('any_member')
const initialWhoApproveValue = ref<'any_member' | 'admins_mods_only'>('any_member')

const toggleEdit = (id: string) => {
  if (props.activeEditId === id) {
    cancelEdit()
  } else {
    emit('update:activeEditId', id)
  }
}

const cancelEdit = () => {
  whoJoinValue.value = initialWhoJoinValue.value
  whoApproveValue.value = initialWhoApproveValue.value
  emit('update:activeEditId', null)
}

const saveWhoJoin = () => {
  initialWhoJoinValue.value = whoJoinValue.value
  const item = items.find(i => i.id === 'who_join')
  if (item) {
    item.subLabel = whoJoinValue.value === 'only_profiles' ? 'Tylko profile' : 'Profile i strony'
  }
  emit('update:activeEditId', null)
}

const saveWhoApprove = () => {
  initialWhoApproveValue.value = whoApproveValue.value
  const item = items.find(i => i.id === 'who_approve')
  if (item) {
    item.subLabel = whoApproveValue.value === 'any_member' ? 'Każdy członek grupy' : 'Tylko administratorzy i moderatorzy'
  }
  emit('update:activeEditId', null)
}

const items = [
  { id: 'who_join', label: 'Kto może dołączyć do grupy', subLabel: 'Tylko profile', action: 'edit' },
  { id: 'who_approve', label: 'Kto może zatwierdzać prośby o dołączenie', subLabel: 'Każdy członek grupy', action: 'edit' },
  { id: 'auto_approved', label: 'Automatycznie zatwierdzani członkowie grupy', subLabel: 'Nikt', action: 'edit' },
]
</script>
