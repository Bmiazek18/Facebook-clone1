package com.facebook.NotificationService.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
public class MqttNotificationPublisher {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    private IMqttClient mqttClient;

    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            
            if (username != null && !username.trim().isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }

            log.info("Connecting to MQTT broker: {}", brokerUrl);
            mqttClient.connect(options);
            log.info("Successfully connected to MQTT broker.");
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker: {}", e.getMessage(), e);
        }
    }

    public void publish(String userId, String payload) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("MQTT client not connected. Attempting reconnect...");
            try {
                if (mqttClient != null) {
                    mqttClient.reconnect();
                } else {
                    init();
                }
            } catch (MqttException e) {
                log.error("Reconnect to MQTT broker failed: {}", e.getMessage());
                return;
            }
        }

        String topic = "user/" + userId + "/notifications";
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // At least once delivery
            mqttClient.publish(topic, message);
            log.info("Published notification to MQTT topic: {}", topic);
        } catch (MqttException e) {
            log.error("Failed to publish message to topic {}: {}", topic, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("Disconnected from MQTT broker.");
            } catch (MqttException e) {
                log.error("Error during MQTT disconnect: {}", e.getMessage());
            }
        }
    }
}
