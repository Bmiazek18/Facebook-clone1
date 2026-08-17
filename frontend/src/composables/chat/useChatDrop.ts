import { ref, onMounted, onUnmounted } from 'vue'

export function useChatDrop(onDropCallback: (files: FileList) => void) {
  const isDragging = ref(false)
  let dragCounter = 0

  // Wykrywanie wdrożenia pliku nad obszarem przeglądarki
  const handleWindowDragEnter = (e: DragEvent) => {
    e.preventDefault()
    if (e.dataTransfer?.types?.includes('Files')) {
      dragCounter++
      isDragging.value = true
    }
  }

  const handleWindowDragOver = (e: DragEvent) => {
    e.preventDefault()
  }

  const handleWindowDragLeave = (e: DragEvent) => {
    e.preventDefault()
    if (e.dataTransfer?.types?.includes('Files')) {
      dragCounter--
      if (dragCounter <= 0) {
        reset()
      }
    }
  }

  // Anulowanie za pomocą klawisza ESC
  const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') reset()
  }

  const reset = () => {
    dragCounter = 0
    isDragging.value = false
  }

  // Wywoływane tylko, gdy plik zostanie upuszczony na konkretny czat
  const handleDrop = (e: DragEvent) => {
    e.preventDefault()
    reset()

    const files = e.dataTransfer?.files
    if (files && files.length > 0) {
      onDropCallback(files)
    }
  }

  onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
    window.addEventListener('dragenter', handleWindowDragEnter)
    window.addEventListener('dragover', handleWindowDragOver)
    window.addEventListener('dragleave', handleWindowDragLeave)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
    window.removeEventListener('dragenter', handleWindowDragEnter)
    window.removeEventListener('dragover', handleWindowDragOver)
    window.removeEventListener('dragleave', handleWindowDragLeave)
  })

  return {
    isDragging,
    reset,
    handleDrop,
  }
}
