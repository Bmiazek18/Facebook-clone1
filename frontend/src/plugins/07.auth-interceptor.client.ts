import { defineNuxtPlugin, useRuntimeConfig } from '#app'
import { ofetch } from 'ofetch'

export default defineNuxtPlugin((nuxtApp) => {
  if (typeof window === 'undefined') return

  const config = useRuntimeConfig()

  // Apollo hook to attach Page Access Token to GraphQL operations when acting as Page
  nuxtApp.hook('apollo:auth' as any, ({ token }: any) => {
    const isActingAsPage = localStorage.getItem('auth-is-acting-as-page') === 'true'
    const pageToken = localStorage.getItem('auth-page-token')
    if (isActingAsPage && pageToken) {
      token.value = pageToken
    }
  })

  // Override global $fetch with interceptors for Token Exchange & 401 errors
  globalThis.$fetch = ofetch.create({
    onRequest({ options }) {
      const isActingAsPage = localStorage.getItem('auth-is-acting-as-page') === 'true'
      const pageToken = localStorage.getItem('auth-page-token')
      const pageId = localStorage.getItem('auth-active-page-id')

      if (isActingAsPage && (pageToken || pageId)) {
        options.headers = {
          ...options.headers,
          ...(pageToken ? { Authorization: `Bearer ${pageToken}` } : {}),
          'X-Actor-Id': pageId || '',
          'X-Page-Id': pageId || '',
          'X-User-Id': pageId || '',
          'X-Entity-Type': 'PAGE',
        }
      }
    },
    onResponseError({ response }) {
      if (response.status === 401) {
        console.warn('BFF Session expired (401). Redirecting to Keycloak login...')
        const keycloakLoginUrl =
          `${config.public.keycloakUrl}/realms/myrealm/protocol/openid-connect/auth?client_id=facebook-clone&redirect_uri=` +
          encodeURIComponent(`${config.public.frontendUrl}/api/auth/callback`) +
          '&response_type=code&scope=openid'
        window.location.href = keycloakLoginUrl
      }
    },
  })
})

