<template>
  <div class="flex justify-center bg-[#f0f2f5] min-h-screen pt-8">
    <div class="w-full ">

      <div class="bg-white rounded-lg shadow-sm border border-gray-300 overflow-hidden">

        <div class="p-4 flex items-start justify-between ">
             <div class="text-[15px] text-gray-500 leading-snug">
                17 znajomych opublikowało post na Twojej <span class="font-bold text-gray-900 cursor-pointer hover:underline">Profilu</span> z okazji Twoich urodzin.
             </div>
             <DotsHorizontalIcon class="text-gray-500 cursor-pointer -mt-1" />
        </div>

        <div class="px-4 py-2" v-for="(post) in posts" :key="post.id">
            <BirthdayPost :post="post" />


        </div>

        <div class="p-3 text-center border-t border-gray-200 hover:bg-gray-50 cursor-pointer transition-colors">
            <span class="text-[15px] font-semibold text-gray-600">Wyświetl jeszcze 14</span>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import BirthdayPost from './BirthdayPostItem.vue';
import type { Post } from '@/types/Post';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';

const availableUserIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// Helper to generate a random subset of user IDs for reactions
const getRandomUserIds = (maxUsers: number, includeCurrentUser = false): number[] => {
    const ids = [...availableUserIds];
    if (includeCurrentUser && !ids.includes(1)) {
        ids.unshift(1); // Ensure current user is in the list if specified
    }

    // Shuffle and pick a random number of unique IDs
    for (let i = ids.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [ids[i], ids[j]] = [ids[j], ids[i]];
    }
    const numToPick = Math.floor(Math.random() * maxUsers) + 1; // At least 1 reaction
    return ids.slice(0, numToPick);
};

// Helper to assign random reactions
const generateRandomReactions = (): Partial<Record<ReactionType, number[]>> => {
    const reactionTypes: ReactionType[] = ['like', 'love', 'haha', 'wow', 'sad', 'angry'];
    const reactions: Partial<Record<ReactionType, number[]>> = {};

    // Ensure 'like' reaction is always present and often includes current user
    reactions['like'] = getRandomUserIds(availableUserIds.length / 2, true);

    // Add other random reactions
    reactionTypes.forEach(type => {
        if (type !== 'like' && Math.random() > 0.6) { // 40% chance to have other reactions
            reactions[type] = getRandomUserIds(availableUserIds.length / 3);
        }
        if (reactions[type as ReactionType]) {
            // Ensure all reaction arrays are unique and contain actual IDs
            reactions[type as ReactionType] = Array.from(new Set(reactions[type as ReactionType]));
        }
    });

    return reactions;
};

// Base timestamp for generating distinct past dates
const baseTimestamp = new Date('2026-01-24T12:00:00.000Z').getTime(); // Today's date (Jan 24, 2026) at noon
const posts = ref<Post[]>([
  {
    "id": "101",
    "content": "Kolejny rok na liczniku! 🎂 Dziękuję wszystkim za pamięć i niesamowitą niespodziankę. To był genialny wieczór w gronie najlepszych ludzi! 🥂✨",
    "authorId": 3,
    "date": new Date(baseTimestamp - (2 * 60 * 60 * 1000)).toISOString(),
    "timestamp": baseTimestamp - (2 * 60 * 60 * 1000),
    "media": [
            { "src": "https://picsum.photos/id/429/800/600", "altText": "Tort urodzinowy i zimne ognie" }
        ],
    "context": { "privacy": "public" },
    "targetId": "1",
    "targetType": "User",
    "stats": { "comments": 3, "shares": 1 },
    "reactions": generateRandomReactions(),
    "comments": [
        {
            "id": 10,
            "authorId": 2,
            "content": "Wszystkiego najlepszego Bartosz! Sto lat! 🎁",
            "date": new Date(baseTimestamp - (1 * 60 * 60 * 1000)).toISOString(),
            "timestamp": baseTimestamp - (1 * 60 * 60 * 1000),
            "likesCount": 12,
            "replies": [
                {
                    "id": 11,
                    "authorId": 1,
                    "content": "Dzięki wielkie Aniu! 🤗",
                    "date": new Date(baseTimestamp - (30 * 60 * 1000)).toISOString(),
                    "likesCount": 2,
                    "timestamp": baseTimestamp - (30 * 60 * 1000)
                }
            ]
        },
        {
            "id": 12,
            "authorId": 3,
            "content": "Najlepszego! Musimy to powtórzyć w przyszły weekend! 🍻",
            "date": new Date(baseTimestamp - (45 * 60 * 1000)).toISOString(),
            "likesCount": 5,
            "timestamp": baseTimestamp - (45 * 60 * 1000)
        }
    ],
    "detectedLanguage": "pl"
  },{
    "id": "104",
    "content": "Wszystkiego najlepszego byczku! 🍻 Zdrowia, szczęścia i spełnienia tego Twojego marzenia o podróży dookoła świata. Pamiętaj, że zawsze możesz na mnie liczyć! Piona! 👊🎈",
    "authorId": 3,
    "date": new Date(baseTimestamp - (8 * 60 * 60 * 1000)).toISOString(),
    "timestamp": baseTimestamp - (8 * 60 * 60 * 1000),
    "media": [
            { "src": "https://picsum.photos/id/129/800/600", "altText": "Dwóch kumpli na wspólnym zdjęciu" }
        ],
    "context": { "privacy": "public" },
    "targetId": "1",
    "targetType": "User",
    "stats": { "comments": 1, "shares": 0 },
    "reactions": generateRandomReactions(),
    "comments": [
        {
            "id": 40,
            "authorId": 1,
            "content": "Wielkie dzięki stary! Widzimy się wieczorem! 🔥",
            "date": new Date(baseTimestamp - (7 * 60 * 60 * 1000)).toISOString(),
            "timestamp": baseTimestamp - (7 * 60 * 60 * 1000),
            "likesCount": 4
        }
    ],
    "detectedLanguage": "pl"
  }
]);
</script>
