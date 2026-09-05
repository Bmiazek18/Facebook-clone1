<script setup lang="ts">
import { ref, computed, inject } from 'vue'

// Import Twojego customowego inputa
import CustomInput from '@/components/common/CustomInput.vue'

// Import ikon z vue-material-design-icons
import Earth from 'vue-material-design-icons/Earth.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import School from 'vue-material-design-icons/School.vue'
import Domain from 'vue-material-design-icons/Domain.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

const isOwner = inject('isOwner', true)

const highSchoolData = ref({
  name: 'Zespół Szkół nr 3 im. Władysława Stanisława Reymonta w Łukowie',
  logo: 'https://via.placeholder.com/40',
})

// --- STANY EDYCJI SEKCJI ---
const activeForm = ref<'none' | 'university' | 'highschool'>('none')

// --- FORMULARZ: SZKOŁA WYŻSZA ---
const uniForm = ref({
  name: '',
  startYear: '',
  endYear: '',
  isGraduated: false,
  fieldOfStudy: '',
  dorm: '',
  description: '',
  activities: '',
  attendedFor: 'Szkoła wyższa',
  degree: '',
})

// --- FORMULARZ: SZKOŁA ŚREDNIA ---
const hsForm = ref({
  name: '',
  startYear: '',
  endYear: '',
  isGraduated: false,
})

const years = Array.from({ length: 40 }, (_, i) => String(new Date().getFullYear() - i))

// Walidacja przycisków zapisu
const isUniValid = computed(() => uniForm.value.name.trim().length > 0)
const isHsValid = computed(() => hsForm.value.name.trim().length > 0)

const resetForms = () => {
  activeForm.value = 'none'
  // Opcjonalnie tutaj reset stanów formularzy
}

const saveUniversity = () => {
  if (!isUniValid.value) return
  resetForms()
}

const saveHighSchool = () => {
  if (!isHsValid.value) return
  resetForms()
}
</script>

<template>
  <div class="max-w-[850px] text-[#050505] antialiased space-y-6">
    <div class="space-y-4">
      <h2 class="font-semibold text-[17px]">Szkoła wyższa</h2>

      <div v-if="activeForm !== 'university'">
        <button
          @click="isOwner ? (activeForm = 'university') : null"
          class="inline-flex items-center text-[15px] text-[#65676B] font-medium transition-colors"
          :class="isOwner ? 'cursor-pointer hover:bg-gray-100 p-2 -ml-2 rounded-md' : ''"
        >
          <School :size="20" class="text-[#65676B] mr-3" />
          <span class="text-[#65676B] font-normal">Szkoła wyższa</span>
        </button>
      </div>

      <div v-else class="space-y-4 border-t border-gray-100 pt-4">
        <div
          class="inline-flex items-center gap-1.5 bg-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] mb-2"
        >
          <Earth :size="16" class="text-[#65676B]" />
          Publiczne
        </div>

        <div>
          <CustomInput id="uni-name" label="Nazwa uczelni" v-model="uniForm.name" variant="new" />
          <p class="text-[12px] text-[#65676B] px-1 mt-1">Wymagane</p>
        </div>

        <div class="space-y-3">
          <div class="flex items-center gap-3">
            <div class="relative inline-block">
              <select
                v-model="uniForm.startYear"
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
            <span class="text-[15px] text-[#65676B]">do</span>
            <div class="relative inline-block">
              <select
                v-model="uniForm.endYear"
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

          <label class="flex items-center gap-2 cursor-pointer select-none">
            <input
              v-model="uniForm.isGraduated"
              type="checkbox"
              class="w-5 h-5 rounded text-[#1877F2] border-gray-300 focus:ring-[#1877F2]"
            />
            <span class="font-normal text-[15px]">Ukończenie szkoły</span>
          </label>
        </div>

        <div>
          <CustomInput
            id="uni-field"
            label="Kierunek"
            v-model="uniForm.fieldOfStudy"
            variant="new"
          />
          <p class="text-[12px] text-[#65676B] px-1 mt-1">0/3 elementy</p>
        </div>

        <CustomInput id="uni-dorm" label="Akademik" v-model="uniForm.dorm" variant="new" />

        <div
          class="group relative border border-[#ccd0d5] rounded-xl p-4 pt-6 pb-2.5 bg-transparent focus-within:border-[#1877f2] focus-within:ring-1 focus-within:ring-[#1877f2] transition-all"
        >
          <label
            class="absolute left-4 z-10 origin-[0] transform duration-300 cursor-text text-[#606770]"
            :class="
              uniForm.description
                ? 'top-1 scale-75'
                : 'top-1/2 -translate-y-1/2 scale-100 group-focus-within:top-1 group-focus-within:-translate-y-0 group-focus-within:scale-75'
            "
            >Opis</label
          >
          <textarea
            v-model="uniForm.description"
            rows="3"
            class="w-full resize-none block border-0 p-0 text-[15px] text-[#1c1e21] focus:outline-none bg-transparent"
          ></textarea>
        </div>

        <div>
          <CustomInput
            id="uni-activities"
            label="Zajęcia"
            v-model="uniForm.activities"
            variant="new"
          />
          <p class="text-[12px] text-[#65676B] px-1 mt-1">Do 10 pozycji</p>
        </div>

        <div
          class="group relative border border-[#ccd0d5] rounded-xl p-4 pt-6 pb-2.5 bg-transparent"
        >
          <label class="absolute left-4 top-1 scale-75 text-[#606770]">Uczęszczał(a) na:</label>
          <div
            class="w-full text-[15px] text-[#1c1e21] pt-1 flex justify-between items-center cursor-pointer"
          >
            <span>{{ uniForm.attendedFor }}</span>
            <ChevronDown :size="18" class="text-[#606770]" />
          </div>
        </div>

        <CustomInput
          id="uni-degree"
          label="Tytuł/stopień naukowy"
          v-model="uniForm.degree"
          variant="new"
        />

        <div class="flex justify-end space-x-2 pt-4 border-t border-gray-200 mt-4">
          <button
            @click="resetForms"
            class="px-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
          >
            Anuluj
          </button>
          <button
            @click="saveUniversity"
            :disabled="!isUniValid"
            class="px-5 py-2 font-semibold rounded-md text-[15px] transition-colors"
            :class="
              isUniValid
                ? 'bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF]'
                : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'
            "
          >
            Zapisz
          </button>
        </div>
      </div>
    </div>

    <hr class="border-gray-200" />

    <div class="space-y-4">
      <h2 class="font-semibold text-[17px]">Szkoła średnia</h2>

      <div v-if="activeForm !== 'highschool'" class="space-y-3">
        <div
          v-if="highSchoolData.name"
          class="flex items-center justify-between group p-2 -ml-2 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <img
              :src="highSchoolData.logo"
              alt="School logo"
              class="w-10 h-10 rounded-full border border-gray-200 object-cover"
            />
            <div class="flex flex-col">
              <span class="text-[15px] font-semibold leading-5 text-[#050505]">{{
                highSchoolData.name
              }}</span>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <div class="p-2 text-[#65676B]"><Earth :size="18" /></div>
            <button
              @click="activeForm = 'highschool'"
              class="p-2 text-[#050505] hover:bg-[#E4E6EB] rounded-full transition-colors"
            >
              <Pencil :size="18" />
            </button>
          </div>
        </div>

        <button
          @click="isOwner ? (activeForm = 'highschool') : null"
          class="inline-flex items-center gap-3 px-4 py-2 bg-[#E4E6EB]/50 hover:bg-[#E4E6EB] transition-colors rounded-lg text-[15px] font-semibold text-[#050505]"
        >
          <Domain :size="20" class="text-[#65676B]" />
          <span class="text-[#65676B] font-normal">Szkoła średnia</span>
        </button>
      </div>

      <div v-else class="space-y-4 border-t border-gray-100 pt-4">
        <div
          class="inline-flex items-center gap-1.5 bg-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] mb-2"
        >
          <Earth :size="16" class="text-[#65676B]" />
          Publiczne
        </div>

        <div>
          <CustomInput id="hs-name" label="Szkoła" v-model="hsForm.name" variant="new" />
          <p class="text-[12px] text-[#65676B] px-1 mt-1">Wymagane</p>
        </div>

        <div class="space-y-4">
          <div class="flex flex-col gap-2">
            <span class="text-[15px] font-semibold text-[#050505]">Okres</span>
            <div class="flex items-center gap-3">
              <div class="relative inline-block">
                <select
                  v-model="hsForm.startYear"
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
              <span class="text-[15px] text-[#65676B]">do</span>
              <div class="relative inline-block">
                <select
                  v-model="hsForm.endYear"
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
          </div>

          <label class="flex items-center gap-2 cursor-pointer select-none">
            <input
              v-model="hsForm.isGraduated"
              type="checkbox"
              class="w-5 h-5 rounded text-[#1877F2] border-gray-300 focus:ring-[#1877F2]"
            />
            <span class="font-normal text-[15px]">Ukończenie szkoły</span>
          </label>
        </div>

        <div class="flex justify-end space-x-2 pt-4 border-t border-gray-200 mt-4">
          <button
            @click="resetForms"
            class="px-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
          >
            Anuluj
          </button>
          <button
            @click="saveHighSchool"
            :disabled="!isHsValid"
            class="px-5 py-2 font-semibold rounded-md text-[15px] transition-colors"
            :class="
              isHsValid
                ? 'bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF]'
                : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'
            "
          >
            Zapisz
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
