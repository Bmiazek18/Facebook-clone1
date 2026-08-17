import { ref, onMounted, onUnmounted, type Ref } from 'vue'

export function useStickySidebar(
  sectionRef: Ref<HTMLElement | null>,
  headerOffset: number,
  bottomOffset: number,
) {
  const stickyTop = ref(0)
  let lastScrollY = 0

  const updateStickyPosition = () => {
    if (!sectionRef.value) return

    const currentScrollY = window.scrollY
    const scrollDiff = currentScrollY - lastScrollY
    const viewportHeight = window.innerHeight
    const sidebarHeight = sectionRef.value.offsetHeight

    if (sidebarHeight + headerOffset + bottomOffset < viewportHeight) {
      stickyTop.value = headerOffset
      lastScrollY = currentScrollY
      return
    }

    let newTop = stickyTop.value - scrollDiff
    const maxTop = headerOffset
    const minTop = viewportHeight - sidebarHeight - bottomOffset

    if (newTop > maxTop) {
      newTop = maxTop
    } else if (newTop < minTop) {
      newTop = minTop
    }

    stickyTop.value = newTop
    lastScrollY = currentScrollY
  }

  let resizeObserver: ResizeObserver | null = null

  onMounted(() => {
    stickyTop.value = headerOffset
    lastScrollY = window.scrollY
    window.addEventListener('scroll', updateStickyPosition, { passive: true })
    window.addEventListener('resize', updateStickyPosition)

    if (sectionRef.value) {
      resizeObserver = new ResizeObserver(() => updateStickyPosition())
      resizeObserver.observe(sectionRef.value)
    }
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', updateStickyPosition)
    window.removeEventListener('resize', updateStickyPosition)
    if (resizeObserver) resizeObserver.disconnect()
  })

  return {
    stickyTop,
  }
}
