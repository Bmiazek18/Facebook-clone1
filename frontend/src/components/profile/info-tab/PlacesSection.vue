<script setup lang="ts">
import { ref, computed, inject, watch } from 'vue'
import { usersApi } from '@/api/users'

// Import Twojego customowego inputa (dostosuj ścieżkę według struktury projektu)
import CustomInput from '@/components/common/CustomInput.vue'

// Import wymaganych ikon z vue-material-design-icons
import Earth from 'vue-material-design-icons/Earth.vue'
import Briefcase from 'vue-material-design-icons/Briefcase.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'

const isOwner = inject('isOwner', true)
const profileUser: any = inject('profileUser')
const fetchUserProfile = inject<() => Promise<void>>('fetchUserProfile')

// --- STANY WIDOKU ---
const isEditing = ref(false)

// --- DANE FORMULARZA ---
const company = ref('')
const position = ref('')
const startYear = ref('')
const isCurrent = ref(true)
const location = ref('')
const description = ref('')

watch(() => profileUser?.value, (val) => {
  if (val) {
    company.value = val.company || ''
    position.value = val.job || ''
  }
}, { immediate: true })

// --- DANE REPERTOARU LAT ---
const years = Array.from({ length: 40 }, (_, i) => String(new Date().getFullYear() - i))

// Walidacja pól wymaganych (Firma i Stanowisko)
const isValid = computed(() => {
  return company.value.trim().length > 0 && position.value.trim().length > 0
})

const handleSave = async () => {
  if (!isValid.value) return
  try {
    await usersApi.updateProfile(profileUser.value.id, {
      job: position.value,
      company: company.value
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditing.value = false
  } catch (err) {
    console.error('Failed to update work details:', err)
  }
}

const handleCancel = () => {
  isEditing.value = false
}
</script>

<template>
  <div class="max-w-[850px] text-theme-text antialiased">
    <h2 class="font-semibold text-[17px] mb-4">{{ $t('profile.info.work') }}</h2>

    <div v-if="!isEditing">
      <div v-if="position || company" class="flex justify-between items-start group mb-4">
        <div class="flex gap-4">
          <div class="w-10 h-10 rounded-full bg-theme-bg-tertiary flex items-center justify-center shrink-0">
            <Briefcase :size="24" class="text-[#1877F2]" />
          </div>
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] font-medium text-theme-text">{{ $t('profile.pracujeJako') }}<span class="font-semibold">{{ position }}</span>{{ $t('profile.w') }}<span class="font-semibold">{{ company }}</span>
            </span>
            <span class="text-[13px] text-theme-text-secondary">{{ $t('profile.doswiadczenieZawodowe') }}</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="isEditing = true"
            class="p-2 hover:bg-theme-hover rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-theme-text-secondary" />
          </button>
        </div>
      </div>
      <button
        v-else
        @click="isOwner ? (isEditing = true) : null"
        class="inline-flex items-center text-[15px] text-theme-text-secondary font-medium transition-colors cursor-pointer"
        :class="isOwner ? 'hover:bg-theme-hover p-2 -ml-2 rounded-md' : ''"
      >
        <Briefcase :size="20" class="text-theme-text mr-3" />{{ $t('profile.dodajDoswiadczenieZawodowe') }}</button>
    </div>

    <div v-else class="space-y-4">
      <div
        class="inline-flex items-center gap-1.5 bg-theme-bg-tertiary px-3 py-1.5 rounded-md font-semibold text-[15px] text-theme-text mb-2"
      >
        <Earth :size="16" class="text-theme-text-secondary" />{{ $t('postFilter.privacyPublic') }}</div>

      <div>
        <CustomInput id="company-input" :label="$t('profile.info.company')" v-model="company" variant="new" />
        <p class="text-[12px] text-theme-text-secondary px-1 mt-1">{{ $t('profile.wymagane') }}</p>
      </div>

      <div>
        <CustomInput id="position-input" :label="$t('profile.stanowisko')" v-model="position" variant="new" />
        <p class="text-[12px] text-theme-text-secondary px-1 mt-1">{{ $t('profile.wymagane') }}</p>
      </div>

      <div class="space-y-3 pt-2">
        <h4 class="font-semibold text-[15px]">{{ $t('profile.okres') }}</h4>

        <div class="flex items-center gap-3">
          <span class="text-[15px] text-theme-text">{{ $t('profile.od') }}</span>

          <div class="relative inline-block">
            <select
              v-model="startYear"
              class="appearance-none bg-theme-bg-tertiary hover:bg-theme-bg-tertiary transition-colors pl-3 pr-8 py-1.5 rounded-md font-semibold text-[15px] text-theme-text outline-none cursor-pointer"
            >
              <option value="" disabled selected>{{ $t('postFilter.yearLabel') }}</option>
              <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
            </select>
            <ChevronDown
              :size="16"
              class="absolute right-2 top-2.5 pointer-events-none text-theme-text"
            />
          </div>
        </div>

        <label class="flex items-center gap-2 cursor-pointer pt-1 select-none">
          <input
            v-model="isCurrent"
            type="checkbox"
            class="w-5 h-5 rounded text-[#1877F2] border-theme-border focus:ring-[#1877F2] cursor-pointer"
          />
          <span class="font-semibold text-[15px]">{{ $t('profile.obecniePracujeTutaj') }}</span>
        </label>
      </div>

      <div class="pt-2">
        <CustomInput
          id="location-input"
          :label="$t('pages.miejscowosc')"
          v-model="location"
          variant="new"
          :disableFocusColor="true"
        />
      </div>

      <div>
        <div
          class="group relative border border-theme-border rounded-xl p-4 pt-6 pb-2.5 bg-transparent focus-within:border-theme-border transition-all"
        >
          <label
            class="absolute left-4 z-10 origin-[0] transform duration-300 cursor-text text-theme-text-secondary"
            :class="
              description.length > 0
                ? 'top-1 scale-75 -translate-y-0'
                : 'top-1/2 -translate-y-1/2 scale-100 group-focus-within:top-1 group-focus-within:-translate-y-0 group-focus-within:scale-75'
            "
          >{{ $t('createLive.description') }}</label>
          <textarea
            v-model="description"
            rows="3"
            class="w-full resize-none block border-0 p-0 text-[15px] text-theme-text focus:outline-none bg-transparent"
          ></textarea>
        </div>
      </div>

      <div class="flex justify-end space-x-2 pt-4 border-t border-theme-border mt-6">
        <button
          @click="handleCancel"
          class="px-5 py-2 bg-theme-bg-tertiary hover:bg-theme-bg-tertiary text-theme-text font-semibold rounded-md text-[15px] transition-colors"
        >{{ $t('common.cancel') }}</button>
        <button
          @click="handleSave"
          :disabled="!isValid"
          class="px-5 py-2 font-semibold rounded-md text-[15px] transition-colors"
          :class="
            isValid
              ? 'bg-theme-bg-tertiary text-theme-text hover:bg-theme-bg-tertiary'
              : 'bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed'
          "
        >{{ $t('createLive.save') }}</button>
      </div>
    </div>
  </div>
</template>
