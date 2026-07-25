import { ref } from 'vue'

// Store a requested Y position to preserve for the next navigation
const preserveY = ref<number | null>(null)

export function setPreserveNextScroll(value = true) {
  if (value && typeof window !== 'undefined') {
    preserveY.value = window.scrollY || 0
  } else {
    preserveY.value = null
  }
}

// Peek the stored Y position without resetting
export function peekPreserveNextScroll(): number | null {
  return preserveY.value
}

// Consume and reset the stored Y position (returns number or null)
export function consumePreserveNextScroll(): number | null {
  const v = preserveY.value
  preserveY.value = null
  return v
}
