import { ref, watch } from 'vue';
import type { Ref } from 'vue';
import { getAllUsers, type User } from '@/data/users';

export function useUserSearch(searchTerm: Ref<string>) {
  const matchingUsers = ref<User[]>([]);

  watch(searchTerm, (newSearchTerm) => {
    const allUsers = getAllUsers();
    if (newSearchTerm) {
      matchingUsers.value = allUsers.filter(user =>
        user.name.toLowerCase().includes(newSearchTerm.toLowerCase())
      ).slice(0, 5);
    } else {
      matchingUsers.value = allUsers.slice(0, 5);
    }
  }, { immediate: true });

  return { matchingUsers };
}
