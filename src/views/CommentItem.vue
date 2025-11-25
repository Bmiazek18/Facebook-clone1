<template>
    <div class="flex items-start mb-4">
        <img class="rounded-full w-8 h-8 mr-2 flex-shrink-0" :src="commentAvatarUrl" :alt="comment.user + ' Avatar'">
        <div>
            <div class="bg-gray-100 rounded-xl p-2 max-w-[280px] inline-block">
                <span class="font-semibold text-sm">{{ comment.user }}</span>
                <p class="text-sm">{{ comment.text }}</p>
            </div>
            <div class="flex items-center space-x-3 ml-1 text-xs text-gray-500 mt-1">
                <span class="font-semibold cursor-pointer hover:underline">{{ comment.time }}</span>
                <span class="font-semibold cursor-pointer hover:underline">Lubię to!</span>
                <span class="font-semibold cursor-pointer hover:underline">Odpowiedz</span>
                <template v-if="comment.likes > 0">
                    <div class="flex items-center ml-2">
                        <ThumbUp fillColor="#1D72E2" :size="12" class="mr-0.5"/>
                        <span class="text-blue-600 font-bold text-xs">{{ comment.likes }}</span>
                    </div>
                </template>
                <span v-if="comment.reaction" class="font-semibold text-base">{{ comment.reaction }}</span>
            </div>
            <div v-if="comment.repliesCount > 0" class="mt-2 ml-4">
                <button class="flex items-center text-blue-500 text-sm font-semibold hover:underline">
                    <ChevronDown :size="16" fillColor="#1D72E2" class="mr-1" />
                    Wyświetl wszystkie {{ comment.repliesCount }} odpowiedzi
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

interface Comment {
    id: number;
    user: string;
    text: string;
    likes: number;
    repliesCount: number;
    reaction: string;
    time: string;
}

const props = defineProps<{
    comment: Comment
}>()

// Generowanie URL avatara na podstawie ID komentarza
const commentAvatarUrl = computed(() => {
    return `https://picsum.photos/40/40?random=${props.comment.id + 10}`
})
</script>
