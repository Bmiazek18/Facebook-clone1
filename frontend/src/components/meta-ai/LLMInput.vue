<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { Dropdown, Tooltip } from 'floating-vue'
import 'floating-vue/dist/style.css'

interface FileAttachment {
  name: string
  url: string
  type: string
  description?: string
  cropData?: { x: number; y: number; w: number; h: number }
}

const props = defineProps({
  modelValue: { type: String, default: '' },
  isLoading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit', 'stop'])

const searchQuery = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isLongText = computed(() => searchQuery.value.length >= 50)
const hasAttachments = computed(() => selectedImages.value.length > 0 || selectedFiles.value.length > 0)

const currentModel = ref<'Flash' | 'Thinking'>('Flash')
const isEditorOpen = ref(false)
const editingImageIndex = ref<number | null>(null)
const currentDescription = ref('')
const canvasRef = ref<HTMLCanvasElement | null>(null)
const isDrawing = ref(false)
const startCoords = ref({ x: 0, y: 0 })
const rect = ref({ x: 0, y: 0, w: 0, h: 0 })

const fileInput = ref<HTMLInputElement | null>(null)

const selectedImages = ref<FileAttachment[]>([]) 
const selectedFiles = ref<FileAttachment[]>([])

const fileUploadActions = [
  { label: 'Upload files', icon: 'lucide:paperclip', action: 'upload' },
  { label: 'Add from Drive', icon: 'lucide:triangle', action: 'drive' },
  { label: 'More uploads', icon: 'lucide:more-horizontal', hasSubmenu: true }
]

const creativeActions = [
  { label: 'Create image', icon: 'lucide:sparkles', isNew: true },
  { label: 'Create video', icon: 'lucide:clapperboard', isNew: false },
  { label: 'Canvas', icon: 'lucide:square-plus', isNew: false },
  { label: 'Deep Research', icon: 'lucide:orbit', isNew: false },
  { label: 'Create music', icon: 'lucide:music', isNew: true },
  { label: 'Guided Learning', icon: 'lucide:book-open', isNew: false }
]

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const adjustHeight = () => {
  const textarea = textareaRef.value
  if (!textarea) return
  textarea.style.height = 'auto'
  textarea.style.height = `${Math.min(textarea.scrollHeight, 200)}px`
}

const handleInput = (e: Event) => {
  searchQuery.value = (e.target as HTMLTextAreaElement).value
  adjustHeight()
}

const openEditor = (idx: number) => {
  editingImageIndex.value = idx
  const img = selectedImages.value[idx]
  currentDescription.value = img.description || ''
  rect.value = img.cropData ? { ...img.cropData } : { x: 0, y: 0, w: 0, h: 0 }
  isEditorOpen.value = true
  
  nextTick(() => drawCanvas())
}

const drawCanvas = () => {
  if (!canvasRef.value || editingImageIndex.value === null) return
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const img = new Image()
  img.src = selectedImages.value[editingImageIndex.value].url
  img.onload = () => {
    canvas.width = img.width
    canvas.height = img.height
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(img, 0, 0)
    
    if (rect.value.w !== 0 || rect.value.h !== 0) {
      ctx.beginPath()
      ctx.strokeStyle = '#00e5ff'
      ctx.lineWidth = Math.max(img.width / 150, 4) 
      ctx.strokeRect(rect.value.x, rect.value.y, rect.value.w, rect.value.h)
      ctx.fillStyle = 'rgba(0, 229, 255, 0.25)'
      ctx.fillRect(rect.value.x, rect.value.y, rect.value.w, rect.value.h)
    }
  }
}

const handleMouseDown = (e: MouseEvent) => {
  if (!canvasRef.value) return
  isDrawing.value = true
  const rectCanvas = canvasRef.value.getBoundingClientRect()
  const scaleX = canvasRef.value.width / rectCanvas.width
  const scaleY = canvasRef.value.height / rectCanvas.height
  
  startCoords.value = {
    x: (e.clientX - rectCanvas.left) * scaleX,
    y: (e.clientY - rectCanvas.top) * scaleY
  }
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDrawing.value || !canvasRef.value) return
  const rectCanvas = canvasRef.value.getBoundingClientRect()
  const scaleX = canvasRef.value.width / rectCanvas.width
  const scaleY = canvasRef.value.height / rectCanvas.height
  const currentX = (e.clientX - rectCanvas.left) * scaleX
  const currentY = (e.clientY - rectCanvas.top) * scaleY
  
  rect.value = {
    x: Math.min(startCoords.value.x, currentX),
    y: Math.min(startCoords.value.y, currentY),
    w: Math.abs(currentX - startCoords.value.x),
    h: Math.abs(currentY - startCoords.value.y)
  }
  drawCanvas()
}

const saveEdit = () => {
  if (editingImageIndex.value !== null) {
    selectedImages.value[editingImageIndex.value].description = currentDescription.value
    selectedImages.value[editingImageIndex.value].cropData = { 
      x: Math.round(rect.value.x), 
      y: Math.round(rect.value.y), 
      w: Math.round(rect.value.w), 
      h: Math.round(rect.value.h) 
    }
  }
  isEditorOpen.value = false
}

const onFileSelected = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    Array.from(target.files).forEach(file => {
      const reader = new FileReader()
      reader.onload = (e) => {
        const result = e.target?.result as string
        if (file.type.startsWith('image/')) {
          selectedImages.value.push({ name: file.name, url: result, type: file.type })
        } else {
          selectedFiles.value.push({ name: file.name, url: result, type: file.type })
        }
      }
      reader.readAsDataURL(file)
    })
  }
}

const handleSubmit = () => {
  if (props.isLoading) {
    emit('stop')
    return
  }

  if (searchQuery.value.trim().length > 0 || selectedImages.value.length > 0 || selectedFiles.value.length > 0) {
    emit('submit', {
      text: searchQuery.value,
      model: currentModel.value,
      images: selectedImages.value.map(img => img.url),
      files: [...selectedFiles.value]
    })
    
    searchQuery.value = ''
    selectedImages.value = []
    selectedFiles.value = []
    if (textareaRef.value) textareaRef.value.style.height = 'auto'
  }
}
</script>

<template>
  <div class="w-full font-sans text-white">
    <input ref="fileInput" type="file" class="hidden" multiple @change="onFileSelected" />

    <div class="w-full max-w-[660px] mx-auto bg-[#1e1f20] border border-white/[0.02] rounded-[32px] p-4 shadow-2xl transition-all duration-200">
      
      <div v-if="hasAttachments" class="flex flex-col gap-4">
        
        <div class="flex flex-wrap gap-3 pl-1 items-center">
          
          <div 
            v-for="(img, idx) in selectedImages" 
            :key="'img-' + idx" 
            class="relative group"
          >
            <Tooltip placement="bottom-start" :distance="6" :triggers="['hover']" theme="custom-tooltip">
              <div class="w-[112px] h-[112px] rounded-[18px] overflow-hidden border border-white/[0.08] bg-black/20 cursor-pointer transition transform" @click="openEditor(idx)">
                <img :src="img.url" class="w-full h-full object-cover" />
              </div>
              <template #popper>{{ img.name }}</template>
            </Tooltip>
            <button 
              @click.stop="selectedImages.splice(idx, 1)" 
              class="absolute top-1.5 right-1.5 bg-white text-black hover:bg-gray-200 rounded-full w-5 h-5 flex items-center justify-center shadow-md z-20 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <Icon name="lucide:x" size="11" class="stroke-[3]" />
            </button>
          </div>

          <div 
            v-for="(file, idx) in selectedFiles" 
            :key="'file-' + idx" 
            class="relative group"
          >
            <Tooltip placement="bottom-start" :distance="6" :triggers="['hover']" theme="custom-tooltip">
              <div class="w-[112px] h-[112px] rounded-[18px] border border-white/[0.08] bg-[#141414]/60 flex flex-col justify-between p-3 cursor-default transition shadow-inner relative select-none">
                <div class="bg-[#ea4335] text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded w-fit tracking-wider">{{ $t('metaAi.pdf') }}</div>
                <div class="text-[12px] text-[#e3e3e3] truncate w-full font-medium">
                  {{ file.name }}
                </div>
              </div>
              <template #popper>{{ file.name }}</template>
            </Tooltip>
            <button 
              @click.stop="selectedFiles.splice(idx, 1)" 
              class="absolute top-1.5 right-1.5 bg-white text-black hover:bg-gray-200 rounded-full w-5 h-5 flex items-center justify-center shadow-md z-20 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <Icon name="lucide:x" size="11" class="stroke-[3]" />
            </button>
          </div>

        </div>

        <div class="w-full pl-1 pr-1">
          <textarea 
            ref="textareaRef"
            :value="searchQuery" 
            @input="handleInput"
            class="w-full bg-transparent outline-none text-[16px] text-[#e3e3e3] placeholder-[#8e918f] resize-none max-h-[160px] leading-relaxed custom-scroll block" 
            rows="1"
            :placeholder="$t('metaAi.askGemini')" 
            @keydown.enter.exact.prevent="handleSubmit"
          ></textarea>
        </div>

        <div class="flex flex-row items-center justify-between w-full pt-1">
          <div class="flex items-center">
            <Dropdown :distance="16" class="flex items-center" placement="top-start">
              <button class="p-2 hover:bg-white/5 rounded-full text-[#c4c7c5] hover:text-white transition flex items-center justify-center">
                <Icon name="lucide:plus" size="22" />
              </button>
              
              <template #popper="{ hide }">
                <div class="bg-[#1e1e1f] py-2 px-1 flex flex-col w-56 rounded-[20px] shadow-lg shadow-black/60 border border-white/[0.03]">
                  <div class="flex flex-col gap-0.5">
                    <button 
                      v-for="a in fileUploadActions" 
                      :key="a.label"
                      @click="a.action === 'upload' ? fileInput?.click() : null; hide()" 
                      class="flex items-center justify-between px-2.5 h-10 hover:bg-white/[0.05] rounded-xl text-[14px] font-normal text-[#e3e3e3] transition text-left"
                    >
                      <div class="flex items-center gap-3">
                        <Icon :name="a.icon" size="17" class="text-[#c4c7c5]" />
                        <span>{{ a.label }}</span>
                      </div>
                      <Icon v-if="a.hasSubmenu" name="lucide:chevron-right" size="14" class="text-[#c4c7c5]" />
                    </button>
                  </div>
                  <div class="border-t border-white/[0.06] my-1.5 mx-1"></div>
                  <div class="flex flex-col gap-0.5">
                    <button 
                      v-for="c in creativeActions" 
                      :key="c.label"
                      @click="hide()" 
                      class="flex items-center justify-between px-2.5 h-10 hover:bg-white/[0.05] rounded-xl text-[14px] font-normal text-[#e3e3e3] transition text-left"
                    >
                      <div class="flex items-center gap-3">
                        <Icon :name="c.icon" size="17" class="text-[#c4c7c5]" />
                        <span>{{ c.label }}</span>
                      </div>
                      <span v-if="c.isNew" class="bg-[#37393b] text-[#e3e3e3] text-[11px] font-normal px-2 py-0.5 rounded-full mr-0.5">{{ $t('metaAi.new') }}</span>
                    </button>
                  </div>
                </div>
              </template>
            </Dropdown>
          </div>

          <div class="flex items-center gap-2">
            <Dropdown :distance="12" placement="top-end">
              <div class="flex items-center text-[14px] font-normal text-[#c4c7c5] hover:text-white cursor-pointer px-3 py-1.5 rounded-full hover:bg-white/5 transition select-none gap-1 shrink-0">
                <span>{{ currentModel }}</span>
                <Icon name="lucide:chevron-down" size="16" class="opacity-70 mt-0.5" />
              </div>
              
              <template #popper="{ hide }">
                <div class="bg-[#1e1e1f] py-1.5 px-1 flex flex-col w-40 rounded-xl shadow-lg border border-white/[0.03]">
                  <button @click="currentModel = 'Flash'; hide()" class="flex items-center gap-2 px-3 h-9 hover:bg-white/[0.05] rounded-lg text-[14px] text-[#e3e3e3] transition text-left" :class="{ 'bg-white/[0.03] font-medium text-blue-400': currentModel === 'Flash' }">
                    <Icon name="lucide:zap" size="15" /><span>{{ $t('metaAi.flash') }}</span>
                  </button>
                  <button @click="currentModel = 'Thinking'; hide()" class="flex items-center gap-2 px-3 h-9 hover:bg-white/[0.05] rounded-lg text-[14px] text-[#e3e3e3] transition text-left" :class="{ 'bg-white/[0.03] font-medium text-purple-400': currentModel === 'Thinking' }">
                    <Icon name="lucide:brain" size="15" /><span>{{ $t('metaAi.thinking') }}</span>
                  </button>
                </div>
              </template>
            </Dropdown>

            <div class="flex items-center justify-center w-9 h-9">
              <button 
                @click="handleSubmit" 
                class="transition flex items-center justify-center w-full h-full shadow-md rounded-full"
                :class="isLoading ? 'bg-white text-black hover:bg-gray-200' : 'bg-[#a8c7fa] text-[#0f0f10] hover:bg-[#c2e7ff]'"
              >
                <svg v-if="isLoading" width="11" height="11" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="4" y="4" width="16" height="16" rx="1.5" />
                </svg>
                <Icon v-else name="lucide:arrow-up" size="18" class="stroke-[2.5]" />
              </button>
            </div>
          </div>
        </div>

      </div>

      <div v-else :class="isLongText ? 'flex flex-col gap-3' : 'flex flex-row items-center gap-2 w-full'">
        
        <div v-if="!isLongText" class="flex items-center shrink-0">
          <Dropdown :distance="16" class="flex items-center" placement="top-start">
            <button class="p-2 hover:bg-white/5 rounded-full text-[#c4c7c5] hover:text-white transition flex items-center justify-center">
              <Icon name="lucide:plus" size="22" />
            </button>
            <template #popper="{ hide }">
              <div class="bg-[#1e1e1f] py-2 px-1 flex flex-col w-56 rounded-[20px] shadow-lg shadow-black/60 border border-white/[0.03]">
                <div class="flex flex-col gap-0.5">
                  <button v-for="a in fileUploadActions" :key="a.label" @click="a.action === 'upload' ? fileInput?.click() : null; hide()" class="flex items-center justify-between px-2.5 h-10 hover:bg-white/[0.05] rounded-xl text-[14px] font-normal text-[#e3e3e3] transition text-left">
                    <div class="flex items-center gap-3"><Icon :name="a.icon" size="17" class="text-[#c4c7c5]" /><span>{{ a.label }}</span></div>
                    <Icon v-if="a.hasSubmenu" name="lucide:chevron-right" size="14" class="text-[#c4c7c5]" />
                  </button>
                </div>
                <div class="border-t border-white/[0.06] my-1.5 mx-1"></div>
                <div class="flex flex-col gap-0.5">
                  <button v-for="c in creativeActions" :key="c.label" @click="hide()" class="flex items-center justify-between px-2.5 h-10 hover:bg-white/[0.05] rounded-xl text-[14px] font-normal text-[#e3e3e3] transition text-left">
                    <div class="flex items-center gap-3"><Icon :name="c.icon" size="17" class="text-[#c4c7c5]" /><span>{{ c.label }}</span></div>
                    <span v-if="c.isNew" class="bg-[#37393b] text-[#e3e3e3] text-[11px] font-normal px-2 py-0.5 rounded-full mr-0.5">{{ $t('metaAi.new') }}</span>
                  </button>
                </div>
              </div>
            </template>
          </Dropdown>
        </div>

        <div class="flex-1 min-w-0 flex items-center w-full" :class="{ 'pl-1 pr-1': isLongText }">
          <textarea 
            ref="textareaRef"
            :value="searchQuery" 
            @input="handleInput"
            class="w-full bg-transparent outline-none text-[17px] text-[#e3e3e3] placeholder-[#b5b9bf] resize-none h-[24px] max-h-[160px] font-400 leading-relaxed custom-scroll block py-0.5" 
            rows="1"
            :placeholder="$t('metaAi.askGemini')" 
            @keydown.enter.exact.prevent="handleSubmit"
          ></textarea>
        </div>

        <div class="flex items-center justify-between shrink-0" :class="isLongText ? 'w-full pt-1' : 'gap-1'">
          
          <div v-if="isLongText" class="flex items-center">
            <Dropdown :distance="16" class="flex items-center" placement="top-start">
              <button class="p-2 hover:bg-white/5 rounded-full text-[#c4c7c5] hover:text-white transition flex items-center justify-center">
                <Icon name="lucide:plus" size="22" />
              </button>
              <template #popper="{ hide }">
                <div class="bg-[#1e1e1f] py-2 px-1 flex flex-col w-56 rounded-[20px] shadow-lg border border-white/[0.03]">
                  <div class="flex flex-col gap-0.5">
                    <button v-for="a in fileUploadActions" :key="a.label" @click="a.action === 'upload' ? fileInput?.click() : null; hide()" class="flex items-center justify-between px-2.5 h-10 hover:bg-white/[0.05] rounded-xl text-[14px] font-normal text-[#e3e3e3] transition text-left">
                      <div class="flex items-center gap-3"><Icon :name="a.icon" size="17" class="text-[#c4c7c5]" /><span>{{ a.label }}</span></div>
                      <Icon v-if="a.hasSubmenu" name="lucide:chevron-right" size="14" class="text-[#c4c7c5]" />
                    </button>
                  </div>
                </div>
              </template>
            </Dropdown>
          </div>

          <div class="flex items-center gap-2">
            <Dropdown :distance="12" placement="top-end">
              <div class="flex items-center text-[14px] font-normal text-[#c4c7c5] hover:text-white cursor-pointer px-2.5 py-1.5 rounded-full hover:bg-white/5 transition select-none gap-1 shrink-0">
                <span>{{ currentModel }}</span>
                <Icon name="lucide:chevron-down" size="14" class="opacity-70 mt-0.5" />
              </div>
              <template #popper="{ hide }">
                <div class="bg-[#1e1e1f] py-1.5 px-1 flex flex-col w-40 rounded-xl shadow-lg border border-white/[0.03]">
                  <button @click="currentModel = 'Flash'; hide()" class="flex items-center gap-2 px-3 h-9 hover:bg-white/[0.05] rounded-lg text-[14px] text-[#e3e3e3] transition text-left" :class="{ 'bg-white/[0.03] font-medium text-blue-400': currentModel === 'Flash' }">
                    <Icon name="lucide:zap" size="15" /><span>{{ $t('metaAi.flash') }}</span>
                  </button>
                  <button @click="currentModel = 'Thinking'; hide()" class="flex items-center gap-2 px-3 h-9 hover:bg-white/[0.05] rounded-lg text-[14px] text-[#e3e3e3] transition text-left" :class="{ 'bg-white/[0.03] font-medium text-purple-400': currentModel === 'Thinking' }">
                    <Icon name="lucide:brain" size="15" /><span>{{ $t('metaAi.thinking') }}</span>
                  </button>
                </div>
              </template>
            </Dropdown>

            <div class="flex items-center justify-center w-9 h-9">
              <button 
                v-if="isLoading"
                @click="handleSubmit" 
                class="transition flex items-center justify-center w-full h-full shadow-md rounded-full bg-white text-black hover:bg-gray-200"
              >
                <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="4" y="4" width="16" height="16" rx="1.5" />
                </svg>
              </button>
              
              <button 
                v-else-if="searchQuery.trim()" 
                @click="handleSubmit" 
                class="p-2 bg-[#a8c7fa] text-[#0f0f10] hover:bg-[#c2e7ff] rounded-full transition flex items-center justify-center w-full h-full shadow-md"
              >
                <Icon name="lucide:arrow-up" size="18" class="stroke-[2.5]" />
              </button>
              <button 
                v-else 
                class="p-2 text-[#c4c7c5] hover:bg-white/5 hover:text-white rounded-full transition flex items-center justify-center w-full h-full"
              >
                <Icon name="lucide:mic" size="20" class="stroke-[2]" />
              </button>
            </div>
          </div>

        </div>

      </div>

    </div>

    <div v-if="isEditorOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-sm p-4">
      <div class="bg-[#1e1f20] w-full max-w-5xl rounded-[32px] overflow-hidden border border-white/10 shadow-2xl flex flex-col max-h-[90vh]">
        <div class="flex justify-between items-center p-6 border-b border-white/5">
          <div>
            <h3 class="text-xl font-medium">{{ $t('metaAi.edytujObszarObrazu') }}</h3>
            <p class="text-xs text-gray-500 mt-1">{{ $t('metaAi.zaznaczFragmentKtoryChcesz') }}</p>
          </div>
          <button @click="isEditorOpen = false" class="hover:bg-white/10 p-2 rounded-full"><Icon name="lucide:x" size="24" /></button>
        </div>
        <div class="flex flex-col md:flex-row flex-1 overflow-hidden">
          <div class="flex-1 bg-black/40 flex items-center justify-center overflow-hidden p-6 relative">
            <canvas ref="canvasRef" @mousedown="handleMouseDown" @mousemove="handleMouseMove" @mouseup="isDrawing = false" @mouseleave="isDrawing = false" class="max-w-full max-h-full cursor-crosshair shadow-2xl"></canvas>
          </div>
          <div class="w-full md:w-80 p-6 flex flex-col border-l border-white/5 bg-[#141414]">
            <label class="text-sm font-bold text-gray-400 mb-2 uppercase tracking-wider">{{ $t('metaAi.twojeInstrukcje') }}</label>
            <textarea v-model="currentDescription" class="flex-1 bg-[#1e1f20] border border-white/10 rounded-2xl p-4 outline-none focus:border-blue-500 transition resize-none text-sm leading-relaxed" :placeholder="$t('metaAi.npDodajOkulary')"></textarea>
            <div class="mt-6 flex flex-col gap-3">
              <button @click="saveEdit" class="bg-white text-black font-bold py-4 rounded-2xl hover:bg-gray-200 transition shadow-lg">{{ $t('metaAi.zatwierdzZmiany') }}</button>
              <button @click="isEditorOpen = false" class="text-gray-500 py-2 hover:text-white transition text-sm">{{ $t('common.cancel') }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.v-popper__inner {
  background: transparent !important;
  color: #1e1f20 !important;
  font-size: 13px !important;
  font-weight: 400 !important;
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5) !important;
  font-family: inherit !important;
}
.v-popper__arrow-container {
  display: none !important;
}
</style>

<style scoped>
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 10px; }
canvas { image-rendering: pixelated; }

textarea {
  font-family: "Google Sans Flex", "Google Sans", "Helvetica Neue", sans-serif;
  scrollbar-width: none;
}
textarea::-webkit-scrollbar {
  display: none;
}
</style>
