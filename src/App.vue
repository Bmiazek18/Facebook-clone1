<template>
  <MainNavLayout v-if="showMainLayout"/>

  <router-view />
  <div v-if="!isInChatView" class="fixed flex flex-row bottom-0 right-[60px] z-50">
      <MessageBox
        v-for="boxId in chatStore.getBoxIds"
        :key="boxId"
        :boxId="boxId"
     />
  </div>

  <ProfileIcon v-if="!isInChatView"/>

</template>

<script setup lang="ts">


import MainNavLayout from './layouts/MainNavLayouts.vue'
import { useRoute } from 'vue-router'
import { computed,onMounted, onUnmounted } from 'vue'

import 'floating-vue/dist/style.css'
import { useChatStore } from './stores/chat'
const chatStore = useChatStore()
import ProfileIcon from './components/profile/ProfileIcon.vue'
import { useTheme } from './composables/useTheme'
import MessageBox from '@/components/chat/messageBox/index.vue'

useTheme()

const route = useRoute()

const showMainLayout = computed(() => {
   const metaVal = (route && route.meta && (route.meta as Record<string, unknown>).showMainLayout);
   return metaVal === false ? false : true;
})

const isInChatView = computed(() => {
   return route.path.startsWith('/chat');
})

import { useNotify } from '@/composables/useNotify'; // Sprawdź ścieżkę

const notify = useNotify();



onMounted(() => {
  window.addEventListener('offline', () => notify.offline());
  window.addEventListener('online', () => notify.online());

  // Sprawdzenie na starcie
  if (!navigator.onLine) notify.offline();
});

onUnmounted(() => {
  window.removeEventListener('offline', () => notify.offline());
  window.removeEventListener('online', () => notify.online());
});
</script>
