<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import EmoticonHappyOutline from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue';
import FileGifBox from 'vue-material-design-icons/FileGifBox.vue';
import Send from 'vue-material-design-icons/Send.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import WebIcon from 'vue-material-design-icons/Web.vue';
import GifSelector from '@/components/common/GifSelector.vue';
import { useAuthStore } from '@/stores/auth';
import { usePostsStore } from '@/stores/posts';
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue';
import { useCommentsStore } from '@/stores/comments';
import { Dropdown as VDropdown } from 'floating-vue';
import MentionInput from '@/components/MentionInput.vue';
import axios from 'axios';
import type { LinkPreviewData } from '@/types/Post';

const props = defineProps<{
    postAvatarSrc: string
    placeholder?: string,
    postId: string,
    parentId?: number | null
}>()

const authStore = useAuthStore();
const postsStore = usePostsStore();
const commentsStore = useCommentsStore();

const postContent = ref('');
const mentionInputRef = ref<InstanceType<typeof MentionInput> | null>(null);

// --- LINK PREVIEW STATE ---
const linkPreview = ref<LinkPreviewData | null>(null);
const isPreviewDismissed = ref(false);
const isLoadingPreview = ref(false);
let linkCheckTimeout: ReturnType<typeof setTimeout> | null = null;

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
             mentionInputRef.value?.moveCursorToEnd();
        });
    }
});

watch(postContent, (newVal) => {
    if (linkCheckTimeout) clearTimeout(linkCheckTimeout);
    linkCheckTimeout = setTimeout(() => {
         const urlMatch = newVal.match(/(https?:\/\/[^\s]+)/g);
         if (urlMatch && urlMatch.length > 0 && !linkPreview.value && !isPreviewDismissed.value) {
            fetchLinkMetadata(urlMatch[0]);
         }
    }, 500);
});

const fetchLinkMetadata = async (url: string) => {
  if (isLoadingPreview.value) return;
  isLoadingPreview.value = true;
  try {
    const { data } = await axios.post('http://127.0.0.1:8000', { url });
    console.log('Pobrane dane Open Graph (komentarz):', data);

    linkPreview.value = {
      url: url,
      domain: data.domain || new URL(url).hostname,
      title: data.title || 'Link Preview',
      description: data.description || '',
      image: data.image || undefined
    };
  } catch (error) {
    console.error('Błąd podczas pobierania metadanych linku (komentarz):', error);
     // Fallback if API fails
     linkPreview.value = {
      url: url,
      domain: new URL(url).hostname,
      title: url,
      description: '',
    };
  } finally {
    isLoadingPreview.value = false;
  }
};

const removeLinkPreview = () => {
    linkPreview.value = null;
    isPreviewDismissed.value = true;
};

const selectedImage = ref<string | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

const selectGif = (gif: string) => {
    selectedImage.value = gif;
    removeLinkPreview(); // Remove link preview if GIF is selected
};

const addEmoji = (emoji: any) => {
    mentionInputRef.value?.addEmoji(emoji);
}

const onFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            selectedImage.value = e.target?.result as string;
            removeLinkPreview(); // Remove link preview if image is selected
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
            linkPreview: linkPreview.value || undefined,
            replies: [],
        };
        postsStore.addComment(props.postId, newComment, props.parentId || null);
        postContent.value = '';
        selectedImage.value = null;
        linkPreview.value = null;
        isPreviewDismissed.value = false;
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

            <MentionInput
                ref="mentionInputRef"
                v-model="postContent"
                :placeholder="props.placeholder || 'Napisz komentarz...'"
            />

            <!-- Loading state dla link preview -->
            <div v-if="isLoadingPreview" class="mt-2 mb-2 bg-gray-100 rounded-lg p-2 border border-gray-200">
                <div class="flex items-center gap-2">
                    <div class="animate-spin rounded-full h-4 w-4 border-2 border-gray-300 border-t-blue-600"></div>
                    <span class="text-xs text-gray-600">Pobieranie podglądu...</span>
                </div>
            </div>

            <!-- Link Preview -->
            <div v-if="linkPreview && !selectedImage && !isLoadingPreview" class="relative mt-2 mb-2 group">
                <button
                @click="removeLinkPreview"
                class="absolute top-1 right-1 z-20 bg-gray-900 bg-opacity-60 hover:bg-opacity-80 rounded-full p-0.5 text-white transition-all opacity-0 group-hover:opacity-100"
                >
                <CloseIcon :size="14" />
                </button>

                <a :href="linkPreview.url" target="_blank" class="block bg-white rounded-lg overflow-hidden border border-gray-300 hover:bg-gray-50 transition-colors cursor-pointer no-underline">
                   <div v-if="linkPreview.image" class="w-full h-32 overflow-hidden bg-gray-200 relative border-b border-gray-300">
                        <img :src="linkPreview.image" class="w-full h-full object-cover" alt="Link preview" />
                   </div>

                   <div class="p-2">
                        <div class="text-[10px] text-gray-500 uppercase font-semibold mb-0.5 flex items-center truncate">
                            <WebIcon :size="10" class="mr-1" v-if="!linkPreview.image" />
                            {{ linkPreview.domain }}
                        </div>
                        <div class="font-bold text-gray-900 text-[13px] leading-snug mb-0.5 line-clamp-1">
                            {{ linkPreview.title }}
                        </div>
                        <div class="text-gray-600 text-[11px] leading-snug line-clamp-1">
                            {{ linkPreview.description }}
                        </div>
                   </div>
                </a>
            </div>

            <div v-if="selectedImage" class="relative mb-2 mt-2">
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
                    :class="postContent.length > 0 || selectedImage || linkPreview ? 'text-blue-500' : 'text-gray-300 pointer-events-none'"
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
