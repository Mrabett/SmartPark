// src/main/java/com/smartpark/backend1/service/AiDescriptionService.java
package com.smartpark.backend1.service;

import com.smartpark.backend1.dto.AiGenerateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDescriptionService {

  @Value("${huggingface.api.token}")
  private String hfToken;

  // ✅ URL correcte selon la doc officielle HuggingFace 2025
  private static final String HF_URL =
    "https://router.huggingface.co/v1/chat/completions";

  // ✅ Modèle avec provider Cerebras (gratuit et rapide)
  private static final String MODEL = "meta-llama/Llama-3.1-8B-Instruct:cerebras";

  private final RestTemplate restTemplate;

  public String generateDescription(AiGenerateRequest req) {
    log.info("Génération IA — titre: {}, type: {}, ton: {}, variante: {}",
      req.getTitre(), req.getType(), req.getTon(), req.getVariant());
    return callHuggingFace(req);
  }

  @SuppressWarnings("unchecked")
  private String callHuggingFace(AiGenerateRequest req) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + hfToken);

    Map<String, Object> body = Map.of(
      "model",    MODEL,
      "messages", List.of(
        Map.of("role", "user", "content", buildPrompt(req))
      ),
      "max_tokens",  400,
      "temperature", 0.7
    );

    try {
      ResponseEntity<Map> resp = restTemplate.postForEntity(
        HF_URL,
        new HttpEntity<>(body, headers),
        Map.class
      );

      if (resp.getBody() == null) {
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Réponse IA vide");
      }

      List<Map<String, Object>> choices =
        (List<Map<String, Object>>) resp.getBody().get("choices");

      if (choices == null || choices.isEmpty()) {
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Aucun résultat IA");
      }

      Map<String, Object> message =
        (Map<String, Object>) choices.get(0).get("message");

      String text = (String) message.get("content");
      return text != null ? text.trim() : "";

    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      log.error("Erreur HuggingFace: {}", e.getMessage());
      throw new ResponseStatusException(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service IA indisponible: " + e.getMessage());
    }
  }

  private String buildPrompt(AiGenerateRequest req) {
    Map<String, String> typeLabels = Map.of(
      "TOURNOI", "tournoi sportif",
      "ATELIER", "atelier créatif",
      "CONCERT", "concert musical",
      "AUTRE",   "événement"
    );
    Map<String, String> variantInstr = Map.of(
      "courte",  "2-3 phrases courtes et percutantes",
      "longue",  "4-6 phrases détaillées et complètes",
      "réseaux", "1-2 phrases avec emojis et hashtags pour réseaux sociaux"
    );
    Map<String, String> tonExemples = Map.of(
      "professionnel", "formel et professionnel",
      "enthousiaste",  "dynamique et enthousiaste",
      "élégant",       "élégant et raffiné",
      "décontracté",   "décontracté et accessible",
      "accrocheur",    "accrocheur et impactant"
    );

    return String.format(
      "Tu es un rédacteur expert en événementiel pour SmartPark en Tunisie. " +
        "Rédige une description en français avec un ton %s en %s pour cet événement : " +
        "Titre: %s, Type: %s, Lieu: %s, Date: %s, Détails: %s. " +
        "Réponds UNIQUEMENT avec la description, sans introduction ni explication.",
      tonExemples.getOrDefault(req.getTon(), "professionnel"),
      variantInstr.getOrDefault(req.getVariant(), "2-3 phrases courtes"),
      req.getTitre(),
      typeLabels.getOrDefault(req.getType(), "événement"),
      req.getLieu()     != null ? req.getLieu()     : "SmartPark",
      req.getDate()     != null ? req.getDate()     : "prochainement",
      req.getKeywords() != null ? req.getKeywords() : "aucun"
    );
  }
}
