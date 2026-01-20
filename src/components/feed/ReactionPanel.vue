<template>
  <div class="w-full">

    <div v-if="totalReactions > 0" class="flex overflow-x-auto whitespace-nowrap mb-4 border-b border-gray-200">
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

    <div v-else class="p-4 text-center text-gray-500">
        Brak reakcji do wyświetlenia.
    </div>

    <HoverScrollbar class="max-h-[300px]" v-if="totalReactions > 0">
      <div
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
import HoverScrollbar from '@/components/common/HoverScrollbar.vue';
import { getUserById } from '@/data/users';
import { usePostReactions } from '@/composables/usePostReactions';

// --- Typy ---
interface ReactionUser {
  userId: number;
  userName: string;
  emoji: string;
  avatarUrl?: string;
}

const props = defineProps<{
  reactions: Partial<Record<string, number[]>>
}>();

const { reactionIcons } = usePostReactions();

const reactionsList = computed<ReactionUser[]>(() => {
  const list: ReactionUser[] = [];
  if (!props.reactions) return list;

  for (const [type, userIds] of Object.entries(props.reactions)) {
    if (userIds) {
      userIds.forEach(userId => {
        const user = getUserById(userId);
        if (user) {
          list.push({
            userId: user.id,
            userName: user.name,
            emoji: reactionIcons[type]?.emoji || '👍', // Fallback emoji
            avatarUrl: user.avatar
          });
        }
      });
    }
  }
  return list;
});

const selectedReaction = ref<string | null>(null);

const totalReactions = computed(() => reactionsList.value.length);

const reactionSummary = computed(() => {
  return reactionsList.value.reduce((acc, reaction) => {
    acc[reaction.emoji] = (acc[reaction.emoji] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
});

const filteredReactions = computed(() => {
  let filtered = reactionsList.value;

  if (selectedReaction.value !== null) {
    filtered = reactionsList.value.filter(r => r.emoji === selectedReaction.value);
  }

  return [...filtered].sort((a, b) => a.userName.localeCompare(b.userName, 'pl'));
});

const selectTab = (emoji: string | null) => {
  selectedReaction.value = emoji;
};
</script>
