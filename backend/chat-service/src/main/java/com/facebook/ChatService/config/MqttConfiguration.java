package com.facebook.ChatService.config;

import com.facebook.ChatService.service.ChatMessageHandler;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Configuration
public class MqttConfiguration {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client-id:chat-service-worker}")
    private String clientId;

    @Value("${mqtt.topic:chat/messages/inbound}")
    private String topic;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    private IMqttClient client;
    private MqttConnectOptions options;

    @Bean
    public IMqttClient mqttClient(ChatMessageHandler chatMessageHandler) throws MqttException {
        String uniqueClientId = clientId + "-" + UUID.randomUUID().toString().substring(0, 8);
        client = new MqttClient(brokerUrl, uniqueClientId);

        options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password != null ? password.toCharArray() : new char[0]);
        }

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                try {
                    client.subscribe(topic, 1);
                    System.out.println("MQTT Client: Successfully connected and subscribed to: " + topic);
                } catch (MqttException e) {
                    System.err.println("MQTT Client: Subscription failed for topic: " + topic + ", error: " + e.getMessage());
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT Client: Connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
            }

            @Override
            public void messageArrived(String t, MqttMessage message) {
                chatMessageHandler.processInboundMessage(message.getPayload());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // No-op (worker is consumer-only)
            }
        });

        return client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connectOnReady() {
        CompletableFuture.runAsync(() -> {
            int retries = 0;
            while (retries < 20 && client != null && !client.isConnected()) {
                try {
                    Thread.sleep(2000);
                    client.connect(options);
                    System.out.println("Chat MQTT Client: Successfully connected to broker: " + brokerUrl);
                    break;
                } catch (Exception e) {
                    retries++;
                    System.err.println("Chat MQTT Client: Retrying connection (" + retries + "/20): " + e.getMessage());
                }
            }
        });
    }
}
