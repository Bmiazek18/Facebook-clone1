<template>
  <div class="bg-theme-bg-secondary p-3 rounded-2xl shadow-sm mb-6">
    <h2 class="text-[17px] font-bold text-theme-text">Nowe dla Ciebie</h2>

    <div
      class="flex items-center gap-3 hover:bg-theme-hover w-[40%] cursor-pointer px-1 py-3 rounded-lg transition"
    >
      <div class="relative w-12 h-12 flex-shrink-0">
        <img
          alt="Technikalia"
          class="w-full h-full rounded-full object-cover border border-theme-border"
        />
      </div>

      <div class="flex flex-col">
        <p class="text-[15px] text-theme-text leading-tight">
          Użytkownik <span class="font-bold hover:underline cursor-pointer">Technikalia</span>
          dodał zdjęcie w grupie
          <span class="font-bold hover:underline cursor-pointer">Technikalia.26</span>.
        </p>
        <span class="text-[13px] text-theme-text-secondary mt-0.5">3 dni temu</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CameraIcon from 'vue-material-design-icons/Camera.vue'
</script>
