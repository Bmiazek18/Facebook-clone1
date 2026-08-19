package com.facebook.SearchService.service;

import com.facebook.SearchService.model.MeiliUser;
import com.facebook.SearchService.model.User;
import com.facebook.SearchService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.facebook.SearchService.model.Event;
import com.facebook.SearchService.model.MeiliEvent;
import com.facebook.SearchService.repository.EventRepository;

@Service
public class SearchService {

    private final Client client;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final String indexName = "users";
    private final String eventIndexName = "events";

    public SearchService(@Value("${meilisearch.host}") String host,
                         @Value("${meilisearch.api-key}") String apiKey,
                         UserRepository userRepository,
                         EventRepository eventRepository) {
        this.client = new Client(new Config(host, apiKey));
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @PostConstruct
    public void init() {
        try {
            // Check if user index exists, create if not
            try {
                client.getIndex(indexName);
            } catch (Exception e) {
                client.createIndex(indexName, "id");
            }
            // Check if event index exists, create if not
            try {
                client.getIndex(eventIndexName);
            } catch (Exception e) {
                client.createIndex(eventIndexName, "id");
            }
            // Reindex all users and events on startup to make sure data is fresh
            reindexAll();
            reindexAllEvents();
        } catch (Exception e) {
            System.err.println("Failed to initialize Meilisearch indexes: " + e.getMessage());
        }
    }

    public void reindexAll() {
        try {
            List<User> dbUsers = userRepository.findAll();
            List<MeiliUser> meiliUsers = dbUsers.stream()
                    .map(u -> new MeiliUser(
                            u.getId().toString(),
                            u.getUsername(),
                            u.getFirstName() != null ? u.getFirstName() : "",
                            u.getLastName() != null ? u.getLastName() : "",
                            u.getAvatarId() != null ? u.getAvatarId() : ""
                    ))
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            String jsonDocuments = mapper.writeValueAsString(meiliUsers);

            Index index = client.index(indexName);
            index.addDocuments(jsonDocuments);
            System.out.println("Meilisearch: Indexed " + meiliUsers.size() + " users successfully.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to reindex users: " + e.getMessage());
        }
    }

    public void indexUser(User user) {
        try {
            MeiliUser meiliUser = new MeiliUser(
                    user.getId().toString(),
                    user.getUsername(),
                    user.getFirstName() != null ? user.getFirstName() : "",
                    user.getLastName() != null ? user.getLastName() : "",
                    user.getAvatarId() != null ? user.getAvatarId() : ""
            );
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(List.of(meiliUser));
            client.index(indexName).addDocuments(jsonDocument);
            System.out.println("Meilisearch: Indexed user " + user.getId() + " successfully.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to index user " + user.getId() + ": " + e.getMessage());
        }
    }

    public List<MeiliUser> searchUsers(String query) {
        try {
            Index index = client.index(indexName);
            var searchResults = index.search(query);
            
            ObjectMapper mapper = new ObjectMapper();
            List<MeiliUser> results = new ArrayList<>();
            for (Object hit : searchResults.getHits()) {
                MeiliUser user = mapper.convertValue(hit, MeiliUser.class);
                results.add(user);
            }
            return results;
        } catch (Exception e) {
            System.err.println("Meilisearch: Search failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void reindexAllEvents() {
        try {
            List<Event> dbEvents = eventRepository.findAll();
            List<MeiliEvent> meiliEvents = dbEvents.stream()
                    .map(e -> MeiliEvent.builder()
                            .id(e.getId())
                            .userId(e.getUserId())
                            .name(e.getName())
                            .title(e.getTitle())
                            .startDate(e.getStartDate() != null ? e.getStartDate() : "")
                            .startTime(e.getStartTime() != null ? e.getStartTime() : "")
                            .endDate(e.getEndDate() != null ? e.getEndDate() : "")
                            .endTime(e.getEndTime() != null ? e.getEndTime() : "")
                            .type(e.getType() != null ? e.getType() : "")
                            .privacy(e.getPrivacy() != null ? e.getPrivacy() : "")
                            .description(e.getDescription() != null ? e.getDescription() : "")
                            .location(e.getLocation() != null ? e.getLocation() : "")
                            .locationName(e.getLocationName() != null ? e.getLocationName() : "")
                            .address(e.getAddress() != null ? e.getAddress() : "")
                            .showGuestList(e.getShowGuestList() != null ? e.getShowGuestList() : true)
                            .date(e.getDate() != null ? e.getDate() : "")
                            .frequency(e.getFrequency() != null ? e.getFrequency() : "")
                            .images(java.util.Collections.emptyList())
                            .build())
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            String jsonDocuments = mapper.writeValueAsString(meiliEvents);

            Index index = client.index(eventIndexName);
            index.addDocuments(jsonDocuments);
            System.out.println("Meilisearch: Indexed " + meiliEvents.size() + " events successfully.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to reindex events: " + e.getMessage());
        }
    }

    public void indexEvent(MeiliEvent event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(List.of(event));
            client.index(eventIndexName).addDocuments(jsonDocument);
            System.out.println("Meilisearch: Indexed event " + event.getId() + " successfully.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to index event " + event.getId() + ": " + e.getMessage());
        }
    }

    public List<MeiliEvent> searchEvents(String query) {
        try {
            Index index = client.index(eventIndexName);
            var searchResults = index.search(query);
            
            ObjectMapper mapper = new ObjectMapper();
            List<MeiliEvent> results = new ArrayList<>();
            for (Object hit : searchResults.getHits()) {
                MeiliEvent event = mapper.convertValue(hit, MeiliEvent.class);
                results.add(event);
            }
            return results;
        } catch (Exception e) {
            System.err.println("Meilisearch: Event search failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
