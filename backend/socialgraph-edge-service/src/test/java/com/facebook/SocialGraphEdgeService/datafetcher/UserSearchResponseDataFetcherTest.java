package com.facebook.SocialGraphEdgeService.datafetcher;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        com.facebook.SocialGraphEdgeService.SocialGraphEdgeServiceApplication.class
}, properties = {
        "grpc.server.port=-1"
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

    private static Server inProcessServer;
    private static ManagedChannel inProcessChannel;
    private static final TestSocialGraphService fakeService = new TestSocialGraphService();

    static class TestSocialGraphService extends com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceImplBase {
        boolean failFriendRequest = false;

        @Override
        public void getRelations(com.facebook.socialgraph.grpc.GetRelationsRequest request,
                                 StreamObserver<com.facebook.socialgraph.grpc.GetRelationsResponse> responseObserver) {
            responseObserver.onNext(com.facebook.socialgraph.grpc.GetRelationsResponse.newBuilder()
                    .addRelations(com.facebook.socialgraph.grpc.UserRelation.newBuilder()
                            .setMutualFriendsCount(5)
                            .build())
                    .build());
            responseObserver.onCompleted();
        }

        @Override
        public void getBirthdayUsers(com.facebook.socialgraph.grpc.GetBirthdayUsersRequest request,
                                     StreamObserver<com.facebook.socialgraph.grpc.GetBirthdayUsersResponse> responseObserver) {
            responseObserver.onNext(com.facebook.socialgraph.grpc.GetBirthdayUsersResponse.newBuilder()
                    .addUsers(com.facebook.socialgraph.grpc.BirthdayUser.newBuilder()
                            .setUserId("user-100")
                            .setBirthDate("1995-10-15")
                            .build())
                    .build());
            responseObserver.onCompleted();
        }

        @Override
        public void sendFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg request,
                                      StreamObserver<com.facebook.socialgraph.grpc.FriendRequestResponse> responseObserver) {
            if (failFriendRequest) {
                responseObserver.onError(new RuntimeException("gRPC error"));
            } else {
                responseObserver.onNext(com.facebook.socialgraph.grpc.FriendRequestResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Request sent successfully")
                        .build());
                responseObserver.onCompleted();
            }
        }
    }

    @BeforeAll
    static void startServer() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        inProcessServer = InProcessServerBuilder.forName(serverName).directExecutor().addService(fakeService).build().start();
        inProcessChannel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterAll
    static void stopServer() {
        if (inProcessChannel != null) inProcessChannel.shutdownNow();
        if (inProcessServer != null) inProcessServer.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        var stub = com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.newBlockingStub(inProcessChannel);
        ReflectionTestUtils.setField(userSearchResponseDataFetcher, "socialGraphGrpcStub", stub);
        ReflectionTestUtils.setField(birthdayDataFetcher, "socialGraphGrpcStub", stub);
        ReflectionTestUtils.setField(friendRequestDataFetcher, "socialGraphGrpcStub", stub);
        fakeService.failFriendRequest = false;
    }

    @Test
    void testMutualFriendsCount() {
        Integer count = dgsQueryExecutor.executeAndExtractJsonPath(
                "query { _entities(representations: [{ __typename: \"UserSearchResponse\", id: \"user-1\" }]) { ... on UserSearchResponse { mutualFriendsCount(currentUserId: \"user-2\") } } }",
                "data._entities[0].mutualFriendsCount"
        );
        assertThat(count).isEqualTo(5);
    }

    @Test
    void testGetBirthdayUsers() {
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
        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(
                "mutation { sendFriendRequest(senderId: \"user-1\", receiverId: \"user-2\") { success message } }",
                "data.sendFriendRequest"
        );
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("Request sent successfully");
    }

    @Test
    void testSendFriendRequestFailure() {
        fakeService.failFriendRequest = true;
        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(
                "mutation { sendFriendRequest(senderId: \"user-1\", receiverId: \"user-2\") { success message } }",
                "data.sendFriendRequest"
        );
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("message")).toString().contains("gRPC error");
    }
}
