<template>
  <div class="w-[190px] h-[300px] border border-gray-200 rounded-lg overflow-hidden shadow-sm flex flex-col bg-white">
    <div class="relative h-[190px] w-[190px]">
      <img
        :src="person.imageUrl"
        :alt="person.name"
        class="w-full h-full object-cover"
      />
      <button
        @click="$emit('remove', person.id)"
        class="absolute top-2 right-2 p-1 bg-black bg-opacity-40 rounded-full text-white hover:bg-opacity-60 transition"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-2 flex flex-col flex-grow">
      <h3 class="font-semibold text-gray-900 text-base ">{{ person.name }}</h3>

      <div class="flex items-center text-sm text-gray-600 ">
        <AccountGroupIcon :size="16" class="mr-1 text-blue-500" />
        <span v-if="person.commonFriends > 1">{{ person.commonFriends }} wspólnych znajomych</span>
        <span v-else-if="person.commonFriends === 1">{{'1 wspólny znajomy' }}</span>
        <span v-else class="text-gray-400">Brak wspólnych znajomych</span>
      </div>

      <button
        :disabled="person.isFriend"
        class="mt-2  py-2 px-3 text-sm font-semibold rounded transition duration-150"
        :class="{
            'bg-blue-600 text-white hover:bg-blue-700': !person.isFriend,
            'bg-gray-200 text-gray-500 cursor-not-allowed': person.isFriend,
        }"
      >
        <span v-if="!person.isFriend" class="flex items-center justify-center">
             <AccountPlusIcon :size="20" class="mr-1" fillColor="white" />
            Dodaj znajomego
        </span>
        <span v-else>Znajomy</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import { Person } from '../types/Person';

// --- IMPORT NOWYCH IKON ---
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';


const props = defineProps<{
  person: Person;
}>();

const emit = defineEmits<{
  (e: 'remove', id: number): void;
}>();
</script>
