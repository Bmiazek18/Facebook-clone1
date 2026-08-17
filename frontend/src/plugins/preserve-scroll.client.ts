import { defineNuxtPlugin } from '#app'
import { useRouter } from '#imports'

export default defineNuxtPlugin((nuxtApp) => {
  const router = useRouter()
  // 1. USUNIĘTO: const route = useRoute() z tego miejsca.
  let savedScrollY = 0

  if (typeof window !== 'undefined' && 'scrollRestoration' in window.history) {
    try {
      window.history.scrollRestoration = 'manual'
    } catch (e) {
      // ignore
    }
  }

  router.beforeEach((to, from) => {
    if (to.meta?.keepScroll) {
      if (typeof window !== 'undefined') {
        const isSameUser = to.params.userId === from.params.userId
        if (!isSameUser) {
          savedScrollY = 0
        } else {
          savedScrollY = window.scrollY
        }
      }
    }
    return true
  })

  nuxtApp.hook('page:finish', () => {
    // 2. POPRAWKA: Bezpieczne pobieranie aktualnej ścieżki wewnątrz hooka
    const currentRoute = router.currentRoute.value

    if (currentRoute.meta?.keepScroll && typeof window !== 'undefined') {
      let attempts = 0
      const maxAttempts = 10 // Zwiększone z 5, żeby dać szansę wolniejszym API

      const restoreScroll = () => {
        try {
          window.scrollTo({ top: savedScrollY, behavior: 'instant' })

          // 3. POPRAWKA: Zwiększony interwał z 30ms na 50ms
          setTimeout(() => {
            if (Math.abs(window.scrollY - savedScrollY) > 10 && attempts < maxAttempts) {
              attempts++
              restoreScroll()
            }
          }, 50)
        } catch (e) {
          window.scrollTo(0, savedScrollY)
        }
      }

      setTimeout(() => {
        restoreScroll()
      }, 30)
    }
  })
})
