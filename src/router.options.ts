// Custom router options for Nuxt/Vue Router
// Prevent scrolling to top when navigating between profile tabs
import type { RouterConfig } from '@nuxt/schema'

export default <RouterConfig>{
  scrollBehavior(to, from, savedPosition) {
    if (typeof window !== 'undefined') {
      ;(window as any).__fb_saved_history_scroll = savedPosition
    }

    // Zwracamy false. Mówimy Nuxtowi: "Zostaw scroll w spokoju, plugin się tym zajmie"
    return false
  },
}
