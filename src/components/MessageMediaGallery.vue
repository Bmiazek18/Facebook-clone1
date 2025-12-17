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
  <div class="mb-2 overflow-visible">


    <div
      v-if="mediaUrls.length >= 2 && mediaUrls.length <= 3"
      class="relative w-[180px] cursor-pointer"
      :class="mediaUrls.length === 3 ? 'h-[260px]' : 'h-[200px]'"

    >
      <img
        :src="mediaUrls[0]"
        @click="emit('openLightbox', mediaUrls[0] ?? '')"
        class="absolute top-0 right-0 w-[110px] h-[110px] rounded-2xl object-cover bg-theme-bg ring-[3px] ring-white shadow-lg z-30"
        alt="Zdjęcie 1"
      />

      <img
        :src="mediaUrls[1]"
        @click="emit('openLightbox', mediaUrls[1] ?? '')"
        class="absolute top-[70px] left-0 w-[110px] h-[110px] rounded-2xl object-cover bg-theme-bg ring-[3px] ring-white shadow-lg z-20"
        alt="Zdjęcie 2"
      />

      <template v-if="mediaUrls.length === 3">
        <img
        @click="emit('openLightbox', mediaUrls[2] ?? '')"
          :src="mediaUrls[2]"
          class="absolute bottom-0 right-0 w-[110px] h-[110px] rounded-2xl object-cover bg-theme-bg ring-[3px] ring-white shadow-lg z-10"
          alt="Zdjęcie 3"
        />

      </template>


    </div>

    <div v-else class="relative w-[140px] h-[100px] cursor-pointer">
      <img
        @click="emit('openLightbox', mediaUrls[0] ?? '')"
        :src="mediaUrls[0]"
        class="absolute top-0 left-0 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 ring-white z-10"
        :style="{ transform: 'rotate(-6deg)' }"
        alt="Zdjęcie 1"
      />
      <img
        @click="emit('openLightbox', mediaUrls[1] ?? '')"
        :src="mediaUrls[1]"
        class="absolute top-2 left-8 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 ring-white z-20"
        :style="{ transform: 'rotate(4deg)' }"
        alt="Zdjęcie 2"
      />
      <img
        @click="emit('openLightbox', mediaUrls[2] ?? '')"
        v-if="mediaUrls.length >= 3"
        :src="mediaUrls[2]"
        class="absolute -top-2 left-4 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 ring-white z-0"
        :style="{ transform: 'rotate(-2deg)' }"
        alt="Zdjęcie 3"
      />
      <span class="absolute bottom-0 right-0 bg-black/60 text-white text-xs px-2 py-0.5 rounded-full z-30">
        {{ mediaUrls.length }}
      </span>
    </div>
  </div>
</template>
