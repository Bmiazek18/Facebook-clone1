<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { User } from '@/data/users'
import ItemMenu from './ItemMenu.vue'
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Heart from 'vue-material-design-icons/Heart.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import AccountCircle from 'vue-material-design-icons/AccountCircle.vue'

defineProps<{ profileUser: User }>()
const allUsers = [{ id: 1, name: 'Anna Nowak', avatar: '' }, { id: 2, name: 'Piotr Kowalski', avatar: '' }]
const activeSection = ref<string | null>(null)
const editingIndex = ref<number | null>(null)
const showDropdown = ref(false)
const form = reactive({ status: 'Wolny', familyName: '', relation: 'Brat' })
const filteredUsers = computed(() => form.familyName ? allUsers.filter(u => u.name.toLowerCase().includes(form.familyName.toLowerCase())) : [])
const close = () => { activeSection.value = null; editingIndex.value = null; showDropdown.value = false }
const save = () => close()
const selectUser = (name: string) => { form.familyName = name; showDropdown.value = false }

const editFamily = (idx: number, member: any) => {
    form.familyName = member.name; form.relation = member.relationship
    editingIndex.value = idx; activeSection.value = 'fam_edit'
}
const addFamily = () => { form.familyName = ''; form.relation = 'Brat'; activeSection.value = 'fam_add' }
const openRel = () => activeSection.value = 'rel'
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Związek</h3>

      <div v-if="activeSection === 'rel'" class="bg-gray-50 p-4 rounded-lg mb-4">
         <div class="relative bg-white border border-gray-300 rounded-lg px-3 py-2 mb-4">
             <select v-model="form.status" class="w-full bg-transparent outline-none"><option>Wolny</option><option>W związku</option></select>
         </div>
         <div class="flex justify-end space-x-2"><button @click="close" class="px-3 py-1 bg-gray-200 rounded">Anuluj</button><button @click="save" class="px-3 py-1 bg-blue-600 text-white rounded">Zapisz</button></div>
      </div>

      <div v-else-if="profileUser.relationshipStatus" class="flex justify-between items-center mb-4">
         <div class="flex items-center text-gray-900"><div class="mr-3 w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center"><Heart class="text-xl"/></div><div><strong>{{ profileUser.relationshipStatus }}</strong></div></div>
         <div class="flex items-center space-x-2 text-gray-500"><Earth class="text-lg"/><ItemMenu editText="Edytuj" removeText="Usuń" @edit="openRel" @remove="close"/></div>
      </div>

      <button v-if="!profileUser.relationshipStatus && activeSection !== 'rel'" @click="openRel" class="flex items-center text-blue-600 hover:underline font-medium"><PlusCircleOutline class="mr-3 text-2xl" /> Dodaj status związku</button>
    </div>

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Członkowie rodziny</h3>
      <div v-for="(member, idx) in profileUser.familyMembers" :key="idx">

         <div v-if="activeSection === 'fam_edit' && editingIndex === idx" class="bg-gray-50 p-4 rounded-lg mb-4">
             <div class="mb-3 border-2 border-blue-500 rounded-lg px-3 py-2 bg-white"><input v-model="form.familyName" class="w-full outline-none" placeholder="Imię"/></div>
             <div class="bg-gray-200 rounded-lg px-3 py-2 mb-4"><select v-model="form.relation" class="w-full bg-transparent outline-none"><option>Brat</option><option>Siostra</option></select></div>
             <div class="flex justify-end space-x-2"><button @click="close" class="px-3 py-1 bg-gray-200 rounded">Anuluj</button><button @click="save" class="px-3 py-1 bg-blue-600 text-white rounded">Zapisz</button></div>
         </div>

         <div v-else class="flex justify-between items-center mb-4">
            <div class="flex items-center text-gray-900">
                <div class="mr-3 w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center"><AccountGroup class="text-xl"/></div>
                <div><div class="font-bold text-lg">{{ member.name }}</div><div class="text-sm text-gray-500">{{ member.relationship }}</div></div>
            </div>
            <div class="flex items-center space-x-2 text-gray-500"><Earth class="text-lg"/><ItemMenu editText="Edytuj" removeText="Usuń" @edit="editFamily(idx, member)" @remove="close"/></div>
         </div>
      </div>

      <div v-if="activeSection === 'fam_add'" class="bg-gray-50 p-4 rounded-lg mt-2 relative">
         <div class="mb-3 border-2 border-blue-500 rounded-lg px-3 py-2 bg-white relative">
            <input v-model="form.familyName" class="w-full outline-none" placeholder="Imię" @input="showDropdown=true" @blur="setTimeout(()=>showDropdown=false, 200)"/>
            <ul v-if="showDropdown && filteredUsers.length" class="absolute left-0 right-0 top-full bg-white shadow-lg border rounded z-50"><li v-for="u in filteredUsers" :key="u.id" @click="selectUser(u.name)" class="p-2 hover:bg-gray-100 cursor-pointer">{{ u.name }}</li></ul>
         </div>
         <div class="bg-gray-200 rounded-lg px-3 py-2 mb-4"><select v-model="form.relation" class="w-full bg-transparent outline-none"><option>Brat</option><option>Siostra</option></select></div>
         <div class="flex justify-end space-x-2"><button @click="close" class="px-3 py-1 bg-gray-200 rounded">Anuluj</button><button @click="save" class="px-3 py-1 bg-blue-600 text-white rounded">Zapisz</button></div>
      </div>
      <button v-else @click="addFamily" class="flex items-center text-blue-600 hover:underline font-medium"><PlusCircleOutline class="mr-3 text-2xl" /> Dodaj członka rodziny</button>
    </div>
  </div>
</template>
