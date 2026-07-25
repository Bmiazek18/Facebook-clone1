import { defineStore } from 'pinia'
import { ref } from 'vue'

interface DeviceOption {
  label: string
  deviceId: string
}

export const useLiveStore = defineStore('live', () => {
  const activeStream = ref<MediaStream | null>(null)
  const selectedCameraId = ref<string>('')
  const selectedMicId = ref<string>('')
  const cameraOptions = ref<DeviceOption[]>([])
  const micOptions = ref<DeviceOption[]>([])
  const permissionError = ref<string>('')
  const isScreenSharing = ref(false)

  const stopStream = () => {
    if (activeStream.value) {
      activeStream.value.getTracks().forEach((track) => track.stop())
      activeStream.value = null
    }
    isScreenSharing.value = false
  }

  const getMediaDevicesList = async () => {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices()
      let camCount = 1
      let micCount = 1

      cameraOptions.value = devices
        .filter((d) => d.kind === 'videoinput')
        .map((d) => ({ label: d.label || `Kamera ${camCount++}`, deviceId: d.deviceId }))

      micOptions.value = devices
        .filter((d) => d.kind === 'audioinput')
        .map((d) => ({ label: d.label || `Mikrofon ${micCount++}`, deviceId: d.deviceId }))

      if (cameraOptions.value.length > 0 && !selectedCameraId.value) {
        selectedCameraId.value = cameraOptions.value[0].deviceId
      }
      if (micOptions.value.length > 0 && !selectedMicId.value) {
        selectedMicId.value = micOptions.value[0].deviceId
      }
    } catch (e) {
      console.error('Enumerate error', e)
    }
  }

  const startStream = async (
    videoConstraints: MediaStreamConstraints['video'],
    audioConstraints: MediaStreamConstraints['audio'],
  ) => {
    permissionError.value = ''
    stopStream()
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: videoConstraints,
        audio: audioConstraints,
      })
      activeStream.value = stream
      // Mute audio for local preview to prevent echo
      if (audioConstraints) {
        stream.getAudioTracks().forEach((track) => (track.enabled = false))
      }
      await getMediaDevicesList()
      return stream
    } catch (err: any) {
      console.error('Access Error:', err)
      if (err.name === 'NotAllowedError' || err.message.includes('denied')) {
        permissionError.value =
          'Dostęp zablokowany przez przeglądarkę. Sprawdź ikonę kamery w pasku adresu lub ustawienia Safari.'
      } else {
        permissionError.value = `Błąd: ${err.message || 'Nie można połączyć kamery'}`
      }
      return null
    }
  }

  const startScreenShare = async () => {
    try {
      stopStream()
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: { cursor: 'always' },
        audio: false,
      })
      activeStream.value = stream
      isScreenSharing.value = true
      return stream
    } catch (err) {
      console.error('Screen share error', err)
      return null
    }
  }

  return {
    activeStream,
    selectedCameraId,
    selectedMicId,
    cameraOptions,
    micOptions,
    permissionError,
    isScreenSharing,
    stopStream,
    getMediaDevicesList,
    startStream,
    startScreenShare,
  }
})
