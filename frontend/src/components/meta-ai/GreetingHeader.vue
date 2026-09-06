<template>
  <component :is="'style'">
    @import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:wght@300;400;500&display=swap');
  </component>

  <div class="px-4 w-full max-w-3xl text-center min-h-[100px] flex flex-col justify-center items-center">
    <transition name="slide-in" appear>
      <h1
        v-if="showGreeting"
        class="gemini-exact-title select-none"
      >{{ $t('metaAi.whatSTheVibe') }}</h1>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const showGreeting = ref(false)

onMounted(() => {
  setTimeout(() => {
    showGreeting.value = true
  }, 100)
})
</script>

<style scoped>
.gemini-exact-title {
  color: rgb(227, 227, 227);
  font-family: "Google Sans Flex", "Google Sans", "Helvetica Neue", sans-serif;
  font-size: 36px;
  font-weight: 320;
  line-height: 44px;
  letter-spacing: normal;
  font-variation-settings: "ROND" 100, "slnt" 0, "wdth" 100;
  display: block;
}

.slide-in-enter-active {
  transition: opacity 1.2s cubic-bezier(0.2, 0, 0, 1), transform 1.2s cubic-bezier(0.2, 0, 0, 1);
}
.slide-in-enter-from {
  opacity: 0;
  transform: translateY(30px);
}
.slide-in-enter-to {
  opacity: 1;
  transform: translateY(0);
}
</style>
