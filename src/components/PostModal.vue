<script setup lang="ts">
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import HoverScrollbar from './HoverScrollbar.vue'
import ReactionButton from './ReactionButton.vue'
// 1. IMPORT NOWEGO KOMPONENTU
import CommentItem from './CommentItem.vue'

import type { Comment } from './CommentItem.vue'



// Przykładowe dane dla postu
const postData = {
    userName: "Kolegium Sędziów Opolskiego Związku Piłki Nożnej",
    date: "10 min",
    text: "✅ Niedziela siatkarska z dużą kontrolą sędziów z 2 stref! (...) Wspieramy! Będziemy kontrolować! Będziemy dbać o rzetelność i będziemy dbać o dobrą komunikację w zespole we współpracy. 😉",
    likesCount: 9,
    commentsCount: 3,
    imageSrc: "https://picsum.photos/600/350?random=1",
    avatarSrc: "https://picsum.photos/40/40?random=2"
}

// Przykładowe komentarze, teraz muszą być zgodne z typem Comment
const comments: Comment[] = [ // Dodano inferencję typu
    {
        id: 1,
        userName: "Marek Kowalski",
        text: "Super inicjatywa! Wsparcie dla sędziów jest kluczowe. Oby tak dalej!",
        date: "5 min",
        likesCount: 2,
        avatarSrc: "https://picsum.photos/40/40?random=3",
        replies: []
    },
    {
        id: 2,
        userName: "Anna Zając",
        text: "Świetnie! Zgadzam się z komunikacją. Ostatnio było z tym kiepsko. 💪",
        date: "3 min",
        likesCount: 5,
        avatarSrc: "https://picsum.photos/40/40?random=4",
        replies: [
            {
                id: 21,
                userName: "Jan Nowak",
                text: "Dokładnie, transparentność jest najważniejsza!",
                date: "2 min",
                likesCount: 1,
                avatarSrc: "https://picsum.photos/40/40?random=6",
                replies: [ // Dodano trzecie zagnieżdżenie dla testu
                     {
                        id: 211,
                        userName: "Komentator Rekurencyjny",
                        text: "To działa! Trzeci poziom komentarzy.",
                        date: "1 min",
                        likesCount: 0,
                        avatarSrc: "https://picsum.photos/40/40?random=8",
                        replies: [{id: 211,
                        userName: "Komentator Rekurencyjny",
                        text: "To działa! 4 poziom komentarzy.",
                        date: "1 min",
                        likesCount: 0,
                        avatarSrc: "https://picsum.photos/40/40?random=8",
                        replies: []}]
                    }
                ]
            },
            {
                id: 22,
                userName: "Tester",
                text: "Popieram, jest nadzieja na poprawę.",
                date: "1 min",
                likesCount: 0,
                avatarSrc: "https://picsum.photos/40/40?random=7",
                replies: []
            }
        ]
    },
    {
        id: 3,
        userName: "Sportowiec",
        text: "Kto będzie sędziował mecz ligowy w ten weekend?",
        date: "1 min",
        likesCount: 0,
        avatarSrc: "https://picsum.photos/40/40?random=5",
        replies: []
    }
]

</script>

<template>


<HoverScrollbar class="grow">
                <div class="flex flex-col h-full">

                    <div class="p-4 border-b border-theme-border">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center">
                                <img class="rounded-full w-10 h-10 mr-3" :src="postData.avatarSrc" alt="Avatar">
                                <div>
                                    <div class="font-extrabold text-[15px] text-theme-text">{{ postData.userName }}</div>
                                    <div class="flex items-center font-semibold text-[13px] text-theme-text-secondary">
                                        {{ postData.date }} <Earth class="ml-1" :size="15" fillColor="#64676B"/>
                                    </div>
                                </div>
                            </div>
                            <button class="text-theme-text-secondary hover:bg-theme-hover rounded-full p-2">
                            <DotsHorizontal :size="23" fillColor="#050505" />
                            </button>
                        </div>
                        <p class="mt-2 text-[15px] text-theme-text">{{ postData.text }}</p>
                    </div>

                    <div v-if="postData.imageSrc" class="py-2">
                        <img class="w-full h-auto object-cover" :src="postData.imageSrc" alt="Post content image">
                    </div>

                    <div class="px-4 pt-2">
                        <div id="Likes" class="flex items-center justify-between pb-2 text-theme-text-secondary text-sm">
                            <div class="flex items-center gap-1">
                                <ThumbUp fillColor="#1D72E2" :size="16" class="mt-[-1px]"/> {{ postData.likesCount }}
                            </div>
                            <span class="cursor-pointer hover:underline">{{ $t('comments.count', { count: postData.commentsCount }) }}</span>
                        </div>

                        <div class="flex items-center justify-around py-1 font-bold text-theme-text-secondary border-y border-theme-border">
                            <button class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer">
                                <ReactionButton/>
                            </button>
                            <button class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer">
                                <MessageOutline :size="18" fillColor="#65686C" class="mr-1" /> {{ $t('actions.comment') }}
                            </button>
                            <button class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer">
                                <ShareIcon :size="18" fillColor="#65686C" class="mr-1" /> {{ $t('actions.share') }}
                            </button>
                        </div>
                    </div>

                    <div class="p-4">
                        <CommentItem
                            v-for="comment in comments"
                            :key="comment.id"
                            :comment="comment"
                            :postAvatarSrc="postData.avatarSrc"
                            :depth="0"
                        />
                    </div>
                    <div class="p-4 border-t border-theme-border">
                        <div class="flex items-start">
                            <div class="flex items-center flex-grow">
                                <img class="rounded-full w-8 h-8 mr-2" :src="postData.avatarSrc" alt="Twój Avatar">
                                <div class="relative flex-grow">
                                    <input
                                        type="text"
                                        :placeholder="$t('comments.replyPlaceholder', { name: postData.userName })"
                                        class="w-full border-none bg-gray-100 dark:bg-[#333333] p-2 pr-12 rounded-full text-sm focus:ring-blue-500 focus:border-blue-500"
                                    />
                                    <div class="absolute inset-y-0 right-0 flex items-center pr-2 space-x-1">
                                        <button class="text-gray-500 hover:text-gray-700">😊</button>
                                        <button class="text-gray-500 hover:text-gray-700">📸</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


            </HoverScrollbar>




</template>
