<template>
  <MainNavLayout v-if="showMainLayout"/>

  <router-view />
  <div class="fixed flex flex-row bottom-0 right-[60px]">

     <MessageBox

        v-for="boxId in chatStore.getBoxIds"
        :key="boxId"
        :boxId="boxId"
     />
  </div>

<ProfileIcon/>


</template>

<script setup lang="ts">


import MainNavLayout from './Layouts/MainNavLayouts.vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'

import 'floating-vue/dist/style.css'

import ProfileIcon from './components/ProfileIcon.vue'
import MessageBox from './components/MessageBox.vue'
import { useTheme } from './composables/useTheme'

import { useChatStore } from './stores/counter'

const chatStore = useChatStore()

// Initialize theme on app mount
useTheme()

const route = useRoute()
// If a route sets `meta.showMainLayout === false` we hide the main layout.
const showMainLayout = computed(() => {
   const metaVal = (route && route.meta && (route.meta as Record<string, unknown>).showMainLayout);
   return metaVal === false ? false : true;
})

</script>
