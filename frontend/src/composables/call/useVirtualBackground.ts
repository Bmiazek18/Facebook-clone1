import { ref } from 'vue'
import * as THREE from 'three'
import { FilesetResolver, ImageSegmenter } from '@mediapipe/tasks-vision'

export function useVirtualBackground() {
  const currentFilter = ref<'none' | 'blur' | 'image'>('none')
  const filterMenuOpen = ref(false)

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

  const renderLoop = (video: HTMLVideoElement) => {
    if (currentFilter.value !== 'none' && imageSegmenter && maskCtx && video.readyState >= 2) {
      if (video.currentTime !== lastVideoTime) {
        lastVideoTime = video.currentTime
        imageSegmenter.segmentForVideo(video, performance.now(), (result) => {
          if (result.categoryMask && maskCtx) {
            const maskData = result.categoryMask.getAsUint8Array()
            const imgData = maskCtx.createImageData(maskCanvas.width, maskCanvas.height)
            for (let i = 0; i < maskData.length; i++) {
              imgData.data[i * 4] = maskData[i] === 0 ? 255 : 0
              imgData.data[i * 4 + 1] = 0
              imgData.data[i * 4 + 2] = 0
              imgData.data[i * 4 + 3] = 255
            }
            maskCtx.putImageData(imgData, 0, 0)
            if (maskTexture) maskTexture.needsUpdate = true
          }
        })
      }
    }

    if (renderer && scene && camera) {
      renderer.render(scene, camera)
    }
    animationFrameId = requestAnimationFrame(() => renderLoop(video))
  }

  const stopRenderLoop = () => {
    if (animationFrameId) {
      cancelAnimationFrame(animationFrameId)
      animationFrameId = null
    }
  }

  const getCanvasStream = (): MediaStream | null => {
    return renderer?.domElement?.captureStream(30) || null
  }

  return {
    currentFilter,
    filterMenuOpen,
    initThree,
    setFilterType,
    handleImageUpload,
    renderLoop,
    stopRenderLoop,
    getCanvasStream,
  }
}
