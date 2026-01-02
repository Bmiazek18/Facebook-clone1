import { ref, watch, computed } from 'vue';
import type { Ref } from 'vue';
import { getAllUsers, type User } from '@/data/users';

export function useUserTagging() {
    const searchTerm = ref<string | null>(null);
    const matchingUsers = ref<User[]>([]);
    const showUserDropdown = computed(() => matchingUsers.value.length > 0 && searchTerm.value !== null);

    watch(searchTerm, (newSearchTerm) => {
        if (newSearchTerm === null) {
            matchingUsers.value = [];
            return;
        }

        const allUsers = getAllUsers();
        if (newSearchTerm === '') {
            matchingUsers.value = allUsers.slice(0, 5);
        } else {
            matchingUsers.value = allUsers.filter(user =>
                user.name.toLowerCase().includes(newSearchTerm.toLowerCase())
            ).slice(0, 5);
        }
    });

    function triggerUserTagging(textBeforeCaret: string) {
        const match = textBeforeCaret.match(/@([^\s]*)$/);
        if (match) {
            searchTerm.value = match[1];
        } else {
            searchTerm.value = null;
        }
    }

    function selectUser(contentRef: Ref<string>, user: User) {
        const currentContent = contentRef.value;
        const newContent = currentContent.replace(/@([^\s]*)$/, `[@${user.id}] `);
        contentRef.value = newContent;
        searchTerm.value = null;
    }

    return {
        matchingUsers,
        showUserDropdown,
        triggerUserTagging,
        selectUser,
        searchTerm, // Also return searchTerm to be able to reset it if needed
    };
}
