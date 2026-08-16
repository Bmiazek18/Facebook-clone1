import { defineEventHandler, proxyRequest } from 'h3'
import { getValidAccessToken } from '../../utils/session'

export default defineEventHandler(async (event) => {
  const path = event.context.params?.path || ''
  
  // Skip proxying for local Nitro handlers (auth + OPAQUE HSM vault)
  if (path.startsWith('auth/') || path.startsWith('hsm/')) {
    return
  }

  const accessToken = await getValidAccessToken(event)
  
  if (!accessToken) {
    event.node.res.statusCode = 401
    event.node.res.setHeader('Content-Type', 'application/json')
    return { error: 'Unauthorized', message: 'Session expired or invalid.' }
  }

  event.node.req.headers['authorization'] = `Bearer ${accessToken}`

  // Nginx forwards the JWT to Kong. Kong extracts `sub` and adds X-User-Id
  // for the downstream service; the BFF never supplies that identity header.
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''
  const targetUrl = `http://localhost:8080/api/${path}${queryString}`
  return proxyRequest(event, targetUrl)
})
