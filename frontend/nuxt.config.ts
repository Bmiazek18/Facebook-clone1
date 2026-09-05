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
      otelEndpoint: process.env.NUXT_PUBLIC_OTEL_ENDPOINT || '',
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
      default: {
        httpEndpoint: process.env.NUXT_PUBLIC_API_URL ? `${process.env.NUXT_PUBLIC_API_URL}/graphql` : 'https://api.lab-bm.com/graphql',
        browserHttpEndpoint: process.env.NUXT_PUBLIC_API_URL ? `${process.env.NUXT_PUBLIC_API_URL}/graphql` : 'https://api.lab-bm.com/graphql',
        tokenStorage: 'cookie',
        tokenName: 'jwt_token',
        authType: 'Bearer',
        authHeader: 'Authorization',
      },
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
