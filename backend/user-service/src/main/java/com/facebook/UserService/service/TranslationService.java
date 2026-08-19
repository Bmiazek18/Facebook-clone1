package com.facebook.UserService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class TranslationService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=AUTO|pl";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode matches = root.path("matches");
                if (matches.isArray() && matches.size() > 0) {
                    String sourceLang = matches.get(0).path("source-language").asText("en");
                    if (sourceLang.contains("-")) {
                        sourceLang = sourceLang.split("-")[0];
                    }
                    return sourceLang.toLowerCase();
                }
            }
        } catch (Exception e) {
            log.error("Failed to detect language via MyMemory API, falling back to basic checks", e);
        }
        
        return detectBasicLanguage(text);
    }

    public String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String target = (targetLanguage == null || targetLanguage.trim().isEmpty()) ? "pl" : targetLanguage;
            String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=AUTO|" + target;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String translatedText = root.path("responseData").path("translatedText").asText("");
                if (!translatedText.isEmpty()) {
                    return translatedText;
                }
            }
        } catch (Exception e) {
            log.error("Failed to translate text via MyMemory API", e);
        }
        return text;
    }

    private String detectBasicLanguage(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("ść") || lower.contains("ą") || lower.contains("ę") || lower.contains("ł") || lower.contains("ż") || lower.contains("ź") || lower.contains("ó")) {
            return "pl";
        }
        if (lower.contains(" the ") || lower.contains(" is ") || lower.contains(" and ") || lower.contains(" hello ")) {
            return "en";
        }
        return "en";
    }
}
