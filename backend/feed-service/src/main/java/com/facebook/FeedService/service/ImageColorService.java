package com.facebook.FeedService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ImageColorService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private final Map<String, String> colorCache = new ConcurrentHashMap<>();

    public String extractDominantColor(String src) {
        if (src == null || src.isBlank()) {
            return null;
        }

        // Skip video formats
        String lower = src.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".m3u8") || lower.contains("/videos/")) {
            return null;
        }

        // Return from cache if already computed
        if (colorCache.containsKey(src)) {
            return colorCache.get(src);
        }

        try {
            BufferedImage image = loadImage(src);
            if (image == null) {
                return null;
            }

            String color = computeDominantColor(image);
            if (color != null) {
                colorCache.put(src, color);
            }
            return color;
        } catch (Exception e) {
            log.warn("Failed to extract dominant color for {}: {}", src, e.getMessage());
            return null;
        }
    }

    private BufferedImage loadImage(String src) {
        try {
            if (src.startsWith("data:image/")) {
                int commaIdx = src.indexOf(',');
                if (commaIdx != -1) {
                    byte[] data = Base64.getDecoder().decode(src.substring(commaIdx + 1));
                    return ImageIO.read(new ByteArrayInputStream(data));
                }
            }

            if (src.startsWith("http://") || src.startsWith("https://")) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(src))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    try (InputStream is = response.body()) {
                        return ImageIO.read(is);
                    }
                }
            }

            // Check if local file exists
            String filename = extractFilename(src);
            File localFile = new File(uploadDir, filename);
            if (localFile.exists() && localFile.isFile()) {
                return ImageIO.read(localFile);
            }

            File dataFile = new File("/data/feed-uploads", filename);
            if (dataFile.exists() && dataFile.isFile()) {
                return ImageIO.read(dataFile);
            }
        } catch (Exception e) {
            log.debug("Could not load image for color extraction: {}", e.getMessage());
        }
        return null;
    }

    private String extractFilename(String src) {
        String path = src;
        if (path.contains("/media/")) {
            path = path.substring(path.lastIndexOf("/media/") + "/media/".length());
        } else if (path.contains("/files/")) {
            path = path.substring(path.lastIndexOf("/files/") + "/files/".length());
        }
        int qIdx = path.indexOf('?');
        if (qIdx != -1) path = path.substring(0, qIdx);
        int hIdx = path.indexOf('#');
        if (hIdx != -1) path = path.substring(0, hIdx);
        int plusIdx = path.indexOf('+');
        if (plusIdx != -1) path = path.substring(0, plusIdx);
        return path;
    }

    public String computeDominantColor(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        if (width <= 0 || height <= 0) return null;

        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;

        int stepX = Math.max(1, width / 40);
        int stepY = Math.max(1, height / 40);

        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                if (alpha < 50) continue;

                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                sumR += r;
                sumG += g;
                sumB += b;
                count++;
            }
        }

        if (count == 0) return "#ffffff";

        double r = ((double) sumR / count) / 255.0;
        double g = ((double) sumG / count) / 255.0;
        double b = ((double) sumB / count) / 255.0;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));

        // 1. Calculate base lightness and saturation
        double initialL = (max + min) / 2.0;
        double initialS = 0.0;
        if (max != min) {
            double d = max - min;
            initialS = initialL > 0.5 ? d / (2.0 - max - min) : d / (max + min);
        }

        // 2. Calculate exact hue (0 - 360 deg)
        double h = 0.0;
        if (max != min) {
            double d = max - min;
            if (max == r) {
                h = (g - b) / d + (g < b ? 6.0 : 0.0);
            } else if (max == g) {
                h = (b - r) / d + 2.0;
            } else {
                h = (r - g) / d + 4.0;
            }
            h /= 6.0;
        }

        int hueDegrees = (int) Math.round(h * 360.0);
        if (initialL > 0.62 || (min > 0.8 && initialS < 0.15)) {
            return "#ffffff";
        }

        // 3. Vibrancy boost:
        int targetS;
        if (initialS < 0.05) {
            targetS = 10;
        } else {
            targetS = (int) Math.min(85, Math.max(65, Math.round(initialS * 100.0 * 2.5)));
        }

        // 4. Dynamic lightness:
        int targetL;
        if (initialL < 0.3) {
            targetL = 42;
        } else if (initialL > 0.7) {
            targetL = 50;
        } else {
            targetL = 46;
        }

        return String.format("hsl(%d, %d%%, %d%%)", hueDegrees, targetS, targetL);
    }
}
