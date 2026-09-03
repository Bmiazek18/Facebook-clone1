package main

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/minio/minio-go/v7"
)

const (
	ServerPort   = ":8085"
	sharedSecret = "secret-media-key"
)

type TusdHook struct {
	Type  string `json:"Type"`
	Event struct {
		Upload struct {
			ID       string            `json:"ID"`
			Size     int64             `json:"Size"`
			MetaData map[string]string `json:"MetaData"`
		} `json:"Upload"`
	} `json:"Event"`
}

func verifyRequest(w http.ResponseWriter, r *http.Request) bool {
	expires := r.URL.Query().Get("expires")
	signature := r.URL.Query().Get("signature")
	if expires != "" && signature != "" {
		path := r.URL.Path
		if verifySignature(path, expires, signature) {
			setHlsCookie(w, expires, signature)
			return true
		}
		// If HLS master playlist, also verify using the files/media path used when signing post media
		if strings.HasSuffix(path, "/master.m3u8") {
			parts := strings.Split(strings.TrimPrefix(path, "/videos/"), "/")
			if len(parts) > 0 {
				fileId := parts[0]
				for _, prefix := range []string{"/files/", "/media/"} {
					if verifySignature(prefix+fileId, expires, signature) {
						setHlsCookie(w, expires, signature)
						return true
					}
				}
			}
		}
		return false
	}

	// Fallback to cookie check
	cookie, err := r.Cookie("hls-session")
	if err == nil {
		parts := strings.Split(cookie.Value, ":")
		if len(parts) == 2 {
			cookieExpires := parts[0]
			cookieSig := parts[1]
			expected := generateSignature("hls-session", cookieExpires, sharedSecret)
			if hmac.Equal([]byte(expected), []byte(cookieSig)) {
				expiresVal, err := strconv.ParseInt(cookieExpires, 10, 64)
				if err == nil && time.Now().Unix() <= expiresVal {
					return true
				}
			}
		}
	}

	return false
}

func setHlsCookie(w http.ResponseWriter, originalExpires, originalSignature string) {
	cookieExpires := time.Now().Add(1 * time.Hour).Unix()
	cookieExpiresStr := strconv.FormatInt(cookieExpires, 10)
	cookieSig := generateSignature("hls-session", cookieExpiresStr, sharedSecret)
	http.SetCookie(w, &http.Cookie{
		Name:     "hls-session",
		Value:    cookieExpiresStr + ":" + cookieSig,
		Path:     "/",
		HttpOnly: true,
		SameSite: http.SameSiteLaxMode,
		MaxAge:   3600,
	})
}

func handleProxyVideo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Range, Authorization")
	w.Header().Set("Access-Control-Expose-Headers", "Content-Length, Content-Range")
	if r.Method == "OPTIONS" {
		w.WriteHeader(http.StatusOK)
		return
	}

	if !verifyRequest(w, r) {
		http.Error(w, "Unauthorized: link expired or invalid", http.StatusUnauthorized)
		return
	}

	path := r.URL.Path
	var s3Key string
	if strings.HasPrefix(path, "/videos/") {
		s3Key = "videos/" + strings.TrimPrefix(path, "/videos/")
	} else if strings.HasPrefix(path, "/stories/") {
		s3Key = "stories/" + strings.TrimPrefix(path, "/stories/")
	} else {
		http.Error(w, "Bad request", http.StatusBadRequest)
		return
	}

	object, err := s3Client.GetObject(context.Background(), "feed-uploads", s3Key, minio.GetObjectOptions{})
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}
	defer object.Close()

	info, err := object.Stat()
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", info.ContentType)
	w.Header().Set("Content-Length", strconv.FormatInt(info.Size, 10))

	http.ServeContent(w, r, info.Key, info.LastModified, object)
}

func handleProxyMedia(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Range, Authorization")
	w.Header().Set("Access-Control-Expose-Headers", "Content-Length, Content-Range, Content-Type")
	if r.Method == "OPTIONS" {
		w.WriteHeader(http.StatusOK)
		return
	}
	if r.Method != http.MethodGet && r.Method != http.MethodHead {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	if !verifyRequest(w, r) {
		http.Error(w, "Unauthorized: link expired or invalid", http.StatusUnauthorized)
		return
	}

	fileId := normalizeTusFileId(strings.TrimPrefix(r.URL.Path, "/media/"))
	if fileId == "" || strings.Contains(fileId, "/") {
		http.Error(w, "Bad request", http.StatusBadRequest)
		return
	}

	object, bucket, err := openMediaObject(fileId)
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}
	defer object.Close()

	info, err := object.Stat()
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}

	contentType := info.ContentType
	if contentType == "" || contentType == "application/octet-stream" {
		contentType = "application/octet-stream"
	}
	w.Header().Set("Content-Type", contentType)
	w.Header().Set("Content-Length", strconv.FormatInt(info.Size, 10))
	w.Header().Set("Cache-Control", "private, max-age=3600")

	if r.Method == http.MethodHead {
		w.WriteHeader(http.StatusOK)
		return
	}

	http.ServeContent(w, r, bucket+"/"+info.Key, info.LastModified, object)
}

func openMediaObject(fileId string) (*minio.Object, string, error) {
	object, err := s3Client.GetObject(context.Background(), "feed-uploads", fileId, minio.GetObjectOptions{})
	if err == nil {
		if _, statErr := object.Stat(); statErr == nil {
			return object, "feed-uploads", nil
		}
		object.Close()
	}
	// Fallback while upload is still in the tus temporary bucket
	object, err = s3Client.GetObject(context.Background(), "poczekalnia", fileId, minio.GetObjectOptions{})
	if err != nil {
		return nil, "", err
	}
	if _, statErr := object.Stat(); statErr != nil {
		object.Close()
		return nil, "", statErr
	}
	return object, "poczekalnia", nil
}

func handleHealth(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("OK"))
}

func normalizeTusFileId(fileId string) string {
	if idx := strings.Index(fileId, "+"); idx != -1 {
		return fileId[:idx]
	}
	return fileId
}

func processNonVideoUpload(fileId, filename, filetype string) error {
	fileId = normalizeTusFileId(fileId)
	localPath := filepath.Join(UploadDir, fileId)
	if err := downloadFromMinio("poczekalnia", fileId, localPath); err != nil {
		return err
	}
	defer os.Remove(localPath)

	if err := uploadToMinio("feed-uploads", fileId, localPath, filetype); err != nil {
		return err
	}

	_ = deleteFromMinio("poczekalnia", fileId)
	_ = deleteFromMinio("poczekalnia", fileId+".info")
	return nil
}

func handleTusdHook(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	body, err := io.ReadAll(r.Body)
	if err != nil {
		log.Printf("Error reading request body: %v", err)
		http.Error(w, "Error reading request body", http.StatusBadRequest)
		return
	}

	var hook TusdHook
	if err := json.Unmarshal(body, &hook); err != nil {
		log.Printf("Error decoding JSON: %v", err)
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	log.Printf("Received tusd hook: Type=%s, UploadID=%s", hook.Type, hook.Event.Upload.ID)

	if hook.Type == "post-finish" {
		isVid := isVideoMetadata(
			hook.Event.Upload.MetaData["filetype"],
			hook.Event.Upload.MetaData["filename"],
		)

		uploadId := normalizeTusFileId(hook.Event.Upload.ID)
		filename := hook.Event.Upload.MetaData["filename"]
		filetype := hook.Event.Upload.MetaData["filetype"]

		if isVid {
			userId := hook.Event.Upload.MetaData["userid"]
			if err := enqueueTranscoding(TranscodeJob{FileID: uploadId, UserID: userId}); err != nil {
				log.Printf("Could not enqueue transcoding for %s: %v", uploadId, err)
				http.Error(w, "Transcoding queue unavailable", http.StatusServiceUnavailable)
				return
			}
			log.Printf("File %s (%s) queued for ABR transcoding", uploadId, filename)
		} else if strings.HasPrefix(strings.ToLower(filename), "story-") {
			log.Printf("File %s (%s) is a story upload; leaving in poczekalnia for ProcessStoryImage", uploadId, filename)
		} else {
			log.Printf("File %s (%s) is not a video. Processing non-video upload.", uploadId, filename)
			go func() {
				if err := processNonVideoUpload(uploadId, filename, filetype); err != nil {
					log.Printf("Failed to process non-video upload for %s: %v", uploadId, err)
				} else {
					log.Printf("Successfully processed and moved non-video upload %s to feed-uploads", uploadId)
				}
			}()
		}
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(`{"status":"success"}`))
}

func handleManualTranscode(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	id := r.URL.Query().Get("id")
	if id == "" {
		http.Error(w, "Missing 'id' parameter", http.StatusBadRequest)
		return
	}

	_, err := s3Client.StatObject(context.Background(), "poczekalnia", id, minio.StatObjectOptions{})
	if err != nil {
		http.Error(w, fmt.Sprintf("File %s does not exist in poczekalnia bucket", id), http.StatusNotFound)
		return
	}

	if err := enqueueTranscoding(TranscodeJob{FileID: id}); err != nil {
		log.Printf("Could not enqueue manual transcoding for %s: %v", id, err)
		http.Error(w, "Transcoding queue unavailable", http.StatusServiceUnavailable)
		return
	}
	log.Printf("Manually queued transcoding for UploadID=%s", id)

	w.WriteHeader(http.StatusAccepted)
	w.Write([]byte(fmt.Sprintf(`{"status":"processing","id":"%s"}`, id)))
}

func handleVideoInfo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "*")
	if r.Method == "OPTIONS" {
		w.WriteHeader(http.StatusOK)
		return
	}

	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	parts := strings.Split(r.URL.Path, "/")
	if len(parts) < 4 {
		http.Error(w, "Missing ID", http.StatusBadRequest)
		return
	}
	id := parts[3]
	if id == "" {
		http.Error(w, "Missing ID", http.StatusBadRequest)
		return
	}

	isVid, err := detectVideo(id)
	if err != nil {
		http.Error(w, "Not found", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]interface{}{
		"id":      id,
		"isVideo": isVid,
	})
}

func detectVideo(id string) (bool, error) {
	id = normalizeTusFileId(id)
	object, err := s3Client.GetObject(context.Background(), "poczekalnia", id+".info", minio.GetObjectOptions{})
	if err == nil {
		defer object.Close()
		data, err := io.ReadAll(object)
		if err == nil {
			var info struct {
				MetaData map[string]string `json:"MetaData"`
			}
			if err := json.Unmarshal(data, &info); err == nil {
				return isVideoMetadata(info.MetaData["filetype"], info.MetaData["filename"]), nil
			}
		}
	}

	_, err = s3Client.StatObject(context.Background(), "feed-uploads", "videos/"+id+"/master.m3u8", minio.StatObjectOptions{})
	if err == nil {
		return true, nil
	}
	_, err = s3Client.StatObject(context.Background(), "feed-uploads", id, minio.StatObjectOptions{})
	if err == nil {
		return false, nil
	}
	return false, fmt.Errorf("media %s not found", id)
}

func isVideoMetadata(filetype, filename string) bool {
	lowerName := strings.ToLower(filename)
	return strings.HasPrefix(filetype, "video/") ||
		strings.HasSuffix(lowerName, ".mp4") ||
		strings.HasSuffix(lowerName, ".mov") ||
		strings.HasSuffix(lowerName, ".avi") ||
		strings.HasSuffix(lowerName, ".mkv")
}

func generateSignature(path string, expires string, secret string) string {
	h := hmac.New(sha256.New, []byte(secret))
	h.Write([]byte(path + expires))
	return hex.EncodeToString(h.Sum(nil))
}

func verifySignature(path string, expiresStr string, signature string) bool {
	expires, err := strconv.ParseInt(expiresStr, 10, 64)
	if err != nil {
		return false
	}
	if time.Now().Unix() > expires {
		return false
	}
	expected := generateSignature(path, expiresStr, sharedSecret)
	return hmac.Equal([]byte(expected), []byte(signature))
}
