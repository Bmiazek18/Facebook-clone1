import { fileURLToPath, URL } from 'node:url'
import { defineNuxtConfig } from 'nuxt/config'
import tailwindcss from '@tailwindcss/vite'
import wasm from 'vite-plugin-wasm'
import topLevelAwait from 'vite-plugin-top-level-await'
import { nodePolyfills } from 'vite-plugin-node-polyfills'

export default defineNuxtConfig({
  compatibilityDate: '2026-06-20',
  srcDir: 'src/',
  serverDir: 'src/server',
  ssr: false,

  runtimeConfig: {
    public: {
      keycloakUrl: 'http://localhost:8089',
      frontendUrl: 'http://localhost:3000',
      apiUrl: 'http://localhost:8080',
      mqttUrl: 'ws://localhost:8080/mqtt',
      storageUrl: 'http://localhost:9000',
    }
  },

  spaLoadingTemplate: './spa-loading-template.html',
  imports: {
    dirs: [
      'composables',
      'composables/**',
    ]
  },

  modules: [
    '@nuxtjs/apollo',
  ],

  apollo: {
    clients: {
      default: {
        httpEndpoint: (process.env.NUXT_PUBLIC_API_URL || process.env.VITE_API_URL || 'http://localhost:8080') + '/graphql'
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
    build: {
      target: 'es2022',
    },
    plugins: [
      tailwindcss(),
      wasm(),
      topLevelAwait(),
      ...(function() {
        const polyfills = nodePolyfills({
          exclude: ['module'],
          globals: {
            Buffer: true,
            global: true,
            process: true,
          },
        })
        const applyFilter = (plugin: any) => {
          if (!plugin) return plugin
          const originalApply = plugin.apply
          plugin.apply = (config: any, env: any) => {
            if (env?.ssrBuild) return false
            if (typeof originalApply === 'function') return originalApply(config, env)
            if (typeof originalApply === 'string') return originalApply === env.command
            return true
          }
          return plugin
        }
        return Array.isArray(polyfills) ? polyfills.map(applyFilter) : [applyFilter(polyfills)]
      })(),
    ],

    define: {
      'process.env': {},
      'process.versions': JSON.stringify({}),
      global: 'window',
    },

    server: {
      allowedHosts: true,
      ws: {
        protocol: 'ws',
        host: 'localhost',
        port: 3000,
        clientPort: 3000,
      },
    },

    optimizeDeps: {
      // KLUCZOWE: Wykluczamy libsignal-client z domyślnego cachowania esbuild,
      // żeby wtyczki WASM mogły przetworzyć go dynamicznie w przeglądarce
      exclude: ['@signalapp/libsignal-client', '@ffmpeg/ffmpeg', '@ffmpeg/util'],
    },
  },

  build: {
    transpile: [
      '@fingerprint/vue',
      'emoji-mart-vue-fast',
      '@signalapp/libsignal-client',
      'floating-vue',
      // Dodane pakiety FullCalendar w celu naprawienia błędu z eksportami w Vite
      '@fullcalendar/core',
      '@fullcalendar/vue3',
      '@fullcalendar/timegrid',
      '@fullcalendar/interaction'
    ],
  },
})
