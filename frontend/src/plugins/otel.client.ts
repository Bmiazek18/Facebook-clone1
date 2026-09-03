import { WebTracerProvider } from '@opentelemetry/sdk-trace-web'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { BatchSpanProcessor } from '@opentelemetry/sdk-trace-base'
import { ZoneContextManager } from '@opentelemetry/context-zone'
import { resourceFromAttributes } from '@opentelemetry/resources'
import {
  ATTR_SERVICE_NAME,
  ATTR_SERVICE_VERSION,
} from '@opentelemetry/semantic-conventions'

import { registerInstrumentations } from '@opentelemetry/instrumentation'
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch'
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request'
import { DocumentLoadInstrumentation } from '@opentelemetry/instrumentation-document-load'
import { UserInteractionInstrumentation } from '@opentelemetry/instrumentation-user-interaction'

import { trace, SpanStatusCode, type Span } from '@opentelemetry/api'

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()

  const rawEndpoint = config.public.otelEndpoint as string | undefined
  if (!rawEndpoint) {
    return
  }

  const otelEndpoint = rawEndpoint.endsWith('/v1/traces')
    ? rawEndpoint
    : `${rawEndpoint.replace(/\/$/, '')}/v1/traces`

  /*
   * ------------------------------------------------------------
   * Resource
   * ------------------------------------------------------------
   */

  const resource = resourceFromAttributes({
    [ATTR_SERVICE_NAME]: 'nuxt-browser-frontend',
    [ATTR_SERVICE_VERSION]: '1.0.0',
  })

  /*
   * ------------------------------------------------------------
   * OTLP exporter
   * ------------------------------------------------------------
   */

  const exporter = new OTLPTraceExporter({
    url: otelEndpoint,
  })

  /*
   * ------------------------------------------------------------
   * Provider
   * ------------------------------------------------------------
   */

  const provider = new WebTracerProvider({
    resource,
    spanProcessors: [
      new BatchSpanProcessor(exporter),
    ],
  })

  provider.register({
    contextManager: new ZoneContextManager(),
  })

  /*
   * ------------------------------------------------------------
   * Automatic browser instrumentation
   * ------------------------------------------------------------
   */

  registerInstrumentations({
    instrumentations: [
      new FetchInstrumentation({
        propagateTraceHeaderCorsUrls: [
          /^https:\/\/api\.twojadomena\.pl/,
          /^https:\/\/router\.twojadomena\.pl/,
        ],
      }),

      new XMLHttpRequestInstrumentation({
        propagateTraceHeaderCorsUrls: [
          /^https:\/\/api\.twojadomena\.pl/,
          /^https:\/\/router\.twojadomena\.pl/,
        ],
      }),

      new DocumentLoadInstrumentation(),


    ],
  })

  /*
   * ------------------------------------------------------------
   * Vue / Nuxt Router tracing
   * ------------------------------------------------------------
   */

  const router = useRouter()
  const tracer = trace.getTracer('nuxt-router')

  let navigationSpan: Span | undefined

  router.beforeEach((to, from) => {
    navigationSpan?.end()

    navigationSpan = tracer.startSpan('navigation', {
      attributes: {
        'navigation.from': from.fullPath,
        'navigation.to': to.fullPath,
        'navigation.type': 'route-change',
      },
    })
  })

  router.afterEach((to) => {
    if (!navigationSpan) return

    navigationSpan.setAttribute(
      'navigation.to',
      to.fullPath,
    )

    navigationSpan.setStatus({
      code: SpanStatusCode.OK,
    })

    navigationSpan.end()
    navigationSpan = undefined
  })

  router.onError((error) => {
    if (!navigationSpan) return

    navigationSpan.recordException(error)

    navigationSpan.setStatus({
      code: SpanStatusCode.ERROR,
      message:
        error instanceof Error
          ? error.message
          : String(error),
    })

    navigationSpan.end()
    navigationSpan = undefined
  })

  console.log(
    `OpenTelemetry frontend initialized: ${otelEndpoint}`,
  )
})
