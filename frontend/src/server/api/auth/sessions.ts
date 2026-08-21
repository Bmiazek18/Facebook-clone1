import { defineEventHandler, getHeader } from 'h3'
import { getValidAccessToken } from '../../utils/session'

interface KeycloakSession {
  id: string
  ipAddress: string
  started: number
  lastAccess: number
  expires: number
  clients: Array<{
    clientId: string
    clientName?: string
  }>
}

export default defineEventHandler(async (event) => {
  const accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    event.node.res.statusCode = 401
    return { error: 'Unauthorized', message: 'No valid access token found.' }
  }

  // Parse User-Agent from request header to identify current browser
  const userAgent = getHeader(event, 'user-agent')
  const currentBrowser = getBrowserFromUserAgent(userAgent)

  // Decode current session ID (sid) from access token
  let currentSessionId = ''
  try {
    const payloadPart = accessToken.split('.')[1]
    const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
    currentSessionId = payload.sid || ''
  } catch (err) {
    console.error('BFF: Failed to parse current session ID from JWT:', err)
  }

  try {
    const config = useRuntimeConfig(event)
    // Call Keycloak's Account REST API to fetch active user sessions
    const response = await fetch(`${config.public.keycloakUrl}/realms/facebook-clone/account/sessions`, {
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Accept': 'application/json'
      }
    })

    if (!response.ok) {
      const errText = await response.text()
      console.error('BFF: Keycloak Account sessions API returned error status:', response.status, errText)
      return getFallbackSessions(currentSessionId, currentBrowser)
    }

    const keycloakSessions = (await response.json()) as KeycloakSession[]

    // Map Keycloak sessions to frontend format
    const mappedLogins = keycloakSessions.map((s, index) => {
      const isCurrent = s.id === currentSessionId
      const browser = isCurrent ? currentBrowser : getBrowserFromUserAgent(undefined, s.id)
      const { location, lat, lng } = getLocationFromIp(s.ipAddress, s.id)

      const timeAgo = formatTimeAgo(s.lastAccess * 1000)
      const registeredTimeAgo = formatTimeAgo(s.started * 1000)

      // Generate a mock security key signature based on session ID
      const mockKey = generateMockKey(s.id)

      return {
        id: s.id,
        title: `Facebook · ${browser}`,
        isCurrent,
        location,
        lat,
        lng,
        icon: 'facebook',
        seen: isCurrent ? 'Ta sesja' : timeAgo,
        key: mockKey,
        keyRegistered: registeredTimeAgo
      }
    })

    return mappedLogins
  } catch (error) {
    console.error('BFF: Error fetching active Keycloak sessions:', error)
    return getFallbackSessions(currentSessionId, currentBrowser)
  }
})

function getBrowserFromUserAgent(ua?: string, seed?: string): string {
  if (ua) {
    if (ua.includes('Firefox')) return 'Firefox'
    if (ua.includes('Chrome') && !ua.includes('Chromium')) return 'Chrome'
    if (ua.includes('Safari') && !ua.includes('Chrome')) return 'Safari'
    if (ua.includes('Edge')) return 'Edge'
    if (ua.includes('Opera') || ua.includes('OPR')) return 'Opera'
  }

  // If no user agent is provided, generate a deterministic fallback based on seed
  if (seed) {
    const hash = getHash(seed)
    const browsers = ['Chrome', 'Safari', 'Firefox', 'Edge']
    return browsers[hash % browsers.length]
  }

  return 'Chrome'
}

function getLocationFromIp(ip: string, id: string): { location: string; lat: number; lng: number } {
  // Diverse Polish cities for local/private IPs to match mock aesthetics
  if (ip === '127.0.0.1' || ip === '::1' || ip.startsWith('192.168.') || ip.startsWith('10.') || ip.startsWith('172.16.')) {
    const hash = getHash(id)
    const locations = [
      { location: 'Warszawa, Masovian Voivodeship, Poland', lat: 52.2297, lng: 21.0122 },
      { location: 'Łuków, Lublin Voivodeship, Poland', lat: 51.9261, lng: 22.3813 },
      { location: 'Kraków, Lesser Poland Voivodeship, Poland', lat: 50.0647, lng: 19.9450 },
      { location: 'Gdańsk, Pomeranian Voivodeship, Poland', lat: 54.3520, lng: 18.6466 },
      { location: 'Poznań, Greater Poland Voivodeship, Poland', lat: 52.4069, lng: 16.9299 }
    ]
    return locations[hash % locations.length]
  }

  return { location: 'Warszawa, Masovian Voivodeship, Poland', lat: 52.2297, lng: 21.0122 }
}

function getHash(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

function generateMockKey(seed: string): string {
  const hash = getHash(seed).toString(16).toUpperCase().padStart(8, '0')
  const keyParts = []
  for (let i = 0; i < 32; i++) {
    // Generate deterministic bytes based on seed and index
    const val = (getHash(seed + i) % 256).toString(16).toUpperCase().padStart(2, '0')
    keyParts.push(val)
  }
  return keyParts.join(' ')
}

function formatTimeAgo(timestamp: number): string {
  const diff = Date.now() - timestamp
  if (diff < 60000) return 'około minuty temu'
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins} min temu`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} godz. temu`
  const days = Math.floor(hours / 24)
  return `${days} dni temu`
}

function getFallbackSessions(currentSessionId: string, currentBrowser: string) {
  // Return a single session representing the current login as fallback
  const mockId = currentSessionId || 'default-session-id'
  const mockKey = generateMockKey(mockId)
  return [
    {
      id: mockId,
      title: `Facebook · ${currentBrowser}`,
      isCurrent: true,
      location: 'Warszawa, Masovian Voivodeship, Poland',
      lat: 52.2297,
      hover: true,
      lng: 21.0122,
      icon: 'facebook',
      seen: 'Ta sesja',
      key: mockKey,
      keyRegistered: '11 min temu'
    }
  ]
}
