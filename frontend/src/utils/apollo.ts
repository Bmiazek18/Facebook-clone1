import { useApolloClient } from '@vue/apollo-composable'
import type { ApolloClient, NormalizedCacheObject } from '@apollo/client'

export function getApolloClient(): ApolloClient<NormalizedCacheObject> {
  try {
    const nuxtApp = useNuxtApp()
    const client =
      (nuxtApp.$apollo as any)?.defaultClient ||
      (nuxtApp.$apollo as any)?.clients?.default ||
      nuxtApp.$apollo?.default ||
      (nuxtApp as any)._apolloClients?.default ||
      (nuxtApp.vueApp as any)?._context?.provides?.ApolloClients?.default ||
      (nuxtApp.vueApp as any)?.config?.globalProperties?.$apolloProvider?.defaultClient
    if (client) {
      return client
    }
  } catch (e) {
    // ignore
  }

  try {
    const apollo = useApolloClient()
    const client = apollo.resolveClient()
    if (client) {
      return client
    }
  } catch (e) {
    // ignore
  }

  throw new Error('Apollo Client is not available')
}
