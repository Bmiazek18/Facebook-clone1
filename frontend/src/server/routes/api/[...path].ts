import { defineEventHandler, getCookie, proxyRequest } from 'h3'
import { getValidAccessToken } from '../../utils/session'

interface RouteRule {
  prefixes: string[]
  getTarget: () => string
  stripPrefix?: string
  noApiPrefix?: boolean
}

const ROUTE_RULES: RouteRule[] = [
  {
    prefixes: ['chat/'],
    getTarget: () => process.env.CHAT_SERVICE_URL || 'http://chat-service.apps.svc.cluster.local:8087',
  },
  {
    prefixes: ['notifications/'],
    getTarget: () => process.env.NOTIFICATION_SERVICE_URL || 'http://notificationservice.apps.svc.cluster.local:8083',
  },
  {
    prefixes: ['analytics/'],
    getTarget: () => process.env.ANALYTICS_SERVICE_URL || 'http://analytics-service.apps.svc.cluster.local:8095',
  },
  {
    prefixes: ['search/'],
    getTarget: () => process.env.SEARCH_SERVICE_URL || 'http://search-service.apps.svc.cluster.local:8088',
  },
  {
    prefixes: ['v1/listings', 'marketplace/'],
    getTarget: () => process.env.MARKETPLACE_SERVICE_URL || 'http://marketplace-service.apps.svc.cluster.local:8090',
  },
  {
    prefixes: ['users/', 'user/'],
    getTarget: () => process.env.USER_SERVICE_URL || 'http://userservice.apps.svc.cluster.local:8081',
  },
  {
    prefixes: ['linkguard/'],
    getTarget: () => process.env.LINKGUARD_SERVICE_URL || 'http://linkguard-service.apps.svc.cluster.local:8086',
  },
  {
    prefixes: ['meta-ai/', 'chat-threads', 'chat-history', 'process-chat', 'generated_charts'],
    getTarget: () => process.env.META_AI_SERVICE_URL || 'http://meta-ai-service.apps.svc.cluster.local:8000',
    stripPrefix: 'meta-ai/',
    noApiPrefix: true,
  },
]

const LOCAL_HANDLER_PREFIXES = ['auth/', 'hsm/']

export default defineEventHandler(async (event) => {
  const path = event.context.params?.path || ''

  // Skip proxying for local Nitro handlers (auth + OPAQUE HSM vault)
  if (LOCAL_HANDLER_PREFIXES.some(prefix => path.startsWith(prefix))) {
    return
  }

  // 1. Resolve Access Token (Session Cookie or Authorization Header)
  let accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    const authHeader = event.node.req.headers['authorization']
    if (authHeader && authHeader.startsWith('Bearer ')) {
      accessToken = authHeader.substring(7)
    } else {
      accessToken = getCookie(event, 'jwt_token') || ''
    }
  }

  if (!accessToken) {
    event.node.res.statusCode = 401
    event.node.res.setHeader('Content-Type', 'application/json')
    return { error: 'Unauthorized', message: 'Session expired or invalid.' }
  }

  // 2. Propagate Identity & Context Headers (BFF Token Mediation)
  event.node.req.headers['authorization'] = `Bearer ${accessToken}`
  try {
    const payloadPart = accessToken.split('.')[1]
    if (payloadPart) {
      const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
      if (payload?.sub) {
        event.node.req.headers['x-user-id'] = payload.sub
      }
    }
  } catch (err) {
    console.warn('BFF: Failed to parse JWT for X-User-Id:', err)
  }

  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''

  // 3. Resolve Target URL via Declarative Route Table
  const globalGateway = process.env.API_GATEWAY_URL || process.env.INTERNAL_API_URL
  if (globalGateway) {
    const base = globalGateway.replace(/\/+$/, '')
    return proxyRequest(event, `${base}/api/${path}${queryString}`)
  }

  const matchedRule = ROUTE_RULES.find(rule => rule.prefixes.some(prefix => path.startsWith(prefix)))
  const targetBase = (matchedRule ? matchedRule.getTarget() : (process.env.NUXT_PUBLIC_API_URL || 'http://localhost:8080')).replace(/\/+$/, '')

  let subPath = path
  if (matchedRule?.stripPrefix && subPath.startsWith(matchedRule.stripPrefix)) {
    subPath = subPath.slice(matchedRule.stripPrefix.length)
  }

  const targetUrl = matchedRule?.noApiPrefix
    ? `${targetBase}/${subPath}${queryString}`
    : `${targetBase}/api/${subPath}${queryString}`

  return proxyRequest(event, targetUrl)
})
