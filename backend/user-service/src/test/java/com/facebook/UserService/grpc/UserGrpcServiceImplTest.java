package com.facebook.UserService.grpc;

import com.facebook.UserService.service.*;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGrpcServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserActiveService userActiveService;

    @Mock
    private TranslationService translationService;

    @Mock
    private DownstreamGrpcService downstreamGrpcService;

    @Mock
    private TicketService ticketService;

    @Mock
    private PageService pageService;

    @Mock
    private PageTokenService pageTokenService;

    @Mock
    private StreamObserver<GetUserByIdResponse> getUserByIdResponseObserver;

    @Mock
    private StreamObserver<GenerateTicketResponse> generateTicketResponseObserver;

    private UserGrpcServiceImpl userGrpcService;

    @BeforeEach
    void setUp() {
        userGrpcService = new UserGrpcServiceImpl(
                userService,
                userActiveService,
                translationService,
                downstreamGrpcService,
                ticketService,
                pageService,
                pageTokenService
        );
    }

    @Test
    void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setUserId(userId.toString()).build();

        UserDto userDto = UserDto.newBuilder()
                .setId(userId.toString())
                .setFirstName("John")
                .setLastName("Doe")
                .build();

        when(userService.getUserProfileById(userId)).thenReturn(userDto);

        userGrpcService.getUserById(request, getUserByIdResponseObserver);

        verify(getUserByIdResponseObserver).onNext(any(GetUserByIdResponse.class));
        verify(getUserByIdResponseObserver).onCompleted();
    }

    @Test
    void testGenerateTicket_Success() {
        String userId = "user-123";
        GenerateTicketRequest request = GenerateTicketRequest.newBuilder().setUserId(userId).build();

        when(ticketService.generateTicket(userId)).thenReturn("ticket-123");

        userGrpcService.generateTicket(request, generateTicketResponseObserver);

        verify(generateTicketResponseObserver).onNext(any(GenerateTicketResponse.class));
        verify(generateTicketResponseObserver).onCompleted();
    }
}
