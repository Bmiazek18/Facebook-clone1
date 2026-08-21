import { defineNuxtPlugin, useRuntimeConfig } from '#app'
import { ofetch } from 'ofetch'
import { useAuthStore } from '@/stores/auth'

export default defineNuxtPlugin((nuxtApp) => {
  if (typeof window === 'undefined') return

  const config = useRuntimeConfig()

  // Apollo hook to attach Page Access Token to GraphQL operations when acting as Page
  nuxtApp.hook('apollo:auth' as any, ({ token }: any) => {
    const authStore = useAuthStore()
    if (authStore.isActingAsPage && authStore.activePageToken) {
      token.value = authStore.activePageToken
    }
  })

  // Override global $fetch with interceptors for Token Exchange & 401 errors
  globalThis.$fetch = ofetch.create({
    onRequest({ options }) {
      const authStore = useAuthStore()

      if (authStore.isActingAsPage && (authStore.activePageToken || authStore.activePageId)) {
        // Safe headers merging using standard Headers API
        const headers = new Headers(options.headers)
        if (authStore.activePageToken) {
          headers.set('Authorization', `Bearer ${authStore.activePageToken}`)
        }
        headers.set('X-Actor-Id', authStore.activePageId || '')
        headers.set('X-Page-Id', authStore.activePageId || '')
        headers.set('X-User-Id', authStore.activePageId || '')
        headers.set('X-Entity-Type', 'PAGE')

        options.headers = headers
      }
    },
    onResponseError({ response }) {
      // Check if response exists to prevent crash on network connection error
      if (response && response.status === 401) {
        console.warn('BFF Session expired (401). Redirecting to Keycloak login...')
        const keycloakLoginUrl =
          `${config.public.keycloakUrl}/realms/facebook-clone/protocol/openid-connect/auth?client_id=facebook-clone&redirect_uri=` +
          encodeURIComponent(`${config.public.frontendUrl}/api/auth/callback`) +
          '&response_type=code&scope=openid'
        window.location.href = keycloakLoginUrl
      }
    },
  }) as any
})

