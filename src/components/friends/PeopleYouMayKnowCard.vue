<template>
  <div class="w-full bg-theme-bg-secondary border border-theme-border rounded-xl overflow-hidden shadow-sm flex flex-col transition-shadow duration-200">

    <div class="relative w-full aspect-square shrink-0">
      <img
        :src="person.imageUrl"
        :alt="person.name"
        class="w-full h-full object-cover object-top cursor-pointer "
      />

      <button
        v-if="variant === 'suggestion'"
        @click="$emit('remove', person.id)"
        class="absolute top-3 right-3 p-2 bg-black/50 hover:bg-black/70 rounded-full text-white transition backdrop-blur-sm"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-2.5 flex flex-col grow">

      <h3 class="text-[17px] leading-tight font-semibold text-theme-text mb-1 cursor-pointer hover:underline truncate">
        {{ person.name }}
      </h3>

      <div class="flex items-center text-[13px] text-theme-text-secondary mb-2">
        <div class="flex shrink-0 mr-2" v-if="person.commonFriends > 0">
           <div class="w-5 h-5 rounded-full bg-theme-border flex items-center justify-center overflow-hidden">
             <img v-if="person.imageUrl" :src="person.imageUrl" class="w-full h-full " />
           </div>
        </div>

        <span v-if="person.commonFriends > 0" class="truncate">
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
            class="w-full bg-theme-primary hover:bg-theme-primary-hover text-white font-bold text-[15px] py-1.5 rounded-lg transition-colors"
          >
            Potwierdź
          </button>

          <button
            @click="$emit('delete', person.id)"
            class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text font-bold text-[15px] py-1.5 rounded-lg transition-colors"
          >
            Usuń
          </button>
        </template>

        <template v-else>
          <button
            @click="$emit('add', person.id)"
            class="w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-bold text-[12px] py-1 rounded-lg transition-colors flex items-center justify-center"
          >
             <AccountPlusIcon :size="16" class="mr-1.5" />
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
  variant: 'suggestion'
});

defineEmits<{
  (e: 'remove', id: number): void;
  (e: 'confirm', id: number): void;
  (e: 'delete', id: number): void;
  (e: 'add', id: number): void;
}>();
</script>
