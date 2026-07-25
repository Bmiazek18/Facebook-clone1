import { ref, type Ref } from 'vue'
import {
  Output,
  Mp4OutputFormat,
  BufferTarget,
  EncodedVideoPacketSource,
  EncodedPacket,
} from 'mediabunny'
import type {
  VideoClip,
  TextOverlay,
  ImageOverlay,
  PipVideoOverlay,
  AnimationState,
  ImageAnimationState,
} from '@/types/video-editor.types'

const CANVAS_WIDTH = 1080
const CANVAS_HEIGHT = 1920

// Easing functions
const easeOutQuart = (t: number) => 1 - Math.pow(1 - t, 4)
const easeOutBack = (t: number) => {
  const c1 = 1.70158
  const c3 = c1 + 1
  return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2)
}

// Animation state calculations
const calculateTextState = (text: TextOverlay, time: number): AnimationState => {
  const timeSinceStart = time - text.startTime
  const timeUntilEnd = text.endTime - time
  const entryDuration = text.entryDuration || 0.5
  const exitDuration = text.exitDuration || 0.5

  let opacity = 1
  let scale = 1
  let translateX = 0
  let translateY = 0

  // Entry Animations
  if (text.entryAnimation && text.entryAnimation !== 'none' && timeSinceStart < entryDuration) {
    const progress = Math.max(0, Math.min(1, timeSinceStart / entryDuration))
    const easeVal = easeOutQuart(progress)

    switch (text.entryAnimation) {
      case 'fade-in':
        opacity *= progress
        break
      case 'zoom-in':
        scale = 0.2 + 0.8 * easeVal
        opacity *= progress
        break
      case 'pop-in':
        scale = Math.max(0, easeOutBack(progress))
        break
      case 'slide-in-left':
        translateX = -100 * (1 - easeVal)
        break
      case 'slide-in-right':
        translateX = 100 * (1 - easeVal)
        break
      case 'slide-in-top':
        translateY = -100 * (1 - easeVal)
        break
      case 'slide-in-bottom':
        translateY = 100 * (1 - easeVal)
        break
    }
  }

  // Loop Animations
  if (text.loopAnimation && timeSinceStart > entryDuration && timeUntilEnd > exitDuration) {
    const loopTime = timeSinceStart - entryDuration
    if (text.loopAnimation === 'pulse') {
      scale *= 1 + 0.05 * Math.sin(loopTime * Math.PI * 2)
    } else if (text.loopAnimation === 'float') {
      translateY += 5 * Math.sin(loopTime * Math.PI)
    } else if (text.loopAnimation === 'shake') {
      translateX += 2 * Math.sin(loopTime * Math.PI * 10)
    }
  }

  // Exit Animations
  if (text.exitAnimation && text.exitAnimation !== 'none' && timeUntilEnd < exitDuration) {
    const progress = Math.max(0, Math.min(1, timeUntilEnd / exitDuration))
    const easeVal = 1 - Math.pow(1 - progress, 4)

    switch (text.exitAnimation) {
      case 'fade-out':
        opacity *= progress
        break
      case 'zoom-out':
        scale *= progress
        opacity *= progress
        break
      case 'slide-out-left':
        translateX -= 100 * (1 - easeVal)
        break
      case 'slide-out-right':
        translateX += 100 * (1 - easeVal)
        break
      case 'slide-out-top':
        translateY -= 100 * (1 - easeVal)
        break
      case 'slide-out-bottom':
        translateY += 100 * (1 - easeVal)
        break
      case 'pop-out':
        scale *= progress
        break
    }
  }

  return { opacity, scale, translateX, translateY }
}

const calculateImageState = (image: ImageOverlay, time: number): ImageAnimationState => {
  const timeSinceStart = time - image.startTime
  const timeUntilEnd = image.endTime - time
  const entryDuration = image.entryDuration || 0.5
  const exitDuration = image.exitDuration || 0.5

  let opacity = image.opacity
  let scale = 1
  let translateX = 0

  // Entry Animations
  if (image.entryAnimation && image.entryAnimation !== 'none' && timeSinceStart < entryDuration) {
    const progress = Math.max(0, Math.min(1, timeSinceStart / entryDuration))
    const easeVal = easeOutQuart(progress)

    switch (image.entryAnimation) {
      case 'fade-in':
        opacity *= progress
        break
      case 'zoom-in':
        scale = 0.2 + 0.8 * easeVal
        opacity *= progress
        break
      case 'slide-in-left':
        translateX = -100 * (1 - easeVal)
        break
      case 'slide-in-right':
        translateX = 100 * (1 - easeVal)
        break
    }
  }

  // Exit Animations
  if (image.exitAnimation && image.exitAnimation !== 'none' && timeUntilEnd < exitDuration) {
    const progress = Math.max(0, Math.min(1, timeUntilEnd / exitDuration))
    const easeVal = 1 - Math.pow(1 - progress, 4)

    switch (image.exitAnimation) {
      case 'fade-out':
        opacity *= progress
        break
      case 'zoom-out':
        scale *= progress
        opacity *= progress
        break
      case 'slide-out-left':
        translateX -= 100 * (1 - easeVal)
        break
      case 'slide-out-right':
        translateX += 100 * (1 - easeVal)
        break
    }
  }

  return { opacity, scale, translateX }
}

// Export calculation functions for preview use
export { calculateTextState, calculateImageState }

export function useVideoExport(
  renderCanvasRef: Ref<HTMLCanvasElement | null>,
  clips: Ref<VideoClip[]>,
  textOverlays: Ref<TextOverlay[]>,
  imageOverlays: Ref<ImageOverlay[]>,
  pipVideoOverlays: Ref<PipVideoOverlay[]>,
  totalDuration: Ref<number>,
) {
  const isRendering = ref(false)
  const renderProgress = ref(0)

  const exportVideo = async (onDone: (url: string) => void) => {
    if (clips.value.length === 0 || !renderCanvasRef.value) return

    isRendering.value = true
    renderProgress.value = 0

    try {
      const canvas = renderCanvasRef.value
      const ctx = canvas.getContext('2d', { alpha: false })
      if (!ctx) throw new Error('Błąd Canvas')

      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'

      const fps = 30
      const totalFrames = Math.ceil(totalDuration.value * fps)
      const safeTotalFrames = totalFrames > 0 ? totalFrames : 100

      // MediaBunny Output
      const output = new Output({
        format: new Mp4OutputFormat({ fastStart: 'reserve' }),
        target: new BufferTarget(),
      })

      const videoSource = new EncodedVideoPacketSource('avc')
      output.addVideoTrack(videoSource, {
        maximumPacketCount: safeTotalFrames + 300,
      })

      await output.start()

      const pendingPackets: Promise<void>[] = []

      // WebCodecs Encoder
      const videoEncoder = new VideoEncoder({
        output: (chunk, meta) => {
          if (meta && meta.decoderConfig && !meta.decoderConfig.colorSpace) {
            const newMeta = { ...meta }
            newMeta.decoderConfig = {
              ...meta.decoderConfig,
              colorSpace: {
                primaries: 'bt709',
                transfer: 'bt709',
                matrix: 'bt709',
                fullRange: true,
              },
            }
            meta = newMeta
          }
          const packet = EncodedPacket.fromEncodedChunk(chunk)
          pendingPackets.push(videoSource.add(packet, meta))
        },
        error: (e) => console.error('Encoder Error:', e),
      })

      videoEncoder.configure({
        codec: 'avc1.64002a',
        width: CANVAS_WIDTH,
        height: CANVAS_HEIGHT,
        bitrate: 10_000_000,
        framerate: 30,
        latencyMode: 'quality',
      })

      // Render Loop
      const videoEl = document.createElement('video')
      videoEl.crossOrigin = 'anonymous'
      videoEl.muted = true
      let currentClipUrl = ''

      // Pre-load images
      const imageElements = new Map<string, HTMLImageElement>()
      for (const image of imageOverlays.value) {
        const img = new Image()
        img.crossOrigin = 'anonymous'
        img.src = image.url
        await new Promise((resolve) => {
          if (img.complete) {
            resolve(true)
          } else {
            img.onload = () => resolve(true)
            img.onerror = () => resolve(false)
          }
        })
        imageElements.set(image.id, img)
      }

      // Pre-load PiP videos
      const pipVideoElements = new Map<string, HTMLVideoElement>()
      for (const pipVideo of pipVideoOverlays.value) {
        const pipEl = document.createElement('video')
        pipEl.crossOrigin = 'anonymous'
        pipEl.src = pipVideo.url
        pipEl.muted = pipVideo.volume === 0
        await new Promise((resolve) => {
          pipEl.onloadedmetadata = () => resolve(true)
          pipEl.onerror = () => resolve(false)
        })
        pipVideoElements.set(pipVideo.id, pipEl)
      }

      // Frame rendering loop
      for (let i = 0; i < totalFrames; i++) {
        const time = i / fps

        // Find active clip
        let activeClip = null
        let localTime = 0
        let accumulated = 0
        for (const clip of clips.value) {
          if (time < accumulated + clip.duration) {
            activeClip = clip
            localTime = time - accumulated
            break
          }
          accumulated += clip.duration
        }

        // Render base video
        if (activeClip) {
          if (currentClipUrl !== activeClip.url) {
            videoEl.src = activeClip.url
            currentClipUrl = activeClip.url
            await new Promise((r) => {
              videoEl.onloadeddata = () => r(true)
            })
          }

          videoEl.currentTime = localTime
          await new Promise((r) => {
            if (videoEl.readyState >= 2) r(true)
            else videoEl.onseeked = () => r(true)
          })

          ctx.fillStyle = '#000'
          ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
          const scale = Math.max(
            CANVAS_WIDTH / videoEl.videoWidth,
            CANVAS_HEIGHT / videoEl.videoHeight,
          )
          const x = CANVAS_WIDTH / 2 - (videoEl.videoWidth / 2) * scale
          const y = CANVAS_HEIGHT / 2 - (videoEl.videoHeight / 2) * scale

          ctx.drawImage(videoEl, x, y, videoEl.videoWidth * scale, videoEl.videoHeight * scale)
        } else {
          ctx.fillStyle = '#000'
          ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
        }

        // Render text overlays
        const activeTexts = textOverlays.value.filter(
          (t) => time >= t.startTime && time <= t.endTime,
        )
        for (const text of activeTexts) {
          const state = calculateTextState(text, time)

          if (state.opacity <= 0.01) continue
          if (state.opacity > 0.5) ctx.shadowColor = `rgba(0,0,0,${0.8 * state.opacity})`

          ctx.save()
          const posX = (text.position.x / 100) * CANVAS_WIDTH
          const posY = (text.position.y / 100) * CANVAS_HEIGHT
          ctx.translate(posX, posY)
          ctx.translate(
            (state.translateX / 100) * CANVAS_WIDTH,
            (state.translateY / 100) * CANVAS_HEIGHT,
          )
          ctx.scale(state.scale, state.scale)

          ctx.globalAlpha = state.opacity
          ctx.font = `${text.fontWeight} ${text.fontSize}px Arial`
          ctx.fillStyle = text.color
          ctx.textAlign = 'center'
          ctx.textBaseline = 'middle'

          let content = text.content
          if (text.entryAnimation === 'typewriter') {
            const dur = text.entryDuration || 1
            const prog = Math.min(1, Math.max(0, (time - text.startTime) / dur))
            content = text.content.substring(0, Math.floor(text.content.length * prog))
          }
          ctx.fillText(content, 0, 0)
          ctx.restore()
        }

        // Render image overlays
        const activeImages = imageOverlays.value.filter(
          (img) => time >= img.startTime && time <= img.endTime,
        )
        for (const image of activeImages) {
          const state = calculateImageState(image, time)
          if (state.opacity <= 0.01) continue

          const img = imageElements.get(image.id)
          if (!img) continue

          ctx.save()
          const posX = (image.position.x / 100) * CANVAS_WIDTH
          const posY = (image.position.y / 100) * CANVAS_HEIGHT
          ctx.translate(posX, posY)
          ctx.translate((state.translateX / 100) * CANVAS_WIDTH, 0)
          ctx.rotate((image.rotation * Math.PI) / 180)
          ctx.scale(state.scale, state.scale)
          ctx.globalAlpha = state.opacity

          ctx.drawImage(img, -image.width / 2, -image.height / 2, image.width, image.height)
          ctx.restore()
        }

        // Render PiP video overlays
        const activePipVideos = pipVideoOverlays.value.filter(
          (vid) => time >= vid.startTime && time <= vid.endTime,
        )
        for (const pipVideo of activePipVideos) {
          const state = calculateImageState(pipVideo, time)
          if (state.opacity <= 0.01) continue

          const pipVideoEl = pipVideoElements.get(pipVideo.id)
          if (!pipVideoEl) continue

          ctx.save()
          const posX = (pipVideo.position.x / 100) * CANVAS_WIDTH
          const posY = (pipVideo.position.y / 100) * CANVAS_HEIGHT
          ctx.translate(posX, posY)
          ctx.translate((state.translateX / 100) * CANVAS_WIDTH, 0)
          ctx.rotate((pipVideo.rotation * Math.PI) / 180)
          ctx.scale(state.scale, state.scale)
          ctx.globalAlpha = state.opacity

          const localTime = time - pipVideo.startTime
          pipVideoEl.currentTime = Math.min(localTime, pipVideoEl.duration)

          if (Math.abs(pipVideoEl.currentTime - localTime) > 0.1) {
            await new Promise((resolve) => {
              pipVideoEl.onseeked = () => resolve(true)
              setTimeout(() => resolve(false), 100)
            })
          }

          // Draw with rounded corners
          const tempCanvas = document.createElement('canvas')
          tempCanvas.width = pipVideo.width
          tempCanvas.height = pipVideo.height
          const tempCtx = tempCanvas.getContext('2d')

          if (tempCtx) {
            const radius = 8
            tempCtx.beginPath()
            tempCtx.moveTo(radius, 0)
            tempCtx.lineTo(pipVideo.width - radius, 0)
            tempCtx.quadraticCurveTo(pipVideo.width, 0, pipVideo.width, radius)
            tempCtx.lineTo(pipVideo.width, pipVideo.height - radius)
            tempCtx.quadraticCurveTo(
              pipVideo.width,
              pipVideo.height,
              pipVideo.width - radius,
              pipVideo.height,
            )
            tempCtx.lineTo(radius, pipVideo.height)
            tempCtx.quadraticCurveTo(0, pipVideo.height, 0, pipVideo.height - radius)
            tempCtx.lineTo(0, radius)
            tempCtx.quadraticCurveTo(0, 0, radius, 0)
            tempCtx.closePath()
            tempCtx.clip()

            tempCtx.drawImage(pipVideoEl, 0, 0, pipVideo.width, pipVideo.height)
          }

          ctx.drawImage(
            tempCanvas,
            -pipVideo.width / 2,
            -pipVideo.height / 2,
            pipVideo.width,
            pipVideo.height,
          )
          ctx.restore()
        }

        const frame = new VideoFrame(canvas, { timestamp: time * 1_000_000, alpha: 'discard' })
        videoEncoder.encode(frame, { keyFrame: i % 30 === 0 })
        frame.close()

        renderProgress.value = Math.round((i / totalFrames) * 100)
        await new Promise((r) => setTimeout(r, 0))
      }

      await videoEncoder.flush()
      await Promise.all(pendingPackets)
      await output.finalize()

      const buffer = output.target.buffer
      if (!buffer) throw new Error('Błąd bufora')

      const blob = new Blob([buffer], { type: 'video/mp4' })
      const url = URL.createObjectURL(blob)

      const a = document.createElement('a')
      a.href = url
      a.download = `high-quality-${Date.now()}.mp4`
      a.click()

      // Cleanup
      videoEl.remove()
      pipVideoElements.forEach((pipEl) => pipEl.remove())
      pipVideoElements.clear()

      isRendering.value = false
      onDone(url)
    } catch (e) {
      console.error(e)
      alert('Błąd: ' + (e instanceof Error ? e.message : String(e)))
      isRendering.value = false
    }
  }

  return {
    isRendering,
    renderProgress,
    exportVideo,
  }
}
