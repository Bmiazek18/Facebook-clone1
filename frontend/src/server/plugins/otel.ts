import { NodeSDK } from '@opentelemetry/sdk-node'
import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-node'
import { resourceFromAttributes } from '@opentelemetry/resources'
import { ATTR_SERVICE_NAME, ATTR_SERVICE_VERSION } from '@opentelemetry/semantic-conventions'

export default defineNitroPlugin(() => {
  const otelEndpoint =
    process.env.NUXT_OTEL_ENDPOINT || 'http://localhost:4318'

  const resource = resourceFromAttributes({
    [ATTR_SERVICE_NAME]: 'nuxt-bff',
    [ATTR_SERVICE_VERSION]: '1.0.0',
  })

  const otlpExporter = new OTLPTraceExporter({
    url: `${otelEndpoint.replace(/\/$/, '')}/v1/traces`,
  })

  const sdk = new NodeSDK({
    resource,

    traceExporter:
      process.env.NODE_ENV === 'development' &&
      !process.env.NUXT_OTEL_ENDPOINT
        ? new ConsoleSpanExporter()
        : otlpExporter,

    instrumentations: [
      getNodeAutoInstrumentations(),
    ],
  })

  sdk.start()

  console.log(
    `OpenTelemetry initialized for nuxt-bff: ${otelEndpoint}`,
  )

  const shutdown = async () => {
    try {
      await sdk.shutdown()
      console.log('OpenTelemetry cleanly shutdown')
    } catch (err) {
      console.error('OpenTelemetry shutdown error:', err)
    }
  }

  process.once('SIGTERM', shutdown)
  process.once('SIGINT', shutdown)
})
