      <script setup lang="ts">
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import CheckCircle from 'vue-material-design-icons/CheckCircle.vue';
import HoverScrollbar from '@/components/HoverScrollbar.vue';
import { useTheme } from '@/composables/useTheme';

const { isDark } = useTheme();

defineProps<{
  birthdayUser?: string;
}>();




import rawChats from '@/data/rawChats';
import { useChatStore } from '@/stores/chat';
import { computed } from 'vue';

const chatStore = useChatStore();

const contacts = computed(() => {
  return rawChats.map(chat => ({
    id: chat.id,
    name: chat.name,
    avatarUrl: chat.avatarUrl,
    status: true,
  }));
});

const openChatBox = (id: number) => {
  chatStore.addMessageBox(id);
};
</script>

<template>
  <div class="max-w-[360px] min-w-[280px] ml-auto sticky top-[72px]">
    <HoverScrollbar>
      <div class="max-h-[calc(100vh-72px)] pr-2 pl-2">
        <div class="font-semibold border-b border-b-theme-border text-theme-text">
          {{ $t('home.birthday') }}
        </div>

        <div class="flex items-center gap-2 p-2 hover:bg-theme-hover rounded-lg cursor-pointer">
          <span class="text-2xl">🎁</span>
          <p class="text-theme-text text-sm">
            <span class="font-semibold">{{ birthdayUser || 'Bartosz Miazek' }}</span> {{ $t('home.birthdayHas') }}.
          </p>
        </div>
        <div class="flex items-center justify-between pt-4 pb-2">
          <div class="text-[17px] font-bold text-gray-500 dark:text-gray-400">
            Kontakty
          </div>
          <div class="flex items-center gap-1">
            <div class="p-2 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full cursor-pointer transition-colors">
              <Magnify :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
            </div>
            <div class="p-2 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full cursor-pointer transition-colors">
              <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-1">
          <div
            v-for="contact in contacts"
            :key="contact.id"
            @click="openChatBox(contact.id)"
            class="flex items-center p-2 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-800 cursor-pointer transition-colors group relative"
          >
            <div class="relative flex-shrink-0"> <div v-if="contact.isMeta" class="w-[38px] h-[38px] rounded-full p-[2px] bg-gradient-to-tr from-blue-400 via-purple-500 to-pink-500">
                 <div class="w-full h-full bg-white rounded-full flex items-center justify-center overflow-hidden">
                    <div class="w-full h-full bg-gradient-to-tr from-blue-500 to-purple-600 opacity-20"></div>
                 </div>
              </div>

              <img
                v-else
                class="w-[38px] h-[38px] rounded-full object-cover border border-gray-200 dark:border-gray-700"
                :src="contact.avatarUrl"
                alt="Avatar"
              />

              <div
                v-if="contact.status && !contact.isMeta"
                class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-white dark:border-[#18191A]"
              ></div>

              <div
                v-else-if="contact.lastActive && !contact.isMeta"
                class="absolute -bottom-1 left-1/2 transform -translate-x-1/2 bg-white dark:bg-[#18191A] px-[2px] rounded-md"
              >
                <div class="text-[10px] font-bold text-green-600 dark:text-green-500 whitespace-nowrap leading-none">
                  {{ contact.lastActive }}
                </div>
              </div>

            </div>

            <div class="flex items-center ml-3 overflow-hidden">
              <span class="text-[15px] text-[#050505] dark:text-[#E4E6EB] font-medium truncate">
                {{ contact.name }}
              </span>

              <span v-if="contact.isMeta" class="ml-1 flex-shrink-0">
                <CheckCircle :size="16" fillColor="#1877F2" />
              </span>
            </div>
          </div>
        </div>

      </div>
    </HoverScrollbar>
  </div>
</template>
