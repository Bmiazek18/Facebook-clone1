<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'
import ItemMenu from './ItemMenu.vue'

// Ikony
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import AccountDetails from 'vue-material-design-icons/AccountDetails.vue'
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import FormatQuoteClose from 'vue-material-design-icons/FormatQuoteClose.vue'
import AccountBadge from 'vue-material-design-icons/AccountBadge.vue'

const props = defineProps<{ profileUser: User }>()

const activeSection = ref<string | null>(null)
const editingIndex = ref<number | null>(null) // Indeks elementu edytowanego
const form = reactive({ bio: '', pronunciation: '', otherName: '', quote: '' })

const close = () => { activeSection.value = null; editingIndex.value = null }

// --- FUNKCJE WYPEŁNIAJĄCE FORMULARZ ---
const editBio = () => { form.bio = props.profileUser.bioDetails || ''; activeSection.value = 'bio' }
const editPron = () => { form.pronunciation = props.profileUser.namePronounciation || ''; activeSection.value = 'pron' }

const editOtherName = (idx: number) => {
  if (props.profileUser.otherNames) form.otherName = props.profileUser.otherNames[idx]
  editingIndex.value = idx
  activeSection.value = 'other_edit'
}
const addOtherName = () => { form.otherName = ''; activeSection.value = 'other_add' }

const editQuote = (idx: number) => {
  if (props.profileUser.favoriteQuotes) form.quote = props.profileUser.favoriteQuotes[idx]
  editingIndex.value = idx
  activeSection.value = 'quote_edit'
}
const addQuote = () => { form.quote = ''; activeSection.value = 'quote_add' }

const save = (s: string) => { console.log(`Zapisano ${s}`, form); close() }
const remove = (s: string, idx?: number) => { console.log(`Usunięto ${s}`) }
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Informacje o Tobie</h3>

      <div v-if="activeSection === 'bio'" class="mb-4">
         <EditForm label="Opis" v-model="form.bio" @cancel="close" @save="save('bio')" />
      </div>

      <div v-else-if="profileUser.bioDetails" class="flex justify-between items-start mb-4">
         <div class="flex items-start text-gray-900">
            <AccountDetails class="text-2xl text-gray-400 mt-1 mr-3"/>
            <p class="whitespace-pre-wrap">{{ profileUser.bioDetails }}</p>
         </div>
         <div class="flex items-center space-x-2 text-gray-500 ml-4">
            <Earth class="text-lg"/>
            <ItemMenu editText="Edytuj biogram" removeText="Usuń" @edit="editBio" @remove="remove('bio')"/>
         </div>
      </div>

      <button v-if="!profileUser.bioDetails && activeSection !== 'bio'" @click="editBio" class="flex items-center text-blue-600 hover:underline font-medium">
         <PlusCircleOutline class="mr-3 text-2xl" /> Podaj szczegółowe informacje na swój temat
      </button>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Wymowa imienia i nazwiska</h3>

       <div v-if="activeSection === 'pron'" class="mb-4">
          <EditForm label="Wymowa" v-model="form.pronunciation" @cancel="close" @save="save('pron')"/>
       </div>

       <div v-else-if="profileUser.namePronounciation" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900">
            <div class="mr-3 w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center"><VolumeHigh class="text-xl text-gray-500"/></div>
            <span>{{ profileUser.namePronounciation }}</span>
         </div>
         <div class="flex items-center space-x-2 text-gray-500"><Earth class="text-lg"/><ItemMenu editText="Edytuj wymowę" removeText="Usuń" @edit="editPron" @remove="remove('pron')"/></div>
      </div>

       <button v-if="!profileUser.namePronounciation && activeSection !== 'pron'" @click="editPron" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj wymowę imienia i nazwiska
       </button>
    </div>

    <div>
       <h3 class="font-bold text-xl text-black mb-4">Inne imiona i nazwiska</h3>
       <div v-if="profileUser.otherNames" class="space-y-4 mb-4">
          <div v-for="(name, idx) in profileUser.otherNames" :key="idx">

             <div v-if="activeSection === 'other_edit' && editingIndex === idx">
                <EditForm label="Nazwa" v-model="form.otherName" @cancel="close" @save="save('other')" />
             </div>

             <div v-else class="flex justify-between items-center">
                <div class="flex items-center text-gray-900"><AccountBadge class="text-2xl text-gray-400 mr-3"/><span class="text-lg">{{ name }}</span></div>
                <div class="flex items-center space-x-2 text-gray-500"><Earth class="text-lg"/><ItemMenu editText="Edytuj nazwę" removeText="Usuń" @edit="editOtherName(idx)" @remove="remove('other', idx)"/></div>
             </div>
          </div>
       </div>

       <div v-if="activeSection === 'other_add'" class="mt-2">
          <EditForm label="Nazwa" v-model="form.otherName" @cancel="close" @save="save('other')"/>
       </div>
       <button v-else @click="addOtherName" class="flex items-center text-blue-600 hover:underline font-medium">
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj pseudonim, nazwisko rodowe...
       </button>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Ulubione cytaty</h3>
      <div v-if="profileUser.favoriteQuotes" class="space-y-4 mb-4">
        <div v-for="(q, i) in profileUser.favoriteQuotes" :key="i">

           <div v-if="activeSection === 'quote_edit' && editingIndex === i">
              <EditForm label="Cytat" v-model="form.quote" @cancel="close" @save="save('quote')" />
           </div>

           <div v-else class="flex justify-between items-start">
              <div class="flex items-start text-gray-900"><FormatQuoteClose class="text-2xl text-gray-400 mt-1 mr-3"/><span class="italic">{{ q }}</span></div>
              <div class="flex items-center space-x-2 text-gray-500 ml-4"><Earth class="text-lg"/><ItemMenu editText="Edytuj cytat" removeText="Usuń" @edit="editQuote(i)" @remove="remove('quote', i)"/></div>
           </div>
        </div>
      </div>

      <div v-if="activeSection === 'quote_add'" class="mt-2">
         <EditForm label="Cytat" v-model="form.quote" @cancel="close" @save="save('quote')" />
      </div>
      <button v-else @click="addQuote" class="flex items-center text-blue-600 hover:underline font-medium">
         <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj ulubione cytaty
      </button>
    </div>

  </div>
</template>
