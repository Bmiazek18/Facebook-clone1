import { defineEventHandler, proxyRequest } from 'h3'

export default defineEventHandler(async (event) => {
  const targetBase = process.env.TUSD_URL || 'http://tusd.apps.svc.cluster.local:1080'
  const path = event.context.params?.path || ''
  const requestUrl = event.node.req.url || ''
  const queryIndex = requestUrl.indexOf('?')
  const queryString = queryIndex !== -1 ? requestUrl.slice(queryIndex) : ''
  const targetUrl = `${targetBase.replace(/\/+$/, '')}/files/${path}${queryString}`
  return proxyRequest(event, targetUrl)
})
