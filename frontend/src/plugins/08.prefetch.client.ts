import { defineNuxtPlugin } from '#app'
import { useUserCache } from '@/composables/shared/useUserCache'

export default defineNuxtPlugin((nuxtApp) => {
  const { preloadUsers } = useUserCache()

  // Dyrektywa v-prefetch-user="userId | [userId1, userId2]"
  // Prefetchuje dane użytkownika do cache przy najechaniu kursorem (hover)
  nuxtApp.vueApp.directive('prefetch-user', {
    mounted(el: HTMLElement & { _prefetchClean?: () => void }, binding) {
      if (!binding.value) return

      let timer: any = null
      const onEnter = () => {
        timer = setTimeout(() => {
          const val = binding.value
          if (val !== undefined && val !== null && val !== '') {
            const ids = Array.isArray(val) ? val : [val]
            preloadUsers(ids)
          }
        }, 50)
      }

      const onLeave = () => {
        if (timer) {
          clearTimeout(timer)
          timer = null
        }
      }

      el.addEventListener('mouseenter', onEnter, { passive: true })
      el.addEventListener('pointerenter', onEnter, { passive: true })
      el.addEventListener('mouseleave', onLeave, { passive: true })
      el.addEventListener('pointerleave', onLeave, { passive: true })

      el._prefetchClean = () => {
        el.removeEventListener('mouseenter', onEnter)
        el.removeEventListener('pointerenter', onEnter)
        el.removeEventListener('mouseleave', onLeave)
        el.removeEventListener('pointerleave', onLeave)
        if (timer) clearTimeout(timer)
      }
    },
    updated(el: HTMLElement & { _prefetchClean?: () => void }, binding) {
      if (binding.value === binding.oldValue) return
      if (el._prefetchClean) {
        el._prefetchClean()
      }
      let timer: any = null
      const onEnter = () => {
        timer = setTimeout(() => {
          const val = binding.value
          if (val !== undefined && val !== null && val !== '') {
            const ids = Array.isArray(val) ? val : [val]
            preloadUsers(ids)
          }
        }, 50)
      }

      const onLeave = () => {
        if (timer) {
          clearTimeout(timer)
          timer = null
        }
      }

      el.addEventListener('mouseenter', onEnter, { passive: true })
      el.addEventListener('pointerenter', onEnter, { passive: true })
      el.addEventListener('mouseleave', onLeave, { passive: true })
      el.addEventListener('pointerleave', onLeave, { passive: true })

      el._prefetchClean = () => {
        el.removeEventListener('mouseenter', onEnter)
        el.removeEventListener('pointerenter', onEnter)
        el.removeEventListener('mouseleave', onLeave)
        el.removeEventListener('pointerleave', onLeave)
        if (timer) clearTimeout(timer)
      }
    },
    unmounted(el: HTMLElement & { _prefetchClean?: () => void }) {
      if (el._prefetchClean) {
        el._prefetchClean()
      }
    },
  })
})
