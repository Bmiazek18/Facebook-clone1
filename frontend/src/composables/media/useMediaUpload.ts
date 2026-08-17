import { type Ref } from 'vue'
import type {
  VideoClip,
  ImageOverlay,
  PipVideoOverlay,
  TextOverlay,
} from '@/types/video-editor.types'

export function useMediaUpload(
  clips: Ref<VideoClip[]>,
  imageOverlays: Ref<ImageOverlay[]>,
  pipVideoOverlays: Ref<PipVideoOverlay[]>,
  currentTime: Ref<number>,
  totalDuration: Ref<number>,
  selectedImage: Ref<ImageOverlay | null>,
  selectedPipVideo: Ref<PipVideoOverlay | null>,
  selectedText: Ref<TextOverlay | null>,
) {
  const handleAddVideo = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || !input.files[0]) return
    const file = input.files[0]
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.src = url
    video.crossOrigin = 'anonymous'
    video.muted = true

    await new Promise((resolve) => {
      video.onloadedmetadata = async () => {
        const canvas = document.createElement('canvas')
        canvas.width = 160
        canvas.height = 90
        const ctx = canvas.getContext('2d')
        const thumbnails: string[] = []

        if (ctx) {
          // Generate 20 thumbnail frames evenly distributed across video duration
          const numThumbnails = 20
          for (let i = 0; i < numThumbnails; i++) {
            const time = (video.duration / numThumbnails) * i
            video.currentTime = time
            await new Promise((r) => (video.onseeked = () => r(true)))
            ctx.drawImage(video, 0, 0, 160, 90)
            thumbnails.push(canvas.toDataURL('image/jpeg', 0.7))
          }
        }

        clips.value.push({
          id: `clip_${Date.now()}`,
          url,
          duration: video.duration,
          startTime: totalDuration.value,
          thumbnails: thumbnails.length > 0 ? thumbnails : [canvas.toDataURL('image/jpeg')],
        })
        resolve(true)
      }
    })
    input.value = ''
  }

  const handleAddImage = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || !input.files[0]) return
    const file = input.files[0]
    const url = URL.createObjectURL(file)

    // Load image to get actual dimensions
    const img = new Image()
    img.src = url

    await new Promise((resolve) => {
      img.onload = () => {
        const aspectRatio = img.width / img.height
        const width = 200
        const height = width / aspectRatio

        const newImage: ImageOverlay = {
          id: `image_${Date.now()}`,
          url,
          startTime: currentTime.value,
          endTime: Math.min(currentTime.value + 3, totalDuration.value),
          position: { x: 50, y: 50 },
          width,
          height,
          rotation: 0,
          opacity: 1,
          entryAnimation: 'none',

          exitAnimation: 'none',
        }

        imageOverlays.value.push(newImage)
        selectedImage.value = newImage
        selectedText.value = null
        resolve(true)
      }
    })

    input.value = ''
  }

  const handleAddPipVideo = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || !input.files[0]) return
    const file = input.files[0]
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.src = url
    video.crossOrigin = 'anonymous'
    video.muted = true

    await new Promise((resolve) => {
      video.onloadedmetadata = () => {
        const aspectRatio = video.videoWidth / video.videoHeight
        const width = 200
        const height = width / aspectRatio

        const newPipVideo: PipVideoOverlay = {
          id: `pipvideo_${Date.now()}`,
          url,
          startTime: currentTime.value,
          endTime: Math.min(currentTime.value + Math.min(video.duration, 5), totalDuration.value),
          position: { x: 75, y: 75 },
          width,
          height,
          rotation: 0,
          opacity: 1,
          volume: 0,
          entryAnimation: 'none',

          exitAnimation: 'none',
        }

        pipVideoOverlays.value.push(newPipVideo)
        selectedPipVideo.value = newPipVideo
        selectedText.value = null
        selectedImage.value = null
        resolve(true)
      }
    })
    input.value = ''
  }

  return {
    handleAddVideo,
    handleAddImage,
    handleAddPipVideo,
  }
}
