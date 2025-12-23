<script setup lang="ts">
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import HoverScrollbar from './HoverScrollbar.vue'
import ReactionButton from './ReactionButton.vue'
import CommentItem from './CommentItem.vue'

import type { Comment } from './CommentItem.vue'

// Przykładowe dane
const postData = {
    userName: "Kolegium Sędziów Opolskiego Związku Piłki Nożnej",
    date: "10 min",
    text: "✅ Niedziela siatkarska z dużą kontrolą sędziów z 2 stref! (...) Wspieramy! Będziemy kontrolować! Będziemy dbać o rzetelność i będziemy dbać o dobrą komunikację w zespole we współpracy. 😉",
    likesCount: 9,
    commentsCount: 3,
    imageSrc: "https://picsum.photos/600/350?random=1",
    avatarSrc: "https://picsum.photos/40/40?random=2"
}

const comments: Comment[] = [
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
                replies: []
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
    <div class="flex flex-col w-[700px] h-[90vh] bg-white dark:bg-[#242526] overflow-hidden">

        <div class="p-3 sm:p-4 border-b border-theme-border flex-shrink-0 z-10 bg-white dark:bg-[#242526]">
            <div class="flex items-center justify-between">
                <div class="flex items-center">
                    <img class="rounded-full w-9 h-9 sm:w-10 sm:h-10 mr-2 sm:mr-3" :src="postData.avatarSrc" alt="Avatar">
                    <div>
                        <div class="font-extrabold text-sm sm:text-[15px] text-theme-text leading-tight">{{ postData.userName }}</div>
                        <div class="flex items-center font-semibold text-xs sm:text-[13px] text-theme-text-secondary mt-0.5">
                            {{ postData.date }} <Earth class="ml-1" :size="14" fillColor="#64676B"/>
                        </div>
                    </div>
                </div>
                <button class="text-theme-text-secondary hover:bg-theme-hover rounded-full p-2 transition-colors">
                    <DotsHorizontal :size="22" fillColor="#050505" />
                </button>
            </div>
            <p class="mt-2 text-sm sm:text-[15px] text-theme-text leading-normal line-clamp-3 hover:line-clamp-none cursor-pointer">{{ postData.text }}</p>
        </div>

        <HoverScrollbar  class="flex-1 min-h-0 w-full">

            <div class="flex flex-col h-full">
                <div v-if="postData.imageSrc" class="py-2">
                     <img class="w-full h-auto object-cover" :src="postData.imageSrc" alt="Post content image">
                </div>

                <div class="px-3 sm:px-4 pt-2">
                    <div id="Likes" class="flex items-center justify-between pb-2 text-theme-text-secondary text-xs sm:text-sm">
                        <div class="flex items-center gap-1">
                            <ThumbUp fillColor="#1D72E2" :size="16" class="mt-[-1px]"/> {{ postData.likesCount }}
                        </div>
                        <span class="cursor-pointer hover:underline">{{ $t('comments.count', { count: postData.commentsCount }) }}</span>
                    </div>

                    <div class="flex items-center justify-between py-1 font-bold text-theme-text-secondary border-y border-theme-border gap-1">
                         <button class="flex-1 flex items-center justify-center h-8 sm:h-[38px] hover:bg-theme-hover rounded-lg cursor-pointer transition-colors group">
                            <ReactionButton class="scale-90 sm:scale-100"/>
                        </button>
                        <button class="flex-1 flex items-center justify-center h-8 sm:h-[38px] hover:bg-theme-hover rounded-lg cursor-pointer transition-colors text-xs sm:text-sm">
                            <MessageOutline :size="18" fillColor="#65686C" class="mr-1 sm:mr-2 scale-90 sm:scale-100" />
                            <span class="truncate">{{ $t('actions.comment') }}</span>
                        </button>
                        <button class="flex-1 flex items-center justify-center h-8 sm:h-[38px] hover:bg-theme-hover rounded-lg cursor-pointer transition-colors text-xs sm:text-sm">
                            <ShareIcon :size="18" fillColor="#65686C" class="mr-1 sm:mr-2 scale-90 sm:scale-100" />
                            <span class="truncate">{{ $t('actions.share') }}</span>
                        </button>
                    </div>
                </div>

                <div class="p-3 sm:p-4">
                    <CommentItem
                        v-for="comment in comments"
                        :key="comment.id"
                        :comment="comment"
                        :postAvatarSrc="postData.avatarSrc"
                        :depth="0"
                    />
                     <div class="h-4"></div>
                </div>
            </div>
        </HoverScrollbar>

        <div class="p-3 sm:p-4 border-t border-theme-border flex-shrink-0 bg-white dark:bg-[#242526] z-10">
            <div class="flex items-start">
                <div class="flex items-center flex-grow">
                    <img class="rounded-full w-8 h-8 mr-2 sm:mr-3 shrink-0" :src="postData.avatarSrc" alt="Twój Avatar">
                    <div class="relative flex-grow">
                        <input
                            type="text"
                            :placeholder="$t('comments.replyPlaceholder', { name: postData.userName })"
                            class="w-full border-none bg-gray-100 dark:bg-[#333333] py-2 pl-3 pr-16 sm:pr-20 rounded-full text-sm focus:ring-1 focus:ring-blue-500 focus:outline-none transition-shadow"
                        />
                        <div class="absolute inset-y-0 right-0 flex items-center pr-2 sm:pr-3 space-x-1">
                            <button class="text-gray-500 hover:text-gray-700 p-1 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-full transition-colors">😊</button>
                            <button class="text-gray-500 hover:text-gray-700 p-1 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-full transition-colors">📸</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</template>

<style scoped>
/* TO JEST KLUCZOWE ROZWIĄZANIE:
   Używamy :deep(), aby zmusić wewnętrzny div komponentu HoverScrollbar
   do zajęcia 100% wysokości dostępnej w flex-1.
   Bez tego div w środku rośnie razem z treścią i scroll się nie pojawia.
*/
:deep(.force-scroll-height) > div {
    height: 100%;
}
</style>
