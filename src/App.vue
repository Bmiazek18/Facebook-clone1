<template>
  <MainNavLayout/>

<router-view />
  <div class="fixed flex flex-row bottom-0 right-[60px]">

     <MessageBox

        v-for="boxId in chatStore.getBoxIds"
        :key="boxId"
        :box-id="boxId"
     />
  </div>

<ProfileIcon/>


</template>

<script setup lang="ts">


import MainNavLayout from './Layouts/MainNavLayouts.vue'

import 'floating-vue/dist/style.css'

import ProfileIcon from './components/ProfileIcon.vue'
import MessageBox from './components/MessageBox.vue'
import { useTheme } from './composables/useTheme'

import { useChatStore } from './stores/counter'

const chatStore = useChatStore()

// Initialize theme on app mount
useTheme()

</script>
