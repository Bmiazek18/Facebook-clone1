<script setup lang="ts">
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import Message from 'vue-material-design-icons/Message.vue';

const props = defineProps<{
    friend: {
        name: string;
        mutual: number;
        isFriend: boolean;
        imageId: number;
    };
}>();
</script>

<template>
    <div class="w-full md:w-1/2 px-2 mb-4">
        <div
            class="
                flex
                items-center
                justify-between
                p-3
                bg-white
                border border-gray-100

                rounded-lg

                transition-shadow duration-200
            "
        >

            <div class="flex items-center">
                <img
                    :src="`https://picsum.photos/id/${friend.imageId}/100/100`"
                    class="w-16 h-16 rounded-lg mr-3 object-cover border border-gray-100"
                    :alt="`Zdjęcie ${friend.name}`"
                >
                <div>
                    <div class="font-bold text-[17px] text-theme-text">{{ friend.name }}</div>
                    <div class="text-sm text-theme-text-secondary">{{ friend.mutual }} wspólnych znajomych</div>
                </div>
            </div>

            <div class="flex items-center space-x-2">
                <button v-if="friend.isFriend"
                    class="w-8 h-8 flex items-center justify-center hover:bg-gray-200 rounded-full text-theme-text-secondary"
                >
                    <DotsHorizontal :size="20"/>
                </button>

                <button v-else class="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1.5 rounded-lg font-bold whitespace-nowrap">
                    Dodaj znajomego
                </button>
            </div>
        </div>
    </div>
</template>
