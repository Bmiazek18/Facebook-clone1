package com.facebook.SearchService.service;

import com.facebook.SearchService.model.User;
import com.facebook.SearchService.model.MeiliUser;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SearchServiceTest {

    private SearchService searchService;
    private Client mockClient;
    private Index mockIndex;
    private com.facebook.SearchService.client.UserServiceClient mockUserServiceClient;

    @BeforeEach
    void setUp() {
        mockClient = mock(Client.class);
        mockIndex = mock(Index.class);
        mockUserServiceClient = mock(com.facebook.SearchService.client.UserServiceClient.class);
        
        searchService = new SearchService("http://localhost:7700", "masterKey", mockUserServiceClient);
        ReflectionTestUtils.setField(searchService, "client", mockClient);
    }

    @Test
    void testIndexUser() throws Exception {
        when(mockClient.index("users")).thenReturn(mockIndex);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAvatarId("avatar123");

        searchService.indexUser(user);

        verify(mockClient).index("users");
        verify(mockIndex).addDocuments(anyString());
    }

    @Test
    void testSearchUsersReturnsEmptyOnException() throws Exception {
        when(mockClient.index("users")).thenThrow(new RuntimeException("Meilisearch down"));

        List<MeiliUser> results = searchService.searchUsers("john");

        assertTrue(results.isEmpty());
    }

    @Test
    void testReindexAll() throws Exception {
        when(mockClient.index("users")).thenReturn(mockIndex);

        com.facebook.user.grpc.UserDto userDto = com.facebook.user.grpc.UserDto.newBuilder()
                .setId("user-123")
                .setUsername("user_name")
                .setFirstName("First")
                .setLastName("Last")
                .setAvatarId("avatar-id")
                .build();

        com.facebook.user.grpc.GetAllUsersResponse responsePage0 = com.facebook.user.grpc.GetAllUsersResponse.newBuilder()
                .addUsers(userDto)
                .build();

        com.facebook.user.grpc.GetAllUsersResponse responsePage1 = com.facebook.user.grpc.GetAllUsersResponse.newBuilder()
                .build();

        when(mockUserServiceClient.getAllUsers(anyInt(), anyInt()))
                .thenReturn(responsePage0)
                .thenReturn(responsePage1);

        searchService.reindexAll();

        verify(mockUserServiceClient, times(2)).getAllUsers(anyInt(), anyInt());
        verify(mockClient).index("users");
        verify(mockIndex).addDocuments(anyString());
    }
}
