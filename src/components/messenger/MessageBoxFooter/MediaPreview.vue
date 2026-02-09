<template>
  <transition name="fade">
    <div v-if="imageUrls.length || gifUrl" class="px-2 pt-2 flex flex-wrap gap-2">
      <div v-for="(url, index) in imageUrls" :key="index" class="relative group inline-block">
        <img
          :src="url"
          alt="Wybrany obraz"
          class="w-16 h-16 object-cover rounded-xl border border-theme-border shadow-sm"
        />

        <button
          @click="$emit('clear-media', index)"
          class="absolute -top-2 -right-2 bg-white text-gray-800 rounded-full w-6 h-6 flex items-center justify-center shadow-md border border-gray-200 hover:bg-gray-100 transition-colors"
          title="Usuń"
        >
          <span class="text-lg leading-none">&times;</span>
        </button>
      </div>
      <div v-if="gifUrl" class="relative group inline-block">
        <img
          :src="gifUrl"
          alt="Wybrany GIF"
          class="w-16 h-16 object-cover rounded-xl border border-theme-border shadow-sm"
        />

        <button
          @click="$emit('clear-gif')"
          class="absolute -top-2 -right-2 bg-white text-gray-800 rounded-full w-6 h-6 flex items-center justify-center shadow-md border border-gray-200 hover:bg-gray-100 transition-colors"
          title="Usuń"
        >
          <span class="text-lg leading-none">&times;</span>
        </button>

        <div class="absolute bottom-1 left-1 bg-black/50 text-white text-[10px] px-1 rounded uppercase font-bold pointer-events-none">
          GIF
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
defineProps<{
    imageUrls: string[];
    gifUrl: string | null;
}>();

defineEmits<{
    'clear-media': [index: number];
    'clear-gif': [];
}>();
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(5px);
}
</style>