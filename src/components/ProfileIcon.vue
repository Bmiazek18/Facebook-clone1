<template>
  <div class="list-container py-6 fixed bottom-0 right-2">
    <div class="reversed-list">

      <transition-group name="list" tag="div" class="flex flex-col-reverse pb-3">

         <button
            v-for="boxId in chatStore.minimizedBoxCache"
            :key="boxId"
            @click="restoreMessageBox(boxId)"
            class="w-12 h-12 mt-3 shadow-md rounded-full bg-blue-600 text-white
                  flex items-center justify-center transition
                  duration-300 ease-in-out hover:bg-blue-700
                  focus:outline-none focus:ring-4 focus:ring-blue-500 focus:ring-opacity-50"
            :aria-label="'Restore Chat ' + boxId"
        >
             {{ boxId }}
        </button>

      </transition-group>
    </div>

     <button
        @click="addNewElement"
        class="w-12 h-12 rounded-full bg-[#46484c]
               shadow-xl flex items-center justify-center transition
               duration-300 ease-in-out hover:bg-gray-100 hover:shadow-2xl
               focus:outline-none focus:ring-4 focus:ring-blue-500 focus:ring-opacity-50"
        aria-label="Add New Element"
      >
        <PencilIcon :size="26" class="text-white" />
     </button>
  </div>
</template>

<script setup lang="ts" >
import { useChatStore } from '@/stores/counter';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';


const chatStore = useChatStore()

// Funkcja przywracająca boxa
const restoreMessageBox = (boxId: string | number) => {
    chatStore.toggleMinimize(boxId);
};


// Funkcja addNewElement musi teraz dodawać nowy ID do systemu czatu
const addNewElement = () => {
  // Generowanie unikalnego ID dla nowego czatu
  const newId = `chat-${Date.now()}`;
  // Używamy akcji z Pinia do dodania nowego boxa
  chatStore.addMessageBox(newId);
};

</script>

<style scoped>
/* Reszta stylów pozostaje bez zmian dla animacji */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.list-leave-active {
  position: absolute;
}
</style>
