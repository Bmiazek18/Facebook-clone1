<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from '#imports'
import AgoraRTC from 'agora-rtc-sdk-ng'
import type {
  IAgoraRTCClient,
  IMicrophoneAudioTrack,
  ILocalVideoTrack,
  IAgoraRTCRemoteUser,
} from 'agora-rtc-sdk-ng'
import * as THREE from 'three'
import { FilesetResolver, ImageSegmenter } from '@mediapipe/tasks-vision'
import CallSummary from '../components/CallSummary.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()

const connectionType = ref(route.query.type || 'video')
const boxId = ref(route.query.boxId || 'domyslny-kanal')
const conversationId = ref(route.query.conversationId || route.query.boxId || 'domyslny-kanal')

let heartbeatTimer: any = null

function getNumericUid(uuidStr: string): number {
  let hash = 0
  for (let i = 0; i < uuidStr.length; i++) {
    const character = uuidStr.charCodeAt(i)
    hash = (hash << 5) - hash + character
    hash = hash & hash
  }
  return Math.abs(hash)
}

let agoraClient: IAgoraRTCClient | null = null
let localAudioTrack: IMicrophoneAudioTrack | null = null
let localVideoTrack: ILocalVideoTrack | null = null
let screenVideoTrack: any = null
let callStartTime: number | null = null

const remoteVideoUser = ref<IAgoraRTCRemoteUser | null>(null)
const remoteVideoRef = ref<HTMLDivElement | null>(null)

const videoRef = ref<HTMLVideoElement | null>(null)
const canvasContainerRef = ref<HTMLDivElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const videoEnabled = ref(false)
const microphoneEnabled = ref(true)
const showCamera = ref(true)
const remoteUserSpeaking = ref(false)
const isSharingScreen = ref(false)
const statusMessage = ref('Inicjalizacja systemu...')

const currentFilter = ref<'none' | 'blur' | 'image'>('none')
const filterMenuOpen = ref(false)
const currentStep = ref<'call' | 'summary'>('call')

const avatarUrl = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800'

let imageSegmenter: ImageSegmenter | null = null
let animationFrameId: number | null = null
let lastVideoTime = -1

let scene: THREE.Scene
let camera: THREE.OrthographicCamera
let renderer: THREE.WebGLRenderer
let shaderMaterial: THREE.ShaderMaterial
let webcamTexture: THREE.VideoTexture
let maskTexture: THREE.Texture
let bgImageTexture: THREE.Texture | null = null

let maskCanvas: HTMLCanvasElement
let maskCtx: CanvasRenderingContext2D | null

const vertexShader = `
  varying vec2 vUv;
  void main() {
    vUv = uv;
    vUv.x = 1.0 - vUv.x;
    gl_Position = vec4(position, 1.0);
  }
`

const fragmentShader = `
  varying vec2 vUv;
  uniform sampler2D tWebcam;
  uniform sampler2D tMask;
  uniform sampler2D tBackground;
  uniform vec2 uResolution;
  uniform int uFilterType;

  vec4 blur(sampler2D tex, vec2 uv, vec2 res) {
    vec4 color = vec4(0.0);
    vec2 blurSize = 8.0 / res;
    for (int x = -2; x <= 2; x++) {
      for (int y = -2; y <= 2; y++) {
        color += texture2D(tex, uv + vec2(float(x), float(y)) * blurSize);
      }
    }
    return color / 25.0;
  }

  void main() {
    vec4 cameraColor = texture2D(tWebcam, vUv);
    vec4 maskColor = texture2D(tMask, vUv);
    float isPerson = 1.0 - maskColor.r;

    if (uFilterType == 0) {
      gl_FragColor = cameraColor;
    } else if (uFilterType == 1) {
      vec4 blurredColor = blur(tWebcam, vUv, uResolution);
      gl_FragColor = mix(blurredColor, cameraColor, isPerson);
    } else if (uFilterType == 2) {
      vec4 bgColor = texture2D(tBackground, vUv);
      gl_FragColor = mix(bgColor, cameraColor, isPerson);
    }
  }
`

const initThree = (container: HTMLDivElement, video: HTMLVideoElement) => {
  scene = new THREE.Scene()
  camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1)

  const width = container.clientWidth || 640
  const height = container.clientHeight || 480

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, preserveDrawingBuffer: true })
  renderer.setSize(width, height)
  container.appendChild(renderer.domElement)

  webcamTexture = new THREE.VideoTexture(video)
  webcamTexture.minFilter = THREE.LinearFilter
  webcamTexture.magFilter = THREE.LinearFilter

  maskCanvas = document.createElement('canvas')
  maskCanvas.width = video.videoWidth || 640
  maskCanvas.height = video.videoHeight || 480
  maskCtx = maskCanvas.getContext('2d')

  maskTexture = new THREE.CanvasTexture(maskCanvas)
  maskTexture.minFilter = THREE.LinearFilter
  maskTexture.magFilter = THREE.LinearFilter

  const dummyCanvas = document.createElement('canvas')
  dummyCanvas.width = 2
  dummyCanvas.height = 2
  const dummyCtx = dummyCanvas.getContext('2d')
  if (dummyCtx) {
    dummyCtx.fillStyle = '#000000'
    dummyCtx.fillRect(0, 0, 2, 2)
  }
  bgImageTexture = new THREE.CanvasTexture(dummyCanvas)

  const geometry = new THREE.PlaneGeometry(2, 2)
  shaderMaterial = new THREE.ShaderMaterial({
    vertexShader,
    fragmentShader,
    uniforms: {
      tWebcam: { value: webcamTexture },
      tMask: { value: maskTexture },
      tBackground: { value: bgImageTexture },
      uResolution: { value: new THREE.Vector2(width, height) },
      uFilterType: { value: 0 },
    },
    depthWrite: false,
    depthTest: false,
  })

  const mesh = new THREE.Mesh(geometry, shaderMaterial)
  scene.add(mesh)
}

const changeFilter = (type: 'none' | 'blur' | 'image') => {
  if (type === 'image' && fileInputRef.value) {
    fileInputRef.value.click()
    return
  }
  setFilterType(type)
}

const setFilterType = (type: 'none' | 'blur' | 'image') => {
  currentFilter.value = type
  filterMenuOpen.value = false
  if (shaderMaterial) {
    let typeId = 0
    if (type === 'blur') typeId = 1
    if (type === 'image') typeId = 2
    shaderMaterial.uniforms.uFilterType.value = typeId
  }
}

const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    const reader = new FileReader()

    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        if (bgImageTexture) bgImageTexture.dispose()

        bgImageTexture = new THREE.Texture(img)
        bgImageTexture.wrapS = THREE.ClampToEdgeWrapping
        bgImageTexture.wrapT = THREE.ClampToEdgeWrapping
        bgImageTexture.minFilter = THREE.LinearFilter
        bgImageTexture.needsUpdate = true

        if (shaderMaterial) {
          shaderMaterial.uniforms.tBackground.value = bgImageTexture
        }

        setFilterType('image')
      }
      img.src = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

const initializeAgoraAndAI = async () => {
  agoraClient = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' })
  agoraClient.enableAudioVolumeIndicator()

  agoraClient.on('user-published', async (user, mediaType) => {
    await agoraClient?.subscribe(user, mediaType)
    if (mediaType === 'video') {
      remoteVideoUser.value = user
      setTimeout(() => {
        if (remoteVideoRef.value) user.videoTrack?.play(remoteVideoRef.value)
      }, 100)
    }
    if (mediaType === 'audio') user.audioTrack?.play()

    if (!callStartTime) {
      callStartTime = Date.now()
    }
  })

  agoraClient.on('user-unpublished', (user, mediaType) => {
    if (mediaType === 'video') remoteVideoUser.value = null
  })

  agoraClient.on('user-left', async (user) => {
    remoteVideoUser.value = null
    await handleDisconnect()
  })

  agoraClient.on('volume-indicator', (volumes) => {
    volumes.forEach((volume) => {
      if (agoraClient && volume.uid !== agoraClient.uid) {
        remoteUserSpeaking.value = volume.level > 20
      }
    })
  })

  try {
    statusMessage.value = 'Uruchamianie kamery i ładowanie AI...'

    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
      audio: false,
    })

    if (videoRef.value) {
      videoRef.value.srcObject = stream
      await new Promise((resolve) => {
        videoRef.value!.onloadedmetadata = () => resolve(true)
      })
      await videoRef.value.play()
    }

    if (canvasContainerRef.value && videoRef.value) {
      initThree(canvasContainerRef.value, videoRef.value)
    }

    const vision = await FilesetResolver.forVisionTasks(
      'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@latest/wasm',
    )
    imageSegmenter = await ImageSegmenter.createFromOptions(vision, {
      baseOptions: {
        modelAssetPath:
          'https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite',
        delegate: 'GPU',
      },
      runningMode: 'VIDEO',
      outputCategoryMask: true,
      outputConfidenceMasks: false,
    })

    videoEnabled.value = true

    // Inicjalizacja pętli klatek wideo przy użyciu rVFC
    if (videoRef.value && 'requestVideoFrameCallback' in videoRef.value) {
      animationFrameId = (videoRef.value as any).requestVideoFrameCallback(processVideoFrame)
    } else {
      processVideoFrame()
    }

    if (renderer && scene && camera) {
      renderer.clear()
      renderer.render(scene, camera)
    }

    const authStore = useAuthStore()
    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce')
    const numericUid = getNumericUid(currentUserId)
    
    let tokenData: any
    try {
      statusMessage.value = 'Pobieranie tokenu Agora...'
      tokenData = await $fetch<any>(`http://localhost:8080/api/chat/calls/token`, {
        query: {
          channelName: conversationId.value.toString(),
          uid: numericUid
        }
      })
    } catch (tokenErr) {
      console.error('Failed to fetch Agora token:', tokenErr)
      statusMessage.value = 'Błąd pobierania tokenu autoryzacji.'
      return
    }

    await agoraClient.join(tokenData.appId, tokenData.channelName, tokenData.token, tokenData.uid)

    // Start heartbeat to keep the call active in Redis
    heartbeatTimer = setInterval(async () => {
      try {
        await $fetch('http://localhost:8080/api/chat/calls/heartbeat', {
          method: 'POST',
          query: { conversationId: conversationId.value.toString() }
        })
      } catch (hbErr) {
        console.error('Failed to send call heartbeat:', hbErr)
      }
    }, 12000)
    localAudioTrack = await AgoraRTC.createMicrophoneAudioTrack()

    const canvasElement = renderer.domElement as HTMLCanvasElement
    const canvasStream = canvasElement.captureStream(30)
    const processedVideoTrack = canvasStream.getVideoTracks()[0]

    localVideoTrack = AgoraRTC.createCustomVideoTrack({ mediaStreamTrack: processedVideoTrack })

    await localVideoTrack.setEncoderConfiguration({
      width: 640,
      height: 480,
      frameRate: 30,
      bitrateMin: 400,
      bitrateMax: 1200,
    })

    await agoraClient.publish([localAudioTrack, localVideoTrack])
    statusMessage.value = 'Połączono bezpiecznie!'
  } catch (err) {
    console.error('Błąd inicjalizacji komponentów:', err)
    statusMessage.value = 'Błąd połączenia z kamerą lub modelami AI.'
  }
}

const processVideoFrame = () => {
  if (!videoRef.value || !imageSegmenter || !videoEnabled.value) return

  const video = videoRef.value

  if (video.currentTime !== lastVideoTime && video.readyState >= 2) {
    lastVideoTime = video.currentTime
    const timestamp = performance.now()

    if (currentFilter.value === 'blur' || currentFilter.value === 'image') {
      imageSegmenter.segmentForVideo(video, timestamp, (result) => {
        const mask = Array.isArray(result.categoryMask)
          ? result.categoryMask[0]
          : result.categoryMask

        if (mask && maskCtx) {
          const width = mask.width
          const height = mask.height

          if (maskCanvas.width !== width || maskCanvas.height !== height) {
            maskCanvas.width = width
            maskCanvas.height = height
          }

          const maskData = mask.getAsFloat32Array() || mask.getAsUint8Array()

          if (maskData) {
            const imageData = maskCtx.createImageData(width, height)
            for (let i = 0; i < maskData.length; i++) {
              const intensity = Math.floor(maskData[i] * 255)
              const pixelIndex = i * 4
              imageData.data[pixelIndex] = intensity
              imageData.data[pixelIndex + 1] = intensity
              imageData.data[pixelIndex + 2] = intensity
              imageData.data[pixelIndex + 3] = 255
            }
            maskCtx.putImageData(imageData, 0, 0)
            if (maskTexture) maskTexture.needsUpdate = true
          }
        }
      })
    }
  }

  if (renderer && scene && camera) {
    renderer.render(scene, camera)
  }

  if ('requestVideoFrameCallback' in video) {
    animationFrameId = (video as any).requestVideoFrameCallback(processVideoFrame)
  } else {
    animationFrameId = requestAnimationFrame(processVideoFrame)
  }
}

const toggleScreenShare = async () => {
  if (!agoraClient) return

  try {
    if (!isSharingScreen.value) {
      statusMessage.value = 'Uruchamianie udostępniania ekranu...'

      screenVideoTrack = await AgoraRTC.createScreenVideoTrack(
        {
          encoderConfig: '1080p_1',
        },
        'auto',
      )

      screenVideoTrack.on('track-ended', () => {
        stopScreenShare()
      })

      if (localVideoTrack) {
        await agoraClient.unpublish([localVideoTrack])
      }
      await agoraClient.publish([screenVideoTrack])

      isSharingScreen.value = true
      statusMessage.value = 'Udostępniasz ekran'
    } else {
      await stopScreenShare()
    }
  } catch (err) {
    console.error('Błąd udostępniania ekranu:', err)
    statusMessage.value = 'Nie udało się udostępnić ekranu.'
  }
}

const stopScreenShare = async () => {
  if (!agoraClient || !isSharingScreen.value) return

  try {
    if (screenVideoTrack) {
      screenVideoTrack.close()
      await agoraClient.unpublish([screenVideoTrack])
      screenVideoTrack = null
    }

    if (localVideoTrack && videoEnabled.value) {
      await agoraClient.publish([localVideoTrack])
    }

    isSharingScreen.value = false
    statusMessage.value = 'Połączono bezpiecznie!'
  } catch (err) {
    console.error('Błąd zatrzymywania udostępniania ekranu:', err)
  }
}

const toggleVideo = async () => {
  if (videoEnabled.value) {
    videoEnabled.value = false
    if (animationFrameId && videoRef.value) {
      if ('cancelVideoFrameCallback' in videoRef.value) {
        ;(videoRef.value as any).cancelVideoFrameCallback(animationFrameId)
      } else {
        cancelAnimationFrame(animationFrameId)
      }
    }
    if (agoraClient && localVideoTrack && !isSharingScreen.value) {
      await agoraClient.unpublish([localVideoTrack])
    }
  } else {
    videoEnabled.value = true
    if (videoRef.value && 'requestVideoFrameCallback' in videoRef.value) {
      animationFrameId = (videoRef.value as any).requestVideoFrameCallback(processVideoFrame)
    } else {
      processVideoFrame()
    }
    if (agoraClient && localVideoTrack && !isSharingScreen.value) {
      await agoraClient.publish([localVideoTrack])
    }
  }
}

const toggleMute = async () => {
  if (localAudioTrack) {
    await localAudioTrack.setEnabled(!microphoneEnabled.value)
    microphoneEnabled.value = !microphoneEnabled.value
  }
}

const handleDisconnect = async () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }

  const duration = callStartTime ? Math.round((Date.now() - callStartTime) / 1000) : 0
  const authStore = useAuthStore()
  const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce')
  const callerId = String(route.query.callerId || '')

  try {
    await $fetch('http://localhost:8080/api/chat/calls/end', {
      method: 'POST',
      query: { conversationId: conversationId.value.toString() }
    })
  } catch (endErr) {
    console.error('Failed to end call state in Redis:', endErr)
  }

  // Only the caller logs the completed/rejected call state to the DB to avoid double logging
  if (callerId === currentUserId) {
    try {
      await $fetch('http://localhost:8080/api/chat/calls/log', {
        method: 'POST',
        query: {
          conversationId: conversationId.value.toString(),
          senderId: currentUserId,
          callerId: callerId,
          duration: duration,
          status: duration > 0 ? 'completed' : 'rejected',
          participantIds: [currentUserId, boxId.value.toString()].join(',')
        }
      })
    } catch (logErr) {
      console.error('Failed to log call outcome to DB:', logErr)
    }
  }

  videoEnabled.value = false

  if (animationFrameId && videoRef.value) {
    if ('cancelVideoFrameCallback' in videoRef.value) {
      ;(videoRef.value as any).cancelVideoFrameCallback(animationFrameId)
    } else {
      cancelAnimationFrame(animationFrameId)
    }
  }

  if (screenVideoTrack) {
    screenVideoTrack.close()
  }
  if (localAudioTrack) {
    localAudioTrack.stop()
    localAudioTrack.close()
  }
  if (localVideoTrack) {
    localVideoTrack.stop()
    localVideoTrack.close()
  }
  if (agoraClient) {
    await agoraClient.leave()
  }

  if (videoRef.value && videoRef.value.srcObject) {
    const stream = videoRef.value.srcObject as MediaStream
    stream.getTracks().forEach((track) => track.stop())
  }

  if (renderer) {
    renderer.dispose()
    renderer.domElement.remove()
  }
  if (shaderMaterial) shaderMaterial.dispose()
  if (bgImageTexture) bgImageTexture.dispose()
  if (imageSegmenter) imageSegmenter.close()

  currentStep.value = 'summary'
}

const handleResize = () => {
  if (!canvasContainerRef.value || !renderer || !shaderMaterial) return
  const width = canvasContainerRef.value.clientWidth || 640
  const height = canvasContainerRef.value.clientHeight || 480
  renderer.setSize(width, height)
  shaderMaterial.uniforms.uResolution.value.set(width, height)
}

onMounted(() => {
  initializeAgoraAndAI()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (currentStep.value === 'call') {
    handleDisconnect()
  }
})

const reconnectCall = () => {
  window.location.reload()
}

const closeAppWindow = () => {
  window.close()
}
</script>

<template>
  <div
    v-if="currentStep === 'call'"
    class="relative w-full h-screen bg-black text-white flex items-center justify-center overflow-hidden  "
  >
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      class="hidden"
      @change="handleImageUpload"
    />

    <div class="absolute top-4 left-4 flex items-center gap-3 z-20">
      <div class="w-10 h-10 rounded-full overflow-hidden bg-zinc-800">
        <img :src="avatarUrl" alt="Grupa" class="w-full h-full object-cover" />
      </div>
      <div>
        <h2 class="text-sm font-semibold tracking-wide">Wiktoria, Bartosz</h2>
        <div class="text-xs text-zinc-400 flex items-center gap-1 mt-0.5">
          <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
          {{ statusMessage }}
        </div>
      </div>
    </div>

    <div
      class="relative h-full w-full max-w-[500px] bg-black flex items-center justify-center overflow-hidden"
    >
      <img
        :src="avatarUrl"
        alt="Blur Background"
        class="absolute inset-0 w-full h-full object-cover blur-3xl scale-110 opacity-40"
      />
      <div class="absolute inset-0 bg-black/50"></div>

      <div
        v-show="remoteVideoUser"
        ref="remoteVideoRef"
        class="absolute inset-0 w-full h-full object-cover z-10"
      ></div>

      <div
        v-show="!remoteVideoUser"
        class="w-24 h-24 rounded-full overflow-hidden shadow-lg bg-zinc-800 flex items-center justify-center relative z-10 transition-all duration-200 ease-in-out"
        :class="
          remoteUserSpeaking ? 'ring-[6px] ring-green-500 scale-105' : 'ring-1 ring-zinc-600/30'
        "
      >
        <img :src="avatarUrl" alt="Avatar" class="w-full h-full object-cover" />
      </div>

      <div
        v-show="remoteVideoUser && remoteUserSpeaking"
        class="absolute inset-0 border-4 border-green-500 pointer-events-none z-20 transition-all"
      ></div>
    </div>

    <div
      v-if="connectionType === 'video'"
      class="absolute bottom-6 right-6 z-20 flex items-center gap-3"
    >
      <button
        @click="showCamera = !showCamera"
        class="w-8 h-8 bg-zinc-800/80 hover:bg-zinc-700 backdrop-blur-md rounded-full flex items-center justify-center transition-colors text-white"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path v-if="showCamera" stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
          <path v-else stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
      </button>

      <div
        class="w-48 aspect-[16/10] bg-zinc-900 rounded-xl overflow-hidden shadow-2xl border border-white/10 relative"
        v-show="showCamera"
      >
        <video
          ref="videoRef"
          autoplay
          playsinline
          muted
          width="640"
          height="480"
          style="display: none"
        ></video>
        <div
          ref="canvasContainerRef"
          v-show="videoEnabled && !isSharingScreen"
          class="w-full h-full"
        ></div>

        <div
          v-show="isSharingScreen"
          class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-zinc-950 text-green-400 p-2"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6 animate-pulse"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            />
          </svg>
          <p class="text-[10px] uppercase font-bold tracking-wider">Ekran wysyłany</p>
        </div>

        <div
          v-show="!videoEnabled && !isSharingScreen"
          class="absolute inset-0 flex flex-col items-center justify-center gap-2 p-4 bg-zinc-950 text-zinc-500 rounded-xl"
        >
          <p class="text-xs">Kamera wyłączona</p>
        </div>
      </div>
    </div>

    <div class="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex items-center gap-4 z-20">
      <button
        @click="toggleMute"
        :class="[
          'w-12 h-12 rounded-full flex items-center justify-center transition-colors',
          microphoneEnabled ? 'bg-white/20 hover:bg-white/30' : 'bg-red-500 hover:bg-red-600',
        ]"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            v-if="microphoneEnabled"
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
          />
          <path
            v-else
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"
          />
        </svg>
      </button>

      <button
        @click="toggleVideo"
        :class="[
          'w-12 h-12 rounded-full flex items-center justify-center transition-colors',
          videoEnabled ? 'bg-white/20 hover:bg-white/30' : 'bg-red-500 hover:bg-red-600',
        ]"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
          />
          <line
            v-show="!videoEnabled"
            x1="1"
            y1="1"
            x2="23"
            y2="23"
            stroke="currentColor"
            stroke-width="2"
          />
        </svg>
      </button>

      <button
        @click="toggleScreenShare"
        :class="[
          'w-12 h-12 rounded-full flex items-center justify-center transition-colors',
          isSharingScreen ? 'bg-green-500 hover:bg-green-600' : 'bg-white/20 hover:bg-white/30',
        ]"
        title="Udostępnij ekran"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      </button>

      <div class="relative">
        <button
          @click="filterMenuOpen = !filterMenuOpen"
          class="w-12 h-12 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-colors"
          title="Efekty i filtry"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
            />
          </svg>
        </button>

        <div
          v-if="filterMenuOpen"
          class="absolute bottom-16 left-1/2 transform -translate-x-1/2 bg-zinc-900 border border-zinc-800 rounded-xl p-2 flex flex-col gap-1 shadow-2xl w-44 z-30"
        >
          <button
            @click="changeFilter('none')"
            :class="[
              'text-xs text-left px-3 py-2 rounded-lg transition-colors',
              currentFilter === 'none'
                ? 'bg-white/10 text-white font-bold'
                : 'text-zinc-400 hover:bg-white/5 hover:text-white',
            ]"
          >
            Brak filtra
          </button>
          <button
            @click="changeFilter('blur')"
            :class="[
              'text-xs text-left px-3 py-2 rounded-lg transition-colors',
              currentFilter === 'blur'
                ? 'bg-white/10 text-white font-bold'
                : 'text-zinc-400 hover:bg-white/5 hover:text-white',
            ]"
          >
            Rozmycie postaci
          </button>
          <button
            @click="changeFilter('image')"
            :class="[
              'text-xs text-left px-3 py-2 rounded-lg transition-colors',
              currentFilter === 'image'
                ? 'bg-white/10 text-white font-bold'
                : 'text-zinc-400 hover:bg-white/5 hover:text-white',
            ]"
          >
            Wgraj własne tło...
          </button>
        </div>
      </div>

      <button
        @click="handleDisconnect"
        class="w-12 h-12 bg-red-600 hover:bg-red-700 rounded-full flex items-center justify-center transition-colors shadow-lg"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-6 w-6 rotate-[135deg]"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M3 5a2 2 0 012-2h3.28a1 1 0 01.94.725l.548 2.2a1 1 0 01-.321.988l-1.305.98a10.582 10.582 0 004.872 4.872l.98-1.305a1 1 0 01.988-.321l2.2.548a1 1 0 01.725.94V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
          />
        </svg>
      </button>
    </div>
  </div>

  <CallSummary v-else @reconnect="reconnectCall" @close="closeAppWindow" />
</template>

<style scoped>
:deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover;
  border-radius: 0.75rem;
}
</style>
