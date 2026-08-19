package com.facebook.NotificationService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/db")
    public ResponseEntity<?> checkDatabase() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            try (Connection connection = dataSource.getConnection()) {
                connection.createStatement().execute("SELECT 1");
                health.put("status", "UP");
                health.put("database", "Connected");
                log.info("Database health check: OK");
                return ResponseEntity.ok(health);
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("database", "Connection failed: " + e.getMessage());
            log.error("Database health check failed", e);
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/ready")
    public ResponseEntity<?> readinessProbe() {
        Map<String, Object> status = new HashMap<>();
        status.put("ready", true);
        status.put("service", "NotificationService");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/live")
    public ResponseEntity<?> livenessProbe() {
        Map<String, Object> status = new HashMap<>();
        status.put("alive", true);
        return ResponseEntity.ok(status);
    }
}
