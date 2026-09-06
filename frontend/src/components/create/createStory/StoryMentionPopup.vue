<script setup lang="ts">
export interface MentionUser {
  id: string
  name: string
  subtitle: string
  avatar: string
}

defineProps<{
  show: boolean
  mentions: MentionUser[]
  position: { top: number; left: number }
  canvasWidth: number
  activeIndex: number
}>()

const emit = defineEmits<{
  select: [user: { id: string; name: string }]
}>()
</script>

<template>
  <div
    v-if="show && mentions.length > 0"
    class="absolute z-[100] bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] flex flex-col gap-1 w-[280px] pointer-events-auto"
    :style="{
      top: position.top + 'px',
      left: Math.max(10, Math.min(position.left, canvasWidth - 290)) + 'px',
    }"
  >
    <div
      v-for="(user, idx) in mentions"
      :key="user.id"
      @mousedown.prevent="emit('select', user)"
      class="flex items-center gap-3 p-1.5 rounded-xl cursor-pointer border-[2px]"
      :class="
        idx === activeIndex ? 'border-blue-600' : 'border-transparent hover:bg-[#F0F2F5]'
      "
    >
      <!-- Avatar użytkownika -->
      <div
        class="w-11 h-11 rounded-full bg-[#E4E6EB] flex items-center justify-center overflow-hidden shrink-0 border border-black/5"
      >
        <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
        <svg
          v-else
          class="w-8 h-8 text-[#B0B3B8] mt-2"
          viewBox="0 0 24 24"
          fill="currentColor"
        >
          <path
            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
          />
        </svg>
      </div>
      <!-- Dane użytkownika -->
      <div class="flex flex-col justify-center min-w-0">
        <span class="font-bold text-[15px] text-[#050505] leading-tight truncate">{{
          user.name
        }}</span>
        <span class="text-[13px] text-[#65676B] leading-tight truncate mt-0.5">{{
          user.subtitle
        }}</span>
      </div>
    </div>
  </div>
</template>
