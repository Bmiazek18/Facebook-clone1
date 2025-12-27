<template>
  <MainNavLayout v-if="showMainLayout"/>

  <router-view :class="showMainLayout ? 'mt-[60px]' : ''"/>
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
import { useChatStore } from './stores/chat'
const chatStore = useChatStore()
import ProfileIcon from './components/ProfileIcon.vue'
import { useTheme } from './composables/useTheme'
import MessageBox from './components/MessageBox.vue'

useTheme()

const route = useRoute()

const showMainLayout = computed(() => {
   const metaVal = (route && route.meta && (route.meta as Record<string, unknown>).showMainLayout);
   return metaVal === false ? false : true;
})

</script>
