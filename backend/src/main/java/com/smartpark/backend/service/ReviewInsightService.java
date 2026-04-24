package com.smartpark.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.backend.dto.ReviewInsightDTO;
import com.smartpark.backend.model.Review;
import com.smartpark.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewInsightService {

    @Value("${ollama.api-url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:qwen2.5:1.5b}")
    private String ollamaModel;

    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewInsightService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public ReviewInsightDTO generateInsight(String produitId) {
        if (produitId == null || produitId.isBlank()) {
            throw new IllegalArgumentException("Produit invalide");
        }

        List<Review> reviews = reviewRepository.findByProduitIdOrderByDateCreationDesc(produitId);
        if (reviews.isEmpty()) {
            return new ReviewInsightDTO(
                    produitId,
                    "Aucun avis disponible pour générer un résumé.",
                    0,
                    0,
                    LocalDateTime.now().toString()
            );
        }

        String resume = callOllamaForSummary(reviews);
        int confiance = computeConfidenceScore(reviews);

        return new ReviewInsightDTO(
                produitId,
                resume,
                confiance,
                reviews.size(),
                LocalDateTime.now().toString()
        );
    }

    private String callOllamaForSummary(List<Review> reviews) {
        try {
            String prompt = buildPrompt(reviews);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("format", "json");

            Map<String, Object> options = new LinkedHashMap<>();
            options.put("temperature", 0.2);
            options.put("num_predict", 250);
            requestBody.put("options", options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl + "/api/generate",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String raw = root.path("response").asText("").trim();
            if (raw.isEmpty()) {
                return fallbackSummary(reviews);
            }

            JsonNode parsed = objectMapper.readTree(raw);
            String summary = parsed.path("resume").asText("").trim();
            if (summary.isEmpty()) {
                return fallbackSummary(reviews);
            }
            return summary;
        } catch (Exception e) {
            return fallbackSummary(reviews);
        }
    }

    private String buildPrompt(List<Review> reviews) {
        StringBuilder content = new StringBuilder();
        int limit = Math.min(reviews.size(), 25);
        for (int i = 0; i < limit; i++) {
            Review r = reviews.get(i);
            String commentaire = r.getCommentaire() == null ? "" : r.getCommentaire().replaceAll("\\s+", " ").trim();
            if (commentaire.length() > 220) {
                commentaire = commentaire.substring(0, 220) + "...";
            }
            content.append("- note=").append(r.getNote()).append("/5, commentaire=\"").append(commentaire).append("\"\n");
        }

        return """
                Tu es un assistant d'analyse d'avis produit pour administrateur.
                Rédige un résumé neutre, clair et court en français (max 3 phrases), basé UNIQUEMENT sur les avis fournis.
                Retourne STRICTEMENT un JSON valide avec cette forme:
                {"resume":"..."}

                AVIS:
                """ + content;
    }

    private int computeConfidenceScore(List<Review> reviews) {
        int count = reviews.size();
        double baseByCount;
        if (count >= 30) baseByCount = 92;
        else if (count >= 20) baseByCount = 85;
        else if (count >= 10) baseByCount = 75;
        else if (count >= 5) baseByCount = 60;
        else if (count >= 3) baseByCount = 45;
        else baseByCount = 25;

        double avg = reviews.stream().mapToInt(Review::getNote).average().orElse(0.0);
        double variance = reviews.stream()
                .mapToDouble(r -> Math.pow(r.getNote() - avg, 2))
                .average()
                .orElse(0.0);

        // Plus la variance est élevée, plus la confiance baisse légèrement.
        double penalty = Math.min(20, variance * 6.5);
        int score = (int) Math.round(Math.max(0, Math.min(100, baseByCount - penalty)));
        return score;
    }

    private String fallbackSummary(List<Review> reviews) {
        double avg = reviews.stream().mapToInt(Review::getNote).average().orElse(0.0);
        String tendance;
        if (avg >= 4.2) {
            tendance = "Les avis sont majoritairement positifs.";
        } else if (avg >= 3.2) {
            tendance = "Les avis sont globalement mitigés.";
        } else {
            tendance = "Les avis sont majoritairement critiques.";
        }
        return String.format("%s Note moyenne observée: %.2f/5 sur %d avis.", tendance, avg, reviews.size());
    }
}
