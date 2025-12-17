<template>
    <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-80 overflow-y-auto"
    >
        <div
            class="bg-theme-bg-secondary shadow-2xl w-full relative flex h-full "
            :class="{
                'rounded-lg': !isFullScreen,
                'max-h-full': isFullScreen
            }"
        >

            <router-link
                to="/"
                class="absolute top-4 left-4 p-2 text-theme-text rounded-full hover:bg-white/10 z-50"
                :aria-label="$t('common.close')"
            >
                <Close :size="28" fillColor="#FFFFFF" />
            </router-link>

            <GalleryImageViewer
                v-if="currentPost"
                v-model:is-full-screen="isFullScreen"
                :image-src="currentImageUrl"
                image-alt="Post content"
                :has-prev="hasPrevImage"
                :has-next="hasNextImage"
                @prev="goToPrevImage"
                @next="goToNextImage"
            />

            <div
                v-if="!isFullScreen && currentPost"
                class="w-full max-w-md flex flex-col min-w-[370px] "
            >
                <HoverScrollbar class="grow p-4 overflow-y-auto">
                    <div class="p-4 ">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center">
                                <img class="rounded-full w-10 h-10 mr-3" :src="currentPost.authorAvatar" alt="Avatar">
                                <div>
                                    <div class="font-extrabold text-[15px] text-theme-text">{{ currentPost.authorName }}</div>
                                    <div class="flex items-center font-semibold text-[13px] text-theme-text-secondary">
                                        {{ currentPost.date }} <Earth class="ml-1" :size="15" fillColor="#64676B"/>
                                    </div>
                                </div>
                            </div>
                            <button class="text-theme-text-secondary hover:bg-theme-hover rounded-full p-2">
                                <DotsHorizontal :size="20" fillColor="#65686C" />
                            </button>
                        </div>
                    </div>

                    <div class="px-4 pb-3 text-[15px] text-theme-text">
                        {{ currentPost.content }}
                    </div>

                    <div class="flex items-center justify-between pb-3 border-b text-theme-text-secondary text-sm font-semibold">
                            <div class="flex items-center">
                                <ThumbUp fillColor="#1D72E2" :size="16" class="mr-1"/> {{ currentPost.likesCount }}
                            </div>
                            <div class="flex items-center">
                                <span class="cursor-pointer hover:underline">{{ $t('comments.count', { count: currentPost.commentsCount }) }}</span>
                                </div>
                    </div>
                    <div class="flex items-center justify-between py-2 px-4  text-theme-text-secondary text-sm font-semibold">
                        <div class="flex items-center space-x-4">
                            <div class="flex items-center">

                                <ReactionButton/>
                            </div>
                            <div class="flex items-center">
                                <CommentTextMultiple :size="20" fillColor="#65686C" class="mr-1" />
                                <span>{{ $t('actions.comment') }}</span>
                            </div>
                        </div>
                        <div class="flex items-center">
                            <Share :size="20" fillColor="#65686C" class="mr-1" />
                            <span>{{ $t('actions.send') }}</span>
                        </div>
                    </div>
                    <div class="flex justify-between items-center pt-3 mb-2">
                        <span class="font-bold text-gray-700 text-sm">Najtrafniejsze</span>
                        <button class="text-blue-500 text-xs font-semibold hover:underline">
                            {{ $t('comments.viewAll') }}
                        </button>
                    </div>

                    <div class="pt-3">
                          <CommentItem
                            v-for="comment in comments"
                            :key="comment.id"
                            :comment="comment"
                            :post-avatar-src="currentPost.authorAvatar"
                            :depth="0"
                        />
                    </div>
                </HoverScrollbar>

                <div class="p-4 border-t flex items-center bg-theme-bg-secondary sticky bottom-0">
                    <img class="rounded-full w-8 h-8 mr-2 shrink-0" src="https://picsum.photos/40/40?random=11" alt="Twój Avatar">
                    <div class="grow relative">
                        <input
                            type="text"
                            :placeholder="$t('comments.placeholder')"
                            class="w-full border-none bg-gray-100 p-2 rounded-full text-sm pr-16 focus:ring-blue-500 focus:border-blue-500"
                        />
                        <div class="absolute inset-y-0 right-0 pr-2 flex items-center space-x-1">
                            <button class="p-1 rounded-full hover:bg-gray-200">
                                <EmoticonOutline :size="20" fillColor="#65686C" />
                            </button>
                            <button class="p-1 rounded-full hover:bg-gray-200">
                                <CameraOutline :size="20" fillColor="#65686C" />
                            </button>
                            <button class="p-1 rounded-full hover:bg-gray-200">
                                <span>GIF</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Close from 'vue-material-design-icons/Close.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue'
import HoverScrollbar from '@/components/HoverScrollbar.vue'
import ReactionButton from '@/components/ReactionButton.vue'
import CommentItem from '@/components/CommentItem.vue'
import GalleryImageViewer from '@/components/gallery/GalleryImageViewer.vue'
import { getPostById } from '@/data/posts'

const route = useRoute()
const router = useRouter()

const isFullScreen = ref(false)

// Get postId and imageIndex from route params
const postId = computed(() => Number(route.params.postId))
const currentImageIndex = ref(Number(route.params.imageIndex) || 0)

// Get the current post data
const currentPost = computed(() => getPostById(postId.value))

// Current image URL
const currentImageUrl = computed((): string => {
    return currentPost.value?.images[currentImageIndex.value] ?? ''
})

// Navigation helpers
const hasPrevImage = computed(() => currentImageIndex.value > 0)
const hasNextImage = computed(() => {
    if (!currentPost.value) return false
    return currentImageIndex.value < currentPost.value.images.length - 1
})

const goToPrevImage = () => {
    if (hasPrevImage.value) {
        currentImageIndex.value--
        router.replace(`/photo/${postId.value}/${currentImageIndex.value}`)
    }
}

const goToNextImage = () => {
    if (hasNextImage.value) {
        currentImageIndex.value++
        router.replace(`/photo/${postId.value}/${currentImageIndex.value}`)
    }
}

// Watch route params changes
watch(() => route.params.imageIndex, (newIndex) => {
    currentImageIndex.value = Number(newIndex) || 0
})

interface CommentData {
    id: number;
    userName: string;
    text: string;
    date: string;
    likesCount: number;
    avatarSrc: string;
    replies: CommentData[];
}

const comments: CommentData[] = [
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
                replies: [
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
