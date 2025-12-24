<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import type { DefineComponent } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';
import { usePostsStore } from '@/stores/posts';
import { storeToRefs } from 'pinia';

// --- FLOATING VUE ---
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
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import HoverScrollbar from './HoverScrollbar.vue';
import PostCreatorToolbar from './PostCreator/PostCreatorToolbar.vue';

// --- LEAFLET (MAPA) ---
import MapPreview from './MapPreview.vue';

// --- TYPY ---
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';
import { getAllUsers } from '@/data/users';
import type { Post } from '@/types/Post';

const props = defineProps<{
  sharedPost?: PostData | null;
  authorName: string;
  authorAvatar: string;
}>();

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
}>();

const createPostStore = useCreatePostStore();
const postsStore = usePostsStore();
const { taggedUsers, selectedLocation, selectedGif, selectedPrivacy, postContent, selectedImage, selectedCardBgId } = storeToRefs(createPostStore);

// --- STAN ---
const userName = computed(() => props.authorName);
const profilePicUrl = computed(() => props.authorAvatar);
const fileInput = ref<HTMLInputElement | null>(null);
const contentEditableDiv = ref<HTMLDivElement | null>(null);

const matchingUsers = ref<User[]>([]);
const showUserDropdown = ref(false);
const isLocalUpdate = ref(false);

const isPublishButtonDisabled = computed(() => {
  if (props.sharedPost) return false;
  return !postContent.value.trim() && !selectedImage.value?.url && !selectedLocation.value && !selectedGif.value;
});

// --- GLÓWNA LOGIKA EDYCJI ---

const onContentInput = (e: Event) => {
  if (!contentEditableDiv.value) return;

  // Oznaczamy, że zmiana pochodzi z klawiatury (blokuje Watcher)
  isLocalUpdate.value = true;

  let newContent = '';
  const nodes = contentEditableDiv.value.childNodes;

  nodes.forEach(node => {
    if (node.nodeType === Node.TEXT_NODE) {
      newContent += node.textContent;
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement;

      // Obsługa tagowania użytkownika
      if (el.dataset.userId) {
        newContent += `[@${el.dataset.userId}]`;
      }
      // Obsługa nowej linii
      else if (el.tagName === 'BR') {
        newContent += '\n';
      }
      // Obsługa Linków (pobieramy czysty tekst z wewnątrz <a>)
      else if (el.tagName === 'A') {
        newContent += el.innerText;
      }
      // Każdy inny element
      else {
        newContent += el.innerText;
      }
    }
  });

  createPostStore.setPostContent(newContent);

  // KLUCZOWA POPRAWKA: Wywołujemy renderowanie ręcznie,
  // ponieważ Watcher jest zablokowany przez isLocalUpdate.
  // To pozwoli wykryć linki w locie.
  renderContentEditable();

  // Logika @User (bez zmian)
  handleUserTagging();

  // Reset flagi po zakończeniu cyklu
  nextTick(() => {
    isLocalUpdate.value = false;
  });
};

const handleUserTagging = () => {
  const selection = window.getSelection();
  if (selection && selection.rangeCount > 0) {
    const range = selection.getRangeAt(0);
    if (range.startContainer.nodeType === Node.TEXT_NODE) {
      const textNode = range.startContainer;
      const textContent = textNode.textContent || '';
      const textBeforeCaret = textContent.substring(0, range.startOffset);
      const match = textBeforeCaret.match(/@([^\s]*)$/);

      if (match) {
        const searchTerm = match[1].toLowerCase();
        const allUsers = getAllUsers();
        if (searchTerm === '') {
          matchingUsers.value = allUsers.slice(0, 5);
        } else {
          matchingUsers.value = allUsers.filter(user =>
            user.name.toLowerCase().includes(searchTerm)
          );
        }
        showUserDropdown.value = matchingUsers.value.length > 0;
      } else {
        showUserDropdown.value = false;
      }
    } else {
      showUserDropdown.value = false;
    }
  }
};

// --- WATCHER ---
watch(postContent, (newValue) => {
  // Jeśli zmiana pochodzi z onContentInput (pisanie), ignorujemy Watcher,
  // bo renderContentEditable wywołaliśmy już ręcznie wyżej.
  // Jeśli zmiana pochodzi z zewnątrz (np. emoji picker), wykonujemy render.
  if (isLocalUpdate.value) return;
  renderContentEditable();
});

const renderContentEditable = () => {
  if (!contentEditableDiv.value) return;

  let htmlContent = postContent.value || '';

  // 1. Sanityzacja
  htmlContent = htmlContent
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\n/g, '<br>');

  const allUsers = getAllUsers();

  // 2. Obsługa @User
  htmlContent = htmlContent.replace(/\[@(\d+)\]/g, (match, userId) => {
    const user = allUsers.find(u => u.id === parseInt(userId));
    if (user) {
      return `<span contenteditable="false" class="bg-blue-100 text-blue-600 font-bold px-1 rounded mx-0.5 cursor-pointer align-middle text-sm" data-user-id="${user.id}">@${user.name}</span>`;
    }
    return match;
  });

  // 3. Obsługa LINKÓW
  // Regex: Szuka http/https, potem znaków, które nie są spacją ani <.
  // Lookahead (?=[\s\u00A0]|<br>|$): Wymaga, aby po linku była spacja (zwykła lub twarda), <br> lub koniec tekstu.
  htmlContent = htmlContent.replace(/(https?:\/\/[^\s<]+)(?=[\s\u00A0]|<br>)/g, (match) => {
    return `<a href="${match}" target="_blank" rel="noopener noreferrer" contenteditable="false" class="text-blue-600 hover:underline cursor-pointer font-medium">${match}</a>`;
  });

  // Ważne: Podmieniamy HTML tylko jeśli faktycznie się zmienił.
  // Dzięki temu, gdy piszesz zwykły tekst, kursor nie wariuje.
  if (contentEditableDiv.value.innerHTML !== htmlContent) {
    contentEditableDiv.value.innerHTML = htmlContent;
    // Kursor przenosimy na koniec tylko wtedy, gdy nastąpiła zmiana struktury (np. powstał link)
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

const onBackspace = (e: KeyboardEvent) => {
  // Opcjonalna obsługa
};

const selectUser = async (user: User) => {
  if (!contentEditableDiv.value) return;
  const currentText = postContent.value;
  const newContent = currentText.replace(/@([^\s]*)$/, `[@${user.id}] `);
  createPostStore.setPostContent(newContent);
  showUserDropdown.value = false;

  // Ponieważ to zmiana "zewnętrzna" (nie bezpośrednio z klawiatury w polu),
  // musimy wymusić aktualizację widoku i fokusu.
  isLocalUpdate.value = false; // Odblokuj watcher
  nextTick(() => {
      contentEditableDiv.value?.focus();
      moveCursorToEnd();
  });
};

// --- PRIVACY & UI ---
const privacyInfo = computed(() => {
  type Info = { label: string; icon: DefineComponent | null };
  const map: Record<string, Info> = {
    only_me: { label: 'Tylko ja', icon: LockIcon },
    public: { label: 'Publiczne', icon: EarthIcon },
    friends: { label: 'Znajomi', icon: AccountGroupIcon },
    friends_except: { label: 'Znajomi z wyjątkiem...', icon: AccountMultipleMinusIcon },
    specific_friends: { label: 'Konkretni znajomi', icon: AccountStarIcon },
  };
  if (!selectedPrivacy.value) return { label: 'Tylko ja', icon: LockIcon };
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
  if (selectedImage.value) {
    emit('navigate', 'imageEditor', selectedImage.value.url);
  }
};

const handleEditVideo = () => {
  if (selectedImage.value) {
    emit('navigate', 'videoEditor', selectedImage.value.url);
  }
};

// --- STORY-LIKE TEXT CARD ---
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

// --- EMOJI ---
const addEmoji = (e: { native: string }) => {
  // Emoji to zmiana zewnętrzna, więc watcher zadziała i sformatuje tekst
  createPostStore.setPostContent(postContent.value + e.native);
};

watch(() => selectedGif.value, () => {
  nextTick(() => emit('updateHeight'));
});

const handlePublish = () => {
  const newPost: Post = {
    id: Date.now(),
    content: postContent.value,
    images: selectedImage.value ? [selectedImage.value.url] : [],
    videoUrl: null,
    authorName: props.authorName,
    authorAvatar: props.authorAvatar,
    authorId: 1,
    date: new Date().toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' }),
    likesCount: 0,
    commentsCount: 0,
    sharesCount: 0,
    taggedUsers: taggedUsers.value,
    location: selectedLocation.value,
    gif: selectedGif.value,
  };
  postsStore.addPost(newPost);
  emit('close');
  createPostStore.reset();
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
            <span class="font-normal text-gray-600"> z: </span>
            <span class="font-bold">
              <template v-for="(user, idx) in taggedUsers" :key="user.id">
                <span v-if="idx > 0">, </span>
                {{ user.name }}
              </template>
            </span>
          </template>
          <template v-if="selectedLocation">
             jest w: <span class="font-bold">{{ selectedLocation.title }}</span>
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
                {{ sharedPost ? 'Powiedz coś o tym...' : (selectedLocation ? 'O czym myślisz, Bartosz?' : 'Co słychać?') }}
              </div>
            </div>

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
          <p class="text-gray-800 text-sm line-clamp-3">{{ sharedPost.content }}</p>
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
