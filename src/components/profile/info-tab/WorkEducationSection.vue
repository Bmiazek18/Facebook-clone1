<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'
import ItemMenu from './ItemMenu.vue'
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Briefcase from 'vue-material-design-icons/Briefcase.vue'
import School from 'vue-material-design-icons/School.vue'
import Earth from 'vue-material-design-icons/Earth.vue'

const props = defineProps<{ profileUser: User }>()
const activeSection = ref<string | null>(null)
const form = reactive({ company: '', school: '' })

const close = () => activeSection.value = null

// Funkcje pre-fill (wypełnianie formularza obecnymi danymi)
const editJob = () => {
  form.company = props.profileUser.company || props.profileUser.job || ''
  activeSection.value = 'job'
}
const addJob = () => {
  form.company = ''
  activeSection.value = 'job'
}

const editSchool = () => {
  form.school = props.profileUser.school || ''
  activeSection.value = 'school'
}
const addSchool = () => {
  form.school = ''
  activeSection.value = 'school'
}
const addUni = () => {
  form.school = ''
  activeSection.value = 'uni'
}

const save = (section: string) => { console.log(`Zapisano ${section}`, form); close() }
const remove = (section: string) => { console.log(`Usunięto ${section}`) }
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Praca</h3>
      <div v-if="profileUser.job" class="flex justify-between items-start mb-4">
        <div class="flex items-center text-gray-900">
           <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mr-3"><Briefcase class="text-xl text-gray-500"/></div>
           <span>Pracuje w: <strong>{{ profileUser.company }}</strong></span>
        </div>
        <div class="flex items-center space-x-2 text-gray-500">
           <Earth class="text-lg"/>
           <ItemMenu editText="Edytuj pracodawcę" removeText="Usuń pracodawcę" @edit="editJob" @remove="remove('job')"/>
        </div>
      </div>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'job'" label="Firma" v-model="form.company" @cancel="close" @save="save('job')" />
        <button v-else @click="addJob" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj miejsce pracy
        </button>
      </div>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Szkoła wyższa</h3>
      <div class="mt-2">
        <EditForm v-if="activeSection === 'uni'" label="Uczelnia" v-model="form.school" @cancel="close" @save="save('uni')" />
        <button v-else @click="addUni" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj szkołę wyższą
        </button>
      </div>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Szkoła średnia</h3>
      <div v-if="profileUser.education" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="mr-3"><School class="text-gray-400 text-4xl" /></div>
            <span>Uczęszczał do: <strong>{{ profileUser.school || profileUser.education }}</strong></span>
         </div>
         <div class="flex items-center space-x-2 text-gray-500">
            <Earth class="text-lg"/>
            <ItemMenu editText="Edytuj szkołę" removeText="Usuń szkołę" @edit="editSchool" @remove="remove('school')"/>
         </div>
      </div>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'school'" label="Szkoła" v-model="form.school" @cancel="close" @save="save('school')" />
        <button v-else @click="addSchool" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj szkołę średnią
        </button>
      </div>
    </div>
  </div>
</template>
