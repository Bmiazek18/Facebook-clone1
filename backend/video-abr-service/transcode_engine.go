package main

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/getsentry/sentry-go"
)

const (
	UploadDir    = "/app/uploads"
	TranscodeDir = "/app/uploads/transcoded"
	StoriesDir   = "/app/uploads/stories"
)

type TranscodeJob struct {
	FileID string `json:"fileId"`
	UserID string `json:"userId,omitempty"`
}

func startTranscoding(fileId string, userId string) error {
	fileId = normalizeTusFileId(fileId)
	if userId != "" {
		sendNotification(userId, "Przetwarzanie wideo", "Twoje wideo się renderuje i niedługo będzie dostępne.")
	}

	inputFile := filepath.Join(UploadDir, fileId)
	outputFolder := filepath.Join(TranscodeDir, fileId)

	// Download from MinIO poczekalnia bucket
	if err := downloadFromMinio("poczekalnia", fileId, inputFile); err != nil {
		return fmt.Errorf("failed to download video %s from poczekalnia bucket: %w", fileId, err)
	}
	defer os.Remove(inputFile)

	// Clean up any existing output folder
	if err := os.RemoveAll(outputFolder); err != nil {
		return fmt.Errorf("clean output folder: %w", err)
	}

	// Create directories for HLS variants
	for _, variant := range []string{"0", "1", "2"} {
		variantPath := filepath.Join(outputFolder, variant)
		if err := os.MkdirAll(variantPath, 0755); err != nil {
			log.Printf("[%s] Failed to create output directory %s: %v", fileId, variantPath, err)
			return fmt.Errorf("create output directory: %w", err)
		}
	}

	log.Printf("[%s] Checking if video has audio stream...", fileId)
	hasAudio := checkAudioStream(inputFile)
	log.Printf("[%s] Audio stream present: %t", fileId, hasAudio)

	var cmd *exec.Cmd
	if hasAudio {
		// Transcode with audio
		cmd = exec.Command("ffmpeg", "-y", "-i", inputFile,
			"-filter_complex", "[0:v]split=3[v1][v2][v3]; [v1]scale=w=1280:h=720[v1out]; [v2]scale=w=854:h=480[v2out]; [v3]scale=w=640:h=360[v3out]",
			"-map", "[v1out]", "-c:v:0", "h264_nvenc", "-b:v:0", "2000k", "-maxrate:v:0", "2200k", "-bufsize:v:0", "3000k",
			"-map", "[v2out]", "-c:v:1", "h264_nvenc", "-b:v:1", "1000k", "-maxrate:v:1", "1100k", "-bufsize:v:1", "1500k",
			"-map", "[v3out]", "-c:v:2", "h264_nvenc", "-b:v:2", "500k", "-maxrate:v:2", "550k", "-bufsize:v:2", "800k",
			"-map", "a:0", "-c:a:0", "aac", "-b:a:0", "128k",
			"-map", "a:0", "-c:a:1", "aac", "-b:a:1", "96k",
			"-map", "a:0", "-c:a:2", "aac", "-b:a:2", "64k",
			"-f", "hls",
			"-hls_time", "6",
			"-hls_playlist_type", "vod",
			"-hls_segment_filename", filepath.Join(outputFolder, "%v", "segment_%03d.ts"),
			"-master_pl_name", "master.m3u8",
			"-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
			filepath.Join(outputFolder, "%v", "index.m3u8"),
		)
	} else {
		// Transcode video only
		cmd = exec.Command("ffmpeg", "-y", "-i", inputFile,
			"-filter_complex", "[0:v]split=3[v1][v2][v3]; [v1]scale=w=1280:h=720[v1out]; [v2]scale=w=854:h=480[v2out]; [v3]scale=w=640:h=360[v3out]",
			"-map", "[v1out]", "-c:v:0", "h264_nvenc", "-b:v:0", "2000k", "-maxrate:v:0", "2200k", "-bufsize:v:0", "3000k",
			"-map", "[v2out]", "-c:v:1", "h264_nvenc", "-b:v:1", "1000k", "-maxrate:v:1", "1100k", "-bufsize:v:1", "1500k",
			"-map", "[v3out]", "-c:v:2", "h264_nvenc", "-b:v:2", "500k", "-maxrate:v:2", "550k", "-bufsize:v:2", "800k",
			"-f", "hls",
			"-hls_time", "6",
			"-hls_playlist_type", "vod",
			"-hls_segment_filename", filepath.Join(outputFolder, "%v", "segment_%03d.ts"),
			"-master_pl_name", "master.m3u8",
			"-var_stream_map", "v:0 v:1 v:2",
			filepath.Join(outputFolder, "%v", "index.m3u8"),
		)
	}

	var stderr bytes.Buffer
	cmd.Stderr = &stderr

	log.Printf("[%s] Executing ffmpeg ABR HLS transcoding...", fileId)
	if err := cmd.Run(); err != nil {
		sentry.CaptureException(fmt.Errorf("ffmpeg failed for file %s: %v, stderr: %s", fileId, err, stderr.String()))
		log.Printf("[%s] ffmpeg failed: %v, stderr: %s", fileId, err, stderr.String())
		return fmt.Errorf("ffmpeg: %w", err)
	}

	log.Printf("[%s] ABR transcoding completed successfully. Uploading to feed-uploads bucket...", fileId)

	// Upload HLS directory to feed-uploads bucket
	s3Prefix := "videos/" + fileId
	if err := uploadDirectoryToMinio(outputFolder, s3Prefix, "feed-uploads"); err != nil {
		return fmt.Errorf("failed to upload transcoded HLS directory to MinIO: %w", err)
	}

	// Clean up local files
	_ = os.RemoveAll(outputFolder)

	// Clean up original file and .info from poczekalnia
	_ = deleteFromMinio("poczekalnia", fileId)
	_ = deleteFromMinio("poczekalnia", fileId+".info")

	// Update post status in FeedService
	updatePostStatusInFeedService(fileId, "ACTIVE")

	if userId != "" {
		sendNotification(userId, "Wideo gotowe", "Twoje wideo jest już gotowe do wyświetlenia.")
	}
	return nil
}

func generateStoryVariants(fileID string) (string, string, error) {
	inputFile := filepath.Join(UploadDir, fileID)
	outputDir := filepath.Join(StoriesDir, fileID)
	if err := os.RemoveAll(outputDir); err != nil {
		return "", "", err
	}
	if err := os.MkdirAll(outputDir, 0755); err != nil {
		return "", "", err
	}

	fullRelative := filepath.Join("stories", fileID, "1080x1920.jpg")
	thumbRelative := filepath.Join("stories", fileID, "227x403.jpg")
	fullOutput := filepath.Join(UploadDir, fullRelative)
	thumbOutput := filepath.Join(UploadDir, thumbRelative)

	if err := runStoryScale(inputFile, fullOutput, 1080, 1920); err != nil {
		return "", "", err
	}
	if err := runStoryScale(inputFile, thumbOutput, 227, 403); err != nil {
		return "", "", err
	}

	return fullRelative, thumbRelative, nil
}

func runStoryScale(inputFile, outputFile string, width, height int) error {
	if err := os.MkdirAll(filepath.Dir(outputFile), 0755); err != nil {
		return err
	}
	filter := fmt.Sprintf(
		"scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2:color=black",
		width, height, width, height,
	)
	cmd := exec.Command(
		"ffmpeg", "-y", "-i", inputFile,
		"-vf", filter,
		"-frames:v", "1",
		outputFile,
	)
	var stderr bytes.Buffer
	cmd.Stderr = &stderr
	if err := cmd.Run(); err != nil {
		sentry.CaptureException(fmt.Errorf("ffmpeg story resize failed for file %s: %v, stderr: %s", inputFile, err, stderr.String()))
		return fmt.Errorf("ffmpeg story resize failed: %w: %s", err, stderr.String())
	}
	return nil
}

func checkAudioStream(inputFile string) bool {
	cmd := exec.Command("ffprobe", "-v", "error", "-select_streams", "a", "-show_entries", "stream=codec_name", "-of", "default=noprint_wrappers=1:nokey=1", inputFile)
	var out bytes.Buffer
	cmd.Stdout = &out
	if err := cmd.Run(); err != nil {
		log.Printf("ffprobe failed on %s: %v", inputFile, err)
		return false
	}
	return strings.TrimSpace(out.String()) != ""
}
