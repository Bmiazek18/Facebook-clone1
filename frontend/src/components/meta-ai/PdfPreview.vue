<script setup lang="ts">
import { ref, shallowRef, watch, onMounted, nextTick } from 'vue'
import staticPdfUrl from '@/assets/projekt_PO_wirtualny_swiat.pdf?url'
import 'vue-pdf-embed/dist/styles/annotationLayer.css'
import 'vue-pdf-embed/dist/styles/textLayer.css'

const props = defineProps<{
  isOpen?: boolean
  url?: string | null
  highlightText?: string
}>()

const emit = defineEmits(['close'])

const VuePdfEmbedComponent = shallowRef<any>(null)
const isClient = ref(false)

const pages = ref<number>(1)
const currentPage = ref(1)
const pageInput = ref('1')
const scale = ref(1)
const isLoading = ref(true)

const scrollContainer = ref<HTMLElement | null>(null)

watch(currentPage, (newPage) => {
  pageInput.value = String(newPage)
})

watch(() => props.url, (newUrl) => {
  if (!newUrl) return
  
  const pageMatch = newUrl.match(/#page=(\d+)/)
  if (pageMatch) {
    const targetPage = parseInt(pageMatch[1], 10)
    nextTick(() => {
      const pageElement = document.querySelector(`[data-page-num="${targetPage}"]`)
      if (pageElement) {
        pageElement.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    })
  }
}, { immediate: true })

const handlePageSubmit = () => {
  const parsed = parseInt(pageInput.value, 10)
  if (!isNaN(parsed) && parsed >= 1 && parsed <= pages.value) {
    const pageElement = document.querySelector(`[data-page-num="${parsed}"]`)
    if (pageElement) {
      pageElement.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  } else {
    pageInput.value = String(currentPage.value)
  }
}

const handleDocumentLoaded = (pdfDoc: any) => {
  if (pdfDoc && pdfDoc.numPages) {
    pages.value = pdfDoc.numPages
  }
}

const handleDocumentRendered = () => {
  isLoading.value = false
  nextTick(() => {
    initIntersectionObserver()
  })
}

const initIntersectionObserver = () => {
  if (!scrollContainer.value) return

  const observerOptions = {
    root: scrollContainer.value,
    rootMargin: '-50% 0px -50% 0px',
    threshold: 0 
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const pageNum = entry.target.getAttribute('data-page-num')
        if (pageNum) {
          currentPage.value = parseInt(pageNum, 10)
        }
      }
    })
  }, observerOptions)

  const pageElements = scrollContainer.value.querySelectorAll('.vue-pdf-embed__page')
  pageElements.forEach((el, index) => {
    el.setAttribute('data-page-num', String(index + 1))
    observer.observe(el)
  })
}

onMounted(async () => {
  if (typeof window === 'undefined') return

  try {
    const module = await import('vue-pdf-embed')
    VuePdfEmbedComponent.value = module.default
    isClient.value = true
  } catch (error) {
    console.error("Błąd ładowania komponentu vue-pdf-embed:", error)
    isLoading.value = false
  }
})

const zoomIn = () => { if (scale.value < 2.5) scale.value = parseFloat((scale.value + 0.1).toFixed(1)) }
const zoomOut = () => { if (scale.value > 0.6) scale.value = parseFloat((scale.value - 0.1).toFixed(1)) }
const resetZoom = () => { scale.value = 1.2 }
</script>

<template>
  <div>
    <div
      v-if="isClient"
      class="flex-1 min-w-0 min-h-[400px] h-full bg-[rgb(31, 31, 31)] rounded-[40px] flex flex-col overflow-hidden shadow-xl relative border border-[#383838]"
    >
      <div class="flex items-center justify-between px-3 py-2.5 bg-[#1e1f20] text-[#f1f3f4] z-20 select-none flex-shrink-0">
        <div class="flex items-center gap-3 min-w-0 flex-1">
          <button @click="emit('close')" class="p-1.5 hover:bg-white/10 text-gray-400 hover:text-white rounded-full transition-colors flex-shrink-0" :title="$t('common.close')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          
          <div class="bg-[#d32f2f] text-white text-[10px] font-bold px-1.5 py-0.5 rounded-[3px] flex-shrink-0 tracking-wide">{{ $t('metaAi.pdf') }}</div>

          <span class="text-[13px] sm:text-[14px] text-gray-200 font-medium truncate tracking-tight pr-4">
            PO_Lab1-2.pdf
          </span>
        </div>

        <div class="flex items-center gap-1 flex-shrink-0 text-gray-400">
          <button class="p-2 hover:bg-white/10 hover:text-white rounded-full transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 6 2 18 2 18 9"></polyline><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path><rect x="6" y="14" width="12" height="8"></rect></svg>
          </button>
          <button class="p-2 hover:bg-white/10 hover:text-white rounded-full transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
          </button>
          <button class="p-2 hover:bg-white/10 hover:text-white rounded-full transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1.5"></circle><circle cx="12" cy="5" r="1.5"></circle><circle cx="12" cy="19" r="1.5"></circle></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 relative bg-[#1e1f20] min-h-0">
        <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-[#1e1f20] z-20">
          <div class="flex flex-col items-center gap-3">
            <div class="w-8 h-8 border-2 border-gray-500 border-t-gray-200 rounded-full animate-spin"></div>
          </div>
        </div>

        <div
          ref="scrollContainer"
          class="absolute inset-0 overflow-auto w-full custom-scroll select-text scroll-smooth py-6"
        >
          <div class="flex flex-col items-center w-full max-w-[800px] mx-auto px-4 relative">
            <div
              class="transition-all duration-300 origin-top max-w-[800px] w-full"
              :style="{ transform: `scale(${scale})`, transformOrigin: 'top center' }"
            >
              <component
                :is="VuePdfEmbedComponent"
                :source="staticPdfUrl"
                @loaded="handleDocumentLoaded"
                :text-layer="true"
                @rendered="handleDocumentRendered"
              />
            </div>
          </div>
        </div>

        <div class="absolute bottom-6 left-1/2 -translate-x-1/2 z-30 flex items-center bg-black/75 rounded-full px-5 py-2 shadow-[0_4px_20px_rgba(0,0,0,0.5)] select-none text-[#f1f3f4] font-sans text-[13px] border border-white/5">
          <div class="flex items-center gap-2.5 pr-4">
            <span class="text-gray-300 font-medium">{{ $t('metaAi.page') }}</span>
            <input
              type="text"
              v-model="pageInput"
              @keydown.enter="handlePageSubmit"
              @blur="handlePageSubmit"
              :disabled="isLoading"
              class="w-10 h-6 bg-[#1a1b1c] text-center rounded-md text-white font-mono border border-transparent focus:border-gray-500 focus:outline-none"
            />
            <span class="text-gray-400">/</span>
            <span class="min-w-[12px] text-center font-mono">{{ pages }}</span>
          </div>
          
          <div class="w-[1px] h-5 bg-[#4d5156]"></div>
          
          <div class="flex items-center pl-4 gap-2">
            <button @click="zoomOut" :disabled="scale <= 0.5 || isLoading" class="p-1.5 hover:bg-white/10 rounded-full transition-colors disabled:opacity-30 text-gray-300 hover:text-white">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </button>
            <button @click="resetZoom" :disabled="isLoading" class="p-1.5 hover:bg-white/10 rounded-full transition-colors text-gray-300 hover:text-white">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line><line x1="11" y1="8" x2="11" y2="14"></line><line x1="8" y1="11" x2="14" y2="11"></line></svg>
            </button>
            <button @click="zoomIn" :disabled="scale >= 2.5 || isLoading" class="p-1.5 hover:bg-white/10 rounded-full transition-colors disabled:opacity-30 text-gray-300 hover:text-white">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scroll::-webkit-scrollbar { width: 10px; height: 10px; }
.custom-scroll::-webkit-scrollbar-track { background: transparent; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5f6368; border-radius: 9999px; border: 2px solid #1e1f20; }
.custom-scroll::-webkit-scrollbar-thumb:hover { background: #9aa0a6; }

:deep(.vue-pdf-embed > div) {
  margin-bottom: 24px !important;
  background-color: white !important;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.6) !important;
  border-radius: 4px !important; 
  overflow: hidden !important;
}

:deep(.vue-pdf-embed > div:last-child) {
  margin-bottom: 0 !important;
}
</style>
