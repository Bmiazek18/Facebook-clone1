import {
  ApolloLink,
  Observable,
  type FetchResult,
  type Operation,
} from '@apollo/client'

import {
  trace,
  SpanStatusCode,
  type Span,
} from '@opentelemetry/api'

const tracer = trace.getTracer('apollo-client')

export function createApolloTracingLink() {
  return new ApolloLink((operation, forward) => {
    return new Observable<FetchResult>((observer) => {
      const operationName =
        operation.operationName || 'anonymous'

      let span: Span | undefined

      tracer.startActiveSpan(
        `apollo.${operationName}`,
        {
          attributes: {
            'graphql.operation.name': operationName,
            'graphql.operation.type': getOperationType(operation),
            'graphql.client': 'apollo',
          },
        },
        (activeSpan) => {
          span = activeSpan

          const subscription = forward(operation).subscribe({
            next: (result) => {
              if (result.errors?.length) {
                span?.setStatus({
                  code: SpanStatusCode.ERROR,
                  message: result.errors[0]?.message,
                })

                span?.setAttribute(
                  'graphql.errors.count',
                  result.errors.length,
                )
              } else {
                span?.setStatus({
                  code: SpanStatusCode.OK,
                })
              }

              observer.next(result)
            },

            error: (error) => {
              span?.recordException(error)

              span?.setStatus({
                code: SpanStatusCode.ERROR,
                message:
                  error instanceof Error
                    ? error.message
                    : String(error),
              })

              observer.error(error)

              span?.end()
              span = undefined
            },

            complete: () => {
              span?.end()
              span = undefined

              observer.complete()
            },
          })

          return () => {
            subscription.unsubscribe()

            if (span) {
              span.setAttribute(
                'graphql.operation.cancelled',
                true,
              )

              span.end()
              span = undefined
            }
          }
        },
      )
    })
  })
}

function getOperationType(operation: Operation): string {
  const definition = operation.query.definitions.find(
    (definition) =>
      definition.kind === 'OperationDefinition',
  )

  if (
    definition?.kind === 'OperationDefinition'
  ) {
    return definition.operation
  }

  return 'unknown'
}