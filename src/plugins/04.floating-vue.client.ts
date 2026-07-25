import FloatingVue from 'floating-vue'

export default defineNuxtPlugin((nuxtApp) => {
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
})
