package com.facebook.SearchService.service;

import com.facebook.SearchService.client.UserServiceClient;
import com.facebook.SearchService.model.MeiliEvent;
import com.facebook.SearchService.model.MeiliUser;
import com.facebook.SearchService.model.User;
import com.facebook.SearchService.model.Event;
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

import com.facebook.user.grpc.GetAllUsersResponse;
import com.facebook.user.grpc.UserDto;

@Service
public class SearchService {

    private final Client client;
    private final String indexName = "users";
    private final String eventIndexName = "events";
    private final UserServiceClient userServiceClient;

    public SearchService(@Value("${meilisearch.host}") String host,
                         @Value("${meilisearch.api-key}") String apiKey,
                         UserServiceClient userServiceClient) {
        this.client = new Client(new Config(host, apiKey));
        this.userServiceClient = userServiceClient;
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
            // Check if listings index exists, create if not
            try {
                client.getIndex("listings");
            } catch (Exception e) {
                client.createIndex("listings", "id");
            }
            // Check if groups index exists, create if not
            try {
                client.getIndex("groups");
            } catch (Exception e) {
                client.createIndex("groups", "id");
            }
            // Check if pages index exists, create if not
            try {
                client.getIndex("pages");
            } catch (Exception e) {
                client.createIndex("pages", "id");
            }
            // Configure geo filterable settings
            try {
                client.index("listings").updateFilterableAttributesSettings(new String[]{"_geo"});
            } catch (Exception e) {
                System.err.println("Failed to configure filterable attributes for listings: " + e.getMessage());
            }
            // Configure searchable attributes to only search by names
            try {
                client.index(indexName).updateSearchableAttributesSettings(new String[]{"username", "firstName", "lastName"});
                client.index("groups").updateSearchableAttributesSettings(new String[]{"name"});
                client.index("pages").updateSearchableAttributesSettings(new String[]{"name"});
                System.out.println("Meilisearch: Successfully configured searchableAttributes settings.");
            } catch (Exception e) {
                System.err.println("Failed to configure searchable attributes settings: " + e.getMessage());
            }
            // Reindex all users on startup to make sure data is fresh
            reindexAll();
            reindexAllEvents();
        } catch (Exception e) {
            System.err.println("Failed to initialize Meilisearch indexes: " + e.getMessage());
        }
    }

    public void reindexAll() {
        try {
            int page = 0;
            int size = 100;
            List<MeiliUser> allMeiliUsers = new ArrayList<>();
            while (true) {
                GetAllUsersResponse response = userServiceClient.getAllUsers(page, size);
                List<UserDto> usersList = response.getUsersList();
                if (usersList.isEmpty()) {
                    break;
                }
                List<MeiliUser> meiliUsers = usersList.stream()
                        .filter(u -> u.getUsername() == null || !u.getUsername().startsWith("page_"))
                        .map(u -> new MeiliUser(
                                u.getId(),
                                u.getUsername(),
                                u.getFirstName(),
                                u.getLastName(),
                                u.getAvatarId()
                        ))
                        .collect(Collectors.toList());
                allMeiliUsers.addAll(meiliUsers);
                page++;
            }

            if (!allMeiliUsers.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonDocuments = mapper.writeValueAsString(allMeiliUsers);
                Index index = client.index(indexName);
                index.addDocuments(jsonDocuments);
                System.out.println("Meilisearch: Indexed " + allMeiliUsers.size() + " users successfully via gRPC.");
            }
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to reindex users via gRPC: " + e.getMessage());
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
        System.out.println("Meilisearch: Event reindexing skipped (event-service is the source of truth).");
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

    public void indexListing(com.facebook.marketplace.event.ListingIndexEvent event) {
        try {
            java.util.Map<String, Object> doc = new java.util.HashMap<>();
            doc.put("id", event.getId());
            doc.put("title", event.getTitle());
            doc.put("description", event.getDescription());
            doc.put("price", event.getPrice());
            doc.put("category", event.getCategory());
            doc.put("condition", event.getCondition());
            if (event.getLatitude() != null && event.getLongitude() != null) {
                doc.put("_geo", java.util.Map.of("lat", event.getLatitude(), "lng", event.getLongitude()));
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(List.of(doc));
            client.index("listings").addDocuments(jsonDocument);
            System.out.println("Meilisearch: Indexed listing " + event.getId() + " successfully from RabbitMQ.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to index listing " + event.getId() + ": " + e.getMessage());
        }
    }

    public void indexGroup(com.facebook.GroupsService.event.GroupIndexEvent event) {
        try {
            if (Boolean.TRUE.equals(event.getDelete())) {
                client.index("groups").deleteDocument(event.getId());
                System.out.println("Meilisearch: Removed group " + event.getId() + " from index (became private/deleted).");
                return;
            }
            java.util.Map<String, Object> doc = new java.util.HashMap<>();
            doc.put("id", event.getId());
            doc.put("name", event.getName());
            doc.put("image", event.getImage() != null ? event.getImage() : "");
            doc.put("newPostsCount", event.getNewPostsCount() != null ? event.getNewPostsCount() : 0);
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(List.of(doc));
            client.index("groups").addDocuments(jsonDocument);
            System.out.println("Meilisearch: Indexed public group " + event.getId() + " successfully from RabbitMQ.");
        } catch (Exception e) {
            System.out.println("Meilisearch: Failed to index public group " + event.getId() + ": " + e.getMessage());
        }
    }

    public List<com.facebook.GroupsService.event.GroupIndexEvent> searchGroups(String query) {
        try {
            Index index = client.index("groups");
            var searchResults = index.search(query);
            
            ObjectMapper mapper = new ObjectMapper();
            List<com.facebook.GroupsService.event.GroupIndexEvent> results = new ArrayList<>();
            for (Object hit : searchResults.getHits()) {
                com.facebook.GroupsService.event.GroupIndexEvent group = mapper.convertValue(hit, com.facebook.GroupsService.event.GroupIndexEvent.class);
                results.add(group);
            }
            return results;
        } catch (Exception e) {
            System.err.println("Meilisearch: Group search failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void indexPage(com.facebook.UserService.dto.PageIndexEvent event) {
        try {
            if (Boolean.TRUE.equals(event.getDelete())) {
                client.index("pages").deleteDocument(event.getId());
                System.out.println("Meilisearch: Removed page " + event.getId() + " from index.");
                return;
            }
            java.util.Map<String, Object> doc = new java.util.HashMap<>();
            doc.put("id", event.getId());
            doc.put("name", event.getName());
            doc.put("category", event.getCategory() != null ? event.getCategory() : "");
            doc.put("avatarUrl", event.getAvatarUrl() != null ? event.getAvatarUrl() : "");
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(List.of(doc));
            client.index("pages").addDocuments(jsonDocument);
            System.out.println("Meilisearch: Indexed page " + event.getId() + " successfully from RabbitMQ.");
        } catch (Exception e) {
            System.err.println("Meilisearch: Failed to index page " + event.getId() + ": " + e.getMessage());
        }
    }

    public List<com.facebook.UserService.dto.PageIndexEvent> searchPages(String query) {
        try {
            Index index = client.index("pages");
            var searchResults = index.search(query);
            
            ObjectMapper mapper = new ObjectMapper();
            List<com.facebook.UserService.dto.PageIndexEvent> results = new ArrayList<>();
            for (Object hit : searchResults.getHits()) {
                com.facebook.UserService.dto.PageIndexEvent page = mapper.convertValue(hit, com.facebook.UserService.dto.PageIndexEvent.class);
                results.add(page);
            }
            return results;
        } catch (Exception e) {
            System.err.println("Meilisearch: Page search failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
