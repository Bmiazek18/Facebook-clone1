<template>
  <div class="w-full bg-white border border-[#dadde1] rounded-lg overflow-hidden shadow-sm flex flex-col">

    <div class="relative w-full aspect-square bg-gray-200">
      <img
        :src="person.image"
        :alt="person.name"
        class="w-full h-full object-cover cursor-pointer hover:opacity-95 transition-opacity"
      />

      <button
        v-if="variant === 'suggestion'"
        @click="$emit('remove', person.id)"
        class="absolute top-2 right-2 p-1 bg-black bg-opacity-40 rounded-full text-white hover:bg-opacity-60 transition"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-3 flex flex-col flex-grow">
      <h3 class="text-[17px] font-semibold text-[#050505] mb-1 cursor-pointer hover:underline truncate">
        {{ person.name }}
      </h3>

      <div class="flex items-center text-[13px] text-[#65676b] mb-3 h-[20px]">
        <div class="flex -space-x-1 mr-2" v-if="person.mutual > 0 && variant === 'request'">
           <div class="w-4 h-4 rounded-full bg-red-500 border border-white"></div>
           <div class="w-4 h-4 rounded-full bg-blue-500 border border-white"></div>
        </div>

        <span v-if="person.mutual > 0">
          {{ person.mutual }} wspólnych znajomych
        </span>
        <span v-else>
          Brak wspólnych znajomych
        </span>
      </div>

      <div class="mt-auto flex flex-col gap-2">

        <template v-if="variant === 'request'">
          <button
            @click="$emit('confirm', person.id)"
            class="w-full bg-[#1b74e4] hover:bg-[#1562bd] text-white font-semibold text-[15px] py-[7px] rounded-md transition-colors"
          >
            Potwierdź
          </button>

          <button
            @click="$emit('delete', person.id)"
            class="w-full bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] font-semibold text-[15px] py-[7px] rounded-md transition-colors"
          >
            Usuń
          </button>
        </template>

        <template v-else>
          <button
            @click="$emit('add', person.id)"
            class="w-full bg-[#e7f3ff] hover:bg-[#dbe7f2] text-[#1877f2] font-semibold text-[15px] py-[7px] rounded-md transition-colors flex items-center justify-center"
          >
             <AccountPlusIcon :size="20" class="mr-1" />
            Dodaj znajomego
          </button>
        </template>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CloseIcon from 'vue-material-design-icons/Close.vue';
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue';

// Definicja interfejsu (możesz go przenieść do oddzielnego pliku types.ts)
export interface Person {
  id?: number | string;
  name: string;
  image: string;
  mutual: number; // Zmieniłem nazewnictwo na prostsze 'mutual' zgodnie z poprzednim krokiem
}

const props = withDefaults(defineProps<{
  person: Person;
  variant?: 'request' | 'suggestion'; // Nowy prop
}>(), {
  variant: 'request' // Domyślnie tryb zaproszenia
});

defineEmits<{
  (e: 'remove', id: number | string): void;
  (e: 'confirm', id: number | string): void;
  (e: 'delete', id: number | string): void;
  (e: 'add', id: number | string): void;
}>();
</script>
