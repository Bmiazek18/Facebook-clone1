import * as Sentry from '@sentry/vue'

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const dsn = config.public.sentryDsn as string

  if (!dsn || dsn.includes('YOUR_SENTRY_DSN')) {
    console.warn('Sentry DSN not found or placeholder used. Skipping Sentry initialization.')
    return
  }

  Sentry.init({
    app: nuxtApp.vueApp,
    dsn,
    environment: process.env.NODE_ENV || 'development',
    tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,

    integrations: [
      Sentry.browserTracingIntegration(),
      Sentry.replayIntegration({
        maskAllText: true,
        blockAllMedia: true,
      }),
      Sentry.captureConsoleIntegration({
        levels: ['error', 'warn'],
      }),
    ],

    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    attachStacktrace: true,

    beforeSend(event, hint) {
      const error = hint.originalException

      if (
        event.exception &&
        error instanceof Error &&
        error.message.includes('ResizeObserver')
      ) {
        return null
      }

      return event
    },
  })
})
