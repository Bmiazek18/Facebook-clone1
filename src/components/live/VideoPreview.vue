<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, onActivated } from 'vue';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { useLiveStore } from '@/stores/live';

// --- IKONY ---
import CameraOffIcon from 'vue-material-design-icons/CameraOff.vue';
import ArrowExpandAllIcon from 'vue-material-design-icons/ArrowExpandAll.vue';

const { t } = useI18n();
const liveStore = useLiveStore();
const {
    activeStream,
    selectedCameraId,
    cameraOptions,
    permissionError,
    isScreenSharing,
} = storeToRefs(liveStore);

const videoElement = ref<HTMLVideoElement | null>(null);



const currentCameraTitle = computed(() => {
  const camera = cameraOptions.value.find(cam => cam.deviceId === selectedCameraId.value);
  return camera ? camera.label : t('createLive.noCameraSelected');
});

watch(activeStream, async (newStream) => {
    if (videoElement.value) {
        videoElement.value.srcObject = newStream;
        if (newStream) {
            await videoElement.value.play();
        }
    }
}, { immediate: true });

onActivated(() => {
    if (videoElement.value && activeStream.value) {
        videoElement.value.srcObject = activeStream.value;
    }
});

const enableCameraAccess = async () => {
    await liveStore.startStream(true, true);
};
</script>

<template>
    <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border p-5 pb-3">
        <h2 class="font-bold text-[17px] mb-3 text-theme-text">{{ t('createLive.video') }}</h2>
        <div class="relative w-full aspect-video bg-theme-bg rounded-lg flex flex-col items-center justify-center text-theme-text overflow-hidden">

            <video
                ref="videoElement"
                autoplay
                playsinline
                muted
                class="w-full h-full object-contain bg-theme-bg"

            ></video>
            <div v-if="!activeStream" class="absolute inset-0 flex flex-col items-center justify-center bg-theme-bg-tertiary z-10 p-6 text-center">
                <CameraOffIcon :size="56" class="text-theme-text-secondary mb-4" />
                <p class="font-bold text-[17px] text-theme-text mb-2">{{ permissionError ? "Błąd dostępu" : "Połącz źródło wideo" }}</p>
                <p v-if="permissionError" class="text-[#F02849] text-sm mb-4 font-semibold max-w-xs">{{ permissionError }}</p>
                <button @click="enableCameraAccess" class="px-6 py-2.5 bg-[#1877F2] hover:bg-[#166FE5] text-white rounded-md font-semibold transition-colors shadow-sm">
                    {{ permissionError ? "Spróbuj ponownie" : "Połącz kamerę" }}
                </button>
            </div>
            <div v-if="activeStream" class="absolute top-3 left-3 bg-theme-bg-tertiary/80 backdrop-blur-sm px-2.5 py-1.5 rounded-md text-[13px] font-semibold text-theme-text flex items-center z-20">
                <div class="w-2 h-2 rounded-full mr-2 animate-pulse" :class="isScreenSharing ? 'bg-[#F7B928]' : 'bg-[#31A24C]'"></div>
                {{ isScreenSharing ? 'Udostępnianie ekranu' : 'Podgląd na żywo' }}
            </div>
        </div>
        <div class="flex justify-between items-center mt-3 pt-1">
            <span class="text-[15px] font-semibold text-theme-text">{{ t('createLive.expandVideoPreview') }}</span>
            <div @click="liveStore.toggleVideoExpansion" class="w-9 h-9 flex items-center justify-center hover:bg-theme-hover rounded-full cursor-pointer transition-colors">
                <ArrowExpandAllIcon :size="22" class="text-theme-text-secondary" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.mirror-x {
  transform: scaleX(-1);
}
</style>
