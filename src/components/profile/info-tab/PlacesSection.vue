<script setup lang="ts">
import { ref, computed, inject, watch } from 'vue'
import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

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

const UPDATE_PROFILE_MUTATION = gql`
  mutation UpdateProfile($userId: ID!, $input: UpdateProfileInput!) {
    updateProfile(userId: $userId, input: $input) {
      id
      job
      company
    }
  }
`

const { mutate: updateProfile } = useMutation(UPDATE_PROFILE_MUTATION)

const handleSave = async () => {
  if (!isValid.value) return
  try {
    await updateProfile({
      userId: String(profileUser.value.id),
      input: {
        job: position.value,
        company: company.value
      }
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
  <div class="max-w-[850px] text-[#050505] antialiased">
    <h2 class="font-semibold text-[17px] mb-4">Praca</h2>

    <div v-if="!isEditing">
      <div v-if="position || company" class="flex justify-between items-start group mb-4">
        <div class="flex gap-4">
          <div class="w-10 h-10 rounded-full bg-[#E4E6EB] flex items-center justify-center shrink-0">
            <Briefcase :size="24" class="text-[#1877F2]" />
          </div>
          <div class="flex flex-col mt-0.5">
            <span class="text-[15px] font-medium text-[#050505]">
              Pracuje jako: <span class="font-semibold">{{ position }}</span> w <span class="font-semibold">{{ company }}</span>
            </span>
            <span class="text-[13px] text-[#65676B]">Doświadczenie zawodowe</span>
          </div>
        </div>
        <div v-if="isOwner" class="flex items-center gap-3">
          <button
            @click="isEditing = true"
            class="p-2 hover:bg-[#F2F2F2] rounded-full transition-colors cursor-pointer"
          >
            <Pencil :size="20" class="text-[#65676B]" />
          </button>
        </div>
      </div>
      <button
        v-else
        @click="isOwner ? (isEditing = true) : null"
        class="inline-flex items-center text-[15px] text-[#65676B] font-medium transition-colors cursor-pointer"
        :class="isOwner ? 'hover:bg-gray-100 p-2 -ml-2 rounded-md' : ''"
      >
        <Briefcase :size="20" class="text-[#050505] mr-3" />
        Dodaj doświadczenie zawodowe
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        class="inline-flex items-center gap-1.5 bg-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] mb-2"
      >
        <Earth :size="16" class="text-[#65676B]" />
        Publiczne
      </div>

      <div>
        <CustomInput id="company-input" label="Firma" v-model="company" variant="new" />
        <p class="text-[12px] text-[#65676B] px-1 mt-1">Wymagane</p>
      </div>

      <div>
        <CustomInput id="position-input" label="Stanowisko" v-model="position" variant="new" />
        <p class="text-[12px] text-[#65676B] px-1 mt-1">Wymagane</p>
      </div>

      <div class="space-y-3 pt-2">
        <h4 class="font-semibold text-[15px]">Okres</h4>

        <div class="flex items-center gap-3">
          <span class="text-[15px] text-[#050505]">Od</span>

          <div class="relative inline-block">
            <select
              v-model="startYear"
              class="appearance-none bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors pl-3 pr-8 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] outline-none cursor-pointer"
            >
              <option value="" disabled selected>Rok</option>
              <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
            </select>
            <ChevronDown
              :size="16"
              class="absolute right-2 top-2.5 pointer-events-none text-[#050505]"
            />
          </div>
        </div>

        <label class="flex items-center gap-2 cursor-pointer pt-1 select-none">
          <input
            v-model="isCurrent"
            type="checkbox"
            class="w-5 h-5 rounded text-[#1877F2] border-gray-300 focus:ring-[#1877F2] cursor-pointer"
          />
          <span class="font-semibold text-[15px]">Obecnie pracuję tutaj</span>
        </label>
      </div>

      <div class="pt-2">
        <CustomInput
          id="location-input"
          label="Miejscowość"
          v-model="location"
          variant="new"
          :disableFocusColor="true"
        />
      </div>

      <div>
        <div
          class="group relative border border-[#ccd0d5] rounded-xl p-4 pt-6 pb-2.5 bg-transparent focus-within:border-[#ccd0d5] transition-all"
        >
          <label
            class="absolute left-4 z-10 origin-[0] transform duration-300 cursor-text text-[#606770]"
            :class="
              description.length > 0
                ? 'top-1 scale-75 -translate-y-0'
                : 'top-1/2 -translate-y-1/2 scale-100 group-focus-within:top-1 group-focus-within:-translate-y-0 group-focus-within:scale-75'
            "
          >
            Opis
          </label>
          <textarea
            v-model="description"
            rows="3"
            class="w-full resize-none block border-0 p-0 text-[15px] text-[#1c1e21] focus:outline-none bg-transparent"
          ></textarea>
        </div>
      </div>

      <div class="flex justify-end space-x-2 pt-4 border-t border-gray-200 mt-6">
        <button
          @click="handleCancel"
          class="px-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="handleSave"
          :disabled="!isValid"
          class="px-5 py-2 font-semibold rounded-md text-[15px] transition-colors"
          :class="
            isValid
              ? 'bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF]'
              : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'
          "
        >
          Zapisz
        </button>
      </div>
    </div>
  </div>
</template>
