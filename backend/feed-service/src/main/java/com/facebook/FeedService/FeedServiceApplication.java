package com.facebook.FeedService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class FeedServiceApplication {
    public static void main(String[] args) {
        try {
            String vaultUri = System.getenv("VAULT_URI");
            String vaultToken = System.getenv("VAULT_TOKEN");
            if (vaultUri != null && vaultToken != null) {
                System.out.println("Vault: Connecting to " + vaultUri + " using token...");
                HttpClient client = HttpClient.newHttpClient();
                // Fetch secret/feed-service
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(vaultUri + "/v1/secret/data/feed-service"))
                        .header("X-Vault-Token", vaultToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(response.body());
                    JsonNode data = node.path("data").path("data");
                    if (data.isObject()) {
                        data.fields().forEachRemaining(entry -> {
                            String key = entry.getKey();
                            String value = entry.getValue().asText();
                            System.setProperty(key, value);
                            System.out.println("Vault: Loaded property " + key);
                        });
                    }
                } else {
                    System.err.println("Vault: Failed to load secrets. HTTP status " + response.statusCode());
                }
            } else {
                System.out.println("Vault env variables not set. Skipping Vault config.");
            }
        } catch (Exception e) {
            System.err.println("Vault: Error fetching secrets: " + e.getMessage());
        }

        SpringApplication.run(FeedServiceApplication.class, args);
    }
}
