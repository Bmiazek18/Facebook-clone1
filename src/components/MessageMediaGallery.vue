<script setup lang="ts">
defineProps<{
  mediaUrls: string[];
  isMe: boolean;
}>();

const emit = defineEmits<{
  (e: 'openLightbox', url: string): void;
}>();
</script>

<template>
  <div class="mb-2">
    <div
      class="grid gap-1 rounded-xl overflow-hidden"
      :class="{
        'grid-cols-2': mediaUrls.length >= 2,
        'grid-cols-1': mediaUrls.length === 1,
        'rounded-tl-none': !isMe,
        'rounded-br-none': isMe
      }"
    >
      <img
        v-for="(url, i) in mediaUrls"
        :key="i"
        :src="url"
        class="object-cover w-full h-full cursor-pointer"
        :class="{ 'aspect-square': mediaUrls.length > 1 }"
        @click="emit('openLightbox', url)"
        alt="Galeria zdjęć"
      />
    </div>
  </div>
</template>
