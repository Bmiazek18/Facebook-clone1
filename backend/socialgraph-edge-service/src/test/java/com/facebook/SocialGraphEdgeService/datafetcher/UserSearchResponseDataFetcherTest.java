package com.facebook.SocialGraphEdgeService.datafetcher;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        com.facebook.SocialGraphEdgeService.SocialGraphEdgeServiceApplication.class
}, properties = {
        "grpc.server.port=-1",
        "grpc.client.social-graph-service.address=in-process:test"
})
public class UserSearchResponseDataFetcherTest {

    @Autowired
    private DgsQueryExecutor dgsQueryExecutor;

    @Autowired
    private UserSearchResponseDataFetcher userSearchResponseDataFetcher;

    @Autowired
    private BirthdayDataFetcher birthdayDataFetcher;

    @Autowired
    private FriendRequestDataFetcher friendRequestDataFetcher;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userSearchResponseDataFetcher, "socialGraphGrpcStub", socialGraphGrpcStub);
        ReflectionTestUtils.setField(birthdayDataFetcher, "socialGraphGrpcStub", socialGraphGrpcStub);
        ReflectionTestUtils.setField(friendRequestDataFetcher, "socialGraphGrpcStub", socialGraphGrpcStub);
    }

    @Test
    void testMutualFriendsCount() {
        // Mock the gRPC response
        com.facebook.socialgraph.grpc.GetRelationsResponse mockResponse = 
                com.facebook.socialgraph.grpc.GetRelationsResponse.newBuilder()
                        .addRelations(com.facebook.socialgraph.grpc.UserRelation.newBuilder()
                                .setMutualFriendsCount(5)
                                .build())
                        .build();

        when(socialGraphGrpcStub.getRelations(any())).thenReturn(mockResponse);

        // Run GraphQL Query using the representations entities fetcher format
        Integer count = dgsQueryExecutor.executeAndExtractJsonPath(
                "query { _entities(representations: [{ __typename: \"UserSearchResponse\", id: \"user-1\" }]) { ... on UserSearchResponse { mutualFriendsCount(currentUserId: \"user-2\") } } }",
                "data._entities[0].mutualFriendsCount"
        );

        assertThat(count).isEqualTo(5);
    }

    @Test
    void testGetBirthdayUsers() {
        // Mock gRPC birthday users response
        com.facebook.socialgraph.grpc.GetBirthdayUsersResponse mockResponse = 
                com.facebook.socialgraph.grpc.GetBirthdayUsersResponse.newBuilder()
                        .addUsers(com.facebook.socialgraph.grpc.BirthdayUser.newBuilder()
                                .setUserId("user-100")
                                .setBirthDate("1995-10-15")
                                .build())
                        .build();

        when(socialGraphGrpcStub.getBirthdayUsers(any())).thenReturn(mockResponse);

        // Execute Birthday Query
        List<Map<String, Object>> users = dgsQueryExecutor.executeAndExtractJsonPath(
                "query { getBirthdayUsers(currentUserId: \"user-200\") { userId birthDate } }",
                "data.getBirthdayUsers"
        );

        assertThat(users).hasSize(1);
        assertThat(users.get(0).get("userId")).isEqualTo("user-100");
        assertThat(users.get(0).get("birthDate")).isEqualTo("1995-10-15");
    }

    @Test
    void testSendFriendRequestSuccess() {
        // Mock gRPC request response
        com.facebook.socialgraph.grpc.FriendRequestResponse mockResponse = 
                com.facebook.socialgraph.grpc.FriendRequestResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Request sent successfully")
                        .build();

        when(socialGraphGrpcStub.sendFriendRequest(any())).thenReturn(mockResponse);

        // Execute sendFriendRequest Mutation
        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(
                "mutation { sendFriendRequest(senderId: \"user-1\", receiverId: \"user-2\") { success message } }",
                "data.sendFriendRequest"
        );

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("Request sent successfully");
    }

    @Test
    void testSendFriendRequestFailure() {
        // Force an exception to test error handling fallback in Data Fetcher
        when(socialGraphGrpcStub.sendFriendRequest(any())).thenThrow(new RuntimeException("gRPC error"));

        // Execute sendFriendRequest Mutation and assert it returns fallback error DTO
        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(
                "mutation { sendFriendRequest(senderId: \"user-1\", receiverId: \"user-2\") { success message } }",
                "data.sendFriendRequest"
        );

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("message")).toString().contains("gRPC error");
    }
}
