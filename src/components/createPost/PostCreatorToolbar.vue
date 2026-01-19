<template>
    <div class="flex justify-between items-center p-3 border border-gray-300 rounded-lg mb-4 shadow-sm">
      <span class="font-medium text-sm text-gray-700">Dodaj do posta</span>
      <div class="flex space-x-1 sm:space-x-3">
        <image-multiple-icon :size="24" class="text-[#45bd62] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="emit('openImageSelector')" title="Zdjęcie/film" />
        <account-group-icon :size="24" class="text-[#1877f2] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="emit('openTagUsers')" title="Oznacz znajomych" />
        <emoticon-icon :size="24" class="text-[#f7b928] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" title="Nastrój/aktywność" @click="emit('openFeelingModal')" />
        <map-marker-icon :size="24" class="text-[#f3425f] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="emit('openLocation')" title="Lokalizacja" />
        <div class="bg-[#1877f2] text-white text-[10px] font-bold px-1 rounded flex items-center cursor-pointer hover:opacity-90" @click="emit('openGifSelector')">GIF</div>
        <dots-horizontal-icon :size="24" class="text-gray-500 cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" />
      </div>
    </div>
</template>

<script setup lang="ts">
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';

const emit = defineEmits<{
  (e: 'openImageSelector'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'openGifSelector'): void;
  (e: 'openFeelingModal'): void;
}>();
</script>
