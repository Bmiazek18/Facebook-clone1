package com.facebook.FeedService.grpc.handler;

import com.facebook.FeedService.entity.EventEntity;
import com.facebook.FeedService.repository.EventRepository;
import com.facebook.feed.grpc.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventGrpcHandler {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public void createEvent(CreateEventRequest request, StreamObserver<CreateEventResponse> responseObserver) {
        log.info("gRPC: Creating event: {}", request.getName());
        try {
            String eventId = request.getId();
            if (eventId == null || eventId.trim().isEmpty()) {
                eventId = UUID.randomUUID().toString();
            }

            EventEntity entity = EventEntity.builder()
                    .id(eventId)
                    .userId(request.getUserId())
                    .name(request.getName())
                    .title(request.getTitle())
                    .startDate(request.getStartDate())
                    .startTime(request.getStartTime())
                    .endDate(request.getEndDate())
                    .endTime(request.getEndTime())
                    .type(request.getType())
                    .privacy(request.getPrivacy())
                    .description(request.getDescription())
                    .images(request.getImagesList() != null ? new ArrayList<>(request.getImagesList()) : new ArrayList<>())
                    .location(request.getLocation())
                    .locationName(request.getLocationName())
                    .address(request.getAddress())
                    .showGuestList(request.getShowGuestList())
                    .hosts(request.getHostsList() != null ? new ArrayList<>(request.getHostsList()) : new ArrayList<>())
                    .date(request.getDate())
                    .coordinates(request.getCoordinatesList() != null ? new ArrayList<>(request.getCoordinatesList()) : new ArrayList<>())
                    .frequency(request.getFrequency())
                    .responses(0)
                    .guestsGoing(0)
                    .guestsInterested(0)
                    .build();

            EventEntity saved = eventRepository.save(entity);

            syncEventToSearchService(saved);

            Event protoEvent = mapToProtoEvent(saved);
            responseObserver.onNext(CreateEventResponse.newBuilder().setEvent(protoEvent).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create event", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getEventById(GetEventByIdRequest request, StreamObserver<GetEventByIdResponse> responseObserver) {
        log.info("gRPC: Fetching event by id: {}", request.getId());
        try {
            Optional<EventEntity> entityOpt = eventRepository.findById(request.getId());
            if (entityOpt.isPresent()) {
                Event protoEvent = mapToProtoEvent(entityOpt.get());
                responseObserver.onNext(GetEventByIdResponse.newBuilder().setEvent(protoEvent).build());
            } else {
                responseObserver.onNext(GetEventByIdResponse.newBuilder().build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get event by id", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getEvents(GetEventsRequest request, StreamObserver<GetEventsResponse> responseObserver) {
        log.info("gRPC: Fetching events");
        try {
            List<EventEntity> entities = eventRepository.findAll();
            List<Event> protoEvents = entities.stream()
                    .map(this::mapToProtoEvent)
                    .collect(Collectors.toList());

            responseObserver.onNext(GetEventsResponse.newBuilder().addAllEvents(protoEvents).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get events", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public Event mapToProtoEvent(EventEntity entity) {
        return Event.newBuilder()
                .setId(entity.getId() != null ? entity.getId() : "")
                .setUserId(entity.getUserId() != null ? entity.getUserId() : "")
                .setName(entity.getName() != null ? entity.getName() : "")
                .setTitle(entity.getTitle() != null ? entity.getTitle() : "")
                .setStartDate(entity.getStartDate() != null ? entity.getStartDate() : "")
                .setStartTime(entity.getStartTime() != null ? entity.getStartTime() : "")
                .setEndDate(entity.getEndDate() != null ? entity.getEndDate() : "")
                .setEndTime(entity.getEndTime() != null ? entity.getEndTime() : "")
                .setType(entity.getType() != null ? entity.getType() : "")
                .setPrivacy(entity.getPrivacy() != null ? entity.getPrivacy() : "")
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "")
                .addAllImages(entity.getImages() != null ? entity.getImages() : Collections.emptyList())
                .setLocation(entity.getLocation() != null ? entity.getLocation() : "")
                .setLocationName(entity.getLocationName() != null ? entity.getLocationName() : "")
                .setAddress(entity.getAddress() != null ? entity.getAddress() : "")
                .setShowGuestList(entity.getShowGuestList() != null ? entity.getShowGuestList() : true)
                .addAllHosts(entity.getHosts() != null ? entity.getHosts() : Collections.emptyList())
                .setDate(entity.getDate() != null ? entity.getDate() : "")
                .setResponses(entity.getResponses() != null ? entity.getResponses() : 0)
                .setGuestsGoing(entity.getGuestsGoing() != null ? entity.getGuestsGoing() : 0)
                .setGuestsInterested(entity.getGuestsInterested() != null ? entity.getGuestsInterested() : 0)
                .addAllCoordinates(entity.getCoordinates() != null ? entity.getCoordinates() : Collections.emptyList())
                .setFrequency(entity.getFrequency() != null ? entity.getFrequency() : "")
                .build();
    }

    private void syncEventToSearchService(EventEntity event) {
        String searchServiceUrl = System.getenv().getOrDefault("SEARCH_SERVICE_URL", "http://search-service:8088");
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", event.getId());
            payload.put("userId", event.getUserId());
            payload.put("name", event.getName());
            payload.put("title", event.getTitle());
            payload.put("startDate", event.getStartDate());
            payload.put("startTime", event.getStartTime());
            payload.put("endDate", event.getEndDate());
            payload.put("endTime", event.getEndTime());
            payload.put("type", event.getType());
            payload.put("privacy", event.getPrivacy());
            payload.put("description", event.getDescription());
            payload.put("location", event.getLocation());
            payload.put("locationName", event.getLocationName());
            payload.put("address", event.getAddress());
            payload.put("showGuestList", event.getShowGuestList());
            payload.put("date", event.getDate());
            payload.put("frequency", event.getFrequency());
            payload.put("images", event.getImages());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest searchReq = HttpRequest.newBuilder()
                    .uri(URI.create(searchServiceUrl + "/api/search/events/index"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(2))
                    .build();
            HttpResponse<String> response = httpClient.send(searchReq, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Successfully synced event {} to search-service", event.getId());
            } else {
                log.error("Failed to sync event to search-service, status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to sync event to search-service", e);
        }
    }
}
