import { fileURLToPath } from 'node:url'
import { defineNuxtConfig } from 'nuxt/config'
import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2026-06-20',
  srcDir: 'src/',
  serverDir: 'src/server',
  ssr: false,

  runtimeConfig: {
    public: {
      keycloakUrl: process.env.NUXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8089',
      frontendUrl: process.env.NUXT_PUBLIC_FRONTEND_URL || 'http://localhost:3000',
      apiUrl: process.env.NUXT_PUBLIC_API_URL || 'http://localhost:8080',
      mqttUrl: process.env.NUXT_PUBLIC_MQTT_URL || 'ws://localhost:8080/mqtt',
      storageUrl: process.env.NUXT_PUBLIC_STORAGE_URL || 'http://localhost:9000',
      sentryDsn: process.env.NUXT_PUBLIC_SENTRY_DSN || '',
      otelEndpoint: process.env.NUXT_PUBLIC_OTEL_ENDPOINT || 'http://localhost:4318/v1/traces',
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
    '@nuxt/icon',
  ],

 apollo: {
  clients: {
    default: '~/apollo/default.ts',
  },
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
      tailwindcss(), // Tylko Tailwind
    ],

    server: {
      allowedHosts: true,
      ws: {
        protocol: 'ws',
        host: 'localhost',
        port: 3000,
        clientPort: 3000,
      },
    },
  },

  build: {
    transpile: [
      '@fingerprint/vue',
      'emoji-mart-vue-fast',
      'floating-vue',

    ],
  },
})
