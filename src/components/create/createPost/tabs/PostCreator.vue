<script setup lang="ts">
import { ref, computed } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';
import { usePostsStore } from '@/stores/posts';
import { useEventsStore } from '@/stores/events';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import axios from 'axios';

// --- COMPOSABLES ---
import { useContentEditable } from '@/composables/useContentEditable';
import { useLinkPreview } from '@/composables/useLinkPreview';

// --- KOMPONENTY ---
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';
import PostCreatorToolbar from '../PostCreatorToolbar.vue';
import LinkPreviewCard from '../item/LinkPreviewCard.vue';
import StoryTextCard from '../item/StoryTextCard.vue';
import MediaPreview from '../item/MediaPreview.vue';
import MapPreview from '../../MapPreview.vue';
import CreatePoll from '../item/CreatePoll.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue';
import HoverScrollbar from '@/components/common/HoverScrollbar.vue';

// --- IKONY ---
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';


// --- TYPY ---
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';
import { type Post, type PostMedia } from '@/types/Post';

const props = defineProps<{
  sharedPost?: PostData | null;
  sharedEventId?: string;
}>();

const emit = defineEmits<{
  (e: 'navigate', viewName: string, data?: any): void;
  (e: 'back'): void;
  (e: 'publish', content: string): void;
  (e: 'close'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'openGifSelector'): void;
  (e: 'removeGif'): void;
  (e: 'openFeelingSelector'): void;
}>();

const { t } = useI18n();
const createPostStore = useCreatePostStore();
const postsStore = usePostsStore();
const eventsStore = useEventsStore();
const authStore = useAuthStore();

const {
  taggedUsers, selectedLocation, selectedGif, selectedPrivacy,
  postContent, selectedImages, selectedCardBgId, selectedFeeling,
  selectedActivity, initialView, postVideoUrl
} = storeToRefs(createPostStore);

// --- LOGIKA LINK PREVIEW ---
const {
  linkPreview, isLoadingPreview, fetchLinkMetadata,
  removeLinkPreview, resetLinkPreview
} = useLinkPreview();

// --- LOGIKA CONTENT EDITABLE ---
const contentEditableDiv = ref<HTMLDivElement | null>(null);
const {
    onContentInput: baseOnContentInput,
    matchingUsers,
    showUserDropdown,
    selectUser: selectUserFromComposable,
    addEmoji: addEmojiFromComposable,
} = useContentEditable(contentEditableDiv, postContent);

// --- STAN UŻYTKOWNIKA I ANONIMOWOŚĆ ---
const currentUser = computed(() => authStore.currentUser);

const displayAvatar = computed(() => {
  if (createPostStore.isAnonymous) return '/img/anonymous-avatar.png'; // Upewnij się, że masz taki plik lub ikonę
  return currentUser.value?.avatar || '/default-avatar.png';
});

const displayName = computed(() => {
  return createPostStore.isAnonymous
    ? (t('post.anonymousUser') || 'Anonim')
    : (currentUser.value?.name || '');
});

// --- STAN SHARED ---
const sharedEvent = computed(() => props.sharedEventId ? eventsStore.getEventById(props.sharedEventId) : null);
const sharedPostAsPost = computed<Post | null>(() => {
  if (!props.sharedPost) return null;
  // (Twój kod mapowania PostData na Post - bez zmian)
  return {
    id: props.sharedPost.id,
    authorId: props.sharedPost.author.id,
    content: props.sharedPost.content,
    date: new Date(props.sharedPost.timestamp).toLocaleDateString(),
    timestamp: props.sharedPost.timestamp,
    media: { images: props.sharedPost.images, videoUrl: props.sharedPost.videoUrl },
    context: { privacy: 'public', taggedUsersIds: [] },
    stats: { comments: 0, shares: 0 },
    reactions: {},
  } as Post;
});

// --- VALIDATION ---
const isPublishButtonDisabled = computed(() => {
  if (props.sharedPost || props.sharedEventId) return false;

  const hasContent = postContent.value.trim().length > 0;
  const hasMedia = selectedImages.value.length > 0 || !!selectedGif.value || !!postVideoUrl.value;
  const hasLocation = !!selectedLocation.value;
  const hasLink = !!linkPreview.value;

  return !(hasContent || hasMedia || hasLocation || hasLink);
});

// --- METODY ---

let linkCheckTimeout: ReturnType<typeof setTimeout> | null = null;

const onContentInput = () => {
  baseOnContentInput();

  // Debounce dla wykrywania linków
  if (linkCheckTimeout) clearTimeout(linkCheckTimeout);

  linkCheckTimeout = setTimeout(() => {
    const text = contentEditableDiv.value?.innerText || '';
    const urlMatch = text.match(/(https?:\/\/[^\s]+)/g);

    // Pobieramy tylko jeśli nie ma jeszcze podglądu i nie ładujemy obrazków
    if (urlMatch && urlMatch.length > 0 && !linkPreview.value && selectedImages.value.length === 0 && !selectedGif.value) {
      fetchLinkMetadata(urlMatch[0]);
    }
  }, 500);
};

const detectLanguage = async (text: string): Promise<string | null> => {
  try {
    const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';
    const { data } = await axios.post(`${API_URL}/detect-language`, { text });
    return data.detectedLanguage || null;
  } catch (error) {
    console.error('Language detection error:', error);
    return null;
  }
};

const handlePublish = async () => {
  if (isPublishButtonDisabled.value) return;

  if (props.sharedPost) {
    emit('publish', postContent.value);
    return;
  }

  const language = await detectLanguage(postContent.value);

  const media: PostMedia[] = selectedImages.value.map(img => ({
    src: img.url,
    altText: img.altText,
    tags: img.tags
  }));

  if (postVideoUrl.value) media.push({ src: postVideoUrl.value });
  if (selectedGif.value) media.push({ src: selectedGif.value });

  const newPost: Post = {
    id: `${Date.now()}`,
    content: postContent.value,
    authorId: currentUser.value?.id ?? 0,
    media,
    linkPreview: linkPreview.value,
    targetId: createPostStore.targetId,
    targetType: createPostStore.targetType,
    context: {
        taggedUsersIds: taggedUsers.value.map(u => u.id),
        location: selectedLocation.value || undefined,
        privacy: selectedPrivacy.value,
        feeling: selectedFeeling.value,
        activity: selectedActivity.value,
        createdEvent: !!props.sharedEventId
    },
    stats: { comments: 0, shares: 0 },
    reactions: {},
    date: new Date().toISOString(),
    selectedCardBgId: selectedCardBgId.value,
    timestamp: Date.now(),
    detectedLanguage: language || undefined,
    isAnonymous: createPostStore.isAnonymous,
    sharedContent: props.sharedEventId ? { type: 'event', originalId: props.sharedEventId } : undefined
  };

  postsStore.addPost(newPost);
  emit('close');
  createPostStore.reset();
  resetLinkPreview();
};

// --- Helpery UI ---
const privacyInfo = computed(() => {
  const map: Record<string, { label: string; icon: any }> = {
    only_me: { label: t('post.only_me'), icon: LockIcon },
    public: { label: t('post.public'), icon: EarthIcon },
    friends: { label: t('post.friends'), icon: AccountGroupIcon },
    friends_except: { label: t('post.friends_except'), icon: AccountMultipleMinusIcon },
    specific_friends: { label: t('post.specific_friends'), icon: AccountStarIcon },
  };
  return map[selectedPrivacy.value] || { label: t('post.only_me'), icon: LockIcon };
});

const fileInput = ref<HTMLInputElement | null>(null);
const handleImageClick = () => fileInput.value?.click();

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files;
  if (files) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (e) => createPostStore.addSelectedImage({ url: e.target?.result as string, altText: '', tags: [] });
        reader.readAsDataURL(file);
      } else if (file.type.startsWith('video/')) {
        createPostStore.setPostVideoUrl(URL.createObjectURL(file));
      }
    }
    target.value = '';
    // Jeśli dodaliśmy media, usuwamy podgląd linku (zazwyczaj post ma albo to, albo to)
    resetLinkPreview();
  }
};

const showTextCard = ref(selectedCardBgId.value !== 0);
const toggleTextCard = () => {
  if (!showTextCard.value && selectedCardBgId.value === 0) createPostStore.setSelectedCardBgId(1);
  showTextCard.value = !showTextCard.value;
};
const selectCardBackground = (id: number) => {
  createPostStore.setSelectedCardBgId(id);
  if (id === 0) showTextCard.value = false;
};

// --- BACKGROUNDY KART ---
const cardBackgrounds = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
];
const currentBackground = computed(() => cardBackgrounds.find(bg => bg.id === selectedCardBgId.value) ?? cardBackgrounds[0]);

</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">

    <div v-if="createPostStore.targetType === 'Group'" class="my-3">
      <label class="flex items-center justify-between w-full p-4 bg-gray-100 dark:bg-theme-bg-tertiary rounded-xl cursor-pointer transition-colors hover:bg-gray-200 dark:hover:bg-theme-bg-hover">
        <span class="text-base font-medium text-gray-700 dark:text-theme-text select-none">
          {{ t('post.anonymousPost') || 'Publikuj anonimowo' }}
        </span>
        <div class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" v-model="createPostStore.isAnonymous" class="sr-only peer">
          <div class="w-11 h-6 bg-gray-400 rounded-full peer dark:bg-gray-600 peer-checked:bg-blue-600 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all"></div>
        </div>
      </label>
    </div>

    <div class="flex items-center mb-4">
      <img :src="displayAvatar" :alt="displayName" class="w-10 h-10 rounded-full mr-3 object-cover border border-theme-border" />

      <div class="flex flex-col">
        <div class="text-[15px] leading-tight mb-1 text-theme-text">
          <span class="font-bold">{{ displayName }}</span>

          <template v-if="!createPostStore.isAnonymous && taggedUsers?.length">
            <span class="font-normal text-theme-text-secondary"> {{ t('post.with') }} </span>
            <span class="font-bold">{{ taggedUsers.map(u => u.name).join(', ') }}</span>
          </template>

          <template v-if="selectedFeeling">
            <span class="font-normal text-theme-text-secondary"> {{ t('post.feelingWith') }} </span>
            <button @click="emit('openFeelingSelector')" class="font-bold hover:underline">
              {{ selectedFeeling.label }} {{ selectedFeeling.emoji }}
            </button>
          </template>

          <template v-if="selectedActivity">
            <button @click="emit('openFeelingSelector')" class="font-normal text-theme-text-secondary">
              - {{ selectedActivity.parent.slice(0,-3) }}
              <span class="font-semibold hover:underline">{{ selectedActivity.item.label }}</span>
              {{ selectedActivity.item.emoji }}
            </button>
          </template>

          <template v-if="selectedLocation">
            <span class="font-normal text-theme-text-secondary"> {{ t('post.isAt') }} </span>
            <span class="font-semibold">{{ selectedLocation.title }}</span>
          </template>
        </div>

        <div
            v-if="!createPostStore.isAnonymous"
            class="flex items-center bg-theme-bg-tertiary px-2 py-0.5 rounded-md text-xs font-semibold text-theme-text-secondary w-fit cursor-pointer hover:bg-theme-bg-hover transition-colors"
            @click="emit('navigate', 'privacy')"
        >
          <component v-if="privacyInfo.icon" :is="privacyInfo.icon" :size="12" class="mr-1" />
          <span>{{ privacyInfo.label }}</span>
          <chevron-down-icon :size="12" class="ml-1" />
        </div>
      </div>
    </div>

    <HoverScrollbar :maxHeight="'360px'">

      <div class="relative w-full mb-2">
          <div v-if="!showTextCard" class="relative w-full z-10">
            <div
               class="relative w-full transition-all duration-300"
               :class="selectedCardBgId !== 0 ? [currentBackground.class, currentBackground.textClass, 'rounded-lg p-4 min-h-[12rem] flex items-center justify-center text-center'] : ''"
            >
              <div
                ref="contentEditableDiv"
                contenteditable="true"
                @input="onContentInput"
                class="w-full border-none resize-none text-xxl focus:ring-0 focus:outline-none p-0 pt-2 cursor-text whitespace-pre-wrap bg-transparent"
                :class="{
                    'h-[150px]': selectedImages.length === 0 && !selectedGif && initialView !== 'poll',
                    'text-base': postContent.length > 80,
                    'min-h-[60px]': selectedCardBgId === 0
                }"
              ></div>

              <div
                v-if="!postContent && selectedCardBgId === 0"
                class="absolute top-2 left-0 text-theme-text-secondary text-xl pointer-events-none"
              >
                {{ sharedPost ? t('post.saySomething') : (selectedLocation ? t('post.whatAreYouThinking', { name: displayName }) : t('post.whatsUp')) }}
              </div>
            </div>

            <div class="absolute bottom-0 left-0 text-[#fe5b70] cursor-pointer" :title="t('post.textStyling')">
              <div class="flex items-center justify-center p-1" @click="toggleTextCard">
                <div v-if="initialView != 'poll'" class="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#FF0080] via-[#7928CA] to-[#0070F3] flex items-center justify-center shadow-md border-3 border-white">
                  <span class="text-white font-bold text-md tracking-tighter select-none">Aa</span>
                </div>
              </div>
            </div>

            <div class="absolute bottom-2 right-0 text-theme-text-secondary cursor-pointer" :title="t('post.addEmoji')">
              <VDropdown placement="top-end" :distance="10" :autoHide="true">
                <emoticon-happy-icon :size="24" class="cursor-pointer hover:text-theme-text-secondary-hover transition" />
                <template #popper>
                  <div class="emoji-popper-content">
                    <LazyEmojiPicker @select="addEmojiFromComposable" />
                  </div>
                </template>
              </VDropdown>
            </div>
          </div>

          <VDropdown
            :shown="showUserDropdown"
            placement="bottom-start"
            class="absolute bottom-0 left-0 w-full h-0 pointer-events-none"
          >
            <div class="w-full h-0"></div>
            <template #popper>
              <div class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto">
                <ul>
                  <li v-for="user in matchingUsers" :key="user.id" class="px-4 py-2 cursor-pointer hover:bg-theme-bg-hover flex items-center gap-2" @mousedown.prevent="selectUserFromComposable(user)">
                    <div class="w-8 h-8 bg-theme-bg-tertiary rounded-full flex-shrink-0">
                      <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover rounded-full" />
                    </div>
                    <span class="font-medium text-sm">{{ user.name }}</span>
                  </li>
                </ul>
              </div>
            </template>
          </VDropdown>
      </div>

      <CreatePoll v-if="initialView == 'poll'"/>

      <LinkPreviewCard
        :preview="linkPreview"
        :loading="isLoadingPreview"
        @remove="removeLinkPreview"
      />

      <StoryTextCard
        v-if="showTextCard"
        v-model="postContent"
        :bgId="selectedCardBgId"
        :backgrounds="cardBackgrounds"
        @update:bgId="selectCardBackground"
        @close="showTextCard = false"
      />

      <MapPreview :selectedLocation="selectedLocation" @removeLocation="createPostStore.setLocation(null)" v-if="selectedLocation" />

      <MediaPreview
        :selectedImages="selectedImages"
        :selectedGif="selectedGif"
        :postVideoUrl="postVideoUrl"
        @remove-image="createPostStore.removeSelectedImage"
        @remove-gif="emit('removeGif')"
        @remove-video="createPostStore.postVideoUrl = null"
        @edit-image="(index) => emit('navigate', 'imageEditor', { url: selectedImages[index].url, index })"
        @edit-video="() => { createPostStore.setVideoToEdit(postVideoUrl); emit('navigate', 'videoEditor', { url: postVideoUrl }); }"
      />

      <input ref="fileInput" type="file" accept="image/*,video/mp4" class="hidden" @change="handleImageSelect" multiple />

      <div v-if="sharedPostAsPost" class="mb-4 rounded-lg overflow-hidden">
        <PostItem :post="sharedPostAsPost" :is-shared="true" />
      </div>

      <div v-if="sharedEvent" class="mb-4 border border-theme-border rounded-lg overflow-hidden cursor-pointer group hover:opacity-95 transition-opacity">
        <div class="relative w-full aspect-[1.91/1] bg-theme-bg-tertiary border-b border-theme-border">
          <img v-if="sharedEvent.images?.[0]" :src="sharedEvent.images[0]" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full bg-linear-to-br from-theme-primary to-purple-600 flex items-center justify-center text-white font-bold text-2xl">
             {{ sharedEvent.date ? sharedEvent.date.split(' ')[0] : 'EVENT' }}
          </div>
        </div>
        <div class="p-3 bg-theme-bg flex items-center justify-between gap-3">
          <div class="flex-1 min-w-0 flex flex-col justify-center">
            <div class="text-theme-danger text-[13px] font-semibold mb-0.5 uppercase tracking-wide leading-none">
              {{ sharedEvent.date || t('post.eventDateFallback') }}
            </div>
            <h3 class="font-bold text-[17px] text-theme-text leading-tight mb-0.5 truncate">{{ sharedEvent.title || sharedEvent.name }}</h3>
            <div class="text-[13px] text-theme-text-secondary truncate leading-tight">{{ sharedEvent.locationName || sharedEvent.location || 'Lokalizacja nieznana' }}</div>
          </div>
          <button class="shrink-0 flex items-center gap-1.5 bg-theme-blue-light hover:bg-theme-blue-light-hover text-theme-primary px-3 py-1.5 rounded-md font-semibold text-[15px] transition-colors border border-transparent">
            <StarIcon :size="18" />
            <span>{{ t('post.interesujeSie') }}</span>
            <ChevronDownIcon :size="16" class="ml-0.5" />
          </button>
        </div>
      </div>
    </HoverScrollbar>

    <hr class="my-4 border-theme-border">

    <PostCreatorToolbar
        @openImageSelector="handleImageClick"
        @openTagUsers="emit('openTagUsers')"
        @openLocation="emit('openLocation')"
        @openGifSelector="emit('openGifSelector')"
        @openFeelingModal="emit('openFeelingSelector')"
    />

    <button
      :disabled="isPublishButtonDisabled"
      class="w-full py-2 rounded-lg font-bold text-base transition-colors duration-200"
      :class="isPublishButtonDisabled
        ? 'bg-gray-200 text-gray-400 cursor-not-allowed dark:bg-gray-700 dark:text-gray-500'
        : 'bg-[#1877f2] text-white hover:bg-blue-700'"
      @click="handlePublish"
    >
      {{ t('post.publish') }}
    </button>
  </div>
</template>

<style scoped>

.emoji-popper-content {
  overflow: hidden;
}
:deep(.v-popper) {
  width: 100%;
}
</style>
