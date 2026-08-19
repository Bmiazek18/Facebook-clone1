package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/getsentry/sentry-go"
)

func main() {
	// Initialize Sentry
	sentryDsn := envOrDefault("SENTRY_DSN", "")
	if sentryDsn != "" {
		err := sentry.Init(sentry.ClientOptions{
			Dsn:              sentryDsn,
			Environment:      envOrDefault("SENTRY_ENVIRONMENT", "production"),
			EnableTracing:    true,
			TracesSampleRate: 0.1,
		})
		if err != nil {
			log.Printf("Failed to initialize Sentry: %v", err)
		} else {
			log.Println("Sentry initialized successfully.")
			defer sentry.Flush(2 * time.Second)
		}
	}

	// Initialize OpenTelemetry
	ctx := context.Background()
	shutdownTracer, err := initTracer(ctx)
	if err != nil {
		log.Printf("Failed to initialize OpenTelemetry: %v", err)
	} else {
		log.Println("OpenTelemetry tracer initialized successfully.")
		defer func() {
			if err := shutdownTracer(ctx); err != nil {
				log.Printf("Failed to shutdown tracer provider: %v", err)
			}
		}()
	}

	// Create transcoded directory if it doesn't exist
	if err := os.MkdirAll(TranscodeDir, 0755); err != nil {
		log.Fatalf("Failed to create transcode directory: %v", err)
	}
	if err := os.MkdirAll(StoriesDir, 0755); err != nil {
		log.Fatalf("Failed to create stories directory: %v", err)
	}

	initMinio()

	go maintainRabbitConsumer()
	go startGrpcServer()

	// Routes
	http.HandleFunc("/health", handleHealth)
	http.HandleFunc("/hooks", handleTusdHook)
	http.HandleFunc("/transcode", handleManualTranscode)
	http.HandleFunc("/videos/info/", handleVideoInfo)

	// Proxy HLS videos, stories, and permanent post media from MinIO bucket feed-uploads
	http.HandleFunc("/videos/", handleProxyVideo)
	http.HandleFunc("/stories/", handleProxyVideo)
	http.HandleFunc("/media/", handleProxyMedia)

	log.Printf("Starting Video ABR Service on port %s...", ServerPort)
	if err := http.ListenAndServe(ServerPort, nil); err != nil {
		log.Fatalf("HTTP server failed: %v", err)
	}
}
