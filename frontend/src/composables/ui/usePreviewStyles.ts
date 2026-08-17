import { type Ref } from 'vue'
import { calculateTextState, calculateImageState } from '@/composables/media/useVideoExport'
import type { TextOverlay, ImageOverlay, PipVideoOverlay } from '@/types/video-editor.types'

const PREVIEW_SCALE = 360 / 1080 // Preview is 360px wide vs 1080px canvas

export function usePreviewStyles(currentTime: Ref<number>) {
  const getTextStyle = (text: TextOverlay) => {
    const state = calculateTextState(text, currentTime.value)

    return {
      left: text.position.x + '%',
      top: text.position.y + '%',
      fontSize: text.fontSize * PREVIEW_SCALE + 'px',
      color: text.color,
      fontWeight: text.fontWeight,
      opacity: state.opacity,
      transform: `translate(-50%, -50%) translate(${state.translateX}%, ${state.translateY}%) scale(${state.scale})`,
      whiteSpace: 'pre-wrap' as const,
      textShadow: '2px 2px 4px rgba(0,0,0,0.8)',
    }
  }

  const getTextContent = (text: TextOverlay) => {
    if (text.entryAnimation === 'typewriter') {
      const duration = text.entryDuration || 1
      const progress = Math.min(1, Math.max(0, (currentTime.value - text.startTime) / duration))
      const charCount = Math.floor(text.content.length * progress)
      return text.content.substring(0, charCount)
    }
    return text.content
  }

  const getImageStyle = (image: ImageOverlay) => {
    const state = calculateImageState(image, currentTime.value)

    return {
      left: image.position.x + '%',
      top: image.position.y + '%',
      transform: `translate(-50%, -50%) scale(${state.scale}) translateX(${state.translateX}%) rotate(${image.rotation}deg)`,
      width: image.width * PREVIEW_SCALE + 'px',
      height: image.height * PREVIEW_SCALE + 'px',
      opacity: state.opacity,
      transition: 'none',
    }
  }

  const getPipVideoStyle = (video: PipVideoOverlay) => {
    const state = calculateImageState(video, currentTime.value)

    return {
      left: video.position.x + '%',
      top: video.position.y + '%',
      transform: `translate(-50%, -50%) scale(${state.scale}) translateX(${state.translateX}%) rotate(${video.rotation}deg)`,
      width: video.width * PREVIEW_SCALE + 'px',
      height: video.height * PREVIEW_SCALE + 'px',
      opacity: state.opacity,
      transition: 'none',
      borderRadius: '8px',
    }
  }

  return {
    getTextStyle,
    getTextContent,
    getImageStyle,
    getPipVideoStyle,
  }
}
