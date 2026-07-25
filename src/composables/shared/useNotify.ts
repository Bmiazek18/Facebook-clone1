// src/composables/useNotify.ts
import { useToast, type ToastID, type ToastOptions } from 'vue-toastification'
import type { Component } from 'vue'
import GlobalToast from '@/components/common/Toast.vue'

// Importy ikon
import CheckCircleOutline from 'vue-material-design-icons/CheckCircleOutline.vue'
import AlertCircleOutline from 'vue-material-design-icons/AlertCircleOutline.vue'
import InformationOutline from 'vue-material-design-icons/InformationOutline.vue'
import WifiOff from 'vue-material-design-icons/WifiOff.vue'
import Wifi from 'vue-material-design-icons/Wifi.vue'

const IconSuccess = CheckCircleOutline
const IconError = AlertCircleOutline
const IconInfo = InformationOutline
const IconOffline = WifiOff
const IconOnline = Wifi

interface ToastAction {
  label: string
  handler: () => void
}

// Opcje dla karty powiadomienia (Zrzut ekranu 2026-06-23 o 08.55.46.jpg)
export interface NotificationOptions {
  title: string
  header?: string
  time?: string
  avatar?: string
  avatarBadgeIcon?: Component
  avatarBadgeColor?: string
  unread?: boolean
  action?: ToastAction
  timeout?: number
}

let offlineToastId: ToastID | null = null

export function useNotify() {
  const toast = useToast()

  const commonOptions: ToastOptions = {
    position: 'bottom-left',
    closeOnClick: false,
    draggable: false,
    icon: false,
    closeButton: false,
    toastClassName: 'custom-toast-container',
    bodyClassName: 'custom-toast-body',
  }

  // Silnik wysyłający dowolną paczkę propsów do GlobalToast
  const dispatchToast = (props: Record<string, any>, timeout: number = 4000) => {
    return toast({ component: GlobalToast, props }, { ...commonOptions, timeout })
  }

  const showRaw = (
    title: string,
    icon: Component,
    iconColor: string,
    timeout: number = 4000,
    action: ToastAction | null = null,
  ) => {
    return dispatchToast({ title, icon, iconColor, action }, timeout)
  }

  return {
    success: (title: string) => showRaw(title, IconSuccess, 'text-emerald-500'),
    error: (title: string) => showRaw(title, IconError, 'text-red-500'),
    info: (title: string) => showRaw(title, IconInfo, 'text-blue-500'),

    // --- NOWA METODA ---
    notification: (options: NotificationOptions) => {
      const { timeout = 6000, ...props } = options

      return dispatchToast(
        {
          header: 'Nowe powiadomienie', // Domyślny nagłówek ze zrzutu ekranu
          unread: true, // Domyślnie włączona niebieska kropka
          ...props, // Przekazane opcje nadpiszą powyższe domyślne
        },
        timeout,
      )
    },

    offline: () => {
      if (offlineToastId !== null) return
      offlineToastId = showRaw('Jesteś teraz w trybie offline.', IconOffline, 'text-gray-500', 0, {
        label: 'Odśwież',
        handler: () => window.location.reload(),
      })
    },

    online: () => {
      if (offlineToastId !== null) {
        toast.dismiss(offlineToastId)
        offlineToastId = null
      }
      showRaw('Przywrócono połączenie z Internetem.', IconOnline, 'text-emerald-600', 4000)
    },
  }
}
