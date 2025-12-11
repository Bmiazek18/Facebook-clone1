<script setup lang="ts">
import { computed } from 'vue';

// Definicja propsów
const props = defineProps<{
    friendsList: {
        name: string;
        mutual: number;
        isFriend: boolean;
        imageId: number;
    }[];
}>();

// Lista dla widoku 3x3 (9 elementów)
const miniListForPostsTab = computed(() => props.friendsList.slice(0, 9));

// Zakładając, że pełna liczba znajomych jest dostępna w innym miejscu,
// dla statycznego przykładu użyjemy liczby z obrazka
const totalFriendsCount = 541;

</script>

<template>
    <div class="bg-theme-bg-secondary p-4 mt-4 rounded-lg shadow-lg">

        <div class="flex justify-between items-baseline mb-2">
            <div>
                <h2 class="text-xl font-extrabold text-theme-text">Znajomi</h2>
                <div class="text-sm text-theme-text-secondary">{{ totalFriendsCount }} znajomych</div>
            </div>
            <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Pokaż wszystkich znajomych</a>
        </div>

        <div class="grid grid-cols-3 gap-2 mt-4">
            <div
                v-for="(friend, index) in miniListForPostsTab"
                :key="index"
                class="cursor-pointer"
            >
                <img
                    :src="`https://picsum.photos/id/${friend.imageId + 100}/150/150`"
                    class="w-full h-auto object-cover aspect-square rounded-lg"
                    :alt="friend.name"
                >
                <div class="mt-1 text-sm font-semibold text-theme-text truncate">
                    {{ friend.name }}
                </div>
            </div>
        </div>

    </div>
</template>
