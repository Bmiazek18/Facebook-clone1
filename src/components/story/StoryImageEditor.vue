<script setup lang="ts">
import { ref, reactive, onUnmounted, computed, onMounted } from 'vue';
import { calculateSnaps, type Guide } from '@/utils/snapping';

// --- IMPORT KOMPONENTÓW ---
import MusicModal, { type MusicTrack } from '@/components/MusicModal.vue';
import LinkStickerModal from '@/components/LinkStickerModal.vue';
import PostShareModal from '@/components/PostShareModal.vue';
import StorySidebar from '@/components/StorySidebar.vue';
import ImageToolbar from '@/components/ImageToolbar.vue';
import MusicToolbar from '@/components/MusicToolbar.vue';
import StoryElement from '@/components/StoryElement.vue';
// --- IKONY ---
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeOff from 'vue-material-design-icons/VolumeOff.vue';

import type { StoryElement as StoryElementType, BackgroundSettings, PostData, ReelData } from '@/types/StoryElement';

// --- PROPS & EMITS ---
const props = defineProps<{
  initialImage?: string | null;
  initialPost?: PostData | null;
  initialReel?: ReelData | null;
}>();

const emit = defineEmits<{
  back: [];
}>();

// --- STAŁE ---
const CANVAS_WIDTH = 558;
const CANVAS_HEIGHT = 1000;

// --- STAN ---
const background = reactive<BackgroundSettings>({
  type: 'gradient',
  value: 'linear-gradient(to bottom, #3b82f6, #86efac)'
});

const setBackgroundGradientFromImage = (imageUrl: string) => {
  const img = new Image();
  img.crossOrigin = 'anonymous';
  img.onload = () => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const SAMPLE_SIZE = 10;
    canvas.width = SAMPLE_SIZE;
    canvas.height = SAMPLE_SIZE;

    ctx.drawImage(img, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE);
    const { data } = ctx.getImageData(0, 0, SAMPLE_SIZE, SAMPLE_SIZE);

    let rTop = 0, gTop = 0, bTop = 0, rBottom = 0, gBottom = 0, bBottom = 0;
    const half = SAMPLE_SIZE / 2;
    let topCount = 0;
    let bottomCount = 0;

    for (let y = 0; y < SAMPLE_SIZE; y++) {
      for (let x = 0; x < SAMPLE_SIZE; x++) {
        const idx = (y * SAMPLE_SIZE + x) * 4;
        if (idx + 2 >= data.length) continue;
        const r = data[idx] ?? 0;
        const g = data[idx + 1] ?? 0;
        const b = data[idx + 2] ?? 0;

        if (y < half) {
          rTop += r; gTop += g; bTop += b; topCount++;
        } else {
          rBottom += r; gBottom += g; bBottom += b; bottomCount++;
        }
      }
    }

    const avgTop = [rTop / topCount, gTop / topCount, bTop / topCount].map(Math.round);
    const avgBottom = [rBottom / bottomCount, gBottom / bottomCount, bBottom / bottomCount].map(Math.round);

    background.type = 'gradient';
    background.value = `linear-gradient(180deg, rgba(${avgTop[0]}, ${avgTop[1]}, ${avgTop[2]}, 0.92), rgba(${avgBottom[0]}, ${avgBottom[1]}, ${avgBottom[2]}, 0.92))`;
  };
  img.onerror = () => {
    background.type = 'gradient';
    background.value = 'linear-gradient(to bottom, #3b82f6, #86efac)';
  };
  img.src = imageUrl;
};

const storyElements = ref<StoryElementType[]>([
  {
    id: 'el_init_text',
    type: 'text',
    content: 'Przesuwaj mnie!',
    x: 80, y: 100, width: 180, height: 40, rotation: 0, scale: 1,
    styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' }
  }
]);

const selectedElementId = ref<string | null>(null);

const selectedElement = computed(() => {
  return storyElements.value.find((el: StoryElementType) => el.id === selectedElementId.value);
});

// --- AUDIO PLAYER ---
const audioPlayer = new Audio();
audioPlayer.loop = true;
const isPlaying = ref(false);

const toggleBackgroundMusic = () => {
  if (audioPlayer.paused && audioPlayer.src) {
    audioPlayer.play();
    isPlaying.value = true;
  } else {
    audioPlayer.pause();
    isPlaying.value = false;
  }
};

const backgroundRef = ref<HTMLElement | null>(null);
const bgDimensions = reactive({ width: CANVAS_WIDTH, height: CANVAS_HEIGHT });

onMounted(() => {
  if (backgroundRef.value) {
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        bgDimensions.width = entry.contentRect.width;
        bgDimensions.height = entry.contentRect.height;
      }
    });
    observer.observe(backgroundRef.value);
  }

  // Load initial post if provided (from share button)
  if (props.initialPost) {
    const newId = `el_post_${Date.now()}`;
    storyElements.value.push({
      id: newId,
      type: 'post',
      content: props.initialPost.content,
      x: 50, y: 200,
      width: 280, height: 180,
      rotation: 0, scale: 1,
      styles: {},
      postData: props.initialPost
    });
    selectedElementId.value = newId;

    // Set gradient background based on post image if available
    if (props.initialPost.imageUrl) {
      setBackgroundGradientFromImage(props.initialPost.imageUrl);
    }
  }
  // Load initial reel if provided (from share button)
  else if (props.initialReel) {
    const newId = `el_reel_${Date.now()}`;
    storyElements.value.push({
      id: newId,
      type: 'reel',
      content: props.initialReel.videoSrc,
      x: 50, y: 150,
      width: 280, height: 400,
      rotation: 0, scale: 1,
      styles: {},
      reelData: props.initialReel
    });
    selectedElementId.value = newId;

    // Set gradient background based on reel poster if available
    if (props.initialReel.poster) {
      setBackgroundGradientFromImage(props.initialReel.poster);
    }
  }
  // Load initial image if provided
  else if (props.initialImage) {
    const imgUrl = props.initialImage;
    const newId = `el_img_${Date.now()}`;
    storyElements.value.push({
      id: newId,
      type: 'image',
      content: imgUrl,
      x: 60, y: 200,
      width: 240, height: 320,
      rotation: 0, scale: 1,
      cropX: 0, cropY: 0, cropZoom: 1,
      styles: {}
    });
    selectedElementId.value = newId;
    setBackgroundGradientFromImage(imgUrl);
  }
});

onUnmounted(() => {
  audioPlayer.pause();
});

// --- MODAL MUZYKI ---
const isMusicModalOpen = ref(false);
const toggleMusicModal = () => isMusicModalOpen.value = !isMusicModalOpen.value;

// --- MODAL LINK STICKER ---
const isLinkModalOpen = ref(false);
const toggleLinkModal = () => isLinkModalOpen.value = !isLinkModalOpen.value;

// --- MODAL POST SHARE ---
const isPostShareModalOpen = ref(false);
const togglePostShareModal = () => isPostShareModalOpen.value = !isPostShareModalOpen.value;

const addMusicPoster = (track: MusicTrack) => {
  const newId = `el_${Date.now()}`;
  storyElements.value.push({
    id: newId,
    type: 'image',
    content: track.coverUrlLarge ?? '',
    x: 50, y: 150,
    width: 220, height: 300,
    rotation: 0, scale: 1, cropX: 0, cropY: 0, cropZoom: 1,
    styles: {},
    musicTitle: track.title,
    musicArtist: track.artist,
    musicStyle: 'large'
  });
  selectedElementId.value = newId;

  if (track.previewUrl && audioPlayer.src !== track.previewUrl) {
    audioPlayer.src = track.previewUrl;
    audioPlayer.play().then(() => { isPlaying.value = true; });
  } else if (audioPlayer.paused) {
    audioPlayer.play().then(() => { isPlaying.value = true; });
  }
  isMusicModalOpen.value = false;
};

const updateMusicStyle = (style: 'large' | 'small' | 'text' | 'icon') => {
  if (!selectedElement.value || selectedElement.value.type !== 'image') return;
  const el = selectedElement.value;
  el.musicStyle = style;
  el.scale = 1;

  if (style === 'large') { el.width = 220; el.height = 300; }
  else if (style === 'small') { el.width = 260; el.height = 70; }
  else if (style === 'text') { el.width = 300; el.height = 100; }
  else if (style === 'icon') { el.width = 60; el.height = 60; }
};

// --- INTERAKCJE ---
const activeDragId = ref<string | null>(null);
const activeResizeId = ref<string | null>(null);
const activeRotateId = ref<string | null>(null);
const editingId = ref<string | null>(null);
const croppingId = ref<string | null>(null);
const dragStart = reactive({ x: 0, y: 0 });
const elementStart = reactive({ x: 0, y: 0, w: 0, h: 0, rotation: 0, cropX: 0, cropY: 0 });
const activeGuides = ref<Guide[]>([]);
const SNAP_THRESHOLD = 12;

const startDrag = (event: MouseEvent, element: StoryElementType) => {
  selectedElementId.value = element.id;
  if (editingId.value === element.id || activeResizeId.value || activeRotateId.value) return;

  if (croppingId.value === element.id && element.type === 'image') {
    activeDragId.value = 'CROP_MOVE';
    dragStart.x = event.clientX; dragStart.y = event.clientY;
    elementStart.cropX = element.cropX || 0; elementStart.cropY = element.cropY || 0;
  } else {
    if (croppingId.value) return;
    activeDragId.value = element.id;
    dragStart.x = event.clientX; dragStart.y = event.clientY;
    elementStart.x = element.x; elementStart.y = element.y;
    const target = event.currentTarget as HTMLElement;
    elementStart.w = target.offsetWidth; elementStart.h = target.offsetHeight;
    element.width = target.offsetWidth;
    element.height = target.offsetHeight;
  }
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', stopInteraction);
};

const startResize = (event: MouseEvent, element: StoryElementType) => {
  event.stopPropagation(); event.preventDefault();
  activeResizeId.value = element.id; selectedElementId.value = element.id;
  dragStart.x = event.clientX;
  elementStart.w = element.width || 200;
  elementStart.h = element.height || 200;
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', stopInteraction);
};

const handleStartDrag = (event: MouseEvent, element: StoryElementType) => startDrag(event, element);
const handleStartResize = (event: MouseEvent, element: StoryElementType) => startResize(event, element);
const rotateElement90 = () => { if (selectedElement.value) selectedElement.value.rotation = (selectedElement.value.rotation + 90) % 360; };

const onMouseMove = (event: MouseEvent) => {
  if (activeDragId.value === 'CROP_MOVE' && croppingId.value) {
    const element = storyElements.value.find((el: StoryElementType) => el.id === croppingId.value);
    if (element && element.type === 'image') {
      element.cropX = elementStart.cropX + (event.clientX - dragStart.x);
      element.cropY = elementStart.cropY + (event.clientY - dragStart.y);
    }
    return;
  }

  if (activeDragId.value && activeDragId.value !== 'CROP_MOVE') {
    const element = storyElements.value.find((el: StoryElementType) => el.id === activeDragId.value);
    if (element) {
      let newX = elementStart.x + (event.clientX - dragStart.x);
      let newY = elementStart.y + (event.clientY - dragStart.y);

      const elementWithDimensions = {
        ...element,
        width: elementStart.w || element.width || 100,
        height: elementStart.h || element.height || 100,
      };
      const { snappedX, snappedY, guides } = calculateSnaps(
        elementWithDimensions, newX, newY,
        storyElements.value.map(el => ({ ...el, width: el.width || 100, height: el.height || 100 })),
        { threshold: SNAP_THRESHOLD, canvasWidth: CANVAS_WIDTH, canvasHeight: CANVAS_HEIGHT }
      );
      activeGuides.value = guides;
      newX = snappedX;
      newY = snappedY;

      if (element.type === 'text' || (element.type === 'image' && element.musicArtist)) {
        if (newX < 0) newX = 0;
        else if (newX + elementStart.w > bgDimensions.width) newX = bgDimensions.width - elementStart.w;
        if (newY < 0) newY = 0;
        else if (newY + elementStart.h > bgDimensions.height) newY = bgDimensions.height - elementStart.h;
      }
      element.x = newX; element.y = newY;
    }
  }

  if (activeResizeId.value) {
    const element = storyElements.value.find((el: StoryElementType) => el.id === activeResizeId.value);
    if (element) {
      const newSize = Math.max(50, elementStart.w + (event.clientX - dragStart.x));
      element.width = newSize;
      if (element.height) element.height = newSize;
      element.scale = 1;
    }
  }

  if (activeRotateId.value) {
    const element = storyElements.value.find((el: StoryElementType) => el.id === activeRotateId.value);
    if (element) {
      const angleRad = Math.atan2(event.clientY - dragStart.y, event.clientX - dragStart.x);
      element.rotation = (angleRad * 180 / Math.PI) + 90;
    }
  }
};

const stopInteraction = () => {
  activeDragId.value = null;
  activeResizeId.value = null;
  activeRotateId.value = null;
  activeGuides.value = [];
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', stopInteraction);
};

const enableEdit = (id: string) => { editingId.value = id; activeDragId.value = null; };
const disableEdit = () => { editingId.value = null; };
const onBackgroundClick = () => { disableEdit(); croppingId.value = null; selectedElementId.value = null; };
const toggleCrop = (id: string) => {
  if (croppingId.value === id) croppingId.value = null;
  else { croppingId.value = id; editingId.value = null; selectedElementId.value = id; }
};

const addTextElement = () => {
  const newId = `el_${Date.now()}`;
  storyElements.value.push({
    id: newId, type: 'text', content: 'Nowy Tekst',
    x: 100, y: 300, width: 150, height: 40, rotation: 0, scale: 1,
    styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' }
  });
  selectedElementId.value = newId;
};

const removeElement = (id: string) => {
  storyElements.value = storyElements.value.filter((el: StoryElementType) => el.id !== id);
  if (selectedElementId.value === id) selectedElementId.value = null;
};

const updateElementContent = (id: string, value: string) => {
  const target = storyElements.value.find((el: StoryElementType) => el.id === id);
  if (target) target.content = value;
};

const removeMusicAndOpenModal = () => {
  if (selectedElementId.value) {
    removeElement(selectedElementId.value);
    isMusicModalOpen.value = true;
  }
};

// --- DODAWANIE LINK STICKER ---
const addLinkSticker = (data: { url: string; title: string; style: 'default' | 'minimal' | 'button' | 'text' }) => {
  const newId = `el_link_${Date.now()}`;
  const linkStyle = data.style === 'text' ? 'default' : data.style;
  storyElements.value.push({
    id: newId,
    type: 'link',
    content: data.title || data.url,
    x: 100, y: 400,
    width: 200, height: 60,
    rotation: 0, scale: 1,
    styles: {},
    linkUrl: data.url,
    linkTitle: data.title,
    linkStyle: linkStyle
  });
  selectedElementId.value = newId;
  isLinkModalOpen.value = false;
};

// --- UDOSTĘPNIANIE POSTA NA STORY ---
const addPostToStory = (postData: PostData) => {
  const newId = `el_post_${Date.now()}`;
  storyElements.value.push({
    id: newId,
    type: 'post',
    content: postData.content,
    x: 50, y: 200,
    width: 280, height: 200,
    rotation: 0, scale: 1,
    styles: {},
    postData: postData
  });
  selectedElementId.value = newId;
  isPostShareModalOpen.value = false;
};

const goBack = () => {
  emit('back');
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans overflow-hidden select-none relative">
    <StorySidebar
      :is-music-modal-open="isMusicModalOpen"
      :current-alt-text="selectedElement?.type === 'image' ? selectedElement?.altText : ''"
      :is-image-selected="selectedElement?.type === 'image'"
      @add-text="addTextElement"
      @toggle-music="toggleMusicModal"
      @add-link="toggleLinkModal"
      @share-post="togglePostShareModal"
      @back="goBack"
    />

    <main class="flex-1 flex flex-col items-center justify-center p-6 bg-[#F0F2F5] overflow-hidden relative">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 w-full h-full max-w-[1000px] flex flex-col relative">
        <div class="flex justify-between items-center mb-2 px-1">
          <span class="text-sm font-semibold text-gray-700">Podgląd</span>
          <div v-if="audioPlayer.src">
            <button @click="toggleBackgroundMusic" class="p-2 rounded-full hover:bg-gray-100 transition text-gray-800" title="Wycisz podgląd">
              <VolumeHigh v-if="isPlaying" :size="20" class="text-blue-600"/>
              <VolumeOff v-else :size="20" class="text-gray-400"/>
            </button>
          </div>
        </div>

        <div class="flex-1 bg-[#18191A] rounded-lg flex items-center justify-center overflow-hidden relative shadow-inner border border-gray-800" @mousedown.self="onBackgroundClick">
          <div class="relative aspect-9/16 h-[calc(100%-68px)] bg-black shadow-2xl rounded-md border border-gray-700" style="overflow: visible;">
            <div class="absolute inset-0 w-full h-full pointer-events-none rounded-md overflow-hidden" :style="{ background: background.value }" ref="backgroundRef"></div>

            <!-- GUIDE LINES -->
            <template v-for="(guide, idx) in activeGuides" :key="`guide-${idx}`">
              <div v-if="guide.type === 'vertical'" class="absolute top-0 bottom-0 pointer-events-none z-40 bg-blue-500 opacity-60" :style="{ left: guide.pos + 'px', width: '1px', transform: 'translateX(-50%)' }"></div>
              <div v-else class="absolute left-0 right-0 pointer-events-none z-40 bg-blue-500 opacity-60" :style="{ top: guide.pos + 'px', height: '1px', transform: 'translateY(-50%)' }"></div>
            </template>

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
              :on-start-drag="handleStartDrag"
              :on-start-resize="handleStartResize"
              :on-toggle-crop="toggleCrop"
              :on-enable-edit="enableEdit"
              :on-disable-edit="disableEdit"
              :on-remove="removeElement"
              @update-content="updateElementContent"
            />
            <div class="pointer-events-none absolute inset-0 z-100 rounded-md shadow-[0_0_0_9999px_rgba(24,25,26,0.75)]"></div>
          </div>

          <ImageToolbar
            v-if="selectedElement && selectedElement.type === 'image' && !selectedElement.musicTitle && croppingId !== selectedElement.id"
            v-model:scale="selectedElement.scale"
            @rotate="rotateElement90"
          />
        </div>

        <MusicToolbar
          v-if="selectedElement && selectedElement.type === 'image' && selectedElement.musicTitle"
          :current-style="selectedElement.musicStyle || 'large'"
          :track-title="selectedElement.musicTitle"
          :track-artist="selectedElement.musicArtist"
          :cover-url="selectedElement.content"
          @update:style="updateMusicStyle"
          @remove="removeMusicAndOpenModal()"
        />

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

        <PostShareModal
          :is-open="isPostShareModalOpen"
          @close="isPostShareModalOpen = false"
          @select-post="addPostToStory"
        />
      </div>
    </main>
  </div>
</template>

<style scoped>
.select-none { user-select: none; }
@keyframes spin-record {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin-record {
  animation: spin-record 8s linear infinite;
}
</style>
