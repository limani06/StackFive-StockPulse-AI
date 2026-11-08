package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIParsingService {

    // ---- Gemini config (loaded from application.properties) ----
    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:}")
    private String apiUrl;

    @Value("${gemini.api.enabled:false}")
    private boolean aiEnabled;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // ---- Rule-based fallback (unchanged from before) ----
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final Pattern FILLER_WORDS = Pattern.compile(
            "(?i)\\b(received|add|bags?|units?|of|the|a|an|stock|in)\\b");

    public ParsedStockResult parseStockInput(String rawInput) {
        return parse(rawInput);
    }

    public ParsedStockResult parseOrderInput(String rawInput) {
        return parse(rawInput);
    }

    // ---- Core logic: try AI, fall back to regex ----
    private ParsedStockResult parse(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return new ParsedStockResult(null, null);
        }

        if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
            ParsedStockResult aiResult = tryAiParse(rawInput);
            if (aiResult != null) {
                System.out.println("[AIParsingService] AI parsed: '" + rawInput + "' -> " + aiResult.getProductName() + " x" + aiResult.getQuantity());
                return aiResult;
            }
            System.out.println("[AIParsingService] AI failed or returned empty, falling back to rule-based parser.");
        }

        return ruleBasedParse(rawInput);
    }

    // ---- Gemini call ----
    private ParsedStockResult tryAiParse(String rawInput) {
        try {
            String prompt = "You extract stock/order data from short messages. " +
                    "Return ONLY valid JSON, no markdown fences, no explanation. " +
                    "Format: {\"productName\": \"<name>\", \"quantity\": <number or null>}. " +
                    "If no quantity is present, use null. " +
                    "Examples:\n" +
                    "Input: 'Received 10 Rice 10kg' -> {\"productName\": \"Rice 10kg\", \"quantity\": 10}\n" +
                    "Input: 'I got some cooking oil in today' -> {\"productName\": \"Cooking Oil\", \"quantity\": null}\n" +
                    "Input: '5 flour 2kg' -> {\"productName\": \"Flour 2kg\", \"quantity\": 5}\n" +
                    "Input: " + rawInput;

            String body = mapper.writeValueAsString(Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[AIParsingService] Gemini HTTP status: " + response.statusCode());

            if (response.statusCode() != 200) {
                System.out.println("[AIParsingService] Gemini error body: " + response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());
            String text = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("");

            System.out.println("[AIParsingService] Gemini raw text: >>>" + text + "<<<");

            if (text.isBlank()) {
                return null;
            }

            // Strip markdown fences if the model added them anyway
            text = text.trim();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```(?:json)?\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            JsonNode parsed = mapper.readTree(text);
            String productName = parsed.path("productName").isNull() ? null : parsed.path("productName").asText(null);
            Integer quantity = parsed.path("quantity").isNull() ? null : parsed.path("quantity").asInt();

            if (productName == null || productName.isBlank()) {
                return null;
            }

            return new ParsedStockResult(productName, quantity);

        } catch (Exception e) {
            System.out.println("[AIParsingService] Gemini call failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ---- Existing rule-based fallback (kept verbatim) ----
    private ParsedStockResult ruleBasedParse(String rawInput) {
        Matcher m = NUMBER_PATTERN.matcher(rawInput);
        Integer quantity = null;
        String withoutQuantity = rawInput;

        if (m.find()) {
            quantity = Integer.parseInt(m.group(1));
            withoutQuantity = rawInput.substring(0, m.start()) + rawInput.substring(m.end());
        }

        String productName = FILLER_WORDS.matcher(withoutQuantity).replaceAll("");
        productName = productName.trim().replaceAll("\\s+", " ");

        return new ParsedStockResult(productName.isEmpty() ? null : productName, quantity);
    }

    // ---- Result DTO (unchanged) ----
    public static class ParsedStockResult {
        private final String productName;
        private final Integer quantity;

        public ParsedStockResult(String productName, Integer quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }

        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
    }
}