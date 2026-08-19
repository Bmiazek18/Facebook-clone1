package com.facebook.SocialGraphEdgeService.datafetcher;

import com.facebook.socialgraph.generated.DgsConstants;
import com.facebook.socialgraph.generated.types.UserSearchResponse;

import com.facebook.socialgraph.grpc.GetRelationsRequest;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@DgsComponent
public class UserSearchResponseDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(UserSearchResponseDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    // 1. Zamiast "UserSearchResponse" używamy bezpiecznej stałej
    @DgsEntityFetcher(name = DgsConstants.USERSEARCHRESPONSE.TYPE_NAME)
    public UserSearchResponse resolveUserSearchResponse(Map<String, Object> representation) {
        // W federacji (EntityFetcher) argument 'representation' zawsze przychodzi jako Mapa z Apollo Routera/Gatewaya
        String id = (String) representation.get("id");

        // Ale ZWRACAMY już silnie typowany obiekt wygenerowany przez Codegen
        UserSearchResponse response = new UserSearchResponse();
        response.setId(id);

        return response;
    }

    // 2. Stałe chronią przed literówką w nazwie pola (np. MutualFriendsCount)
    @DgsData(
            parentType = DgsConstants.USERSEARCHRESPONSE.TYPE_NAME,
            field = DgsConstants.USERSEARCHRESPONSE.MutualFriendsCount
    )
    public int mutualFriendsCount(DgsDataFetchingEnvironment dfe, @InputArgument String currentUserId) {

        // 3. Pobieramy konkretny typ z kontekstu, bo to on został zwrócony przez resolveUserSearchResponse
        UserSearchResponse source = dfe.getSource();

        // 4. Pobieramy ID bezpiecznym getterem – koniec z (String) source.get("id")!
        String idStr = source.getId();

        if (idStr == null || currentUserId == null || currentUserId.isEmpty()) {
            return 0;
        }

        try {
            var response = socialGraphGrpcStub.getRelations(GetRelationsRequest.newBuilder()
                    .setUserId(currentUserId)
                    .addTargetUserIds(idStr)
                    .build());

            if (response.getRelationsCount() > 0) {
                return response.getRelations(0).getMutualFriendsCount();
            }
        } catch (Exception e) {
            log.error("Failed to fetch relations for mutualFriendsCount, target: {}, current: {}", idStr, currentUserId, e);
        }

        return 0;
    }
}