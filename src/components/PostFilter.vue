<script setup lang="ts">
import { ref } from 'vue';

// Import ikon (upewnij się, że masz je zainstalowane, lub podmień na swoje)
import Tune from 'vue-material-design-icons/Tune.vue';
import Cog from 'vue-material-design-icons/Cog.vue';
import FormatListBulleted from 'vue-material-design-icons/FormatListBulleted.vue';
import ViewGrid from 'vue-material-design-icons/ViewGrid.vue';

const activeView = ref('list'); // 'list' lub 'grid'

const setView = (view: string) => {
    activeView.value = view;
};
</script>

<template>
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 mt-4">

        <div class="flex items-center justify-between p-4 pb-2">
            <h2 class="text-[20px] font-bold text-[#050505]">Posty</h2>

            <div class="flex items-center space-x-2">
                <button class="flex items-center px-3 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md transition-colors text-[#050505] font-semibold text-[15px]">
                    <Tune :size="20" class="mr-1.5" />
                    Filtry
                </button>
                <button class="flex items-center px-3 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md transition-colors text-[#050505] font-semibold text-[15px]">
                    <Cog :size="20" class="mr-1.5" />
                    Zarządzaj postami
                </button>
            </div>
        </div>

        <div class="flex border-t border-gray-200 mt-1">

            <button
                @click="setView('list')"
                class="flex-1 h-[50px] flex items-center justify-center font-semibold text-[15px] relative hover:bg-gray-50 transition-colors rounded-bl-lg"
                :class="activeView === 'list' ? 'text-[#1877F2]' : 'text-[#65676B]'"
            >
                <FormatListBulleted :size="20" class="mr-2" />
                Widok listy
                <div v-if="activeView === 'list'" class="absolute bottom-[-1px] left-0 w-full h-[3px] bg-[#1877F2]"></div>
            </button>

            <button
                @click="setView('grid')"
                class="flex-1 h-[50px] flex items-center justify-center font-semibold text-[15px] relative hover:bg-gray-50 transition-colors rounded-br-lg"
                :class="activeView === 'grid' ? 'text-[#1877F2]' : 'text-[#65676B]'"
            >
                <ViewGrid :size="20" class="mr-2" />
                Widok siatki
                <div v-if="activeView === 'grid'" class="absolute bottom-[-1px] left-0 w-full h-[3px] bg-[#1877F2]"></div>
            </button>

        </div>
    </div>
</template>
