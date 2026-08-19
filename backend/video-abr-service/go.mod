module video-abr-service

go 1.22

require (
	github.com/rabbitmq/amqp091-go v1.10.0
	google.golang.org/grpc v1.66.2
	google.golang.org/protobuf v1.34.2
	github.com/minio/minio-go/v7 v7.0.70
	github.com/getsentry/sentry-go v0.27.0
	go.opentelemetry.io/otel v1.24.0
	go.opentelemetry.io/otel/sdk v1.24.0
	go.opentelemetry.io/otel/trace v1.24.0
	go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc v1.24.0
)
