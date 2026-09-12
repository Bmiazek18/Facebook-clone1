import {
  defineEventHandler,
  getCookie,
  readRawBody,
  getMethod,
} from 'h3'
import { getValidAccessToken } from '../utils/session'
import crypto from 'crypto'

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

  const reqHeaders: Record<string, string> = {
    authorization: `Bearer ${accessToken}`,
    'content-type': (event.node.req.headers['content-type'] as string) || 'application/json',
    accept: (event.node.req.headers['accept'] as string) || 'application/json',
  }

  try {
    const payloadPart = accessToken.split('.')[1]
    if (payloadPart) {
      const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
      if (payload && payload.sub) {
        reqHeaders['x-user-id'] = payload.sub
      }
    }
  } catch (err) {
    console.warn('BFF: Failed to parse JWT for X-User-Id:', err)
  }

  // Ensure X-Request-ID and OpenTelemetry trace context propagation
  if (event.node.req.headers['x-request-id']) {
    reqHeaders['x-request-id'] = event.node.req.headers['x-request-id'] as string
  } else {
    reqHeaders['x-request-id'] = crypto.randomUUID()
  }

  try {
    const { propagation, context } = await import('@opentelemetry/api')
    propagation.inject(context.active(), reqHeaders)
  } catch (e) {
    // ignore if otel api unavailable in local test
  }

  const routerUrl =
    process.env.APOLLO_ROUTER_URL ||
    process.env.GRAPHQL_URL ||
    'http://apollo-router.apps.svc.cluster.local:4000/graphql'

  const method = getMethod(event)
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''
  const targetUrl = `${routerUrl}${queryString}`

  let body: any = undefined
  if (method !== 'GET' && method !== 'HEAD') {
    body = await readRawBody(event)
  }

  // Forward request to Apollo Router upstream
  const upstreamRes = await fetch(targetUrl, {
    method,
    headers: reqHeaders,
    body,
  })

  const resStatus = upstreamRes.status
  const resHeaders = upstreamRes.headers

  // Propagate standard response headers (like content-type)
  const contentType = resHeaders.get('content-type') || 'application/json'
  event.node.res.setHeader('Content-Type', contentType)

  // Read response buffer
  const arrayBuffer = await upstreamRes.arrayBuffer()
  const buffer = Buffer.from(arrayBuffer)

  // Check if this was a successful GraphQL response (200 OK)
  if (resStatus === 200) {
    // Compute weak ETag from response content
    const hash = crypto.createHash('sha1').update(buffer).digest('base64url')
    const etag = `W/"${hash}"`

    event.node.res.setHeader('ETag', etag)
    event.node.res.setHeader('Cache-Control', 'private, no-cache')

    const ifNoneMatch = event.node.req.headers['if-none-match']
    if (
      ifNoneMatch &&
      (ifNoneMatch === etag || ifNoneMatch === `"${hash}"` || ifNoneMatch === hash)
    ) {
      event.node.res.statusCode = 304
      event.node.res.end()
      return
    }
  } else {
    event.node.res.statusCode = resStatus
  }

  return buffer
})
