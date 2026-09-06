<script setup lang="ts">
interface Props {
  avatarUrl?: string
  showAvatar?: boolean
}

withDefaults(defineProps<Props>(), {
  showAvatar: true,
  avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80'
})
</script>

<template>
  <div class="flex items-end gap-2 my-2 select-none max-w-fit">
    <!-- Awatar użytkownika (opcjonalny) -->
    <div
      v-if="showAvatar"
      class="w-7 h-7 rounded-full overflow-hidden shrink-0 bg-gray-200 border border-gray-100"
    >
      <img :src="avatarUrl" :alt="$t('chat.avatar')" class="w-full h-full object-cover" />
    </div>

    <!-- Bąbelek z animacją -->
    <div
      class="bg-[#e4e6eb] dark:bg-[#3a3b3c] px-3.5 py-3 rounded-[18px] rounded-bl-sm inline-flex items-center gap-1 h-[32px]"
    >
      <span class="w-2 h-2 rounded-full bg-[#65676b] dark:bg-[#b0b3b8] inline-block animate-wave"></span>
      <span class="w-2 h-2 rounded-full bg-[#65676b] dark:bg-[#b0b3b8] inline-block animate-wave [animation-delay:0.2s]"></span>
      <span class="w-2 h-2 rounded-full bg-[#65676b] dark:bg-[#b0b3b8] inline-block animate-wave [animation-delay:0.4s]"></span>
    </div>
  </div>
</template>

<style scoped>
/* Customowa animacja unoszenia i zmiany przezroczystości */
.animate-wave {
  animation: messengerWave 1.3s infinite ease-in-out;
}

@keyframes messengerWave {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}
</style>
