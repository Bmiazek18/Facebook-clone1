package com.facebook.SocialGraphEdgeService.datafetcher;

// Importujemy bezpieczne typy z generatora DGS
import com.facebook.socialgraph.generated.types.BirthdayUser;
import com.facebook.socialgraph.generated.types.UserSearchResponse;

import com.facebook.socialgraph.grpc.GetBirthdayUsersRequest;
import com.facebook.socialgraph.grpc.GetBirthdayUsersResponse;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class BirthdayDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(BirthdayDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @DgsQuery
    public List<BirthdayUser> getBirthdayUsers(@InputArgument String currentUserId) {
        log.info("Edge: Fetching birthday users via gRPC for user: {}", currentUserId);
        try {
            GetBirthdayUsersResponse response = socialGraphGrpcStub.getBirthdayUsers(GetBirthdayUsersRequest.newBuilder()
                    .setUserId(currentUserId)
                    .build());

            return response.getUsersList().stream()
                    .map(u -> {
                        // Tworzymy główny obiekt wygenerowany przez DGS
                        BirthdayUser birthdayUser = new BirthdayUser();
                        birthdayUser.setUserId(u.getUserId());
                        birthdayUser.setBirthDate(u.getBirthDate());

                        // Od razu budujemy i przypisujemy zagnieżdżony obiekt reprezentujący użytkownika
                        UserSearchResponse user = new UserSearchResponse();
                        user.setId(u.getUserId());
                        birthdayUser.setUser(user);

                        return birthdayUser;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch birthday users", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }
}