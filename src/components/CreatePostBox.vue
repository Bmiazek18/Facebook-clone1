<script setup lang="ts">
import { toRefs, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import VideoImage from 'vue-material-design-icons/VideoImage.vue'
import Image from 'vue-material-design-icons/Image.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import BaseModal from './BaseModal.vue'
import CreatePost from './CreatePost.vue'
import { useCreatePostStore } from '@/stores/createPost'

useI18n()

const createPostStore = useCreatePostStore()
const props = defineProps({
  image: String,
  placeholder: String,
})

const isOpen = ref(false)
const { image, placeholder } = toRefs(props)
const fileInput = ref<HTMLInputElement | null>(null)

// Open modal to create post
const openCreatePost = () => {
  isOpen.value = true
}

const handleFileClick = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];

  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      createPostStore.setSelectedImage({ url: e.target?.result as string, altText: '' });
      isOpen.value = true;
    };
    reader.readAsDataURL(file);
  }
  
  // Reset the input value
  if(target) {
    target.value = ''
  }
}


// Close modal
const closeCreatePost = () => {
  isOpen.value = false
}

// Handle post publish
const handlePostPublish = (content: string) => {
  console.log('Post published:', content)
  // Add any additional handling here if needed
  closeCreatePost()
}
</script>

<template>
  <div id="CreatePostBox" class="w-full bg-theme-bg-secondary rounded-lg px-3 mt-4 shadow-md dark:shadow-lg">
    <div class="flex items-center py-3 border-b border-b-gray-300 dark:border-b-gray-700">
      <a class="mr-2">
        <img class="rounded-full ml-1 min-w-9 max-h-9" :src="image || ''" />
      </a>
      <div
        @click="openCreatePost"
        class="flex items-center justify-start bg-[#EFF2F5] dark:bg-gray-700 hover:bg-[#E5E6E9] dark:hover:bg-gray-600 p-2 rounded-full w-full cursor-pointer"
      >
        <div class="text-left pl-2 text-theme-text-secondary">{{ placeholder }}</div>
      </div>
    </div>

    <div class="flex items-center py-2">
      <RouterLink to="/live/produce"
        class="flex items-center justify-center hover:bg-theme-hover p-1 w-full rounded-lg cursor-pointer"
      >
        <VideoImage :size="35" fillColor="#F12848" />
        <div class="text-[#6F7275] text-theme-text-secondary font-medium">{{ $t('post.addLive') }}</div>
      </RouterLink>
      <button
        @click="handleFileClick"
        class="flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
      >
        <Image :size="35" fillColor="#43BE62" />
        <div class="text-[#6F7275] text-theme-text-secondary font-medium">{{ $t('post.addPhoto') }}</div>
      </button>
      <input
        ref="fileInput"
        type="file"
        accept="image/*,video/mp4"
        class="hidden"
        @change="handleFileSelect"
      />
      <button
        class="flex items-center justify-center hover:bg-theme-hover w-full rounded-lg cursor-pointer"
      >
        <EmoticonOutline :size="35" fillColor="#F8B927" />
        <div class="text-[#6F7275] text-theme-text-secondary font-medium">{{ $t('post.addFeeling') }}</div>
      </button>
    </div>
  </div>

  <BaseModal v-if="isOpen" title="Utwórz post" @close="closeCreatePost">
    <CreatePost @publish="handlePostPublish" />
  </BaseModal>
</template>
