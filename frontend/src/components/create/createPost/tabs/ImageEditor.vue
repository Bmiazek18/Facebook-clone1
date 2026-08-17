<script setup lang="ts">
// --- IMPORTY ---
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

import CropIcon from 'vue-material-design-icons/Crop.vue'
import RotateRightIcon from 'vue-material-design-icons/RotateRight.vue'
import TagIcon from 'vue-material-design-icons/Tag.vue'
import FormatLetterCaseIcon from 'vue-material-design-icons/FormatLetterCase.vue'
import FileImageIcon from 'vue-material-design-icons/FileImage.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'

import { ref, reactive, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import VueCropper from 'vue-cropperjs'
import 'cropperjs/dist/cropper.css'
import { storeToRefs } from 'pinia'

import { useStoryElementInteraction } from '@/composables/media/useStoryElementInteraction'
import StoryElement from '@/components/create/createStory/StoryElement.vue'
import ImageTag from '@/components/media/ImageTag.vue'
import EditorSidebar from '../item/EditorSidebar.vue'

import type { StoryElement as StoryElementType } from '@/types/StoryElement'
import type { ImageTagType } from '@/types/Post'
import { useCreatePostStore } from '@/stores/createPost'
import type { User } from '@/utils/users'
import type { Person } from '@/types/Person'
import { useFriendSearch } from '@/composables/shared/useFriendSearch'

// --- TYPY DANYCH ---
type CropData = {
  x: number
  y: number
  width: number
  height: number
  rotate: number
}

const { users: searchableUsers, isLoading: isUserSearchLoading, loadSuggestions, searchUsers } =
  useFriendSearch()

const userToPerson = (user: User): Person => ({
  id: user.id,
  name: user.name,
  imageUrl: user.avatar,
  commonFriends: user.mutualFriendsCount || 0,
  isFriend: true,
})

// ==========================================
// 1. PINIA STORE
// ==========================================
const createPostStore = useCreatePostStore()
const { uiState, postData } = storeToRefs(createPostStore)

// ==========================================
// 2. REFS I REACTIVE
// ==========================================
const cropperRef = ref<InstanceType<typeof VueCropper> | null>(null)
const imageWrapperRef = ref<HTMLElement | null>(null)
const newTagInputRef = ref<HTMLInputElement | null>(null)

const imageUrl = ref(uiState.value.imageToEdit?.url || '')
const altText = ref(uiState.value.imageToEdit?.altText || '')

const imageRotation = ref(0)
const taggingMode = ref(false)
const isCroppingMode = ref(false)
const showAltTextInput = ref(false)
const searchQuery = ref('')
const newTag = ref<{ x: number; y: number; name: string; isCreating: boolean; user?: User } | null>(null)

const currentCropData = ref<CropData>({ x: 0, y: 0, width: 0, height: 0, rotate: 0 })
const storyElements = ref<StoryElementType[]>([])
const bgDimensions = reactive({ width: 0, height: 0 })

let resizeObserver: ResizeObserver | null = null

const cropperOptions = reactive({
  viewMode: 1,
  dragMode: 'move',
  aspectRatio: NaN,
  autoCropArea: 0.8,
  background: false,
  movable: true,
  rotatable: true,
  zoomable: true,
})

// ==========================================
// 3. COMPUTED VARIABLES
// ==========================================
const taggedUsers = computed(() => postData.value.taggedUsers)

const selectedImage = computed(() => {
  const idx = uiState.value.imageIndexToEdit
  if (idx !== null && postData.value.images[idx]) {
    return postData.value.images[idx]
  }
  return uiState.value.imageToEdit
})

const tags = ref<ImageTagType[]>(selectedImage.value?.tags ? [...selectedImage.value.tags] : [])

const filteredUsers = computed(() => searchableUsers.value)

watch(searchQuery, (q) => {
  if (newTag.value?.isCreating) {
    searchUsers(q)
  }
})

const transformedTags = computed(() => {
  if (!bgDimensions.width || !bgDimensions.height) return []

  const allTags = tags.value.map((t) => ({ ...t, isTemp: false }))
  if (newTag.value && newTag.value.isCreating) {
    allTags.push({
      id: 'temp_new',
      x: newTag.value.x,
      y: newTag.value.y,
      name: newTag.value.name,
      isTemp: true,
    })
  }

  const w = bgDimensions.width
  const h = bgDimensions.height

  return allTags.map((tag) => {
    return {
      ...tag,
      x: (tag.x / 100) * w,
      y: (tag.y / 100) * h,
    }
  })
})

// ==========================================
// 4. WATCHERS
// ==========================================
watch(
  () => uiState.value.imageToEdit?.url,
  (newUrl) => {
    if (newUrl && newUrl !== imageUrl.value) {
      imageUrl.value = newUrl
    }
  }
)

watch(
  () => uiState.value.imageToEdit?.altText,
  (newAltText) => {
    if (newAltText !== undefined && newAltText !== altText.value) {
      altText.value = newAltText
    }
  }
)

watch(
  tags,
  (newTags) => {
    if (selectedImage.value) {
      selectedImage.value.tags = newTags
    }
  },
  { deep: true },
)

// ==========================================
// 5. COMPOSABLES
// ==========================================
const {
  activeDragId,
  activeResizeId,
  croppingId,
  editingId,
  selectedElementId,
  startDrag,
  startResize,
  stopInteraction,
  enableEdit,
  disableEdit,
  onBackgroundClick,
  toggleCrop,
} = useStoryElementInteraction(storyElements, bgDimensions)


// ==========================================
// 6. LIFECYCLE HOOKS & FUNKCJE
// ==========================================
onMounted(() => {
  loadSuggestions()
  if (imageWrapperRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        bgDimensions.width = entry.contentRect.width
        bgDimensions.height = entry.contentRect.height
      }
    })
    resizeObserver.observe(imageWrapperRef.value)
  }
  window.addEventListener('mouseup', stopInteraction)
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  window.removeEventListener('mouseup', stopInteraction)
})

const handleAltTextUpdate = (newText: string) => {
  altText.value = newText
  if (selectedImage.value) {
    selectedImage.value.altText = newText
  }
}

const toggleCropMode = () => {
  isCroppingMode.value = !isCroppingMode.value
  if (isCroppingMode.value && cropperRef.value) {
    nextTick(() => {
      cropperRef.value?.rotateTo?.(imageRotation.value)
    })
  }
}

const onCropChange = (event: CustomEvent) => {
  const detail = event.detail
  currentCropData.value = {
    x: detail.x,
    y: detail.y,
    width: detail.width,
    height: detail.height,
    rotate: detail.rotate,
  }
}

const handleCropConfirm = () => {
  if (!cropperRef.value) return
  const crop = currentCropData.value
  if (crop.width <= 0 || crop.height <= 0) return

  const canvas = cropperRef.value.getCroppedCanvas({
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high',
  })

  if (!canvas) return

  const imageData = cropperRef.value.getImageData()
  const nw = imageData.naturalWidth
  const nh = imageData.naturalHeight
  const rotation = ((crop.rotate % 360) + 360) % 360

  tags.value = tags.value.map((tag) => {
    let oldX = (tag.x / 100) * nw
    let oldY = (tag.y / 100) * nh
    let rotatedX = oldX, rotatedY = oldY

    if (rotation === 90) { rotatedX = nh - oldY; rotatedY = oldX }
    else if (rotation === 180) { rotatedX = nw - oldX; rotatedY = nh - oldY }
    else if (rotation === 270) { rotatedX = oldY; rotatedY = nw - oldX }

    if (rotatedX >= crop.x && rotatedX <= crop.x + crop.width &&
        rotatedY >= crop.y && rotatedY <= crop.y + crop.height) {
      return {
        ...tag,
        x: ((rotatedX - crop.x) / crop.width) * 100,
        y: ((rotatedY - crop.y) / crop.height) * 100,
      }
    }
    return null
  }).filter((t): t is ImageTagType => t !== null)

  imageUrl.value = canvas.toDataURL('image/png')
  isCroppingMode.value = false
  imageRotation.value = 0
}

const handleCropCancel = () => {
  isCroppingMode.value = false
}

const handleDone = () => {
  createPostStore.saveEditedMedia(imageUrl.value)
}

const handleCancel = () => {
  createPostStore.navigateBack()
}

const rotateImage = () => {
  if (isCroppingMode.value && cropperRef.value) {
    cropperRef.value.rotate(90)
  } else if (imageUrl.value) {
    const img = new Image()
    img.src = imageUrl.value
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')

      if (!ctx) {
        console.warn('Nie udało się uzyskać kontekstu 2D dla obrotu obrazu.')
        return
      }

      const width = img.width
      const height = img.height

      canvas.width = height
      canvas.height = width

      ctx.save()
      ctx.translate(canvas.width / 2, canvas.height / 2)
      ctx.rotate((90 * Math.PI) / 180)
      ctx.drawImage(img, -width / 2, -height / 2, width, height)
      ctx.restore()

      const rotatedTags = tags.value.map((tag) => ({
        ...tag,
        x: 100 - tag.y,
        y: tag.x,
      }))

      tags.value = rotatedTags
      imageUrl.value = canvas.toDataURL('image/png')
      imageRotation.value = 0
    }
  }
}

const handleImageClickForTagging = async (event: MouseEvent) => {
  if (!taggingMode.value) return

  const img = event.target as HTMLImageElement
  const rect = img.getBoundingClientRect()

  const clickX = event.clientX - rect.left
  const clickY = event.clientY - rect.top

  const finalXPerc = (clickX / rect.width) * 100
  const finalYPerc = (clickY / rect.height) * 100

  if (finalXPerc < 0 || finalXPerc > 100 || finalYPerc < 0 || finalYPerc > 100) return

  newTag.value = { x: finalXPerc, y: finalYPerc, name: '', isCreating: true }
  searchQuery.value = ''
  taggingMode.value = false
  await loadSuggestions()

  await nextTick()
  if (newTagInputRef.value) newTagInputRef.value.focus()
}

const createTag = () => {
  const nameToSave = searchQuery.value || newTag.value?.name
  const user = newTag.value?.user

  if (newTag.value && nameToSave && nameToSave.trim()) {
    const tag: ImageTagType = {
      id: `tag_${Date.now()}`,
      x: newTag.value.x,
      y: newTag.value.y,
      name: nameToSave.trim(),
      isTemp: false,
    }
    if (user) {
      tag.userId = String(user.id)
      tag.user = userToPerson(user)
    }
    tags.value.push(tag)
  }
  newTag.value = null
  searchQuery.value = ''
}

const selectUser = (user: User) => {
  if (newTag.value) {
    newTag.value.name = user.name
    newTag.value.user = user
    searchQuery.value = user.name
    createTag()

    if (!taggedUsers.value.some((tu) => String(tu.id) === String(user.id))) {
      createPostStore.addTaggedUser(user)
    }
  }
}

const removeTag = (id: string) => {
  tags.value = tags.value.filter((tag) => tag.id !== id)
}

const addTextElement = () => {
  const newId = `el_${Date.now()}`
  storyElements.value.push({
    id: newId,
    type: 'text',
    content: 'Wpisz tekst',
    x: 50,
    y: 50,
    width: 200,
    height: 50,
    rotation: 0,
    scale: 1,
    styles: { color: '#ffffff', fontSize: '30px', fontWeight: 'bold' },
  })
  selectedElementId.value = newId
}

const updateImageDimensions = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img) {
    bgDimensions.width = img.offsetWidth
    bgDimensions.height = img.offsetHeight
  }
}

const removeElement = (id: string) => {
  storyElements.value = storyElements.value.filter((el) => el.id !== id)
  if (selectedElementId.value === id) selectedElementId.value = null
}

const updateElementContent = (id: string, value: string) => {
  const target = storyElements.value.find((el) => el.id === id)
  if (target) target.content = value
}

const tools = [
  { id: 1, label: 'Przytnij', icon: CropIcon, action: 'toggleCropMode' },
  { id: 2, label: 'Obróć', icon: RotateRightIcon, action: 'rotateImage' },
  { id: 3, label: 'Oznacz zdjęcie', icon: TagIcon, action: 'addTag' },
  { id: 4, label: 'Narzędzie tekstowe', icon: FormatLetterCaseIcon, action: 'addTextElement' },
  { id: 5, label: 'Tekst alternatywny', icon: FileImageIcon, action: 'toggleAltText' },
]

const handleToolAction = (action: string | undefined) => {
  if (action === 'addTextElement') addTextElement()
  else if (action === 'rotateImage') rotateImage()
  else if (action === 'toggleCropMode') toggleCropMode()
  else if (action === 'addTag') taggingMode.value = true
  else if (action === 'toggleAltText') showAltTextInput.value = !showAltTextInput.value
}
</script>

<template>
  <div class="flex h-[80vh] w-full flex-col lg:flex-row bg-black overflow-hidden relative">
    <!-- Sidebar -->
    <EditorSidebar
      class="hidden lg:flex"
      :tools="tools"
      :tags="tags"
      :is-cropping-mode="isCroppingMode"
      :alt-text="altText"
      :show-alt-text-input="showAltTextInput"
      @tool-action="handleToolAction"
      @remove-tag="removeTag"
      @confirm-crop="handleCropConfirm"
      @cancel-crop="handleCropCancel"
      @done="handleDone"
      @cancel-edit="handleCancel"
      @update:altText="handleAltTextUpdate"
    />

    <main class="flex-1 bg-[#18191a] relative flex flex-col h-full">
      <div
        class="absolute inset-0 z-0 blur-background"
        :style="{ backgroundImage: imageUrl ? `url(${imageUrl})` : 'none' }"
      ></div>

      <div
        class="flex-1 flex items-center justify-center overflow-hidden relative z-10 p-[1px]"
        @mousedown.self="onBackgroundClick"
      >
        <VueCropper
          v-if="isCroppingMode"
          ref="cropperRef"
          :src="imageUrl"
          alt="Edytowane zdjęcie"
          class="w-full h-full"
          v-bind="cropperOptions"
          @crop="onCropChange"
        ></VueCropper>

        <template v-else>
          <div
            class="relative flex justify-center h-full w-full"
            :class="{ 'cursor-crosshair': taggingMode }"
          >
            <div class="relative">
              <img
                v-if="imageUrl"
                :src="imageUrl"
                ref="imageWrapperRef"
                alt="Edytowane zdjęcie"
                class="max-h-[50vh] lg:max-h-[70vh] max-w-[85vw] lg:max-w-[800px] object-contain w-auto h-full shadow-2xl transition-transform duration-300 ease-out"
                :style="{ transform: `rotate(${imageRotation}deg)` }"
                @click="handleImageClickForTagging"
                @load="updateImageDimensions"
              />

              <template v-for="tag in transformedTags" :key="tag.id">
                <VDropdown
                  v-if="tag.isTemp"
                  :shown="true"
                  :triggers="[]"
                  :auto-hide="false"
                  :distance="10"
                  placement="bottom"
                  class="absolute z-50"
                  :style="{
                    left: `${tag.x}px`,
                    top: `${tag.y}px`,
                  }"
                >
                  <div
                    class="w-24 h-24 border-2 border-white/80 shadow-[0_0_10px_rgba(0,0,0,0.3)] rounded-sm -translate-x-1/2 -translate-y-1/2 cursor-default"
                  ></div>

                  <template #popper>
                    <div class="w-[340px] flex flex-col bg-white text-left" @click.stop>
                      <div class="p-3 border-b border-gray-100 flex items-center gap-3">
                        <MagnifyIcon :size="20" class="text-gray-400 shrink-0" />
                        <input
                          ref="newTagInputRef"
                          v-model="searchQuery"
                          type="text"
                          class="w-full text-[15px] outline-none text-gray-700 placeholder-gray-400 bg-transparent py-1"
                          placeholder="Wprowadź dowolne imię i nazwisko"
                          @keydown.enter="createTag"
                          autoFocus
                        />
                      </div>

                      <div class="max-h-[320px] overflow-y-auto py-1 scrollbar-thin">
        <div
                          v-for="user in filteredUsers"
                          :key="String(user.id)"
                          @click="selectUser(user)"
                          class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors group"
                        >
                          <div
                            class="w-10 h-10 rounded-full overflow-hidden shrink-0 border border-gray-100"
                          >
                            <img
                              :src="user.avatar"
                              :alt="user.name"
                              class="w-full h-full object-cover"
                            />
                          </div>
                          <span
                            class="text-[15px] text-gray-900 font-medium group-hover:text-black"
                          >
                            {{ user.name }}
                          </span>
                        </div>

                        <div
                          v-if="isUserSearchLoading"
                          class="px-4 py-4 text-sm text-gray-500 text-center"
                        >
                          Szukam...
                        </div>
                        <div
                          v-else-if="filteredUsers.length === 0"
                          class="px-4 py-4 text-sm text-gray-500 text-center"
                        >
                          Brak wyników dla "{{ searchQuery }}".<br />Naciśnij Enter, aby dodać nowy
                          tag.
                        </div>
                      </div>
                    </div>
                  </template>
                </VDropdown>

                <div
                  v-else
                  class="absolute"
                  :style="{
                    left: `${tag.x}px`,
                    top: `${tag.y}px`,
                  }"
                >
                  <ImageTag :tag="tag" />
                </div>
              </template>
            </div>
          </div>

          <StoryElement
            v-for="element in storyElements"
            :key="element.id"
            :element="element"
            :state="{
              active:
                activeDragId === element.id ||
                activeResizeId === element.id ||
                selectedElementId === element.id,
              cropping: croppingId === element.id,
              editing: editingId === element.id,
              selected: selectedElementId === element.id,
            }"
            :on-start-drag="startDrag"
            :on-start-resize="startResize"
            :on-toggle-crop="toggleCrop"
            :on-enable-edit="enableEdit"
            :on-disable-edit="disableEdit"
            :on-remove="removeElement"
            @update-content="updateElementContent"
          />
        </template>
      </div>

      <!-- Zoptymalizowany Mobile Toolbar -->
      <div class="lg:hidden absolute bottom-0 left-0 right-0 bg-white/95 backdrop-blur-sm border-t border-gray-200 z-20">

        <!-- Narzędzia wyświetlane tylko gdy NIE kadrujemy -->
        <div v-if="!isCroppingMode" class="flex items-center justify-around p-2 border-b border-gray-200">
          <button
            v-for="tool in tools.slice(0, 5)"
            :key="tool.id"
            @click="handleToolAction(tool.action)"
            class="flex flex-col items-center gap-1 p-2 rounded-lg hover:bg-gray-100 active:scale-95 transition-all"
          >
            <div class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center">
              <component :is="tool.icon" :size="20" />
            </div>
            <span class="text-xs text-gray-700 text-center leading-tight max-w-[60px]">
              {{ tool.label }}
            </span>
          </button>
        </div>

        <!-- Dynamiczne przyciski akcji (Zapisz / Anuluj) -->
        <div class="flex gap-2 p-3">
          <button
            @click="isCroppingMode ? handleCropConfirm() : handleDone()"
            :class="isCroppingMode ? 'bg-green-500 hover:bg-green-600' : 'bg-blue-500 hover:bg-blue-600'"
            class="flex-1 py-3 px-4 text-white font-semibold rounded-lg text-sm transition-colors"
          >
            {{ isCroppingMode ? 'Przytnij' : 'Gotowe' }}
          </button>
          <button
            @click="isCroppingMode ? handleCropCancel() : handleCancel()"
            class="flex-1 py-3 px-4 bg-gray-200 hover:bg-gray-300 text-gray-900 font-semibold rounded-lg text-sm transition-colors"
          >
            Anuluj
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.blur-background {
  background-size: 125%;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(20px) saturate(30%) brightness(30%);
  -webkit-filter: blur(20px) saturate(30%) brightness(30%);
}

.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 20px;
}

:deep(.v-popper__inner) {
  background: white;
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
    0 20px 25px -5px rgb(0 0 0 / 0.1),
    0 8px 10px -6px rgb(0 0 0 / 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.v-popper__arrow-container) {
  display: none;
}
</style>
