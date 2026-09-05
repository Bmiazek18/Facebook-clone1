package com.facebook.UserService.controller;

import com.facebook.UserService.dto.RegisterRequest;
import com.facebook.UserService.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register-user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
