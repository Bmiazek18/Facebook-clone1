package com.facebook.UserService.config;

import com.facebook.UserService.service.UserActiveService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttPresenceConfiguration {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.topic:user/presence/heartbeat}")
    private String topic;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public IMqttClient mqttPresenceClient(UserActiveService userActiveService) throws MqttException {
        String uniqueClientId = "user-service-presence-" + UUID.randomUUID().toString().substring(0, 8);
        IMqttClient client = new MqttClient(brokerUrl, uniqueClientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password != null ? password.toCharArray() : new char[0]);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                try {
                    client.subscribe(topic, 1);
                    System.out.println("Presence MQTT: Subscribed to topic: " + topic);
                } catch (MqttException e) {
                    System.err.println("Presence MQTT: Subscription failed for topic: " + topic + ", error: " + e.getMessage());
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("Presence MQTT: Connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
            }

            @Override
            public void messageArrived(String t, MqttMessage message) {
                try {
                    String payloadStr = new String(message.getPayload());
                    JsonNode node = objectMapper.readTree(payloadStr);
                    if (node.has("userId")) {
                        String userIdStr = node.get("userId").asText();
                        UUID userId = UUID.fromString(userIdStr);
                        userActiveService.setUserActive(userId);
                        System.out.println("Presence MQTT: Updated active status for user: " + userId);
                    }
                } catch (Exception e) {
                    System.err.println("Presence MQTT: Error processing presence message: " + e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // No-op
            }
        });

        client.connect(options);
        return client;
    }
}
