<template>
  <div class="w-[500px] mx-auto bg-white rounded-xl shadow-lg border border-gray-200 font-sans overflow-hidden">

    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <!-- Main View -->
        <div v-if="currentView === 'main'" key="main" class="view-container bg-white" data-view="main">
          <div class="p-4 flex items-start gap-3">
            <img :src="user.avatar" class="w-10 h-10 rounded-full" alt="Profile">
            <div class="flex-1">
              <h3 class="font-bold text-gray-900">{{ user.name }}</h3>
              <div class="flex gap-2 mt-1 text-[13px] font-semibold text-gray-700">
                <button class="bg-gray-100 px-3 py-1 rounded-md">Aktualności</button>
                <button
                  @click="openPrivacySelector"
                  class="bg-gray-100 px-3 py-1 rounded-md flex items-center gap-1 hover:bg-gray-200 transition-colors"
                >
                  <component :is="selectedPrivacy.icon" :size="14" />
                  {{ selectedPrivacy.label }}
                  <ChevronDownIcon :size="16" />
                </button>
              </div>
            </div>
          </div>

          <div class="px-4 py-2 relative">
            <textarea
              v-model="textareaContent"
              placeholder="Napisz coś o tym..."
              class="w-full h-20 outline-none resize-none text-[17px] pr-10"
            ></textarea>

            <div class="absolute bottom-2 right-4 text-gray-500 cursor-pointer">
              <VDropdown
                placement="top"
                :distance="10"
                :skidding="0"
                :triggers="['click']"
                :autoHide="true"
              >
                <EmoticonOutlineIcon :size="24" />

                <template #popper>
                  <div class="emoji-popper-content">
                    <LazyEmojiPicker @select="handleEmojiSelect" />
                  </div>
                </template>
              </VDropdown>
            </div>

          </div>

          <div class="px-4 pb-4">
            <button
              @click="handleShareNow"
              class="w-full bg-blue-600 text-white font-bold py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Udostępnij teraz
            </button>
          </div>

          <hr class="border-gray-100">

          <div class="p-4">
            <h4 class="font-bold mb-4">Wyślij w Messengerze</h4>
            <div class="relative group">
              <!-- Left arrow -->
              <button
                v-if="!isStart"
                @click="scrollLeft"
                class="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-10 h-10 bg-white rounded-full shadow-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors"
              >
                <ChevronLeftIcon :size="24" fillColor="#4B5563" />
              </button>

              <!-- Carousel -->
              <div
                ref="carouselRef"
                class="flex gap-4 overflow-x-auto no-scrollbar scroll-smooth"
              >
                <div
                  v-for="contact in contacts"
                  :key="contact.id"
                  class="flex flex-col items-center min-w-[72px] cursor-pointer group/contact"
                >
                  <div class="relative">
                    <img :src="contact.avatar" class="w-14 h-14 rounded-full border-2 border-gray-200 group-hover/contact:border-blue-500 transition-colors">
                  </div>
                  <span class="text-[11px] mt-2 text-center line-clamp-1 w-full">{{ contact.name }}</span>
                </div>
              </div>

              <!-- Right arrow -->
              <button
                v-if="!isEnd"
                @click="scrollRight"
                class="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-10 h-10 bg-white rounded-full shadow-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors"
              >
                <ChevronRightIcon :size="24" fillColor="#4B5563" />
              </button>
            </div>
          </div>

          <hr class="border-gray-100">

          <div class="p-4">
            <h4 class="font-bold mb-4">Udostępnij</h4>
            <div class="flex gap-3 overflow-x-auto no-scrollbar">
              <button
                v-for="action in shareActions"
                :key="action.label"
                @click="handleShareAction(action.id)"
                class="flex flex-col items-center gap-1 px-[5px]  shrink-0 cursor-pointer group"
              >
                <div class="w-12 h-12 bg-gray-200 group-hover:bg-gray-300 rounded-full flex items-center justify-center text-gray-700 transition-colors">
                  <component :is="action.icon" :size="24" />
                </div>
                <span class="text-[11px] font-medium text-gray-600 text-center">{{ action.label }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Privacy Selector View -->
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-white"
          data-view="privacy"
          @back="handlePrivacyBack"
          @confirm="handlePrivacyConfirm"
        />
      </Transition>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useCarousel } from '@/composables/useCarousel';
import { useSlideTransition } from '@/composables/useSlideTransition';
import { getAllUsers } from '@/data/users';
import LazyEmojiPicker from '@/components/LazyEmojiPicker.vue';
import { usePostsStore } from '@/stores/posts';
import { useRouter } from 'vue-router';
import type { Reel } from '@/stores/reels';
import '@/assets/animations/slideTransition.css';

// Import poszczególnych ikon jako komponenty
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import EmoticonOutlineIcon from 'vue-material-design-icons/EmoticonOutline.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue';
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue';
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue';
import WhatsappIcon from 'vue-material-design-icons/Whatsapp.vue';
import BookOpenVariantIcon from 'vue-material-design-icons/BookOpenVariant.vue';
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue';
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue';
import PrivacySelector from './PrivacySelector.vue';

const props = defineProps<{
  reel?: Reel | null;
}>();

const emit = defineEmits<{
  close: [];
}>();

const postsStore = usePostsStore();
const router = useRouter();

const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition();

// Initialize view to 'main'
currentView.value = 'main';

const user = ref({ name: 'Bartosz Miazek', avatar: 'https://i.pravatar.cc/150?u=b' });
const showEmojiPicker = ref(false);
const textareaContent = ref('');
const selectedPrivacy = ref({ icon: LockIcon, label: 'Tylko ja' });

// Get all users from data
const allUsers = getAllUsers();
const contacts = ref(allUsers.slice(0, 10)); // First 10 users

const shareActions = [
  { id: 'messenger', label: 'Messenger', icon: FacebookMessengerIcon },
  { id: 'whatsapp', label: 'WhatsApp', icon: WhatsappIcon },
  { id: 'story', label: 'Twoja relacja', icon: BookOpenVariantIcon },
  { id: 'copy_link', label: 'Kopiuj link', icon: LinkVariantIcon },
  { id: 'group', label: 'Grupa', icon: AccountGroupIcon },
  { id: 'profile', label: 'Profil', icon: AccountCircleOutlineIcon },
];

const {
  carouselRef,
  isStart,
  isEnd,
  scrollLeft,
  scrollRight,
} = useCarousel(4);

const handleShareAction = async (actionId: string) => {
  if (actionId === 'copy_link' && props.reel) {
    const reelUrl = `${window.location.origin}/reel/${props.reel.id}`;

    try {
      await navigator.clipboard.writeText(reelUrl);
      // Możesz tutaj dodać toast/notification o skopiowaniu
      console.log('Link skopiowany:', reelUrl);
    } catch (err) {
      console.error('Nie udało się skopiować linku:', err);
    }
  }
  // Możesz dodać inne akcje dla pozostałych buttonów
};

const handleEmojiSelect = (emoji: { native: string }) => {
  textareaContent.value += emoji.native;
  showEmojiPicker.value = false;
};

const openPrivacySelector = () => {
  previousView.value = currentView.value;
  currentView.value = 'privacy';
};

const handlePrivacyBack = () => {
  previousView.value = currentView.value;
  currentView.value = 'main';
};

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  // Map privacy ID to icon and label
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const privacyMap: Record<string, { icon: any; label: string }> = {
    'only_me': { icon: LockIcon, label: 'Tylko ja' },
    'public': { icon: EarthIcon, label: 'Publiczne' },
    'friends': { icon: AccountGroupIcon, label: 'Znajomi' },
    'friends_except': { icon: AccountMultipleMinusIcon, label: 'Znajomi z wyjątkiem...' },
    'specific_friends': { icon: AccountStarIcon, label: 'Konkretni znajomi' },
  };

  selectedPrivacy.value = privacyMap[payload.id] || { icon: LockIcon, label: 'Tylko ja' };
  previousView.value = currentView.value;
  currentView.value = 'main';
};

const handleShareNow = () => {
  if (!props.reel) return;

  const newPost = {
    id: `post_${Date.now()}`,
    authorName: user.value.name,
    authorAvatar: user.value.avatar,
    authorId: 1, // Current user ID
    content: textareaContent.value,
    date: new Date().toLocaleDateString('pl-PL'),
    timestamp: Date.now(),
    images: [],
    likesCount: 0,
    commentsCount: 0,
    sharesCount: 0,
    sharedReelId: props.reel.id,
  };

  postsStore.addPost(newPost);
  emit('close');
  router.push('/');
};
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
