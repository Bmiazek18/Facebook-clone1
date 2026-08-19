package com.facebook.SearchService.service;

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

import net.devh.boot.grpc.client.inject.GrpcClient;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import com.facebook.user.grpc.GetAllUsersRequest;
import com.facebook.user.grpc.GetAllUsersResponse;
import com.facebook.user.grpc.UserDto;

@Service
public class SearchService {

    private final Client client;
    private final String indexName = "users";
    private final String eventIndexName = "events";

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    public SearchService(@Value("${meilisearch.host}") String host,
                         @Value("${meilisearch.api-key}") String apiKey) {
        this.client = new Client(new Config(host, apiKey));
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
                GetAllUsersResponse response = userGrpcStub.getAllUsers(
                        GetAllUsersRequest.newBuilder()
                                .setPage(page)
                                .setSize(size)
                                .build()
                );
                List<UserDto> usersList = response.getUsersList();
                if (usersList.isEmpty()) {
                    break;
                }
                List<MeiliUser> meiliUsers = usersList.stream()
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
}
