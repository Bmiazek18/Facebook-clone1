<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ActiveStoriesList from '@/components/feed/stories/list/ActiveStoriesList.vue'
import AppCloseHeader from '@/components/common/AppCloseHeader.vue'

const { t } = useI18n()
</script>

<template>
  <aside
    class="w-[360px] shrink-0 bg-theme-bg-secondary shadow-xl flex flex-col h-full z-20 hidden md:flex border-r border-gray-200 dark:border-gray-700"
  >
    <AppCloseHeader class="mt-3 pl-3 pb-3" />
    <div class="pt-2 px-4 pb-2">
      <h1 class="text-2xl font-semibold text-black dark:text-gray-200 tracking-tight">
        {{ t('story.stories') }}
      </h1>
      <div class="flex gap-4 mt-1 text-[15px] text-blue-500 border-theme-border">
        <span class="cursor-pointer hover:underline">{{ t('story.archive') }}</span>
        <span class="cursor-pointer hover:underline">{{ t('story.settings') }}</span>
      </div>
    </div>
    <div class="px-2 py-2">
      <h2 class="px-2 text-[17px] font-semibold text-black dark:text-gray-200 mb-2">
        {{ t('story.yourStory') }}
      </h2>
      <NuxtLink to="/stories/create">
        <div class="flex items-center gap-3 p-2 rounded-lg cursor-pointer transition">
          <div class="relative">
            <div
              class="w-[60px] h-[60px] bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center"
            >
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-blue-500">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                  <path d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"></path>
                </svg>
              </div>
            </div>
          </div>
          <div>
            <p class="font-semibold text-black dark:text-gray-200 text-[15px]">
              {{ t('story.createStory') }}
            </p>
            <p class="text-[13px] text-gray-500 dark:text-gray-400">
              {{ t('story.sharePhotoOrWriteSomething') }}
            </p>
          </div>
        </div>
      </NuxtLink>
    </div>
    <div class="px-4 py-2 text-[17px] font-semibold text-black dark:text-gray-200 mt-2">
      {{ t('story.allStories') }}
    </div>
    <div class="flex-1 overflow-y-auto custom-scrollbar px-2 pb-4">
      <ActiveStoriesList />
    </div>
  </aside>
</template>
<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: #a0a4a8;
}
</style>
