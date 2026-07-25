import { fileURLToPath, URL } from 'node:url'
import { defineNuxtConfig } from 'nuxt/config'
import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2026-06-20',
  srcDir: 'src/',
  serverDir: 'src/server',
  ssr: true,

  routeRules: {
    '/**': { ssr: false },
  },

  imports: {
    dirs: [
      'composables',
      'composables/**',
    ]
  },

  // --- DODANE: Moduły Nuxt ---
  modules: [
    '@nuxtjs/apollo',
  ],

  // --- DODANE: Konfiguracja Apollo ---
  apollo: {
    clients: {
      default: {
        httpEndpoint: (process.env.VITE_API_URL || 'http://localhost:8080') + '/graphql'
      }
    }
  },

  css: [
    '~/assets/main.css',
  ],

  typescript: {
    strict: true,
    shim: false,
  },

  alias: {
    '@/stores/posts': fileURLToPath(new URL('./src/composables/useAppState.ts', import.meta.url)),
    '@/stores/reels': fileURLToPath(new URL('./src/composables/useAppState.ts', import.meta.url)),
    '@/stores/stories': fileURLToPath(new URL('./src/composables/useAppState.ts', import.meta.url)),
    '@/data/users': fileURLToPath(new URL('./src/utils/users.ts', import.meta.url)),
    '@': fileURLToPath(new URL('./src', import.meta.url)),
  },

  vite: {
    plugins: [tailwindcss()],
    server: {
      allowedHosts: true,
      hmr: {
        protocol: 'ws',
        host: 'localhost',
        port: 3000,
        clientPort: 3000,
      },
    },
    optimizeDeps: {
      exclude: ['@ffmpeg/ffmpeg', '@ffmpeg/util'],
    },
  },

  build: {
    transpile: ['@fingerprint/vue', 'emoji-mart-vue-fast'],
  },
})
