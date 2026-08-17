<template>
  <!-- WIDOK 1: Wybór zdjęcia (wyświetlany, gdy selectedImage jest null) -->
  <div v-if="!selectedImage" class="w-full max-w-[700px] h-[85vh] max-h-[630px] mx-auto bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col">
    <div class="p-4 space-y-6 overflow-y-auto flex-1">

      <!-- UKRYTY NATYWNY INPUT DLA PLIKÓW -->
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileUpload"
      />

      <div class="flex gap-2">
        <button
          @click="triggerFileInput"
          class="flex-1 flex items-center justify-center gap-2 bg-[#edf2fa] hover:bg-[#e0e8f6] text-[#0b57d0] font-medium py-2.5 px-4 rounded-full transition text-sm cursor-pointer"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-4 h-4">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          Dodaj zdjęcie
        </button>
        <button class="flex items-center justify-center bg-[#edf2fa] hover:bg-[#e0e8f6] text-gray-700 p-2.5 rounded-lg transition cursor-pointer">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
            <path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" />
          </svg>
        </button>
      </div>

      <!-- Proponowane zdjęcia -->
      <div>
        <h3 class="text-base font-semibold text-gray-900 mb-3">Proponowane zdjęcia</h3>
        <div class="flex flex-row gap-3">
          <div
            v-for="(img, index) in proponowane"
            :key="'prop-' + index"
            @click="selectImage(img)"
            class="aspect-square w-[105px] overflow-hidden cursor-pointer border border-gray-100 hover:brightness-95 transition first:rounded-l-xl last:rounded-r-xl"
          >
            <img :src="img" alt="Proponowane" class="w-full h-full object-cover" />
          </div>
        </div>
      </div>

      <!-- Przesłane -->
      <div>
        <h3 class="text-base font-semibold text-gray-900 mb-3">Przesłane</h3>
        <div class="flex flex-row gap-3">
          <div
            v-for="(img, index) in przeslane"
            :key="'pres-' + index"
            @click="selectImage(img)"
            class="aspect-square w-[105px] overflow-hidden cursor-pointer border border-gray-100 hover:brightness-95 transition first:rounded-l-xl last:rounded-r-xl"
          >
            <img :src="img" alt="Przesłane" class="w-full h-full object-cover" />
          </div>
        </div>
      </div>

      <!-- Zdjęcia profilowe -->
      <div>
        <h3 class="text-base font-semibold text-gray-900 mb-3">Zdjęcia profilowe</h3>
        <div class="flex flex-row gap-3">
          <div
            v-for="(img, index) in profilowe"
            :key="'prof-' + index"
            @click="selectImage(img)"
            class="aspect-square w-[105px] overflow-hidden cursor-pointer border border-gray-100 hover:brightness-95 transition first:rounded-l-xl last:rounded-r-xl"
          >
            <img :src="img" alt="Profilowe" class="w-full h-full object-cover" />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- WIDOK 2: Edytor i kadrowanie (wyświetlany po wybraniu zdjęcia) -->
  <div
    v-else
    class="w-full max-w-[700px] h-[85vh] max-h-[630px] mx-auto bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col select-none"
  >
    <div class="p-6 overflow-y-auto flex-1">
      <div class="mb-6">
        <textarea
          v-model="description"
          placeholder="Opis"
          class="w-full h-20 p-3 border border-gray-200 rounded-xl outline-none resize-none text-gray-800 placeholder-gray-400 text-base focus:border-blue-500 transition-colors"
        ></textarea>
      </div>

      <div class="flex flex-col items-center">
        <!-- Obszar kadrowania -->
        <div
          ref="containerRef"
          class="relative w-full h-[260px] bg-white rounded-lg overflow-hidden mb-6 cursor-grab active:cursor-grabbing select-none ring-1 ring-gray-100 touch-none"
          @mousedown="startDrag"
          @touchstart.prevent="startDrag"
        >
          <div class="absolute inset-0 flex items-center justify-center pointer-events-none overflow-hidden">
            <img
              ref="imageRef"
              :src="selectedImage"
              alt="Podgląd"
              class="max-w-none pointer-events-auto select-none"
              @load="onImageLoad"
              @dragstart.prevent
              :style="{
                width: imageAspectRatio > containerRatio ? 'auto' : '100%',
                height: imageAspectRatio > containerRatio ? '100%' : 'auto',
                transform: `translate(${position.x}px, ${position.y}px) scale(${zoom / 100})`,
                transition: isDragging ? 'none' : 'transform 0.05s ease-out',
              }"
            />
          </div>

          <!-- Maska (Podgląd Kółka/Awataru) -->
          <div
            ref="cropRef"
            class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 pointer-events-none z-10"
            :class="isCover ? 'w-[90%] h-[140px]' : 'w-[200px] h-[200px]'"
          >
            <!-- 1. WIDOK DOMYŚLNY (Widoczne całe zdjęcie + Ciemna maska + Przezroczyste kółko) -->
            <div
              v-if="!isCropMode && !isCover"
              class="absolute inset-0 rounded-full"
              style="box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.6);"
            ></div>

            <!-- 2. WIDOK PRZYCIĘCIA (Poza kwadratem wszystko białe + Rogi wewnątrz kwadratu ciemne) -->
            <div
              v-if="isCropMode"
              class="absolute inset-0"
              style="
                outline: 9999px solid #ffffff;
                background: radial-gradient(circle closest-side, transparent 99%, rgba(0, 0, 0, 0.6) 100%);
              "
            ></div>
          </div>
        </div>

        <!-- Suwak Zoomu (usunięto @input, załatwia to watch) -->
        <div class="flex items-center w-full max-w-[380px] gap-6 mb-6 px-2">
          <button @click="changeZoom(-10)" class="text-gray-400 hover:text-gray-800 transition-colors cursor-pointer">
            <minus-icon :size="20" />
          </button>
          <div class="relative w-full h-6 flex items-center">
            <input
              type="range"
              :min="minZoom"
              max="250"
              step="1"
              v-model.number="zoom"
              class="custom-slider w-full h-1 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
          </div>
          <button @click="changeZoom(10)" class="text-gray-400 hover:text-gray-800 transition-colors cursor-pointer">
            <plus-icon :size="20" />
          </button>
        </div>

        <!-- Przyciski Funkcyjne -->
        <div class="flex justify-center gap-2 w-full mb-6">
          <button
            @click="toggleCropMode"
            :class="isCropMode ? 'bg-[#ebf5ff] text-[#0b57d0]' : 'bg-[#e4e6eb] hover:bg-[#d8dadf] text-gray-900'"
            class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-colors cursor-pointer"
          >
            <crop-icon :size="16" class="mt-[1px]" />
            Przytnij zdjęcie
          </button>

          <button class="flex items-center gap-2 px-4 py-2 bg-[#e4e6eb] hover:bg-[#d8dadf] text-gray-900 rounded-lg text-sm font-semibold transition-colors cursor-pointer">
            <clock-outline-icon :size="16" class="mt-[1px]" />
            Ustaw jako tymczasowe
          </button>
        </div>
      </div>

      <div class="text-xs text-gray-500 leading-relaxed text-center">
        Twoje zdjęcie profilowe dla profilu <span class="font-bold text-gray-900">Bartosz Miazek</span> zostanie także zaktualizowane na platformach Instagram.
      </div>
    </div>

    <!-- Stopka zapisu/anulowania -->
    <div class="border-t border-gray-100 p-4 flex justify-end gap-3 bg-white mt-auto shrink-0">
      <button
        @click="cancelSelection"
        class="px-6 py-2 text-blue-600 font-semibold text-sm hover:bg-blue-50 rounded-lg transition-colors cursor-pointer"
      >
        Anuluj
      </button>
      <button
        @click="savePhoto"
        :disabled="isSaving"
        class="px-8 py-2 bg-blue-600 text-white font-semibold text-sm rounded-lg hover:bg-blue-700 transition-colors shadow-md shadow-blue-200 cursor-pointer disabled:opacity-50"
      >
        {{ isSaving ? 'Zapisywanie...' : 'Zapisz' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useProfilePhotoPost } from '@/composables/feed/useProfilePhotoPost'

import MinusIcon from 'vue-material-design-icons/Minus.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import CropIcon from 'vue-material-design-icons/Crop.vue'
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue'

const props = defineProps({
  isCover: { type: Boolean, default: false },
  autoTrigger: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'updated'])
const auth = useAuthStore()
const config = useRuntimeConfig()
const { createProfilePhotoPost } = useProfilePhotoPost()

// --- STAN ---
const selectedImage = ref<string | null>(null)
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const isSaving = ref(false)

const imgMeteo = 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809'
const imgKucany = 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e'
const imgPalac = 'https://images.unsplash.com/photo-1513694203232-719a280e022f'

const proponowane = ref([imgKucany, imgPalac, imgMeteo])
const przeslane = ref([imgMeteo, imgKucany, imgPalac])
const profilowe = ref([imgKucany, imgPalac])

const description = ref<string>('')
const zoom = ref<number>(100)
const minZoom = ref<number>(50)
const isCropMode = ref<boolean>(false)

const containerRef = ref<HTMLElement | null>(null)
const imageRef = ref<HTMLImageElement | null>(null)
const cropRef = ref<HTMLElement | null>(null)

const position = reactive<{ x: number; y: number }>({ x: 0, y: 0 })
const imageAspectRatio = ref<number>(1)
const containerRatio = ref<number>(1)

const isDragging = ref<boolean>(false)
let startMouseX = 0
let startMouseY = 0
let startPosX = 0
let startPosY = 0

// --- NOWOŚĆ: Obserwator zoomu (zapewnia zoomowanie wokół centrum) ---
watch(zoom, (newZoom, oldZoom) => {
  if (oldZoom && newZoom !== oldZoom) {
    const ratio = newZoom / oldZoom
    position.x *= ratio
    position.y *= ratio
    checkBounds()
  }
})

// --- PAMIĘĆ I EVENTY ---
const clearMemory = () => {
  if (selectedFile.value && selectedImage.value) {
    URL.revokeObjectURL(selectedImage.value)
  }
}

onMounted(() => {
  if (props.autoTrigger) nextTick(() => triggerFileInput())
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDrag)
  window.addEventListener('touchmove', onDrag, { passive: false })
  window.addEventListener('touchend', stopDrag)
})

onUnmounted(() => {
  clearMemory()
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDrag)
  window.removeEventListener('touchmove', onDrag)
  window.removeEventListener('touchend', stopDrag)
})

// --- OBSŁUGA PLIKU ---
const triggerFileInput = () => fileInputRef.value?.click()

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    clearMemory()
    const file = target.files[0]
    selectedFile.value = file
    selectedImage.value = URL.createObjectURL(file)
    resetCropState()
    target.value = ''
  }
}

const selectImage = (url: string) => {
  clearMemory()
  selectedFile.value = null
  selectedImage.value = url
  resetCropState()
}

const resetCropState = () => {
  zoom.value = 100
  position.x = 0
  position.y = 0
  isCropMode.value = false
}

const toggleCropMode = () => {
  isCropMode.value = !isCropMode.value
}

const cancelSelection = () => {
  clearMemory()
  selectedFile.value = null
  selectedImage.value = null
  description.value = ''
  isCropMode.value = false
}

// --- FIZYCZNE PRZYCINANIE (CANVAS) ---
const getCroppedImageBlob = (): Promise<Blob | null> => {
  return new Promise((resolve) => {
    if (!imageRef.value || !containerRef.value || !cropRef.value) {
      resolve(null)
      return
    }

    const img = imageRef.value
    const crop = cropRef.value
    const container = containerRef.value

    const cropWidth = crop.clientWidth
    const cropHeight = crop.clientHeight

    const canvas = document.createElement('canvas')
    canvas.width = cropWidth
    canvas.height = cropHeight
    const ctx = canvas.getContext('2d')

    if (!ctx) {
      resolve(null)
      return
    }

    const currentScale = zoom.value / 100
    const containerW = container.clientWidth
    const containerH = container.clientHeight

    let baseW: number, baseH: number
    if (imageAspectRatio.value > containerRatio.value) {
      baseH = containerH
      baseW = containerH * imageAspectRatio.value
    } else {
      baseW = containerW
      baseH = containerW / imageAspectRatio.value
    }

    const renderedW = baseW * currentScale
    const renderedH = baseH * currentScale

    const imgCenterX = containerW / 2 + position.x
    const imgCenterY = containerH / 2 + position.y

    const drawX = imgCenterX - renderedW / 2 - (containerW - cropWidth) / 2
    const drawY = imgCenterY - renderedH / 2 - (containerH - cropHeight) / 2

    ctx.drawImage(img, drawX, drawY, renderedW, renderedH)
    canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.95)
  })
}

// --- ZAPIS (LOGIKA: 1 LUB 2 PLIKI W ZALEŻNOŚCI OD PROPORCJI I PRZYCISKU) ---
const savePhoto = async () => {
  isSaving.value = true
  try {
    const croppedBlob = await getCroppedImageBlob()

    if (croppedBlob) {
      const formData = new FormData()

      // Zawsze dodajemy przycięty plik jako główny 'file'
      formData.append('file', croppedBlob, 'profile_cropped.jpg')

      // Sprawdzamy, czy zdjęcie NIE jest idealnym kwadratem lub zostało zmodyfikowane
      const isModifiedOrNotSquare =
        Math.abs(imageAspectRatio.value - 1) > 0.01 ||
        position.x !== 0 ||
        position.y !== 0 ||
        zoom.value !== minZoom.value

      // Jeśli NIE kliknięto "Przytnij" ORAZ obrazek wymagał dopasowania
      if (!isCropMode.value && isModifiedOrNotSquare) {
        let originalFile: File | Blob | null = selectedFile.value

        if (!originalFile && selectedImage.value) {
          const response = await fetch(selectedImage.value)
          originalFile = await response.blob()
        }

        if (originalFile) {
          formData.append('original', originalFile, 'profile_original.jpg')
          formData.append('crop_x', position.x.toString())
          formData.append('crop_y', position.y.toString())
          formData.append('zoom', zoom.value.toString())
        }
      }

      const endpoint = props.isCover
        ? `${config.public.apiUrl}/api/users/${auth.currentUserId}/cover`
        : `${config.public.apiUrl}/api/users/${auth.currentUserId}/avatar`

      const uploadResponse = await fetch(endpoint, {
        method: 'POST',
        body: formData,
      })

      if (!uploadResponse.ok) {
        throw new Error('Upload failed')
      }

      // Utwórz post ze zdjęciem, żeby dało się komentować w /photo/
      try {
        let mediaSrc: string | null = null
        const contentType = uploadResponse.headers.get('content-type') || ''
        if (contentType.includes('json')) {
          const userDto = await uploadResponse.json()
          const mediaId = props.isCover
            ? userDto.coverId || userDto.coverPhotoId || userDto.cover_photo_id
            : userDto.avatarId || userDto.avatar_id
          if (mediaId) {
            mediaSrc = `${config.public.apiUrl}/api/users/avatar/${mediaId}`
          }
        }

        // Fallback: odśwież profil GraphQL i weź aktualny URL
        if (!mediaSrc) {
          const profileRes = await fetch(config.public.apiUrl + '/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              query: `query($userId: ID!) { getUserById(userId: $userId) { avatar cover avatarId coverId } }`,
              variables: { userId: String(auth.currentUserId) },
            }),
          })
          const profileJson = await profileRes.json()
          const u = profileJson.data?.getUserById
          mediaSrc = props.isCover
            ? u?.cover || (u?.coverId ? `${config.public.apiUrl}/api/users/avatar/${u.coverId}` : null)
            : u?.avatar || (u?.avatarId ? `${config.public.apiUrl}/api/users/avatar/${u.avatarId}` : null)
        }

        if (mediaSrc) {
          await createProfilePhotoPost(props.isCover ? 'cover' : 'avatar', mediaSrc)
        }
      } catch (postErr) {
        console.warn('Nie udało się utworzyć posta ze zdjęciem profilowym:', postErr)
      }

      emit('updated')
      emit('close')
    } else {
      cancelSelection()
    }
  } catch (error) {
    console.error('Error saving profile photo:', error)
    alert('Błąd podczas zapisywania zdjęcia. Spróbuj ponownie.')
  } finally {
    isSaving.value = false
    selectedImage.value = null
    selectedFile.value = null
  }
}

// --- KADROWANIE ---
const onImageLoad = (): void => {
  nextTick(() => {
    if (!imageRef.value || !containerRef.value || !cropRef.value) return

    const { naturalWidth, naturalHeight } = imageRef.value
    const containerW = containerRef.value.clientWidth
    const containerH = containerRef.value.clientHeight

    imageAspectRatio.value = naturalWidth / naturalHeight
    containerRatio.value = containerW / containerH

    position.x = 0
    position.y = 0

    const cropW = cropRef.value.clientWidth
    const cropH = cropRef.value.clientHeight

    let baseRenderedW: number, baseRenderedH: number

    if (imageAspectRatio.value > containerRatio.value) {
      baseRenderedH = containerH
      baseRenderedW = containerH * imageAspectRatio.value
    } else {
      baseRenderedW = containerW
      baseRenderedH = containerW / imageAspectRatio.value
    }

    const scaleToFitCrop = Math.max(cropW / baseRenderedW, cropH / baseRenderedH)
    const newMinZoom = Math.max(10, Math.ceil(scaleToFitCrop * 100))

    minZoom.value = newMinZoom
    zoom.value = newMinZoom

    checkBounds()
  })
}

const calculateBounds = (): { maxX: number; maxY: number } => {
  if (!containerRef.value || !cropRef.value) return { maxX: 0, maxY: 0 }

  const containerW = containerRef.value.clientWidth
  const containerH = containerRef.value.clientHeight
  const cropW = cropRef.value.clientWidth
  const cropH = cropRef.value.clientHeight

  const currentScale = zoom.value / 100
  let renderedW: number, renderedH: number

  if (imageAspectRatio.value > containerRatio.value) {
    renderedH = containerH * currentScale
    renderedW = containerH * imageAspectRatio.value * currentScale
  } else {
    renderedW = containerW * currentScale
    renderedH = (containerW / imageAspectRatio.value) * currentScale
  }

  const maxX = Math.max(0, (renderedW - cropW) / 2)
  const maxY = Math.max(0, (renderedH - cropH) / 2)

  return { maxX, maxY }
}

const checkBounds = (): void => {
  const bounds = calculateBounds()
  position.x = Math.max(-bounds.maxX, Math.min(position.x, bounds.maxX))
  position.y = Math.max(-bounds.maxY, Math.min(position.y, bounds.maxY))
}

const changeZoom = (amount: number): void => {
  const newZoom = zoom.value + amount
  if (newZoom >= minZoom.value && newZoom <= 250) {
    zoom.value = newZoom
  }
}

const getClientCoords = (e: MouseEvent | TouchEvent): { x: number; y: number } => {
  if ('touches' in e && e.touches.length > 0) {
    return { x: e.touches[0].clientX, y: e.touches[0].clientY }
  } else if ('clientX' in e) {
    return { x: e.clientX, y: e.clientY }
  }
  return { x: 0, y: 0 }
}

const startDrag = (e: MouseEvent | TouchEvent): void => {
  isDragging.value = true
  const coords = getClientCoords(e)
  startMouseX = coords.x
  startMouseY = coords.y
  startPosX = position.x
  startPosY = position.y
}

const onDrag = (e: MouseEvent | TouchEvent): void => {
  if (!isDragging.value) return

  if (e.cancelable) e.preventDefault()

  const coords = getClientCoords(e)
  const deltaX = coords.x - startMouseX
  const deltaY = coords.y - startMouseY

  const bounds = calculateBounds()

  position.x = Math.max(-bounds.maxX, Math.min(startPosX + deltaX, bounds.maxX))
  position.y = Math.max(-bounds.maxY, Math.min(startPosY + deltaY, bounds.maxY))
}

const stopDrag = (): void => {
  isDragging.value = false
}
</script>

<style scoped>
.custom-slider {
  -webkit-appearance: none;
  background: transparent;
}
.custom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #2563eb;
  cursor: pointer;
  margin-top: -8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}
.custom-slider::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: #e5e7eb;
  border-radius: 2px;
}
</style>
