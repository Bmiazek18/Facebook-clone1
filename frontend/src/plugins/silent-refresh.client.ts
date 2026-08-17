import { defineNuxtPlugin } from '#app'

export default defineNuxtPlugin(() => {
  if (typeof window === 'undefined') return

  // Unique identifier for this specific tab/window
  const tabId = Math.random().toString(36).substring(7)
  let worker: SharedWorker | null = null
  let workerPort: MessagePort | null = null
  let fallbackTimer: any = null
  let lastActivity = Date.now()
  const IDLE_TIMEOUT = 5 * 60 * 1000 // 5 minutes

  // Helper to read cookie value
  const getCookie = (name: string): string | null => {
    const value = `; ${document.cookie}`
    const parts = value.split(`; ${name}=`)
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null
    return null
  }

  // Activity listeners to track active/idle state
  const handleActivity = () => {
    lastActivity = Date.now()
    if (workerPort) {
      workerPort.postMessage({ type: 'ACTIVITY' })
    }
  }

  const activityEvents = ['mousemove', 'keydown', 'scroll', 'click', 'touchstart']
  activityEvents.forEach(event => {
    window.addEventListener(event, handleActivity, { passive: true })
  })

  // --- CHECK FOR SHARED WORKER SUPPORT ---
  if ('SharedWorker' in window) {
    console.log(`[Tab: ${tabId}] Using SharedWorker for session lifetime sync.`)
    try {
      worker = new SharedWorker('/session-worker.js')
      workerPort = worker.port

      workerPort.onmessage = (event) => {
        const data = event.data
        if (data.type === 'REFRESH_START') {
          console.log(`[Tab: ${tabId} (Worker sync)] Silent session refresh started by worker.`)
        } else if (data.type === 'REFRESH_SUCCESS') {
          console.log(`[Tab: ${tabId} (Worker sync)] Session refreshed successfully. New expiry: ${data.expiresAt}`)
        } else if (data.type === 'STATUS') {
          console.log(`[Tab: ${tabId} (Worker sync)] Connected. Active expiry time: ${data.expiresAt}`)
        }
      }

      workerPort.start()

      // Initialize the worker with the current session expiry cookie
      const expiresAt = getCookie('session_expires_at')
      if (expiresAt) {
        workerPort.postMessage({ type: 'INIT', expiresAt })
      }

      // Track page closure to inform worker
      window.addEventListener('beforeunload', () => {
        if (workerPort) {
          workerPort.postMessage({ type: 'CLOSE' })
        }
      })
      return
    } catch (err) {
      console.error('[Session Manager] Failed to init SharedWorker, falling back to local timer:', err)
      worker = null
      workerPort = null
    }
  }

  // --- FALLBACK: LOCAL SINGLE-TAB TIMER FOR UNSUPPORTED ENVIRONMENTS ---
  console.log(`[Tab: ${tabId}] SharedWorker unsupported. Falling back to local refresh timer.`)
  
  const checkAndRefreshSessionFallback = async () => {
    // Skip if page is hidden
    if (document.hidden) return

    // Skip if user is idle
    if (Date.now() - lastActivity > IDLE_TIMEOUT) {
      return
    }

    const expiresAtStr = getCookie('session_expires_at')
    if (!expiresAtStr) return

    const expiresAt = parseInt(expiresAtStr, 10)
    if (isNaN(expiresAt)) return

    const timeLeft = expiresAt - Date.now()

    // Refresh if the token expires in less than 45 seconds
    if (timeLeft > 0 && timeLeft <= 45000) {
      console.log(`[Tab: ${tabId} (Fallback)] Token expires in ${Math.round(timeLeft / 1000)}s. Performing local refresh...`)
      try {
        const res = await fetch('/api/auth/refresh', { method: 'POST' })
        if (res.ok) {
          const data = await res.json()
          if (data.success) {
            console.log(`[Tab: ${tabId} (Fallback)] Session refreshed successfully.`)
          }
        }
      } catch (err) {
        console.error(`[Tab: ${tabId} (Fallback)] Local refresh failed:`, err)
      }
    }
  }

  const startFallbackTimer = () => {
    if (fallbackTimer) return
    fallbackTimer = setInterval(checkAndRefreshSessionFallback, 10000)
    checkAndRefreshSessionFallback()
  }

  const stopFallbackTimer = () => {
    if (fallbackTimer) {
      clearInterval(fallbackTimer)
      fallbackTimer = null
    }
  }

  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      stopFallbackTimer()
    } else {
      lastActivity = Date.now()
      startFallbackTimer()
    }
  })

  startFallbackTimer()
})
