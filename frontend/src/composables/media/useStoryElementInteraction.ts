import { ref, reactive, onUnmounted, type Ref } from 'vue'
import type { StoryElement as StoryElementType } from '@/types/StoryElement'
import { calculateSnaps, type Guide } from '@/utils/snapping'

export function useStoryElementInteraction(
  storyElements: Ref<StoryElementType[]>,
  bgDimensions: { width: number; height: number },
) {
  const activeDragId = ref<string | null>(null)
  const activeResizeId = ref<string | null>(null)
  const activeRotateId = ref<string | null>(null)
  const activeScaleId = ref<string | null>(null)
  const editingId = ref<string | null>(null)
  const croppingId = ref<string | null>(null)

  const dragStart = reactive({ x: 0, y: 0 })
  const elementStart = reactive({
    x: 0,
    y: 0,
    w: 0,
    h: 0,
    rotation: 0,
    cropX: 0,
    cropY: 0,
    scale: 1,
    // Dodatkowe stany do zaawansowanego skalowania i rotacji
    dirX: 1,
    dirY: 1,
    centerX: 0,
    centerY: 0,
    initialAngle: 0
  })

  const activeGuides = ref<Guide[]>([])
  const SNAP_THRESHOLD = 12
  const selectedElementId = ref<string | null>(null)

  const startDrag = (event: MouseEvent, element: StoryElementType) => {
    if (editingId.value && editingId.value !== element.id) {
      disableEdit()
    }
    selectedElementId.value = element.id

    if (editingId.value === element.id || activeResizeId.value || activeRotateId.value || activeScaleId.value) return

    if (croppingId.value === element.id && element.type === 'image') {
      activeDragId.value = 'CROP_MOVE'
      dragStart.x = event.clientX
      dragStart.y = event.clientY
      elementStart.cropX = element.cropX || 0
      elementStart.cropY = element.cropY || 0
    } else {
      if (croppingId.value) return
      activeDragId.value = element.id
      dragStart.x = event.clientX
      dragStart.y = event.clientY
      elementStart.x = element.x
      elementStart.y = element.y
      const target = event.currentTarget as HTMLElement
      elementStart.w = target.offsetWidth
      elementStart.h = target.offsetHeight
      element.width = target.offsetWidth
      element.height = target.offsetHeight
    }
    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', stopInteraction)
  }

  const startResize = (event: MouseEvent, element: StoryElementType) => {
    if (element.type === 'image' && !element.musicTitle) return
    event.stopPropagation()
    event.preventDefault()
    activeResizeId.value = element.id
    selectedElementId.value = element.id
    dragStart.x = event.clientX
    elementStart.w = element.width || 200
    elementStart.h = element.height || 200
    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', stopInteraction)
  }

  const startScale = (event: MouseEvent, element: StoryElementType) => {
    event.stopPropagation()
    event.preventDefault()
    activeScaleId.value = element.id
    selectedElementId.value = element.id
    dragStart.x = event.clientX
    dragStart.y = event.clientY
    elementStart.scale = element.scale ?? 1

    // Pobieramy kliknięty element (uchwyt) i sprawdzamy jego klasy,
    // aby wiedzieć, w którym rogu się znajdujemy (-1 dla lewej/górnej krawędzi, 1 dla prawej/dolnej)
    const target = event.target as HTMLElement
    elementStart.dirX = target.className.includes('-left-') ? -1 : 1
    elementStart.dirY = target.className.includes('-top-') ? -1 : 1

    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', stopInteraction)
  }

  const startRotate = (event: MouseEvent, element: StoryElementType) => {
    event.stopPropagation()
    event.preventDefault()
    activeRotateId.value = element.id
    selectedElementId.value = element.id

    // Próbujemy wyznaczyć środek elementu na ekranie, by obrót był płynny
    const target = (event.target as HTMLElement).closest('.group') as HTMLElement
    if (target) {
      const rect = target.getBoundingClientRect()
      elementStart.centerX = rect.left + rect.width / 2
      elementStart.centerY = rect.top + rect.height / 2
    } else {
      elementStart.centerX = event.clientX
      elementStart.centerY = event.clientY
    }

    // Obliczamy początkowy kąt kliknięcia względem środka
    elementStart.initialAngle = Math.atan2(event.clientY - elementStart.centerY, event.clientX - elementStart.centerX) * (180 / Math.PI)
    elementStart.rotation = element.rotation || 0

    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', stopInteraction)
  }

  const onMouseMove = (event: MouseEvent) => {
    // ---- PRZESUWANIE CROP'A ----
    if (activeDragId.value === 'CROP_MOVE' && croppingId.value) {
      const element = storyElements.value.find((el: StoryElementType) => el.id === croppingId.value)
      if (element && element.type === 'image') {
        element.cropX = elementStart.cropX + (event.clientX - dragStart.x)
        element.cropY = elementStart.cropY + (event.clientY - dragStart.y)
      }
      return
    }

    // ---- PRZESUWANIE ELEMENTU ----
    if (activeDragId.value && activeDragId.value !== 'CROP_MOVE') {
      const element = storyElements.value.find((el: StoryElementType) => el.id === activeDragId.value)
      if (element) {
        let newX = elementStart.x + (event.clientX - dragStart.x)
        let newY = elementStart.y + (event.clientY - dragStart.y)

        const elementWithDimensions = {
          ...element,
          width: elementStart.w || element.width || 100,
          height: elementStart.h || element.height || 100,
        }

        const { snappedX, snappedY, guides } = calculateSnaps(
          elementWithDimensions,
          newX,
          newY,
          storyElements.value.map((el: StoryElementType) => ({
            ...el,
            width: el.width || 100,
            height: el.height || 100,
          })),
          {
            threshold: SNAP_THRESHOLD,
            canvasWidth: bgDimensions.width,
            canvasHeight: bgDimensions.height,
          },
        )
        activeGuides.value = guides

        if (element.type === 'text' || (element.type === 'image' && element.musicArtist)) {
          const mainImage = storyElements.value.find((el: StoryElementType) => el.id === 'main-image')
          if (mainImage) {
            const minX = mainImage.x
            const maxX = mainImage.x + mainImage.width - elementStart.w
            newX = Math.max(minX, Math.min(newX, maxX))

            const minY = 0
            const maxY = Math.max(minY, mainImage.y - elementStart.h)
            newY = Math.max(minY, Math.min(newY, maxY))
          } else {
            if (newX < 0) newX = 0
            else if (newX + elementStart.w > bgDimensions.width) newX = bgDimensions.width - elementStart.w
            if (newY < 0) newY = 0
            else if (newY + elementStart.h > bgDimensions.height) newY = bgDimensions.height - elementStart.h
          }
        } else {
          newX = snappedX
          newY = snappedY
        }
        element.x = newX
        element.y = newY
      }
    }

    // ---- ZMIANA SZEROKOŚCI (RESIZE) ----
    if (activeResizeId.value) {
      const element = storyElements.value.find((el: StoryElementType) => el.id === activeResizeId.value)
      if (element) {
        const newWidth = Math.max(50, elementStart.w + (event.clientX - dragStart.x))
        const ratio = elementStart.w / (elementStart.h || 1)
        element.width = newWidth
        if (element.height) {
          element.height = newWidth / ratio
        }
        element.scale = 1
      }
    }

    // ---- SWOBODNY OBRÓT (ROTATE) ----
    if (activeRotateId.value) {
      const element = storyElements.value.find((el: StoryElementType) => el.id === activeRotateId.value)
      if (element) {
        // Wyliczamy obecny kąt myszy i porównujemy go z kątem początkowym z momentu kliknięcia
        const currentAngle = Math.atan2(event.clientY - elementStart.centerY, event.clientX - elementStart.centerX) * (180 / Math.PI)
        element.rotation = elementStart.rotation + (currentAngle - elementStart.initialAngle)
      }
    }

    // ---- SKALOWANIE (SCALE - ZA ROGI) ----
    if (activeScaleId.value) {
      const element = storyElements.value.find((el: StoryElementType) => el.id === activeScaleId.value)
      if (element) {
        // Dzięki uwzględnieniu dirX i dirY kierunek "na zewnątrz" niezależnie od rogu daje wektor dodatni,
        // a "do wewnątrz" - wektor ujemny.
        const deltaX = (event.clientX - dragStart.x) * elementStart.dirX
        const deltaY = (event.clientY - dragStart.y) * elementStart.dirY

        const scaleDelta = (deltaX + deltaY) * 0.005
        element.scale = Math.max(0.1, elementStart.scale + scaleDelta)
      }
    }
  }

  const stopInteraction = () => {
    activeDragId.value = null
    activeResizeId.value = null
    activeRotateId.value = null
    activeScaleId.value = null
    activeGuides.value = []
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', stopInteraction)
  }

  const enableEdit = (id: string) => {
    editingId.value = id
    activeDragId.value = null
  }

  const disableEdit = () => {
    editingId.value = null
  }

  const onBackgroundClick = () => {
    disableEdit()
    croppingId.value = null
    selectedElementId.value = null
  }

  const toggleCrop = (id: string) => {
    if (croppingId.value === id) croppingId.value = null
    else {
      croppingId.value = id
      editingId.value = null
      selectedElementId.value = id
    }
  }

  // Funkcja obrotu sztywnego o 90 stopni (np. podpięta pod przycisk na toolbarze)
  const rotateElement90 = () => {
    const selectedElement = storyElements.value.find(
      (el: StoryElementType) => el.id === selectedElementId.value,
    )
    if (selectedElement) selectedElement.rotation = (selectedElement.rotation + 90) % 360
  }

  // Sprzątanie procesów na wypadek zniszczenia komponentu
  onUnmounted(() => {
    stopInteraction()
  })

  return {
    activeDragId,
    activeResizeId,
    activeRotateId,
    activeScaleId,
    editingId,
    croppingId,
    dragStart,
    elementStart,
    activeGuides,
    selectedElementId,
    startDrag,
    startResize,
    startScale,
    startRotate, // Wyeksportowana funkcja do swobodnego obracania myszką
    onMouseMove,
    stopInteraction,
    enableEdit,
    disableEdit,
    onBackgroundClick,
    toggleCrop,
    rotateElement90,
  }
}
