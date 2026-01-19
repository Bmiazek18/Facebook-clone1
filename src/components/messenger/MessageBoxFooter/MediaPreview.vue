<template>
    <div v-if="imageUrl || gifUrl" class="p-2 mb-2 bg-gray-100 rounded-lg flex items-center justify-between">
        <div class="flex items-center space-x-2">
            <img :src="imageUrl || gifUrl || ''" :alt="gifUrl ? 'Wybrany GIF' : 'Wybrany obraz'" class="w-10 h-10 object-cover rounded" />
            <span class="text-xs text-gray-600 truncate">{{ gifUrl ? 'Wybrany GIF' : 'Gotowy do wysłania' }}</span>
        </div>
        <button @click="emit('clear-media')" class="text-red-500 hover:text-red-700 text-sm font-semibold">
            Usuń
        </button>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    imageUrl: string | null;
    gifUrl: string | null;
}>();

const emit = defineEmits<{
    'clear-media': [];
}>();
</script>
