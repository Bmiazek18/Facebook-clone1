package com.facebook.marketplace.edge.datafetcher;

import com.facebook.marketplace.generated.types.CreateMarketplaceItemInput;
import com.facebook.marketplace.generated.types.MarketplaceItem;
import com.facebook.marketplace.grpc.*;
import com.netflix.graphql.dgs.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class MarketplaceDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceDataFetcher.class);

    @GrpcClient("marketplace-service")
    private MarketplaceGrpcServiceGrpc.MarketplaceGrpcServiceBlockingStub marketplaceGrpcStub;

    @DgsQuery
    public List<MarketplaceItem> marketplaceItems(
            @InputArgument Double latitude,
            @InputArgument Double longitude,
            @InputArgument Double radiusKm,
            @InputArgument String query) {
        log.info("GraphQL Edge: Searching marketplace items at lat: {}, lon: {}, radius: {}, query: {}", 
                latitude, longitude, radiusKm, query);
        
        try {
            SearchItemsRequest request = SearchItemsRequest.newBuilder()
                    .setLatitude(latitude != null ? latitude : 0.0)
                    .setLongitude(longitude != null ? longitude : 0.0)
                    .setRadiusKm(radiusKm != null ? radiusKm : 10.0)
                    .setQuery(query != null ? query : "")
                    .build();

            SearchItemsResponse response = marketplaceGrpcStub.searchItems(request);
            return response.getItemsList().stream()
                    .map(this::mapToGraphQl)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to query marketplace items via gRPC: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @DgsQuery
    public MarketplaceItem marketplaceItem(@InputArgument String id) {
        log.info("GraphQL Edge: Fetching marketplace item: {}", id);
        try {
            GetItemRequest request = GetItemRequest.newBuilder()
                    .setId(id)
                    .build();

            GetItemResponse response = marketplaceGrpcStub.getItem(request);
            return mapToGraphQl(response.getItem());
        } catch (Exception e) {
            log.error("Failed to fetch marketplace item {} via gRPC: {}", id, e.getMessage(), e);
            return null;
        }
    }

    @DgsMutation
    public MarketplaceItem createMarketplaceItem(@InputArgument CreateMarketplaceItemInput input) {
        log.info("GraphQL Edge: Creating marketplace item: {}", input.getTitle());
        try {
            CreateItemRequest request = CreateItemRequest.newBuilder()
                    .setTitle(input.getTitle())
                    .setDescription(input.getDescription() != null ? input.getDescription() : "")
                    .setPrice(input.getPrice())
                    .setLocationName(input.getLocationName() != null ? input.getLocationName() : "")
                    .setLatitude(input.getLatitude() != null ? input.getLatitude() : 0.0)
                    .setLongitude(input.getLongitude() != null ? input.getLongitude() : 0.0)
                    .setImageUrl(input.getImageUrl() != null ? input.getImageUrl() : "")
                    .setOwnerId(input.getOwnerId())
                    .setCategory(input.getCategory() != null ? input.getCategory() : "")
                    .setCondition(input.getCondition() != null ? input.getCondition() : "")
                    .build();

            CreateItemResponse response = marketplaceGrpcStub.createItem(request);
            return mapToGraphQl(response.getItem());
        } catch (Exception e) {
            log.error("Failed to create marketplace item via gRPC: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create listing: " + e.getMessage());
        }
    }

    private MarketplaceItem mapToGraphQl(MarketplaceItemDto dto) {
        if (dto == null) return null;
        MarketplaceItem item = new MarketplaceItem();
        item.setId(dto.getId());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setLocationName(dto.getLocationName());
        item.setLatitude(dto.getLatitude());
        item.setLongitude(dto.getLongitude());
        item.setImageUrl(dto.getImageUrl());
        item.setOwnerId(dto.getOwnerId());
        item.setCreatedAt(dto.getCreatedAt());
        item.setDistance(dto.getDistance());
        item.setCategory(dto.getCategory());
        item.setCondition(dto.getCondition());
        return item;
    }
}
