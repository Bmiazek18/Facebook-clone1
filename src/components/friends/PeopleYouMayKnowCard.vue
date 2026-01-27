<template>
  <div class="w-full bg-theme-bg-secondary border border-theme-border rounded-lg overflow-hidden shadow-sm flex flex-col">

    <div class="relative w-full aspect-square bg-theme-bg-subtle">
      <img
        :src="person.imageUrl"
        :alt="person.name"
        class="w-full h-full object-cover cursor-pointer hover:opacity-95 transition-opacity"
      />

      <button
        v-if="variant === 'suggestion'"
        @click="$emit('remove', person.id)"
        class="absolute top-2 right-2 p-1 bg-black/40 rounded-full text-white hover:bg-black/60 transition"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-3 flex flex-col grow">
      <h3 class="text-[17px] font-semibold text-theme-text mb-1 cursor-pointer hover:underline truncate">
        {{ person.name }}
      </h3>

      <div class="flex items-center text-[13px] text-theme-text-secondary mb-3">
        <div class="flex -space-x-1 mr-2" v-if="person.commonFriends > 0 && variant === 'request'">
           <div class="w-4 h-4 rounded-full bg-red-500 border border-theme-bg-secondary"></div>
           <div class="w-4 h-4 rounded-full bg-theme-primary border border-theme-bg-secondary"></div>
        </div>

        <span v-if="person.commonFriends > 0">
          {{ person.commonFriends }} wspólnych znajomych
        </span>
        <span v-else>
          Brak wspólnych znajomych
        </span>
      </div>

      <div class="mt-auto flex flex-col gap-2">

        <template v-if="variant === 'request'">
          <button
            @click="$emit('confirm', person.id)"
            class="w-full bg-theme-primary hover:bg-theme-primary-hover text-white font-semibold text-[15px] py-[7px] rounded-md transition-colors"
          >
            Potwierdź
          </button>

          <button
            @click="$emit('delete', person.id)"
            class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text font-semibold text-[15px] py-[7px] rounded-md transition-colors"
          >
            Usuń
          </button>
        </template>

        <template v-else>
          <button
            @click="$emit('add', person.id)"
            class="w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-semibold text-[15px] py-[7px] rounded-md transition-colors flex items-center justify-center"
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
import type { Person } from '@/types/Person';

withDefaults(defineProps<{
  person: Person;
  variant?: 'request' | 'suggestion';
}>(), {
  variant: 'request'
});

defineEmits<{
  (e: 'remove', id: number): void;
  (e: 'confirm', id: number): void;
  (e: 'delete', id: number): void;
  (e: 'add', id: number): void;
}>();
</script>