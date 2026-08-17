<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
import { useConversationsStore } from '@/stores/conversations'

const route = useRoute()

const connectionType = ref(route.query.type || 'video')
const rawBoxId = route.query.boxId || 'domyslny-kanal'
const rawConversationId = route.query.conversationId || route.query.boxId || 'domyslny-kanal'

const boxId = ref(String(rawBoxId).replace(/^user_/, ''))
const conversationId = ref(String(rawConversationId).replace(/^user_/, ''))

let heartbeatTimer: any = null
let checkAloneTimer: any = null

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
let hasLoggedCall = false

// Dodany stan połączenia: dzwonienie / połączono
const callState = ref<'ringing' | 'connected'>('ringing')

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

const conversationsStore = useConversationsStore()
const authStore = useAuthStore()

const myName = computed(() => {
  return authStore.currentUser?.name || 'Ja'
})

const myAvatar = computed(() => {
  return authStore.currentUser?.avatar || '/default-avatar.png'
})

const recipientId = computed(() => String(boxId.value).replace(/^user_/, ''))

const recipientName = computed(() => {
  const user = conversationsStore.usersCache[recipientId.value]
  if (user) return user.name
  conversationsStore.getOrFetchUser(recipientId.value)
  return 'Ładowanie...'
})

const recipientAvatar = computed(() => {
  const user = conversationsStore.usersCache[recipientId.value]
  if (user && user.avatar) return user.avatar
  conversationsStore.getOrFetchUser(recipientId.value)
  return '/default-avatar.png'
})

// --- DODAWANIE OSÓB DO ROZMOWY I TWORZENIE GRUPY ---
const isAddUserModalOpen = ref(false)
const searchQuery = ref('')
const isAddingUser = ref(false)
const callNotificationToast = ref<string | null>(null)

const contactsList = ref([
  {
    id: '7f23f5b8-87fb-4250-9ba9-6b5ed04afff0',
    name: 'dsd User',
    avatarUrl: 'https://i.pravatar.cc/150?img=1'
  },
  {
    id: 'd8604ec9-2999-4730-9409-d4c13a78a68e',
    name: 'E2EE Partner',
    avatarUrl: 'https://i.pravatar.cc/150?img=2'
  },
  {
    id: '0d4b14bc-1337-490f-ba79-27b62f4fdaf6',
    name: 'Bmiazek User',
    avatarUrl: 'https://i.pravatar.cc/150?img=3'
  },
  {
    id: '41da76f0-fc3e-362a-a939-e634bfb6a342',
    name: 'Piotr Kowalski',
    avatarUrl: 'https://i.pravatar.cc/150?img=4'
  },
  {
    id: '9a936f54-ceff-3813-9eba-fd21984efcf4',
    name: 'Tomasz Lewandowski',
    avatarUrl: 'https://i.pravatar.cc/150?img=5'
  }
])

const callParticipants = ref<string[]>([
  String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, ''),
  recipientId.value
])

const availableContactsToInvite = computed(() => {
  const currentParticipants = callParticipants.value.map(id => String(id).toLowerCase())
  return contactsList.value.filter(c => {
    const cleanId = String(c.id).toLowerCase()
    const matchesParticipant = currentParticipants.includes(cleanId)
    const matchesQuery = !searchQuery.value || c.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    return !matchesParticipant && matchesQuery
  })
})

const inviteUserToCall = async (user: any) => {
  try {
    isAddingUser.value = true
    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, '')
    const newUserId = String(user.id).replace(/^user_/, '')

    if (!callParticipants.value.includes(newUserId)) {
      callParticipants.value.push(newUserId)
    }

    const allParticipantIds = [...new Set(callParticipants.value)].filter(Boolean)
    const activeCallChannel = conversationId.value.toString()

    // 1. Sprawdź czy tworzymy nową grupę (jeśli była to rozmowa 1 na 1)
    let groupConvId = activeCallChannel
    const isPrivateCall = !groupConvId.startsWith('group_') && allParticipantIds.length > 2
    if (isPrivateCall) {
      groupConvId = `group_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
      // Zauważ: NIE zmieniamy conversationId.value, aby czas trwania i sesja połączenia nie uległy zresetowaniu!
    }

    // 2. Dodaj / zaktualizuj grupę w conversationsStore
    const groupMembers = allParticipantIds.map(pId => {
      const cached = conversationsStore.usersCache[pId]
      const foundContact = contactsList.value.find(c => c.id === pId)
      return {
        id: pId,
        name: cached?.name || foundContact?.name || (pId === currentUserId ? myName.value : 'Użytkownik'),
        avatar: cached?.avatar || foundContact?.avatarUrl || '/default-avatar.png'
      }
    })

    let existingChat = conversationsStore.chats.find(c => String(c.id) === String(groupConvId))
    if (!existingChat) {
      existingChat = {
        id: groupConvId,
        type: 'group' as any,
        name: groupMembers.filter(m => m.id !== currentUserId).map(m => m.name).join(', ') || 'Grupa wideo',
        avatarUrl: '/default-avatar.png',
        groupMembers: groupMembers as any,
        lastMessage: 'Trwa rozmowa grupowa',
        unreadCount: 0,
        createdAt: new Date().toISOString()
      }
      conversationsStore.chats.unshift(existingChat)
    } else {
      existingChat.groupMembers = groupMembers as any
      existingChat.name = groupMembers.filter(m => m.id !== currentUserId).map(m => m.name).join(', ') || existingChat.name
    }

    // 3. Wyślij zaproszenie do backendu (tworzy wiadomość w grupie ze wskazaniem kanału aktywnej rozmowy)
    const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'
    await $fetch(`${apiBase}/api/chat/calls/invite`, {
      method: 'POST',
      query: {
        conversationId: groupConvId,
        senderId: currentUserId,
        invitedUserId: newUserId,
        channelName: activeCallChannel,
        participantIds: allParticipantIds.join(',')
      }
    }).catch(err => console.warn('Failed to post invite to API:', err))

    // 4. Wyślij powiadomienie MQTT do wszystkich uczestników
    if (conversationsStore.isMqttConnected) {
      for (const pId of allParticipantIds) {
        conversationsStore.publishMqtt(`chat/messages/user/${pId}`, {
          type: 'call_group',
          systemActionType: 'call_started',
          systemActionPayload: `channel:${activeCallChannel}`,
          conversationId: groupConvId,
          channelName: activeCallChannel,
          senderId: currentUserId,
          callerId: currentUserId,
          callType: connectionType.value || 'video',
          participantIds: allParticipantIds,
          text: `SYSTEM_ACTION:call_started:channel:${activeCallChannel}`
        })
      }
    }

    callNotificationToast.value = `Dodano ${user.name} do rozmowy i utworzono grupę!`
    setTimeout(() => {
      callNotificationToast.value = null
    }, 4000)

    isAddUserModalOpen.value = false
  } catch (err) {
    console.error('Błąd podczas dodawania użytkownika do rozmowy:', err)
  } finally {
    isAddingUser.value = false
  }
}

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
  if (type === 'blur' || type === 'image') {
    loadMediaPipeBackground()
  }
  if (shaderMaterial && shaderMaterial.uniforms.uFilterType) {
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

        if (shaderMaterial && shaderMaterial.uniforms.tBackground) {
          shaderMaterial.uniforms.tBackground.value = bgImageTexture
        }

        setFilterType('image')
      }
      img.src = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

let isAiLoading = false
let aiLoaded = false

const loadMediaPipeBackground = async () => {
  if (isAiLoading || aiLoaded || imageSegmenter) return
  isAiLoading = true
  try {
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
    aiLoaded = true
  } catch (aiErr) {
    console.warn('MediaPipe background load failed:', aiErr)
  } finally {
    isAiLoading = false
  }
}

const initializeAgoraAndAI = async () => {
  agoraClient = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' })
  agoraClient.enableAudioVolumeIndicator()

  const callerId = String(route.query.callerId || '').replace(/^user_/, '')
  const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce').replace(/^user_/, '')
  const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'
  
  // Jeśli użytkownik odbiera połączenie (nie jest inicjatorem), od razu przechodzimy do rozmowy
  if (callerId && callerId !== currentUserId) {
    callState.value = 'connected'
    if (!callStartTime) {
      callStartTime = Date.now()
    }
  }

  // Gdy inny użytkownik dołączy
  agoraClient.on('user-joined', () => {
    callState.value = 'connected'
    if (!callStartTime) {
      callStartTime = Date.now()
    }
  })

  agoraClient.on('user-published', async (user, mediaType) => {
    callState.value = 'connected'
    if (!callStartTime) {
      callStartTime = Date.now()
    }
    await agoraClient?.subscribe(user, mediaType)
    if (mediaType === 'video') {
      remoteVideoUser.value = user
      setTimeout(() => {
        if (remoteVideoRef.value) user.videoTrack?.play(remoteVideoRef.value)
      }, 50)
    }
    if (mediaType === 'audio') user.audioTrack?.play()
  })

  agoraClient.on('user-unpublished', (user, mediaType) => {
    if (mediaType === 'video') remoteVideoUser.value = null
  })

  agoraClient.on('user-left', async () => {
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

  // 1. Ładowanie MediaPipe startuje natychmiast w tle (nie blokuje połączenia ani audio)
  setTimeout(() => {
    loadMediaPipeBackground()
  }, 50)

  const numericUid = getNumericUid(currentUserId)

  // 2. Równoległe pobieranie tokenu Agora i tworzenie mikrofonu
  const tokenPromise = $fetch<any>(`${apiBase}/api/chat/calls/token`, {
    query: {
      channelName: conversationId.value.toString(),
      uid: numericUid
    }
  })

  const audioTrackPromise = AgoraRTC.createMicrophoneAudioTrack().catch((err) => {
    console.warn('Mikrofon niedostępny:', err)
    return null
  })

  // 3. Połączenie z kanałem Agora zaraz po otrzymaniu tokenu
  const agoraJoinPromise = tokenPromise.then(async (tokenData) => {
    if (!agoraClient) return tokenData
    await agoraClient.join(tokenData.appId, tokenData.channelName, tokenData.token || null, tokenData.uid)
    if (agoraClient.remoteUsers.length > 0) {
      callState.value = 'connected'
      if (!callStartTime) {
        callStartTime = Date.now()
      }
    }

    heartbeatTimer = setInterval(async () => {
      try {
        await $fetch(`${apiBase}/api/chat/calls/heartbeat`, {
          method: 'POST',
          query: { conversationId: conversationId.value.toString() }
        })
      } catch (hbErr) {
        console.error('Failed to send call heartbeat:', hbErr)
      }
    }, 12000)

    checkAloneTimer = setInterval(async () => {
      if (callState.value === 'connected' && agoraClient && agoraClient.remoteUsers.length === 0) {
        console.log('Jesteś sam w pokoju po połączeniu. Rozłączanie...')
        await handleDisconnect()
      }
    }, 2000)

    return tokenData
  }).catch((tokenErr) => {
    console.error('Failed to fetch Agora token / join:', tokenErr)
    statusMessage.value = 'Błąd pobierania tokenu autoryzacji.'
    return null
  })

  // 4. Publikacja audio NATYCHMIAST gdy Agora i mikrofon są gotowe (rozmowa działa od razu!)
  Promise.all([agoraJoinPromise, audioTrackPromise]).then(async ([, micTrack]) => {
    if (micTrack && agoraClient) {
      localAudioTrack = micTrack
      try {
        await agoraClient.publish([localAudioTrack])
        statusMessage.value = 'Połączono (audio gotowe)'
      } catch (pubErr) {
        console.warn('Błąd publikacji mikrofonu:', pubErr)
      }
    }
  })

  // 5. Równoległa inicjalizacja kamery i wideo Three.js
  try {
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

    videoEnabled.value = true

    if (videoRef.value && 'requestVideoFrameCallback' in videoRef.value) {
      animationFrameId = (videoRef.value as any).requestVideoFrameCallback(processVideoFrame)
    } else {
      processVideoFrame()
    }

    if (renderer && scene && camera) {
      renderer.clear()
      renderer.render(scene, camera)
    }

    if (renderer?.domElement) {
      const canvasElement = renderer.domElement as HTMLCanvasElement
      const canvasStream = canvasElement.captureStream(30)
      const processedVideoTrack = canvasStream.getVideoTracks()[0]
      if (processedVideoTrack) {
        localVideoTrack = AgoraRTC.createCustomVideoTrack({ mediaStreamTrack: processedVideoTrack })
        await localVideoTrack.setEncoderConfiguration({
          width: 640,
          height: 480,
          frameRate: 30,
          bitrateMin: 400,
          bitrateMax: 1200,
        })

        // Publikacja wideo zaraz po dołączeniu do Agora
        await agoraJoinPromise
        if (agoraClient && localVideoTrack && videoEnabled.value && !isSharingScreen.value) {
          await agoraClient.publish([localVideoTrack])
        }
      }
    }

    statusMessage.value = 'Połączono bezpiecznie!'
  } catch (videoErr) {
    console.warn('Kamera niedostępna lub błąd:', videoErr)
    statusMessage.value = 'Połączono (audio)'
  }
}

const processVideoFrame = () => {
  if (!videoRef.value || !videoEnabled.value) return

  const video = videoRef.value

  if (video.currentTime !== lastVideoTime && video.readyState >= 2) {
    lastVideoTime = video.currentTime
    const timestamp = performance.now()

    if ((currentFilter.value === 'blur' || currentFilter.value === 'image') && imageSegmenter) {
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
  if (checkAloneTimer) {
    clearInterval(checkAloneTimer)
    checkAloneTimer = null
  }

  const duration = callStartTime ? Math.round((Date.now() - callStartTime) / 1000) : 0
  const authStore = useAuthStore()
  const rawUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce')
  const currentUserId = rawUserId.replace(/^user_/, '')
  const callerId = String(route.query.callerId || '')
  const cleanCallerId = callerId.replace(/^user_/, '')
  const isCaller = !cleanCallerId || cleanCallerId === currentUserId
  const actualCallerId = isCaller ? currentUserId : cleanCallerId

  const cleanConvId = conversationId.value.toString().replace(/^user_/, '')
  const cleanBoxId = boxId.value.toString().replace(/^user_/, '')
  const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'

  try {
    await $fetch(`${apiBase}/api/chat/calls/end`, {
      method: 'POST',
      query: { conversationId: cleanConvId }
    })
  } catch (endErr) {
    console.error('Failed to end call state in Redis:', endErr)
  }

  if (!hasLoggedCall) {
    hasLoggedCall = true
    try {
      await $fetch(`${apiBase}/api/chat/calls/log`, {
        method: 'POST',
        query: {
          conversationId: cleanConvId,
          senderId: currentUserId,
          callerId: actualCallerId,
          duration: duration,
          status: duration > 0 ? 'completed' : 'rejected',
          participantIds: [currentUserId, cleanBoxId].join(',')
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
  if (shaderMaterial.uniforms.uResolution) {
    shaderMaterial.uniforms.uResolution.value.set(width, height)
  }
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
  <div v-if="currentStep === 'call'" class="relative w-full h-screen bg-black text-white overflow-hidden">

    <!-- 1. EKRAN "DZWONIENIE..." -->
    <div v-if="callState === 'ringing'" class="absolute inset-0 bg-black z-[100] flex flex-col items-center justify-center">

      <!-- Top Bar (Absolute, żeby nie przesuwał środka) -->
      <div class="absolute top-0 left-0 right-0 p-5 flex justify-between items-start z-10 w-full">
        <!-- Left: Moje info -->
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-full overflow-hidden bg-zinc-800">
            <img :src="myAvatar" class="w-full h-full object-cover" />
          </div>
          <div class="flex flex-col">
            <span class="text-[14px] font-medium text-white tracking-wide">{{ myName }}</span>
            <div class="flex items-center gap-1.5 text-[11px] text-zinc-400 mt-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
              </svg>
              W pełni szyfrowane
            </div>
          </div>
        </div>

        <!-- Right: Kropki & Toast -->
        <div class="flex flex-col items-end gap-3">
          <button class="p-2 rounded-full hover:bg-white/10 transition-colors text-white mr-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z" />
            </svg>
          </button>

          <!-- Audio toast -->
          <div class="bg-[#28292A] rounded-[8px] pl-2 pr-4 py-2 flex items-center gap-3 shadow-lg max-w-sm">
            <div class="w-8 h-8 rounded-full bg-[#444746] flex items-center justify-center shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-[18px] w-[18px] text-zinc-200" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              </svg>
            </div>
            <span class="text-[13px] font-medium text-zinc-200 leading-tight">Podłączony mikrofon i głośnik: Mikrofon<br/>(MacBook Air)</span>
          </div>
        </div>
      </div>

      <!-- Center: Odbiorca -->
      <div class="flex flex-col items-center justify-center mb-12">
        <div class="w-[120px] h-[120px] rounded-full overflow-hidden mb-5 bg-zinc-800">
          <img :src="recipientAvatar" class="w-full h-full object-cover" />
        </div>
        <h1 class="text-[28px] font-medium text-white mb-1.5">{{ recipientName }}</h1>
        <p class="text-[15px] text-zinc-400 tracking-wide">Dzwonienie...</p>
      </div>

      <!-- Bottom: Controls -->
      <div class="absolute bottom-8 flex items-center gap-4">
        <!-- Zastępcze ikony i style zbliżone do Meet -->
        <button class="w-[50px] h-[50px] rounded-full bg-[#3C4043] flex items-center justify-center transition-colors">
          <svg class="w-5 h-5 text-zinc-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
          </svg>
        </button>

        <button
          @click="isAddUserModalOpen = true"
          class="w-[50px] h-[50px] rounded-full bg-[#3C4043] hover:bg-[#4E5256] flex items-center justify-center transition-colors cursor-pointer"
          title="Dodaj osobę do rozmowy"
        >
          <svg class="w-5 h-5 text-zinc-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
          </svg>
        </button>

        <button @click="toggleVideo" class="w-[50px] h-[50px] rounded-full flex items-center justify-center transition-colors" :class="videoEnabled ? 'bg-[#3C4043]' : 'bg-[#EA4335]'">
          <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
            <line v-show="!videoEnabled" x1="3" y1="3" x2="21" y2="21" stroke="currentColor" stroke-width="2" />
          </svg>
        </button>

        <button @click="toggleMute" class="w-[50px] h-[50px] rounded-full flex items-center justify-center transition-colors" :class="microphoneEnabled ? 'bg-[#3C4043]' : 'bg-[#EA4335]'">
          <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path v-if="microphoneEnabled" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
          </svg>
        </button>

        <button @click="handleDisconnect" class="w-[60px] h-[40px] rounded-full bg-[#EA4335] hover:bg-[#D33426] flex items-center justify-center transition-colors px-4">
          <svg class="w-[22px] h-[22px] text-white rotate-[135deg]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.94.725l.548 2.2a1 1 0 01-.321.988l-1.305.98a10.582 10.582 0 004.872 4.872l.98-1.305a1 1 0 01.988-.321l2.2.548a1 1 0 01.725.94V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 2. WŁAŚCIWY EKRAN ROZMOWY (włączany, gdy callState === 'connected') -->
    <div class="absolute inset-0 flex items-center justify-center">
      <input ref="fileInputRef" type="file" accept="image/*" class="hidden" @change="handleImageUpload" />

      <!-- Minimalistyczne info o połączeniu -->
      <div v-show="callState === 'connected'" class="absolute top-4 left-4 flex items-center gap-3 z-20">
        <div class="w-10 h-10 rounded-full overflow-hidden bg-zinc-800">
          <img :src="recipientAvatar" alt="Avatar" class="w-full h-full object-cover" />
        </div>
        <div>
          <h2 class="text-sm font-semibold tracking-wide">{{ recipientName }}</h2>
          <div class="text-xs text-zinc-400 flex items-center gap-1 mt-0.5">
            <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
            {{ statusMessage }}
          </div>
        </div>
      </div>

      <div class="relative h-full w-full max-w-[500px] bg-black flex items-center justify-center overflow-hidden">
        <img
          :src="recipientAvatar"
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
          :class="remoteUserSpeaking ? 'ring-[6px] ring-green-500 scale-105' : 'ring-1 ring-zinc-600/30'"
        >
          <img :src="recipientAvatar" alt="Avatar" class="w-full h-full object-cover" />
        </div>

        <div
          v-show="remoteVideoUser && remoteUserSpeaking"
          class="absolute inset-0 border-4 border-green-500 pointer-events-none z-20 transition-all"
        ></div>
      </div>

      <div
       v-show="connectionType === 'video' && callState === 'connected'"
        class="absolute bottom-6 right-6 z-20 flex items-center gap-3"
      >
        <button
          @click="showCamera = !showCamera"
          class="w-8 h-8 bg-zinc-800/80 hover:bg-zinc-700 backdrop-blur-md rounded-full flex items-center justify-center transition-colors text-white"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path v-if="showCamera" stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <div class="w-48 aspect-[16/10] bg-zinc-900 rounded-xl overflow-hidden shadow-2xl border border-white/10 relative" v-show="showCamera">
          <video
            ref="videoRef"
            autoplay
            playsinline
            muted
            width="640"
            height="480"
            style="display: none"
          ></video>
          <div ref="canvasContainerRef" v-show="videoEnabled && !isSharingScreen" class="w-full h-full"></div>

          <div v-show="isSharingScreen" class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-zinc-950 text-green-400 p-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            <p class="text-[10px] uppercase font-bold tracking-wider">Ekran wysyłany</p>
          </div>

          <div v-show="!videoEnabled && !isSharingScreen" class="absolute inset-0 flex flex-col items-center justify-center gap-2 p-4 bg-zinc-950 text-zinc-500 rounded-xl">
            <p class="text-xs">Kamera wyłączona</p>
          </div>
        </div>
      </div>

      <div v-show="callState === 'connected'" class="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex items-center gap-4 z-20">
        <button
          @click="toggleMute"
          :class="[
            'w-12 h-12 rounded-full flex items-center justify-center transition-colors',
            microphoneEnabled ? 'bg-white/20 hover:bg-white/30' : 'bg-red-500 hover:bg-red-600',
          ]"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path v-if="microphoneEnabled" stroke-linecap="round" stroke-linejoin="round" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
          </svg>
        </button>

        <button
          @click="toggleVideo"
          :class="[
            'w-12 h-12 rounded-full flex items-center justify-center transition-colors',
            videoEnabled ? 'bg-white/20 hover:bg-white/30' : 'bg-red-500 hover:bg-red-600',
          ]"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
            <line v-show="!videoEnabled" x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2" />
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
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </button>

        <button
          @click="isAddUserModalOpen = true"
          class="w-12 h-12 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-colors cursor-pointer shadow-md"
          title="Dodaj osobę do rozmowy"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </button>

        <div class="relative">
          <button
            @click="filterMenuOpen = !filterMenuOpen"
            class="w-12 h-12 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-colors"
            title="Efekty i filtry"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
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
                currentFilter === 'none' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
              ]"
            >
              Brak filtra
            </button>
            <button
              @click="changeFilter('blur')"
              :class="[
                'text-xs text-left px-3 py-2 rounded-lg transition-colors',
                currentFilter === 'blur' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
              ]"
            >
              Rozmycie postaci
            </button>
            <button
              @click="changeFilter('image')"
              :class="[
                'text-xs text-left px-3 py-2 rounded-lg transition-colors',
                currentFilter === 'image' ? 'bg-white/10 text-white font-bold' : 'text-zinc-400 hover:bg-white/5 hover:text-white',
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
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 rotate-[135deg]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.94.725l.548 2.2a1 1 0 01-.321.988l-1.305.98a10.582 10.582 0 004.872 4.872l.98-1.305a1 1 0 01.988-.321l2.2.548a1 1 0 01.725.94V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Modal dodawania użytkownika do rozmowy -->
    <div
      v-if="isAddUserModalOpen"
      class="fixed inset-0 z-[200] bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
      @click.self="isAddUserModalOpen = false"
    >
      <div class="bg-[#242526] border border-zinc-700/60 rounded-2xl w-full max-w-md overflow-hidden shadow-2xl flex flex-col font-sans text-white">
        <!-- Header -->
        <div class="p-4 border-b border-zinc-700/60 flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-full bg-[#0084FF]/20 flex items-center justify-center text-[#0084FF]">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
              </svg>
            </div>
            <h3 class="font-semibold text-lg">Dodaj osoby do rozmowy</h3>
          </div>
          <button
            @click="isAddUserModalOpen = false"
            class="w-8 h-8 rounded-full bg-zinc-800 hover:bg-zinc-700 flex items-center justify-center text-zinc-400 hover:text-white transition-colors cursor-pointer"
          >
            ✕
          </button>
        </div>

        <!-- Search input -->
        <div class="p-4 pb-2">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Wyszukaj znajomych..."
              class="w-full bg-[#3A3B3C] text-white placeholder-zinc-400 text-sm px-4 py-2.5 rounded-xl border border-transparent focus:border-[#0084FF] focus:outline-none transition-all"
            />
          </div>
          <p class="text-xs text-zinc-400 mt-2">
            Dodanie osoby natychmiast utworzy grupę i wyśle wiadomość z przyciskiem dołączenia do rozmowy.
          </p>
        </div>

        <!-- Contacts list -->
        <div class="max-h-[300px] overflow-y-auto p-4 pt-2 space-y-2 custom-scrollbar">
          <div
            v-for="user in availableContactsToInvite"
            :key="user.id"
            class="flex items-center justify-between p-2.5 hover:bg-zinc-800/80 rounded-xl transition-colors group"
          >
            <div class="flex items-center gap-3">
              <img :src="user.avatarUrl" class="w-10 h-10 rounded-full object-cover border border-zinc-700" />
              <div>
                <div class="text-sm font-medium text-white">{{ user.name }}</div>
                <div class="text-xs text-zinc-400">Dostępny do połączenia</div>
              </div>
            </div>

            <button
              @click="inviteUserToCall(user)"
              :disabled="isAddingUser"
              class="bg-[#0084FF] hover:bg-[#0073E6] disabled:opacity-50 text-white font-medium text-xs px-3.5 py-1.5 rounded-lg transition-colors flex items-center gap-1.5 shadow-sm cursor-pointer"
            >
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Dodaj
            </button>
          </div>

          <div v-if="availableContactsToInvite.length === 0" class="text-center py-8 text-zinc-500 text-sm">
            Brak dostępnych kontaktów do dodania.
          </div>
        </div>
      </div>
    </div>

    <!-- Toast powiadomienia -->
    <transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-y-4 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-4 opacity-0"
    >
      <div
        v-if="callNotificationToast"
        class="fixed top-5 right-5 z-[210] bg-[#0084FF] text-white px-4 py-3 rounded-xl shadow-2xl flex items-center gap-2 font-medium text-sm border border-white/10"
      >
        <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        {{ callNotificationToast }}
      </div>
    </transition>
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
