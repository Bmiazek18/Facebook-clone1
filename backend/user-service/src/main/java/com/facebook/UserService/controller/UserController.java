package com.facebook.UserService.controller;

import com.facebook.UserService.dto.RegisterRequest;
import com.facebook.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register-user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("REST: Received registration webhook from Keycloak for username: {}", request.getUsername());
        try {
            var response = userService.registerUser(request);
            log.info("REST: User {} successfully registered and synchronized", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("REST: Keycloak registration validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, String>() {{
                put("success", "false");
                put("message", e.getMessage());
            }});
        } catch (Exception e) {
            log.error("REST: Unexpected error during Keycloak registration", e);
            return ResponseEntity.internalServerError().body(new java.util.HashMap<String, String>() {{
                put("success", "false");
                put("message", "Internal server error: " + e.getMessage());
            }});
        }
    }
}
