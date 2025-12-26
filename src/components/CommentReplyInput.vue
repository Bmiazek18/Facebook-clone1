<script setup lang="ts">
import { ref } from 'vue';
import EmoticonHappyOutline from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue';
import FileGifBox from 'vue-material-design-icons/FileGifBox.vue';
import Send from 'vue-material-design-icons/Send.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import GifSelector from './GifSelector.vue';
import { useAuthStore } from '@/stores/auth';
import { usePostsStore } from '@/stores/posts';
import LazyEmojiPicker from './LazyEmojiPicker.vue';

const props = defineProps<{
    postAvatarSrc: string
    placeholder?: string,
    postId: string,
    parentId?: number | null
}>()

const authStore = useAuthStore();
const postsStore = usePostsStore();

// Stan dla tekstu
const textValue = ref('');

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
            authorName: authStore.currentUser.name,
            authorAvatar: authStore.currentUser.avatar,
            content: textValue.value,
            date: 'now',
            likesCount: 0,
            image: selectedImage.value,
            replies: [],
        };
        postsStore.addComment(props.postId, newComment, props.parentId || null);
        textValue.value = '';
        selectedImage.value = null;
        emit('onCommentSubmitted');
    }
};

// Funkcja do automatycznego rozszerzania textarea
const autoResize = (event: Event) => {
    const target = event.target as HTMLTextAreaElement;
    target.style.height = 'auto'; // Resetujemy wysokość, aby zmierzyć nową
    target.style.height = target.scrollHeight + 'px'; // Ustawiamy wysokość na podstawie zawartości
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
            <div v-if="selectedImage" class="relative mb-2">
                <img :src="selectedImage" class="rounded-lg max-h-40" />
                <button @click="removeImage" class="absolute top-2 right-2 bg-gray-800 text-white rounded-full p-1 text-xs">X</button>
            </div>
            <div class="w-full flex">
                <textarea
                    v-model="textValue"
                    rows="1"
                    @input="autoResize"
                    :placeholder="props.placeholder || 'Napisz komentarz...'"
                    class="w-full bg-transparent border-none outline-none focus:ring-0 p-0 text-[15px] placeholder-gray-500 text-[#050505] resize-none overflow-hidden min-h-[22px] leading-relaxed"
                ></textarea>
            </div>

            <div class="flex justify-between items-center mt-1 text-gray-500">

                <div class="flex items-center space-x-0 -ml-1">

    <VDropdown :distance="10">
        <button class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors" title="Wstaw emoji">
                        <EmoticonHappyOutline :size="18" />

                    </button>
        <template #popper>
            <LazyEmojiPicker @select="emoji => textValue += emoji.native" />
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
                    :class="textValue.length > 0 || selectedImage ? 'text-blue-500' : 'text-gray-300 pointer-events-none'"
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
