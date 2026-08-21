package com.facebook.marketplace.service;

import com.facebook.marketplace.dto.CreateListingRequest;
import com.facebook.marketplace.model.Listing;
import com.facebook.marketplace.repository.ListingRepository;
import com.facebook.marketplace.event.ListingIndexEvent;
import com.facebook.marketplace.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final RabbitTemplate rabbitTemplate;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${meilisearch.host:http://meilisearch:7700}")
    private String meiliHost;

    @org.springframework.beans.factory.annotation.Value("${meilisearch.api-key:masterKey}")
    private String meiliApiKey;

    private static final String INDEX_NAME = "listings";

    // Inicjalizacja fabryki geometrii z SRID 4326 (standardowy system współrzędnych GPS - WGS 84)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + meiliApiKey);

            try {
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>("{\"uid\":\"" + INDEX_NAME + "\",\"primaryKey\":\"id\"}", headers);
                restTemplate.postForObject(meiliHost + "/indexes", entity, String.class);
            } catch (Exception e) {
                // Index may already exist
            }

            try {
                // Configure filterableAttributes so we can search by _geoRadius using PUT
                org.springframework.http.HttpEntity<String> settingsEntity = new org.springframework.http.HttpEntity<>("[\"_geo\"]", headers);
                restTemplate.exchange(
                    meiliHost + "/indexes/" + INDEX_NAME + "/settings/filterable-attributes",
                    org.springframework.http.HttpMethod.PUT,
                    settingsEntity,
                    String.class
                );
                System.out.println("Meilisearch: Configured filterableAttributes successfully.");
            } catch (Exception e) {
                System.err.println("Failed to update Meilisearch settings: " + e.getMessage());
            }

            // Index all listings
            reindexAll();
        } catch (Exception e) {
            System.err.println("Failed to initialize Meilisearch listings index: " + e.getMessage());
        }
    }

    public void reindexAll() {
        try {
            List<Listing> allListings = listingRepository.findAll();
            indexListings(allListings);
        } catch (Exception e) {
            System.err.println("Failed to reindex listings: " + e.getMessage());
        }
    }

    private void indexListings(List<Listing> listings) {
        try {
            List<java.util.Map<String, Object>> docs = new java.util.ArrayList<>();
            for (Listing l : listings) {
                java.util.Map<String, Object> doc = new java.util.HashMap<>();
                doc.put("id", l.getId().toString());
                doc.put("title", l.getTitle());
                doc.put("price", l.getPrice());
                doc.put("category", l.getCategory());
                doc.put("condition", l.getCondition());
                doc.put("description", l.getDescription());
                if (l.getLatitude() != null && l.getLongitude() != null) {
                    doc.put("_geo", java.util.Map.of("lat", l.getLatitude(), "lng", l.getLongitude()));
                }
                docs.add(doc);
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + meiliApiKey);

            org.springframework.http.HttpEntity<List<java.util.Map<String, Object>>> entity = new org.springframework.http.HttpEntity<>(docs, headers);
            restTemplate.postForObject(meiliHost + "/indexes/" + INDEX_NAME + "/documents", entity, String.class);
            System.out.println("Meilisearch: Indexed " + docs.size() + " listings successfully.");
        } catch (Exception e) {
            System.err.println("Failed to index listings in Meilisearch: " + e.getMessage());
        }
    }

    @Transactional
    public Listing createListing(CreateListingRequest request) {
        // JTS przyjmuje kolejność współrzędnych: (Longitude, Latitude), co odpowiada (X, Y)
        Coordinate coordinate = new Coordinate(request.getLongitude(), request.getLatitude());
        Point locationPoint = geometryFactory.createPoint(coordinate);

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .category(request.getCategory())
                .condition(request.getCondition())
                .description(request.getDescription())
                .location(locationPoint)
                .build();

        Listing saved = listingRepository.save(listing);
        
        // Publish indexing event to RabbitMQ for asynchronous indexing in search-service
        ListingIndexEvent event = ListingIndexEvent.builder()
                .id(saved.getId().toString())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .price(saved.getPrice())
                .category(saved.getCategory())
                .condition(saved.getCondition())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    event
            );
            System.out.println("Marketplace: Published listing creation event to RabbitMQ for ID: " + saved.getId());
        } catch (Exception e) {
            System.err.println("Marketplace: Failed to publish indexing event to RabbitMQ: " + e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Listing> getListingsNearby(double lat, double lon, double radiusInMeters) {
        return listingRepository.findWithinDistance(lat, lon, radiusInMeters);
    }

    @Transactional(readOnly = true)
    public Listing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono ogłoszenia o ID: " + id));
    }

    public List<Listing> searchListingsMeili(String query, double lat, double lon, double radiusInMeters) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + meiliApiKey);

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("q", query != null ? query : "");
            body.put("filter", String.format("_geoRadius(%f, %f, %d)", lat, lon, Math.round(radiusInMeters)));

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
            var response = restTemplate.postForObject(meiliHost + "/indexes/" + INDEX_NAME + "/search", entity, java.util.Map.class);

            if (response != null && response.containsKey("hits")) {
                List<?> hits = (List<?>) response.get("hits");
                List<Long> ids = new java.util.ArrayList<>();
                for (Object hit : hits) {
                    if (hit instanceof java.util.Map) {
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) hit;
                        if (map.containsKey("id")) {
                            ids.add(Long.parseLong(map.get("id").toString()));
                        }
                    }
                }
                if (ids.isEmpty()) {
                    return List.of();
                }
                return listingRepository.findAllById(ids);
            }
        } catch (Exception e) {
            System.err.println("Meilisearch: Listings search failed: " + e.getMessage());
        }
        return getListingsNearby(lat, lon, radiusInMeters);
    }
}