import { ref, onMounted, onUnmounted, type Ref } from 'vue'

export interface UseVideoAutoplayOptions {
  threshold?: number
}

export function useVideoAutoplay(
  containerRef: Ref<HTMLElement | null>,
  options: UseVideoAutoplayOptions = {}
) {
  const { threshold = 0.5 } = options

  const isVisible = ref(false)
  let observer: IntersectionObserver | null = null

  const getVideoElement = (): HTMLVideoElement | null => {
    return containerRef.value?.querySelector('video') ?? null
  }

  const play = () => {
    const video = getVideoElement()
    video?.play().catch((err) => {
      console.debug('Autoplay prevented:', err.name)
    })
  }

  const pause = () => {
    const video = getVideoElement()
    video?.pause()
  }

  onMounted(() => {
    if (!containerRef.value) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          isVisible.value = entry.isIntersecting
          if (entry.isIntersecting) {
            play()
          } else {
            pause()
          }
        })
      },
      { threshold }
    )

    observer.observe(containerRef.value)
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  return {
    isVisible,
    play,
    pause
  }
}
