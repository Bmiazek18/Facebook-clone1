<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import * as THREE from 'three'
// Oficjalna i poprawna paczka npm
import { FilesetResolver, FaceLandmarker } from '@mediapipe/tasks-vision'

const videoRef = ref(null)
const canvasRef = ref(null)
const cheekSize = ref(1.2) // Domyślna, silniejsza wartość dla efektu ze zdjęcia

let scene, camera, renderer, faceMaterial, videoTexture
let faceLandmarker
let animationFrameId
let stream

// VERTEX SHADER - Specjalne horyzontalne rozpychanie dolnych partii twarzy
const vertexShader = `
varying vec2 vUv;
uniform float uCheekSize;
uniform float uEffectRadius;
uniform vec2 uLeftCheek;
uniform vec2 uRightCheek;

void main() {
    vUv = uv;
    vec3 pos = position;

    // Środek twarzy to punkt dokładnie pomiędzy lewym a prawym policzkiem
    vec2 faceCenter = (uLeftCheek + uRightCheek) * 0.5;

    // Obliczamy odległość wierzchołka od środka twarzy
    float distToFaceCenter = length(pos.xy - faceCenter);

    // Maska twarzy: jeśli wierzchołek jest dalej niż np. 1.8 * promień policzka,
    // to znaczy, że to już tło i nie chcemy go dotykać.
    float faceMaskRadius = uEffectRadius * 1.8;
    float faceMask = smoothstep(faceMaskRadius, faceMaskRadius * 0.7, distToFaceCenter);

    // --- LEWY POLICZEK ---
    float distToLeft = length(pos.xy - uLeftCheek);
    if (distToLeft < uEffectRadius) {
        float normalizedDist = distToLeft / uEffectRadius;
        float strength = pow(cos(normalizedDist * 1.5707), 2.0);

        // Mnożymy efekt przez faceMask, aby wygasić go na krawędziach wideo (w tle)
        pos.x -= strength * uEffectRadius * uCheekSize * 0.65 * faceMask;
        pos.y -= strength * uEffectRadius * uCheekSize * 0.15 * faceMask;
    }

    // --- PRAWY POLICZEK ---
    float distToRight = length(pos.xy - uRightCheek);
    if (distToRight < uEffectRadius) {
        float normalizedDist = distToRight / uEffectRadius;
        float strength = pow(cos(normalizedDist * 1.5707), 2.0);

        pos.x += strength * uEffectRadius * uCheekSize * 0.65 * faceMask;
        pos.y -= strength * uEffectRadius * uCheekSize * 0.15 * faceMask;
    }

    gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
}
`

const fragmentShader = `
varying vec2 vUv;
uniform sampler2D uVideoTexture;

void main() {
    gl_FragColor = texture2D(uVideoTexture, vUv);
}
`

const updateCheekSize = () => {
  if (faceMaterial) {
    faceMaterial.uniforms.uCheekSize.value = cheekSize.value
  }
}

onMounted(async () => {
  const width = 640
  const height = 480

  // Inicjalizacja Three.js
  scene = new THREE.Scene()
  camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 10)
  camera.position.z = 1

  renderer = new THREE.WebGLRenderer({ canvas: canvasRef.value, alpha: true, antialias: true })
  renderer.setSize(width, height)

  // Kamera użytkownika
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: { width, height } })
    videoRef.value.srcObject = stream
    videoRef.value.play()
  } catch (err) {
    console.error('Brak dostępu do kamery:', err)
    return
  }

  videoTexture = new THREE.VideoTexture(videoRef.value)
  videoTexture.colorSpace = THREE.SRGBColorSpace

  // Gęsta siatka 128x128 zapobiega poszarpanym krawędziom przy dużym rozciągnięciu
  const faceGeometry = new THREE.PlaneGeometry(2, 2, 128, 128)

  faceMaterial = new THREE.ShaderMaterial({
    vertexShader,
    fragmentShader,
    uniforms: {
      uVideoTexture: { value: videoTexture },
      uCheekSize: { value: cheekSize.value },
      uEffectRadius: { value: 0.4 },
      uLeftCheek: { value: new THREE.Vector2(-2, -2) },
      uRightCheek: { value: new THREE.Vector2(2, -2) },
    },
    depthWrite: false,
  })

  const faceMesh = new THREE.Mesh(faceGeometry, faceMaterial)
  scene.add(faceMesh)

  // Inicjalizacja MediaPipe
  try {
    const filesetResolver = await FilesetResolver.forVisionTasks(
      'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@latest/wasm',
    )

    faceLandmarker = await FaceLandmarker.createFromOptions(filesetResolver, {
      baseOptions: {
        modelAssetPath: `https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task`,
        delegate: 'GPU',
      },
      outputFaceBlendshapes: false,
      runningMode: 'VIDEO',
      numFaces: 2,
    })
  } catch (e) {
    console.error('Błąd ładowania MediaPipe:', e)
    return
  }

  // Mapowanie współrzędnych dostosowane pod scaleX(-1) na canvasie
  const mapLandmarkToThree = (landmark) => {
    const x = landmark.x * 2 - 1
    const y = (1 - landmark.y) * 2 - 1
    return new THREE.Vector2(x, y)
  }

  // Pętla renderowania
  const renderLoop = () => {
    if (videoRef.value && videoRef.value.readyState >= 2 && faceLandmarker) {
      const startTimeMs = performance.now()
      const results = faceLandmarker.detectForVideo(videoRef.value, startTimeMs)

      if (results.faceLandmarks && results.faceLandmarks.length > 0) {
        const landmarks = results.faceLandmarks[0]

        // Punkty dolnych zewnętrznych policzków/żuchwy (132 i 361)
        const leftCheekPt = landmarks[132]
        const rightCheekPt = landmarks[361]

        // Źrenice do wyliczania skali odległości od kamery
        const leftPupil = landmarks[468]
        const rightPupil = landmarks[473]

        if (leftCheekPt && rightCheekPt && leftPupil && rightPupil) {
          faceMaterial.uniforms.uLeftCheek.value.copy(mapLandmarkToThree(leftCheekPt))
          faceMaterial.uniforms.uRightCheek.value.copy(mapLandmarkToThree(rightCheekPt))

          // Dynamiczne skalowanie obszaru działania na bazie dystansu między oczami
          const pupilDist = Math.sqrt(
            Math.pow(leftPupil.x - rightPupil.x, 2) + Math.pow(leftPupil.y - rightPupil.y, 2),
          )

          // Mnożnik 1.6 pozwala objąć cały dół konturu twarzy
          const dynamicRadius = pupilDist * 1.6
          faceMaterial.uniforms.uEffectRadius.value = THREE.MathUtils.clamp(
            dynamicRadius,
            0.25,
            0.65,
          )
        }
      } else {
        // Ukryj efekt poza ekranem, gdy brak twarzy
        faceMaterial.uniforms.uLeftCheek.value.set(-5, -5)
        faceMaterial.uniforms.uRightCheek.value.set(5, -5)
      }
    }

    renderer.render(scene, camera)
    animationFrameId = requestAnimationFrame(renderLoop)
  }

  renderLoop()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
  if (stream) {
    stream.getTracks().forEach((track) => track.stop())
  }
  if (faceLandmarker) {
    faceLandmarker.close()
  }
  if (renderer) {
    renderer.dispose()
  }
})
</script>

<template>
  <div class="filter-wrapper">
    <div class="canvas-container">
      <video ref="videoRef" autoplay playsinline muted class="hidden-video"></video>
      <canvas ref="canvasRef"></canvas>
    </div>

    <div class="control-panel">
      <h3>Filtr: Mega Policzki</h3>
      <div class="slider-group">
        <label for="cheekSlider">Rozmiar:</label>
        <input
          id="cheekSlider"
          type="range"
          min="0.0"
          max="3.0"
          step="0.1"
          v-model.number="cheekSize"
          @input="updateCheekSize"
        />
        <span class="value-display">{{ (cheekSize * 100).toFixed(0) }}%</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100vw;
  height: 100vh;
  background-color: #1a1a1a;
  font-family: sans-serif;
  color: #fff;
}

.canvas-container {
  position: relative;
  width: 640px;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

.hidden-video {
  display: none;
}

canvas {
  width: 100%;
  height: 100%;
  transform: scaleX(-1); /* Odbicie lustrzane obrazu */
}

.control-panel {
  margin-top: 24px;
  background-color: #2a2a2a;
  padding: 16px 32px;
  border-radius: 8px;
}

.slider-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

input[type='range'] {
  width: 200px;
}

.value-display {
  font-weight: bold;
  color: #4caf50;
}
</style>
