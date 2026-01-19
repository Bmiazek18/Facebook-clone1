<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { User } from '@/data/users'
import EditForm from './EditForm.vue'
import ItemMenu from './ItemMenu.vue'

// Ikony
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Star from 'vue-material-design-icons/Star.vue'
import Earth from 'vue-material-design-icons/Earth.vue'

defineProps<{ profileUser: User }>()

// Stan: Przechowuje nazwę aktualnie otwartej sekcji
const activeSection = ref<string | null>(null)

// Dane formularza
const form = reactive({
  eventName: '',
  date: ''
})

const open = (section: string) => activeSection.value = section
const close = () => activeSection.value = null

const save = (section: string) => {
  console.log(`Zapisano ${section}:`, form)
  close()
}

const remove = (section: string, index: number) => {
  if(confirm(`Czy na pewno chcesz usunąć to wydarzenie?`)) {
    console.log(`Usunięto wydarzenie z indeksu: ${index}`)
  }
}
</script>

<template>
  <div class="space-y-8 text-base">

    <div>
      <h3 class="font-bold text-xl text-black mb-4">Wydarzenia z życia</h3>

      <div v-if="profileUser.lifeEvents && profileUser.lifeEvents.length > 0" class="space-y-4 mb-4">
        <div v-for="(event, idx) in profileUser.lifeEvents" :key="idx" class="flex justify-between items-start">
          <div class="flex items-center text-gray-900">
            <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mr-3 text-gray-500">
               <Star class="text-xl" />
            </div>
            <div>
              <div class="font-bold text-lg leading-tight">{{ event.event }}</div>
              <div class="text-sm text-gray-500">
                {{ new Date(event.date).toLocaleDateString('pl-PL', { year: 'numeric', month: 'long', day: 'numeric' }) }}
              </div>
            </div>
          </div>

          <div class="flex items-center space-x-2 text-gray-500">
            <Earth class="text-lg"/>
            <ItemMenu
              editText="Edytuj wydarzenie"
              removeText="Usuń wydarzenie"
              @edit="open('lifeEvent')"
              @remove="remove('lifeEvent', idx)"
            />
          </div>
        </div>
      </div>

      <div v-else-if="!activeSection" class="text-gray-500 mb-4 flex items-center">
         <Star class="text-gray-400 text-xl mr-3" /> Brak wydarzeń do wyświetlenia
      </div>

      <div class="mt-2">
        <EditForm
          v-if="activeSection === 'lifeEvent'"
          label="Tytuł wydarzenia"
          placeholder="Np. Ślub, Nowa praca"
          v-model="form.eventName"
          @cancel="close"
          @save="save('lifeEvent')"
        />

        <button
          v-else
          @click="open('lifeEvent')"
          class="flex items-center text-blue-600 hover:underline font-medium"
        >
          <PlusCircleOutline class="mr-3 text-2xl" /> Dodaj wydarzenie z życia
        </button>
      </div>
    </div>

  </div>
</template>
