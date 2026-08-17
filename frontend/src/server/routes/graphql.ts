import { defineEventHandler, proxyRequest } from 'h3'
import { getValidAccessToken } from '../utils/session'

export default defineEventHandler(async (event) => {
  const accessToken = await getValidAccessToken(event)
  
  if (!accessToken) {
    event.node.res.statusCode = 401
    event.node.res.setHeader('Content-Type', 'application/json')
    return { errors: [{ message: 'Unauthorized', extensions: { code: 'UNAUTHENTICATED' } }] }
  }

  event.node.req.headers['authorization'] = `Bearer ${accessToken}`
  try {
    const payloadPart = accessToken.split('.')[1]
    const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
    if (payload && payload.sub) {
      event.node.req.headers['x-user-id'] = payload.sub
    }
  } catch (err) {
    console.warn('BFF: Failed to parse JWT for X-User-Id:', err)
  }

  // Forward the request to Kong gateway on http://localhost:8000/graphql with original query string
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''
  return proxyRequest(event, `http://localhost:8000/graphql${queryString}`)
})
