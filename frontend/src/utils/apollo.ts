import { useApolloClient } from '@vue/apollo-composable'
import type { ApolloClient, NormalizedCacheObject, DocumentNode, OperationVariables } from '@apollo/client'

let _cachedClient: ApolloClient<NormalizedCacheObject> | null = null

export function setGlobalApolloClient(client: ApolloClient<NormalizedCacheObject>) {
  if (client) {
    _cachedClient = client
  }
}

export function getApolloClient(): ApolloClient<NormalizedCacheObject> {
  if (_cachedClient) {
    return _cachedClient
  }

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
      _cachedClient = client
      return client
    }
  } catch (e) {
    // ignore
  }

  try {
    const apollo = useApolloClient()
    const client = apollo.resolveClient()
    if (client) {
      _cachedClient = client
      return client
    }
  } catch (e) {
    // ignore
  }

  throw new Error('Apollo Client is not available')
}

export function isMicroserviceUnavailable(error: any): boolean {
  if (!error) return false
  const msg = String(error.message || error).toLowerCase()
  return (
    msg.includes('unavailable') ||
    msg.includes('service unavailable') ||
    msg.includes('io exception') ||
    msg.includes('econnrefused') ||
    msg.includes('503') ||
    msg.includes('502') ||
    msg.includes('failed to fetch') ||
    msg.includes('network error')
  )
}

/**
 * Bezpieczne wywołanie zapytania GraphQL (domyślnie network-only z graceful degradation dla niedostępnych mikroserwisów)
 */
export async function gqlQuery<T = any, V extends OperationVariables = OperationVariables>(
  query: DocumentNode,
  variables?: V,
  options?: {
    fetchPolicy?: 'network-only' | 'cache-first' | 'no-cache'
    errorPolicy?: 'none' | 'ignore' | 'all'
    graceful?: boolean
  }
): Promise<T | undefined> {
  try {
    const client = getApolloClient()
    const res = await client.query<T, V>({
      query,
      variables: (variables || {}) as V,
      fetchPolicy: options?.fetchPolicy || 'network-only',
      errorPolicy: options?.errorPolicy || 'all',
    })
    return res.data
  } catch (err: any) {
    if (options?.graceful !== false && isMicroserviceUnavailable(err)) {
      console.warn('[Microservice Warning] Backend service temporarily unavailable:', err?.message || err)
      return undefined
    }
    throw err
  }
}

/**
 * Bezpieczne wywołanie mutacji GraphQL
 */
export async function gqlMutate<T = any, V extends OperationVariables = OperationVariables>(
  mutation: DocumentNode,
  variables?: V,
  options?: {
    update?: any
    errorPolicy?: 'none' | 'ignore' | 'all'
    graceful?: boolean
  }
): Promise<T | undefined> {
  try {
    const client = getApolloClient()
    const res = await client.mutate<T, V>({
      mutation,
      variables: (variables || {}) as V,
      update: options?.update,
      errorPolicy: options?.errorPolicy || 'all',
    })
    return res.data ?? undefined
  } catch (err: any) {
    if (options?.graceful !== false && isMicroserviceUnavailable(err)) {
      console.warn('[Microservice Warning] Mutation failed - service unavailable:', err?.message || err)
      return undefined
    }
    throw err
  }
}
