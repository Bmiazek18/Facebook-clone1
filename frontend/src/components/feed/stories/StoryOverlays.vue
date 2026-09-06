<script setup lang="ts">
import { type PropType } from 'vue'
import type { Story } from '@/types/Story'
import AccountCircle from 'vue-material-design-icons/AccountCircle.vue'
import Play from 'vue-material-design-icons/Play.vue'
import Pause from 'vue-material-design-icons/Pause.vue'
import VolumeMute from 'vue-material-design-icons/VolumeMute.vue'
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'

defineProps({
  storyItems: {
    type: Array as PropType<Story[]>,
    required: true,
  },
  currentStoryIndex: {
    type: Number,
    required: true,
  },
  progress: {
    type: Number,
    required: true,
  },
  currentItem: {
    type: Object as PropType<
      Story & {
        type: string
        src: string
        user: { name: string; avatar: string }
        createAt: string
      }
    >,
    required: true,
  },
  musicElement: {
    type: Object,
    default: null,
  },
  isPaused: {
    type: Boolean,
    required: true,
  },
  isVideo: {
    type: Boolean,
    required: true,
  },
  isMusicPlaying: {
    type: Boolean,
    required: true,
  },
  storyMusicMuted: {
    type: Boolean,
    required: true,
  },
  togglePlay: {
    type: Function,
    required: true,
  },
  toggleMasterMute: {
    type: Function,
    required: true,
  },
})
</script>

<template>
  <div>
    <div class="absolute top-2 left-2 right-2 flex gap-1 z-30 h-1">
      <div
        v-for="(item, index) in storyItems"
        :key="item.id"
        class="flex-1 bg-white/30 rounded-full overflow-hidden h-full backdrop-blur-sm"
      >
        <div
          class="h-full bg-white transition-all duration-100 ease-linear"
          :style="{
            width:
              index < currentStoryIndex
                ? '100%'
                : index === currentStoryIndex
                  ? progress + '%'
                  : '0%',
          }"
        ></div>
      </div>
    </div>

    <div class="absolute top-5 left-4 right-4 flex justify-between items-start z-30">
      <div class="flex items-center gap-3">
        <div
          class="w-10 h-10 rounded-full border border-gray-400 overflow-hidden bg-gray-500 flex items-center justify-center"
        >
          <img
            v-if="currentItem.user.avatar"
            :src="currentItem.user.avatar"
            :alt="$t('feed.userAvatar')"
            class="w-full h-full object-cover"
          />
          <AccountCircle v-else :size="42" class="text-gray-300" />
        </div>
        <div class="flex flex-col text-white drop-shadow-md leading-tight">
          <div class="flex items-center gap-2">
            <span class="font-semibold text-[15px] hover:underline cursor-pointer">{{
              currentItem.user.name
            }}</span>
            <span class="text-white/80 text-[13px]">{{ currentItem.createAt }}</span>
          </div>
          <div
            v-if="musicElement"
            class="flex items-center gap-1 text-[13px] font-medium text-white/90"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              class="w-3 h-3"
            >
              <path
                d="M18.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM12.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM6.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM18.75 8.25h2.25c.621 0 1.125.504 1.125 1.125v10.5a3 3 0 01-3 3H3.375a3 3 0 01-3-3V9.375c0-.621.504-1.125 1.125-1.125h2.25h15z"
              />
            </svg>
            <span>{{ musicElement.musicArtist }} - {{ musicElement.musicTitle }}</span>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-4 text-white drop-shadow-md z-40">
        <div class="cursor-pointer hover:opacity-80 transition" @click.stop="togglePlay">
          <Play v-if="isPaused" :size="24" />
          <Pause v-else :size="24" />
        </div>
        <div
          v-if="isVideo || isMusicPlaying"
          class="cursor-pointer hover:opacity-80 transition"
          @click.stop="toggleMasterMute"
        >
          <VolumeMute v-if="storyMusicMuted" :size="24" />
          <VolumeHigh v-else :size="24" />
        </div>
        <div class="cursor-pointer hover:opacity-80">
          <DotsHorizontal :size="24" />
        </div>
      </div>
    </div>
  </div>
</template>
