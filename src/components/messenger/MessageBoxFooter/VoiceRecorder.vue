<template>
    <div v-if="isRecording" class="flex items-center space-x-2 w-full">
        <button @click="cancelVoiceRecording" class="p-2 rounded-full bg-purple-600 text-white shrink-0 hover:bg-purple-700 transition">
            <CloseIcon :size="24" />
        </button>

        <div class="grow h-10 flex items-center bg-purple-600 rounded-full px-2 py-1 space-x-2 relative shadow-lg">
            <button @click="pauseRecording" class="w-8 h-8 rounded-full bg-white text-purple-600 flex items-center justify-center shrink-0">
            <StopIcon :size="20" class="text-purple-600" />
            </button>
            <div class="grow h-full flex items-center justify-center">
            <div class="w-full h-1 bg-purple-700 opacity-60 rounded-full relative">
                <div class="absolute inset-y-0 flex items-center space-x-1 left-2">
                <div class="w-1 h-2 bg-white rounded-full opacity-50 animate-wave delay-1"></div>
                <div class="w-1 h-3 bg-white rounded-full opacity-80 animate-wave delay-2"></div>
                <div class="w-1 h-4 bg-white rounded-full opacity-100 animate-wave delay-3"></div>
                </div>
            </div>
            </div>
            <span class="text-white text-sm font-semibold w-10 text-right shrink-0">{{ formatDuration(recordingDuration) }}</span>
        </div>

        <button disabled class="w-12 h-12 rounded-full bg-gray-300 text-gray-500 border-2 border-gray-400 flex items-center justify-center shrink-0">
            <SendIcon :size="24" />
        </button>
    </div>

    <div v-else-if="isPaused" class="flex items-center space-x-2 w-full">
        <button @click="cancelVoiceRecording" class="p-2 rounded-full bg-purple-600 text-white shrink-0 hover:bg-purple-700 transition">
            <CloseIcon :size="24" />
        </button>

        <div class="grow h-10 flex items-center bg-purple-600 rounded-full px-2 py-1 space-x-2 relative shadow-lg">
            <button @click="startVoiceRecording" class="w-8 h-8 rounded-full bg-white text-purple-600 flex items-center justify-center shrink-0">
            <PlayIcon :size="20" class="text-purple-600" />
            </button>
            <div class="grow flex items-center"></div>
            <span class="text-white text-sm font-semibold w-10 text-right shrink-0">{{ formatDuration(recordingDuration) }}</span>
        </div>

        <button @click="sendRecordedAudio" class="w-12 h-12 rounded-full bg-white text-blue-500 border-2 border-blue-500 flex items-center justify-center shrink-0 hover:bg-gray-50 transition">
            <SendIcon :size="24" />
        </button>
    </div>
    <div v-else class="flex items-center space-x-1">
        <MicrophoneIcon @click="startVoiceRecording" :size="24" class="text-blue-500 hover:text-blue-700 cursor-pointer" />
    </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue';
import StopIcon from 'vue-material-design-icons/Stop.vue';
import SendIcon from 'vue-material-design-icons/Send.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';

const emit = defineEmits<{
  'audio-recorded': [payload: { audioUrl: string, duration: number }];
}>();

const isRecording = ref(false);
const isPaused = ref(false);
const recordingDuration = ref(0);

// Media Recorder
const mediaRecorder = ref<MediaRecorder | null>(null);
const audioChunks = ref<Blob[]>([]);
const recordingTimer = ref<ReturnType<typeof setInterval> | null>(null);
const isRecordingInitialized = ref(false);
let mediaStream: MediaStream | null = null;
const recordedBlob = ref<Blob | null>(null);
const audioUrlPreview = ref<string | null>(null);

const formatDuration = (totalSeconds: number): string => {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

const resetRecordingState = () => {
  if (recordingTimer.value) clearInterval(recordingTimer.value);
  isRecording.value = false;
  isPaused.value = false;
  recordingDuration.value = 0;
  audioChunks.value = [];
  recordedBlob.value = null;
  audioUrlPreview.value = null;
};

const startVoiceRecording = async () => {
  if (isPaused.value) {
    mediaRecorder.value?.resume();
    isPaused.value = false;
    isRecording.value = true;
    recordingTimer.value = setInterval(() => {
      recordingDuration.value++;
    }, 1000);
    return;
  }

  if (isRecording.value) return;

  try {
    if (!isRecordingInitialized.value) {
      mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorder.value = new MediaRecorder(mediaStream, { mimeType: 'audio/webm' });

      mediaRecorder.value.ondataavailable = event => {
        if (event.data.size > 0) {
          audioChunks.value.push(event.data);
        }
      };
      isRecordingInitialized.value = true;
    }

    audioChunks.value = [];
    recordedBlob.value = null;
    if (audioUrlPreview.value) URL.revokeObjectURL(audioUrlPreview.value);
    audioUrlPreview.value = null;

    mediaRecorder.value?.start();
    isRecording.value = true;
    recordingDuration.value = 0;

    recordingTimer.value = setInterval(() => {
      recordingDuration.value++;
    }, 1000);
  } catch (error) {
    console.error('Błąd dostępu do mikrofonu:', error);
    alert('Nie można uzyskać dostępu do mikrofonu. Upewnij się, że zezwoliłeś na użycie mikrofonu.');
  }
};

const pauseRecording = () => {
  if (!isRecording.value || !mediaRecorder.value) return;

  mediaRecorder.value.stop();

  if (recordingTimer.value) {
    clearInterval(recordingTimer.value);
    recordingTimer.value = null;
  }

  isRecording.value = false;
  isPaused.value = true;

  mediaRecorder.value.onstop = () => {
    if (audioChunks.value.length === 0) {
      resetRecordingState();
      return;
    }

    const blob = new Blob(audioChunks.value, { type: 'audio/webm' });
    recordedBlob.value = blob;
    audioUrlPreview.value = URL.createObjectURL(blob);

    audioChunks.value = [];
  };
};

const sendRecordedAudio = () => {
  if (!recordedBlob.value || !audioUrlPreview.value) return;

  const durationSeconds = Math.floor(recordingDuration.value);
  emit('audio-recorded', { audioUrl: audioUrlPreview.value, duration: durationSeconds });

  isRecording.value = false;
  isPaused.value = false;
  recordingDuration.value = 0;
  audioChunks.value = [];
  recordedBlob.value = null;
  audioUrlPreview.value = null;

  if (recordingTimer.value) clearInterval(recordingTimer.value);
};

const cancelVoiceRecording = () => {
  if (mediaRecorder.value) {
    mediaRecorder.value.stop();
    mediaRecorder.value.onstop = () => {};
  }

  if (audioUrlPreview.value) {
    URL.revokeObjectURL(audioUrlPreview.value);
  }

  resetRecordingState();
};

onUnmounted(() => {
  if (audioUrlPreview.value) URL.revokeObjectURL(audioUrlPreview.value);
  resetRecordingState();
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
  }
});

</script>

<style scoped>
@keyframes wave {
  0%, 100% {
    transform: scaleY(0.5);
  }
  50% {
    transform: scaleY(1.5);
  }
}

.animate-wave {
  animation: wave 1.2s ease-in-out infinite;
}
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
</style>
