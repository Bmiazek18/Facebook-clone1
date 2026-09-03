<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSidebar } from '@/composables/useSidebar'

const router = useRouter()
const route = useRoute()
const { isExpanded, toggleSidebar } = useSidebar()

interface ChatThread {
  thread_id: string
  title: string
}
const recentChats = ref<ChatThread[]>([])

const activeThreadId = ref<string | null>(null)

const fetchChats = async () => {
  try {
    const response = await fetch('http://localhost:8000/chat-threads')
    if (response.ok) {
      const data = await response.json()
      recentChats.value = data.threads || []
    }
  } catch (error) {
    console.error('Błąd pobierania listy czatów:', error)
  }
}

const selectChat = async (threadId: string) => {
  activeThreadId.value = threadId
  await router.push(`/meta-ai/${threadId}`)
}

const createNewChat = async () => {
  activeThreadId.value = null
  await router.push('/meta-ai')
}

const goToLive = async () => {
  await router.push('/meta-ai/live')
}

onMounted(() => {
  fetchChats()
  if (route.params.id) {
    activeThreadId.value = route.params.id as string
  }
})

defineExpose({ fetchChats })
</script>

<template>
  <aside
    :class="[
      'fixed left-0 top-0 z-50 flex h-screen flex-col text-[#e3e3e3] transition-all duration-200 ease-in-out select-none font-sans border-r border-white/[0.01]',
      isExpanded ? 'w-[280px] bg-[#1e1f20]' : 'w-[68px] bg-[#131314]'
    ]"
  >
    <div class="flex flex-col pt-4 shrink-0" :class="isExpanded ? 'px-4 gap-3' : 'px-0 items-center gap-6'">
      
      <div class="flex items-center w-full" :class="isExpanded ? 'justify-between pl-2 h-10' : 'justify-center'">
        <div v-if="isExpanded" class="flex items-center gap-2 cursor-pointer" @click="createNewChat">
          <img src="https://www.gstatic.com/lamda/images/gemini_sparkle_v002_d4728d4c12061218db7de.svg" class="w-5 h-5" alt="Meta AI" />
          <span class="text-[18px] font-normal text-[#e3e3e3] tracking-wide">Meta AI</span>
        </div>
        
        <img 
          v-else 
          src="https://www.gstatic.com/lamda/images/gemini_sparkle_v002_d4728d4c12061218db7de.svg" 
          class="w-7 h-7 cursor-pointer hover:opacity-80 transition-opacity mt-1" 
          @click="toggleSidebar"
          alt="Meta AI"
        />

        <button 
          v-if="isExpanded"
          class="w-10 h-10 rounded-full hover:bg-white/[0.04] flex items-center justify-center transition-colors text-[#c4c7c5]" 
          @click="toggleSidebar"
        >
          <Icon name="lucide:menu" size="20" />
        </button>
      </div>

      <div class="flex flex-col w-full mt-2" :class="isExpanded ? 'gap-0.5' : 'gap-5 items-center'">
        
        <!-- PRZYCISK NOWEGO CZATU -->
        <button
          @click="createNewChat"
          :class="[
            'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition-all duration-150 rounded-full w-full',
            isExpanded ? 'gap-4 px-3 h-[40px]' : 'h-5 w-5 justify-center'
          ]"
        >
          <Icon name="lucide:square-pen" :size="isExpanded ? '20' : '17'" class="text-[#e3e3e3] shrink-0" />
          <span v-if="isExpanded" class="text-[14px] font-normal">New chat</span>
        </button>

        <button
          @click="goToLive"
          :class="[
            'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition-all duration-150 rounded-full w-full',
            isExpanded ? 'gap-4 px-3 h-[40px]' : 'h-5 w-5 justify-center'
          ]"
        >
          <Icon name="lucide:sparkles" :size="isExpanded ? '20' : '17'" class="text-[#a8c7fa] shrink-0" />
          <span v-if="isExpanded" class="text-[14px] font-normal">Live</span>
        </button>

        <button
          :class="[
            'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition-all duration-150 rounded-full w-full',
            isExpanded ? 'gap-4 px-3 h-[40px]' : 'h-5 w-5 justify-center'
          ]"
        >
          <Icon name="lucide:search" :size="isExpanded ? '20' : '17'" class="text-[#e3e3e3] shrink-0" />
          <span v-if="isExpanded" class="text-[14px] font-normal">Search chats</span>
        </button>

        <button
          :class="[
            'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition-all duration-150 rounded-full w-full',
            isExpanded ? 'gap-4 px-3 h-[40px]' : 'h-5 w-5 justify-center'
          ]"
        >
          <Icon name="lucide:clapperboard" :size="isExpanded ? '20' : '17'" class="text-[#e3e3e3] shrink-0" />
          <span v-if="isExpanded" class="text-[14px] font-normal">Videos</span>
        </button>

        <button
          :class="[
            'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition-all duration-150 rounded-full w-full',
            isExpanded ? 'gap-4 px-3 h-[40px]' : 'h-5 w-5 justify-center'
          ]"
        >
          <Icon name="lucide:layout-grid" :size="isExpanded ? '19' : '16'" class="text-[#e3e3e3] shrink-0" />
          <span v-if="isExpanded" class="text-[14px] font-normal">Library</span>
        </button>

      </div>
    </div>

    <!-- DYNAMICZNA LISTA CZATÓW -->
    <div v-if="isExpanded" class="custom-scroll mt-4 flex-1 overflow-y-auto px-3">
      <div class="mb-2 pl-3 text-[12px] font-medium text-[#c4c7c5] tracking-wide">
        Recent
      </div>

      <div class="space-y-[2px]">
        <div
          v-for="chat in recentChats"
          :key="chat.thread_id"
          @click="selectChat(chat.thread_id)"
          :class="[
            'group flex cursor-pointer items-center transition-all duration-150 rounded-full py-1.5 pl-3 pr-2 justify-between h-[34px]',
            activeThreadId === chat.thread_id ? 'bg-white/[0.10]' : 'hover:bg-white/[0.05]'
          ]"
        >
          <div class="flex items-center gap-3 min-w-0 flex-1">
            <span class="truncate text-[13.5px] text-[#e3e3e3] font-light tracking-wide">
              {{ chat.title }}
            </span>
          </div>
          
          <div class="flex items-center shrink-0 w-6 h-6 justify-center relative">
            <button 
              class="p-1 hover:bg-white/10 rounded-full transition-opacity absolute opacity-0 group-hover:opacity-100"
            >
              <Icon name="lucide:more-vertical" size="14" class="text-[#c4c7c5]" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="flex-1"></div>

    <div class="mt-auto pb-6 shrink-0 flex flex-col items-center bg-transparent" :class="isExpanded ? 'px-3 pt-2 gap-0.5' : 'px-0 gap-5'">
      
      <button
        :class="[
          'flex items-center text-[#e3e3e3] hover:bg-white/[0.04] transition rounded-full',
          isExpanded ? 'w-full px-3 h-[40px] gap-4' : 'h-5 w-5 justify-center'
        ]"
      >
        <Icon name="lucide:history" :size="isExpanded ? '20' : '17'" class="text-[#e3e3e3] shrink-0" />
        <span v-if="isExpanded" class="text-[13.5px] font-normal tracking-wide">Activity</span>
      </button>

      <div 
        :class="[
          'flex transition justify-between items-center',
          isExpanded ? 'w-full px-2 h-[64px] mt-1 border-t border-white/[0.02]' : 'flex-col gap-5 w-full justify-center'
        ]"
      >
        <button 
          :class="[
            'relative hover:bg-white/[0.04] rounded-full text-[#e3e3e3] shrink-0',
            isExpanded ? 'p-2 order-2' : 'h-5 w-5 flex items-center justify-center order-1'
          ]"
        >
          <Icon name="lucide:settings" :size="isExpanded ? '20' : '17'" />
          <span 
            class="absolute bg-[#1a73e8] rounded-full"
            :class="isExpanded ? 'top-1.5 right-1.5 w-[7px] h-[7px] ring-2 ring-[#1e1f20]' : '-top-0.5 -right-0.5 w-[7.5px] h-[7.5px] ring-2 ring-[#131314]'"
          ></span>
        </button>

        <div class="flex items-center gap-3 min-w-0" :class="!isExpanded && 'order-2'">
          <div class="rounded-full bg-[#007ed6] text-white flex items-center justify-center font-medium shrink-0 select-none cursor-pointer"
               :class="isExpanded ? 'w-7 h-7 text-[13px]' : 'w-[30px] h-[30px] text-[13.5px]'"
               @click="toggleSidebar"
          >
            B
          </div>
          
          <div v-if="isExpanded" class="flex flex-col min-w-0 text-left leading-tight">
            <div class="flex items-center gap-1.5">
              <span class="text-[13.5px] font-normal text-[#e3e3e3] truncate">Bartosz Miazek</span>
              <span class="text-[10px] text-[#c4c7c5] font-normal bg-white/10 px-1 rounded-sm uppercase tracking-wider scale-90">Pro</span>
            </div>
            
            <div class="flex flex-col text-[11px] text-[#c4c7c5] mt-0.5 font-light">
              <div class="flex items-center gap-1">
                <span class="w-[3px] h-[3px] rounded-full bg-[#c4c7c5]"></span>
                <span class="underline cursor-pointer text-[#e3e3e3] hover:text-white">Gdańsk, Poland</span>
              </div>
              <span class="text-[#8e918f] text-[10.5px]">From your IP address</span>
              <span class="underline cursor-pointer text-[#a8c7fa] hover:text-[#c2e7ff] text-[10.5px] mt-0.5">Update location</span>
            </div>
          </div>
        </div>

      </div>

    </div>
  </aside>
</template>

<style scoped>
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 10px; }
</style>
