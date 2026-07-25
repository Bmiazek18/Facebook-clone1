import { defineNuxtPlugin } from '#app'
import { useRouter } from '#imports'

export default defineNuxtPlugin((nuxtApp) => {
  const router = useRouter()
  let savedScrollY = 0

  if (typeof window !== 'undefined' && 'scrollRestoration' in window.history) {
    try {
      window.history.scrollRestoration = 'manual'
    } catch (e) {}
  }

  // Zapisujemy aktualną wysokość przed każdą zmianą podstrony
  router.beforeEach((to, from) => {
    if (typeof window !== 'undefined') {
      const isSameUser = to.params.userId === from.params.userId
      if (!isSameUser) {
        savedScrollY = 0
      } else {
        savedScrollY = window.scrollY
      }
    }
    return true
  })

  nuxtApp.hook('page:finish', () => {
    const currentRoute = router.currentRoute.value

    if (typeof window !== 'undefined') {
      const historyPosition = (window as any).__fb_saved_history_scroll
      ;(window as any).__fb_saved_history_scroll = null

      let targetScrollY = 0

      if (historyPosition) {
        // SCENARIUSZ A: Kliknięto Wstecz/Dalej -> przywracamy pozycję z historii przeglądarki
        targetScrollY = historyPosition.top
      } else if (currentRoute.meta?.keepScroll) {
        // SCENARIUSZ B: Kliknięto przycisk/link ze strony z keepScroll -> zostajemy na tej samej wysokości
        targetScrollY = savedScrollY
      } else {
        // SCENARIUSZ C: Kliknięto normalny link bez keepScroll -> przewijamy na samą górę nowej strony
        targetScrollY = 0
      }

      // Bezpieczna pętla pilnująca, żeby strona nie uciekła podczas renderowania asynchronicznego
      let attempts = 0
      const maxAttempts = 10

      const restoreScroll = () => {
        try {
          window.scrollTo({ top: targetScrollY, behavior: 'instant' })

          setTimeout(() => {
            if (Math.abs(window.scrollY - targetScrollY) > 10 && attempts < maxAttempts) {
              attempts++
              restoreScroll()
            }
          }, 50)
        } catch (e) {
          window.scrollTo(0, targetScrollY)
        }
      }

      setTimeout(() => {
        restoreScroll()
      }, 30)
    }
  })
})
