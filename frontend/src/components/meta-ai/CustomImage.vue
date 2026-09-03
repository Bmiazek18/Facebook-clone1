<template>
  <div class="card-container">
    <div class="image-wrapper">
      <div v-if="!imageLoaded" class="loading-skeleton">
        <div class="ripple-effect"></div>
      </div>

      <img 
        v-show="imageLoaded"
        :src="imageSrc" 
        alt="Wygenerowany obraz" 
        class="final-image"
        @load="handleImageLoad"
      />

      <div v-if="imageLoaded" class="status-badge">
        <span class="badge-icon">✓</span> Obrazek wygenerowany pomyślnie
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps({
  imageSrc: {
    type: String,
    required: true,
    default: 'https://images.unsplash.com/photo-1547514701-42782101795e?q=80&w=600'
  }
})

const imageLoaded = ref(false)

const handleImageLoad = () => {
  imageLoaded.value = true
}
</script>

<style scoped>
.card-container {
  padding: 20px;
  border-radius: 24px;
  max-width: 500px;
  width: 100%;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #ffffff;
}

.image-wrapper {
  position: relative;
  aspect-ratio: 1 / 1;
  width: 100%;
  border-radius: 32px;
  overflow: hidden;
}

.loading-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #1a1a1a;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.ripple-effect {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, rgba(0,0,0,0) 70%);
  border-radius: 50%;
  animation: pulse 2s infinite ease-in-out;
}

@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 0.3;
  }
  50% {
    transform: scale(2.5);
    opacity: 1;
  }
  100% {
    transform: scale(4);
    opacity: 0;
  }
}

.final-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: opacity 0.5s ease-in-out;
}

.status-badge {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background-color: rgba(16, 24, 20, 0.85);
  border: 1px solid rgba(52, 211, 153, 0.2);
  backdrop-filter: blur(8px);
  color: #34d399;
  padding: 6px 14px;
  border-radius: 99px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.badge-icon {
  font-weight: bold;
}
</style>
