import { defineNuxtRouteMiddleware, navigateTo, useCookie, useRuntimeConfig } from '#imports'

export default defineNuxtRouteMiddleware((to) => {
  const token = useCookie('jwt_token')
  const isAuthenticated = !!token.value
  const config = useRuntimeConfig()

  const authPaths = ['/login', '/register', '/identify', '/confirmemail']
  const isAuthPath = authPaths.includes(to.path) || to.path.startsWith('/recover')

  if (!isAuthenticated && !isAuthPath) {
    // Redirect to Keycloak login page
    const keycloakLoginUrl = `${config.public.keycloakUrl}/realms/myrealm/protocol/openid-connect/auth?client_id=facebook-clone&redirect_uri=` + encodeURIComponent(`${config.public.frontendUrl}/api/auth/callback`) + '&response_type=code&scope=openid'
    return navigateTo(keycloakLoginUrl, { external: true })
  }

  if (isAuthenticated && isAuthPath) {
    // Redirect to / (home)
    return navigateTo('/')
  }
})
