package com.facebook.SearchService.listener;

import com.facebook.SearchService.model.User;
import com.facebook.SearchService.service.SearchService;
import com.facebook.UserService.dto.UserIndexEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class UserEventListenerTest {

    @Test
    void testHandleUserIndexEvent() {
        SearchService mockSearchService = Mockito.mock(SearchService.class);
        UserEventListener listener = new UserEventListener(mockSearchService);

        UUID userId = UUID.randomUUID();
        UserIndexEvent event = new UserIndexEvent(
                userId.toString(),
                "test_user",
                "Test",
                "User",
                "avatar_test"
        );

        listener.handleUserIndexEvent(event);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockSearchService).indexUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(userId, capturedUser.getId());
        assertEquals("test_user", capturedUser.getUsername());
        assertEquals("Test", capturedUser.getFirstName());
        assertEquals("User", capturedUser.getLastName());
        assertEquals("avatar_test", capturedUser.getAvatarId());
    }
}
