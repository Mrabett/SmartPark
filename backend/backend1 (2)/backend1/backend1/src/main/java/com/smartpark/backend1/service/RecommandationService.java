// src/main/java/com/smartpark/backend1/service/RecommandationService.java
package com.smartpark.backend1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.model.Inscription;
import com.smartpark.backend1.repository.EvenementRepository;
import com.smartpark.backend1.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommandationService {

  private final InscriptionRepository inscriptionRepository;
  private final EvenementRepository   evenementRepository;
  private final RestTemplate          restTemplate;

  // ✅ Même token HuggingFace que AiDescriptionService
  @Value("${huggingface.api.token}")
  private String hfToken;

  // ✅ Même URL router HuggingFace
  private static final String HF_URL =
    "https://router.huggingface.co/v1/chat/completions";
  private static final String MODEL =
    "meta-llama/Llama-3.1-8B-Instruct:cerebras";

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // ══════════════════════════════════════════════════════════
  // MÉTHODE PRINCIPALE
  // ══════════════════════════════════════════════════════════

  public Map<String, Object> genererRecommandations(String email, String nom) {
    log.info("Génération recommandations pour : {}", email);

    // 1. Historique de l'utilisateur (hors annulées)
    List<Inscription> historique = inscriptionRepository
      .findByParticipantEmail(email)
      .stream()
      .filter(i -> !"ANNULEE".equals(i.getStatut()))
      .collect(Collectors.toList());

    // 2. Événements disponibles
    List<Evenement> disponibles = evenementRepository
      .findAll()
      .stream()
      .filter(e -> "PLANIFIE".equals(e.getStatut())
        || "EN_COURS".equals(e.getStatut()))
      .collect(Collectors.toList());

    if (disponibles.isEmpty()) {
      return Map.of(
        "recommandations", List.of(),
        "messageIA",       "Aucun événement disponible pour le moment.",
        "profil",          Map.of(),
        "sourceIA",        false
      );
    }

    // 3. Analyser le profil localement
    Map<String, Object> profil = analyserProfil(historique);

    // 4. Appeler HuggingFace pour les recommandations IA
    try {
      return appellerHuggingFace(
        nom.isBlank() ? email : nom,
        profil, historique, disponibles
      );
    } catch (Exception e) {
      log.warn("HuggingFace indisponible, fallback algorithmique : {}", e.getMessage());
      return recommandationsAlgorithmiques(profil, historique, disponibles);
    }
  }

  // ══════════════════════════════════════════════════════════
  // ANALYSE DU PROFIL UTILISATEUR
  // ══════════════════════════════════════════════════════════

  private Map<String, Object> analyserProfil(List<Inscription> historique) {
    Map<String, Object> profil = new LinkedHashMap<>();

    if (historique.isEmpty()) {
      profil.put("nouveauUtilisateur", true);
      profil.put("typesFrequents",     List.of());
      profil.put("budgetMoyen",        0.0);
      profil.put("nbInscriptions",     0);
      return profil;
    }

    // Types d'événements préférés
    Map<String, Long> comptageTypes = historique.stream()
      .collect(Collectors.groupingBy(
        i -> getTypeEvenement(i.getEvenementId()),
        Collectors.counting()
      ));

    List<String> typesFrequents = comptageTypes.entrySet().stream()
      .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
      .limit(3)
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());

    // Budget moyen des inscriptions confirmées
    double budgetMoyen = historique.stream()
      .filter(i -> "CONFIRMEE".equals(i.getStatut())
        || "PRESENT".equals(i.getStatut()))
      .mapToDouble(Inscription::getMontantPaye)
      .average()
      .orElse(0.0);

    // Titres passés pour contexte IA
    List<String> evenementsPasses = historique.stream()
      .map(Inscription::getEvenementTitre)
      .filter(Objects::nonNull)
      .distinct()
      .limit(10)
      .collect(Collectors.toList());

    profil.put("nouveauUtilisateur", false);
    profil.put("nbInscriptions",     historique.size());
    profil.put("typesFrequents",     typesFrequents);
    profil.put("budgetMoyen",        Math.round(budgetMoyen * 100.0) / 100.0);
    profil.put("evenementsPasses",   evenementsPasses);
    return profil;
  }

  private String getTypeEvenement(String evenementId) {
    if (evenementId == null) return "AUTRE";
    return evenementRepository.findById(evenementId)
      .map(Evenement::getType)
      .orElse("AUTRE");
  }

  // ══════════════════════════════════════════════════════════
  // APPEL HUGGINGFACE
  // ══════════════════════════════════════════════════════════

  @SuppressWarnings("unchecked")
  private Map<String, Object> appellerHuggingFace(
    String nomUser,
    Map<String, Object> profil,
    List<Inscription> historique,
    List<Evenement> disponibles) {

    // Résumé léger des événements pour le prompt
    List<Map<String, Object>> resumeEvs = disponibles.stream()
      .map(ev -> {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              ev.getId());
        m.put("titre",           ev.getTitre());
        m.put("type",            ev.getType());
        m.put("lieu",            ev.getLieu());
        m.put("dateDebut",       ev.getDateDebut() != null ? ev.getDateDebut().toString() : "");
        m.put("prixBillet",      ev.getPrixBillet());
        m.put("placesRestantes", Math.max(0, ev.getCapaciteMax() - ev.getNbInscrits()));
        return m;
      })
      .collect(Collectors.toList());

    String prompt = buildPrompt(nomUser, profil, resumeEvs);

    // ── Appel HuggingFace (même pattern que AiDescriptionService) ──
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + hfToken);

    Map<String, Object> body = Map.of(
      "model",    MODEL,
      "messages", List.of(Map.of("role", "user", "content", prompt)),
      "max_tokens",  800,
      "temperature", 0.5
    );

    ResponseEntity<Map> resp = restTemplate.postForEntity(
      HF_URL,
      new HttpEntity<>(body, headers),
      Map.class
    );

    if (resp.getBody() == null) {
      throw new RuntimeException("Réponse HuggingFace vide");
    }

    List<Map<String, Object>> choices =
      (List<Map<String, Object>>) resp.getBody().get("choices");
    if (choices == null || choices.isEmpty()) {
      throw new RuntimeException("Aucun choix dans la réponse HuggingFace");
    }

    Map<String, Object> message =
      (Map<String, Object>) choices.get(0).get("message");
    String jsonTexte = ((String) message.get("content")).trim();

    // Nettoyer les balises ```json si présentes
    jsonTexte = jsonTexte
      .replaceAll("(?s)```json\\s*", "")
      .replaceAll("(?s)```\\s*", "")
      .trim();

    // Extraire uniquement la partie JSON (entre { et })
    int debut = jsonTexte.indexOf('{');
    int fin   = jsonTexte.lastIndexOf('}');
    if (debut >= 0 && fin > debut) {
      jsonTexte = jsonTexte.substring(debut, fin + 1);
    }

    // Parser le JSON
    Map<String, Object> resultatIA;
    try {
      resultatIA = objectMapper.readValue(jsonTexte, Map.class);
    } catch (Exception e) {
      log.warn("JSON HuggingFace invalide, fallback algorithmique");
      throw new RuntimeException("JSON invalide : " + e.getMessage());
    }

    // Enrichir avec les données complètes des événements
    List<Map<String, Object>> recommandations =
      (List<Map<String, Object>>) resultatIA.getOrDefault("recommandations", List.of());

    List<Map<String, Object>> enrichies = recommandations.stream()
      .map(rec -> enrichirRecommandation(rec, disponibles))
      .collect(Collectors.toList());

    return Map.of(
      "recommandations", enrichies,
      "messageIA",       resultatIA.getOrDefault("messageIA", ""),
      "profil",          profil,
      "sourceIA",        true
    );
  }

  private Map<String, Object> enrichirRecommandation(
    Map<String, Object> rec,
    List<Evenement> disponibles) {

    String id = (String) rec.get("evenementId");
    Evenement ev = disponibles.stream()
      .filter(e -> e.getId().equals(id))
      .findFirst().orElse(null);

    if (ev != null) {
      rec.put("titre",           ev.getTitre());
      rec.put("type",            ev.getType());
      rec.put("lieu",            ev.getLieu());
      rec.put("dateDebut",       ev.getDateDebut()  != null ? ev.getDateDebut().toString()  : "");
      rec.put("heureDebut",      ev.getHeureDebut() != null ? ev.getHeureDebut().toString() : "");
      rec.put("prixBillet",      ev.getPrixBillet());
      rec.put("placesRestantes", Math.max(0, ev.getCapaciteMax() - ev.getNbInscrits()));
      rec.put("description",     ev.getDescription() != null ? ev.getDescription() : "");
    }
    return rec;
  }

  // ══════════════════════════════════════════════════════════
  // CONSTRUCTION DU PROMPT
  // ══════════════════════════════════════════════════════════

  private String buildPrompt(
    String nomUser,
    Map<String, Object> profil,
    List<Map<String, Object>> evenements) {

    String profilJson;
    String eventsJson;
    try {
      profilJson = objectMapper.writeValueAsString(profil);
      eventsJson = objectMapper.writeValueAsString(evenements);
    } catch (Exception e) {
      profilJson = profil.toString();
      eventsJson = evenements.toString();
    }

    return String.format("""
Tu es le moteur de recommandation de SmartPark (plateforme d'événements en Tunisie).

Analyse le profil et recommande les événements les plus pertinents.

UTILISATEUR: %s
PROFIL: %s
ÉVÉNEMENTS DISPONIBLES: %s

INSTRUCTIONS:
1. Sélectionne 3 à 5 événements pertinents selon le profil
2. Score de 0 à 100 pour chaque événement
3. Raison courte personnalisée (1-2 phrases en français)
4. Message d'accueil chaleureux (2-3 phrases en français)
5. Si nouveau utilisateur, recommande les événements populaires

Réponds UNIQUEMENT avec ce JSON sans aucun texte avant ou après:
{
  "messageIA": "message personnalisé pour %s",
  "recommandations": [
    {
      "evenementId": "id_exact",
      "score": 95,
      "raison": "raison courte",
      "tag": "🔥 Parfait pour vous"
    }
  ]
}
""", nomUser, profilJson, eventsJson, nomUser);
  }

  // ══════════════════════════════════════════════════════════
  // FALLBACK ALGORITHMIQUE (si HuggingFace indisponible)
  // ══════════════════════════════════════════════════════════

  @SuppressWarnings("unchecked")
  private Map<String, Object> recommandationsAlgorithmiques(
    Map<String, Object> profil,
    List<Inscription> historique,
    List<Evenement> disponibles) {

    List<String> typesFrequents =
      (List<String>) profil.getOrDefault("typesFrequents", List.of());
    double budgetMoyen =
      ((Number) profil.getOrDefault("budgetMoyen", 100.0)).doubleValue();

    List<Map<String, Object>> recommandations = disponibles.stream()
      .map(ev -> {
        int    score  = 50;
        String raison = "Événement disponible";
        String tag    = "📅 Disponible";

        // +30 si type préféré
        if (typesFrequents.contains(ev.getType())) {
          score += 30;
          raison = "Correspond à votre type d'événement préféré";
          tag    = "⭐ Recommandé";
        }
        // +20 si dans le budget
        if (budgetMoyen > 0 && ev.getPrixBillet() <= budgetMoyen * 1.2) {
          score += 20;
        }
        // Bonus places
        int places = Math.max(0, ev.getCapaciteMax() - ev.getNbInscrits());
        if (places > 5)  score += 10;
        if (places > 0 && places <= 5) { tag = "🔥 Dernières places"; score += 5; }

        Map<String, Object> rec = new LinkedHashMap<>();
        rec.put("evenementId",     ev.getId());
        rec.put("titre",           ev.getTitre());
        rec.put("type",            ev.getType());
        rec.put("lieu",            ev.getLieu());
        rec.put("dateDebut",       ev.getDateDebut()  != null ? ev.getDateDebut().toString()  : "");
        rec.put("heureDebut",      ev.getHeureDebut() != null ? ev.getHeureDebut().toString() : "");
        rec.put("prixBillet",      ev.getPrixBillet());
        rec.put("placesRestantes", places);
        rec.put("description",     ev.getDescription() != null ? ev.getDescription() : "");
        rec.put("score",           Math.min(100, score));
        rec.put("raison",          raison);
        rec.put("tag",             tag);
        return rec;
      })
      .sorted((a, b) -> Integer.compare(
        ((Number) b.get("score")).intValue(),
        ((Number) a.get("score")).intValue()))
      .limit(5)
      .collect(Collectors.toList());

    String messageIA = historique.isEmpty()
      ? "Bienvenue sur SmartPark ! Découvrez nos événements disponibles."
      : "Basé sur vos " + historique.size()
      + " inscriptions, voici ce qui pourrait vous intéresser.";

    return Map.of(
      "recommandations", recommandations,
      "messageIA",       messageIA,
      "profil",          profil,
      "sourceIA",        false
    );
  }
}
