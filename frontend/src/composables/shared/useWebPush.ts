export function useWebPush() {
  const config = useRuntimeConfig()
  const apiUrl = config.public.apiUrl

  const isSupported = () => {
    return import.meta.client && typeof window !== 'undefined' && 'Notification' in window && 'serviceWorker' in navigator
  }

  const getPermission = (): NotificationPermission | 'unsupported' => {
    if (!isSupported()) return 'unsupported'
    return Notification.permission
  }

  const registerWebPush = async (userId: string | number): Promise<boolean> => {
    if (!userId || !import.meta.client) return false

    if (isSupported()) {
      try {
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
          return true
        }
      } catch (err) {
        console.warn('[Web Push] Failed to register desktop notifications:', err)
        return false
      }
    }
    return false
  }

  const requestPermissionAndRegister = async (userId: string | number): Promise<boolean> => {
    if (!userId || !import.meta.client) return false

    if (isSupported()) {
      try {
        let perm = Notification.permission
        if (perm === 'default') {
          perm = await Notification.requestPermission()
        }
        if (perm === 'granted') {
          await registerWebPush(userId)
          return true
        }
        return false
      } catch (err) {
        console.warn('[Web Push] Failed to request notification permission:', err)
        return false
      }
    }
    return false
  }

  return {
    isSupported,
    getPermission,
    registerWebPush,
    requestPermissionAndRegister
  }
}
