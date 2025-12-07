<template>



    <div v-if="totalReactions > 0">
      <div class="flex  overflow-x-auto whitespace-nowrap mb-4">

        <button
          @click="selectTab(null)"
          :class="['px-4 py-2 text-sm font-medium border-b-2 transition-colors duration-200', {
            'border-blue-600 text-blue-600 font-semibold': selectedReaction === null,
            'border-transparent text-gray-500 hover:border-gray-300': selectedReaction !== null,
          }]"
        >
          Wszystkie {{ totalReactions }}
        </button>

        <button
          v-for="(count, emoji) in reactionSummary"
          :key="emoji"
          @click="selectTab(emoji)"
          :class="['flex items-center px-4 py-2 text-sm font-medium border-b-2 transition-colors duration-200', {
            'border-blue-600 text-blue-600 font-semibold': selectedReaction === emoji,
            'border-transparent text-gray-500 hover:border-gray-300': selectedReaction !== emoji,
          }]"
        >
          <span class="text-xl mr-1">{{ emoji }}</span>
          <span>{{ count }}</span>
        </button>
      </div>
<HoverScrollbar>

        <div
          v-for="reaction in filteredReactions"
          :key="reaction.userId + reaction.emoji"
          class="flex items-center justify-between px-6 py-2 rounded-lg cursor-pointer"
        >
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center overflow-hidden flex-shrink-0">
                <img v-if="reaction.avatarUrl" :src="reaction.avatarUrl" :alt="reaction.userName" class="w-full h-full object-cover">
                <span v-else class="text-sm font-bold text-white">{{ reaction.userName.charAt(0) }}</span>
            </div>

            <div class="text-gray-800 font-medium truncate">
              {{ reaction.userName }}
            </div>
          </div>

          <span class="text-2xl flex-shrink-0">{{ reaction.emoji }}</span>
        </div>

</HoverScrollbar>

    </div>

</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import HoverScrollbar from './HoverScrollbar.vue';

// Definicja typu dla pojedynczej reakcji
interface Reaction {
  userId: number;
  userName: string;
  emoji: string;
  avatarUrl?: string; // Opcjonalny URL do awatara
}

// Przykładowe dane (Mock Reactions)
const mockReactions: Reaction[] = [
  { userId: 1, userName: 'Bartosz Miazek', emoji: '😢' }, // Zgodnie z Twoim zrzutem
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

export default defineComponent({
  name: 'ReactionsPanel',
  // Wersja w jednym pliku używa mockReactions bezpośrednio
  setup() {
    // Lista reakcji pobrana z mockowych danych
    const reactions = ref<Reaction[]>(mockReactions);

    // Stan przechowujący aktualnie wybraną reakcję (emotikonę)
    const selectedReaction = ref<string | null>(null);

    // --- Logika Obliczeniowa ---

    // 1. Obliczenie łącznej liczby reakcji
    const totalReactions = computed(() => reactions.value.length);

    // 2. Podsumowanie reakcji: liczenie wystąpień każdej emotikony
    // np. { '😢': 3, '👍': 3, ... }
    const reactionSummary = computed(() => {
      return reactions.value.reduce((acc, reaction) => {
        acc[reaction.emoji] = (acc[reaction.emoji] || 0) + 1;
        return acc;
      }, {} as Record<string, number>);
    });

    // 3. Filtrowanie listy reakcji w zależności od wybranej zakładki
    const filteredReactions = computed(() => {
      let filtered = reactions.value;

      if (selectedReaction.value !== null) {
        // Filtruj po wybranej emotikonie
        filtered = reactions.value.filter(r => r.emoji === selectedReaction.value);
      }

      // Sortowanie alfabetyczne po nazwie użytkownika
      return filtered.slice().sort((a, b) => a.userName.localeCompare(b.userName));
    });

    // 4. Funkcja do zmiany aktywnej zakładki
    const selectTab = (emoji: string | null) => {
      selectedReaction.value = emoji;
    };

    return {
      selectedReaction,
      totalReactions,
      reactionSummary,
      filteredReactions,
      selectTab,
    };
  },
});
</script>
