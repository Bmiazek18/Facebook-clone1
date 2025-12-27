<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'
import ItemMenu from './ItemMenu.vue'
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import Earth from 'vue-material-design-icons/Earth.vue'

defineProps<{ profileUser: User }>()
const activeSection = ref<string | null>(null)
const form = reactive({ city: '' })
const open = (s: string) => activeSection.value = s
const close = () => activeSection.value = null
const save = (s: string) => { console.log(s, form); close() }
const remove = (s: string) => console.log('Remove', s)
</script>

<template>
  <div class="space-y-8 text-base">
    <div>
      <h3 class="font-bold text-xl text-black mb-4">Wcześniejsze miejsca zamieszkania</h3>

      <div v-if="profileUser.location" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mr-3 text-gray-500"><MapMarker class="text-2xl"/></div>
            <div><div class="font-bold text-lg">{{ profileUser.location }}</div><div class="text-sm text-gray-500">Aktualne</div></div>
         </div>
         <div class="flex items-center space-x-2 text-gray-500">
           <Earth class="text-lg"/>
           <ItemMenu editText="Edytuj miejsce" removeText="Usuń miejsce" @edit="open('loc')" @remove="remove('loc')"/>
         </div>
      </div>
      <div class="mt-2">
         <EditForm v-if="activeSection === 'loc'" label="Miasto" v-model="form.city" @cancel="close" @save="save('loc')" />
         <button v-else @click="open('loc')" class="flex items-center text-blue-600 hover:underline font-medium mb-6">
           <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj miasto
         </button>
      </div>

      <div v-if="profileUser.hometown" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mr-3 text-gray-500"><MapMarker class="text-2xl"/></div>
            <div><div class="font-bold text-lg">{{ profileUser.hometown }}</div><div class="text-sm text-gray-500">Miasto rodzinne</div></div>
         </div>
         <div class="flex items-center space-x-2 text-gray-500">
           <Earth class="text-lg"/>
           <ItemMenu editText="Edytuj miasto rodzinne" removeText="Usuń miasto rodzinne" @edit="open('home')" @remove="remove('home')"/>
         </div>
      </div>
      <div class="mt-2">
         <EditForm v-if="activeSection === 'home'" label="Miasto rodzinne" v-model="form.city" @cancel="close" @save="save('home')" />
         <button v-else-if="!profileUser.hometown" @click="open('home')" class="flex items-center text-blue-600 hover:underline font-medium">
           <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj miasto rodzinne
         </button>
      </div>
    </div>
  </div>
</template>
