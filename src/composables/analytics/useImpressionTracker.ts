import { useAuthStore } from '@/stores/auth'

export interface ImpressionPayload {
  postId: string
  pageId?: string
  viewerId?: string
  isFollower?: boolean
  contentType?: string
  dwellTimeMs?: number
  source?: string
  timestamp?: number
}

export interface TelemetryPayload {
  eventType: string
  postId?: string
  pageId?: string
  userId?: string
  completionPercent?: number
  loopCount?: number
  linkUrl?: string
  dwellTimeMs?: number
  contentType?: string
  isFollower?: boolean
  source?: string
  timestamp?: number
}

const impressionBuffer: ImpressionPayload[] = []
const eventBuffer: TelemetryPayload[] = []
let flushTimer: any = null

export function useImpressionTracker() {
  const authStore = useAuthStore()
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

  const flushBuffer = async () => {
    // 1. Flush impressions
    if (impressionBuffer.length > 0) {
      const batch = [...impressionBuffer]
      impressionBuffer.length = 0

      try {
        if (typeof navigator !== 'undefined' && navigator.sendBeacon) {
          const blob = new Blob([JSON.stringify(batch)], { type: 'application/json' })
          navigator.sendBeacon(`${apiUrl}/api/analytics/impressions/batch`, blob)
        } else {
          fetch(`${apiUrl}/api/analytics/impressions/batch`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(batch),
            keepalive: true
          }).catch(() => {})
        }
      } catch (err) {
        console.warn('Analytics impression batch send failed:', err)
      }
    }

    // 2. Flush general telemetry events
    if (eventBuffer.length > 0) {
      const eventBatch = [...eventBuffer]
      eventBuffer.length = 0

      try {
        if (typeof navigator !== 'undefined' && navigator.sendBeacon) {
          const blob = new Blob([JSON.stringify(eventBatch)], { type: 'application/json' })
          navigator.sendBeacon(`${apiUrl}/api/analytics/events/batch`, blob)
        } else {
          fetch(`${apiUrl}/api/analytics/events/batch`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(eventBatch),
            keepalive: true
          }).catch(() => {})
        }
      } catch (err) {
        console.warn('Analytics event batch send failed:', err)
      }
    }
  }

  // Periodic flush
  if (!flushTimer && import.meta.client) {
    flushTimer = setInterval(flushBuffer, 5000)
    window.addEventListener('beforeunload', flushBuffer)
  }

  const activeViews = new Map<string, number>()

  const observePostElement = (
    el: HTMLElement,
    postId: string,
    authorId?: string,
    contentType: string = 'text',
    source: string = 'feed'
  ) => {
    if (!import.meta.client || !el || !postId) return

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && entry.intersectionRatio >= 0.5) {
            if (!activeViews.has(postId)) {
              activeViews.set(postId, Date.now())
            }
          } else {
            if (activeViews.has(postId)) {
              const startTime = activeViews.get(postId)!
              activeViews.delete(postId)
              const dwellTimeMs = Date.now() - startTime

              if (dwellTimeMs >= 400) {
                impressionBuffer.push({
                  postId: String(postId),
                  pageId: authorId ? String(authorId) : undefined,
                  viewerId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
                  isFollower: false,
                  contentType,
                  dwellTimeMs,
                  source,
                  timestamp: Date.now()
                })

                if (impressionBuffer.length >= 10) {
                  flushBuffer()
                }
              }
            }
          }
        })
      },
      { threshold: 0.5 }
    )

    observer.observe(el)

    return () => {
      observer.disconnect()
      if (activeViews.has(postId)) {
        const startTime = activeViews.get(postId)!
        activeViews.delete(postId)
        const dwellTimeMs = Date.now() - startTime
        if (dwellTimeMs >= 400) {
          impressionBuffer.push({
            postId: String(postId),
            pageId: authorId ? String(authorId) : undefined,
            viewerId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
            isFollower: false,
            contentType,
            dwellTimeMs,
            source,
            timestamp: Date.now()
          })
        }
      }
    }
  }

  // --- VIDEO TELEMETRY ---

  const trackVideoProgress = (postId: string, pageId?: string, percent: number = 100) => {
    eventBuffer.push({
      eventType: 'VIDEO_PROGRESS',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      completionPercent: percent,
      contentType: 'video',
      timestamp: Date.now()
    })
  }

  const trackVideoLoop = (postId: string, pageId?: string, loopCount: number = 1) => {
    eventBuffer.push({
      eventType: 'VIDEO_LOOP',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      loopCount,
      contentType: 'video',
      timestamp: Date.now()
    })
  }

  const trackAudioToggle = (postId: string, pageId?: string, isMuted: boolean = false) => {
    if (!isMuted) {
      eventBuffer.push({
        eventType: 'AUDIO_UNMUTE',
        postId: String(postId),
        pageId: pageId ? String(pageId) : undefined,
        userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
        contentType: 'video',
        timestamp: Date.now()
      })
    }
  }

  // --- DEEP CONTENT INTENT ---

  const trackExpandText = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'EXPAND_TEXT',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  const trackLightboxOpen = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'LIGHTBOX_OPEN',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  const trackLinkClick = (postId: string, pageId?: string, linkUrl?: string) => {
    eventBuffer.push({
      eventType: 'LINK_CLICK',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      linkUrl,
      timestamp: Date.now()
    })
  }

  const trackCopyLink = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'COPY_LINK',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  const trackSavePost = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'SAVE_POST',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  const trackHidePost = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'HIDE_POST',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  const trackFastSkip = (postId: string, pageId?: string) => {
    eventBuffer.push({
      eventType: 'FAST_SKIP',
      postId: String(postId),
      pageId: pageId ? String(pageId) : undefined,
      userId: authStore.currentUserId ? String(authStore.currentUserId) : undefined,
      timestamp: Date.now()
    })
  }

  return {
    observePostElement,
    trackVideoProgress,
    trackVideoLoop,
    trackAudioToggle,
    trackExpandText,
    trackLightboxOpen,
    trackLinkClick,
    trackCopyLink,
    trackSavePost,
    trackHidePost,
    trackFastSkip,
    flushBuffer
  }
}
