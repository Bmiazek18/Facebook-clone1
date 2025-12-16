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
          // 1. Zgrupuj rdzeń frameworka (są małe i zawsze potrzebne razem)
          if (
            id.includes('/vue/') ||
            id.includes('/@vue/') ||
            id.includes('vue-router') ||
            id.includes('pinia') ||
            id.includes('vue-i18n') ||
            id.includes('@intlify')
          ) {
            return 'framework';
          }

          
          if (id.includes('vue-material-design-icons')) {
            return 'icons';
          }

      
          if (id.includes('emoji-mart')) {
            return 'emoji';
          }
          if (id.includes('@giphy')) {
            return 'giphy';
          }

         
        }
      },
    },
  },
},
})
