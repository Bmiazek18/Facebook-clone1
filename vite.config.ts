import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'
// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // Vue core
            if (id.includes('/vue/') || id.includes('/@vue/')) {
              return 'vue-core'
            }
            // Vue Router
            if (id.includes('vue-router')) {
              return 'vue-router'
            }
            // Pinia
            if (id.includes('pinia')) {
              return 'pinia'
            }
            // i18n
            if (id.includes('vue-i18n') || id.includes('@intlify')) {
              return 'i18n'
            }
            // Material Design Icons
            if (id.includes('vue-material-design-icons')) {
              return 'icons'
            }
            // Emoji Mart
            if (id.includes('emoji-mart')) {
              return 'emoji'
            }
            // Giphy
            if (id.includes('@giphy')) {
              return 'giphy'
            }
            // PrimeVue
            if (id.includes('primevue') || id.includes('@primeuix')) {
              return 'primevue'
            }
            // Utils
            if (id.includes('@vueuse') || id.includes('floating-vue')) {
              return 'utils'
            }
          }
        },
      },
    },
  },
})
