<template>
  <div
    class="w-[113px] h-[200px] mt-3 rounded-xl shadow-sm overflow-hidden flex flex-col relative group transition duration-300 ease-in-out cursor-pointer shrink-0"
    @click="createStory"
  >
    <div class="absolute inset-x-0 top-0 h-[70%] z-0">
      <img
        :src="auth.currentUser?.avatar"
        :alt="$t('feed.osobyNaStacjiMetra')"
        class="w-full h-full object-cover transition duration-300 ease-in-out group-hover:scale-103 filter brightness-90 contrast-100 group-hover:brightness-85 group-hover:contrast-105"
      />
    </div>

    <div
      class="absolute inset-x-0 bottom-0 h-[30%] bg-theme-bg-secondary pt-4 flex flex-col items-center justify-start z-45 group-hover:bg-gray-200 transition-colors duration-300 ease-in-out"
    >
      <button
        class="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center shadow-md transition-colors mt-[-30px] ring-4 dark:ring-[#252729] p-0"
        :aria-label="$t('story.createStory')"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="3"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
      </button>

      <p class="text-theme-text text-center text-xs font-semibold leading-tight pt-1">{{ $t('feed.utworz') }}<br />{{ $t('feed.relacje') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const router = useRouter()

const auth = useAuthStore()
const createStory = () => {
  router.push('/stories/create')
}
</script>
