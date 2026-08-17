<script setup lang="ts">
import { ref, reactive, onUnmounted, computed, onMounted, watch } from 'vue'
import * as fabric from 'fabric' // Fabric.js v6
import { useStoriesStore } from '@/composables/feed/useAppState'
import { useAuthStore } from '@/stores/auth'
import { useFriendSearch } from '@/composables/shared/useFriendSearch'
import { getMediaUrl } from '@/utils/stories'
import type { StoryItem } from '@/types/Story'

// --- IMPORT KOMPONENTÓW UI ---
import MusicModal, { type MusicTrack } from './MusicModal.vue'
import LinkStickerModal from '@/components/LinkStickerModal.vue'
import StorySidebar from './StorySidebar/StorySidebar.vue'
import ImageToolbar from './ImageToolbar.vue'
import MusicToolbar from './MusicToolbar.vue'
import TextToolbar from './TextToolbar.vue'

import type { BackgroundSettings, PostData, ReelData } from '@/types/StoryElement'

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

interface CustomFabricObject extends fabric.FabricObject {
  id?: string
  elementType?: 'image' | 'text' | 'link' | 'post' | 'reel'
  musicTitle?: string
  musicArtist?: string
  musicStyle?: string
  linkUrl?: string
  postId?: string
  mentionedUserId?: string
}

// Audio Player
const audioPlayer = new Audio()
audioPlayer.loop = true
const isPlaying = ref(false)
const selectedMusicUrl = ref<string | null>(null)

// Modale
const isMusicModalOpen = ref(false)
const isLinkModalOpen = ref(false)
const isMentionModalOpen = ref(false) // Zostawione globalnie jeśli chcesz osobny modal, ale tu użyjemy tooltipa.

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

  // Zastąp @szukana_fraza wybranym tagiem (widoczne imię, ID w metadata)
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

// --- IKONY DLA KONTROLEK FABRIC ---
const deleteIconSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cline x1='18' y1='6' x2='6' y2='18'%3E%3C/line%3E%3Cline x1='6' y1='6' x2='18' y2='18'%3E%3C/line%3E%3C/svg%3E";
const deleteImg = new Image(); deleteImg.src = deleteIconSvg;

const rotateIconSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='23 4 23 10 17 10'%3E%3C/polyline%3E%3Cpath d='M20.49 15a9 9 0 1 1-2.12-9.36L23 10'%3E%3C/path%3E%3C/svg%3E";
const rotateImg = new Image(); rotateImg.src = rotateIconSvg;

// --- FUNKCJA NAKŁADAJĄCA NOWY WYGLĄD NA OBIEKT ---
const applyCustomControls = (obj: CustomFabricObject) => {
  obj.set({
    transparentCorners: false,
    cornerColor: '#ffffff',
    cornerStrokeColor: '#9ca3af',
    borderColor: '#ffffff',
    cornerSize: 12,
    padding: 6,
    cornerStyle: 'circle',
    borderDashArray: null,
  })

  const customControls = { ...obj.controls }

  delete customControls.ml
  delete customControls.mr
  delete customControls.mt
  delete customControls.mb

  customControls.tl = new fabric.Control({
    x: -0.5, y: -0.5,
    offsetX: -16, offsetY: -16,
    cursorStyle: 'pointer',
    mouseUpHandler: (eventData, transform) => {
      const target = transform.target as CustomFabricObject
      const canvas = target.canvas
      if (target.musicTitle && audioPlayer.src) {
        audioPlayer.pause()
        selectedMusicUrl.value = null
      }
      canvas?.remove(target)
      canvas?.discardActiveObject()
      canvas?.requestRenderAll()
      return true
    },
    render: (ctx, left, top, styleOverride, fabricObject) => {
      if ((fabricObject as CustomFabricObject).elementType === 'image' && !(fabricObject as CustomFabricObject).musicTitle) return
      const size = 28
      ctx.save()
      ctx.translate(left, top)
      ctx.beginPath()
      ctx.arc(0, 0, size / 2, 0, Math.PI * 2)
      ctx.fillStyle = 'white'
      ctx.shadowColor = 'rgba(0,0,0,0.15)'
      ctx.shadowBlur = 6
      ctx.fill()
      ctx.lineWidth = 1
      ctx.strokeStyle = '#e5e7eb'
      ctx.stroke()
      ctx.shadowColor = 'transparent'
      ctx.drawImage(deleteImg, -8, -8, 16, 16)
      ctx.restore()
    },
    cornerSize: 28
  })

  customControls.mtr = new fabric.Control({
    x: 0.5, y: -0.5,
    offsetX: 28, offsetY: -28,
    actionHandler: fabric.controlsUtils.rotationWithSnapping,
    cursorStyle: 'crosshair',
    actionName: 'rotate',
    render: (ctx, left, top, styleOverride, fabricObject) => {
      if ((fabricObject as CustomFabricObject).elementType === 'image' && !(fabricObject as CustomFabricObject).musicTitle) return
      const size = 24
      ctx.save()
      ctx.translate(left, top)
      ctx.shadowColor = 'rgba(0,0,0,0.4)'
      ctx.shadowBlur = 4
      ctx.drawImage(rotateImg, -size / 2, -size / 2, size, size)
      ctx.restore()
    },
    cornerSize: 32,
    withConnection: false
  })

  obj.controls = customControls
}

const toggleBackgroundMusic = () => {
  if (audioPlayer.paused && audioPlayer.src) {
    audioPlayer.play()
    isPlaying.value = true
  } else {
    audioPlayer.pause()
    isPlaying.value = false
  }
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

    let rTop = 0, gTop = 0, bTop = 0, rBottom = 0, gBottom = 0, bBottom = 0
    let topCount = 0, bottomCount = 0

    for (let y = 0; y < 10; y++) {
      for (let x = 0; x < 10; x++) {
        const idx = (y * 10 + x) * 4
        if (y < 5) {
          rTop += data[idx]; gTop += data[idx + 1]; bTop += data[idx + 2]; topCount++
        } else {
          rBottom += data[idx]; gBottom += data[idx + 1]; bBottom += data[idx + 2]; bottomCount++
        }
      }
    }

    const avgTop = [rTop / topCount, gTop / topCount, bTop / topCount].map(Math.round)
    const avgBottom = [rBottom / bottomCount, gBottom / bottomCount, bBottom / bottomCount].map(Math.round)

    background.value = [
      `rgba(${avgTop[0]}, ${avgTop[1]}, ${avgTop[2]}, 0.92)`,
      `rgba(${avgBottom[0]}, ${avgBottom[1]}, ${avgBottom[2]}, 0.92)`
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
      x1: 0, y1: storyOffset.y,
      x2: 0, y2: storyOffset.y + storyDimensions.height
    },
    colorStops: [
      { offset: 0, color: background.value[0] },
      { offset: 1, color: background.value[1] }
    ]
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
  } catch(err) {}
}

const getStoryCenter = () => ({
  x: storyOffset.x + storyDimensions.width / 2,
  y: storyOffset.y + storyDimensions.height / 2
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
      newLeft += (minX - br.left)
      isModified = true
    } else if (br.left + br.width > maxX) {
      newLeft -= (br.left + br.width - maxX)
      isModified = true
    }
  }

  if (br.height <= storyDimensions.height) {
    if (br.top < minY) {
      newTop += (minY - br.top)
      isModified = true
    } else if (br.top + br.height > maxY) {
      newTop -= (br.top + br.height - maxY)
      isModified = true
    }
  }

  if (isModified) {
    obj.set({ left: newLeft, top: newTop })
    obj.setCoords()
  }
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
    showMentionMenu.value = false // Chowaj dymek odznaczając
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
const isAnyElementActive = computed(() => {
  return selectedElementId.value !== null
})

// --- DODAWANIE ELEMENTÓW ---
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

  // LOGIKA MENTIONS - NASŁUCHIWANIE NA @
  text.on('changed', () => {
    const currentText = text.text || ''
    const cursorPosition = text.selectionStart || 0
    const textBeforeCursor = currentText.substring(0, cursorPosition)

    // Detekcja: Znak @ zaraz po spacji lub na samym początku wiersza
    const match = textBeforeCursor.match(/(?:^|\s)@(\w*)$/)

    if (match) {
      mentionQuery.value = match[1] || ''
      showMentionMenu.value = true
      activeMentionIndex.value = 0
      searchUsers(mentionQuery.value)

      const bound = text.getBoundingRect()
      mentionMenuPosition.top = bound.top + bound.height + 15
      mentionMenuPosition.left = bound.left + (bound.width / 2) - 140
    } else {
      showMentionMenu.value = false
    }
  })

  text.on('editing:exited', () => {
    setTimeout(() => { showMentionMenu.value = false }, 150)
  })

  fCanvas.add(text)
  fCanvas.setActiveObject(text)
  text.enterEditing()
  text.selectAll()
}

const addMusicPoster = async (track: MusicTrack) => {
  if (!fCanvas) return
  const id = `el_music_${Date.now()}`
  const center = getStoryCenter()

  try {
    const img = await fabric.FabricImage.fromURL(track.coverUrlLarge || '', { crossOrigin: 'anonymous' })
    const targetWidth = storyDimensions.width * 0.4
    img.scaleToWidth(targetWidth)
    img.set({ originX: 'center', originY: 'center', top: -30 })

    const bg = new fabric.Rect({
      width: targetWidth + 20,
      height: targetWidth + 100,
      fill: 'white',
      rx: 10, ry: 10,
      originX: 'center', originY: 'center',
      shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.2)', blur: 10 }),
    })

    const titleText = new fabric.Text(track.title, {
      top: (targetWidth / 2) - 10,
      originX: 'center', originY: 'center',
      fontSize: storyDimensions.width * 0.035,
      fontWeight: 'bold', fill: '#000',
    })

    const artistText = new fabric.Text(track.artist, {
      top: (targetWidth / 2) + 15,
      originX: 'center', originY: 'center',
      fontSize: storyDimensions.width * 0.025,
      fill: '#666',
    })

    const group = new fabric.Group([bg, img, titleText, artistText], {
      id: id,
      elementType: 'image',
      musicTitle: track.title,
      musicArtist: track.artist,
      musicStyle: 'large',
      left: center.x,
      top: center.y,
      originX: 'center', originY: 'center',
    } as CustomFabricObject)

    // Aplikujemy customowe style do muzyki
    applyCustomControls(group)

    fCanvas.add(group)
    fCanvas.setActiveObject(group)

    selectedMusicUrl.value = track.previewUrl
    if (track.previewUrl) {
      audioPlayer.src = track.previewUrl
      audioPlayer.play().then(() => (isPlaying.value = true))
    }
  } catch (err) {
    console.error('Błąd ładowania plakatów muzycznych:', err)
  }
  isMusicModalOpen.value = false
}

const addLinkSticker = (data: { url: string; title: string; style: string }) => {
  if (!fCanvas) return
  const id = `el_link_${Date.now()}`
  const center = getStoryCenter()

  const textVal = data.title || data.url
  const text = new fabric.Text('🔗 ' + textVal, {
    fontSize: storyDimensions.width * 0.04,
    fill: data.style === 'button' ? 'white' : '#2563eb',
    fontWeight: 'bold',
    originX: 'center', originY: 'center',
  })

  const bg = new fabric.Rect({
    width: (text.width || 100) + 40,
    height: storyDimensions.height * 0.06,
    fill: data.style === 'button' ? '#3b82f6' : 'white',
    rx: 25, ry: 25,
    originX: 'center', originY: 'center',
    shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.2)', blur: 5 }),
  })

  const group = new fabric.Group([bg, text], {
    id: id,
    elementType: 'link',
    linkUrl: data.url,
    left: center.x,
    top: center.y + 100,
    originX: 'center', originY: 'center',
  } as CustomFabricObject)

  // Aplikujemy customowe style do linku
  applyCustomControls(group)

  fCanvas.add(group)
  fCanvas.setActiveObject(group)
  isLinkModalOpen.value = false
}

const getHotspotFromObject = (obj: CustomFabricObject) => {
  const br = obj.getBoundingRect()
  return {
    x: ((br.left - storyOffset.x) / storyDimensions.width) * 100,
    y: ((br.top - storyOffset.y) / storyDimensions.height) * 100,
    width: (br.width / storyDimensions.width) * 100,
    height: (br.height / storyDimensions.height) * 100,
  }
}

const addPostSticker = async (post: PostData) => {
  if (!fCanvas) return
  const center = getStoryCenter()
  const cardWidth = storyDimensions.width * 0.78
  const objects: fabric.FabricObject[] = []

  const headerHeight = 52
  const content = (post.content || '').slice(0, 120)
  const hasMedia = !!(post.media?.[0]?.src)

  let mediaImg: fabric.FabricImage | null = null
  let mediaHeight = 0
  if (hasMedia) {
    try {
      mediaImg = await fabric.FabricImage.fromURL(getMediaUrl(post.media[0]!.src), {
        crossOrigin: 'anonymous',
      })
      const maxH = storyDimensions.height * 0.35
      mediaImg.scaleToWidth(cardWidth)
      if ((mediaImg.getScaledHeight() || 0) > maxH) {
        mediaImg.scaleToHeight(maxH)
      }
      mediaHeight = mediaImg.getScaledHeight() || 0
    } catch (err) {
      console.warn('Nie udało się wczytać mediów posta do story:', err)
      mediaImg = null
      mediaHeight = 0
    }
  }

  const contentText = new fabric.Textbox(content || 'Udostępniony post', {
    width: cardWidth - 28,
    fontSize: Math.max(12, storyDimensions.width * 0.028),
    fill: '#1c1e21',
    fontFamily: 'Arial, sans-serif',
    originX: 'left',
    originY: 'top',
  })
  const textHeight = contentText.calcTextHeight() || 20
  const footerHeight = 34
  const cardHeight = headerHeight + 12 + textHeight + 12 + mediaHeight + footerHeight

  const bg = new fabric.Rect({
    width: cardWidth,
    height: cardHeight,
    fill: '#ffffff',
    rx: 14,
    ry: 14,
    originX: 'left',
    originY: 'top',
    shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.25)', blur: 12, offsetY: 4 }),
  })
  objects.push(bg)

  try {
    const avatar = await fabric.FabricImage.fromURL(post.author.avatar || '/default-avatar.png', {
      crossOrigin: 'anonymous',
    })
    avatar.scaleToWidth(32)
    avatar.set({ left: 14, top: 12, originX: 'left', originY: 'top' })
    // Circular clip
    const clip = new fabric.Circle({
      radius: 16,
      originX: 'center',
      originY: 'center',
      left: 14 + 16,
      top: 12 + 16,
    })
    avatar.set({ clipPath: clip as any, left: 14, top: 12 })
    objects.push(avatar)
  } catch {
    const avatarFallback = new fabric.Circle({
      radius: 16,
      fill: '#e4e6eb',
      left: 14,
      top: 12,
      originX: 'left',
      originY: 'top',
    })
    objects.push(avatarFallback)
  }

  const authorName = new fabric.Text(post.author.name || 'Użytkownik', {
    left: 56,
    top: 14,
    fontSize: Math.max(12, storyDimensions.width * 0.03),
    fontWeight: 'bold',
    fill: '#050505',
    fontFamily: 'Arial, sans-serif',
  })
  objects.push(authorName)

  const publicLabel = new fabric.Text('Publiczny', {
    left: 56,
    top: 34,
    fontSize: Math.max(10, storyDimensions.width * 0.022),
    fill: '#65676b',
    fontFamily: 'Arial, sans-serif',
  })
  objects.push(publicLabel)

  contentText.set({ left: 14, top: headerHeight })
  objects.push(contentText)

  if (mediaImg) {
    mediaImg.set({
      left: (cardWidth - mediaImg.getScaledWidth()) / 2,
      top: headerHeight + 12 + textHeight + 8,
      originX: 'left',
      originY: 'top',
    })
    objects.push(mediaImg)
  }

  const footerBg = new fabric.Rect({
    width: cardWidth,
    height: footerHeight,
    fill: '#f0f2f5',
    left: 0,
    top: cardHeight - footerHeight,
    originX: 'left',
    originY: 'top',
  })
  objects.push(footerBg)

  const footerText = new fabric.Text('Dotknij, aby wyświetlić post', {
    left: cardWidth / 2,
    top: cardHeight - footerHeight / 2,
    originX: 'center',
    originY: 'center',
    fontSize: Math.max(11, storyDimensions.width * 0.024),
    fill: '#0866ff',
    fontWeight: 'bold',
    fontFamily: 'Arial, sans-serif',
  })
  objects.push(footerText)

  const group = new fabric.Group(objects, {
    id: `el_post_${Date.now()}`,
    elementType: 'post',
    postId: String(post.id),
    left: center.x,
    top: center.y,
    originX: 'center',
    originY: 'center',
  } as CustomFabricObject)

  applyCustomControls(group)
  fCanvas.add(group)
  fCanvas.setActiveObject(group)
  fCanvas.renderAll()

  // Soft gradient background for shared post stories
  background.value = ['rgba(24, 25, 26, 0.95)', 'rgba(50, 52, 54, 0.95)']
  applyFabricBackground()
}

const addReelSticker = async (reel: ReelData) => {
  if (!fCanvas) return
  const center = getStoryCenter()
  const cardWidth = storyDimensions.width * 0.55

  const objects: fabric.FabricObject[] = []
  let mediaHeight = cardWidth * 1.4

  try {
    const poster = await fabric.FabricImage.fromURL(getMediaUrl(reel.poster || reel.videoSrc), {
      crossOrigin: 'anonymous',
    })
    poster.scaleToWidth(cardWidth)
    mediaHeight = poster.getScaledHeight() || mediaHeight
    poster.set({ left: 0, top: 0, originX: 'left', originY: 'top' })
    objects.push(poster)
  } catch {
    objects.push(
      new fabric.Rect({
        width: cardWidth,
        height: mediaHeight,
        fill: '#111',
        originX: 'left',
        originY: 'top',
      }),
    )
  }

  const label = new fabric.Text('Reel · Dotknij, aby otworzyć', {
    left: cardWidth / 2,
    top: mediaHeight + 18,
    originX: 'center',
    originY: 'center',
    fill: '#fff',
    fontSize: Math.max(12, storyDimensions.width * 0.028),
    fontWeight: 'bold',
  })

  const bg = new fabric.Rect({
    width: cardWidth,
    height: mediaHeight + 36,
    fill: 'rgba(0,0,0,0.35)',
    rx: 12,
    ry: 12,
    originX: 'left',
    originY: 'top',
  })

  const group = new fabric.Group([bg, ...objects, label], {
    id: `el_reel_${Date.now()}`,
    elementType: 'reel',
    postId: String(reel.id),
    left: center.x,
    top: center.y,
    originX: 'center',
    originY: 'center',
  } as CustomFabricObject)

  applyCustomControls(group)
  fCanvas.add(group)
  fCanvas.setActiveObject(group)
  background.value = ['rgba(0,0,0,0.95)', 'rgba(30,30,30,0.95)']
  applyFabricBackground()
}

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
      const hotspot = getHotspotFromObject(customObj)

      if ((customObj.elementType === 'post' || customObj.elementType === 'reel') && customObj.postId) {
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
    linkUrl: obj.linkUrl
  }
})

const goBack = () => emit('back')
</script>

<template>
  <div class="flex h-screen w-full bg-theme-bg overflow-hidden select-none relative">

    <div v-if="isRendering" class="absolute inset-0 bg-black/90 flex items-center justify-center z-50">
      <div class="bg-theme-bg-secondary rounded-lg p-8 max-w-md w-full mx-4 text-center">
        <h3 class="text-xl font-semibold text-theme-text mb-4">Przetwarzanie Story...</h3>
        <div class="w-full bg-theme-border rounded-full h-2.5">
          <div class="bg-blue-600 h-2.5 rounded-full transition-all duration-300" :style="{ width: `${renderProgress}%` }"></div>
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
    <main class="flex-1 flex flex-col items-center justify-center p-6 bg-theme-bg overflow-hidden relative">

      <div class="bg-theme-bg-secondary rounded-xl shadow-sm border border-theme-border p-4 w-full h-full max-w-[1000px] flex flex-col relative">

        <div class="flex justify-between items-center mb-2 px-1">
          <span class="text-sm font-semibold text-theme-text-secondary">Podgląd</span>
        </div>

        <!-- MIEJSCE ROBOCZE -->
        <div
          ref="workspaceRef"
          class="flex-1 bg-[#18191A] rounded-lg relative overflow-hidden shadow-inner border border-theme-border"
        >
          <!-- 1. Canvas -->
          <canvas ref="canvasRef" class="absolute inset-0 z-0"></canvas>

          <!-- MENU WZMIANEK (TOOLTIP @) NAŁOŻONY ABSOLUTNIE NA WARSTWĘ CANVASA -->
          <div
            v-if="showMentionMenu && filteredMentions.length > 0"
            class="absolute z-[100] bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] flex flex-col  gap-1 w-[280px] pointer-events-auto"
            :style="{
              top: mentionMenuPosition.top + 'px',
              left: Math.max(10, Math.min(mentionMenuPosition.left, canvasDimensions.width - 290)) + 'px'
            }"
          >
            <div
              v-for="(user, idx) in filteredMentions"
              :key="user.id"
              @mousedown.prevent="selectMention(user)"
              class="flex items-center gap-3 p-1.5 rounded-xl cursor-pointer border-[2px]"
              :class="idx === activeMentionIndex ? 'border-blue-600' : 'border-transparent hover:bg-[#F0F2F5]'"
            >
              <!-- Avatar uzytkownika -->
              <div class="w-11 h-11 rounded-full bg-[#E4E6EB] flex items-center justify-center overflow-hidden shrink-0 border border-black/5">
                <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
                <svg v-else class="w-8 h-8 text-[#B0B3B8] mt-2" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </div>
              <!-- Dane uzytkownika -->
              <div class="flex flex-col justify-center min-w-0">
                <span class="font-bold text-[15px] text-[#050505] leading-tight truncate">{{ user.name }}</span>
                <span class="text-[13px] text-[#65676B] leading-tight truncate mt-0.5">{{ user.subtitle }}</span>
              </div>
            </div>
          </div>

          <!-- 2. Maska Wizualna -->
          <div class="pointer-events-none absolute inset-0 z-10 overflow-hidden">
            <div
              :class="[
                'absolute rounded-md border border-white  transition-shadow duration-200',
                isAnyElementActive
                  ? 'shadow-[0_0_0_9999px_rgba(24,25,26,0.75)]'
                  : 'shadow-[0_0_0_9999px_rgba(24,25,26,1)]'
              ]"
              :style="{
                width: storyDimensions.width + 'px',
                height: storyDimensions.height + 'px',
                left: storyOffset.x + 'px',
                top: storyOffset.y + 'px'
              }"
            ></div>
          </div>

          <!-- 3. Toolbary -->
          <div class="absolute inset-0 pointer-events-none z-20 flex flex-col items-center justify-center">
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
              :currentColor="activeObjectComputed?.fill?.toString() || '#ffffff'"
              @update:color="handleTextColorChange"
              class="pointer-events-auto absolute"
            />
          </div>

        </div>

        <!-- Modale -->
        <MusicModal :is-open="isMusicModalOpen" @close="isMusicModalOpen = false" @add-track="addMusicPoster" />
        <LinkStickerModal :is-open="isLinkModalOpen" @close="isLinkModalOpen = false" @add-link="addLinkSticker" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.select-none {
  user-select: none;
}
</style>
