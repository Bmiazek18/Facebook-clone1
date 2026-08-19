# Build stage
FROM golang:1.22-alpine AS builder
WORKDIR /app

RUN apk add --no-cache protobuf protobuf-dev git
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.34.2 && \
    go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.4.0

COPY video-abr-service/go.mod ./video-abr-service/go.mod
WORKDIR /app/video-abr-service
RUN go mod download

COPY video-abr-service/*.go ./
COPY shared-proto /app/shared-proto

RUN mkdir -p pb/feed pb/notification && \
    protoc -I /app/shared-proto/src/main/proto \
      --go_out=paths=source_relative:./pb \
      --go-grpc_out=paths=source_relative:./pb \
      /app/shared-proto/src/main/proto/abr.proto && \
    protoc -I /app/shared-proto/src/main/proto \
      --go_out=paths=source_relative:./pb/feed \
      --go-grpc_out=paths=source_relative:./pb/feed \
      /app/shared-proto/src/main/proto/feed.proto && \
    protoc -I /app/shared-proto/src/main/proto \
      --go_out=paths=source_relative:./pb/notification \
      --go-grpc_out=paths=source_relative:./pb/notification \
      /app/shared-proto/src/main/proto/notification.proto

RUN go mod tidy
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/video-abr-service-bin .

# Final stage
FROM alpine:3.19
RUN apk add --no-cache ffmpeg

WORKDIR /app
COPY --from=builder /app/video-abr-service-bin ./video-abr-service

EXPOSE 8085
EXPOSE 9095
CMD ["./video-abr-service"]
