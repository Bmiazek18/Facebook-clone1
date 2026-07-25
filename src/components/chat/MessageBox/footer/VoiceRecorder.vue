<template>
  <div class="w-full flex items-center justify-between gap-2">
    <div v-if="isRecording || isPaused" class="shrink-0">
      <button
        @click="cancelVoiceRecording"
        class="w-10 h-10 rounded-full flex items-center justify-center text-white transition hover:opacity-90 shadow-md"
        :style="{ backgroundColor: props.themeColor }"
      >
        <CloseIcon :size="20" />
      </button>
    </div>

    <div
      v-if="isRecording || isPaused"
      class="grow h-10 rounded-full flex items-center px-2 relative overflow-hidden shadow-md"
      :style="{ backgroundColor: props.themeColor }"
    >
      <div
        class="absolute inset-y-0 left-0 bg-black/20 pointer-events-none"
        :style="{ width: `${currentProgress}%` }"
      ></div>

      <div class="relative z-10 flex items-center justify-between w-full pl-1 pr-3">
        <button
          @click="togglePauseResume"
          class="w-8 h-8 rounded-full bg-white flex items-center justify-center shrink-0 hover:scale-105 transition-transform cursor-pointer text-gray-800"
        >
          <StopIcon v-if="isRecording" :size="20" />
          <PlayIcon v-else :size="20" class="ml-0.5" />
        </button>

        <span class="text-white text-sm font-semibold tabular-nums tracking-wide">
          {{ formatDuration(recordingDuration) }}
        </span>
      </div>
    </div>

    <div class="shrink-0">
      <button
        @click="finishAndSend"
        class="w-10 h-10 rounded-full flex items-center justify-center text-white transition hover:opacity-90 shadow-md"
        :style="{ backgroundColor: props.themeColor }"
      >
        <SendIcon :size="20" class="ml-1" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted, computed, onMounted } from 'vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue'
import StopIcon from 'vue-material-design-icons/Stop.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import PlayIcon from 'vue-material-design-icons/Play.vue'

const props = defineProps<{
  themeColor: string
}>()

const emit = defineEmits<{
  'audio-recorded': [payload: { audioUrl: string; duration: number }]
  'recording-start': []
  'recording-stop': []
}>()

// Stan
const isRecording = ref(false) // Czy mikrofon aktywnie zbiera dźwięk
const isPaused = ref(false) // Czy nagrywanie jest wstrzymane (ale plik otwarty)

const recordingDuration = ref(0)
const currentProgress = ref(0)

// Timery
const recordingTimer = ref<ReturnType<typeof setInterval> | null>(null)
const progressTimer = ref<ReturnType<typeof setInterval> | null>(null)

// Media Recorder
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<Blob[]>([])
const isRecordingInitialized = ref(false)
let mediaStream: MediaStream | null = null

const formatDuration = (totalSeconds: number): string => {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// --- LOGIKA STEROWANIA ---

// 1. Start nowego nagrania
const startNewRecording = async () => {
  if (isRecording.value) return

  try {
    if (!isRecordingInitialized.value) {
      mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
      // Używamy mimeType 'audio/webm' (wspierane przez Chrome/Firefox).
      // Safari może wymagać 'audio/mp4' w nowszych wersjach, ale webm jest bezpiecznym domysłem dla logicznych komponentów Vue.
      mediaRecorder.value = new MediaRecorder(mediaStream, { mimeType: 'audio/webm' })

      mediaRecorder.value.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunks.value.push(event.data)
        }
      }
      isRecordingInitialized.value = true
    }

    audioChunks.value = []

    // Start nagrywania
    mediaRecorder.value?.start()
    isRecording.value = true
    isPaused.value = false

    recordingDuration.value = 0
    currentProgress.value = 0

    emit('recording-start')
    startTimers()
  } catch (error) {
    console.error('Błąd dostępu do mikrofonu:', error)
    alert('Brak dostępu do mikrofonu.')
  }
}

// 2. Pauza / Wznowienie (Kliknięcie w środkowy przycisk)
const togglePauseResume = () => {
  if (!mediaRecorder.value) return

  if (isRecording.value) {
    // AKCJA: PAUZA
    mediaRecorder.value.pause() // Kluczowa zmiana: .pause() zamiast .stop()
    isRecording.value = false
    isPaused.value = true
    stopTimers()
  } else if (isPaused.value) {
    // AKCJA: WZNÓW (RÉSUMÉ)
    mediaRecorder.value.resume() // Kluczowa zmiana: .resume() działa tylko po .pause()
    isRecording.value = true
    isPaused.value = false
    startTimers()
  }
}

// 3. Zakończ i Wyślij
const finishAndSend = () => {
  if (!mediaRecorder.value) return

  // Definicja co zrobić po zatrzymaniu
  mediaRecorder.value.onstop = () => {
    stopTimers()
    if (audioChunks.value.length === 0) {
      fullReset()
      emit('recording-stop')
      return
    }

    const blob = new Blob(audioChunks.value, { type: 'audio/webm' })
    const audioUrl = URL.createObjectURL(blob)
    const durationSeconds = Math.floor(recordingDuration.value)

    emit('audio-recorded', { audioUrl, duration: durationSeconds })
    fullReset()
    emit('recording-stop')
  }

  // Niezależnie czy nagrywamy, czy jest pauza - wywołujemy .stop() by sfinalizować plik
  mediaRecorder.value.stop()
  isRecording.value = false
  isPaused.value = false
}

// 4. Anuluj
const cancelVoiceRecording = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
    // Nadpisujemy onstop, żeby nie wysłało się przypadkiem
    mediaRecorder.value.onstop = null
  }
  fullReset()
  emit('recording-stop')
}

// --- TIMERY I HLEPERY ---

const startTimers = () => {
  stopTimers() // Dobrej praktyki: czyścimy stare przed startem nowych

  // Timer sekundowy
  recordingTimer.value = setInterval(() => {
    recordingDuration.value++
  }, 1000)

  // Timer paska postępu (płynny)
  progressTimer.value = setInterval(() => {
    // Pasek wypełnia się przez 60 sekund
    const increment = 100 / (60 * 20)
    currentProgress.value += increment
    if (currentProgress.value >= 100) currentProgress.value = 0
  }, 50)
}

const stopTimers = () => {
  if (recordingTimer.value) clearInterval(recordingTimer.value)
  if (progressTimer.value) clearInterval(progressTimer.value)
  recordingTimer.value = null
  progressTimer.value = null
}

const fullReset = () => {
  stopTimers()
  isRecording.value = false
  isPaused.value = false
  recordingDuration.value = 0
  currentProgress.value = 0
  audioChunks.value = []
}

onUnmounted(() => {
  fullReset()
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
  }
})

onMounted(() => {
  startNewRecording()
})
</script>
