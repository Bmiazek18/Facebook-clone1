import { defineEventHandler, getCookie, proxyRequest } from 'h3'
import { getValidAccessToken } from '../utils/session'

export default defineEventHandler(async (event) => {
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
    return { errors: [{ message: 'Unauthorized', extensions: { code: 'UNAUTHENTICATED' } }] }
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

  // Ensure X-Request-ID and OpenTelemetry trace context propagation
  if (!event.node.req.headers['x-request-id']) {
    event.node.req.headers['x-request-id'] = crypto.randomUUID()
  }
  try {
    const { propagation, context } = await import('@opentelemetry/api')
    propagation.inject(context.active(), event.node.req.headers)
  } catch (e) {
    // ignore if otel api unavailable in local test
  }

  const routerUrl = process.env.APOLLO_ROUTER_URL || process.env.GRAPHQL_URL || 'http://apollo-router.apps.svc.cluster.local:4000/graphql'
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''
  return proxyRequest(event, `${routerUrl}${queryString}`)
})
