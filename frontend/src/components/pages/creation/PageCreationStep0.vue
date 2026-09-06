<script setup lang="ts">
import { computed } from 'vue'
import CustomInput from '~/components/common/CustomInput.vue'
import CustomTextarea from '~/components/common/CustomTextarea.vue'
import type { PageForm } from '@/types/pageCreation'

const props = defineProps<{
  form: PageForm
}>()

const emit = defineEmits<{
  'next-step': []
}>()

const isNameValid = computed(() => props.form.pageName.trim().length > 0)
const isCategoryValid = computed(() => props.form.category.trim().length > 0)

const removeCategory = () => {
  props.form.category = ''
}

const handleCreate = () => {
  if (isNameValid.value && isCategoryValid.value) {
    emit('next-step')
  }
}
</script>

<template>
  <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
    <div class="text-[13px] text-[#65676B] mb-2 font-medium">
      <span class="hover:underline cursor-pointer">{{ $t('home.pages') }}</span>
      <span class="mx-1.5">›</span>
      <span class="text-[#65676B]">{{ $t('login.createPage') }}</span>
    </div>

    <h1 class="text-[24px] font-bold leading-tight mb-3 text-[#050505]">{{ $t('login.createPage') }}</h1>

    <p class="text-[15px] text-[#65676B] leading-snug mb-6">{{ $t('pages.twojaStronaToMiejsce') }}</p>

    <div class="mb-4">
      <CustomInput
        v-model="form.pageName"
        :label="$t('pages.nazwaStronyWymagana')"
      />
      <p class="text-[12px] text-[#65676B] mt-1.5 leading-snug">{{ $t('pages.uzyjNazwySwojejFirmy') }}<a href="#" class="text-[#1877F2] hover:underline">{{ $t('pages.dowiedzSieWiecejO') }}</a>
      </p>
    </div>

    <div class="mb-4">
      <div class="relative border border-[#CED0D4] focus-within:border-[#1877F2] focus-within:shadow-[0_0_0_1px_#1877F2] rounded-md px-3 pt-5 pb-1.5 bg-white transition-shadow">
        <label class="absolute left-3 top-2 text-[12px] text-[#65676B] focus-within:text-[#1877F2]">{{ $t('pages.kategoriaWymagana') }}</label>
        <div class="flex items-center">
          <div class="flex-1 flex flex-wrap gap-1 items-center min-h-[24px]">
            <div v-if="form.category" class="bg-[#E7F3FF] text-[#1877F2] flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[15px] font-medium mr-1">
              {{ form.category }}
              <button @click="removeCategory" class="hover:bg-[#DBE7F2] rounded-full p-0.5 transition-colors">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                </svg>
              </button>
            </div>
            <input
              v-if="!form.category"
              v-model="form.category"
              type="text"
              :placeholder="$t('pages.wprowadzKategorie')"
              class="w-full focus:outline-none text-[15px] text-[#050505] bg-transparent"
            />
          </div>
          <div v-if="isCategoryValid" class="w-5 h-5 rounded-full bg-[#31A24C] flex items-center justify-center shrink-0 ml-2">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="white">
              <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"></path>
            </svg>
          </div>
        </div>
      </div>
      <p class="text-[12px] text-[#65676B] mt-1.5">{{ $t('pages.wprowadzKategorieKtoraOpisuje') }}</p>
    </div>

    <div class="mb-5">
      <CustomTextarea
        v-model="form.bio"
        :label="$t('pages.opisOpcjonalny')"
        :placeholder="$t('pages.opowiedzUzytkownikomCzymSie')"
      />
      <p class="text-[12px] text-[#65676B] mt-1.5">{{ $t('pages.opowiedzUzytkownikomCzymSie') }}</p>
    </div>
  </div>

  <div class="p-4 bg-white shrink-0 mt-auto border-t border-[#E5E5E5]">
    <button
      @click="handleCreate"
      class="w-full py-[9px] rounded-md font-semibold text-[15px] transition-colors"
      :class="isNameValid && isCategoryValid ? 'bg-[#1877F2] text-white hover:bg-[#166FE5]' : 'bg-[#E4E6EB] text-[#8A8D91] cursor-not-allowed'"
      :disabled="!isNameValid || !isCategoryValid"
    >{{ $t('login.createPage') }}</button>
    <p class="text-[12px] text-[#65676B] text-center mt-3 leading-snug">{{ $t('pages.tworzacStroneAkceptujesz') }}<a href="#" class="text-[#1877F2] hover:underline font-semibold">{{ $t('pages.zasadyDotyczaceStron') }}<br>{{ $t('pages.grupIWydarzen') }}</a>
    </p>
  </div>
</template>
