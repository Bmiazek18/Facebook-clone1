<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useI18n } from 'vue-i18n';

// --- IKONY ---
import WebcamIcon from 'vue-material-design-icons/Webcam.vue';
import KeyVariantIcon from 'vue-material-design-icons/KeyVariant.vue';
import CameraIcon from 'vue-material-design-icons/Camera.vue';
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue';
import MonitorShareIcon from 'vue-material-design-icons/MonitorShare.vue';
import CameraOffIcon from 'vue-material-design-icons/CameraOff.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import HeartIcon from 'vue-material-design-icons/Heart.vue';
import ArrowExpandAllIcon from 'vue-material-design-icons/ArrowExpandAll.vue';
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import RefreshIcon from 'vue-material-design-icons/Refresh.vue';
import StopCircleOutlineIcon from 'vue-material-design-icons/StopCircleOutline.vue';

const { t } = useI18n();

// --- TYPY ---
type VideoSource = 'webcam' | 'software';

interface DeviceOption {
    label: string;
    deviceId: string;
}

// --- STAN ---
const selectedSource = ref<VideoSource>('webcam');
const title = ref('');
const description = ref('');
const shareToStory = ref(true);

// --- MEDIA I URZĄDZENIA ---
const videoElement = ref<HTMLVideoElement | null>(null);
const activeStream = ref<MediaStream | null>(null);
const cameraOptions = ref<DeviceOption[]>([]);
const micOptions = ref<DeviceOption[]>([]);
const selectedCameraId = ref<string>('');
const selectedMicId = ref<string>('');
const permissionError = ref<string>('');
const isScreenSharing = ref(false);

// Logika lustrzanego odbicia
const isMirrored = computed(() => {
    if (isScreenSharing.value) return false;
    const cam = cameraOptions.value.find(c => c.deviceId === selectedCameraId.value);
    // Domyślnie lustro, chyba że nazwa sugeruje tylną kamerę lub widok blatu
    if (!cam) return true;
    const label = cam.label.toLowerCase();
    return !(label.includes('desk') || label.includes('blat') || label.includes('tylna') || label.includes('back'));
});

const stopStream = () => {
    if (activeStream.value) {
        activeStream.value.getTracks().forEach(track => track.stop());
        activeStream.value = null;
    }
    isScreenSharing.value = false;
};

// 1. POBIERANIE LISTY (BEZ ZGODY = GENERYCZNE NAZWY)
const getMediaDevicesList = async () => {
    try {
        const devices = await navigator.mediaDevices.enumerateDevices();

        // Filtrujemy i tworzymy listę. Jeśli label jest pusty (Safari przed zgodą), nadajemy nazwę "Kamera X"
        let camCount = 1;
        let micCount = 1;

        cameraOptions.value = devices
            .filter(d => d.kind === 'videoinput')
            .map(d => ({
                label: d.label || `Kamera ${camCount++}`,
                deviceId: d.deviceId
            }));

        micOptions.value = devices
            .filter(d => d.kind === 'audioinput')
            .map(d => ({
                label: d.label || `Mikrofon ${micCount++}`,
                deviceId: d.deviceId
            }));

        // Ustaw domyślne ID jeśli nie wybrano
        if (cameraOptions.value.length > 0 && !selectedCameraId.value) {
            selectedCameraId.value = cameraOptions.value[0].deviceId;
        }
        if (micOptions.value.length > 0 && !selectedMicId.value) {
            selectedMicId.value = micOptions.value[0].deviceId;
        }

    } catch (e) { console.error("Enumerate error", e); }
};

// 2. RĘCZNE WYMUSZENIE ZGODY (KLIKNIĘCIE PRZYCISKU)
const enableCameraAccess = async () => {
    permissionError.value = '';

    try {
        // Prosimy o najprostszy możliwy strumień
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

        // Zgoda udzielona!
        // 1. Przypisujemy strumień
        activeStream.value = stream;

        // 2. Safari Fix: Wyłączamy audio na podglądzie (żeby nie było echa)
        stream.getAudioTracks().forEach(track => track.enabled = false);

        await nextTick();
        if (videoElement.value) {
            videoElement.value.srcObject = stream;
            // Wymagane w Safari
            await videoElement.value.play();
        }

        // 3. Odświeżamy listę urządzeń - teraz pojawią się prawdziwe nazwy (np. "Desk View")
        await getMediaDevicesList();

    } catch (err: any) {
        console.error("Access Error:", err);
        // Jeśli błąd NotAllowedError - użytkownik musi zmienić ustawienia przeglądarki
        if (err.name === 'NotAllowedError' || err.message.includes('denied')) {
            permissionError.value = "Dostęp zablokowany przez przeglądarkę. Sprawdź ikonę kamery w pasku adresu lub ustawienia Safari.";
        } else {
            permissionError.value = `Błąd: ${err.message || "Nie można połączyć kamery"}`;
        }
    }
};

// 3. ZMIANA KAMERY Z LISTY
const onCameraChange = async () => {
    if (!selectedCameraId.value) return;
    stopStream();

    const constraints = {
        audio: false, // Podgląd bez audio
        video: { deviceId: { exact: selectedCameraId.value } }
    };

    try {
        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        activeStream.value = stream;

        await nextTick();
        if (videoElement.value) {
            videoElement.value.srcObject = stream;
            await videoElement.value.play();
        }
    } catch (error) {
        console.error("Change camera error", error);
        // Fallback jeśli exact ID zawiedzie
        enableCameraAccess();
    }
};

// 4. EKRAN
const startScreenShare = async () => {
    try {
        stopStream();
        // @ts-ignore
        const stream = await navigator.mediaDevices.getDisplayMedia({ video: { cursor: "always" }, audio: false });
        activeStream.value = stream;
        isScreenSharing.value = true;

        await nextTick();
        if (videoElement.value) {
            videoElement.value.srcObject = stream;
            await videoElement.value.play();
        }

        stream.getVideoTracks()[0].onended = () => {
            stopStream();
            enableCameraAccess(); // Powrót do kamery
        };
    } catch (err) {
        enableCameraAccess(); // Powrót do kamery w razie anulowania
    }
};

onMounted(() => {
   enableCameraAccess()
    getMediaDevicesList();
    navigator.mediaDevices.addEventListener('devicechange', getMediaDevicesList);
});

onUnmounted(() => {
    stopStream();
    navigator.mediaDevices.removeEventListener('devicechange', getMediaDevicesList);
});
</script>

<template>
    <div class="h-full overflow-y-auto p-8 flex justify-center gap-6 custom-scrollbar relative">

        <div class="flex-1 flex flex-col gap-5 max-w-[720px] min-w-[500px]">

            <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5">
                <h2 class="font-bold text-[17px] mb-4 flex items-center gap-2">
                    {{ t('createLive.selectVideoSource') }}
                </h2>
                <div class="grid grid-cols-2 gap-4">
                    <div @click="selectedSource = 'webcam'" class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36" :class="selectedSource === 'webcam' ? 'border-[#1877F2] bg-[#E7F3FF]' : 'border-[#CED0D4] hover:bg-[#F2F2F2]'">
                        <div class="w-14 h-14 rounded-full flex items-center justify-center shadow-sm" :class="selectedSource === 'webcam' ? 'bg-[#1877F2] text-white' : 'text-[#65676B] bg-[#E4E6EB]'">
                            <WebcamIcon :size="32" />
                        </div>
                        <span class="font-semibold text-[15px]" :class="selectedSource === 'webcam' ? 'text-[#1877F2]' : 'text-[#050505]'">{{ t('createLive.webcam') }}</span>
                    </div>
                    <div @click="selectedSource = 'software'" class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36" :class="selectedSource === 'software' ? 'border-[#1877F2] bg-[#E7F3FF]' : 'border-[#CED0D4] hover:bg-[#F2F2F2]'">
                         <div class="w-14 h-14 rounded-full flex items-center justify-center" :class="selectedSource === 'software' ? 'bg-[#1877F2] text-white' : 'text-[#65676B] bg-[#E4E6EB]'">
                            <KeyVariantIcon :size="28" />
                        </div>
                        <span class="font-semibold text-[15px] text-center" :class="selectedSource === 'software' ? 'text-[#1877F2]' : 'text-[#050505]'">{{ t('createLive.streamingSoftware') }}</span>
                    </div>
                </div>
            </div>

            <div v-if="selectedSource === 'webcam'" class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5 space-y-5">
                <div class="flex justify-between items-start">
                    <div>
                        <h2 class="font-bold text-[17px]">{{ t('createLive.cameraControls') }}</h2>
                        <p class="text-[13px] text-[#65676B] mt-1">{{ t('createLive.cameraControlsDescription') }}</p>
                    </div>
                    <button @click="getMediaDevicesList" class="p-2 hover:bg-[#F0F2F5] rounded-full text-[#65676B]" title="Odśwież listę">
                        <RefreshIcon :size="20"/>
                    </button>
                </div>

                <div class="space-y-4">
                    <div class="relative group">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#050505]">
                           <CameraIcon :size="22"/>
                        </div>
                        <select
                            v-model="selectedCameraId"
                            @change="onCameraChange"
                            :disabled="isScreenSharing"
                            class="block w-full pl-11 pr-10 py-3.5 text-[15px] border border-transparent bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#1877F2] font-semibold appearance-none cursor-pointer transition-colors disabled:opacity-50"
                        >
                            <option v-if="cameraOptions.length === 0" value="" disabled>Szukanie kamer...</option>
                            <option v-for="cam in cameraOptions" :key="cam.deviceId" :value="cam.deviceId">
                                {{ cam.label }}
                            </option>
                        </select>
                        <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-[#050505]">
                            <ChevronDownIcon :size="24"/>
                        </div>
                    </div>

                     <div class="relative group">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#050505]">
                           <MicrophoneIcon :size="22"/>
                        </div>
                        <select v-model="selectedMicId" class="block w-full pl-11 pr-10 py-3.5 text-[15px] border border-transparent bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#1877F2] font-semibold appearance-none cursor-pointer transition-colors">
                            <option v-if="micOptions.length === 0" value="" disabled>Szukanie mikrofonów...</option>
                            <option v-for="mic in micOptions" :key="mic.deviceId" :value="mic.deviceId">
                                {{ mic.label }}
                            </option>
                        </select>
                         <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-[#050505]">
                            <ChevronDownIcon :size="24"/>
                        </div>
                    </div>
                </div>

                <div v-if="!isScreenSharing">
                    <button @click="startScreenShare" class="w-full flex items-center justify-center gap-2 py-2.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg font-semibold text-[15px] text-[#050505] transition-colors">
                        <MonitorShareIcon :size="22" />
                        {{ t('createLive.startScreenShare') }}
                    </button>
                </div>
                <div v-else>
                     <button @click="enableCameraAccess" class="w-full flex items-center justify-center gap-2 py-2.5 bg-[#FCE8E8] hover:bg-[#FAD1D1] text-[#D32F2F] rounded-lg font-semibold text-[15px] transition-colors border border-[#D32F2F]">
                        <StopCircleOutlineIcon :size="22" />
                        Zatrzymaj udostępnianie ekranu
                    </button>
                </div>
            </div>

            <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5 pb-3">
                <h2 class="font-bold text-[17px] mb-3">{{ t('createLive.video') }}</h2>
                <div class="relative w-full aspect-video bg-black rounded-lg flex flex-col items-center justify-center text-white overflow-hidden">

                    <video
                        ref="videoElement"
                        autoplay
                        playsinline
                        muted
                        class="w-full h-full object-contain bg-black"
                        :class="{'transform -scale-x-100': isMirrored}"
                    ></video>

                    <div v-if="!activeStream" class="absolute inset-0 flex flex-col items-center justify-center bg-[#242526] z-10 p-6 text-center">
                        <CameraOffIcon :size="56" class="text-[#B0B3B8] mb-4" />
                        <p class="font-bold text-[17px] text-[#E4E6EB] mb-2">{{ permissionError ? "Błąd dostępu" : "Połącz źródło wideo" }}</p>

                        <p v-if="permissionError" class="text-[#F02849] text-sm mb-4 font-semibold max-w-xs">{{ permissionError }}</p>

                        <button @click="enableCameraAccess" class="px-6 py-2.5 bg-[#1877F2] hover:bg-[#166FE5] text-white rounded-md font-semibold transition-colors shadow-sm">
                            {{ permissionError ? "Spróbuj ponownie" : "Połącz kamerę" }}
                        </button>
                    </div>

                    <div v-if="activeStream" class="absolute top-3 left-3 bg-[#242526]/80 backdrop-blur-sm px-2.5 py-1.5 rounded-md text-[13px] font-semibold text-[#E4E6EB] flex items-center z-20">
                        <div class="w-2 h-2 rounded-full mr-2 animate-pulse" :class="isScreenSharing ? 'bg-[#F7B928]' : 'bg-[#31A24C]'"></div>
                        {{ isScreenSharing ? 'Udostępnianie ekranu' : 'Podgląd na żywo' }}
                    </div>
                </div>
                 <div class="flex justify-between items-center mt-3 pt-1">
                     <span class="text-[15px] font-semibold text-[#050505]">{{ t('createLive.expandVideoPreview') }}</span>
                      <div class="w-9 h-9 flex items-center justify-center hover:bg-[#F2F2F2] rounded-full cursor-pointer transition-colors">
                        <ArrowExpandAllIcon :size="22" class="text-[#65676B]" />
                      </div>
                 </div>
            </div>

        </div>

        <div class="w-[360px] flex flex-col gap-4 shrink-0">
             <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-4">
                <h2 class="font-bold text-[17px] mb-4 text-[#050505]">{{ t('createLive.addPostDetails') }}</h2>
                <div class="flex items-start gap-3 mb-5 p-1">
                     <div class="flex items-center h-6">
                        <input id="story" v-model="shareToStory" type="checkbox" class="w-5 h-5 text-[#1877F2] border-[#CED0D4] rounded focus:ring-[#1877F2] cursor-pointer">
                    </div>
                    <div class="ml-1">
                        <label for="story" class="font-semibold text-[15px] text-[#050505] cursor-pointer">{{ t('createLive.shareToStory') }}</label>
                        <p class="text-[#65676B] text-[13px] mt-0.5 leading-snug">{{ t('createLive.shareToStoryDescription') }}</p>
                    </div>
                </div>
                <div class="space-y-4">
                    <input v-model="title" type="text" :placeholder="t('createLive.titleOptional')" class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-[#8A8D91] transition-colors">
                    <div class="relative">
                        <textarea v-model="description" rows="4" :placeholder="t('createLive.description')" class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-[#8A8D91] transition-colors resize-none pb-12"></textarea>
                        <div class="absolute bottom-3 right-3 flex gap-3 items-center">
                             <AccountMultipleIcon class="text-[#42B72A] cursor-pointer" :size="24" />
                             <MapMarkerIcon class="text-[#F02849] cursor-pointer" :size="24" />
                             <EmoticonIcon class="text-[#F7B928] cursor-pointer" :size="24" />
                             <div class="w-6 h-6 bg-[#E11D48] rounded-full flex items-center justify-center text-white cursor-pointer"><HeartIcon :size="14" /></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
