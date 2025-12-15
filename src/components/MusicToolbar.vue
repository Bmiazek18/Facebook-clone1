<script setup lang="ts">
import Delete from 'vue-material-design-icons/Delete.vue';
import ViewAgenda from 'vue-material-design-icons/ViewAgenda.vue'; // Duży widok
import ViewList from 'vue-material-design-icons/ViewList.vue';     // Mały widok
import TextBox from 'vue-material-design-icons/TextBox.vue';       // Tekst
import MusicNote from 'vue-material-design-icons/MusicNote.vue';   // Ikona

defineProps<{
  trackTitle?: string;
  trackArtist?: string;
  coverUrl?: string;
  currentStyle: 'large' | 'small' | 'text' | 'icon';
}>();

const emit = defineEmits<{
  (e: 'update:style', style: 'large' | 'small' | 'text' | 'icon'): void;
  (e: 'remove'): void;
}>();
</script>

<template>
  <div class="absolute -right-[115px] top-15 mt-20 bg-white rounded-2xl shadow-2xl p-4 w-[320px] z-[900] animate-pop">
    <div class="flex items-start justify-between mb-4 border-b border-gray-100 pb-3">
        <div class="flex items-center gap-3">
            <img v-if="coverUrl" :src="coverUrl" class="w-10 h-10 rounded-md object-cover border border-gray-200" />
            <div class="flex flex-col overflow-hidden max-w-[160px]">
                <span class="text-sm font-bold text-gray-900 truncate">{{ trackTitle }}</span>
                <span class="text-xs text-gray-500 truncate">{{ trackArtist }}</span>
            </div>
        </div>
        <button @click="emit('remove')" class="text-red-500 text-sm font-semibold hover:text-red-600 transition">
            Usuń
        </button>
    </div>

    <div class="flex justify-between gap-2">
        <button
            @click="emit('update:style', 'large')"
            class="w-12 h-12 rounded-full flex items-center justify-center transition"
            :class="currentStyle === 'large' ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
        >
            <ViewAgenda />
        </button>

        <button
            @click="emit('update:style', 'small')"
            class="w-12 h-12 rounded-full flex items-center justify-center transition"
            :class="currentStyle === 'small' ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
        >
            <ViewList />
        </button>

        <button
            @click="emit('update:style', 'text')"
            class="w-12 h-12 rounded-full flex items-center justify-center transition"
            :class="currentStyle === 'text' ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
        >
            <TextBox />
        </button>

        <button
            @click="emit('update:style', 'icon')"
            class="w-12 h-12 rounded-full flex items-center justify-center transition"
            :class="currentStyle === 'icon' ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
        >
            <MusicNote />
        </button>
    </div>
  </div>
</template>

<style scoped>
.animate-pop { animation: popIn 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
@keyframes popIn {
    from { opacity: 0; transform: translate(-50%, -40%) scale(0.9); }
    to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
}
</style>
