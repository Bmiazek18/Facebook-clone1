<script setup lang="ts">
import { ref, reactive, inject, computed } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'
import ItemMenu from './ItemMenu.vue'
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Briefcase from 'vue-material-design-icons/Briefcase.vue'
import School from 'vue-material-design-icons/School.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const profileUser: any = inject('profileUser');
const activeSection = ref<string | null>(null)
const form = reactive({ company: '', university: '', highSchool: '' })

const isOwner = inject('isOwner');

const close = () => activeSection.value = null

const hasUniversityInfo = computed(() => !!(profileUser.value.school || profileUser.value.education));
const hasHighSchoolInfo = computed(() => !!profileUser.value.highSchool);


// Funkcje pre-fill (wypełnianie formularza obecnymi danymi)
const editJob = () => {
  form.company = profileUser.value.company || profileUser.value.job || ''
  activeSection.value = 'job'
}
const addJob = () => {
  form.company = ''
  activeSection.value = 'job'
}

const editUniversity = () => {
  form.university = profileUser.value.school || profileUser.value.education || ''
  activeSection.value = 'uni'
}
const addUniversity = () => {
  form.university = ''
  activeSection.value = 'uni'
}

const editHighSchool = () => {
  form.highSchool = profileUser.value.highSchool || ''
  activeSection.value = 'highSchool'
}
const addHighSchool = () => {
  form.highSchool = ''
  activeSection.value = 'highSchool'
}

const save = (section: string) => { console.log(`Zapisano ${section}`, form); close() }
const remove = (section: string) => { console.log(`Usunięto ${section}`) }
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">{{ $t('profile.info.work') }}</h3>
      <div v-if="profileUser.job" class="flex justify-between items-start mb-4">
        <div class="flex items-center text-gray-900">
           <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mr-3"><Briefcase class="text-xl text-gray-500"/></div>
           <span>{{ $t('profile.info.worksAt') }} <strong>{{ profileUser.company }}</strong></span>
        </div>
        <div v-if="isOwner" class="flex items-center space-x-2 text-gray-500">
           <Earth class="text-lg"/>
           <ItemMenu :editText="$t('profile.info.editEmployer')" :removeText="$t('profile.info.removeEmployer')" @edit="editJob" @remove="remove('job')"/>
        </div>
      </div>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'job'" :label="$t('profile.info.company')" v-model="form.company" @cancel="close" @save="save('job')" />
        <button v-else-if="isOwner" @click="addJob" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> {{ $t('profile.info.addWorkplace') }}
        </button>
      </div>
    </div>

    <div v-if="hasUniversityInfo || isOwner">
      <h3 class="font-bold text-xl text-black mb-4">{{ $t('profile.info.university') }}</h3>
      <div v-if="profileUser.school || profileUser.education" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="mr-3"><School class="text-gray-400 text-4xl" /></div>
            <div>
                <div class="font-bold text-lg">{{ profileUser.school }}</div>
                <div class="text-sm text-gray-500" v-if="profileUser.education">{{ profileUser.education }}</div>
            </div>
         </div>
         <div v-if="isOwner" class="flex items-center space-x-2 text-gray-500">
            <Earth class="text-lg"/>
            <ItemMenu :editText="$t('profile.info.editUniversity')" :removeText="$t('profile.info.removeUniversity')" @edit="editUniversity" @remove="remove('uni')"/>
         </div>
      </div>
      <div class="mt-2">
        <EditForm v-if="activeSection === 'uni'" :label="$t('profile.info.universityLabel')" v-model="form.university" @cancel="close" @save="save('uni')" />
        <button v-else-if="isOwner && (!profileUser.school && !profileUser.education)" @click="addUniversity" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> {{ $t('profile.info.addUniversity') }}
        </button>
      </div>
    </div>

    <div v-if="hasHighSchoolInfo || isOwner">
      <h3 class="font-bold text-xl text-black mb-4">{{ $t('profile.info.highSchool') }}</h3>
      <div v-if="profileUser.highSchool" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="mr-3"><School class="text-gray-400 text-4xl" /></div>
            <span>{{ $t('profile.info.attended') }} <strong>{{ profileUser.highSchool }}</strong></span>
         </div>
         <div v-if="isOwner" class="flex items-center space-x-2 text-gray-500">
            <Earth class="text-lg"/>
            <ItemMenu :editText="$t('profile.info.editHighSchool')" :removeText="$t('profile.info.removeHighSchool')" @edit="editHighSchool" @remove="remove('highSchool')"/>
         </div>
      </div>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'highSchool'" :label="$t('profile.info.school')" v-model="form.highSchool" @cancel="close" @save="save('highSchool')" />
        <button v-else-if="isOwner && !profileUser.highSchool" @click="addHighSchool" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> {{ $t('profile.info.addHighSchool') }}
        </button>
      </div>
    </div>
  </div>
</template>
