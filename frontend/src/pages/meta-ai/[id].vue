<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LLMInput from '@/components/meta-ai/LLMInput.vue'
import CodeBlock from '@/components/meta-ai/CodeBlock.vue'
import PdfPreview from '@/components/meta-ai/PdfPreview.vue'
import CustomLightbox from '@/components/meta-ai/CustomLightbox.vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import katex from 'katex'

import 'katex/dist/katex.min.css'
import pdf from '@/assets/projekt_PO_wirtualny_swiat.pdf?url'

definePageMeta({
  layout: 'meta-ai',
  showMainLayout: false,
})

const sanitizeHTML = DOMPurify.sanitize || (DOMPurify as any).default?.sanitize

// --- INTERFEJSY ---
interface FileAttachment {
  name: string
  url: string
  type: string
}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  parsedTokens?: any[]
  images?: string[]
  files?: FileAttachment[]
  isStreaming?: boolean
}

// --- DYNAMICZNY STAN WĄTKU (THREAD ID) ---
const currentThreadId = ref<string>("")

const generateUUID = () => {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

const WELCOME_TEXT = `Witaj! Mogę wygenerować dla Ciebie wykresy, przeszukać sieć lub odpowiedzieć na Twoje pytania.`

const initNewChat = () => {
  currentThreadId.value = generateUUID()
  messages.value = [
    { 
      id: Date.now(), 
      role: 'assistant', 
      content: WELCOME_TEXT, 
      parsedTokens: marked.lexer(WELCOME_TEXT) 
    }
  ]
}

const loadExistingChat = async (threadId: string) => {
  if (!threadId) return
  currentThreadId.value = threadId
  
  try {
    const response = await fetch(`http://localhost:8000/chat-history/${threadId}`)
    if (response.ok) {
      const data = await response.json()
      
      messages.value = (data.messages || []).map((m: any) => ({
        id: m.id,
        role: m.role,
        content: m.content,
        parsedTokens: marked.lexer(m.content)
      }))
      
      if (messages.value.length === 0) {
        messages.value.push({
          id: Date.now(),
          role: 'assistant',
          content: WELCOME_TEXT,
          parsedTokens: marked.lexer(WELCOME_TEXT)
        })
      }
      
      await nextTick()
      if (scrollContainer.value) {
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
      }
    } else {
      console.error("Backend zwrócił błąd podczas pobierania historii.")
    }
  } catch (error) {
    console.error("Błąd sieci podczas pobierania historii czatu:", error)
  }
}

// --- REAKTYWNE ZMIENNE WIDOKU ---
const messages = ref<Message[]>([])
const userInput = ref("")
const scrollContainer = ref<HTMLElement | null>(null)
const route = useRoute()
const router = useRouter()
let abortController: AbortController | null = null

const isAssistantStreaming = computed(() => {
  return messages.value.some(m => m.role === 'assistant' && m.isStreaming)
})

const expandedMessages = ref<Record<number, boolean>>({})
const shouldShowExpandButton = (content: string) => {
  const lineCount = content.split('\n').length
  return lineCount > 5 || content.length > 350
}

const toggleExpand = (msgId: number) => {
  expandedMessages.value[msgId] = !expandedMessages.value[msgId]
}

const copiedId = ref<number | null>(null)
const copyToClipboard = async (text: string, msgId: number) => {
  try {
    await navigator.clipboard.writeText(text)
    copiedId.value = msgId
    setTimeout(() => { copiedId.value = null }, 2000)
  } catch (err) {
    console.error('Nie udało się skopiować tekstu:', err)
  }
}

const editMessage = (msg: Message) => {
  userInput.value = msg.content
}

const activePdfUrl = ref<string | null>(null)
const isSidePanelOpen = ref(false)
const pdfSearchQuery = ref<string>("")
const HARDCODED_PDF_URL = pdf

const openPdfPreview = (url: string, searchText: string = "") => {
  pdfSearchQuery.value = searchText
  activePdfUrl.value = url
  isSidePanelOpen.value = true
}

const closeSidePanel = () => {
  isSidePanelOpen.value = false
  nextTick(() => {
    activePdfUrl.value = null
    pdfSearchQuery.value = ""
  })
}

const goToPdfPage = (page: number, searchText: string = "") => {
  activePdfUrl.value = null
  pdfSearchQuery.value = searchText
  nextTick(() => {
    activePdfUrl.value = `${HARDCODED_PDF_URL}#page=${page}`
    isSidePanelOpen.value = true
  })
}

const lightboxVisible = ref(false)
const lightboxIndex = ref(0)
const lightboxImages = ref<string[]>([])

const showImage = (url: string, allImages: string[] = []) => {
  lightboxImages.value = allImages.length > 0 ? allImages : [url]
  lightboxIndex.value = allImages.indexOf(url) !== -1 ? allImages.indexOf(url) : 0
  lightboxVisible.value = true
}

const handleMessageClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const ragHighlight = target.closest('.rag-highlight')
  if (ragHighlight) {
    const page = ragHighlight.getAttribute('data-page')
    const textToHighlight = ragHighlight.querySelector('.highlight-text')?.textContent || ""
    if (page) goToPdfPage(parseInt(page, 10), textToHighlight)
  }
}

onMounted(() => { 
  (window as any).goToPdfPage = (page: number) => goToPdfPage(page, "")
  
  const urlThreadId = route.params.id || route.query.thread_id
  
  if (urlThreadId) {
    loadExistingChat(urlThreadId as string)
  } else {
    initNewChat()
  }

  if (route.query.q) {
    const modelMode = (route.query.model as string) || 'Flash'
    handleChatSubmit({ text: route.query.q as string, model: modelMode, images: [] })
    router.replace({ query: {} })
  }
})

onUnmounted(() => { 
  delete (window as any).goToPdfPage 
})

const renderSimpleToken = (token: any) => {
  let content = token.raw || token.text || ''
  const latexRenderer = (match: string, equation: string, isDisplay: boolean) => {
    try {
      const rawEq = equation.replace(/\\\\/g, '\\')
      return katex.renderToString(rawEq.trim(), { displayMode: isDisplay, throwOnError: false })
    } catch (err) {
      return match
    }
  }

  content = content.replace(/\\\[([\s\S]*?)\\\]/g, (m, eq) => latexRenderer(m, eq, true))
  content = content.replace(/\\\(([\s\S]*?)\\\)/g, (m, eq) => latexRenderer(m, eq, false))
  content = content.replace(/\$\.(.*?)\$\$/gs, (m, eq) => latexRenderer(m, eq, true))

  content = content.replace(/\]\(generated_charts\//g, '](http://localhost:8000/generated_charts/')

  let html = marked.parse(content, { breaks: true }) as string
  
  return sanitizeHTML(html, { 
    ALLOWED_TAGS: [
      'p', 'a', 'ul', 'ol', 'li', 'b', 'i', 'strong', 'em', 'span', 'div', 'img', 
      'svg', 'path', 'polyline', 'math', 'annotation', 'semantics', 'mrow', 'mi', 'mn', 'mo', 'msup', 'sub', 'sup', 'mfrac', 'mover', 'munder', 'munderover', 'mspace'
    ],
    ALLOWED_ATTR: [
      'href', 'src', 'alt', 'title', 'class', 'style', 'data-page', 'data-ref', 'aria-hidden', 'viewBox', 'focusable'
    ]
  })
}

const handleStopGeneration = () => {
  if (abortController) {
    abortController.abort()
    const currentMsg = messages.value.find(m => m.role === 'assistant' && m.isStreaming)
    if (currentMsg) currentMsg.isStreaming = false
  }
}

const handleChatSubmit = async (payload: { text: string, model?: string, images: string[], files?: FileAttachment[] }) => {
  const { text, model = 'Flash', images, files = [] } = payload
  userInput.value = ""
  messages.value.push({ id: Date.now(), role: 'user', content: text, images, files })
  await startChat(text, model, images, files)
}

const startChat = async (text: string, model: string = 'Flash', images: string[] = [], files: FileAttachment[] = []) => {
  const assistantId = Date.now() + 1

  messages.value.push({ id: assistantId, role: 'assistant', content: '', parsedTokens: [], isStreaming: true })
  
  await nextTick()
  const userMsgIndex = messages.value.length - 2 
  const msgElement = document.getElementById('msg-' + userMsgIndex)
  
  if (msgElement && scrollContainer.value) {
    scrollContainer.value.scrollTo({
      top: msgElement.offsetTop, 
      behavior: 'smooth'
    })
  }
  
  abortController = new AbortController()
  
  try {
    const response = await fetch('http://localhost:8000/process-chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        query: text, 
        thread_id: currentThreadId.value,
        model: model
      }),
      signal: abortController.signal
    })

    
    if (!response.body) return
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    const currentMsg = messages.value.find(m => m.id === assistantId)

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      if (currentMsg) {
        currentMsg.content += chunk
        currentMsg.parsedTokens = marked.lexer(currentMsg.content)
      }
    }
    if (currentMsg) currentMsg.isStreaming = false

  } catch (e: any) { 
    if (e.name !== 'AbortError') {
      console.error("Błąd strumienia:", e) 
    }
  }
}
</script>

<template>
  <div class="flex h-screen w-full bg-[#141414] text-[#e3e3e3] font-sans overflow-hidden">
    <div 
      class="w-full h-full max-w-[1800px] mx-auto overflow-hidden transition-all duration-500 ease-in-out"
      :class="isSidePanelOpen ? 'grid grid-cols-1 lg:grid-cols-[minmax(360px,1fr)_minmax(0,2fr)] gap-4 lg:gap-6' : 'flex justify-center'"
    >
      <div 
        class="flex flex-col relative h-full w-full min-h-0 min-w-0"
        :class="!isSidePanelOpen ? 'max-w-3xl' : ''"
      >
        <main 
          ref="scrollContainer" 
          class="flex-1 overflow-y-auto px-4 pb-32 [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-thumb]:bg-white/10 [&::-webkit-scrollbar-thumb]:rounded-full"
        >
          <div class="max-w-3xl mx-auto py-8 space-y-10">
            <div v-for="(msg, index) in messages" :key="msg.id" :id="'msg-' + index" class="flex flex-col group/row">
              <div v-if="msg.role === 'user'" class="flex flex-col items-end mb-2 w-full">
                <div v-if="(msg.images && msg.images.length > 0) || (msg.files && msg.files.length > 0)" class="flex flex-wrap gap-3 mb-2 justify-end max-w-[70%]">
                  <img 
                    v-for="img in msg.images" 
                    :key="img" 
                    :src="img" 
                    class="w-[112px] h-[112px] rounded-2xl border border-white/10 cursor-zoom-in hover:opacity-90 transition shadow-md object-cover"
                    @click="showImage(img, msg.images)" 
                  />

                  <div 
                    v-for="file in msg.files" 
                    :key="file.url"
                    @click="openPdfPreview(file.url)"
                    class="w-[112px] h-[112px] rounded-2xl border border-white/10 bg-[#1e1f20] hover:bg-[#252627] transition flex flex-col justify-between p-3 cursor-pointer shadow-md select-none relative"
                  >
                    <div class="text-[11px] font-extrabold text-white tracking-wider">PDF</div>
                    <div class="text-[12px] text-[#e3e3e3]/90 truncate w-full font-medium">{{ file.name }}</div>
                  </div>
                </div>

                <div v-if="msg.content" class="bg-[#1e1f20] px-[24px] pt-4 pb-4 rounded-[28px] max-w-[70%] border border-white/[0.04] shadow-md relative min-h-[56px]">
                  <div 
                    class="text-[15px] whitespace-pre-wrap text-[#e3e3e3] leading-relaxed transition-all duration-200 overflow-hidden"
                    :class="{ 'line-clamp-5': shouldShowExpandButton(msg.content) && !expandedMessages[msg.id] }"
                  >
                    {{ msg.content }}
                  </div>
                  
                  <div v-if="shouldShowExpandButton(msg.content)" class="absolute right-4 bottom-4">
                    <Tooltip placement="top" :distance="6" theme="gemini-action-tooltip">
                      <button @click="toggleExpand(msg.id)" class="text-white p-1 rounded-full hover:bg-white/5 transition flex items-center justify-center w-7 h-7">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" :class="['transition-transform duration-200', expandedMessages[msg.id] ? 'rotate-180' : '']">
                          <polyline points="6 9 12 15 18 9"></polyline>
                        </svg>
                      </button>
                      <template #popper>{{ expandedMessages[msg.id] ? 'Zwiń' : 'Rozwiń' }}</template>
                    </Tooltip>
                  </div>
                </div>

                <div class="flex items-center gap-1 mt-1.5 text-white opacity-0 group-hover/row:opacity-100 focus-within:opacity-100 transition-opacity duration-200 mr-2">
                  <Tooltip placement="bottom" :distance="6" theme="gemini-action-tooltip">
                    <button @click="editMessage(msg)" class="p-2 hover:bg-white/5 rounded-full transition flex items-center justify-center w-8 h-8">
                      <Icon name="lucide:pencil" size="14" />
                    </button>
                    <template #popper>Edytuj</template>
                  </Tooltip>

                  <Tooltip placement="bottom" :distance="6" theme="gemini-action-tooltip">
                    <button @click="copyToClipboard(msg.content, msg.id)" class="p-2 hover:bg-white/5 rounded-full transition flex items-center justify-center w-8 h-8">
                      <Icon :name="copiedId === msg.id ? 'lucide:check' : 'lucide:copy'" size="14" :class="copiedId === msg.id ? 'text-green-400' : ''" />
                    </button>
                    <template #popper>{{ copiedId === msg.id ? 'Skopiowano!' : 'Kopiuj' }}</template>
                  </Tooltip>
                </div>
              </div>

              <div 
                v-else 
                class="flex gap-4 items-start mb-4 w-full transition-all duration-300"
                :class="{ 'min-h-[80vh]': msg.isStreaming || index === messages.length - 1 }"
              >
                <div class="relative w-9 h-9 flex-shrink-0 mt-1 flex items-center justify-center">
                  <div v-if="msg.isStreaming" class="absolute inset-0 flex items-center justify-center">
                    <div class="gemini-orbit-container">
                      <div class="gemini-orbit-white-dot"></div>
                    </div>
                  </div>
                  <span class="text-blue-500 text-[30px] z-10 select-none transition-transform duration-200" :class="{ 'scale-90 opacity-80': msg.isStreaming }">✦</span>
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="markdown-content prose prose-invert max-w-none" @click="handleMessageClick">
                    <template v-for="(token, tIdx) in msg.parsedTokens" :key="tIdx">
                      <CodeBlock v-if="token.type === 'code'" :code="token.text" :lang="token.lang" />
                      <div v-else v-html="renderSimpleToken(token)" class="inline-render"></div>
                    </template>
                  </div>

                  <div v-if="!msg.isStreaming" class="flex items-center gap-1 mt-3 text-white opacity-0 group-hover/row:opacity-100 focus-within:opacity-100 transition-opacity duration-200">
                    <Tooltip placement="bottom" :distance="6" theme="gemini-action-tooltip">
                      <button class="p-2 hover:bg-white/5 rounded-full transition flex items-center justify-center w-8 h-8">
                        <Icon name="lucide:thumbs-up" size="15" />
                      </button>
                      <template #popper>Dobre</template>
                    </Tooltip>

                    <Tooltip placement="bottom" :distance="6" theme="gemini-action-tooltip">
                      <button class="p-2 hover:bg-white/5 rounded-full transition flex items-center justify-center w-8 h-8">
                        <Icon name="lucide:thumbs-down" size="15" />
                      </button>
                      <template #popper>Złe</template>
                    </Tooltip>

                    <Tooltip placement="bottom" :distance="6" theme="gemini-action-tooltip">
                      <button @click="copyToClipboard(msg.content, msg.id)" class="p-2 hover:bg-white/5 rounded-full transition flex items-center justify-center w-8 h-8">
                        <Icon :name="copiedId === msg.id ? 'lucide:check' : 'lucide:copy'" size="15" :class="copiedId === msg.id ? 'text-green-400' : ''" />
                      </button>
                      <template #popper>{{ copiedId === msg.id ? 'Skopiowano!' : 'Kopiuj' }}</template>
                    </Tooltip>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>

        <footer class="absolute bottom-0 left-0 right-0 pb-[48px] flex justify-center z-20 bg-gradient-to-t from-[#141414] via-[#141414] to-transparent pt-12">
          <div class="w-full max-w-3xl">
            <LLMInput v-model="userInput" :is-loading="isAssistantStreaming" @submit="handleChatSubmit" @stop="handleStopGeneration" />
          </div>
        </footer>
      </div>

      <PdfPreview 
        v-if="isSidePanelOpen"
        class="mt-[24px] mb-[48px]"
        :is-open="isSidePanelOpen"
        :url="activePdfUrl"
        :highlight-text="pdfSearchQuery"
        @close="closeSidePanel"
      />
    </div>

    <CustomLightbox v-model:visible="lightboxVisible" v-model:index="lightboxIndex" :imgs="lightboxImages" />
  </div>
</template>

<style>
.v-popper--theme-gemini-action-tooltip .v-popper__inner {
  background: #2a2b2c !important;
  color: #c4c7c5 !important;
  font-size: 12px !important;
  font-weight: 400 !important;
  padding: 5px 10px !important;
  border-radius: 8px !important;
  border: 1px solid rgba(255, 255, 255, 0.04) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4) !important;
}
.v-popper--theme-gemini-action-tooltip .v-popper__arrow-outer,
.v-popper--theme-gemini-action-tooltip .v-popper__arrow-inner {
  border-color: #2a2b2c !important;
}
</style>

<style scoped>
.gemini-orbit-container {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  animation: orbit-variable-speed 1.5s cubic-bezier(0.77, 0, 0.175, 1) infinite;
}
.gemini-orbit-white-dot {
  position: absolute;
  top: 0px;
  left: calc(50% - 3.5px);
  width: 7px;
  height: 7px;
  background-color: #ffffff; 
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}
@keyframes orbit-variable-speed {
  0% { transform: rotate(0deg); }
  50% { transform: rotate(180deg); }
  75% { transform: rotate(290deg); }
  100% { transform: rotate(360deg); }
}

:deep(.rag-highlight) {
  display: inline;
  position: relative;
  cursor: pointer;
}
:deep(.highlight-text) {
  border-bottom: 1px dashed rgba(59, 130, 246, 0.5);
  transition: all 0.2s ease-in-out;
}
:deep(.rag-highlight:hover .highlight-text) {
  background-color: rgba(59, 130, 246, 0.2);
  border-bottom: 1px solid #3b82f6;
  color: #60a5fa;
}
:deep(.page-link-container) {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
}
:deep(.page-link-text) {
  font-size: 13px;
  font-weight: 700;
  color: #3b82f6;
  white-space: nowrap;
}
:deep(.pdf-icon) {
  width: 14px;
  height: 14px;
  margin-right: 2px;
  display: inline-block;
  vertical-align: middle;
}
:deep(.katex-display) {
  margin: 1em 0;
  overflow-x: auto;
  overflow-y: hidden;
}
</style>
