<script setup lang="ts">
import { ref, computed } from 'vue';
import CreateBox from '@/components/create/createPost/CreateBox.vue';
import ProfileFriendsMini from '@/components/profile/ProfileFriendsMini.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import PostFilter from '@/components/feed/PostFilter.vue';
import { usePostsStore } from '@/stores/posts';
import { useRoute } from 'vue-router';
import BirthdayPostFeed from '@/components/BirthdayPostFeed.vue';

// Ikony do sekcji Intro
import HomeOutline from 'vue-material-design-icons/HomeOutline.vue';
import CakeVariant from 'vue-material-design-icons/CakeVariant.vue';
import HeartOutline from 'vue-material-design-icons/HeartOutline.vue';
import { useStickySidebar } from '@/composables/useStickySidebar';

defineProps({
    friendsList: {
        type: Array,
        required: true,
    },
    miniPhotosList: {
        type: Array,
        required: true,
    },
    userName: {
        type: String,
        required: true,
    },
    userImage: {
        type: String,
        required: true,
    },
});

const route = useRoute();
const postsStore = usePostsStore();
const activeView = ref('list');

const HEADER_OFFSET = 110;
const BOTTOM_OFFSET = 16;
const leftSectionRef = ref<HTMLElement | null>(null);
const { stickyTop } = useStickySidebar(leftSectionRef, HEADER_OFFSET, BOTTOM_OFFSET);

const targetId = computed(() => route.params.userId as string);

const groupedPostsByMonth = computed(() => {
    const userPosts = postsStore.posts.filter(p => p.authorId === parseInt(targetId.value) || (p.targetType === 'User' && p.targetId === targetId.value));
    const grouped = userPosts.reduce((acc, post) => {
        const date = new Date(post.date);
        const monthYear = date.toLocaleString('pl-PL', { month: 'long', year: 'numeric' });
        if (!acc[monthYear]) {
            acc[monthYear] = [];
        }
        acc[monthYear].push(post);
        return acc;
    }, {} as Record<string, typeof userPosts>);
    return grouped;
});

const handleViewChanged = (view: string) => {
    activeView.value = view;
};

const handleDeletePost = (postId: number) => {
    postsStore.deletePost(postId);
};
</script>

<template>
    <div class="flex flex-col md:flex-row w-full justify-between items-start relative max-w-[1250px] mx-auto px-4">

        <div
            id="LeftSection"
            ref="leftSectionRef"
            class="w-full md:w-[40%] mt-4 md:sticky md:z-10 self-start"
            :style="{ top: `${stickyTop}px` }"
        >
            <div class="bg-theme-bg-secondary p-4 rounded-lg shadow-md border border-theme-border">
                <div class="font-extrabold pb-4 text-theme-text text-xl">Informacje osobiste</div>

                <div class="space-y-4 mb-4">
                    <div class="flex items-center gap-3 text-theme-text">
                        <HomeOutline :size="24" class="text-gray-500" />
                        <span>Pochodzi z: <span class="font-bold">Międzyrzec Podlaski</span></span>
                    </div>
                    <div class="flex items-center gap-3 text-theme-text">
                        <CakeVariant :size="24" class="text-gray-500" />
                        <span>25 lutego 1993</span>
                    </div>
                    <div class="flex items-center gap-3 text-theme-text">
                        <HeartOutline :size="24" class="text-gray-500" />
                        <span>Wolny(a)</span>
                    </div>
                </div>

                <div class="pt-4 border-t border-theme-border">
                    <div class="font-bold text-lg mb-3 text-theme-text text-xl">Wykształcenie</div>
                    <div class="flex items-center gap-3 mb-1">
                        <div class="w-10 h-10 rounded-full bg-white flex items-center justify-center border overflow-hidden">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Logo_AWF_Warszawa.png/600px-Logo_AWF_Warszawa.png" alt="AWF Logo" class="object-contain w-8 h-8">
                        </div>
                        <span class="font-bold text-theme-text">AWF Biała Podlaska</span>
                    </div>
                    <button class="text-gray-500 text-sm hover:underline ml-13">Zobacz więcej informacji o edukacji</button>
                </div>


            </div>

            <div class="bg-theme-bg-secondary p-4 mt-4 rounded-lg shadow-md border border-theme-border">
                <div class="flex justify-between items-center mb-4">
                    <div class="font-extrabold text-theme-text text-xl">Zdjęcia</div>
                    <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Zobacz wszystkie</a>
                </div>
                <div class="grid grid-cols-3 gap-2">
                    <img v-for="id in miniPhotosList" :key="id"
                        :src="`https://picsum.photos/id/${id}/200/200`"
                        class="w-full h-28 object-cover rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                        alt="Zdjęcie"
                    >
                </div>
            </div>

            <ProfileFriendsMini :friends-list="friendsList" />

            <div class="mt-4 text-[13px] text-gray-500 px-2 pb-4">
                Prywatność · Regulamin · Reklama · Pliki cookie · Meta © 2026
            </div>
        </div>

        <div id="ContentSection" class="w-full md:w-[58%] mt-4 min-h-screen pb-20">
            <CreateBox
                :target-id="targetId"
                target-type="User"
            />
            <PostFilter @view-changed="handleViewChanged" />

            <template v-if="activeView === 'list'">
                <PostItem
                    v-for="post in postsStore.posts.filter(p => p.authorId === parseInt(targetId)|| (p.targetType ===('User') && p.targetId === targetId))"
                    :key="post.id"
                    class="mt-4"
                    :post="post"
                    @delete="handleDeletePost"
                />
                <BirthdayPostFeed/>
            </template>

            <template v-else-if="activeView === 'grid'">
                <div class="space-y-6 mt-4">
                    <div v-for="(postsInMonth, monthYear) in groupedPostsByMonth" :key="monthYear"
                         class="bg-theme-bg-secondary p-4 rounded-lg shadow-lg">
                        <h3 class="text-theme-text text-xl font-bold mb-4">{{ monthYear }}</h3>
                        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                            <div v-for="post in postsInMonth" :key="post.id"
                                 class="bg-theme-bg-secondary rounded-xl overflow-hidden flex flex-col h-full border border-theme-border hover:brightness-110 transition-all cursor-pointer relative group">
                                <div class="p-3 text-theme-text text-sm line-clamp-2 min-h-[3rem]">
                                    {{ post.content }}
                                </div>
                                <div class="aspect-square w-full overflow-hidden bg-black">
                                    <img v-if="post.media && post.media.length > 0" :src="post.media[0].src" class="w-full h-full object-cover" />
                                    <div v-else class="w-full h-full flex items-center justify-center bg-zinc-800 text-gray-500">
                                         Brak zdjęcia
                                    </div>
                                </div>
                                <div class="p-3 mt-auto flex items-center gap-2">
                                    <img :src="userImage" class="w-8 h-8 rounded-full" />
                                    <span class="text-[10px] text-gray-500">{{ new Date(post.date).toLocaleDateString('pl-PL') }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
