<script setup lang="ts">
import Magnify from 'vue-material-design-icons/Magnify.vue';
import FriendListItem from './FriendListItem.vue';
// Zmieniona Definicja propsów: Został tylko friendsList i isFullView
defineProps<{
    friendsList: {
        name: string;
        mutual: number;
        isFriend: boolean;
        imageId: number;
    }[];
    isFullView: boolean; // false = Info Tab Preview, true = Full Friends Tab
}>();

</script>

<template>

    <div class="bg-theme-bg-secondary p-4 rounded-lg shadow-lg border border-gray-200">

        <div class="flex justify-between items-center mb-4 pb-3">
            <h2 :class="{'text-2xl font-extrabold': isFullView, 'text-xl font-extrabold': !isFullView, 'text-theme-text': true}">Znajomi</h2>

            <div v-if="isFullView" class="relative w-48">
                <input
                    type="text"
                    placeholder="Szukaj znajomych"
                    class="bg-gray-100 rounded-full py-2 pl-10 pr-4 w-full text-sm text-theme-text-secondary focus:outline-none focus:ring-2 focus:ring-blue-500 hover:bg-gray-200"
                >
                <Magnify :size="18" class="absolute left-3 top-2.5 text-gray-500"/>
            </div>

            <a v-if="!isFullView" class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Wszyscy znajomi</a>
        </div>



        <div class="flex space-x-6 mb-4 text-theme-text-secondary border-b overflow-x-auto whitespace-nowrap">
            <span class="font-bold border-b-4 border-b-blue-500 pb-3 text-blue-600 cursor-pointer text-[15px]">Wszyscy znajomi</span>
            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Urodziny</span>
            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Szkoła średnia</span>
            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Aktualne miejsce zamieszkania</span>
            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Obserwowani</span>
        </div>




        <div class="flex flex-wrap -mx-2 mt-4">
            <FriendListItem
                v-for="(friend, index) in friendsList"
                :key="index"
                :friend="friend"
            />
        </div>

        <button v-if="!isFullView" class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold mt-4 text-theme-text">
            Zobacz wszystko
        </button>
    </div>
</template>
