import { type Ref } from 'vue'

export function useFlipAnimation(containerRef: Ref<HTMLElement | null>) {
  // Mapa pozycji jest lokalna dla każdego boxa, aby uniknąć konfliktów między oknami czatu
  const avatarPositions = new Map<string, DOMRect>()

  const capturePositions = () => {
    if (!containerRef.value) return

    avatarPositions.clear()

    // Szukamy tylko wewnątrz konkretnego kontenera
    const elements = containerRef.value.querySelectorAll('[data-avatar-userid]')

    elements.forEach((el) => {
      const htmlEl = el as HTMLElement
      const userId = htmlEl.dataset.avatarUserid
      const rect = htmlEl.getBoundingClientRect()

      // Zapisujemy tylko elementy, które faktycznie mają wymiary (są w DOM)
      if (userId && rect.width > 0) {
        avatarPositions.set(userId, rect)
      }
    })
  }

  const onAvatarEnter = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement
    const userId = htmlEl.dataset.avatarUserid

    /**
     * Funkcja pomocnicza dla bezpiecznego pojawiania się (Fade In).
     * Używamy fill: 'both', aby stan opacity: 1 został zachowany do czasu done().
     */
    const fallbackFadeIn = () => {
      htmlEl.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 300, fill: 'both' }).onfinish =
        () => {
          htmlEl.style.opacity = ''
          done()
        }
    }

    // Jeśli nie znamy poprzedniej pozycji, po prostu płynnie pokaż
    if (!userId || !avatarPositions.has(userId)) {
      fallbackFadeIn()
      return
    }

    const oldPos = avatarPositions.get(userId)!
    const newRect = htmlEl.getBoundingClientRect()
    const containerRect = containerRef.value?.getBoundingClientRect()

    if (!containerRect) {
      done()
      return
    }

    // Sprawdzamy, czy stara pozycja była widoczna w obrębie kontenera (zapobiega skokom "znikąd")
    const isOldPosVisible =
      oldPos.top < containerRect.bottom &&
      oldPos.bottom > containerRect.top &&
      oldPos.left < containerRect.right &&
      oldPos.right > containerRect.left

    if (!isOldPosVisible) {
      fallbackFadeIn()
      return
    }

    const deltaX = oldPos.left - newRect.left
    const deltaY = oldPos.top - newRect.top

    // Jeśli zmiana pozycji jest pomijalna, nie animujemy (ochrona przed jitterem)
    if (Math.abs(deltaX) < 0.5 && Math.abs(deltaY) < 0.5) {
      done()
      return
    }

    // Wykonujemy animację FLIP
    requestAnimationFrame(() => {
      const animation = htmlEl.animate(
        [
          {
            transform: `translate(${deltaX}px, ${deltaY}px) scale(1.1)`,
            opacity: 0.5,
          },
          {
            transform: 'translate(0, 0) scale(1)',
            opacity: 1,
          },
        ],
        {
          duration: 600,
          easing: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
          fill: 'both', // Kluczowe: utrzymuje stan końcowy do momentu wyczyszczenia stylów
        },
      )

      animation.onfinish = () => {
        // Czyścimy style inline po zakończeniu animacji, pozwalając klasom CSS przejąć kontrolę
        htmlEl.style.transform = ''
        htmlEl.style.opacity = ''
        htmlEl.style.transition = ''
        done()
      }
    })
  }

  const onAvatarLeave = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement

    // Blokujemy interakcję podczas znikania
    htmlEl.style.pointerEvents = 'none'

    htmlEl.animate(
      [
        { opacity: 1, transform: 'scale(1)' },
        { opacity: 0, transform: 'scale(0.01)' },
      ],
      {
        duration: 300,
        easing: 'ease-in',
        fill: 'both',
      },
    ).onfinish = done
  }

  return { capturePositions, onAvatarEnter, onAvatarLeave }
}
