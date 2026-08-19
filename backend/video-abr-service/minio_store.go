package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"strings"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

var s3Client *minio.Client

func initMinio() {
	endpoint := envOrDefault("MINIO_ENDPOINT", "minio:9000")
	accessKey := envOrDefault("MINIO_ACCESS_KEY", "minioadmin")
	secretKey := envOrDefault("MINIO_SECRET_KEY", "minioadmin")

	var err error
	s3Client, err = minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKey, secretKey, ""),
		Secure: false,
	})
	if err != nil {
		log.Fatalf("Failed to initialize MinIO client: %v", err)
	}
}

func downloadFromMinio(bucketName, objectName, destPath string) error {
	err := s3Client.FGetObject(context.Background(), bucketName, objectName, destPath, minio.GetObjectOptions{})
	if err != nil {
		return fmt.Errorf("failed to download object %s from bucket %s: %w", objectName, bucketName, err)
	}
	return nil
}

func uploadToMinio(bucketName, objectName, filePath, contentType string) error {
	_, err := s3Client.FPutObject(context.Background(), bucketName, objectName, filePath, minio.PutObjectOptions{
		ContentType: contentType,
	})
	if err != nil {
		return fmt.Errorf("failed to upload file %s to bucket %s: %w", objectName, bucketName, err)
	}
	return nil
}

func deleteFromMinio(bucketName, objectName string) error {
	err := s3Client.RemoveObject(context.Background(), bucketName, objectName, minio.RemoveObjectOptions{})
	if err != nil {
		return fmt.Errorf("failed to remove object %s from bucket %s: %w", objectName, bucketName, err)
	}
	return nil
}

func uploadDirectoryToMinio(localDir, s3Prefix, bucketName string) error {
	err := filepath.Walk(localDir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if info.IsDir() {
			return nil
		}
		rel, err := filepath.Rel(localDir, path)
		if err != nil {
			return err
		}
		s3Key := filepath.Join(s3Prefix, rel)
		s3Key = strings.ReplaceAll(s3Key, "\\", "/")

		contentType := "application/octet-stream"
		if strings.HasSuffix(path, ".m3u8") {
			contentType = "application/x-mpegURL"
		} else if strings.HasSuffix(path, ".ts") {
			contentType = "video/MP2T"
		}

		return uploadToMinio(bucketName, s3Key, path, contentType)
	})
	return err
}
