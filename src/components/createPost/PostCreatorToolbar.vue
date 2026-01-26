<template>
    <div class="flex justify-between items-center p-3 border border-theme-border rounded-lg mb-4 shadow-sm">
      <span class="font-medium text-sm text-theme-text">{{ $t('post.addToPost') }}</span>
      <div class="flex space-x-1 sm:space-x-3">
        <image-multiple-icon
  :size="24"
  class="p-0.5 rounded transition"
  :class="isMediaDisabled ? 'text-gray-400 cursor-not-allowed' : 'text-[#45bd62] cursor-pointer hover:bg-gray-100'"
  @click="!isMediaDisabled && emit('openImageSelector')"
  v-tooltip="isMediaDisabled ? $t('post.cannotAddContentBgGif') : $t('post.photoVideo')"
/>

<account-group-icon
  :size="24"
  class="text-[#1877f2] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition"
  @click="emit('openTagUsers')"
  :title="$t('post.tagFriends')"
/>

<emoticon-icon
  :size="24"
  class="text-[#f7b928] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition"
  :title="$t('post.addFeeling')"
  @click="emit('openFeelingModal')"
/>

<map-marker-icon
  :size="24"
  class="text-[#f3425f] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition"
  @click="emit('openLocation')"
  :title="$t('post.location')"
/>

<div
  class="text-white text-[10px] font-bold px-1 rounded flex items-center transition"
  :class="isMediaDisabled ? 'bg-gray-400 cursor-not-allowed' : 'bg-[#1877f2] cursor-pointer hover:opacity-90'"
  @click="!isMediaDisabled && emit('openGifSelector')"
  v-tooltip="isMediaDisabled ? $t('post.cannotAddContentBgImage') : ''"
>GIF</div>

<dots-horizontal-icon
  :size="24"
  class="text-gray-500 cursor-pointer hover:bg-gray-100 p-0.5 rounded transition"
/>
      </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import { storeToRefs } from 'pinia';

const createPostStore = useCreatePostStore();
const { selectedCardBgId, selectedGif, selectedImage, videoToEdit } = storeToRefs(createPostStore);

const isMediaDisabled = computed(() => {
  return selectedCardBgId.value !== 0 || !!selectedGif.value || !!selectedImage.value || !!videoToEdit.value;
});

const emit = defineEmits<{
  (e: 'openImageSelector'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'openGifSelector'): void;
  (e: 'openFeelingModal'): void;
}>();
</script>
