import {
  ApolloLink,
  Observable,
  type FetchResult,
  type Operation,
} from '@apollo/client'
import { print } from 'graphql'

interface ETagCacheEntry {
  etag: string
  data: any
  timestamp: number
  ttl: number
}

export interface ApolloETagLinkOptions {
  maxEntries?: number
  defaultTTLMs?: number
}

export function createApolloETagLink(options: ApolloETagLinkOptions = {}) {
  const maxEntries = options.maxEntries ?? 500
  const defaultTTL = options.defaultTTLMs ?? 5 * 60 * 1000 // 5 minutes
  const memoryCache = new Map<string, ETagCacheEntry>()

  const generateQueryKey = (operation: Operation): string => {
    const queryStr = print(operation.query)
    const varsStr = JSON.stringify(operation.variables || {})
    return `${operation.operationName || 'Anonymous'}_${queryStr}_${varsStr}`
  }

  const setCache = (key: string, etag: string, data: any) => {
    if (memoryCache.size >= maxEntries) {
      const firstKey = memoryCache.keys().next().value
      if (firstKey) memoryCache.delete(firstKey)
    }
    memoryCache.set(key, {
      etag,
      data,
      timestamp: Date.now(),
      ttl: defaultTTL,
    })
  }

  const invalidateOnMutation = (operation: Operation) => {
    const opName = (operation.operationName || '').toLowerCase()
    for (const key of memoryCache.keys()) {
      const lowerKey = key.toLowerCase()
      if (
        (opName.includes('post') && lowerKey.includes('post')) ||
        (opName.includes('comment') && (lowerKey.includes('comment') || lowerKey.includes('post'))) ||
        (opName.includes('user') && lowerKey.includes('user')) ||
        (opName.includes('group') && lowerKey.includes('group')) ||
        (opName.includes('friend') && lowerKey.includes('friend')) ||
        (opName.includes('message') && lowerKey.includes('chat')) ||
        (opName.includes('event') && lowerKey.includes('event')) ||
        (opName.includes('market') && lowerKey.includes('market'))
      ) {
        memoryCache.delete(key)
      }
    }
  }

  return new ApolloLink((operation, forward) => {
    const isQuery = operation.query.definitions.some(
      (def) => def.kind === 'OperationDefinition' && def.operation === 'query',
    )

    if (!isQuery) {
      invalidateOnMutation(operation)
      return forward(operation)
    }

    const queryKey = generateQueryKey(operation)
    const cached = memoryCache.get(queryKey)

    // If we have a valid, non-expired cached ETag, append If-None-Match header
    if (cached && Date.now() - cached.timestamp < cached.ttl) {
      operation.setContext(({ headers = {} }: { headers?: Record<string, any> }) => ({
        headers: {
          ...headers,
          'If-None-Match': cached.etag,
          'X-GraphQL-Operation': operation.operationName || 'UnnamedQuery',
        },
      }))
    }

    return new Observable<FetchResult>((observer) => {
      const subscription = forward(operation).subscribe({
        next: (result: FetchResult) => {
          const context = operation.getContext()
          const responseHeaders = context.response?.headers

          // Extract ETag from server response headers
          const etag =
            responseHeaders?.get?.('etag') ||
            responseHeaders?.get?.('ETag') ||
            responseHeaders?.get?.('x-etag')

          if (etag && result.data && !result.errors?.length) {
            setCache(queryKey, etag, result.data)
          }

          observer.next(result)
        },
        error: (networkError: any) => {
          const status =
            networkError?.statusCode ||
            networkError?.response?.status ||
            networkError?.result?.status ||
            networkError?.networkError?.statusCode

          // Transparently handle HTTP 304 Not Modified
          if (status === 304) {
            const cachedEntry = memoryCache.get(queryKey)
            if (cachedEntry && cachedEntry.data) {
              observer.next({ data: cachedEntry.data })
              observer.complete()
              return
            }
          }

          observer.error(networkError)
        },
        complete: () => observer.complete(),
      })

      return () => subscription.unsubscribe()
    })
  })
}
