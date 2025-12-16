<script setup lang="ts">
import VideoImage from 'vue-material-design-icons/VideoImage.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import HoverScrollbar from '@/components/HoverScrollbar.vue';
import { useTheme } from '@/composables/useTheme';

const { isDark } = useTheme();

defineProps<{
  birthdayUser?: string;
}>();

// Generate contacts list
const contacts = Array.from({ length: 30 }, (_, i) => ({
  id: i + 1,
  name: `Contact ${i + 1}`,
  avatar: `https://picsum.photos/id/${140 + (i % 10)}/300/320`
}));
</script>

<template>
  <div class="max-w-[340px] min-w-[250px] ml-auto sticky top-[72px]">
    <HoverScrollbar>
      <div class="max-h-[calc(100vh-72px)] pr-2">
        <!-- Birthday Section -->
        <div class="font-semibold border-b border-b-theme-border text-theme-text">
          {{ $t('home.birthday') }}
        </div>

        <div class="flex items-center gap-2 p-2 hover:bg-theme-hover rounded-lg cursor-pointer">
          <span class="text-2xl">🎁</span>
          <p class="text-theme-text text-sm">
            <span class="font-semibold">{{ birthdayUser || 'Bartosz Miazek' }}</span> {{ $t('home.birthdayHas') }}.
          </p>
        </div>

        <!-- Contacts Header -->
        <div class="flex items-center justify-between border-b border-b-theme-border">
          <div class="font-semibold text-theme-text">{{ $t('home.contact') }}</div>
          <div class="flex items-center">
            <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
              <VideoImage :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
            </div>
            <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
              <Magnify :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
            </div>
            <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
              <DotsHorizontal :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
            </div>
          </div>
        </div>

        <!-- Contacts List -->
        <div
          v-for="contact in contacts"
          :key="contact.id"
          class="flex items-center justify-start cursor-pointer hover:bg-theme-hover py-2 rounded-md"
        >
          <img
            class="rounded-full ml-1 min-w-[38px] max-h-[38px]"
            :src="contact.avatar"
          />
          <div class="text-[15px] text-theme-text font-medium pl-3">
            {{ contact.name }}
          </div>
        </div>
      </div>
    </HoverScrollbar>
  </div>
</template>
