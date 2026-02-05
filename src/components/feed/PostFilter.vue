<script setup lang="ts">
import { ref } from 'vue';
import BaseModal from '@/components/common/BaseModal.vue';
import PostFilterModal from './PostFilterModal.vue';

// Define emits
const emits = defineEmits(['view-changed']);

// Import ikon
import Tune from 'vue-material-design-icons/Tune.vue';
import FormatListBulleted from 'vue-material-design-icons/FormatListBulleted.vue';
import ViewGrid from 'vue-material-design-icons/ViewGrid.vue';

const activeView = ref('list'); // 'list' lub 'grid'
const isFilterModalOpen = ref(false);

const setView = (view: string) => {
    activeView.value = view;
    emits('view-changed', view);
};

const toggleFilterModal = () => {
  isFilterModalOpen.value = !isFilterModalOpen.value;
};
</script>

<template>
    <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border mt-4">

        <div class="flex items-center justify-between p-4 pb-2">
            <h2 class="text-[20px] font-bold text-theme-text">Posty</h2>

            <div class="flex items-center space-x-2">
                <button class="flex items-center px-3 py-1.5 bg-theme-bg-third hover:bg-theme-hover rounded-md transition-colors text-theme-text font-semibold text-[15px]"
                    @click="toggleFilterModal">
                    <Tune :size="20" class="mr-1.5" />
                    Filtry
                </button>
                <button class="flex items-center px-3 py-1.5 bg-theme-bg-third hover:bg-theme-hover rounded-md transition-colors text-theme-text font-semibold text-[15px]">

                    Zarządzaj postami
                </button>
            </div>
        </div>

        <div class="flex border-t border-theme-border mt-1">

            <button
                @click="setView('list')"
                class="flex-1 h-[50px] flex items-center justify-center font-semibold text-[15px] relative hover:bg-theme-hover transition-colors rounded-bl-lg"
                :class="activeView === 'list' ? 'text-blue-500' : 'text-theme-text-secondary'"
            >
                <FormatListBulleted :size="20" class="mr-2" />
                Widok listy
                <div v-if="activeView === 'list'" class="absolute bottom-[-1px] left-0 w-full h-[3px] bg-blue-500"></div>
            </button>

            <button
                @click="setView('grid')"
                class="flex-1 h-[50px] flex items-center justify-center font-semibold text-[15px] relative hover:bg-theme-hover transition-colors rounded-br-lg"
                :class="activeView === 'grid' ? 'text-blue-500' : 'text-theme-text-secondary'"
            >
                <ViewGrid :size="20" class="mr-2" />
                Widok siatki
                <div v-if="activeView === 'grid'" class="absolute bottom-[-1px] left-0 w-full h-[3px] bg-blue-500"></div>
            </button>

        </div>
    </div>

    <BaseModal v-if="isFilterModalOpen" @close="toggleFilterModal" title="Filtry Postów">
      <PostFilterModal />
    </BaseModal>
</template>
