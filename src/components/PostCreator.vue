<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import type { DefineComponent } from 'vue';

// --- FLOATING VUE ---
// Ensure you import the CSS in main.ts or here if not globally available
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';

// --- IKONY ---
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import FormatColorTextIcon from 'vue-material-design-icons/FormatColorText.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';
import LazyEmojiPicker from './LazyEmojiPicker.vue';
import StoryTextCard from './StoryTextCard.vue';
import MediaPreview from './MediaPreview.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import HoverScrollbar from './HoverScrollbar.vue';
import PostCreatorToolbar from './PostCreator/PostCreatorToolbar.vue';

// --- LEAFLET (MAPA) ---
import MapPreview from './MapPreview.vue';

// --- TYPY ---
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';

export interface PostLocation {
  title: string;
  subtitle: string;
  type: string;
  lat: string | null;
  lon: string | null;
}

const props = defineProps<{
  sharedPost?: PostData | null;
  taggedUsers?: User[];
  selectedLocation?: PostLocation | null;
  selectedGif?: string | null;
  selectedPrivacy?: string | null;
}>();

const emit = defineEmits<{
  (e: 'navigate', viewName: string): void;
  (e: 'back'): void;
  (e: 'publish', content: string): void;
  (e: 'close'): void;
  (e: 'updateHeight'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'removeLocation'): void;
  (e: 'openGifSelector'): void;
  (e: 'removeGif'): void;
}>();

// --- STAN ---
const userName = ref('Bartosz Miazek');
const profilePicUrl = ref('https://i.pravatar.cc/150?img=12');
const postContent = ref('');
const selectedImage = ref<string | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

const isPublishButtonDisabled = computed(() => {
  if (props.sharedPost) return false;
  return !postContent.value.trim() && !selectedImage.value && !props.selectedLocation && !props.selectedGif;
});

const privacyInfo = computed(() => {
  type Info = { label: string; icon: DefineComponent | null };
  const map: Record<string, Info> = {
    only_me: { label: 'Tylko ja', icon: LockIcon },
    public: { label: 'Publiczne', icon: EarthIcon },
    friends: { label: 'Znajomi', icon: AccountGroupIcon },
    friends_except: { label: 'Znajomi z wyjątkiem...', icon: AccountMultipleMinusIcon },
    specific_friends: { label: 'Konkretni znajomi', icon: AccountStarIcon },

  };
  if (!props.selectedPrivacy) return { label: 'Tylko ja', icon: LockIcon };
  return map[props.selectedPrivacy] || { label: props.selectedPrivacy, icon: null };
});

// --- OBSŁUGA INTERFEJSU ---
const openPrivacySelector = () => emit('navigate', 'privacy');
const handleImageClick = () => fileInput.value?.click();

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader();
    reader.onload = async (e) => {
      selectedImage.value = e.target?.result as string;
      await nextTick();
      emit('updateHeight');
    };
    reader.readAsDataURL(file);
  }
};

// --- STORY-LIKE TEXT CARD (mini) ---
const showTextCard = ref(false);
const textCardContent = ref('');
interface CardBackground { id: number; class: string; textClass?: string }
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
];
const selectedCardBgId = ref<number>(1);

const toggleTextCard = () => {
  if (!showTextCard.value) {
    textCardContent.value = postContent.value || '';
  }
  showTextCard.value = !showTextCard.value;
  nextTick(() => emit('updateHeight'));
};

const selectCardBackground = (id: number) => {
  if (id === 0) {
    postContent.value = textCardContent.value;
    showTextCard.value = false;
    nextTick(() => emit('updateHeight'));
  } else {
    selectedCardBgId.value = id;
  }
};

const handleCardClose = () => {
  postContent.value = textCardContent.value;
  showTextCard.value = false;
  nextTick(() => emit('updateHeight'));
};

const removeImage = () => {
  selectedImage.value = null;
  if (fileInput.value) fileInput.value.value = '';
  nextTick(() => emit('updateHeight'));
};

const removeLocation = () => {
  emit('removeLocation');
};

// --- EMOJI PICKER HANDLING ---
const addEmoji = (e: { native: string }) => {
  if (showTextCard.value) {
    textCardContent.value = textCardContent.value + e.native;
  } else {
    postContent.value = postContent.value + e.native;
  }
  // No need to manually close or toggle boolean, FloatingVue handles the DOM.
};

// Removed handleClickOutside and showPicker refs as FloatingVue handles this internally

watch(() => props.selectedGif, () => {
  nextTick(() => emit('updateHeight'));
});

const handlePublish = () => {
  emit('publish', postContent.value);
  emit('close');
  postContent.value = '';
  selectedImage.value = null;
};
</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">

    <div class="flex items-center mb-4">
      <img :src="profilePicUrl" :alt="userName" class="w-10 h-10 rounded-full mr-3 object-cover" />
      <div class="flex flex-col">
        <div class="text-[15px] leading-tight mb-1 text-gray-900">
          <span class="font-bold">{{ userName }}</span>
          <template v-if="props.taggedUsers && props.taggedUsers.length">
            <span class="font-normal text-gray-600"> z: </span>
            <span class="font-bold">
              <template v-for="(user, idx) in props.taggedUsers" :key="user.id">
                <span v-if="idx > 0">, </span>
                {{ user.name }}
              </template>
            </span>
          </template>
          <template v-if="props.selectedLocation">
             jest w: <span class="font-bold">{{ props.selectedLocation.title }}</span>
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
      <div v-if="!showTextCard" class="relative mb-2">
        <textarea
          v-model="postContent"
          :placeholder="sharedPost ? 'Powiedz coś o tym...' : (props.selectedLocation ? 'O czym myślisz, Bartosz?' : 'Co słychać?')"
          class="w-full min-h-[60px] border-none resize-none text-2xl placeholder-gray-500 focus:ring-0 focus:outline-none p-0 pt-2"
          :class="{'text-base': postContent.length > 80}"
        ></textarea>

        <div class="absolute bottom-2 left-0 text-[#fe5b70] cursor-pointer" title="Stylizacja tekstu">
          <format-color-text-icon :size="24" @click="toggleTextCard" />
        </div>

        <div class="absolute bottom-2 right-0 text-gray-500 cursor-pointer" title="Dodaj emoji">
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
                <LazyEmojiPicker @select="(e) => { addEmoji(e); }" />
              </div>
            </template>
          </VDropdown>
        </div>
      </div>

      <StoryTextCard
        v-if="showTextCard"
        v-model="textCardContent"
        :bgId="selectedCardBgId"
        :backgrounds="cardBackgrounds"
        @update:bgId="selectCardBackground"
        @close="handleCardClose"
      />

      <MapPreview :selectedLocation="props.selectedLocation" @removeLocation="removeLocation" v-if="props.selectedLocation" />

      <MediaPreview
        :selectedImage="selectedImage"
        :selectedGif="props.selectedGif"
        @remove-image="removeImage"
        @remove-gif="() => emit('removeGif')"
        @loaded="() => emit('updateHeight')"
      />

      <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleImageSelect" />

      <div v-if="sharedPost" class="mb-4 border border-gray-200 rounded-lg overflow-hidden">
        <img v-if="sharedPost.imageUrl" :src="sharedPost.imageUrl" class="w-full h-48 object-cover" />
        <div class="p-3 bg-gray-50">
          <div class="flex items-center gap-2 mb-2">
            <img :src="sharedPost.authorAvatar" class="w-8 h-8 rounded-full object-cover" />
            <div>
              <p class="font-semibold text-gray-900 text-sm">{{ sharedPost.authorName }}</p>
              <div class="flex items-center gap-1 text-xs text-gray-500">
                <earth-icon :size="10" />
                <span>Publiczny</span>
              </div>
            </div>
          </div>
          <p class="text-gray-800 text-sm line-clamp-3">{{ sharedPost.content }}</p>.
        </div>
      </div>
    </HoverScrollbar>

    <hr class="my-4 border-gray-200">

    <PostCreatorToolbar
        @openImageSelector="handleImageClick"
        @openTagUsers="emit('openTagUsers')"
        @openLocation="emit('openLocation')"
        @openGifSelector="emit('openGifSelector')"
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
      Opublikuj
    </button>

  </div>
</template>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Fix CSS dla Leaflet */
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

/* Optional: Customize the floating vue container if needed */
.emoji-popper-content {

  overflow: hidden;
}
</style>
