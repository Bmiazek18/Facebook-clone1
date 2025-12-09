<template>
  <div class="w-[190px] h-[300px] border border-gray-200 rounded-lg overflow-hidden shadow-sm flex flex-col bg-theme-bg-secondary">
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

    <div class="p-2 flex flex-col grow">
      <h3 class="font-semibold text-theme-color text-base ">{{ person.name }}</h3>

      <div class="flex items-center text-sm text-theme-text-secondary">
        <AccountGroupIcon :size="16" class="mr-1 text-blue-500" />
        <span v-if="person.commonFriends > 1">{{ person.commonFriends }} {{ $t('profile.commonFriends') }}</span>
        <span v-else-if="person.commonFriends === 1">{{ $t('profile.oneCommonFriend') }}</span>
        <span v-else class="text-gray-400">{{ $t('profile.noCommonFriends') }}</span>
      </div>

      <button
        :disabled="person.isFriend"
        class="mt-2  px-3 text-sm font-semibold rounded transition duration-150"
        :class="{
            'bg-blue-600 text-white hover:bg-blue-700': !person.isFriend,
            'bg-gray-200 text-gray-500 cursor-not-allowed': person.isFriend,
        }"
      >
        <span v-if="!person.isFriend" class="flex items-center justify-center">
             <AccountPlusIcon :size="20" class="mr-1" fillColor="white" />
            {{ $t('profile.addFriend') }}
        </span>
        <span v-else>{{ $t('profile.isFriend') }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { Person } from '../types/Person';

// i18n
useI18n()

// --- IMPORT NOWYCH IKON ---
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';


defineProps<{
  person: Person;
}>();

defineEmits<{
  (e: 'remove', id: number): void;
}>();
</script>
