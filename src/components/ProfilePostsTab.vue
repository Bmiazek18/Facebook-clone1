<script setup lang="ts">
import CreatePostBox from '@/components/CreatePostBox.vue';
import ProfileFriendsMini from './ProfileFriendsMini.vue';
import PostItem from './PostItem.vue';
import BirthdayPostFeed from './BirthdayPostFeed.vue';
import { usePostsStore } from '@/stores/posts';

const postsStore = usePostsStore();

defineProps<{
    friendsList: {
        name: string;
        mutual: number;
        isFriend: boolean;
        imageId: number;
    }[];
    miniPhotosList: number[];
    userName: string;
    userImage: string;
}>();

const handleDeletePost = (postId: string) => {
    postsStore.removeSharedPost(postId);
};
</script>

<template>
    <div class="flex-cols md:flex w-full justify-between">

        <div id="LeftSection" class="w-full md:w-5/12 mt-4 mr-4">
            <div class="bg-theme-bg-secondary p-3 rounded-lg shadow-lg">
                <div class="font-extrabold pb-2 text-theme-text text-xl">Intro</div>
                <div class="pb-5">
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                        Dodaj bio
                    </button>
                </div>
                <div class="pb-5">
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                        Edytuj szczegóły
                    </button>
                </div>
                <div class="pb-5">
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                        Dodaj hobby
                    </button>
                </div>
                <div>
                    <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                        Dodaj polecane
                    </button>
                </div>
            </div>

            <div class="bg-theme-bg-secondary p-3 mt-4 rounded-lg shadow-lg">
                <div class="flex justify-between items-center mb-2">
                    <div class="font-extrabold text-theme-text text-xl">Zdjęcia</div>
                    <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Zobacz wszystkie zdjęcia</a>
                </div>

                <div class="grid grid-cols-3 gap-1">
                    <img v-for="id in miniPhotosList" :key="id"
                        :src="`https://picsum.photos/id/${id}/150/150`"
                        class="w-full h-24 md:h-28 object-cover rounded-lg aspect-square cursor-pointer"
                        alt="Zdjęcie"
                    >
                </div>
            </div>

            <ProfileFriendsMini :friends-list="friendsList" />
        </div>

        <div id="ContentSection" class="w-full md:w-7/12 mt-4 overflow-auto">
            <CreatePostBox
                :image="userImage"
                :placeholder="`Co słychać, ${userName.split(' ')[0]}?`"
                :author-name="userName"
                :author-avatar="userImage"
            />

            <!-- Shared Posts using PostItem -->
            <PostItem
                v-for="post in postsStore.getSharedPosts"
                :key="post.id"
                :shared-post="post"
                @delete="handleDeletePost"
            />

            <BirthdayPostFeed/>

            </div>
    </div>
</template>
