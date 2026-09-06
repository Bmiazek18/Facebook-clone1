<script setup lang="ts">
export interface MentionMember {
  id: string
  name: string
  avatar: string
}

defineProps<{
  members: MentionMember[]
  selectedIndex: number
}>()

const emit = defineEmits<{
  'select': [member: MentionMember]
  'hover': [index: number]
}>()
</script>

<template>
  <div
    v-if="members.length > 0"
    class="absolute bottom-full left-0 mb-2 z-50 bg-white dark:bg-[#242526] rounded-2xl shadow-2xl border border-gray-200 dark:border-gray-700 overflow-hidden w-64 max-h-60 overflow-y-auto py-1 animate-in fade-in slide-in-from-bottom-2 duration-150"
  >
    <div class="px-3 py-1.5 text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider">
      Oznacz w grupie
    </div>
    <div
      v-for="(member, idx) in members"
      :key="member.id"
      @mousedown.prevent="emit('select', member)"
      @mouseenter="emit('hover', idx)"
      class="flex items-center gap-2.5 px-3 py-2 cursor-pointer transition-colors"
      :class="selectedIndex === idx ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400' : 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-800 dark:text-gray-200'"
    >
      <div class="w-7 h-7 rounded-full overflow-hidden bg-gray-200 shrink-0 flex items-center justify-center">
        <img
          v-if="member.id !== 'all'"
          :src="member.avatar"
          class="w-full h-full object-cover"
        />
        <span v-else class="text-xs font-bold text-gray-600">@</span>
      </div>
      <span class="text-sm font-semibold truncate">{{ member.name }}</span>
    </div>
  </div>
</template>
