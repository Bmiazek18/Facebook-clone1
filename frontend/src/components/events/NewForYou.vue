<template>
  <div class="bg-theme-bg-secondary p-3 rounded-2xl shadow-sm mb-6">
    <h2 class="text-[17px] font-bold text-theme-text">{{ $t('events.noweDlaCiebie') }}</h2>

    <div
      class="flex items-center gap-3 hover:bg-theme-hover w-[40%] cursor-pointer px-1 py-3 rounded-lg transition"
    >
      <div class="relative w-12 h-12 flex-shrink-0">
        <img
          :alt="$t('events.technikalia')"
          class="w-full h-full rounded-full object-cover border border-theme-border"
        />
      </div>

      <div class="flex flex-col">
        <p class="text-[15px] text-theme-text leading-tight">{{ $t('events.uzytkownik') }}<span class="font-bold hover:underline cursor-pointer">{{ $t('events.technikalia') }}</span>{{ $t('events.dodalZdjecieWGrupie') }}<span class="font-bold hover:underline cursor-pointer">{{ $t('events.technikalia26') }}</span>.
        </p>
        <span class="text-[13px] text-theme-text-secondary mt-0.5">{{ $t('events.3DniTemu') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CameraIcon from 'vue-material-design-icons/Camera.vue'
</script>
