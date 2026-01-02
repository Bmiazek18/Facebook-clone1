<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';
import EmoticonHappyOutline from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue';
import FileGifBox from 'vue-material-design-icons/FileGifBox.vue';
import Send from 'vue-material-design-icons/Send.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import GifSelector from './GifSelector.vue';
import { useAuthStore } from '@/stores/auth';
import { usePostsStore } from '@/stores/posts';
import LazyEmojiPicker from './LazyEmojiPicker.vue';
import { useCommentsStore } from '@/stores/comments';
import { type User } from '@/data/users'; // No longer need getAllUsers, getUserById
import { Dropdown as VDropdown } from 'floating-vue';
import { useContentEditable } from '@/composables/useContentEditable';

const props = defineProps<{
    postAvatarSrc: string
    placeholder?: string,
    postId: string,
    parentId?: number | null
}>()

const authStore = useAuthStore();
const postsStore = usePostsStore();
const commentsStore = useCommentsStore();

const contentEditableDiv = ref<HTMLDivElement | null>(null);
const postContent = ref(''); // Pass this ref to composable

const {
    onContentInput,
    matchingUsers,
    showUserDropdown,
    selectUser,
    addEmoji,
    renderContentEditable, // Exposed from composable
    moveCursorToEnd, // Exposed from composable
} = useContentEditable(contentEditableDiv, postContent);

// Call renderContentEditable on mount for initial content
onMounted(() => {
    renderContentEditable();
});


const taggedUser = computed(() => {
    if (commentsStore.activeReplyInput === props.parentId) {
        return commentsStore.replyingToUser
    }
    return null
})

watch(taggedUser, (newUser) => {
    if (newUser && !postContent.value.includes(`[@${newUser.id}]`)) {
        postContent.value = `[@${newUser.id}] ` + postContent.value;
        nextTick(() => {
             moveCursorToEnd(); // Move cursor to end after programmatic content change
        });
    }
});

const selectedImage = ref<string | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

const selectGif = (gif: string) => {
    selectedImage.value = gif;
};

const onFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            selectedImage.value = e.target?.result as string;
        };
        reader.readAsDataURL(file);
    }
};

const removeImage = () => {
    selectedImage.value = null;
    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

const emit = defineEmits<{
    (e: 'onCommentSubmitted'): void;
}>()

const submitComment = () => {
    if (authStore.currentUser) {
        const newComment = {
            id: Date.now(),
            authorId: authStore.currentUser.id,
            authorName: authStore.currentUser.name,
            authorAvatar: authStore.currentUser.avatar,
            content: postContent.value,
            date: 'now',
            likesCount: 0,
            reactions: {},
            image: selectedImage.value,
            replies: [],
        };
        postsStore.addComment(props.postId, newComment, props.parentId || null);
        postContent.value = '';
        selectedImage.value = null;
        emit('onCommentSubmitted');
    }
};

</script>

<template>
    <div class="flex items-start w-full font-sans">
        <div class="relative shrink-0 mr-1.5 group cursor-pointer">
            <img
                class="w-8 h-8 rounded-full object-cover"
                :src="props.postAvatarSrc"
                alt="Avatar"
            >
            <div class="absolute -bottom-1 -right-1 bg-[#e4e6eb] rounded-full w-4 h-4 flex items-center justify-center border-[2px] border-white text-black">
                <ChevronDown :size="10" />
            </div>
        </div>

        <div class="grow bg-[#f0f2f5] rounded-[18px] px-3 py-2 relative group-focus-within:bg-gray-100 transition-colors">
            
            <div class="relative">
                <VDropdown
                  :shown="showUserDropdown"
                  placement="bottom-start"
                  :triggers="[]"
                  :auto-hide="true"
                >
                    <div
                        ref="contentEditableDiv"
                        contenteditable="true"
                        @input="onContentInput"
                        class="w-full bg-transparent border-none outline-none focus:ring-0 p-0 text-[15px] text-[#050505] resize-none overflow-hidden min-h-[22px] leading-relaxed whitespace-pre-wrap"
                    ></div>
                    <template #popper>
                      <div class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto bg-white dark:bg-gray-800 shadow-lg rounded-lg">
                        <ul>
                          <li
                            v-for="user in matchingUsers"
                            :key="user.id"
                            class="px-4 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                            @mousedown.prevent="selectUser(user)"
                          >
                            <img :src="user.avatar" class="w-8 h-8 rounded-full">
                            <span class="font-medium text-sm text-gray-900 dark:text-gray-100">{{ user.name }}</span>
                          </li>
                        </ul>
                      </div>
                    </template>
                </VDropdown>
                <div v-if="!postContent" class="absolute top-0 left-0 text-gray-500 pointer-events-none text-[15px]">
                    {{ props.placeholder || 'Napisz komentarz...' }}
                </div>
            </div>

            <div v-if="selectedImage" class="relative mb-2">
                <img :src="selectedImage" class="rounded-lg max-h-40" />
                <button @click="removeImage" class="absolute top-2 right-2 bg-gray-800 text-white rounded-full p-1 text-xs">X</button>
            </div>

            <div class="flex justify-between items-center mt-1 text-gray-500">

                <div class="flex items-center space-x-0 -ml-1">

                    <VDropdown :distance="10">
                        <button class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors" title="Wstaw emoji">
                            <EmoticonHappyOutline :size="18" />

                        </button>
                        <template #popper>
                            <LazyEmojiPicker @select="addEmoji" />
                        </template>
                    </VDropdown>



                    <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" class="hidden" />
                    <button @click="fileInput?.click()" class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors" title="Dołącz zdjęcie">
                        <CameraOutline :size="18" />
                    </button>

                    <VDropdown :distance="10">
                        <button class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors" title="Wstaw GIF">
                            <FileGifBox :size="18" />
                        </button>
                        <template #popper>
                            <GifSelector @select="selectGif" />
                        </template>
                    </VDropdown>


                </div>

                <button
                    @click="submitComment"
                    class="p-1 rounded-full transition-colors cursor-pointer hover:bg-[rgba(0,0,0,0.05)]"
                    :class="postContent.length > 0 || selectedImage ? 'text-blue-500' : 'text-gray-300 pointer-events-none'"
                >
                    <Send :size="16" class="ml-0.5" />
                </button>
            </div>

        </div>
    </div>
</template>

<style scoped>
.material-design-icon {
    display: flex;
    align-items: center;
    justify-content: center;
}
</style>