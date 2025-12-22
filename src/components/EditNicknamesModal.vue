<script setup lang="ts">
import { ref, watch } from 'vue';
import BaseModal from './BaseModal.vue';
import type { GroupMember } from '@/data/rawChats';

const props = defineProps<{
  members: GroupMember[];
  chatName: string;
}>();

const emit = defineEmits(['updateNicknames', 'close']);

const localMembers = ref<GroupMember[]>([]);

watch(() => props.members, (newMembers) => {
  localMembers.value = newMembers.map(member => ({ ...member }));
}, { immediate: true });

const saveNicknames = () => {
  emit('updateNicknames', localMembers.value);
  emit('close');
};

const cancelEdit = () => {
  emit('close');
};
</script>

<template>
  <BaseModal :title="`Edytuj nicki w ${chatName}`" @close="cancelEdit">
    <div class="p-4 max-h-[70vh] overflow-y-auto">
      <div v-for="member in localMembers" :key="member.id" class="flex items-center space-x-3 mb-4">
        <img :src="'https://i.pravatar.cc/150?img=' + member.id" class="w-10 h-10 rounded-full object-cover">
        <div class="flex-1">
          <p class="text-sm font-medium text-gray-700">{{ member.name }}</p>
          <input
            v-model="member.nickname"
            type="text"
            :placeholder="member.name"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          />
        </div>
      </div>
      <div class="mt-4 flex justify-end space-x-2">
        <button
          @click="cancelEdit"
          class="px-4 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600"
        >
          Anuluj
        </button>
        <button
          @click="saveNicknames"
          class="px-4 py-2 rounded-md bg-blue-500 text-white hover:bg-blue-600"
        >
          Zapisz
        </button>
      </div>
    </div>
  </BaseModal>
</template>