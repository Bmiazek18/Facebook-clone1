package com.facebook.SocialGraphService.grpc;

import com.facebook.socialgraph.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialGraphGrpcServiceImplTest {

    @Mock
    private Driver neo4jDriver;

    @Mock
    private Session session;

    @Mock
    private TransactionContext transactionContext;

    @Mock
    private Result result;

    @Mock
    private Record record;

    @Mock
    private StreamObserver<CreateNodeResponse> createNodeResponseObserver;

    @Mock
    private StreamObserver<GetRelationsResponse> getRelationsResponseObserver;

    @Mock
    private StreamObserver<GetBirthdayUsersResponse> getBirthdayUsersResponseObserver;

    @Mock
    private StreamObserver<GetFriendSuggestionsResponse> getFriendSuggestionsResponseObserver;

    @Mock
    private com.facebook.SocialGraphService.client.NotificationServiceClient notificationServiceClient;

    private SocialGraphGrpcServiceImpl socialGraphGrpcService;

    @BeforeEach
    void setUp() {
        socialGraphGrpcService = new SocialGraphGrpcServiceImpl(neo4jDriver, notificationServiceClient);
    }

    @Test
    void testCreateUserNode_Success() {
        CreateNodeRequest request = CreateNodeRequest.newBuilder()
                .setUserId("user-123")
                .setBirthDate("1995-05-15")
                .build();

        when(neo4jDriver.session()).thenReturn(session);
        when(session.executeWrite(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(transactionContext);
        });
        when(transactionContext.run(anyString(), any(Value.class))).thenReturn(result);

        socialGraphGrpcService.createUserNode(request, createNodeResponseObserver);

        verify(createNodeResponseObserver).onNext(any(CreateNodeResponse.class));
        verify(createNodeResponseObserver).onCompleted();
        verify(createNodeResponseObserver, never()).onError(any());
    }

    @Test
    void testCreateUserNode_Exception() {
        CreateNodeRequest request = CreateNodeRequest.newBuilder()
                .setUserId("user-123")
                .build();

        when(neo4jDriver.session()).thenReturn(session);
        when(session.executeWrite(any())).thenThrow(new RuntimeException("Neo4j database error"));

        socialGraphGrpcService.createUserNode(request, createNodeResponseObserver);

        verify(createNodeResponseObserver).onError(any(RuntimeException.class));
        verify(createNodeResponseObserver, never()).onNext(any());
    }

    @Test
    void testGetRelations_Success() {
        GetRelationsRequest request = GetRelationsRequest.newBuilder()
                .setUserId("user-123")
                .addTargetUserIds("user-456")
                .build();

        when(neo4jDriver.session()).thenReturn(session);
        when(session.executeRead(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(transactionContext);
        });
        when(transactionContext.run(anyString(), any(Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.get("targetId")).thenReturn(Values.value("user-456"));
        when(record.get("isFriend")).thenReturn(Values.value(true));
        when(record.get("mutualFriendsCount")).thenReturn(Values.value(5));

        socialGraphGrpcService.getRelations(request, getRelationsResponseObserver);

        verify(getRelationsResponseObserver).onNext(any(GetRelationsResponse.class));
        verify(getRelationsResponseObserver).onCompleted();
    }

    @Test
    void testGetBirthdayUsers_Success() {
        GetBirthdayUsersRequest request = GetBirthdayUsersRequest.newBuilder()
                .setUserId("user-123")
                .build();

        when(neo4jDriver.session()).thenReturn(session);
        when(session.executeRead(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(transactionContext);
        });
        when(transactionContext.run(anyString(), any(Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.get("userId")).thenReturn(Values.value("friend-456"));
        when(record.get("birthDate")).thenReturn(Values.value("1990-08-19"));

        socialGraphGrpcService.getBirthdayUsers(request, getBirthdayUsersResponseObserver);

        verify(getBirthdayUsersResponseObserver).onNext(any(GetBirthdayUsersResponse.class));
        verify(getBirthdayUsersResponseObserver).onCompleted();
    }

    @Test
    void testGetFriendSuggestions_Success() {
        GetFriendSuggestionsRequest request = GetFriendSuggestionsRequest.newBuilder()
                .setUserId("user-123")
                .build();

        when(neo4jDriver.session()).thenReturn(session);
        when(session.executeRead(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(transactionContext);
        });
        when(transactionContext.run(anyString(), any(Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.get("userId")).thenReturn(Values.value("suggest-456"));
        when(record.get("mutualFriendsCount")).thenReturn(Values.value(3));

        socialGraphGrpcService.getFriendSuggestions(request, getFriendSuggestionsResponseObserver);

        verify(getFriendSuggestionsResponseObserver).onNext(any(GetFriendSuggestionsResponse.class));
        verify(getFriendSuggestionsResponseObserver).onCompleted();
    }
}
