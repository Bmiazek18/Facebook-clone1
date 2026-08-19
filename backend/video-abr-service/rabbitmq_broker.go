package main

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"sync"
	"time"

	"github.com/rabbitmq/amqp091-go"
)

const transcodeQueue = "video.transcode"

var rabbit struct {
	sync.RWMutex
	connection *amqp091.Connection
}

func rabbitURL() string {
	return envOrDefault("RABBITMQ_URL", "amqp://guest:guest@rabbitmq:5672/")
}

func envOrDefault(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}

func openRabbitConnection() (*amqp091.Connection, error) {
	connection, err := amqp091.Dial(rabbitURL())
	if err != nil {
		return nil, err
	}
	channel, err := connection.Channel()
	if err != nil {
		connection.Close()
		return nil, err
	}
	defer channel.Close()
	_, err = channel.QueueDeclare(transcodeQueue, true, false, false, false, nil)
	if err != nil {
		connection.Close()
		return nil, err
	}
	return connection, nil
}

func connection() (*amqp091.Connection, error) {
	rabbit.RLock()
	current := rabbit.connection
	rabbit.RUnlock()
	if current != nil && !current.IsClosed() {
		return current, nil
	}

	rabbit.Lock()
	defer rabbit.Unlock()
	if rabbit.connection != nil && !rabbit.connection.IsClosed() {
		return rabbit.connection, nil
	}
	newConnection, err := openRabbitConnection()
	if err != nil {
		return nil, err
	}
	rabbit.connection = newConnection
	return newConnection, nil
}

func enqueueTranscoding(job TranscodeJob) error {
	body, err := json.Marshal(job)
	if err != nil {
		return err
	}
	conn, err := connection()
	if err != nil {
		return fmt.Errorf("connect to RabbitMQ: %w", err)
	}
	channel, err := conn.Channel()
	if err != nil {
		return err
	}
	defer channel.Close()
	return channel.Publish("", transcodeQueue, false, false, amqp091.Publishing{
		ContentType:  "application/json",
		DeliveryMode: amqp091.Persistent,
		Body:         body,
	})
}

func maintainRabbitConsumer() {
	for {
		if err := consumeTranscodingJobs(); err != nil {
			log.Printf("RabbitMQ consumer disconnected: %v; retrying in 5s", err)
			time.Sleep(5 * time.Second)
		}
	}
}

func consumeTranscodingJobs() error {
	conn, err := connection()
	if err != nil {
		return err
	}
	channel, err := conn.Channel()
	if err != nil {
		return err
	}
	defer channel.Close()
	if err := channel.Qos(1, 0, false); err != nil {
		return err
	}
	deliveries, err := channel.Consume(transcodeQueue, "", false, false, false, false, nil)
	if err != nil {
		return err
	}
	log.Printf("RabbitMQ consumer listening on %q", transcodeQueue)
	for delivery := range deliveries {
		var job TranscodeJob
		if err := json.Unmarshal(delivery.Body, &job); err != nil || job.FileID == "" {
			log.Printf("Discarding invalid transcode job: %v", err)
			delivery.Ack(false)
			continue
		}
		if err := startTranscoding(job.FileID, job.UserID); err != nil {
			log.Printf("[%s] Transcoding failed: %v; message will be retried", job.FileID, err)
			delivery.Nack(false, true)
			continue
		}
		delivery.Ack(false)
	}
	return fmt.Errorf("RabbitMQ delivery channel closed")
}
