package com.smartpark.backend.service;

import com.smartpark.backend.model.Terrain;
import com.smartpark.backend.model.Reservation;
import com.smartpark.backend.repository.TerrainRepository;
import com.smartpark.backend.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private ReservationRepository reservationRepository;

        @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> processMessage(
            String userMessage, String context) {

        // 1. Construire le contexte SmartPark
        String parkContext = buildParkContext();

        // 2. Appeler Claude AI
        String aiResponse = callClaudeAI(userMessage,
                parkContext, context);

        // 3. Détecter les intentions
        Map<String, Object> intent = detectIntent(userMessage);

        // 4. Retourner réponse + actions
        Map<String, Object> result = new HashMap<>();
        result.put("response",  aiResponse);
        result.put("intent",    intent);
        result.put("timestamp", new java.util.Date().toString());

        return result;
    }

    private String buildParkContext() {
        List<Terrain> terrains = terrainRepository.findAll();
        List<Reservation> reservations =
                reservationRepository.findAll();

        StringBuilder ctx = new StringBuilder();
        ctx.append("=== DONNÉES SMARTPARK EN TEMPS RÉEL ===\n\n");

        // Terrains disponibles
        ctx.append("TERRAINS DISPONIBLES :\n");
        terrains.forEach(t -> {
            long nbRes = reservations.stream()
                    .filter(r -> t.getId().equals(r.getTerrainId())
                            && "CONFIRMEE".equals(r.getStatut()))
                    .count();
            ctx.append(String.format(
                    "- %s (%s) | Tarif: %.0f DT/h | Statut: %s | Réservations: %d\n",
                    t.getNom(), t.getType(),
                    t.getTarifHeure(), t.getStatut(), nbRes));
        });

        // Stats générales
        long totalRes = reservations.stream()
                .filter(r -> "CONFIRMEE".equals(r.getStatut())).count();
        long disponibles = terrains.stream()
                .filter(t -> "DISPONIBLE".equals(t.getStatut())).count();

        ctx.append("\nSTATISTIQUES :\n");
        ctx.append("- Total terrains: ").append(terrains.size()).append("\n");
        ctx.append("- Terrains disponibles: ").append(disponibles).append("\n");
        ctx.append("- Réservations confirmées: ").append(totalRes).append("\n");
        ctx.append("- Date aujourd'hui: ").append(LocalDate.now()).append("\n");

        // Heures populaires
        ctx.append("\nHEURES LES PLUS DEMANDÉES : 17h-21h (heure de pointe)\n");
        ctx.append("TARIFICATION : +50% heure de pointe, ");
        ctx.append("+30% weekend, +20% forte demande\n");

        return ctx.toString();
    }

    private String callClaudeAI(String userMessage,
                                String parkContext,
                                String conversationHistory) {
        String systemPrompt =
                "Tu es l'assistant IA de SmartPark, un parc sportif moderne. " +
                        "Tu aides les clients à :\n" +
                        "- Trouver et réserver des terrains de sport\n" +
                        "- Connaître les disponibilités et tarifs\n" +
                        "- Comprendre la tarification dynamique\n" +
                        "- Obtenir des recommandations personnalisées\n\n" +
                        "Réponds toujours en français, de façon " +
                        "amicale et professionnelle. " +
                        "Sois concis (max 3-4 phrases). " +
                        "Si tu proposes une réservation, " +
                        "termine par une action claire.\n\n" +
                        "DONNÉES ACTUELLES DU PARC :\n" + parkContext;

        // Construire les messages
        List<Map<String, String>> messages = new ArrayList<>();

        // Historique conversation
        if (conversationHistory != null
                && !conversationHistory.isEmpty()) {
            try {
                // Parser l'historique simple
                String[] lines = conversationHistory.split("\n");
                for (String line : lines) {
                    if (line.startsWith("USER:")) {
                        Map<String, String> m = new HashMap<>();
                        m.put("role", "user");
                        m.put("content", line.substring(5).trim());
                        messages.add(m);
                    } else if (line.startsWith("AI:")) {
                        Map<String, String> m = new HashMap<>();
                        m.put("role", "assistant");
                        m.put("content", line.substring(3).trim());
                        messages.add(m);
                    }
                }
            } catch (Exception e) { /* ignorer */ }
        }

        // Message actuel
        Map<String, String> currentMsg = new HashMap<>();
        currentMsg.put("role", "user");
        currentMsg.put("content", userMessage);
        messages.add(currentMsg);

        // Appel API Anthropic
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-haiku-20240307");
        requestBody.put("max_tokens", 500);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.anthropic.com/v1/messages",
                    entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> content =
                        (List<Map<String, Object>>)
                                response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }
        } catch (Exception e) {
            return generateFallbackResponse(userMessage);
        }

        return generateFallbackResponse(userMessage);
    }

    // Réponse de secours si API indisponible
    private String generateFallbackResponse(String message) {
        String msg = message.toLowerCase();
        List<Terrain> terrains = terrainRepository.findAll();
        List<Terrain> disponibles = terrains.stream()
                .filter(t -> "DISPONIBLE".equals(t.getStatut()))
                .collect(Collectors.toList());

        if (msg.contains("disponible") || msg.contains("libre")) {
            return String.format(
                    "Nous avons %d terrain(s) disponible(s) en ce moment : %s. " +
                            "Souhaitez-vous réserver ?",
                    disponibles.size(),
                    disponibles.stream()
                            .map(t -> t.getNom() + " (" + t.getType() + ")")
                            .collect(Collectors.joining(", ")));
        }
        if (msg.contains("prix") || msg.contains("tarif")) {
            return "Nos tarifs varient selon l'heure et la demande. " +
                    "Tarif de base entre 7 et 100 DT/h. " +
                    "+50% aux heures de pointe (17h-21h). " +
                    "Consultez la page Tarification pour voir les prix en temps réel.";
        }
        if (msg.contains("foot") || msg.contains("football")) {
            return terrains.stream()
                    .filter(t -> "FOOT".equals(t.getType()))
                    .findFirst()
                    .map(t -> String.format(
                            "Notre terrain de football '%s' est %s au tarif de %.0f DT/h. " +
                                    "Voulez-vous le réserver ?",
                            t.getNom(), t.getStatut(), t.getTarifHeure()))
                    .orElse("Nous n'avons pas de terrain de football disponible.");
        }
        return "Bonjour ! Je suis l'assistant SmartPark. " +
                "Je peux vous aider à trouver un terrain disponible, " +
                "connaître les tarifs ou faire une réservation. " +
                "Que puis-je faire pour vous ?";
    }

    // Détecter l'intention pour actions automatiques
    private Map<String, Object> detectIntent(String message) {
        Map<String, Object> intent = new HashMap<>();
        String msg = message.toLowerCase();

        if (msg.contains("réserver") || msg.contains("reserver")
                || msg.contains("réservation")) {
            intent.put("type", "RESERVATION");
            intent.put("action", "/reservations/nouvelle");
            intent.put("label", "Faire une réservation");
        } else if (msg.contains("disponible") || msg.contains("libre")) {
            intent.put("type", "DISPONIBILITE");
            intent.put("action", "/terrains");
            intent.put("label", "Voir les terrains");
        } else if (msg.contains("prix") || msg.contains("tarif")) {
            intent.put("type", "TARIF");
            intent.put("action", "/tarification");
            intent.put("label", "Voir les tarifs");
        } else if (msg.contains("mes réservations")
                || msg.contains("historique")) {
            intent.put("type", "HISTORIQUE");
            intent.put("action", "/user/mes-reservations");
            intent.put("label", "Mes réservations");
        } else {
            intent.put("type", "INFO");
            intent.put("action", null);
            intent.put("label", null);
        }

        return intent;
    }

    // Suggestions intelligentes basées sur les données
    public Map<String, Object> getSmartSuggestions() {
        List<Terrain> disponibles = terrainRepository
                .findByStatut("DISPONIBLE");
        List<Reservation> reservations =
                reservationRepository.findAll();

        Map<String, Object> result = new HashMap<>();

        // Terrain le moins réservé = meilleure disponibilité
        Map<String, Long> countMap = reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::getTerrainId,
                        Collectors.counting()));

        List<String> suggestions = new ArrayList<>();
        suggestions.add("Quels terrains sont disponibles ?");
        suggestions.add("Quel est le tarif pour ce soir ?");
        suggestions.add("Je veux réserver un terrain de foot");
        suggestions.add("Quelles sont les heures les moins chères ?");
        suggestions.add("Comment fonctionne la tarification ?");

        result.put("suggestions", suggestions);
        result.put("terrainsDisponibles", disponibles.size());
        result.put("heurePeakActive",
                java.time.LocalTime.now().getHour() >= 17 &&
                        java.time.LocalTime.now().getHour() < 21);

        return result;
    }
}