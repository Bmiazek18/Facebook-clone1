import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick, type Ref } from 'vue'

export const useSlideTransition = () => {
  const wrapperRef = ref<HTMLElement | null>(null)
  const currentView: Ref<string> = ref('creator')
  const previousView: Ref<string | null> = ref(null)

  // Get active view element for height calculation
  const getActiveViewElement = (): HTMLElement | null => {
    if (!wrapperRef.value) return null
    return wrapperRef.value.querySelector<HTMLElement>('[data-view]') ?? null
  }

  // Update wrapper height based on active view
  const updateHeight = async () => {
    await nextTick()
    const active = getActiveViewElement()
    if (!wrapperRef.value) return

    if (active) {
      const height = active.scrollHeight
      wrapperRef.value.style.height = height + 'px'
    } else {
      wrapperRef.value.style.height = '0px'
    }
  }

  // ResizeObserver for responsive height updates
  let resizeObserver: ResizeObserver | null = null

  // Initialize observer and listeners
  const initializeObserver = () => {
    updateHeight()

    const active = getActiveViewElement()
    if (active && 'ResizeObserver' in window) {
      resizeObserver = new ResizeObserver(() => {
        updateHeight()
      })
      resizeObserver.observe(active)
    }

    window.addEventListener('resize', updateHeight)
  }

  // Cleanup observer and listeners
  const cleanupObserver = () => {
    window.removeEventListener('resize', updateHeight)
    if (resizeObserver) {
      resizeObserver.disconnect()
      resizeObserver = null
    }
  }

  // Update observer when view changes
  const updateObserver = () => {
    updateHeight()

    if (resizeObserver) {
      resizeObserver.disconnect()
      const active = getActiveViewElement()
      if (active && 'ResizeObserver' in window) {
        resizeObserver = new ResizeObserver(() => {
          updateHeight()
        })
        resizeObserver.observe(active)
      }
    }
  }

  // Setup lifecycle
  onMounted(() => {
    initializeObserver()
  })

  onBeforeUnmount(() => {
    cleanupObserver()
  })

  // Watch for view changes and update observer
  watch(currentView, () => {
    updateObserver()
  })

  // Calculate transition name based on view change direction
  const transitionName = computed(() => {
    // Going to privacy view from any view (forward)
    const isGoingForward = currentView.value === 'privacy' &&
                          (previousView.value === 'creator' || previousView.value === 'main')

    // Going back to creator/main from privacy (backward)
    const isGoingBackward = (currentView.value === 'creator' || currentView.value === 'main') &&
                           previousView.value === 'privacy'

    if (isGoingForward) {
      return 'slide-left' // main/creator -> privacy
    }
    if (isGoingBackward) {
      return 'slide-right' // privacy -> main/creator
    }
    return 'slide-left' // Default
  })

  return {
    wrapperRef,
    currentView,
    previousView,
    updateHeight,
    transitionName,
  }
}
