package com.facebook.NotificationService.service;

import com.facebook.NotificationService.model.WebPushSubscription;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.security.Security;

@Slf4j
@Service
public class WebPushService {

    @Value("${vapid.public.key}")
    private String publicKey;

    @Value("${vapid.private.key}")
    private String privateKey;

    @Value("${vapid.subject}")
    private String subject;

    private PushService pushService;

    @PostConstruct
    public void init() {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            pushService = new PushService(publicKey, privateKey, subject);
            log.info("Initialized WebPushService with subject: {}", subject);
        } catch (Exception e) {
            log.error("Failed to initialize WebPushService: {}", e.getMessage(), e);
        }
    }

    public void sendPush(WebPushSubscription userSub, String payload) {
        if (pushService == null) {
            log.error("PushService not initialized. Cannot send push.");
            return;
        }

        try {
            // Map our database model to the WebPush library Subscription object
            Subscription subscription = new Subscription(
                userSub.getEndpoint(),
                new Subscription.Keys(userSub.getP256dh(), userSub.getAuth())
            );

            Notification notification = new Notification(subscription, payload);
            var response = pushService.send(notification);
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201) {
                log.info("Successfully sent Web Push notification to endpoint: {}", userSub.getEndpoint());
            } else {
                log.warn("Web Push returned status code: {} for endpoint: {}", statusCode, userSub.getEndpoint());
            }
        } catch (Exception e) {
            log.error("Error sending Web Push notification: {}", e.getMessage());
        }
    }
}
