import { defineEventHandler, getQuery, setCookie, sendRedirect, setHeader } from 'h3'
import { randomUUID } from 'node:crypto'
import { getRedisClient } from '../../utils/redis'

export default defineEventHandler(async (event) => {
  // Prevent browser from caching this redirect
  setHeader(event, 'Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0')

  const query = getQuery(event)
  const code = query.code as string

  if (!code) {
    const error = query.error as string
    const errorDesc = query.error_description as string
    console.error('No authorization code provided in callback. Error:', error, '-', errorDesc)
    return {
      error: 'No authorization code provided',
      details: error || 'Missing code parameter',
      description: errorDesc || 'Ensure Keycloak is configured correctly.'
    }
  }

  try {
    const tokenUrl = 'http://localhost:8089/realms/myrealm/protocol/openid-connect/token'
    const body = new URLSearchParams()
    body.append('grant_type', 'authorization_code')
    body.append('code', code)
    body.append('redirect_uri', 'http://localhost:3000/api/auth/callback')
    body.append('client_id', 'facebook-clone')

    const tokenRes = await fetch(tokenUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: body.toString()
    })

    if (!tokenRes.ok) {
      const errText = await tokenRes.text()
      throw new Error(`Keycloak token exchange failed: ${tokenRes.status} ${errText}`)
    }

    const tokens = await tokenRes.json()
    const accessToken = tokens.access_token
    const refreshToken = tokens.refresh_token

    // Decode JWT payload (second part of JWT)
    const payloadPart = accessToken.split('.')[1]
    const payload = JSON.parse(Buffer.from(payloadPart, 'base64').toString('utf8'))
    const userId = payload.sub

    // Create session ID and session payload
    const sessionId = randomUUID()
    const sessionData = {
      accessToken,
      refreshToken,
      userId,
      expiresAt: Date.now() + (tokens.expires_in * 1000)
    }

    // Save session in Redis for 7 days
    const redis = getRedisClient()
    const sessionKey = `session:${sessionId}`
    await redis.set(sessionKey, JSON.stringify(sessionData), 'EX', 7 * 24 * 60 * 60)

    // Set HTTP-only BFF session cookie containing only the sessionId
    setCookie(event, 'bff_session', sessionId, {
      httpOnly: true,
      secure: false, // Set to true if HTTPS is used
      sameSite: 'lax',
      path: '/',
      maxAge: 7 * 24 * 60 * 60
    })

    // Set public jwt_token cookie (visible to client routing middleware)
    setCookie(event, 'jwt_token', userId, {
      httpOnly: false,
      secure: false,
      sameSite: 'lax',
      path: '/',
      maxAge: 7 * 24 * 60 * 60
    })

    // Set public session_expires_at cookie (visible to client background refresh timer)
    setCookie(event, 'session_expires_at', String(sessionData.expiresAt), {
      httpOnly: false,
      secure: false,
      sameSite: 'lax',
      path: '/',
      maxAge: 7 * 24 * 60 * 60
    })

    return sendRedirect(event, '/?from_callback=true')
  } catch (err: any) {
    console.error('Failed to handle Keycloak callback:', err)
    return {
      error: 'Failed to handle Keycloak callback',
      message: err.message,
      stack: err?.stack
    }
  }
})
