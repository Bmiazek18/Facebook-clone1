<script setup lang="ts">
import { ref, reactive, onUnmounted, computed, onMounted, watch } from 'vue'
import * as fabric from 'fabric' // Fabric.js v6
import { useStoriesStore } from '@/composables/feed/useAppState'
import { useAuthStore } from '@/stores/auth'
import { useFriendSearch } from '@/composables/shared/useFriendSearch'
import type { StoryItem } from '@/types/Story'
import type { BackgroundSettings, PostData, ReelData } from '@/types/StoryElement'

// --- IMPORT KOMPONENTÓW UI ---
import MusicModal, { type MusicTrack } from './modals/MusicModal.vue'
import LinkStickerModal from '@/components/create/createStory/modals/LinkStickerModal.vue'
import StorySidebar from './StorySidebar/StorySidebar.vue'
import ImageToolbar from './ImageToolbar.vue'
import MusicToolbar from './MusicToolbar.vue'
import TextToolbar from './TextToolbar.vue'
import StoryMentionPopup from './StoryMentionPopup.vue'

import {
  useStoryFabricControls,
  type CustomFabricObject,
} from '@/composables/media/useStoryFabricControls'
import { useStoryFabricStickers } from '@/composables/media/useStoryFabricStickers'

// --- PROPS & EMITS ---
const props = defineProps<{
  initialImage?: string | null
  initialPost?: PostData | null
  initialReel?: ReelData | null
}>()

const emit = defineEmits<{ back: [] }>()

// --- REFY I STAN ---
const canvasRef = ref<HTMLCanvasElement | null>(null)
const workspaceRef = ref<HTMLElement | null>(null)

const canvasDimensions = reactive({ width: 0, height: 0 })
const storyDimensions = reactive({ width: 558, height: 1000 })
const storyOffset = reactive({ x: 0, y: 0 })

const background = reactive<BackgroundSettings>({
  type: 'gradient',
  value: ['#3b82f6', '#86efac'],
})

const isRendering = ref(false)
const renderProgress = ref(0)
const selectedElementId = ref<string | null>(null)
const selectedElementType = ref<string | null>(null)

let fCanvas: fabric.Canvas | null = null
let resizeObserver: ResizeObserver | null = null

// Audio Player
const audioPlayer = new Audio()
audioPlayer.loop = true
const isPlaying = ref(false)
const selectedMusicUrl = ref<string | null>(null)

// Modale
const isMusicModalOpen = ref(false)
const isLinkModalOpen = ref(false)

// Composable controls & stickers
const { applyCustomControls } = useStoryFabricControls(audioPlayer, selectedMusicUrl)
const {
  getHotspotFromObject,
  addMusicPoster: addMusicPosterHelper,
  addLinkSticker: addLinkStickerHelper,
  addPostSticker: addPostStickerHelper,
  addReelSticker: addReelStickerHelper,
} = useStoryFabricStickers(applyCustomControls)

// --- STAN TOOLTIPA MENTIONS (ZNAJOMYCH) ---
const showMentionMenu = ref(false)
const mentionQuery = ref('')
const mentionMenuPosition = reactive({ top: 0, left: 0 })
const activeMentionIndex = ref(0)
const { users: friendUsers, loadSuggestions, searchUsers } = useFriendSearch()

const filteredMentions = computed(() =>
  friendUsers.value.map((u) => ({
    id: String(u.id),
    name: u.name,
    subtitle: 'Znajomy',
    avatar: u.avatar || '',
  })),
)

watch(mentionQuery, (q) => {
  if (showMentionMenu.value) searchUsers(q)
})

const selectMention = (user: { id: string; name: string }) => {
  const activeObj = fCanvas?.getActiveObject() as fabric.IText & CustomFabricObject
  if (!activeObj || activeObj.elementType !== 'text') return

  const currentText = activeObj.text || ''
  const cursorPosition = activeObj.selectionStart || 0

  const textBeforeCursor = currentText.substring(0, cursorPosition)
  const textAfterCursor = currentText.substring(cursorPosition)

  const newTextBefore = textBeforeCursor.replace(/(?:^|\s)@(\w*)$/, (match) => {
    const prefix = match.match(/^\s/) ? match[0] : ''
    return `${prefix}@${user.name} `
  })

  activeObj.set('text', newTextBefore + textAfterCursor)
  activeObj.mentionedUserId = user.id

  const newCursorPos = newTextBefore.length
  activeObj.selectionStart = newCursorPos
  activeObj.selectionEnd = newCursorPos

  fCanvas?.renderAll()
  showMentionMenu.value = false

  activeObj.exitEditing()
  activeObj.enterEditing()
}

// --- LOGIKA TŁA (Gradient z obrazu) ---
const setBackgroundGradientFromImage = (imageUrl: string) => {
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.onload = () => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = 10
    canvas.height = 10
    ctx.drawImage(img, 0, 0, 10, 10)
    const { data } = ctx.getImageData(0, 0, 10, 10)

    let rTop = 0,
      gTop = 0,
      bTop = 0,
      rBottom = 0,
      gBottom = 0,
      bBottom = 0
    let topCount = 0,
      bottomCount = 0

    for (let y = 0; y < 10; y++) {
      for (let x = 0; x < 10; x++) {
        const idx = (y * 10 + x) * 4
        if (y < 5) {
          rTop += data[idx]
          gTop += data[idx + 1]
          bTop += data[idx + 2]
          topCount++
        } else {
          rBottom += data[idx]
          gBottom += data[idx + 1]
          bBottom += data[idx + 2]
          bottomCount++
        }
      }
    }

    const avgTop = [rTop / topCount, gTop / topCount, bTop / topCount].map(Math.round)
    const avgBottom = [rBottom / bottomCount, gBottom / bottomCount, bBottom / bottomCount].map(
      Math.round,
    )

    background.value = [
      `rgba(${avgTop[0]}, ${avgTop[1]}, ${avgTop[2]}, 0.92)`,
      `rgba(${avgBottom[0]}, ${avgBottom[1]}, ${avgBottom[2]}, 0.92)`,
    ]
    applyFabricBackground()
  }
  img.src = imageUrl
}

const applyFabricBackground = () => {
  if (!fCanvas) return

  const grad = new fabric.Gradient({
    type: 'linear',
    coords: {
      x1: 0,
      y1: storyOffset.y,
      x2: 0,
      y2: storyOffset.y + storyDimensions.height,
    },
    colorStops: [
      { offset: 0, color: background.value[0] },
      { offset: 1, color: background.value[1] },
    ],
  })

  fCanvas.backgroundColor = grad
  fCanvas.renderAll()
}

const updateDimensions = (workspaceWidth: number, workspaceHeight: number) => {
  if (!fCanvas) return

  canvasDimensions.width = workspaceWidth
  canvasDimensions.height = workspaceHeight

  try {
    fCanvas.setDimensions({ width: workspaceWidth, height: workspaceHeight })
  } catch (err) {
    return
  }

  const paddingX = 40
  const paddingY = 40
  const availableWidth = workspaceWidth - paddingX * 2
  const availableHeight = workspaceHeight - paddingY * 2

  let sHeight = availableHeight
  let sWidth = sHeight * (9 / 16)

  if (sWidth > availableWidth) {
    sWidth = availableWidth
    sHeight = sWidth * (16 / 9)
  }

  storyDimensions.width = sWidth
  storyDimensions.height = sHeight
  storyOffset.x = (workspaceWidth - sWidth) / 2
  storyOffset.y = (workspaceHeight - sHeight) / 2

  try {
    fCanvas.calcOffset()
    applyFabricBackground()
  } catch (err) {}
}

const getStoryCenter = () => ({
  x: storyOffset.x + storyDimensions.width / 2,
  y: storyOffset.y + storyDimensions.height / 2,
})

// --- LOGIKA BLOKOWANIA WYCHODZENIA POZA RAMKĘ ---
const constrainObjectToBounds = (e: any) => {
  const obj = e.target as CustomFabricObject
  if (!obj) return
  if (obj.elementType === 'image' && !obj.musicTitle) return

  obj.setCoords()
  const br = obj.getBoundingRect()

  const minX = storyOffset.x
  const minY = storyOffset.y
  const maxX = storyOffset.x + storyDimensions.width
  const maxY = storyOffset.y + storyDimensions.height

  let newLeft = obj.left || 0
  let newTop = obj.top || 0
  let isModified = false

  if (br.width <= storyDimensions.width) {
    if (br.left < minX) {
      newLeft += minX - br.left
      isModified = true
    } else if (br.left + br.width > maxX) {
      newLeft -= br.left + br.width - maxX
      isModified = true
    }
  }

  if (br.height <= storyDimensions.height) {
    if (br.top < minY) {
      newTop += minY - br.top
      isModified = true
    } else if (br.top + br.height > maxY) {
      newTop -= br.top + br.height - maxY
      isModified = true
    }
  }

  if (isModified) {
    obj.set({ left: newLeft, top: newTop })
    obj.setCoords()
  }
}

// --- STICKERS DELEGATES ---
const addMusicPoster = async (track: MusicTrack) => {
  if (!fCanvas) return
  await addMusicPosterHelper(
    fCanvas,
    track,
    storyDimensions,
    getStoryCenter(),
    audioPlayer,
    selectedMusicUrl,
    isPlaying,
  )
  isMusicModalOpen.value = false
}

const addLinkSticker = (data: { url: string; title: string; style: string }) => {
  if (!fCanvas) return
  addLinkStickerHelper(fCanvas, data, storyDimensions, getStoryCenter())
  isLinkModalOpen.value = false
}

const addPostSticker = async (post: PostData) => {
  if (!fCanvas) return
  await addPostStickerHelper(
    fCanvas,
    post,
    storyDimensions,
    getStoryCenter(),
    background,
    applyFabricBackground,
  )
}

const addReelSticker = async (reel: ReelData) => {
  if (!fCanvas) return
  await addReelStickerHelper(
    fCanvas,
    reel,
    storyDimensions,
    getStoryCenter(),
    background,
    applyFabricBackground,
  )
}

// --- DODAWANIE TEKSTU ---
const addTextElement = () => {
  if (!fCanvas) return
  const center = getStoryCenter()
  const id = `el_text_${Date.now()}`

  const text = new fabric.IText('Wpisz tekst', {
    id: id,
    elementType: 'text',
    left: center.x,
    top: center.y,
    originX: 'center',
    originY: 'center',
    fontFamily: 'sans-serif',
    fill: '#ffffff',
    fontSize: storyDimensions.width * 0.08,
    fontWeight: 'bold',
    textAlign: 'center',
    shadow: new fabric.Shadow({
      color: 'rgba(0,0,0,0.5)',
      blur: 4,
      offsetX: 2,
      offsetY: 2,
    }),
  } as CustomFabricObject)

  applyCustomControls(text)

  text.on('changed', () => {
    const currentText = text.text || ''
    const cursorPosition = text.selectionStart || 0
    const textBeforeCursor = currentText.substring(0, cursorPosition)

    const match = textBeforeCursor.match(/(?:^|\s)@(\w*)$/)
    if (match) {
      mentionQuery.value = match[1] || ''
      showMentionMenu.value = true
      activeMentionIndex.value = 0
      searchUsers(mentionQuery.value)

      const bound = text.getBoundingRect()
      mentionMenuPosition.top = bound.top + bound.height + 15
      mentionMenuPosition.left = bound.left + bound.width / 2 - 140
    } else {
      showMentionMenu.value = false
    }
  })

  text.on('editing:exited', () => {
    setTimeout(() => {
      showMentionMenu.value = false
    }, 150)
  })

  fCanvas.add(text)
  fCanvas.setActiveObject(text)
  text.enterEditing()
  text.selectAll()
}

// --- INICJALIZACJA FABRIC.JS V6 ---
onMounted(async () => {
  if (!canvasRef.value || !workspaceRef.value) return

  const rect = workspaceRef.value.getBoundingClientRect()
  fCanvas = new fabric.Canvas(canvasRef.value, {
    preserveObjectStacking: true,
    selection: false,
  })

  updateDimensions(rect.width, rect.height)

  resizeObserver = new ResizeObserver((entries) => {
    const entry = entries[0]
    if (entry) {
      updateDimensions(entry.contentRect.width, entry.contentRect.height)
    }
  })
  resizeObserver.observe(workspaceRef.value)

  fCanvas.on('selection:created', (e) => {
    const obj = e.selected?.[0] as CustomFabricObject
    if (obj) {
      selectedElementId.value = obj.id || null
      selectedElementType.value = obj.elementType || null
    }
  })

  fCanvas.on('selection:updated', (e) => {
    const obj = e.selected?.[0] as CustomFabricObject
    if (obj) {
      selectedElementId.value = obj.id || null
      selectedElementType.value = obj.elementType || null
    }
  })

  fCanvas.on('selection:cleared', () => {
    selectedElementId.value = null
    selectedElementType.value = null
    showMentionMenu.value = false
  })

  fCanvas.on('object:moving', constrainObjectToBounds)
  fCanvas.on('object:scaling', constrainObjectToBounds)

  if (props.initialImage) {
    try {
      const img = await fabric.FabricImage.fromURL(props.initialImage, {
        crossOrigin: 'anonymous',
      })
      const center = getStoryCenter()
      const scale = storyDimensions.width / (img.width || 1)

      img.set({
        id: `el_img_${Date.now()}`,
        elementType: 'image',
        originX: 'center',
        originY: 'center',
        left: center.x,
        top: center.y,
        scaleX: scale,
        scaleY: scale,
        hasControls: false,
        hasBorders: false,
      } as CustomFabricObject)

      fCanvas.add(img)
      fCanvas.setActiveObject(img)
      setBackgroundGradientFromImage(props.initialImage)
    } catch (err) {
      console.error('Błąd wczytywania obrazka:', err)
    }
  } else if (props.initialPost) {
    await addPostSticker(props.initialPost)
  } else if (props.initialReel) {
    await addReelSticker(props.initialReel)
  }

  await loadSuggestions()
})

onUnmounted(() => {
  audioPlayer.pause()
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (fCanvas) {
    try {
      fCanvas.dispose()
    } catch (err) {}
    fCanvas = null
  }
})

const isAnyElementActive = computed(() => selectedElementId.value !== null)

// --- AKCJE Z TOOLBARÓW ---
const handleTextColorChange = (color: string) => {
  if (!fCanvas) return
  const activeObj = fCanvas.getActiveObject() as fabric.IText
  if (activeObj && (activeObj as CustomFabricObject).elementType === 'text') {
    activeObj.set('fill', color)
    fCanvas.renderAll()
  }
}

const removeActiveElement = () => {
  if (!fCanvas) return
  const activeObj = fCanvas.getActiveObject()
  if (activeObj) {
    if ((activeObj as CustomFabricObject).musicTitle) {
      selectedMusicUrl.value = null
      audioPlayer.pause()
    }
    fCanvas.remove(activeObj)
    fCanvas.discardActiveObject()
    fCanvas.renderAll()
  }
}

const handleRotateMainImage = () => {
  if (!fCanvas) return
  const activeObj = fCanvas.getActiveObject()
  if (activeObj) {
    const currentAngle = activeObj.angle || 0
    activeObj.rotate(currentAngle + 90)
    fCanvas.renderAll()
  }
}

const handleScaleChange = (scaleVal: number) => {
  if (!fCanvas) return
  const activeObj = fCanvas.getActiveObject()
  if (activeObj) {
    activeObj.set({ scaleX: scaleVal, scaleY: scaleVal })
    fCanvas.renderAll()
  }
}

const removeMusicAndOpenModal = () => {
  removeActiveElement()
  isMusicModalOpen.value = true
}

// --- EKSPORT STORY ---
const storiesStore = useStoriesStore()
const authStore = useAuthStore()

const handleExportStory = async () => {
  if (!fCanvas) return

  try {
    isRendering.value = true
    renderProgress.value = 50

    fCanvas.discardActiveObject()
    fCanvas.renderAll()

    const exportMultiplier = 1080 / storyDimensions.width

    const imageUrl = fCanvas.toDataURL({
      format: 'jpeg',
      quality: 0.9,
      multiplier: exportMultiplier,
      left: storyOffset.x,
      top: storyOffset.y,
      width: storyDimensions.width,
      height: storyDimensions.height,
    })

    renderProgress.value = 100

    const authorId = String(authStore.currentUserId || '')
    if (!authorId) {
      alert('Musisz być zalogowany aby dodać story')
      isRendering.value = false
      return
    }

    let sharedPostInfoForExport: StoryItem['sharedPostInfo'] | null = null
    let sharedLinkInfoForExport: StoryItem['sharedLinkInfo'] | null = null
    const userTagsForExport: NonNullable<StoryItem['userTags']> = []

    fCanvas.getObjects().forEach((obj) => {
      const customObj = obj as CustomFabricObject
      const hotspot = getHotspotFromObject(customObj, storyDimensions, storyOffset)

      if (
        (customObj.elementType === 'post' || customObj.elementType === 'reel') &&
        customObj.postId
      ) {
        sharedPostInfoForExport = {
          postId: customObj.postId,
          ...hotspot,
        }
      }

      if (customObj.elementType === 'link' && customObj.linkUrl) {
        sharedLinkInfoForExport = {
          url: customObj.linkUrl,
          ...hotspot,
        }
      }

      if (customObj.elementType === 'text' && customObj.mentionedUserId) {
        userTagsForExport.push({
          userId: customObj.mentionedUserId,
          ...hotspot,
        })
      }
    })

    await storiesStore.addStory(authorId, {
      imageUrl: imageUrl,
      sharedPostInfo: sharedPostInfoForExport,
      sharedLinkInfo: sharedLinkInfoForExport,
      userTags: userTagsForExport,
    })

    window.location.href = '/'
  } catch (error) {
    console.error('Błąd podczas eksportowania story:', error)
    alert('Błąd podczas eksportowania story')
  } finally {
    isRendering.value = false
  }
}

const activeObjectComputed = computed(() => {
  if (!fCanvas) return null
  const obj = fCanvas.getActiveObject() as CustomFabricObject
  if (!obj) return null

  return {
    type: obj.elementType,
    scaleX: obj.scaleX,
    fill: obj.fill,
    musicTitle: obj.musicTitle,
    musicArtist: obj.musicArtist,
    musicStyle: obj.musicStyle,
    linkUrl: obj.linkUrl,
  }
})

const goBack = () => emit('back')
</script>

<template>
  <div class="flex h-screen w-full bg-theme-bg overflow-hidden select-none relative">
    <div
      v-if="isRendering"
      class="absolute inset-0 bg-black/90 flex items-center justify-center z-50"
    >
      <div class="bg-theme-bg-secondary rounded-lg p-8 max-w-md w-full mx-4 text-center">
        <h3 class="text-xl font-semibold text-theme-text mb-4">{{ $t('create.przetwarzanieStory') }}</h3>
        <div class="w-full bg-theme-border rounded-full h-2.5">
          <div
            class="bg-blue-600 h-2.5 rounded-full transition-all duration-300"
            :style="{ width: `${renderProgress}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Pasek Boczny -->
    <StorySidebar
      mode="image"
      :has-music="!!selectedMusicUrl"
      :is-music-modal-open="isMusicModalOpen"
      :is-image-selected="selectedElementType === 'image'"
      @add-text="addTextElement"
      @toggle-music="isMusicModalOpen = true"
      @add-link="isLinkModalOpen = true"
      @export-story="handleExportStory"
      @back="goBack"
    />

    <!-- GŁÓWNY OBSZAR -->
    <main
      class="flex-1 flex flex-col items-center justify-center p-6 bg-theme-bg overflow-hidden relative"
    >
      <div
        class="bg-theme-bg-secondary rounded-xl shadow-sm border border-theme-border p-4 w-full h-full max-w-[1000px] flex flex-col relative"
      >
        <div class="flex justify-between items-center mb-2 px-1">
          <span class="text-sm font-semibold text-theme-text-secondary">{{ $t('chat.podglad') }}</span>
        </div>

        <!-- MIEJSCE ROBOCZE -->
        <div
          ref="workspaceRef"
          class="flex-1 bg-[#18191A] rounded-lg relative overflow-hidden shadow-inner border border-theme-border"
        >
          <!-- 1. Canvas -->
          <canvas ref="canvasRef" class="absolute inset-0 z-0"></canvas>

          <!-- MENU WZMIANEK (TOOLTIP @) -->
          <StoryMentionPopup
            :show="showMentionMenu"
            :mentions="filteredMentions"
            :position="mentionMenuPosition"
            :canvas-width="canvasDimensions.width"
            :active-index="activeMentionIndex"
            @select="selectMention"
          />

          <!-- 2. Maska Wizualna -->
          <div class="pointer-events-none absolute inset-0 z-10 overflow-hidden">
            <div
              :class="[
                'absolute rounded-md border border-white transition-shadow duration-200',
                isAnyElementActive
                  ? 'shadow-[0_0_0_9999px_rgba(24,25,26,0.75)]'
                  : 'shadow-[0_0_0_9999px_rgba(24,25,26,1)]',
              ]"
              :style="{
                width: storyDimensions.width + 'px',
                height: storyDimensions.height + 'px',
                left: storyOffset.x + 'px',
                top: storyOffset.y + 'px',
              }"
            ></div>
          </div>

          <!-- 3. Toolbary -->
          <div
            class="absolute inset-0 pointer-events-none z-20 flex flex-col items-center justify-center"
          >
            <ImageToolbar
              v-if="selectedElementType === 'image' && !activeObjectComputed?.musicTitle"
              :scale="activeObjectComputed?.scaleX || 1"
              @update:scale="handleScaleChange"
              @rotate="handleRotateMainImage"
              class="pointer-events-auto absolute"
            />

            <MusicToolbar
              v-if="selectedElementType === 'image' && activeObjectComputed?.musicTitle"
              :current-style="activeObjectComputed?.musicStyle || 'large'"
              :track-title="activeObjectComputed?.musicTitle"
              :track-artist="activeObjectComputed?.musicArtist"
              :cover-url="''"
              @update:style="() => {}"
              @remove="removeMusicAndOpenModal"
              class="pointer-events-auto absolute"
            />

            <TextToolbar
              v-else-if="selectedElementType === 'text'"
              :current-color="activeObjectComputed?.fill?.toString() || '#ffffff'"
              @update:color="handleTextColorChange"
              class="pointer-events-auto absolute"
            />
          </div>
        </div>

        <!-- Modale -->
        <MusicModal
          :is-open="isMusicModalOpen"
          @close="isMusicModalOpen = false"
          @add-track="addMusicPoster"
        />
        <LinkStickerModal
          :is-open="isLinkModalOpen"
          @close="isLinkModalOpen = false"
          @add-link="addLinkSticker"
        />
      </div>
    </main>
  </div>
</template>

<style scoped>
.select-none {
  user-select: none;
}
</style>
