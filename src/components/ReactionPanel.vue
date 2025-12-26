<template>
  <div v-if="totalReactions > 0" class="w-full">

    <div class="flex overflow-x-auto whitespace-nowrap mb-4 border-b border-gray-200">
      <button
        @click="selectTab(null)"
        :class="['px-4 py-2 text-sm font-medium border-b-2 transition-colors duration-200 focus:outline-none', {
          'border-blue-600 text-blue-600 font-semibold': selectedReaction === null,
          'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700': selectedReaction !== null,
        }]"
      >
        Wszystkie {{ totalReactions }}
      </button>

      <button
        v-for="(count, emoji) in reactionSummary"
        :key="emoji"
        @click="selectTab(emoji as string)"
        :class="['flex items-center px-4 py-2 text-sm font-medium border-b-2 transition-colors duration-200 focus:outline-none', {
          'border-blue-600 text-blue-600 font-semibold': selectedReaction === emoji,
          'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700': selectedReaction !== emoji,
        }]"
      >
        <span class="text-xl mr-1">{{ emoji }}</span>
        <span>{{ count }}</span>
      </button>
    </div>

    <HoverScrollbar class="max-h-[300px]"> <div
        v-for="reaction in filteredReactions"
        :key="`${reaction.userId}-${reaction.emoji}`"
        class="flex items-center justify-between px-6 py-2 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors"
      >
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center overflow-hidden shrink-0 shadow-sm">
            <img
              v-if="reaction.avatarUrl"
              :src="reaction.avatarUrl"
              :alt="reaction.userName"
              class="w-full h-full object-cover"
            >
            <span v-else class="text-sm font-bold text-white uppercase">
              {{ reaction.userName.charAt(0) }}
            </span>
          </div>

          <div class="text-gray-800 font-medium truncate">
            {{ reaction.userName }}
          </div>
        </div>

        <span class="text-2xl shrink-0 select-none">{{ reaction.emoji }}</span>
      </div>
    </HoverScrollbar>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

import HoverScrollbar from './HoverScrollbar.vue';

// --- Typy ---
interface Reaction {
  userId: number;
  userName: string;
  emoji: string;
  avatarUrl?: string;
}


const mockReactions: Reaction[] = [
  { userId: 1, userName: 'Bartosz Miazek', emoji: '😢' },
  { userId: 2, userName: 'Anna Kowalska', emoji: '👍' },
  { userId: 3, userName: 'Piotr Nowak', emoji: '😢' },
  { userId: 4, userName: 'Krzysztof Zając', emoji: '😂' },
  { userId: 5, userName: 'Magdalena Wójcik', emoji: '👍' },
  { userId: 6, userName: 'Tomasz Lewandowski', emoji: '👍' },
  { userId: 7, userName: 'Ewa Zielińska', emoji: '😂' },
  { userId: 8, userName: 'Robert Wiśniewski', emoji: '😢' },
  { userId: 9, userName: 'Dominika Krawczyk', emoji: '❤️' },
  { userId: 10, userName: 'Jan Nowakowski', emoji: '❤️' },
];


const reactions = ref<Reaction[]>(mockReactions);
const selectedReaction = ref<string | null>(null);


const totalReactions = computed(() => reactions.value.length);


const reactionSummary = computed(() => {
  return reactions.value.reduce((acc, reaction) => {
    acc[reaction.emoji] = (acc[reaction.emoji] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
});


const filteredReactions = computed(() => {
  let filtered = reactions.value;

  if (selectedReaction.value !== null) {
    filtered = reactions.value.filter(r => r.emoji === selectedReaction.value);
  }


  return [...filtered].sort((a, b) => a.userName.localeCompare(b.userName, 'pl'));
});


const selectTab = (emoji: string | null) => {
  selectedReaction.value = emoji;
};
</script>
