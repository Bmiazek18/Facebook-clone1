<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import LLMInput from '@/components/meta-ai/LLMInput.vue'
import GreetingHeader from '@/components/meta-ai/GreetingHeader.vue'

definePageMeta({
  layout: 'meta-ai',
  showMainLayout: false,
})

const userInput = ref("")
const router = useRouter()

const handleStart = (payload: { text?: string, model?: string } | any) => {
  const queryText = typeof payload === 'string' ? payload : (payload?.text || userInput.value)
  if (!queryText.trim()) return
  
  const chatId = Date.now().toString()
  const modelMode = payload?.model || 'Flash'
  
  router.push({
    path: `/meta-ai/${chatId}`,
    query: { q: queryText, model: modelMode }
  })
}

</script>

<template>
  <div class="relative flex h-screen bg-[#141414] flex-col justify-center items-center px-4">
    <div class="absolute inset-0 overflow-hidden pointer-events-none z-0">
      <div class="gemini-glow-container"></div>
    </div>
    
    <div class="w-full max-w-3xl flex flex-col justify-center items-center flex-1 z-10">
      <GreetingHeader />
      
      <div class="w-full animated-input-bar mt-6 z-10">
        <LLMInput v-model="userInput" @submit="handleStart" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.animated-input-bar {
  view-transition-name: llm-input-bar;
}

.gemini-glow-container {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: -1;
  margin: 0 auto;
  width: 100%;
  max-width: 850px; 
  height: 50%;
  max-height: 350px; 
  pointer-events: none;
  border-radius: 9999px;
  -webkit-filter: blur(125px);
  filter: blur(125px);
  background: -webkit-radial-gradient(center 8%, ellipse 100% 100%, #1d4ed8 0%, #6366f1 35%, #141414 70%);
  background: radial-gradient(ellipse 100% 100% at center 8%, #1d4ed8 0%, #6366f1 35%, #141414 70%);
  -webkit-transform: translate(-50%, -45%);
  transform: translate(-50%, -45%);
  -webkit-animation: lm-background-grow 1s cubic-bezier(0.2, 0, 0, 1) both;
  animation: lm-background-grow 1s cubic-bezier(0.2, 0, 0, 1) both;
}

@keyframes lm-background-grow {
  0% {
    transform: translate(-50%, -45%) scale(0.85);
    opacity: 0;
  }
  100% {
    transform: translate(-50%, -45%) scale(1);
    opacity: .35;
  }
}

@-webkit-keyframes lm-background-grow {
  0% {
    -webkit-transform: translate(-50%, -45%) scale(0.85);
    opacity: 0;
  }
  100% {
    -webkit-transform: translate(-50%, -45%) scale(1);
    opacity: 1;
  }
}
</style>
