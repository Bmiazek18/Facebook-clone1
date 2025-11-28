<script setup lang="ts">
import { toRefs } from 'vue'
import { useI18n } from 'vue-i18n'

import VideoImage from 'vue-material-design-icons/VideoImage.vue'
import Image from 'vue-material-design-icons/Image.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'

useI18n()

const props = defineProps({
  image: String,
  placeholder: String,
})

const { image, placeholder } = toRefs(props)
</script>

<template>
  <div id="CreatePostBox" class="w-full bg-theme-bg-secondary rounded-lg px-3 mt-4 shadow-md dark:shadow-lg">
    <div class="flex items-center py-3 border-b border-b-gray-300 dark:border-b-gray-700">
      <a class="mr-2">
        <img class="rounded-full ml-1 min-w-[36px] max-h-[36px]" :src="image || ''" />
      </a>
      <div
        class="flex items-center justify-start bg-[#EFF2F5] dark:bg-gray-700 hover:bg-[#E5E6E9] dark:hover:bg-gray-600 p-2 rounded-full w-full cursor-pointer"
      >
        <div class="text-left pl-2 text-theme-text-secondary ">{{ placeholder }}</div>
      </div>
    </div>

    <div class="flex items-center py-2">
      <button
        class="flex items-center justify-center hover:bg-theme-hover p-1  w-full rounded-lg cursor-pointer"
      >
        <VideoImage :size="35" fillColor="#F12848" />
        <div class="text-[#6F7275] text-theme-text-secondary  font-medium">{{ $t('post.addLive') }}</div>
      </button>
      <button
        class="flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
      >
        <Image :size="35" fillColor="#43BE62" />
        <div class="text-[#6F7275] text-theme-text-secondary  font-medium">{{ $t('post.addPhoto') }}</div>
      </button>
      <button
        class="flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
      >
        <EmoticonOutline :size="35" fillColor="#F8B927" />
        <div class="text-[#6F7275] text-theme-text-secondary  font-medium">{{ $t('post.addFeeling') }}</div>
      </button>
    </div>
  </div>
</template>
