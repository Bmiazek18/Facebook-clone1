<script setup lang="ts">
import { ref, computed } from 'vue';

// Import ikon z vue-material-design-icons
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import FormatColorTextIcon from 'vue-material-design-icons/FormatColorText.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';

import type { PostData } from '@/types/StoryElement';

// Props dla udostępnianego posta
const props = defineProps<{
    sharedPost?: PostData | null;
}>();

// Definicja emitowanych zdarzeń dla nawigacji
const emit = defineEmits<{
    (e: 'navigate', viewName: string): void;
    (e: 'back'): void;
    (e: 'publish', content: string): void;
    (e: 'close'): void;
}>();

// Stan komponentu
const userName = ref('Bartosz Miazek');
const profilePicUrl = ref('https://i.pravatar.cc/150?img=12');
const postContent = ref('');

const isPublishButtonDisabled = computed(() => {
  // Jeśli mamy udostępniany post, przycisk może być aktywny nawet bez komentarza
  if (props.sharedPost) {
    return false;
  }
  return !postContent.value.trim();
});

// Funkcja wywołująca przejście do widoku 'privacy'
const openPrivacySelector = () => {
    emit('navigate', 'privacy');
};

const handlePublish = () => {
    emit('publish', postContent.value);
    emit('close'); // Emituj event zamknięcia po publikacji
    postContent.value = '';
};
</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">

    <div class="flex items-center mb-4">
      <img :src="profilePicUrl" :alt="userName" class="w-10 h-10 rounded-full mr-3 object-cover" />
      <div class="flex flex-col">
        <span class="font-bold text-gray-800 text-sm">{{ userName }}</span>

        <div
            class="flex items-center bg-gray-200 px-2 py-0.5 rounded-md text-xs text-gray-600 cursor-pointer hover:bg-gray-300 transition-colors"
            @click="openPrivacySelector"
        >
          <lock-icon :size="14" class="mr-1" />
          <span>Tylko ja</span>
          <chevron-down-icon :size="14" class="ml-1" />
        </div>
      </div>
    </div>

    <div class="relative mb-2">
        <textarea
            v-model="postContent"
            :placeholder="sharedPost ? 'Powiedz coś o tym...' : 'Co słychać?'"
            class="w-full h-24 border-none resize-none text-2xl placeholder-gray-500 focus:ring-0 focus:outline-none p-0 pt-2"
        ></textarea>

        <div class="absolute bottom-2 left-0 text-[#fe5b70] cursor-pointer" title="Stylizacja tekstu">
             <format-color-text-icon :size="24" />
        </div>
        <div class="absolute bottom-2 right-0 text-gray-500 cursor-pointer" title="Dodaj emoji">
            <emoticon-happy-icon :size="24" />
        </div>
    </div>

    <!-- Shared Post Preview -->
    <div v-if="sharedPost" class="mb-4 border border-gray-200 rounded-lg overflow-hidden">
      <!-- Post Image -->
      <img
        v-if="sharedPost.imageUrl"
        :src="sharedPost.imageUrl"
        class="w-full h-48 object-cover"
      />

      <!-- Post Content -->
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

    <hr class="my-4 border-gray-200">

    <div class="flex justify-between items-center p-3 border border-gray-300 rounded-lg mb-4">
      <span class="font-medium text-sm text-gray-700">Dodaj do posta</span>
      <div class="flex space-x-3">
        <image-multiple-icon :size="24" class="text-[#45bd62] cursor-pointer hover:opacity-80" />
        <account-group-icon :size="24" class="text-[#1877f2] cursor-pointer hover:opacity-80" />
        <emoticon-icon :size="24" class="text-[#f7b928] cursor-pointer hover:opacity-80" />
        <map-marker-icon :size="24" class="text-[#f3425f] cursor-pointer hover:opacity-80" />
       <span>GIF</span>>
        <dots-horizontal-icon :size="24" class="text-gray-500 cursor-pointer hover:opacity-80" />
      </div>
    </div>

    <button
      :disabled="isPublishButtonDisabled"
      class="w-full py-2 rounded-lg font-bold text-base transition-colors duration-200"
      :class="{
        'bg-blue-500 text-white hover:bg-blue-600': !isPublishButtonDisabled,
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
</style>
