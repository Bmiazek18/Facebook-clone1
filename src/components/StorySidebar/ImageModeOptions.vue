<template>
    <div class="flex flex-col mt-2 pb-4">
        <div @click="emit('add-text')" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition active:scale-95">
            <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300">
                <FormatFont :size="24" class="text-gray-700" />
            </div>
            <span class="font-medium text-gray-700 text-sm">Dodaj tekst</span>
        </div>

        <div @click="emit('toggle-music')" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition" :class="{'bg-blue-50 border-l-4 border-blue-500': isMusicModalOpen}">
            <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300">
                <MusicNote :size="24" class="text-gray-700" />
            </div>
            <span class="font-medium text-gray-700 text-sm">Dodaj muzykę</span>
        </div>

        <!-- Link Sticker -->
        <div @click="emit('add-link')" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition active:scale-95">
            <div class="bg-linear-to-r from-blue-500 to-purple-500 p-2.5 rounded-full">
                <Link :size="24" class="text-white" />
            </div>
            <span class="font-medium text-gray-700 text-sm">Dodaj naklejkę z linkiem</span>
        </div>

        <!-- Share Post -->
        <div @click="emit('share-post')" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition active:scale-95">
            <div class="bg-green-500 p-2.5 rounded-full">
                <ShareVariant :size="24" class="text-white" />
            </div>
            <span class="font-medium text-gray-700 text-sm">Udostępnij post</span>
        </div>

        <div @click="toggleAltTextSection" class="flex items-center justify-between px-4 py-3 hover:bg-gray-100 cursor-pointer transition" :class="{'bg-gray-50': isAltTextExpanded}">
            <div class="flex items-center gap-4">
                <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300">
                    <AlphaABox :size="24" class="text-gray-700" />
                </div>
                <span class="font-medium text-gray-700 text-sm">Tekst alternatywny</span>
            </div>
        </div>

        <AltTextEditor v-if="isAltTextExpanded" @save-alt-text="(text) => emit('save-alt-text', text)" />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import FormatFont from 'vue-material-design-icons/FormatFont.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';
import Link from 'vue-material-design-icons/Link.vue';
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue';
import AlphaABox from 'vue-material-design-icons/AlphaABox.vue';
import AltTextEditor from './AltTextEditor.vue';

const props = defineProps<{
    isMusicModalOpen: boolean;
    isImageSelected: boolean;
}>();

const emit = defineEmits<{
    (e: 'add-text'): void;
    (e: 'toggle-music'): void;
    (e: 'add-link'): void;
    (e: 'share-post'): void;
    (e: 'save-alt-text', text: string): void;
}>();

const isAltTextExpanded = ref(false);

const toggleAltTextSection = () => {
    if (!props.isImageSelected) {
        alert("Wybierz zdjęcie, aby edytować tekst alternatywny.");
        return;
    }
    isAltTextExpanded.value = !isAltTextExpanded.value;
};
</script>
