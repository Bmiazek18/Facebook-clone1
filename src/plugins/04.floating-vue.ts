import FloatingVue from 'floating-vue'

export default defineNuxtPlugin((nuxtApp) => {
  if (import.meta.client) {
    FloatingVue.options.handleResize = false

    nuxtApp.vueApp.use(FloatingVue, {
      themes: {
        tooltip: {
          $gap: 14,
          placement: 'bottom',
          delay: { show: 500, hide: 0 },
        },
        dark: {
          $extend: 'tooltip',
        },
      },
      customContainers: ['.emoji-mart'],
    })
  } else {
    // SSR stub for directives registered by floating-vue to prevent compilation/rendering errors
    nuxtApp.vueApp.directive('tooltip', {})
    nuxtApp.vueApp.directive('close-popper', {})

    // SSR stub for components registered by floating-vue to prevent "Failed to resolve component" warnings
    const slotStub = {
      render(this: any) {
        return this.$slots.default ? this.$slots.default() : null
      }
    }
    nuxtApp.vueApp.component('VTooltip', slotStub)
    nuxtApp.vueApp.component('VDropdown', slotStub)
    nuxtApp.vueApp.component('VMenu', slotStub)
  }
})
