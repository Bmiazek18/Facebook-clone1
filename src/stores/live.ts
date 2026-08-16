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

  // Agora Live Streaming State
  const isLive = ref(false)
  const agoraClient = ref<any | null>(null)
  const localVideoTrack = ref<any | null>(null)
  const localAudioTrack = ref<any | null>(null)
  const channelName = 'global-live-stream'

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

  // Helper to generate numeric UID from user UUID string
  const getNumericUid = (uuidStr: string): number => {
    let hash = 0
    for (let i = 0; i < uuidStr.length; i++) {
      const character = uuidStr.charCodeAt(i)
      hash = (hash << 5) - hash + character
      hash = hash & hash
    }
    return Math.abs(hash)
  }

  // Go live action
  const goLive = async (userId: string) => {
    try {
      const AgoraRTC = (await import('agora-rtc-sdk-ng')).default
      agoraClient.value = AgoraRTC.createClient({ mode: 'live', codec: 'vp8' })
      await agoraClient.value.setClientRole('host')

      const numericUid = getNumericUid(userId)
      const tokenResponse = await fetch(`http://localhost:8080/api/chat/calls/token?channelName=${channelName}&uid=${numericUid}`)
      const tokenData = await tokenResponse.json()

      if (!tokenData.token) {
        throw new Error('Agora token acquisition failed')
      }

      await agoraClient.value.join(tokenData.appId, channelName, tokenData.token, numericUid)

      // Create Video Track
      if (isScreenSharing.value && activeStream.value) {
        const videoTrack = activeStream.value.getVideoTracks()[0]
        localVideoTrack.value = AgoraRTC.createCustomVideoTrack({ mediaStreamTrack: videoTrack })
      } else {
        localVideoTrack.value = await AgoraRTC.createCameraVideoTrack({
          deviceId: selectedCameraId.value || undefined,
          encoderConfig: '720p_1'
        })
      }

      // Create Audio Track
      localAudioTrack.value = await AgoraRTC.createMicrophoneAudioTrack({
        deviceId: selectedMicId.value || undefined
      })

      // Publish local tracks to Agora channel
      await agoraClient.value.publish([localAudioTrack.value, localVideoTrack.value])
      
      isLive.value = true
      console.log('Broadcaster is now LIVE on channel: ' + channelName)
    } catch (err) {
      console.error('Failed to start live stream:', err)
      await stopLive()
      throw err
    }
  }

  // Stop live action
  const stopLive = async () => {
    try {
      if (agoraClient.value) {
        const tracks = []
        if (localVideoTrack.value) tracks.push(localVideoTrack.value)
        if (localAudioTrack.value) tracks.push(localAudioTrack.value)
        
        if (tracks.length > 0) {
          await agoraClient.value.unpublish(tracks)
        }
        await agoraClient.value.leave()
      }
    } catch (err) {
      console.error('Error during stopping live broadcast:', err)
    } finally {
      if (localVideoTrack.value) {
        localVideoTrack.value.stop()
        localVideoTrack.value.close()
        localVideoTrack.value = null
      }
      if (localAudioTrack.value) {
        localAudioTrack.value.stop()
        localAudioTrack.value.close()
        localAudioTrack.value = null
      }
      agoraClient.value = null
      isLive.value = false
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
    isLive,
    goLive,
    stopLive,
    stopStream,
    getMediaDevicesList,
    startStream,
    startScreenShare,
  }
})

