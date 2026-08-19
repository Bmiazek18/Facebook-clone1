package main

import (
	"context"
	"fmt"
	"log"
	"net"
	"os"
	"path/filepath"
	"strings"
	"time"

	feedpb "video-abr-service/pb/feed"
	notificationpb "video-abr-service/pb/notification"
	abrpb "video-abr-service/pb"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

const GrpcPort = ":9095"

type abrServer struct {
	abrpb.UnimplementedAbrGrpcServiceServer
}

func startGrpcServer() {
	listener, err := net.Listen("tcp", GrpcPort)
	if err != nil {
		log.Fatalf("failed to listen on gRPC port %s: %v", GrpcPort, err)
	}

	server := grpc.NewServer()
	abrpb.RegisterAbrGrpcServiceServer(server, &abrServer{})
	log.Printf("Starting Video ABR gRPC service on port %s...", GrpcPort)

	if err := server.Serve(listener); err != nil {
		log.Fatalf("gRPC server failed: %v", err)
	}
}

func (s *abrServer) GetVideoInfo(ctx context.Context, request *abrpb.GetVideoInfoRequest) (*abrpb.GetVideoInfoResponse, error) {
	isVid, err := detectVideo(request.GetFileId())
	if err != nil {
		return nil, err
	}
	return &abrpb.GetVideoInfoResponse{
		FileId:  request.GetFileId(),
		IsVideo: isVid,
	}, nil
}

func (s *abrServer) ProcessStoryImage(ctx context.Context, request *abrpb.ProcessStoryImageRequest) (*abrpb.ProcessStoryImageResponse, error) {
	fileId := normalizeTusFileId(request.GetFileId())
	originalPath := filepath.Join(UploadDir, fileId)

	if err := downloadFromMinio("poczekalnia", fileId, originalPath); err != nil {
		if err2 := downloadFromMinio("feed-uploads", fileId, originalPath); err2 != nil {
			return &abrpb.ProcessStoryImageResponse{Success: false, Error: fmt.Sprintf("failed to download from poczekalnia: %v", err)}, nil
		}
	}
	defer os.Remove(originalPath)

	fullPath, thumbPath, err := generateStoryVariants(fileId)
	if err != nil {
		return &abrpb.ProcessStoryImageResponse{Success: false, Error: err.Error()}, nil
	}

	s3Prefix := "stories/" + fileId
	localStoriesDir := filepath.Join(StoriesDir, fileId)
	if err := uploadDirectoryToMinio(localStoriesDir, s3Prefix, "feed-uploads"); err != nil {
		return &abrpb.ProcessStoryImageResponse{Success: false, Error: fmt.Sprintf("failed to upload variants: %v", err)}, nil
	}
	_ = os.RemoveAll(localStoriesDir)

	_ = deleteFromMinio("poczekalnia", fileId)
	_ = deleteFromMinio("poczekalnia", fileId+".info")
	_ = deleteFromMinio("feed-uploads", fileId)

	return &abrpb.ProcessStoryImageResponse{
		Success: true,
		Variants: []*abrpb.StoryImageVariant{
			{Width: 1080, Height: 1920, Path: fullPath},
			{Width: 227, Height: 403, Path: thumbPath},
		},
	}, nil
}

func (s *abrServer) EnqueueVideoTranscode(ctx context.Context, request *abrpb.EnqueueVideoTranscodeRequest) (*abrpb.EnqueueVideoTranscodeResponse, error) {
	if err := enqueueTranscoding(TranscodeJob{FileID: request.GetFileId(), UserID: request.GetUserId()}); err != nil {
		return nil, err
	}
	return &abrpb.EnqueueVideoTranscodeResponse{Queued: true}, nil
}

func updatePostStatusInFeedService(fileId, status string) {
	address := strings.TrimPrefix(envOrDefault("FEED_SERVICE_GRPC_ADDRESS", "dns:///feedservice:9092"), "dns:///")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address, grpc.WithTransportCredentials(insecure.NewCredentials()), grpc.WithBlock())
	if err != nil {
		log.Printf("Failed to connect to FeedService gRPC for media %s: %v", fileId, err)
		return
	}
	defer conn.Close()

	client := feedpb.NewFeedGrpcServiceClient(conn)
	ctx, cancel = context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	response, err := client.UpdatePostMediaStatus(ctx, &feedpb.UpdatePostMediaStatusRequest{
		FileId: fileId,
		Status: status,
	})
	if err != nil {
		log.Printf("Failed to update status in FeedService for media %s: %v", fileId, err)
		return
	}

	log.Printf("FeedService gRPC status update for media %s succeeded=%t updated=%d", fileId, response.GetSuccess(), response.GetUpdatedCount())
}

func sendNotification(userId, title, message string) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	address := strings.TrimPrefix(envOrDefault("NOTIFICATION_SERVICE_GRPC_ADDRESS", "dns:///notificationservice:9093"), "dns:///")
	conn, err := grpc.DialContext(ctx, address, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Printf("Failed to dial notification service gRPC: %v", err)
		return
	}
	defer conn.Close()

	client := notificationpb.NewNotificationGrpcServiceClient(conn)
	req := &notificationpb.SendNotificationRequest{
		UserId:   userId,
		Title:    title,
		Message:  message,
		TargetId: "",
	}

	_, err = client.SendNotification(ctx, req)
	if err != nil {
		log.Printf("Failed to send notification via gRPC: %v", err)
		return
	}

	log.Printf("Notification sent successfully via gRPC to user %s", userId)
}
