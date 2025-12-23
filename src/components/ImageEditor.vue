<script setup lang="ts">
// --- FLOATING VUE ---
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';

import CropIcon from 'vue-material-design-icons/Crop.vue';
import RotateRightIcon from 'vue-material-design-icons/RotateRight.vue';
import TagIcon from 'vue-material-design-icons/Tag.vue';
import FormatLetterCaseIcon from 'vue-material-design-icons/FormatLetterCase.vue';
import FileImageIcon from 'vue-material-design-icons/FileImage.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';

import { ref, reactive, onMounted, onUnmounted, computed, watchEffect, nextTick, watch } from 'vue';
import VueCropper from 'vue-cropperjs';
import 'cropperjs/dist/cropper.css';

import { useStoryElementInteraction } from '@/composables/useStoryElementInteraction';
import StoryElement from './StoryElement.vue';
import ImageTag from './ImageTag.vue';
import TagList from './TagList.vue';
import EditorSidebar from './EditorSidebar.vue';
import type { StoryElement as StoryElementType } from '@/types/StoryElement';
import type { ImageTagType } from '@/types/ImageTag';

import { useCreatePostStore } from '@/stores/createPost';

// --- TYPY DANYCH ---
type CropData = {
  x: number;
  y: number;
  width: number;
  height: number;
  rotate: number;
};

// --- MOCK DANYCH UŻYTKOWNIKÓW ---
const MOCK_USERS = [
  { id: 1, name: 'Marcin Chwedoruk', avatar: 'https://ui-avatars.com/api/?name=Marcin+Chwedoruk&background=random&color=fff' },
  { id: 2, name: 'Mateusz Sak', avatar: 'https://ui-avatars.com/api/?name=Mateusz+Sak&background=random&color=fff' },
  { id: 3, name: 'Paweł Szlaski', avatar: 'https://ui-avatars.com/api/?name=Paweł+Szlaski&background=random&color=fff' },
  { id: 4, name: 'Kornel Boguś', avatar: 'https://ui-avatars.com/api/?name=Kornel+Boguś&background=random&color=fff' },
  { id: 5, name: 'Marcin Krasnodębski', avatar: 'https://ui-avatars.com/api/?name=Marcin+Krasnodębski&background=random&color=fff' },
  { id: 6, name: 'Łukasz Bieńczak', avatar: 'https://ui-avatars.com/api/?name=Łukasz+Bieńczak&background=random&color=fff' },
  { id: 7, name: 'Jakub Wierzbicki', avatar: 'https://ui-avatars.com/api/?name=Jakub+Wierzbicki&background=random&color=fff' },
  { id: 8, name: 'Mateusz Golian', avatar: 'https://ui-avatars.com/api/?name=Mateusz+Golian&background=random&color=fff' },
  { id: 9, name: 'Marcin Klamka', avatar: 'https://ui-avatars.com/api/?name=Marcin+Klamka&background=random&color=fff' },
  { id: 10, name: 'Artur Sieraj', avatar: 'https://ui-avatars.com/api/?name=Artur+Sieraj&background=random&color=fff' },
];

// --- PROPS ---
const props = defineProps<{
  initialImage?: string | null;
}>();

const emit = defineEmits<{
  (e: 'done', editedImageUrl: string): void;
}>();

// --- STATE ---
const createPostStore = useCreatePostStore();
const imageRotation = ref(0);
const taggingMode = ref(false);
const tags = ref<ImageTagType[]>([]);

const newTag = ref<{ x: number, y: number, name: string, isCreating: boolean } | null>(null);
const newTagInputRef = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const cropperRef = ref<any | null>(null);
const isCroppingMode = ref(false);

const currentCropData = ref<CropData>({
  x: 0,
  y: 0,
  width: 0,
  height: 0,
  rotate: 0
});
const imageUrl = ref(createPostStore.selectedImage?.url || props.initialImage);
const altText = ref(createPostStore.selectedImage?.altText || '');
watch(altText, (newAltText) => {
  if (createPostStore.selectedImage) {
    createPostStore.selectedImage.altText = newAltText;
  } else {
    // If there's no selected image, but alt text is being set,
    // this scenario might need further consideration based on UX.
    // For now, we'll just set it if an image exists.
    createPostStore.setSelectedImage({ url: imageUrl.value || '', altText: newAltText });
  }
});
const showAltTextInput = ref(false);

const cropperOptions = reactive({
  viewMode: 1,
  dragMode: 'move',
  aspectRatio: NaN,
  autoCropArea: 0.8,
  background: false,
  movable: true,
  rotatable: true,
  zoomable: true,
});

// --- WATCHERS & COMPUTED ---
watchEffect(() => {
  if (imageUrl.value && cropperRef.value) {
    cropperRef.value.replace(imageUrl.value);
  }
});

const filteredUsers = computed(() => {
  if (!searchQuery.value) return MOCK_USERS;
  const lowerQuery = searchQuery.value.toLowerCase();
  return MOCK_USERS.filter(user =>
    user.name.toLowerCase().includes(lowerQuery)
  );
});

// --- TOOLBAR ---
const tools = [
  { id: 1, label: 'Przytnij', icon: CropIcon, action: 'toggleCropMode' },
  { id: 2, label: 'Obróć', icon: RotateRightIcon, action: 'rotateImage' },
  { id: 3, label: 'Oznacz zdjęcie', icon: TagIcon, action: 'addTag' },
  { id: 4, label: 'Narzędzie tekstowe', icon: FormatLetterCaseIcon, action: 'addTextElement' },
  { id: 5, label: 'Tekst alternatywny', icon: FileImageIcon, action: 'toggleAltText' },
];

const handleToolAction = (action: string | undefined) => {
  if (action === 'addTextElement') addTextElement();
  else if (action === 'rotateImage') rotateImage();
  else if (action === 'toggleCropMode') toggleCropMode();
  else if (action === 'addTag') taggingMode.value = true;
  else if (action === 'toggleAltText') showAltTextInput.value = !showAltTextInput.value;
};

// --- LOGIKA STORY ELEMENTS ---
const storyElements = ref<StoryElementType[]>([]);
const imageWrapperRef = ref<HTMLElement | null>(null);
const bgDimensions = reactive({ width: 0, height: 0 });

const {
  activeDragId, activeResizeId, croppingId, editingId, selectedElementId,
  startDrag, startResize, stopInteraction, enableEdit, disableEdit,
  onBackgroundClick, toggleCrop,
} = useStoryElementInteraction(storyElements, bgDimensions);

onMounted(() => {
  if (imageWrapperRef.value) {
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        bgDimensions.width = entry.contentRect.width;
        bgDimensions.height = entry.contentRect.height;
      }
    });
    observer.observe(imageWrapperRef.value);
  }
  window.addEventListener('mouseup', stopInteraction);
});

onUnmounted(() => {
  window.removeEventListener('mouseup', stopInteraction);
});

// --- LOGIKA CROPPERA ---
const toggleCropMode = () => {
  isCroppingMode.value = !isCroppingMode.value;
  if (isCroppingMode.value && cropperRef.value) {
    nextTick(() => {
      cropperRef.value.rotateTo(imageRotation.value);
    });
  }
};

const onCropChange = (event: CustomEvent) => {
  const detail = event.detail;
  currentCropData.value = {
    x: detail.x,
    y: detail.y,
    width: detail.width,
    height: detail.height,
    rotate: detail.rotate
  };
};

const handleCropConfirm = () => {
  if (!cropperRef.value) return;
  const crop = currentCropData.value;
  if (crop.width <= 0 || crop.height <= 0) return;

  const canvas = cropperRef.value.getCroppedCanvas({
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high',
  });

  if (!canvas) return;

  canvas.toBlob((blob: Blob | null) => {
    if (!blob) return;
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target && e.target.result) {
        const imageData = cropperRef.value.getImageData();
        const nw = imageData.naturalWidth;
        const nh = imageData.naturalHeight;
        const rotation = ((crop.rotate % 360) + 360) % 360;

        const newTags = tags.value.map(tag => {
          const oldX = (tag.x / 100) * nw;
          const oldY = (tag.y / 100) * nh;

          let rotatedX = oldX;
          let rotatedY = oldY;

          if (rotation === 90) {
            rotatedX = nh - oldY;
            rotatedY = oldX;
          } else if (rotation === 180) {
            rotatedX = nw - oldX;
            rotatedY = nh - oldY;
          } else if (rotation === 270) {
            rotatedX = oldY;
            rotatedY = nw - oldX;
          }

          if (
            rotatedX >= crop.x && rotatedX <= crop.x + crop.width &&
            rotatedY >= crop.y && rotatedY <= crop.y + crop.height
          ) {
            const localX = rotatedX - crop.x;
            const localY = rotatedY - crop.y;
            return {
              ...tag,
              x: (localX / crop.width) * 100,
              y: (localY / crop.height) * 100,
            };
          }
          return null;
        }).filter((t): t is ImageTagType => t !== null);

        imageUrl.value = e.target.result as string;
        tags.value = newTags;
        isCroppingMode.value = false;
        imageRotation.value = 0;
      }
    };
    reader.readAsDataURL(blob);
  }, 'image/png');
};

const handleCropCancel = () => {
  isCroppingMode.value = false;
};

const handleDone = () => {
  if (imageUrl.value) {
    emit('done', imageUrl.value);
  }
};

const handleCancel = () => console.log('Anuluj');

// --- LOGIKA OBROTU ---
const rotateImage = () => {
  if (isCroppingMode.value && cropperRef.value) {
    cropperRef.value.rotate(90);
  } else {
    imageRotation.value = (imageRotation.value + 90) % 360;
  }
};

// --- LOGIKA TAGOWANIA ---
const handleImageClickForTagging = async (event: MouseEvent) => {
  if (!taggingMode.value) return;
  const container = event.currentTarget as HTMLElement;
  const rect = container.getBoundingClientRect();

  if (rect.width === 0 || rect.height === 0) return;

  const clickX = event.clientX - rect.left;
  const clickY = event.clientY - rect.top;

  const cx = rect.width / 2;
  const cy = rect.height / 2;

  const angleRad = -imageRotation.value * (Math.PI / 180);

  const rotatedX = (clickX - cx) * Math.cos(angleRad) - (clickY - cy) * Math.sin(angleRad) + cx;
  const rotatedY = (clickX - cx) * Math.sin(angleRad) + (clickY - cy) * Math.cos(angleRad) + cy;

  const finalXPerc = (rotatedX / rect.width) * 100;
  const finalYPerc = (rotatedY / rect.height) * 100;

  if (finalXPerc < 0 || finalXPerc > 100 || finalYPerc < 0 || finalYPerc > 100) return;

  newTag.value = { x: finalXPerc, y: finalYPerc, name: '', isCreating: true };
  searchQuery.value = ''; // Reset wyszukiwania
  taggingMode.value = false;

  await nextTick();
  // Focusowanie po pojawieniu się modala
  if (newTagInputRef.value) newTagInputRef.value.focus();
};

const createTag = () => {
  const nameToSave = searchQuery.value || newTag.value?.name;

  if (newTag.value && nameToSave && nameToSave.trim()) {
    tags.value.push({
      id: `tag_${Date.now()}`,
      x: newTag.value.x,
      y: newTag.value.y,
      name: nameToSave,
      isTemp: false,
    });
  }
  newTag.value = null;
  searchQuery.value = '';
};

const selectUser = (userName: string) => {
  if (newTag.value) {
    newTag.value.name = userName;
    searchQuery.value = userName;
    createTag();
  }
};

const removeTag = (id: string) => {
  tags.value = tags.value.filter(tag => tag.id !== id);
};

// --- WIZUALIZACJA TAGÓW ---
const transformedTags = computed(() => {
  if (!bgDimensions.width || !bgDimensions.height) return [];

  const allTags = tags.value.map(t => ({ ...t, isTemp: false }));
  if (newTag.value && newTag.value.isCreating) {
    allTags.push({
      id: 'temp_new', x: newTag.value.x, y: newTag.value.y,
      name: newTag.value.name, isTemp: true
    });
  }

  const w = bgDimensions.width;
  const h = bgDimensions.height;
  const cx = w / 2;
  const cy = h / 2;
  const angleRad = imageRotation.value * (Math.PI / 180);

  return allTags.map(tag => {
    const pxX = (tag.x / 100) * w;
    const pxY = (tag.y / 100) * h;

    const newX = (pxX - cx) * Math.cos(angleRad) - (pxY - cy) * Math.sin(angleRad) + cx;
    const newY = (pxX - cx) * Math.sin(angleRad) + (pxY - cy) * Math.cos(angleRad) + cy;

    return {
      ...tag,
      x: newX,
      y: newY
    };
  });
});

// --- TEKST ---
const addTextElement = () => {
  const newId = `el_${Date.now()}`;
  storyElements.value.push({
    id: newId, type: 'text', content: 'Wpisz tekst',
    x: 50, y: 50, width: 200, height: 50, rotation: 0, scale: 1,
    styles: { color: '#ffffff', fontSize: '30px', fontWeight: 'bold' }
  });
  selectedElementId.value = newId;
};

const updateImageDimensions = (event: Event) => {
  const img = event.target as HTMLImageElement;
  if (img) {
    bgDimensions.width = img.offsetWidth;
    bgDimensions.height = img.offsetHeight;
  }
};

const removeElement = (id: string) => {
  storyElements.value = storyElements.value.filter((el) => el.id !== id);
  if (selectedElementId.value === id) selectedElementId.value = null;
};

const updateElementContent = (id: string, value: string) => {
  const target = storyElements.value.find((el) => el.id === id);
  if (target) target.content = value;
};
</script>

<template>
  <div class="fixed inset-0 flex z-50 h-full w-screen bg-black overflow-hidden font-sans">

    <EditorSidebar
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
      @update:altText="(value) => altText = value"
    />

    <main class="flex-1 bg-[#18191a] relative flex flex-col h-full">

      <div class="absolute inset-0 z-0 blur-background"
           :style="{ backgroundImage: imageUrl ? `url(${imageUrl})` : 'none' }">
      </div>

      <div class="flex-1 flex items-center justify-center overflow-hidden relative z-10" @mousedown.self="onBackgroundClick">

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
            class="relative max-w-full max-h-full"
            :class="{ 'cursor-crosshair': taggingMode }"
            @click="handleImageClickForTagging"
          >
            <img
              v-if="imageUrl"
              :src="imageUrl"
              ref="imageWrapperRef"
              alt="Edytowane zdjęcie"
              class="max-h-full object-contain h-[500px] shadow-2xl rounded-sm transition-transform duration-300 ease-out"
              :style="{ transform: `rotate(${imageRotation}deg)` }"
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
                  top: `${tag.y}px`
                }"
              >
                <div class="w-24 h-24 border-2 border-white/80 shadow-[0_0_10px_rgba(0,0,0,0.3)] rounded-sm -translate-x-1/2 -translate-y-1/2 cursor-default"></div>

                <template #popper>
                  <div class="w-[340px] flex flex-col font-sans bg-white text-left" @click.stop>

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
                        :key="user.id"
                        @click="selectUser(user.name)"
                        class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors group"
                      >
                        <div class="w-10 h-10 rounded-full overflow-hidden shrink-0 border border-gray-100">
                          <img :src="user.avatar" :alt="user.name" class="w-full h-full object-cover" />
                        </div>
                        <span class="text-[15px] text-gray-900 font-medium group-hover:text-black">
                          {{ user.name }}
                        </span>
                      </div>

                      <div v-if="filteredUsers.length === 0" class="px-4 py-4 text-sm text-gray-500 text-center">
                        Brak wyników dla "{{ searchQuery }}".<br/>Naciśnij Enter, aby dodać nowy tag.
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
                  top: `${tag.y}px`
                }"
              >
                <ImageTag
                  :tag="tag"
                  :image-rotation="imageRotation"
                />
              </div>
            </template>
          </div>

          <StoryElement
            v-for="element in storyElements"
            :key="element.id"
            :element="element"
            :state="{
              active: activeDragId === element.id || activeResizeId === element.id || selectedElementId === element.id,
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
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
  border: 1px solid rgba(0,0,0,0.05);
}

:deep(.v-popper__arrow-container) {
  display: none; /* Opcjonalnie: ukryj strzałkę jeśli chcesz "płaski" wygląd */
}
</style>
