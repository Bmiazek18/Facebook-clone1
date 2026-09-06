<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  currentItem: {
    user: {
      avatar: string
      name: string
    }
  }
}>()

const { t } = useI18n()
</script>

<template>
  <div
    class="absolute inset-0 w-full h-full z-0 overflow-hidden flex flex-col items-center justify-center text-center select-none"
    style="background: radial-gradient(circle at center, #60a5fa 0%, #2563eb 100%)"
  >
    <div class="absolute inset-0 pointer-events-none opacity-80">
      <div
        class="absolute top-20 left-10 w-24 h-32 bg-green-400 rounded-[50%] rotate-[-15deg] shadow-lg animate-float-slow opacity-90"
      ></div>
      <div
        class="absolute top-40 right-10 w-28 h-36 bg-blue-500 rounded-[50%] rotate-[10deg] shadow-xl animate-float-fast"
      ></div>
      <div
        class="absolute bottom-32 left-10 w-20 h-26 bg-indigo-400 rounded-[50%] rotate-[5deg] shadow-lg blur-[1px] animate-float-slow"
      ></div>
    </div>

    <div class="relative z-0 flex flex-col items-center animate-scale-in">
      <div
        class="w-48 h-48 rounded-full border-4 border-white/20 p-1 mb-8 shadow-2xl bg-white/10 backdrop-blur-sm"
      >
        <img
          :src="currentItem.user.avatar"
          class="w-full h-full rounded-full object-cover shadow-inner"
          :alt="$t('feed.birthdayUser')"
        />
      </div>
      <h2 class="text-white text-2xl font-bold mb-2 drop-shadow-md px-4">
        {{ currentItem.user.name }} {{ t('story.hasBirthday') || 'ma dziś urodziny' }}
      </h2>
      <div class="text-4xl mb-4">🎉</div>
    </div>
  </div>
</template>

<style scoped>
@keyframes float {
  0%,
  100% {
    transform: translateY(0) rotate(var(--r, 0deg));
  }
  50% {
    transform: translateY(-20px) rotate(var(--r, 0deg));
  }
}

.animate-float-slow {
  animation: float 6s ease-in-out infinite;
  --r: -5deg;
}
.animate-float-medium {
  animation: float 5s ease-in-out infinite;
  animation-delay: 1s;
  --r: 5deg;
}
.animate-float-fast {
  animation: float 4s ease-in-out infinite;
  animation-delay: 2s;
  --r: 10deg;
}

@keyframes scale-in {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.animate-scale-in {
  animation: scale-in 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
</style>
