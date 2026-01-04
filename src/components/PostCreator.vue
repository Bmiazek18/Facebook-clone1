<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import type { DefineComponent } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';
import { usePostsStore } from '@/stores/posts';
import { useEventsStore } from '@/stores/events';
import { storeToRefs } from 'pinia';
import axios from 'axios';

// --- FLOATING VUE ---
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';

// --- IKONY ---
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import FormatColorTextIcon from 'vue-material-design-icons/FormatColorText.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import WebIcon from 'vue-material-design-icons/Web.vue';
import LazyEmojiPicker from './LazyEmojiPicker.vue';
import StoryTextCard from './StoryTextCard.vue';
import MediaPreview from './MediaPreview.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import HoverScrollbar from './HoverScrollbar.vue';
import PostItem from './PostItem.vue';
import PostCreatorToolbar from './PostCreator/PostCreatorToolbar.vue';

// --- LEAFLET (MAPA) ---
import MapPreview from './MapPreview.vue';

// --- TYPY ---
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';
import { getAllUsers } from '@/data/users';
import { type Post } from '@/types/Post';
import { useI18n } from 'vue-i18n';
import { useUserTagging } from '@/composables/useUserTagging';

const { t } = useI18n();

const props = defineProps({
  sharedPost: { type: Object as () => PostData | null, default: null },
  sharedEventId: { type: String, default: null },
  authorName: { type: String, required: true },
  authorAvatar: { type: String, required: true },
});

const emit = defineEmits<{
  (e: 'navigate', viewName: string, selectedImage: string | null): void;
  (e: 'back'): void;
  (e: 'publish', content: string): void;
  (e: 'close'): void;
  (e: 'updateHeight'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'removeLocation'): void;
  (e: 'openGifSelector'): void;
  (e: 'removeGif'): void;
  (e: 'openFeelingSelector'): void;
}>();

const createPostStore = useCreatePostStore();
const postsStore = usePostsStore();
const eventsStore = useEventsStore();
const { taggedUsers, selectedLocation, selectedGif, selectedPrivacy, postContent, selectedImage, selectedCardBgId, selectedFeeling, selectedActivity } = storeToRefs(createPostStore);

// Computed property dla udostępnionego eventu
const sharedEvent = computed(() => {
  if (!props.sharedEventId) return null;
  return eventsStore.getEventById(props.sharedEventId);
});

// --- STAN ---
const userName = computed(() => props.authorName);
const profilePicUrl = computed(() => props.authorAvatar);
const fileInput = ref<HTMLInputElement | null>(null);
const contentEditableDiv = ref<HTMLDivElement | null>(null);
const isLocalUpdate = ref(false);

const { matchingUsers, showUserDropdown, triggerUserTagging, selectUser: selectUserFromComposable } = useUserTagging();

// --- LINK PREVIEW STATE ---
interface LinkPreviewData {
  url: string;
  title: string;
  description: string;
  image?: string;
  domain: string;
}
const linkPreview = ref<LinkPreviewData | null>(null);
const isPreviewDismissed = ref(false);
const isLoadingPreview = ref(false);
const detectedLanguage = ref<string | null>(null);
let linkCheckTimeout: ReturnType<typeof setTimeout> | null = null;

const isPublishButtonDisabled = computed(() => {
  if (props.sharedPost || props.sharedEventId) return false;
  return !postContent.value.trim() && !selectedImage.value?.url && !selectedLocation.value && !selectedGif.value && !linkPreview.value;
});

// --- GLÓWNA LOGIKA EDYCJI ---

const onContentInput = () => {
  if (!contentEditableDiv.value) return;
  isLocalUpdate.value = true;

  let newContent = '';
  const nodes = contentEditableDiv.value.childNodes;

  nodes.forEach(node => {
    if (node.nodeType === Node.TEXT_NODE) {
      newContent += node.textContent;
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement;
      if (el.dataset.userId) {
        newContent += `[@${el.dataset.userId}]`;
      } else if (el.tagName === 'BR') {
        newContent += '\n';
      } else if (el.tagName === 'A') {
        newContent += el.innerText;
      } else {
        newContent += el.innerText;
      }
    }
  });

  createPostStore.setPostContent(newContent);

  const selection = window.getSelection();
  if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      if (range.startContainer.nodeType === Node.TEXT_NODE) {
          const textNode = range.startContainer;
          const textContent = textNode.textContent || '';
          const textBeforeCaret = textContent.substring(0, range.startOffset);
          triggerUserTagging(textBeforeCaret);
      } else {
          triggerUserTagging('');
      }
  }

  nextTick(() => {
    isLocalUpdate.value = false;
  });
};

// --- WATCHER DLA TREŚCI ---
watch(postContent, (newValue) => {
  // 1. Rendering (istniejąca logika)
  if (!isLocalUpdate.value) {
    renderContentEditable();
  }

  // 2. Wykrywanie linków (debounce 500ms)
  if (linkCheckTimeout) clearTimeout(linkCheckTimeout);

  linkCheckTimeout = setTimeout(() => {
    checkForLinks(newValue);
  }, 500);
});

// --- LOGIKA LINK PREVIEW ---
const checkForLinks = async (text: string) => {
  const urlRegex = /(https?:\/\/[^\s]+)/;
  const match = text.match(urlRegex);

  if (match) {
    const url = match[0];

    if (linkPreview.value?.url === url || (isPreviewDismissed.value && lastCheckedUrl === url)) {
      return;
    }

    lastCheckedUrl = url;
    isPreviewDismissed.value = false;

    await fetchLinkMetadata(url);
  } else {
    linkPreview.value = null;
    lastCheckedUrl = '';
  }
};

let lastCheckedUrl = '';

const fetchLinkMetadata = async (url: string) => {
  try {
    isLoadingPreview.value = true;

    // Wywołanie API backendu przez axios
    const { data } = await axios.post('http://127.0.0.1:8000/scrape-og', {
      url: url
    });
    console.log('Pobrane dane Open Graph:', data);

    linkPreview.value = {
      url: url,
      domain: data.domain || new URL(url).hostname,
      title: data.title || 'Link Preview',
      description: data.description || '',
      image: data.image || undefined
    };

  } catch (error) {
    console.error('Błąd podczas pobierania metadanych linku:', error);

  } finally {
    isLoadingPreview.value = false;
    nextTick(() => emit('updateHeight'));
  }
};

const removeLinkPreview = () => {
  linkPreview.value = null;
  isPreviewDismissed.value = true;
  nextTick(() => emit('updateHeight'));
};

// Funkcja do wykrywania języka posta
const detectLanguage = async (text: string): Promise<string | null> => {


  try {
    const { data } = await axios.post('http://127.0.0.1:8000/detect-language', {
      text: text
    });

    return data.detectedLanguage || null;
  } catch (error) {
    console.error('Błąd podczas wykrywania języka:', error);
    return null;
  }
};

const renderContentEditable = () => {
  if (!contentEditableDiv.value) return;

  let htmlContent = postContent.value || '';
  htmlContent = htmlContent
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\n/g, '<br>');

  const allUsers = getAllUsers();

  htmlContent = htmlContent.replace(/\[@(\d+)\]/g, (match, userId) => {
    const user = allUsers.find(u => u.id === parseInt(userId));
    if (user) {
      return `<span contenteditable="false" class="bg-blue-100 text-blue-600 font-bold px-1 rounded mx-0.5 cursor-pointer align-middle text-sm" data-user-id="${user.id}">@${user.name}</span>`;
    }
    return match;
  });

  htmlContent = htmlContent.replace(/(https?:\/\/[^\s<]+)(?=[\s\u00A0]|<br>)/g, (match) => {
    return `<a href="${match}" target="_blank" rel="noopener noreferrer" contenteditable="false" class="text-blue-600 hover:underline cursor-pointer font-medium">${match}</a>`;
  });

  if (contentEditableDiv.value.innerHTML !== htmlContent) {
    contentEditableDiv.value.innerHTML = htmlContent;
    moveCursorToEnd();
  }
};

const moveCursorToEnd = () => {
  nextTick(() => {
    if (contentEditableDiv.value) {
      const selection = window.getSelection();
      const range = document.createRange();
      range.selectNodeContents(contentEditableDiv.value);
      range.collapse(false);
      selection?.removeAllRanges();
      selection?.addRange(range);
    }
  });
};

const onBackspace = (e: KeyboardEvent) => { /* Opcjonalne */ };

const selectUser = async (user: User) => {
  selectUserFromComposable(postContent, user);
  nextTick(() => {
      contentEditableDiv.value?.focus();
      moveCursorToEnd();
  });
};

const privacyInfo = computed(() => {
  type Info = { label: string; icon: DefineComponent | null };
  const map: Record<string, Info> = {
    only_me: { label: t('post.only_me'), icon: LockIcon },
    public: { label: t('post.public'), icon: EarthIcon },
    friends: { label: t('post.friends'), icon: AccountGroupIcon },
    friends_except: { label: t('post.friends_except'), icon: AccountMultipleMinusIcon },
    specific_friends: { label: t('post.specific_friends'), icon: AccountStarIcon },
  };
  if (!selectedPrivacy.value) return { label: t('post.only_me'), icon: LockIcon };
  return map[selectedPrivacy.value] || { label: selectedPrivacy.value, icon: null };
});

const openPrivacySelector = () => emit('navigate', 'privacy', null);
const handleImageClick = () => fileInput.value?.click();

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader();
    reader.onload = async (e) => {
      createPostStore.setSelectedImage({ url: e.target?.result as string, altText: '' });
      await nextTick();
      emit('updateHeight');
    };
    reader.readAsDataURL(file);
  }
};

const handleEditImage = () => {
  if (selectedImage.value) emit('navigate', 'imageEditor', selectedImage.value.url);
};

const handleEditVideo = () => {
  if (selectedImage.value) emit('navigate', 'videoEditor', selectedImage.value.url);
};

const showTextCard = ref(selectedCardBgId.value !== 0);

interface CardBackground { id: number; class: string; textClass?: string }
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
];

const currentBackground = computed(() => {
  return cardBackgrounds.find(bg => bg.id === selectedCardBgId.value) ?? cardBackgrounds[0]
})

const toggleTextCard = () => {
  if (!showTextCard.value && selectedCardBgId.value === 0) {
      createPostStore.setSelectedCardBgId(1);
  }
  showTextCard.value = !showTextCard.value;
  nextTick(() => emit('updateHeight'));
};

const selectCardBackground = (id: number) => {
  createPostStore.setSelectedCardBgId(id);
  if (id === 0) {
    showTextCard.value = false;
    nextTick(() => emit('updateHeight'));
  }
};

const handleCardClose = () => {
  showTextCard.value = false;
  nextTick(() => emit('updateHeight'));
};

const removeImage = () => {
  createPostStore.setSelectedImage(null);
  if (fileInput.value) fileInput.value.value = '';
  nextTick(() => emit('updateHeight'));
};

const removeLocation = () => {
  createPostStore.setLocation(null);
};

const addEmoji = (e: { native: string }) => {
  createPostStore.setPostContent(postContent.value + e.native);
};

watch(() => selectedGif.value, () => {
  nextTick(() => emit('updateHeight'));
});

const handlePublish = async () => {
  if (props.sharedPost) {
    emit('publish', postContent.value);
    return;
  }

  // Wykryj język posta przed dodaniem
  const language = await detectLanguage(postContent.value);
  if (language) {
    detectedLanguage.value = language;
    console.log('Wykryty język posta:', language);
  }

  const newPost: Post = {
    id: `${Date.now()}`,
    content: postContent.value,
    images: selectedImage.value ? [{ src: selectedImage.value.url, tags: selectedImage.value.tags }] : [],
    videoUrl: undefined,
    authorName: props.authorName,
    authorAvatar: props.authorAvatar,
    authorId: postsStore.currentUser.id,
    date: new Date().toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' }),
    likesCount: 0,
    commentsCount: 0,
    sharesCount: 0,
    taggedUsers: taggedUsers.value,
    location: selectedLocation.value,
    gif: selectedGif.value,
    selectedCardBgId: selectedCardBgId.value,
    privacy: selectedPrivacy.value,
    feeling: selectedFeeling.value,
    activity: selectedActivity.value,
    timestamp: Date.now(),
    sharedEventId: props.sharedEventId,
    detectedLanguage: detectedLanguage.value || undefined,
  };
  postsStore.addPost(newPost);
  emit('close');
  createPostStore.reset();
  linkPreview.value = null; // Reset
};
</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">

    <div class="flex items-center mb-4">
      <img :src="profilePicUrl" :alt="userName" class="w-10 h-10 rounded-full mr-3 object-cover" />
      <div class="flex flex-col">
        <div class="text-[15px] leading-tight mb-1 text-gray-900">
          <span class="font-bold">{{ userName }}</span>
          <template v-if="taggedUsers && taggedUsers.length">
            <span class="font-normal text-gray-600"> {{ t('post.with') }} </span>
            <span class="font-bold">
              <template v-for="(user, idx) in taggedUsers" :key="user.id">
                <span v-if="idx > 0">, </span>
                {{ user.name }}
              </template>
            </span>
          </template>
          <template v-if="selectedFeeling">
            <span class="font-normal text-gray-600"> {{ t('post.feelingWith') }} <button @click="emit('openFeelingSelector')" class="font-bold  hover:underline rounded-md px-1">{{ selectedFeeling.label }}</button> {{ selectedFeeling.emoji }}</span>
          </template>
          <template v-if="selectedActivity">
            <button @click="emit('openFeelingSelector')" class="font-normal text-gray-600   rounded-md px-1"> {{ t('post.feelingWith') }} {{ selectedActivity.parent.slice(0,-3) }} <span class="font-semibold hover:underline">{{ selectedActivity.item.label }}</span>  {{ selectedActivity.item.emoji }} </button>
          </template>
          <template v-if="location">
            <span class="font-normal text-gray-600"> {{ t('post.isAt') }} </span>
            <span class="font-semibold">{{ location.title }}</span>
          </template>
        </div>

        <div
            class="flex items-center bg-gray-200 px-2 py-0.5 rounded-md text-xs font-semibold text-gray-700 w-fit cursor-pointer hover:bg-gray-300 transition-colors"
            @click="openPrivacySelector"
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
                @keydown.backspace="onBackspace"
                class="w-full border-none resize-none text-xl focus:ring-0 focus:outline-none p-0 pt-2 cursor-text whitespace-pre-wrap bg-transparent"
                :class="{
                    'text-base': postContent.length > 80,
                    'min-h-[60px]': selectedCardBgId === 0
                }"
              ></div>

              <div
                v-if="!postContent && selectedCardBgId === 0"
                class="absolute top-2 left-0 text-gray-500 text-xl pointer-events-none"
              >
                {{ sharedPost ? t('post.saySomething') : (selectedLocation ? t('post.whatAreYouThinking', { name: 'Bartosz' }) : t('post.whatsUp')) }}
              </div>
            </div>

             <div class="absolute bottom-2 left-0 text-[#fe5b70] cursor-pointer" :title="t('post.textStyling')">
              <format-color-text-icon :size="24" @click="toggleTextCard" />
            </div>

            <div class="absolute bottom-2 right-0 text-gray-500 cursor-pointer" :title="t('post.addEmoji')">
              <VDropdown
                placement="top-end"
                :distance="10"
                :skidding="0"
                :triggers="['click']"
                :autoHide="true"
              >
                <emoticon-happy-icon :size="24" class="cursor-pointer hover:text-gray-700 transition" />

                <template #popper>
                  <div class="emoji-popper-content">
                    <LazyEmojiPicker @select="(_e) => { addEmoji(_e); }" />
                  </div>
                </template>
              </VDropdown>
            </div>
          </div>

          <VDropdown
            :shown="showUserDropdown"
            placement="bottom-start"
            :triggers="[]"
            :auto-hide="false"
            :auto-focus="false"
            :trap-focus="false"
            class="absolute bottom-0 left-0 w-full h-0 pointer-events-none"
          >
            <div class="w-full h-0"></div>
            <template #popper>
              <div class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto">
                <ul>
                  <li
                    v-for="user in matchingUsers"
                    :key="user.id"
                    class="px-4 py-2 cursor-pointer hover:bg-gray-100 flex items-center gap-2"
                    @mousedown.prevent="selectUser(user)"
                  >
                    <div class="w-8 h-8 bg-gray-300 rounded-full flex-shrink-0"></div>
                    <span class="font-medium text-sm">{{ user.name }}</span>
                  </li>
                </ul>
              </div>
            </template>
          </VDropdown>

      </div>

      <!-- Loading state dla link preview -->
      <div v-if="isLoadingPreview" class="mb-3 bg-gray-100 rounded-lg p-4 border border-gray-300">
        <div class="flex items-center gap-3">
          <div class="animate-spin rounded-full h-5 w-5 border-2 border-gray-300 border-t-blue-600"></div>
          <span class="text-sm text-gray-600">Pobieranie podglądu linku...</span>
        </div>
      </div>

      <div v-if="linkPreview && !selectedImage && !selectedGif && !isLoadingPreview" class="relative mb-3 group">
        <button
          @click="removeLinkPreview"
          class="absolute top-2 right-2 z-20 bg-gray-900 bg-opacity-60 hover:bg-opacity-80 rounded-full p-1 text-white transition-all opacity-0 group-hover:opacity-100"
        >
          <close-icon :size="16" />
        </button>

        <a :href="linkPreview.url" target="_blank" class="block bg-gray-100 rounded-lg overflow-hidden border border-gray-300 hover:bg-gray-200 transition-colors cursor-pointer no-underline">
           <div v-if="linkPreview.image" class="w-full h-48 overflow-hidden bg-gray-200 relative border-b border-gray-300">
             <img :src="linkPreview.image" class="w-full h-full object-cover" alt="Link preview" />
           </div>

           <div class="p-3">
             <div class="text-xs text-gray-500 uppercase font-semibold mb-1 flex items-center truncate">
               <web-icon :size="12" class="mr-1" v-if="!linkPreview.image" />
               {{ linkPreview.domain }}
             </div>
             <div class="font-bold text-gray-900 text-[15px] leading-snug mb-0.5 line-clamp-2">
               {{ linkPreview.title }}
             </div>
             <div class="text-gray-600 text-sm leading-snug line-clamp-1">
               {{ linkPreview.description }}
             </div>
           </div>
        </a>
      </div>

      <StoryTextCard
        v-if="showTextCard"
        v-model="postContent"
        :bgId="selectedCardBgId"
        :backgrounds="cardBackgrounds"
        @update:bgId="selectCardBackground"
        @close="handleCardClose"
      />

      <MapPreview :selectedLocation="selectedLocation" @removeLocation="removeLocation" v-if="selectedLocation" />

      <MediaPreview
        :selectedImage="selectedImage"
        :selectedGif="selectedGif"
        @remove-image="removeImage"
        @remove-gif="() => emit('removeGif')"
        @loaded="() => emit('updateHeight')"
        @edit-image="handleEditImage"
        @edit-video="handleEditVideo"
      />

      <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleImageSelect" />

      <div v-if="props.sharedPost" class="mb-4 rounded-lg overflow-hidden">
        <PostItem :post="(props.sharedPost as Post)" :is-shared="true" />
      </div>

      <!-- Shared Event Preview -->
<div v-if="sharedEvent" class="mb-4 border border-[#ced0d4] rounded-lg overflow-hidden cursor-pointer group hover:opacity-95 transition-opacity">
        <div class="relative w-full aspect-[1.91/1] bg-gray-100 border-b border-[#ced0d4]">
          <img
            v-if="sharedEvent.images && sharedEvent.images[0]"
            :src="sharedEvent.images[0]"
            alt="Event cover"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full bg-linear-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-bold text-2xl">
             {{ sharedEvent.date ? sharedEvent.date.split(' ')[0] : 'EVENT' }}
          </div>
        </div>

        <div class="p-3 bg-[#f0f2f5] flex items-center justify-between gap-3">

          <div class="flex-1 min-w-0 flex flex-col justify-center">
            <div class="text-[#F02849] text-[13px] font-semibold mb-0.5 uppercase tracking-wide leading-none">
              {{ sharedEvent.date || 'SOB, 16 MAJ O 15:00' }}
            </div>

            <h3 class="font-bold text-[17px] text-[#050505] leading-tight mb-0.5 truncate">
              {{ sharedEvent.title || sharedEvent.name }}
            </h3>

            <div class="text-[13px] text-[#65676B] truncate leading-tight">
              {{ sharedEvent.locationName || sharedEvent.location || 'Camper Park Politechniki Gdańskiej' }}
            </div>
          </div>

          <button class="shrink-0 flex items-center gap-1.5 bg-[#eaf3ff] hover:bg-[#dce9fc] text-[#1877f2] px-3 py-1.5 rounded-md font-semibold text-[15px] transition-colors border border-transparent">
            <StarIcon :size="18" />
            <span>Interesuję się</span>
            <ChevronDownIcon :size="16" class="ml-0.5" />
          </button>
        </div>
      </div>
    </HoverScrollbar>

    <hr class="my-4 border-gray-200">

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
      :class="{
        'bg-[#1877f2] text-white hover:bg-blue-700': !isPublishButtonDisabled,
        'bg-gray-200 text-gray-400 cursor-not-allowed': isPublishButtonDisabled
      }"
      @click="handlePublish"
    >
      {{ t('post.publish') }}
    </button>

  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(.leaflet-container) {
    z-index: 1111;
    font-family: inherit;
    background-color: #e5e7eb;
}
:deep(.leaflet-pane) {
    z-index: 1111;
}
:deep(.leaflet-top), :deep(.leaflet-bottom) {
    z-index: 1111;
}

.emoji-popper-content {
  overflow: hidden;
}

/* Styl dla VDropdown żeby był na szerokość inputa */
:deep(.v-popper) {
  width: 100%;
}
</style>
