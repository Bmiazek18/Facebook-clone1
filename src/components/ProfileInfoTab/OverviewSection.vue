<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'

// Import ikon
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Phone from 'vue-material-design-icons/Phone.vue'
import Lock from 'vue-material-design-icons/Lock.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Account from 'vue-material-design-icons/Account.vue' // Płeć
import CakeVariant from 'vue-material-design-icons/CakeVariant.vue' // Data urodzenia
import CommentText from 'vue-material-design-icons/CommentText.vue' // Zaimki (Dymek)
import Earth from 'vue-material-design-icons/Earth.vue' // Publiczne
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue' // Znajomi

defineProps<{ profileUser: User }>()

const activeSection = ref<string | null>(null)
// Dodajemy pola social i language do formularza
const form = reactive({ website: '', social: '', language: '' })

const open = (s: string) => activeSection.value = s
const close = () => activeSection.value = null
const save = (s: string) => { console.log(`Zapisano ${s}`); close() }
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Dane kontaktowe</h3>

      <div v-if="profileUser.phone" class="flex justify-between items-center mb-6">
         <div class="flex items-center text-gray-900">
            <Phone class="text-gray-500 mr-4 text-2xl" />
            <div>
                <div class="font-medium text-lg">{{ profileUser.phone }}</div>
                <div class="text-xs text-gray-500">Telefon komórkowy</div>
            </div>
         </div>
         <div class="flex space-x-3 text-gray-500">
             <Lock class="text-lg text-gray-400"/>
             <button class="hover:bg-gray-200 p-2 rounded-full"><Pencil class="text-xl text-gray-700"/></button>
         </div>
      </div>

      <h3 class="font-bold text-xl text-black mb-4">Witryny i linki społecznościowe</h3>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'web'" label="Adres witryny" v-model="form.website" @cancel="close" @save="save('web')" />
        <button v-else @click="open('web')" class="flex items-center text-blue-600 hover:underline font-medium mb-4">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj witrynę internetową
        </button>
      </div>

      <div class="mt-2">
        <EditForm v-if="activeSection === 'social'" label="Link społecznościowy" v-model="form.social" @cancel="close" @save="save('social')" />
        <button v-else @click="open('social')" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj link społecznościowy
        </button>
      </div>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Podstawowe informacje</h3>

      <div class="mb-6">
         <EditForm v-if="activeSection === 'lang'" label="Język" v-model="form.language" @cancel="close" @save="save('lang')" />
         <button v-else @click="open('lang')" class="flex items-center text-blue-600 hover:underline font-medium">
            <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj język
         </button>
      </div>

      <div class="space-y-6">

         <div class="flex justify-between items-center">
            <div class="flex items-center text-gray-900">
               <Account class="text-gray-500 mr-4 text-2xl"/>
               <div>
                   <div class="font-semibold text-lg">{{ profileUser.gender || 'Mężczyzna' }}</div>
                   <div class="text-xs text-gray-500">Płeć</div>
               </div>
            </div>
            <button class="hover:bg-gray-200 p-2 rounded-full"><Pencil class="text-xl text-gray-700"/></button>
         </div>

         <div class="flex justify-between items-center">
            <div class="flex items-center text-gray-900">
               <CommentText class="text-gray-500 mr-4 text-2xl"/>
               <div>
                   <div class="font-semibold text-lg">on/jego</div>
                   <div class="text-xs text-gray-500">Zaimki systemowe</div>
               </div>
            </div>
            <div class="flex items-center space-x-3 text-gray-500">
                <Earth class="text-lg text-gray-400"/>
                <button class="hover:bg-gray-200 p-2 rounded-full"><Pencil class="text-xl text-gray-700"/></button>
            </div>
         </div>

         <div class="flex justify-between items-center">
            <div class="flex items-center text-gray-900">
               <CakeVariant class="text-gray-500 mr-4 text-2xl"/>
               <div>
                   <div class="font-semibold text-lg">23 lutego</div>
                   <div class="text-xs text-gray-500">Data urodzenia</div>
               </div>
            </div>
            <div class="flex items-center space-x-3 text-gray-500">
                <AccountMultiple class="text-lg text-gray-400"/> <button class="hover:bg-gray-200 p-2 rounded-full"><Pencil class="text-xl text-gray-700"/></button>
            </div>
         </div>

         <div class="flex justify-between items-center">
            <div class="flex items-center text-gray-900 ml-[2.6rem]">
               <div>
                   <div class="font-semibold text-lg">2005</div>
                   <div class="text-xs text-gray-500">Rok urodzenia</div>
               </div>
            </div>
            <div class="flex items-center space-x-3 text-gray-500 pr-[3.25rem]">
                <AccountMultiple class="text-lg text-gray-400"/>
            </div>
         </div>

      </div>
    </div>
  </div>
</template>
