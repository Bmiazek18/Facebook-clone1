import { defineEventHandler, getCookie, proxyRequest } from 'h3'
import { getValidAccessToken } from '../../utils/session'

export default defineEventHandler(async (event) => {
  const path = event.context.params?.path || ''
  
  // Skip proxying for local Nitro handlers (auth + OPAQUE HSM vault)
  if (path.startsWith('auth/') || path.startsWith('hsm/')) {
    return
  }

  let accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    const authHeader = event.node.req.headers['authorization']
    if (authHeader && authHeader.startsWith('Bearer ')) {
      accessToken = authHeader.substring(7)
    } else {
      const jwtCookie = getCookie(event, 'jwt_token')
      if (jwtCookie) {
        accessToken = jwtCookie
      }
    }
  }

  if (!accessToken) {
    event.node.res.statusCode = 401
    event.node.res.setHeader('Content-Type', 'application/json')
    return { error: 'Unauthorized', message: 'Session expired or invalid.' }
  }

  event.node.req.headers['authorization'] = `Bearer ${accessToken}`
  try {
    const payloadPart = accessToken.split('.')[1]
    if (payloadPart) {
      const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
      if (payload && payload.sub) {
        event.node.req.headers['x-user-id'] = payload.sub
      }
    }
  } catch (err) {
    console.warn('BFF: Failed to parse JWT for X-User-Id:', err)
  }

  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''

  let targetBase = process.env.API_GATEWAY_URL || process.env.INTERNAL_API_URL || ''

  if (!targetBase) {
    if (path.startsWith('chat/')) {
      targetBase = process.env.CHAT_SERVICE_URL || 'http://chat-service.apps.svc.cluster.local:8087'
    } else if (path.startsWith('notifications/')) {
      targetBase = process.env.NOTIFICATION_SERVICE_URL || 'http://notificationservice.apps.svc.cluster.local:8083'
    } else if (path.startsWith('analytics/')) {
      targetBase = process.env.ANALYTICS_SERVICE_URL || 'http://analytics-service.apps.svc.cluster.local:8095'
    } else if (path.startsWith('search/')) {
      targetBase = process.env.SEARCH_SERVICE_URL || 'http://search-service.apps.svc.cluster.local:8088'
    } else if (path.startsWith('v1/listings') || path.startsWith('marketplace/')) {
      targetBase = process.env.MARKETPLACE_SERVICE_URL || 'http://marketplace-service.apps.svc.cluster.local:8090'
    } else if (path.startsWith('users/') || path.startsWith('user/')) {
      targetBase = process.env.USER_SERVICE_URL || 'http://userservice.apps.svc.cluster.local:8081'
    } else if (path.startsWith('linkguard/')) {
      targetBase = process.env.LINKGUARD_SERVICE_URL || 'http://linkguard-service.apps.svc.cluster.local:8086'
    } else if (path.startsWith('meta-ai/') || path.startsWith('chat-threads') || path.startsWith('chat-history') || path.startsWith('process-chat') || path.startsWith('generated_charts')) {
      const metaBase = process.env.META_AI_SERVICE_URL || 'http://meta-ai-service.apps.svc.cluster.local:8000'
      const cleanPath = path.replace(/^meta-ai\//, '')
      return proxyRequest(event, `${metaBase.replace(/\/+$/, '')}/${cleanPath}${queryString}`)
    } else {
      targetBase = process.env.NUXT_PUBLIC_API_URL || 'http://localhost:8080'
    }
  }

  targetBase = targetBase.replace(/\/+$/, '')
  const targetUrl = `${targetBase}/api/${path}${queryString}`
  return proxyRequest(event, targetUrl)
})
