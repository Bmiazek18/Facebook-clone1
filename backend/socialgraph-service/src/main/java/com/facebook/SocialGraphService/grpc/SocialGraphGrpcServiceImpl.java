package com.facebook.SocialGraphService.grpc;

import com.facebook.socialgraph.grpc.CreateNodeRequest;
import com.facebook.socialgraph.grpc.CreateNodeResponse;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class SocialGraphGrpcServiceImpl extends SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceImplBase {

    private final Driver neo4jDriver;
    private final com.facebook.SocialGraphService.client.NotificationServiceClient notificationServiceClient;

    @Autowired
    public SocialGraphGrpcServiceImpl(Driver neo4jDriver,
                                      com.facebook.SocialGraphService.client.NotificationServiceClient notificationServiceClient) {
        this.neo4jDriver = neo4jDriver;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    public void createUserNode(CreateNodeRequest request, StreamObserver<CreateNodeResponse> responseObserver) {
        String userId = request.getUserId();
        String birthDate = request.getBirthDate();
        String city = request.getCity();
        String highSchool = request.getHighSchool();
        
        // Extract month from birthDate (expected format yyyy-MM-dd)
        String month = "UNKNOWN";
        try {
            if (birthDate != null && birthDate.contains("-")) {
                java.time.LocalDate date = java.time.LocalDate.parse(birthDate.trim());
                month = date.getMonth().name(); // e.g. JANUARY, FEBRUARY...
            }
        } catch (Exception e) {
            System.err.println("Failed to parse birthDate: " + birthDate + ", error: " + e.getMessage());
        }

        System.out.println("gRPC Server: Creating user node in Neo4j for User ID: " 
                + userId + ", Birth Date: " + birthDate + ", Month: " + month
                + ", City: " + city + ", HighSchool: " + highSchool);

        final String finalMonth = month;
        final String finalBirthDate = birthDate != null ? birthDate : "";
        final String finalCity = city != null ? city.trim() : "";
        final String finalHighSchool = highSchool != null ? highSchool.trim() : "";

        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(
                    "MERGE (u:User {userId: $userId}) " +
                    "SET u.birthDate = $birthDate, u.month = $month " +
                    "WITH u " +
                    "FOREACH (_ IN CASE WHEN $city <> '' THEN [1] ELSE [] END | " +
                    "   MERGE (c:City {name: $city}) " +
                    "   MERGE (u)-[:LIVES_IN]->(c) " +
                    ") " +
                    "FOREACH (_ IN CASE WHEN $highSchool <> '' THEN [1] ELSE [] END | " +
                    "   MERGE (s:School {name: $highSchool}) " +
                    "   MERGE (u)-[:ATTENDED_HIGH_SCHOOL]->(s) " +
                    ")",
                    Values.parameters(
                        "userId", userId,
                        "birthDate", finalBirthDate,
                        "month", finalMonth,
                        "city", finalCity,
                        "highSchool", finalHighSchool
                    )
                ).consume();
                return null;
            });

            CreateNodeResponse response = CreateNodeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Node and relationships created in Neo4j successfully for userId: " + userId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Neo4j Error: " + e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Neo4j database error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRelations(com.facebook.socialgraph.grpc.GetRelationsRequest request, 
                             StreamObserver<com.facebook.socialgraph.grpc.GetRelationsResponse> responseObserver) {
        String userId = request.getUserId();
        java.util.List<String> targetUserIds = request.getTargetUserIdsList();

        System.out.println("gRPC Server: Fetching relations for User ID: " + userId + ", Targets: " + targetUserIds);

        com.facebook.socialgraph.grpc.GetRelationsResponse.Builder responseBuilder =
                com.facebook.socialgraph.grpc.GetRelationsResponse.newBuilder();

        if (targetUserIds.isEmpty()) {
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                var result = tx.run(
                    "UNWIND $targetUserIds AS targetId " +
                    "OPTIONAL MATCH (me:User {userId: $currentUserId}) " +
                    "OPTIONAL MATCH (other:User {userId: targetId}) " +
                    "OPTIONAL MATCH (me)-[r:FRIEND]-(other) " +
                    "OPTIONAL MATCH (me)-[:FRIEND]-(mutual:User)-[:FRIEND]-(other) " +
                    "RETURN targetId, " +
                    "       CASE WHEN r IS NOT NULL THEN true ELSE false END AS isFriend, " +
                    "       count(distinct mutual) AS mutualFriendsCount",
                    Values.parameters("currentUserId", userId, "targetUserIds", targetUserIds)
                );

                while (result.hasNext()) {
                    var record = result.next();
                    String targetIdStr = record.get("targetId").asString();
                    boolean isFriend = record.get("isFriend").asBoolean();
                    int mutualFriendsCount = (int) record.get("mutualFriendsCount").asLong();

                    com.facebook.socialgraph.grpc.UserRelation rel =
                            com.facebook.socialgraph.grpc.UserRelation.newBuilder()
                                    .setTargetUserId(targetIdStr)
                                    .setFriend(isFriend)
                                    .setMutualFriendsCount(mutualFriendsCount)
                                    .build();
                    responseBuilder.addRelations(rel);
                }
                return null;
            });

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Neo4j Error in getRelations: " + e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Neo4j database error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getBirthdayUsers(com.facebook.socialgraph.grpc.GetBirthdayUsersRequest request,
                                 StreamObserver<com.facebook.socialgraph.grpc.GetBirthdayUsersResponse> responseObserver) {
        String todayMMDD = "-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
        String currentUserIdStr = request.getUserId();
        com.facebook.socialgraph.grpc.GetBirthdayUsersResponse.Builder responseBuilder =
                com.facebook.socialgraph.grpc.GetBirthdayUsersResponse.newBuilder();

        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                var result = tx.run(
                        "MATCH (me:User {userId: $currentUserId})-[:FRIEND]-(u:User) " +
                        "WHERE u.birthDate ENDS WITH $todayMMDD " +
                        "RETURN DISTINCT u.userId AS userId, u.birthDate AS birthDate",
                        Values.parameters("currentUserId", currentUserIdStr, "todayMMDD", todayMMDD)
                );
                while (result.hasNext()) {
                    var record = result.next();
                    responseBuilder.addUsers(com.facebook.socialgraph.grpc.BirthdayUser.newBuilder()
                            .setUserId(record.get("userId").asString())
                            .setBirthDate(record.get("birthDate").asString())
                            .build());
                }
                return null;
            });
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getFriendSuggestions(com.facebook.socialgraph.grpc.GetFriendSuggestionsRequest request,
                                     StreamObserver<com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse> responseObserver) {
        String currentUserIdStr = request.getUserId();
        com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse.Builder responseBuilder =
                com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse.newBuilder();

        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                var result = tx.run(
                        "MATCH (me:User {userId: $currentUserId}) " +
                        "MATCH (other:User) " +
                        "WHERE other.userId <> $currentUserId " +
                        "  AND NOT (me)-[:FRIEND]-(other) " +
                        "  AND NOT (me)-[:FRIEND_REQUEST]-(other) " +
                        "OPTIONAL MATCH (me)-[:FRIEND]-(mutual:User)-[:FRIEND]-(other) " +
                        "RETURN other.userId AS userId, count(distinct mutual) AS mutualFriendsCount " +
                        "ORDER BY mutualFriendsCount DESC, other.userId ASC",
                        Values.parameters("currentUserId", currentUserIdStr)
                );
                while (result.hasNext()) {
                    var record = result.next();
                    responseBuilder.addSuggestions(com.facebook.socialgraph.grpc.FriendSuggestion.newBuilder()
                            .setUserId(record.get("userId").asString())
                            .setMutualFriendsCount((int) record.get("mutualFriendsCount").asLong())
                            .build());
                }
                return null;
            });
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void sendFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg request,
                                  StreamObserver<com.facebook.socialgraph.grpc.FriendRequestResponse> responseObserver) {
        String senderId = request.getSenderId();
        String receiverId = request.getReceiverId();

        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> tx.run(
                    "MERGE (sender:User {userId: $senderId}) " +
                    "MERGE (receiver:User {userId: $receiverId}) " +
                    "MERGE (sender)-[r:FRIEND_REQUEST]->(receiver) " +
                    "RETURN r",
                    Values.parameters("senderId", senderId, "receiverId", receiverId)
            ).consume());

            sendNotification(receiverId, "Friend Request", senderId);

            responseObserver.onNext(com.facebook.socialgraph.grpc.FriendRequestResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Friend request sent successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void acceptFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg request,
                                    StreamObserver<com.facebook.socialgraph.grpc.FriendRequestResponse> responseObserver) {
        String senderId = request.getSenderId();
        String receiverId = request.getReceiverId();

        try (Session session = neo4jDriver.session()) {
            long deletedCount = session.executeWrite(tx -> {
                var result = tx.run(
                        "MATCH (sender:User {userId: $senderId})-[r:FRIEND_REQUEST]->(receiver:User {userId: $receiverId}) " +
                        "DELETE r " +
                        "MERGE (sender)-[:FRIEND]->(receiver) " +
                        "RETURN count(r) as deletedCount",
                        Values.parameters("senderId", senderId, "receiverId", receiverId)
                );
                if (result.hasNext()) {
                    return result.next().get("deletedCount").asLong();
                }
                return 0L;
            });

            if (deletedCount == 0) {
                responseObserver.onNext(com.facebook.socialgraph.grpc.FriendRequestResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("No pending friend request found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            sendNotification(senderId, "Friend Request Accepted", receiverId);

            responseObserver.onNext(com.facebook.socialgraph.grpc.FriendRequestResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Friend request accepted successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getFriendRequests(com.facebook.socialgraph.grpc.GetFriendSuggestionsRequest request,
                                 StreamObserver<com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse> responseObserver) {
        String currentUserIdStr = request.getUserId();
        com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse.Builder responseBuilder =
                com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse.newBuilder();

        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                var result = tx.run(
                        "MATCH (other:User)-[:FRIEND_REQUEST]->(me:User {userId: $currentUserId}) " +
                        "OPTIONAL MATCH (me)-[:FRIEND]-(mutual:User)-[:FRIEND]-(other) " +
                        "RETURN other.userId AS userId, count(distinct mutual) AS mutualFriendsCount",
                        Values.parameters("currentUserId", currentUserIdStr)
                );
                while (result.hasNext()) {
                    var record = result.next();
                    responseBuilder.addSuggestions(com.facebook.socialgraph.grpc.FriendSuggestion.newBuilder()
                            .setUserId(record.get("userId").asString())
                            .setMutualFriendsCount((int) record.get("mutualFriendsCount").asLong())
                            .build());
                }
                return null;
            });
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getFriends(com.facebook.socialgraph.grpc.GetFriendsRequest request,
                           StreamObserver<com.facebook.socialgraph.grpc.GetFriendsResponse> responseObserver) {
        String userIdStr = request.getUserId();
        String filterType = request.getFilterType();
        int limit = request.getLimit() > 0 ? request.getLimit() : 50;
        int offset = Math.max(0, request.getOffset());

        com.facebook.socialgraph.grpc.GetFriendsResponse.Builder responseBuilder =
                com.facebook.socialgraph.grpc.GetFriendsResponse.newBuilder();

        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                String cypher;
                if ("HIGH_SCHOOL".equalsIgnoreCase(filterType)) {
                    cypher = "MATCH (me:User {userId: $userId})-[:FRIEND]-(f:User)-[:ATTENDED_HIGH_SCHOOL]->(s:School)<-[:ATTENDED_HIGH_SCHOOL]-(me) " +
                             "RETURN DISTINCT f.userId AS friendId " +
                             "SKIP $offset LIMIT $limit";
                } else if ("CURRENT_CITY".equalsIgnoreCase(filterType)) {
                    cypher = "MATCH (me:User {userId: $userId})-[:FRIEND]-(f:User)-[:LIVES_IN]->(c:City)<-[:LIVES_IN]-(me) " +
                             "RETURN DISTINCT f.userId AS friendId " +
                             "SKIP $offset LIMIT $limit";
                } else if ("BIRTHDAYS".equalsIgnoreCase(filterType)) {
                    cypher = "MATCH (me:User {userId: $userId})-[:FRIEND]-(f:User) " +
                             "WHERE f.birthDate IS NOT NULL AND f.birthDate <> '' " +
                             "RETURN DISTINCT f.userId AS friendId " +
                             "SKIP $offset LIMIT $limit";
                } else {
                    cypher = "MATCH (u:User {userId: $userId})-[:FRIEND]-(f:User) " +
                             "RETURN DISTINCT f.userId AS friendId " +
                             "SKIP $offset LIMIT $limit";
                }

                var result = tx.run(
                        cypher,
                        Values.parameters("userId", userIdStr, "offset", offset, "limit", limit)
                );
                while (result.hasNext()) {
                    var record = result.next();
                    responseBuilder.addFriendIds(record.get("friendId").asString());
                }
                return null;
            });
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Neo4j Error in getFriends: " + e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Neo4j database error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private void sendNotification(String targetUserId, String title, String message) {
        notificationServiceClient.sendNotification(targetUserId, title, message);
    }
}
