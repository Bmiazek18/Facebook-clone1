import type { H3Event } from 'h3'
import { proxyRequest } from 'h3'

export async function handleTusProxy(event: H3Event, subPath: string = '') {
  const targetBase = process.env.TUSD_URL || 'http://tusd.apps.svc.cluster.local:1080'
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''

  // Normalize target URL
  const cleanBase = targetBase.replace(/\/+$/, '')
  const cleanSub = subPath.replace(/^\/+/, '')
  const targetUrl = cleanSub
    ? `${cleanBase}/files/${cleanSub}${queryString}`
    : `${cleanBase}/files/${queryString}`

  // Apply CORS headers for TUS clients
  const origin = event.node.req.headers['origin'] || '*'
  event.node.res.setHeader('Access-Control-Allow-Origin', origin)
  event.node.res.setHeader('Access-Control-Allow-Credentials', 'true')
  event.node.res.setHeader('Access-Control-Allow-Methods', 'POST, GET, HEAD, PATCH, DELETE, OPTIONS')
  event.node.res.setHeader(
    'Access-Control-Allow-Headers',
    'Origin, X-Requested-With, Content-Type, Accept, Authorization, Upload-Offset, Upload-Length, Upload-Metadata, Tus-Resumable, Tus-Version, Tus-Extension, Tus-Max-Size, X-HTTP-Method-Override'
  )
  event.node.res.setHeader(
    'Access-Control-Expose-Headers',
    'Location, Upload-Offset, Upload-Length, Tus-Version, Tus-Resumable, Tus-Max-Size, Tus-Extension, Upload-Metadata'
  )

  // Handle preflight OPTIONS request
  if (event.node.req.method === 'OPTIONS') {
    event.node.res.statusCode = 204
    event.node.res.setHeader('Tus-Resumable', '1.0.0')
    event.node.res.setHeader('Tus-Version', '1.0.0')
    event.node.res.setHeader('Tus-Extension', 'creation,creation-with-upload,termination,concatenation')
    event.node.res.setHeader('Tus-Max-Size', '1073741824')
    return ''
  }

  // Rewrite Location header in response so browser receives relative /files/... path instead of internal cluster URL
  const origSetHeader = event.node.res.setHeader.bind(event.node.res)
  event.node.res.setHeader = (name: string, value: any) => {
    if (typeof name === 'string' && name.toLowerCase() === 'location' && typeof value === 'string') {
      if (value.includes('/files/')) {
        value = '/files/' + value.split('/files/').pop()
      }
    }
    return origSetHeader(name, value)
  }

  return proxyRequest(event, targetUrl)
}

