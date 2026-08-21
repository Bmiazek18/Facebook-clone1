package com.facebook.marketplace.grpc;

import com.facebook.marketplace.dto.CreateListingRequest;
import com.facebook.marketplace.model.Listing;
import com.facebook.marketplace.service.ListingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class MarketplaceGrpcServiceImpl extends MarketplaceGrpcServiceGrpc.MarketplaceGrpcServiceImplBase {

    private final ListingService listingService;

    @Override
    public void createItem(CreateItemRequest request, StreamObserver<CreateItemResponse> responseObserver) {
        try {
            CreateListingRequest listingRequest = new CreateListingRequest();
            listingRequest.setTitle(request.getTitle());
            listingRequest.setDescription(request.getDescription());
            listingRequest.setPrice(BigDecimal.valueOf(request.getPrice()));
            listingRequest.setCategory(request.getCategory());
            listingRequest.setCondition(request.getCondition());
            listingRequest.setLatitude(request.getLatitude());
            listingRequest.setLongitude(request.getLongitude());

            Listing listing = listingService.createListing(listingRequest);
            responseObserver.onNext(CreateItemResponse.newBuilder()
                    .setItem(toProto(listing))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to create listing: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getItem(GetItemRequest request, StreamObserver<GetItemResponse> responseObserver) {
        try {
            Listing listing = listingService.getListingById(Long.parseLong(request.getId()));
            responseObserver.onNext(GetItemResponse.newBuilder()
                    .setItem(toProto(listing))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to fetch listing: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void searchItems(SearchItemsRequest request, StreamObserver<SearchItemsResponse> responseObserver) {
        try {
            double radiusMeters = request.getRadiusKm() > 0 ? request.getRadiusKm() * 1000 : 10000;
            List<Listing> listings;

            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                listings = listingService.searchListingsMeili(
                        request.getQuery(),
                        request.getLatitude(),
                        request.getLongitude(),
                        radiusMeters
                );
            } else {
                listings = listingService.getListingsNearby(
                        request.getLatitude(),
                        request.getLongitude(),
                        radiusMeters
                );
            }

            SearchItemsResponse.Builder builder = SearchItemsResponse.newBuilder();
            listings.forEach(listing -> builder.addItems(toProto(listing)));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to search listings: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteItem(DeleteItemRequest request, StreamObserver<DeleteItemResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Delete listing is not implemented")
                .asRuntimeException());
    }

    private MarketplaceItemDto toProto(Listing listing) {
        MarketplaceItemDto.Builder builder = MarketplaceItemDto.newBuilder()
                .setId(String.valueOf(listing.getId()))
                .setTitle(listing.getTitle())
                .setDescription(listing.getDescription() != null ? listing.getDescription() : "")
                .setPrice(listing.getPrice() != null ? listing.getPrice().doubleValue() : 0.0)
                .setCategory(listing.getCategory())
                .setCondition(listing.getCondition());

        if (listing.getLatitude() != null) {
            builder.setLatitude(listing.getLatitude());
        }
        if (listing.getLongitude() != null) {
            builder.setLongitude(listing.getLongitude());
        }
        if (listing.getCreatedAt() != null) {
            builder.setCreatedAt(listing.getCreatedAt().toString());
        }
        return builder.build();
    }
}
