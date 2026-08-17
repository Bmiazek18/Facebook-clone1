<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useLiveStore } from '@/stores/live'

// --- IKONY ---
import WebcamIcon from 'vue-material-design-icons/Webcam.vue'
import KeyVariantIcon from 'vue-material-design-icons/KeyVariant.vue'
import CameraIcon from 'vue-material-design-icons/Camera.vue'
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue'
import MonitorShareIcon from 'vue-material-design-icons/MonitorShare.vue'

import HeartIcon from 'vue-material-design-icons/Heart.vue'

import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue'
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue'
import RefreshIcon from 'vue-material-design-icons/Refresh.vue'
import StopCircleOutlineIcon from 'vue-material-design-icons/StopCircleOutline.vue'
import CustomDropdown from '@/components/common/CustomDropdown.vue'
import VideoPreview from '@/components/live/VideoPreview.vue' // Import the new component

const { t } = useI18n()
const liveStore = useLiveStore()
const {
  activeStream,
  selectedCameraId,
  selectedMicId,
  cameraOptions,
  micOptions,
  isScreenSharing,
} = storeToRefs(liveStore)

// --- TYPY ---
type VideoSource = 'webcam' | 'software'

// --- STAN ---
const selectedSource = ref<VideoSource>('webcam')
const title = ref('')
const description = ref('')
const shareToStory = ref(true)

const formattedCameraOptions = computed(() => {
  if (cameraOptions.value.length === 0) {
    return [{ id: '', title: 'Szukanie kamer...', icon: CameraIcon as any }]
  }
  return cameraOptions.value.map((cam) => ({
    id: cam.deviceId,
    title: cam.label,
    icon: CameraIcon as any,
  }))
})

const formattedMicOptions = computed(() => {
  if (micOptions.value.length === 0) {
    return [{ id: '', title: 'Szukanie mikrofonów...', icon: MicrophoneIcon as any }]
  }
  return micOptions.value.map((mic) => ({
    id: mic.deviceId,
    title: mic.label,
    icon: MicrophoneIcon as any,
  }))
})

const onCameraChange = async () => {
  if (!selectedCameraId.value) return
  await liveStore.startStream({ deviceId: { exact: selectedCameraId.value } }, false)
}

const handleStartScreenShare = async () => {
  const stream = await liveStore.startScreenShare()
  if (stream) {
    const track = stream.getVideoTracks()[0]
    if (track) {
      track.onended = () => {
        liveStore.stopStream()
        liveStore.startStream(true, true) // Powrót do kamery
      }
    }
  } else {
    liveStore.startStream(true, true) // Fallback on cancel
  }
}

const getMediaDevicesList = () => {
  liveStore.getMediaDevicesList()
}

const enableCameraAccess = async () => {
  await liveStore.startStream(true, true)
}

onMounted(() => {
  if (!activeStream.value) {
    liveStore.startStream(true, true)
  }
  liveStore.getMediaDevicesList()
  navigator.mediaDevices.addEventListener('devicechange', liveStore.getMediaDevicesList)
})

onUnmounted(() => {
  // Do not stop the stream here because of keep-alive
  // The stream will be stopped when the parent layout is unmounted or manually
  navigator.mediaDevices.removeEventListener('devicechange', liveStore.getMediaDevicesList)
})
</script>

<template>
  <div
    class="h-full overflow-y-auto p-8 flex justify-center gap-6 custom-scrollbar relative bg-theme-bg"
  >
    <div class="flex-1 flex flex-col gap-5 max-w-[720px] min-w-[500px]">
      <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border p-5">
        <h2 class="font-bold text-[17px] mb-4 flex items-center gap-2 text-theme-text">
          {{ t('createLive.selectVideoSource') }}
        </h2>
        <div class="grid grid-cols-2 gap-4">
          <div
            @click="selectedSource = 'webcam'"
            class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36"
            :class="
              selectedSource === 'webcam'
                ? 'border-[#1877F2] bg-[#E7F3FF]'
                : 'border-theme-border hover:bg-theme-hover'
            "
          >
            <div
              class="w-14 h-14 rounded-full flex items-center justify-center shadow-sm"
              :class="
                selectedSource === 'webcam'
                  ? 'bg-[#1877F2] text-white'
                  : 'text-theme-text-secondary bg-theme-bg-tertiary'
              "
            >
              <WebcamIcon :size="32" />
            </div>
            <span
              class="font-semibold text-[15px]"
              :class="selectedSource === 'webcam' ? 'text-[#1877F2]' : 'text-theme-text'"
              >{{ t('createLive.webcam') }}</span
            >
          </div>
          <div
            @click="selectedSource = 'software'"
            class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36"
            :class="
              selectedSource === 'software'
                ? 'border-[#1877F2] bg-[#E7F3FF]'
                : 'border-theme-border hover:bg-theme-hover'
            "
          >
            <div
              class="w-14 h-14 rounded-full flex items-center justify-center"
              :class="
                selectedSource === 'software'
                  ? 'bg-[#1877F2] text-white'
                  : 'text-theme-text-secondary bg-theme-bg-tertiary'
              "
            >
              <KeyVariantIcon :size="28" />
            </div>
            <span
              class="font-semibold text-[15px] text-center"
              :class="selectedSource === 'software' ? 'text-[#1877F2]' : 'text-theme-text'"
              >{{ t('createLive.streamingSoftware') }}</span
            >
          </div>
        </div>
      </div>

      <div
        v-if="selectedSource === 'webcam'"
        class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border p-5 space-y-5"
      >
        <div class="flex justify-between items-start">
          <div>
            <h2 class="font-bold text-[17px] text-theme-text">
              {{ t('createLive.cameraControls') }}
            </h2>
            <p class="text-[13px] text-theme-text-secondary mt-1">
              {{ t('createLive.cameraControlsDescription') }}
            </p>
          </div>
          <button
            @click="getMediaDevicesList"
            class="p-2 hover:bg-theme-hover rounded-full text-theme-text-secondary"
            title="Odśwież listę"
          >
            <RefreshIcon :size="20" />
          </button>
        </div>

        <div class="space-y-4">
          <CustomDropdown
            v-model="selectedCameraId"
            :options="formattedCameraOptions"
            @update:modelValue="onCameraChange"
            :disabled="isScreenSharing"
          />
          <CustomDropdown
            v-model="selectedMicId"
            :options="formattedMicOptions"
            :disabled="isScreenSharing"
          />
        </div>

        <div v-if="!isScreenSharing">
          <button
            @click="handleStartScreenShare"
            class="w-full flex items-center justify-center gap-2 py-2.5 bg-theme-bg-tertiary hover:bg-theme-hover rounded-lg font-semibold text-[15px] text-theme-text transition-colors"
          >
            <MonitorShareIcon :size="22" />
            {{ t('createLive.startScreenShare') }}
          </button>
        </div>
        <div v-else>
          <button
            @click="enableCameraAccess"
            class="w-full flex items-center justify-center gap-2 py-2.5 bg-[#FCE8E8] hover:bg-[#FAD1D1] text-[#D32F2F] rounded-lg font-semibold text-[15px] transition-colors border border-[#D32F2F]"
          >
            <StopCircleOutlineIcon :size="22" />
            Zatrzymaj udostępnianie ekranu
          </button>
        </div>
      </div>

      <VideoPreview />
    </div>

    <div class="w-[360px] flex flex-col gap-4 shrink-0">
      <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border p-4">
        <h2 class="font-bold text-[17px] mb-4 text-theme-text">
          {{ t('createLive.addPostDetails') }}
        </h2>
        <div class="flex items-start gap-3 mb-5 p-1">
          <div class="flex items-center h-6">
            <input
              id="story"
              v-model="shareToStory"
              type="checkbox"
              class="w-5 h-5 text-[#1877F2] border-theme-border rounded focus:ring-[#1877F2] cursor-pointer"
            />
          </div>
          <div class="ml-1">
            <label for="story" class="font-semibold text-[15px] text-theme-text cursor-pointer">{{
              t('createLive.shareToStory')
            }}</label>
            <p class="text-theme-text-secondary text-[13px] mt-0.5 leading-snug">
              {{ t('createLive.shareToStoryDescription') }}
            </p>
          </div>
        </div>
        <div class="space-y-4">
          <input
            v-model="title"
            type="text"
            :placeholder="t('createLive.titleOptional')"
            class="block w-full border border-theme-border rounded-md px-4 py-3.5 text-[15px] placeholder-theme-text-secondary focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-theme-border-hover transition-colors"
          />
          <div class="relative">
            <textarea
              v-model="description"
              rows="4"
              :placeholder="t('createLive.description')"
              class="block w-full border border-theme-border rounded-md px-4 py-3.5 text-[15px] placeholder-theme-text-secondary focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-theme-border-hover transition-colors resize-none pb-12"
            ></textarea>
            <div class="absolute bottom-3 right-3 flex gap-3 items-center">
              <AccountMultipleIcon class="text-[#42B72A] cursor-pointer" :size="24" />
              <MapMarkerIcon class="text-[#F02849] cursor-pointer" :size="24" />
              <EmoticonIcon class="text-[#F7B928] cursor-pointer" :size="24" />
              <div
                class="w-6 h-6 bg-[#E11D48] rounded-full flex items-center justify-center text-white cursor-pointer"
              >
                <HeartIcon :size="14" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
