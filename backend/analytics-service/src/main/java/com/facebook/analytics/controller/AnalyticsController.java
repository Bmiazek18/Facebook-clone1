package com.facebook.analytics.controller;

import com.facebook.analytics.dto.BotDto;
import com.facebook.analytics.dto.ImpressionEvent;
import com.facebook.analytics.dto.PageInsightsResponse;
import com.facebook.analytics.dto.TelemetryEvent;
import com.facebook.analytics.dto.UserFeatureVectorDto;
import com.facebook.analytics.service.AnalyticsService;
import com.facebook.analytics.service.BotDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;
    private final BotDetectionService botDetectionService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, BotDetectionService botDetectionService) {
        this.analyticsService = analyticsService;
        this.botDetectionService = botDetectionService;
    }

    private void checkTarpit(String userId) {
        if (userId != null && botDetectionService.isBot(userId)) {
            try {
                log.info("Tarpitting detected bot user {} - introducing 5 seconds delay...", userId);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Ingest single post / page impression
     */
    @PostMapping("/impressions")
    public ResponseEntity<Map<String, Object>> recordImpression(@RequestBody ImpressionEvent event) {
        if (event != null) {
            checkTarpit(event.getViewerId());
        }
        analyticsService.recordImpression(event);
        return ResponseEntity.ok(Map.of("status", "recorded"));
    }

    /**
     * Ingest batch of impressions from frontend buffer
     */
    @PostMapping("/impressions/batch")
    public ResponseEntity<Map<String, Object>> recordImpressionBatch(@RequestBody List<ImpressionEvent> events) {
        if (events != null && !events.isEmpty()) {
            checkTarpit(events.get(0).getViewerId());
            for (ImpressionEvent e : events) {
                analyticsService.recordImpression(e);
            }
        }
        return ResponseEntity.ok(Map.of("status", "batch_recorded", "count", events != null ? events.size() : 0));
    }

    /**
     * Ingest general telemetry event (video progress, loop, mute, expand text, lightbox, link click, copy link, save)
     */
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> recordTelemetryEvent(@RequestBody TelemetryEvent event) {
        if (event != null) {
            checkTarpit(event.getUserId());
        }
        analyticsService.processTelemetryEvent(event);
        return ResponseEntity.ok(Map.of("status", "recorded"));
    }

    /**
     * Ingest batch of general telemetry events
     */
    @PostMapping("/events/batch")
    public ResponseEntity<Map<String, Object>> recordTelemetryEventBatch(@RequestBody List<TelemetryEvent> events) {
        if (events != null && !events.isEmpty()) {
            checkTarpit(events.get(0).getUserId());
            for (TelemetryEvent e : events) {
                analyticsService.processTelemetryEvent(e);
            }
        }
        return ResponseEntity.ok(Map.of("status", "batch_recorded", "count", events != null ? events.size() : 0));
    }

    /**
     * Fetch aggregated metrics for Professional Dashboard
     */
    @GetMapping("/pages/{pageId}/insights")
    public ResponseEntity<PageInsightsResponse> getPageInsights(
            @PathVariable("pageId") UUID pageId,
            @RequestParam(value = "period", defaultValue = "28d") String period) {
        PageInsightsResponse response = analyticsService.getPageInsights(pageId, period);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch feature vector for AI recommendation engine
     */
    @GetMapping("/features/user/{userId}")
    public ResponseEntity<UserFeatureVectorDto> getUserFeatures(@PathVariable("userId") String userId) {
        checkTarpit(userId);
        UserFeatureVectorDto features = analyticsService.getUserFeaturesForRecommendation(userId);
        return ResponseEntity.ok(features);
    }

    /**
     * Get detected bots from ClickHouse
     */
    @GetMapping("/bots")
    public ResponseEntity<List<BotDto>> getDetectedBots() {
        List<BotDto> bots = botDetectionService.getDetectedBots();
        return ResponseEntity.ok(bots);
    }

    /**
     * Manually trigger bot detection scan and return detected bots
     */
    @PostMapping("/bots/check")
    public ResponseEntity<List<BotDto>> checkBots() {
        botDetectionService.detectAndStoreBots();
        List<BotDto> bots = botDetectionService.getDetectedBots();
        return ResponseEntity.ok(bots);
    }
}
