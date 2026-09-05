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
