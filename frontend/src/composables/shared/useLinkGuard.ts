import { ref } from 'vue'
import { useEventListener } from '@vueuse/core'

export function useLinkGuard() {
  const isVerifying = ref(false)
  const GRAPHQL_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/linkguard/graphql`

  const SAFE_DOMAINS = new Set([
    "google.com", "facebook.com", "github.com", "youtube.com", "wikipedia.org",
    "pl.wikipedia.org", "microsoft.com", "apple.com", "twitter.com", "linkedin.com",
    "instagram.com"
  ])

  const getDomain = (urlStr: string): string => {
    try {
      const hostname = new URL(urlStr).hostname.toLowerCase();
      return hostname.replace(/^www\./, '');
    } catch {
      return '';
    }
  }

  const verifyAndNavigate = async (url: string) => {
    if (!url || (!url.startsWith('http://') && !url.startsWith('https://'))) {
      if (import.meta.client) {
        window.open(url, '_blank')
      }
      return
    }

    // Bypass inspection for whitelisted safe domains to speed up navigation and reduce server load
    const domain = getDomain(url);
    if (SAFE_DOMAINS.has(domain)) {
      if (import.meta.client) {
        window.open(url, '_blank')
      }
      return
    }

    // Immediately open a blank tab to bypass browser popup blockers
    let newTab: Window | null = null
    if (import.meta.client) {
      newTab = window.open('about:blank', '_blank')
      if (newTab) {
        newTab.document.write(`
          <!DOCTYPE html>
          <html>
            <head>
              <title>LinkGuard - Weryfikacja bezpieczeństwa</title>
              <meta charset="utf-8">
              <style>
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  height: 100vh;
                  margin: 0;
                  background: #f0f2f5;
                  color: #1c1e21;
                }
                .card {
                  background: white;
                  padding: 40px;
                  border-radius: 12px;
                  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                  text-align: center;
                  max-width: 400px;
                }
                .spinner {
                  width: 44px;
                  height: 44px;
                  border: 4px solid #f3f3f3;
                  border-top: 4px solid #1877f2;
                  border-radius: 50%;
                  animation: spin 1s linear infinite;
                  margin: 0 auto 24px auto;
                }
                .shield-icon {
                  color: #1877f2;
                  margin-bottom: 16px;
                }
                @keyframes spin {
                  0% { transform: rotate(0deg); }
                  100% { transform: rotate(360deg); }
                }
                h1 {
                  font-size: 20px;
                  margin: 0 0 10px 0;
                  font-weight: 600;
                }
                p {
                  font-size: 14px;
                  color: #65676b;
                  margin: 0;
                  line-height: 1.4;
                }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="spinner"></div>
                <h1>Weryfikacja LinkGuard...</h1>
                <p>Trwa sprawdzanie bezpieczeństwa linku docelowego. Zaraz zostaniesz przekierowany.</p>
              </div>
            </body>
          </html>
        `)
        newTab.document.close()
      }
    }

    isVerifying.value = true
    try {
      const response = await fetch(GRAPHQL_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query Inspect($url: String!) { inspectLink(url: $url) { shieldUrl isSafe riskScore } }`,
          variables: { url },
        }),
      })

      const result = await response.json()
      if (!response.ok || result.errors?.length) {
        throw new Error(result.errors?.[0]?.message || 'LinkGuard unavailable')
      }

      const shieldUrl = result.data?.inspectLink?.shieldUrl
      if (shieldUrl) {
        if (newTab) {
          newTab.location.href = shieldUrl
        } else if (import.meta.client) {
          window.open(shieldUrl, '_blank')
        }
      } else {
        if (newTab) {
          newTab.location.href = url
        } else if (import.meta.client) {
          window.open(url, '_blank')
        }
      }
    } catch (error) {
      console.error('LinkGuard verification failed, falling back to direct navigation:', error)
      if (newTab) {
        newTab.location.href = url
      } else if (import.meta.client) {
        window.open(url, '_blank')
      }
    } finally {
      isVerifying.value = false
    }
  }

  const handleGlobalLinkClick = async (event: MouseEvent) => {
    // Bypass if click is modified (e.g. Cmd/Ctrl/Shift + Click) or not left-click
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
      return
    }

    const target = event.target as HTMLElement
    const anchor = target.closest('a')
    if (!anchor) return

    const href = anchor.getAttribute('href')
    if (!href) return

    // Intercept only absolute http/https links
    if (href.startsWith('http://') || href.startsWith('https://')) {
      // Bypass if link points to the same hostname (internal absolute link)
      try {
        const url = new URL(href, window.location.origin)
        if (url.hostname === window.location.hostname) {
          return
        }
      } catch (e) {
        // Ignore invalid URL
      }

      // Bypass if already translated to redirect link (l.php)
      if (href.includes('/l.php?')) return

      event.preventDefault()
      event.stopPropagation()
      await verifyAndNavigate(href)
    }
  }

  const initLinkGuard = () => {
    if (import.meta.client) {
      useEventListener(document, 'click', handleGlobalLinkClick, { capture: true })
    }
  }

  return {
    isVerifying,
    verifyAndNavigate,
    initLinkGuard
  }
}
