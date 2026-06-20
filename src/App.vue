<template>
  <MainNavLayout v-if="showMainLayout && !isPopupRoute"/>

  <router-view />
  <div v-if="!isInChatView&& !isPopupRoute" class="fixed flex flex-row bottom-0 right-[60px] z-40">
      <MessageBox
        v-for="boxId in chatStore.getBoxIds"
        :key="boxId"
        :boxId="boxId"
     />
  </div>

  <ProfileIcon v-if="!isInChatView&& !isPopupRoute"/>

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
const isPopupRoute = computed(() => {
  const metaVal = (route && route.meta && (route.meta as Record<string, unknown>).isPopup);
  return metaVal === true ? true : false;
})
const showMainLayout = computed(() => {
   const metaVal = (route && route.meta && (route.meta as Record<string, unknown>).showMainLayout);
   return metaVal === false ? false : true;
})

const isInChatView = computed(() => {
   return route.path.startsWith('/chat');
})

import { useNotify } from '@/composables/useNotify'; // Sprawdź ścieżkę

const notify = useNotify();


import { useVisitorData } from '@fingerprint/vue'

// Inicjujemy hook bez natychmiastowego wywołania
const { getData } = useVisitorData({ immediate: false })

onMounted(async () => {
  try {
    const result = await getData()
    console.log('Dane Fingerprint pobrane:', result)

    if (result && result.visitor_id) {
      // ZMIANA: SameSite=Lax zamiast Strict dla środowiska lokalnego
      document.cookie = `visitorId=${result.visitor_id}; max-age=31536000; path=/; SameSite=Lax`
      console.log('Zapisano Fingerprint ID w cookie:', result.visitor_id)
    }
  } catch (error) {
    console.error('Błąd podczas pobierania danych Fingerprint:', error)
  }
})

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
