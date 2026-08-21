import { getCookie, setCookie, deleteCookie } from 'h3'
import { getRedisClient } from './redis'

// Map to store in-flight refresh promises per session ID to prevent concurrent refreshes
const inFlightRefreshes = new Map<string, Promise<string | null>>()

export async function getValidAccessToken(event: any): Promise<string | null> {
  const sessionId = getCookie(event, 'bff_session')
  if (!sessionId) {
    return null
  }

  // If there is already a refresh in progress for this session, wait for it
  if (inFlightRefreshes.has(sessionId)) {
    console.log(`BFF: Concurrency lock - waiting for in-flight refresh for session: ${sessionId}`)
    return inFlightRefreshes.get(sessionId) || null
  }

  const redis = getRedisClient()
  const sessionKey = `session:${sessionId}`

  const performRefreshIfNeeded = async (): Promise<string | null> => {
    const sessionStr = await redis.get(sessionKey)
    if (!sessionStr) {
      console.warn(`BFF: Session not found in Redis for ID: ${sessionId}`)
      return null
    }

    const session = JSON.parse(sessionStr)

    const timeDiff = session.expiresAt - Date.now()

    // Check if token is expired or close to expiring (within 10 seconds)
    if (Date.now() + 10000 >= session.expiresAt) {
      console.log(`BFF: Access token is expired or close to expiry (diff: ${timeDiff}ms), refreshing...`)
      const config = useRuntimeConfig(event)
      const tokenUrl = `${config.public.keycloakUrl}/realms/facebook-clone/protocol/openid-connect/token`
      const body = new URLSearchParams()
      body.append('grant_type', 'refresh_token')
      body.append('refresh_token', session.refreshToken)
      body.append('client_id', 'facebook-clone')

      const refreshRes = await fetch(tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: body.toString()
      })

      if (!refreshRes.ok) {
        const errText = await refreshRes.text()
        throw new Error(`Failed to refresh token: ${refreshRes.status} ${errText}`)
      }

      const newTokens = await refreshRes.json()
      session.accessToken = newTokens.access_token
      session.refreshToken = newTokens.refresh_token
      session.expiresAt = Date.now() + (newTokens.expires_in * 1000)

      console.log(`BFF: Successfully refreshed access token in Redis. New expiresAt = ${session.expiresAt} (expires_in = ${newTokens.expires_in}s)`)

      // Update session in Redis (reset TTL to 7 days)
      await redis.set(sessionKey, JSON.stringify(session), 'EX', 7 * 24 * 60 * 60)

      // Update public cookie session_expires_at so the frontend background refresh timer knows the new expiry time
      try {
        setCookie(event, 'session_expires_at', String(session.expiresAt), {
          httpOnly: false,
          secure: false,
          sameSite: 'lax',
          path: '/',
          maxAge: 7 * 24 * 60 * 60
        })
      } catch (cookieErr) {
        console.warn('BFF: Failed to update session_expires_at cookie (might be a non-HTTP context):', cookieErr)
      }
    }

    return session.accessToken
  }

  try {
    const sessionStr = await redis.get(sessionKey)
    if (!sessionStr) {
      try {
        deleteCookie(event, 'jwt_token')
        deleteCookie(event, 'session_expires_at')
        deleteCookie(event, 'bff_session')
      } catch (cookieErr) {}
      return null
    }
    const session = JSON.parse(sessionStr)

    if (Date.now() + 10000 >= session.expiresAt) {
      let promise = inFlightRefreshes.get(sessionId)
      if (!promise) {
        promise = performRefreshIfNeeded().catch((err) => {
          console.error('BFF: Error refreshing token inside promise lock:', err)
          return null
        })
        inFlightRefreshes.set(sessionId, promise)
      }
      try {
        const resToken = await promise
        if (!resToken) {
          try {
            deleteCookie(event, 'jwt_token')
            deleteCookie(event, 'session_expires_at')
            deleteCookie(event, 'bff_session')
          } catch (cookieErr) {}
        }
        return resToken
      } finally {
        inFlightRefreshes.delete(sessionId)
      }
    } else {
      return session.accessToken
    }
  } catch (err) {
    console.error('BFF: Error getting or refreshing access token from Redis:', err)
    try {
      deleteCookie(event, 'jwt_token')
      deleteCookie(event, 'session_expires_at')
      deleteCookie(event, 'bff_session')
    } catch (cookieErr) {}
    return null
  }
}
