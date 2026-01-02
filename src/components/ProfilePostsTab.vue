<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import CreatePostBox from '@/components/CreatePostBox.vue';
import ProfileFriendsMini from './ProfileFriendsMini.vue';
import PostItem from './PostItem.vue';
import BirthdayPostFeed from './BirthdayPostFeed.vue';
import { usePostsStore } from '@/stores/posts';
import PostFilter from './PostFilter.vue';
const postsStore = usePostsStore();

const props = defineProps<{
    friendsList: any[];
    miniPhotosList: number[];
    userName: string;
    userImage: string;
}>();

const handleDeletePost = (postId: string) => {
    postsStore.removePost(postId);
};

// --- LOGIKA "FACEBOOK DUAL STICKY" ---
const leftSectionRef = ref<HTMLElement | null>(null);
const stickyTop = ref(0); // Dynamiczna wartość CSS 'top'

// Konfiguracja marginesów
const HEADER_OFFSET = 110; // Wysokość nawigacji + zakładek (ok. 50px + 60px)
const BOTTOM_OFFSET = 16;  // Margines od dołu ekranu

let lastScrollY = window.scrollY;

const updateStickyPosition = () => {
    if (!leftSectionRef.value) return;

    const currentScrollY = window.scrollY;
    const direction = currentScrollY > lastScrollY ? 'down' : 'up';
    const scrollDiff = currentScrollY - lastScrollY;

    const viewportHeight = window.innerHeight;
    const sidebarHeight = leftSectionRef.value.offsetHeight;

    // 1. Jeśli sidebar jest KRÓTSZY niż okno -> Zawsze kleimy do góry
    if (sidebarHeight + HEADER_OFFSET + BOTTOM_OFFSET < viewportHeight) {
        stickyTop.value = HEADER_OFFSET;
        lastScrollY = currentScrollY;
        return;
    }

    // 2. Jeśli sidebar jest DŁUŻSZY niż okno -> Obliczamy "pływający" top
    let newTop = stickyTop.value - scrollDiff;

    // Ograniczenie Górne (Sticky Top): Nie może być niżej niż nagłówek
    const maxTop = HEADER_OFFSET;

    // Ograniczenie Dolne (Sticky Bottom): Nie może uciec za bardzo do góry
    // Wartość ujemna, która sprawia, że dół sidebaru styka się z dołem ekranu
    const minTop = viewportHeight - sidebarHeight - BOTTOM_OFFSET;

    // Aplikujemy ograniczenia (Clamping)
    if (newTop > maxTop) {
        newTop = maxTop;
    } else if (newTop < minTop) {
        newTop = minTop;
    }

    stickyTop.value = newTop;
    lastScrollY = currentScrollY;
};

// Obserwator zmian wysokości (np. gdy załadują się obrazki)
let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
    // Inicjalizacja pozycji
    stickyTop.value = HEADER_OFFSET;
    lastScrollY = window.scrollY;

    window.addEventListener('scroll', updateStickyPosition, { passive: true });
    window.addEventListener('resize', updateStickyPosition);

    if (leftSectionRef.value) {
        resizeObserver = new ResizeObserver(() => {
            updateStickyPosition();
        });
        resizeObserver.observe(leftSectionRef.value);
    }
});

onUnmounted(() => {
    window.removeEventListener('scroll', updateStickyPosition);
    window.removeEventListener('resize', updateStickyPosition);
    if (resizeObserver) resizeObserver.disconnect();
});
</script>

<template>
    <div class="flex flex-col md:flex-row w-full justify-between items-start relative">

        <div
            id="LeftSection"
            ref="leftSectionRef"
            class="w-full md:w-5/12 mt-4 mr-4 sticky z-10"
            :style="{ top: `${stickyTop}px` }"
        >
            <div class="bg-theme-bg-secondary p-3 rounded-lg shadow-lg">
                <div class="font-extrabold pb-2 text-theme-text text-xl">Intro</div>
                <div class="space-y-4 pb-2">
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold transition-colors">Dodaj bio</button>
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold transition-colors">Edytuj szczegóły</button>
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold transition-colors">Dodaj hobby</button>
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold transition-colors">Dodaj polecane</button>
                </div>
            </div>

            <div class="bg-theme-bg-secondary p-3 mt-4 rounded-lg shadow-lg">
                <div class="flex justify-between items-center mb-2">
                    <div class="font-extrabold text-theme-text text-xl">Zdjęcia</div>
                    <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Zobacz wszystkie</a>
                </div>
                <div class="grid grid-cols-3 gap-1">
                    <img v-for="id in miniPhotosList" :key="id"
                        :src="`https://picsum.photos/id/${id}/150/150`"
                        class="w-full h-24 md:h-28 object-cover rounded-lg aspect-square cursor-pointer hover:opacity-90 transition-opacity"
                        alt="Zdjęcie"
                    >
                </div>
            </div>

            <ProfileFriendsMini :friends-list="friendsList" />

            <div class="mt-4 text-[13px] text-gray-500 px-2 pb-4">
                Prywatność · Regulamin · Reklama · Pliki cookie · Meta © 2024
            </div>
        </div>

        <div id="ContentSection" class="w-full md:w-7/12 mt-4 min-h-screen pb-20">
            <CreatePostBox
                :image="userImage"
                :placeholder="`Co słychać, ${userName.split(' ')[0]}?`"
                :author-name="userName"
                :author-avatar="userImage"
            />
            <PostFilter />
            <PostItem
                v-for="post in postsStore.posts.filter(p => p.authorId === 1)"
                :key="post.id"
                :post="post"
                @delete="handleDeletePost"
            />
        </div>
    </div>
</template>
