import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groups as groupsData } from '@/data/groups';
import type { Group } from '@/types/Group';

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref<Group[]>(groupsData);

  const getGroupById = (id: string) => {
    return groups.value.find((group) => group.id === id);
  };

  return {
    groups,
    getGroupById,
  };
});
