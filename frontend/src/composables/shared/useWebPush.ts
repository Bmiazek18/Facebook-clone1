export function useWebPush() {
  const config = useRuntimeConfig()
  const apiUrl = config.public.apiUrl

  const registerWebPush = async (userId: string | number) => {
    if (!userId || !import.meta.client) return

    if (typeof window !== 'undefined' && 'Notification' in window && 'serviceWorker' in navigator) {
      try {
        if (Notification.permission === 'default') {
          const permission = await Notification.requestPermission()
          if (permission !== 'granted') {
            console.log('[Web Push] User denied desktop notifications permission.')
            return
          }
        }

        if (Notification.permission === 'granted') {
          const registration = await navigator.serviceWorker.ready
          const applicationServerKey = 'BEl4A8Fv3w4T2lT4zG-8V4p33rY2G3JtG3_G4P3jY2P4x2D3x4F3e4D3x4E3e4D3x4F3e4D3x4E3e4D3x4F3e4A=='

          const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: applicationServerKey
          })

          const subscriptionJson = subscription.toJSON()
          const payload = {
            endpoint: subscriptionJson.endpoint,
            p256dh: subscriptionJson.keys?.p256dh,
            auth: subscriptionJson.keys?.auth
          }

          await $fetch(`${apiUrl}/api/notifications/wp-subscription`, {
            method: 'POST',
            headers: {
              'X-User-Id': String(userId)
            },
            body: payload
          })
          console.log('[Web Push] Web Push subscription successfully registered on backend.')
        }
      } catch (err) {
        console.warn('[Web Push] Failed to register desktop notifications:', err)
      }
    }
  }

  return {
    registerWebPush
  }
}
