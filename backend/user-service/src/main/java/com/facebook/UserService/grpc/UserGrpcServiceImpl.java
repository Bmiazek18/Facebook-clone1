package com.facebook.UserService.grpc;

import com.facebook.UserService.grpc.handler.PageManagementGrpcHandler;
import com.facebook.UserService.grpc.handler.UserProfileAndSearchGrpcHandler;
import com.facebook.UserService.grpc.handler.UserUtilityAndVaultGrpcHandler;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserProfileAndSearchGrpcHandler userProfileAndSearchGrpcHandler;
    private final PageManagementGrpcHandler pageManagementGrpcHandler;
    private final UserUtilityAndVaultGrpcHandler userUtilityAndVaultGrpcHandler;

    // --- USER PROFILE & SEARCH ---
    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.getUserById(request, responseObserver);
    }

    @Override
    public void recordSearch(RecordSearchRequest request, StreamObserver<RecordSearchResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.recordSearch(request, responseObserver);
    }

    @Override
    public void deleteSearchHistoryItem(DeleteSearchHistoryRequest request, StreamObserver<DeleteSearchHistoryResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.deleteSearchHistoryItem(request, responseObserver);
    }

    @Override
    public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.searchUsers(request, responseObserver);
    }

    @Override
    public void getSearchHistory(GetSearchHistoryRequest request, StreamObserver<GetSearchHistoryResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.getSearchHistory(request, responseObserver);
    }

    @Override
    public void getActiveStatuses(GetActiveStatusesRequest request, StreamObserver<GetActiveStatusesResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.getActiveStatuses(request, responseObserver);
    }

    @Override
    public void setUserActive(SetUserActiveRequest request, StreamObserver<SetUserActiveResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.setUserActive(request, responseObserver);
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.updateProfile(request, responseObserver);
    }

    @Override
    public void getAllUsers(GetAllUsersRequest request, StreamObserver<GetAllUsersResponse> responseObserver) {
        userProfileAndSearchGrpcHandler.getAllUsers(request, responseObserver);
    }

    // --- PAGE OPERATIONS ---
    @Override
    public void createPage(CreatePageRequest request, StreamObserver<CreatePageResponse> responseObserver) {
        pageManagementGrpcHandler.createPage(request, responseObserver);
    }

    @Override
    public void getPageById(GetPageByIdRequest request, StreamObserver<GetPageByIdResponse> responseObserver) {
        pageManagementGrpcHandler.getPageById(request, responseObserver);
    }

    @Override
    public void getPageByDomain(GetPageByDomainRequest request, StreamObserver<GetPageByDomainResponse> responseObserver) {
        pageManagementGrpcHandler.getPageByDomain(request, responseObserver);
    }

    @Override
    public void getPagesByOwnerId(GetPagesByOwnerIdRequest request, StreamObserver<GetPagesByOwnerIdResponse> responseObserver) {
        pageManagementGrpcHandler.getPagesByOwnerId(request, responseObserver);
    }

    @Override
    public void exchangePageToken(ExchangePageTokenRequest request, StreamObserver<ExchangePageTokenResponse> responseObserver) {
        pageManagementGrpcHandler.exchangePageToken(request, responseObserver);
    }

    @Override
    public void verifyPageAccess(VerifyPageAccessRequest request, StreamObserver<VerifyPageAccessResponse> responseObserver) {
        pageManagementGrpcHandler.verifyPageAccess(request, responseObserver);
    }

    @Override
    public void clearActivePageSession(ClearActivePageSessionRequest request, StreamObserver<ClearActivePageSessionResponse> responseObserver) {
        pageManagementGrpcHandler.clearActivePageSession(request, responseObserver);
    }

    // --- UTILITY, TRANSLATION, DOWNSTREAM, VAULT ---
    @Override
    public void generateTicket(GenerateTicketRequest request, StreamObserver<GenerateTicketResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.generateTicket(request, responseObserver);
    }

    @Override
    public void resolveMediaUrl(ResolveMediaUrlRequest request, StreamObserver<ResolveMediaUrlResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.resolveMediaUrl(request, responseObserver);
    }

    @Override
    public void detectLanguage(DetectLanguageRequest request, StreamObserver<DetectLanguageResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.detectLanguage(request, responseObserver);
    }

    @Override
    public void translateText(TranslateTextRequest request, StreamObserver<TranslateTextResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.translateText(request, responseObserver);
    }

    @Override
    public void getChatMessages(GetChatMessagesRequest request, StreamObserver<GetChatMessagesResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.getChatMessages(request, responseObserver);
    }

    @Override
    public void getNotificationsHistory(GetNotificationsHistoryRequest request, StreamObserver<GetNotificationsHistoryResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.getNotificationsHistory(request, responseObserver);
    }

    @Override
    public void markNotificationAsRead(MarkNotificationAsReadRequest request, StreamObserver<MarkNotificationAsReadResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.markNotificationAsRead(request, responseObserver);
    }

    @Override
    public void getListing(GetListingRequest request, StreamObserver<GetListingResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.getListing(request, responseObserver);
    }

    @Override
    public void getNearbyListings(GetNearbyListingsRequest request, StreamObserver<GetNearbyListingsResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.getNearbyListings(request, responseObserver);
    }

    @Override
    public void createListing(CreateListingRequest request, StreamObserver<CreateListingResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.createListing(request, responseObserver);
    }

    @Override
    public void getVault(GetVaultRequest request, StreamObserver<GetVaultResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.getVault(request, responseObserver);
    }

    @Override
    public void saveVault(SaveVaultRequest request, StreamObserver<SaveVaultResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.saveVault(request, responseObserver);
    }

    @Override
    public void updateVaultAttempts(UpdateVaultAttemptsRequest request, StreamObserver<UpdateVaultAttemptsResponse> responseObserver) {
        userUtilityAndVaultGrpcHandler.updateVaultAttempts(request, responseObserver);
    }
}
