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

            <button
                class="absolute -top-10 right-0 p-2 text-theme-text rounded-full hover:bg-theme-hover z-50"
                :aria-label="$t('common.close')"
            >
                <Close :size="28" fillColor="#FFFFFF" />
            </button>

            <div
                class="bg-black flex flex-col items-center justify-center relative"
                :class="{
                    // Jeśli pełny ekran: zajmij całą szerokość, pełne zaokrąglenie w lewo, bez zaokrągleń w prawo
                    'flex-grow w-full min-w-[50%] rounded-l-lg': !isFullScreen,
                    'w-full rounded-r-none rounded-l-lg': isFullScreen
                }"
            >
                <div class="absolute top-4 right-4 flex space-x-2 z-40">
                    <button
                        @click="zoomIn"
                        :disabled="currentZoom >= maxZoom"
                        class="p-2 bg-gray-700 bg-opacity-80 text-white rounded-full hover:bg-opacity-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
                        aria-label="Powiększ"
                    >
                        <Plus :size="20" fillColor="#FFFFFF" />
                    </button>
                    <button
                        @click="zoomOut"
                        :disabled="currentZoom <= minZoom"
                        class="p-2 bg-gray-700 bg-opacity-80 text-white rounded-full hover:bg-opacity-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
                        aria-label="Pomniejsz"
                    >
                        <Minus :size="20" fillColor="#FFFFFF" />
                    </button>

                    <button
                        @click="toggleFullScreen"
                        class="p-1 text-white rounded-full hover:bg-white/10 transition self-center"
                        aria-label="Pełny ekran"
                    >
                        <component :is="isFullScreen ? ArrowCollapse : ArrowExpand" :size="28" fillColor="#FFFFFF" />
                    </button>
                </div>
                <div
                    ref="imageContainer"
                    class="flex items-center justify-center overflow-hidden w-full h-full"
                >
                    <img
                        ref="imageElement"
                        class="max-w-full max-h-full object-contain"
                        :class="{
                            'cursor-grab': currentZoom > 1.0 && !isDragging,
                            'cursor-grabbing': isDragging
                        }"
                        :style="{
                            transform: `scale(${currentZoom}) translate(${imageOffsetX}px, ${imageOffsetY}px)`,
                            transition: isDragging ? 'none' : 'transform 100ms ease-in-out'
                        }"
                        src="https://picsum.photos/800/800?random=1"
                        alt="Post content"
                        @mousedown="startDrag"
                        @touchstart="startDrag"
                    >
                </div>
            </div>

            <div
                v-if="!isFullScreen"
                class="w-full max-w-md flex flex-col min-w-[350px] "
            >
                <HoverScrollbar class="flex-grow p-4 overflow-y-auto">
                    <div class="p-4 ">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center">
                                <img class="rounded-full w-10 h-10 mr-3" src="https://picsum.photos/40/40?random=10" alt="Avatar">
                                <div>
                                    <div class="font-extrabold text-[15px] text-theme-text">{{ postData.userName }}</div>
                                    <div class="flex items-center font-semibold text-[13px] text-theme-text-secondary">
                                        {{ postData.date }} <Earth class="ml-1" :size="15" fillColor="#64676B"/>
                                    </div>
                                </div>
                            </div>
                            <button class="text-theme-text-secondary hover:bg-theme-hover rounded-full p-2">
                                <DotsHorizontal :size="20" fillColor="#65686C" />
                            </button>
                        </div>
                    </div>

                    <div class="flex items-center justify-between pb-3 border-b text-theme-text-secondary text-sm font-semibold">
                            <div class="flex items-center">
                                <ThumbUp fillColor="#1D72E2" :size="16" class="mr-1"/> {{ postData.mainLikesCount }}
                            </div>
                            <div class="flex items-center">
                                <span class="cursor-pointer hover:underline">{{ $t('comments.count', { count: postData.mainCommentsCount }) }}</span>
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
                        <span class="font-bold text-gray-700 text-sm">{{ postData.commentsHeader }}</span>
                        <button class="text-blue-500 text-xs font-semibold hover:underline">
                            {{ $t('comments.viewAll') }}
                        </button>
                    </div>

                    <div class="pt-3">
                        <CommentItem
                            v-for="comment in postData.comments"
                            :key="comment.id"
                            :comment="comment"
                        />ś
                    </div>
                </HoverScrollbar>

                <div class="p-4 border-t flex items-center bg-theme-bg-secondary sticky bottom-0">
                    <img class="rounded-full w-8 h-8 mr-2 flex-shrink-0" src="https://picsum.photos/40/40?random=11" alt="Twój Avatar">
                    <div class="flex-grow relative">
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
import { ref, onMounted, onUnmounted, watch } from 'vue'
import Close from 'vue-material-design-icons/Close.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import Minus from 'vue-material-design-icons/Minus.vue'

import ArrowExpand from 'vue-material-design-icons/ArrowExpand.vue'
// DODANY NOWY IMPORT IKONY
import ArrowCollapse from 'vue-material-design-icons/ArrowCollapse.vue'

import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue'
import HoverScrollbar from '@/components/HoverScrollbar.vue'
import ReactionButton from '@/components/ReactionButton.vue'
// DODANY IMPORT KOMPONENTU
import CommentItem from './CommentItem.vue'


// DODANA ZMIENNA STANU
const isFullScreen = ref(false)

const currentZoom = ref(1.0)
const zoomStep = 0.2
const maxZoom = 3.0
const minZoom = 1.0

const isDragging = ref(false)
const startDragX = ref(0)
const startDragY = ref(0)
const imageOffsetX = ref(0)
const imageOffsetY = ref(0)

const imageContainer = ref<HTMLElement | null>(null)
const imageElement = ref<HTMLElement | null>(null)


const zoomIn = () => {
    if (currentZoom.value < maxZoom) {
        currentZoom.value = Math.min(maxZoom, currentZoom.value + zoomStep)
    }
}

const zoomOut = () => {
    if (currentZoom.value > minZoom) {
        currentZoom.value = Math.max(minZoom, currentZoom.value - zoomStep)
    }
}

// DODANA FUNKCJA PRZEŁĄCZAJĄCA TRYB PEŁNEGO EKRANU
const toggleFullScreen = () => {
    isFullScreen.value = !isFullScreen.value
    // Resetuj zoom i pozycję przy przełączaniu, aby obraz był poprawnie wyśrodkowany
    currentZoom.value = 1.0
    imageOffsetX.value = 0
    imageOffsetY.value = 0
}

watch(currentZoom, () => {
    if (currentZoom.value <= 1.0) {
        imageOffsetX.value = 0
        imageOffsetY.value = 0
    } else {
        applyBounds()
    }
})


type PointerEvent = MouseEvent | TouchEvent;

const startDrag = (event: PointerEvent) => {
    if (currentZoom.value > 1.0) {
        isDragging.value = true
        const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
        const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY
        startDragX.value = clientX
        startDragY.value = clientY

        if ('preventDefault' in event) event.preventDefault()
    }
}

const drag = (event: PointerEvent) => {
    if (!isDragging.value) return

    const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
    const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY

    const deltaX = clientX - startDragX.value
    const deltaY = clientY - startDragY.value

    imageOffsetX.value += deltaX
    imageOffsetY.value += deltaY

    startDragX.value = clientX
    startDragY.value = clientY

    applyBounds()
}

const endDrag = () => {
    isDragging.value = false
}

const applyBounds = () => {
    if (!imageContainer.value || !imageElement.value || currentZoom.value <= 1.0) return

    const containerWidth = imageContainer.value.clientWidth
    const containerHeight = imageContainer.value.clientHeight

    const imageDisplayedWidth = imageElement.value.clientWidth
    const imageDisplayedHeight = imageElement.value.clientHeight

    const zoomedWidth = imageDisplayedWidth * currentZoom.value
    const zoomedHeight = imageDisplayedHeight * currentZoom.value

    const maxShiftX = Math.max(0, (zoomedWidth - containerWidth) / 2 / currentZoom.value)
    const maxShiftY = Math.max(0, (zoomedHeight - containerHeight) / 2 / currentZoom.value)

    if (imageOffsetX.value > maxShiftX) {
        imageOffsetX.value = maxShiftX
    } else if (imageOffsetX.value < -maxShiftX) {
        imageOffsetX.value = -maxShiftX
    }

    if (imageOffsetY.value > maxShiftY) {
        imageOffsetY.value = maxShiftY
    } else if (imageOffsetY.value < -maxShiftY) {
        imageOffsetY.value = -maxShiftY
    }
}

onMounted(() => {
    document.addEventListener('mousemove', drag as EventListener)
    document.addEventListener('mouseup', endDrag)

    document.addEventListener('touchmove', drag as EventListener)
    document.addEventListener('touchend', endDrag)
    document.addEventListener('touchcancel', endDrag)
})

onUnmounted(() => {
    document.removeEventListener('mousemove', drag as EventListener)
    document.removeEventListener('mouseup', endDrag)
    document.removeEventListener('touchmove', drag as EventListener)
    document.removeEventListener('touchend', endDrag)
    document.removeEventListener('touchcancel', endDrag)
})


const postData = {
    userName: "Bartosz Gulka",
    date: "20 listopada o 10:01",
    text: "To jest treść posta, która może być długa i zajmować kilka linii.",
    mainLikesCount: 9,
    mainCommentsCount: 18,
    commentsHeader: "Najtrafniejsze",
    comments: [
        {
            id: 1,
            user: "Sajmon Tuszyński",
            text: "A na kogo chcesz napadać czarny jest to się nada Xd 😂",
            likes: 12,
            repliesCount: 0,
            reaction: '😂',
            time: "1 dzień"
        },
        {
            id: 2,
            user: "Piotr Wilk",
            text: "Ja miałem go na N na początku, ale dałem go na skrzydło. Tam sprawdza się znacznie lepiej, a na N zdecydowanie lepiej gra mi się Gyokeresem.",
            likes: 2,
            repliesCount: 6,
            reaction: '',
            time: "2 dni"
        },
        {
            id: 3,
            user: "Dawid Libera",
            text: "Jeśli chodzi o wychodzenie za plecy to daje rade mimo braku roli + na wysuniętym napastniku ale jeśli chodzi o wykończenie z tym ma trochę problem",
            likes: 0,
            repliesCount: 3,
            reaction: '',
            time: "2 dni"
        },
        {
            id: 4,
            user: "Nowasielski Konrad",
            text: "Sprzedałem, kupiłem Owajrana i wciąga go nosem",
            likes: 0,
            repliesCount: 0,
            reaction: '',
            time: "2 dni"
        },
        {
            id: 5,
            user: "Testowy User",
            text: "To jest dodatkowy komentarz, aby sprawdzić scrollowanie.",
            likes: 5,
            repliesCount: 1,
            reaction: '👍',
            time: "1 dzień"
        },
        {
            id: 6,
            user: "Inny User",
            text: "Jeszcze jeden komentarz, trochę dłuższy, żeby zajął więcej miejsca w dymku.",
            likes: 0,
            repliesCount: 0,
            reaction: '',
            time: "3 dni"
        }
    ]
}

</script>
