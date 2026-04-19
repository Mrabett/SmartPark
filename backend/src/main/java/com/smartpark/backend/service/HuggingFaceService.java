package com.smartpark.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class HuggingFaceService {

    @Value("${huggingface.api.key:}")
    private String apiKey;

    // ✅ URL FINALE — api-inference (pas router)
    private static final String HF_BASE =
            "https://api-inference.huggingface.co/models";

    private final RestTemplate restTemplate =
            new RestTemplate();

    private boolean hasApiKey() {
        return apiKey != null
                && !apiKey.isBlank()
                && apiKey.startsWith("hf_");
    }

    // ✅ Headers CORRECTS — méthode validée
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ════════════════════════════════════════════
    // 🤖 CHATBOT — utilise fallback enrichi
    // ════════════════════════════════════════════
    public String genererReponse(String prompt) {
        if (!hasApiKey()) {
            return genererReponseLocale(prompt);
        }

        // ✅ Modèle text2text — 100% stable et gratuit
        String url = HF_BASE
                + "/google/flan-t5-base";

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", prompt);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, buildHeaders());

        try {
            // ✅ flan-t5 retourne List
            ResponseEntity<Object[]> response =
                    restTemplate.postForEntity(
                            url, request, Object[].class);

            if (response.getBody() != null
                    && response.getBody().length > 0) {
                Object first = response.getBody()[0];
                if (first instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map =
                            (Map<String, Object>) first;
                    Object text =
                            map.get("generated_text");
                    if (text != null
                            && !text.toString().isBlank()) {
                        return text.toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "HF Chat error: " + e.getMessage());
        }

        // Fallback toujours disponible
        return genererReponseLocale(prompt);
    }

    // ════════════════════════════════════════════
    // 😊 SENTIMENT — distilbert stable
    // ════════════════════════════════════════════
    public Map<String, Object> analyserSentiment(
            String texte) {
        if (!hasApiKey()) {
            return analyserSentimentLocal(texte);
        }

        String url = HF_BASE
                + "/distilbert-base-uncased-finetuned-sst-2-english";

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", texte);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, buildHeaders());

        try {
            ResponseEntity<Object[]> response =
                    restTemplate.postForEntity(
                            url, request, Object[].class);

            if (response.getBody() != null
                    && response.getBody().length > 0) {

                // La réponse est [[{label, score}, ...]]
                Object first = response.getBody()[0];
                List<?> list = null;

                if (first instanceof List) {
                    list = (List<?>) first;
                } else if (first instanceof Object[]) {
                    list = Arrays.asList(
                            (Object[]) first);
                }

                if (list != null && !list.isEmpty()) {
                    String sentiment = "NEUTRE";
                    double scoreMax  = 0;

                    for (Object item : list) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m =
                                    (Map<String, Object>) item;
                            Object s = m.get("score");
                            Object l = m.get("label");
                            if (s instanceof Number
                                    && l != null) {
                                double sc = ((Number) s)
                                        .doubleValue();
                                if (sc > scoreMax) {
                                    scoreMax  = sc;
                                    sentiment =
                                            l.toString();
                                }
                            }
                        }
                    }

                    // distilbert: POSITIVE / NEGATIVE
                    String fr =
                            sentiment.equalsIgnoreCase("POSITIVE")
                                    ? "POSITIF" : "NEGATIF";

                    Map<String, Object> res =
                            new HashMap<>();
                    res.put("sentiment", fr);
                    res.put("score",
                            Math.round(scoreMax * 100.0)
                                    / 100.0);
                    return res;
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "Sentiment error: " + e.getMessage());
        }

        return analyserSentimentLocal(texte);
    }

    // ════════════════════════════════════════════
    // 🏷️ CLASSIFICATION — bart-large-mnli
    // ════════════════════════════════════════════
    public String classerIntention(String message) {
        if (!hasApiKey()) {
            return classerIntentionLocale(message);
        }

        // ✅ BART retourne Map — ne pas changer en List !
        String url = HF_BASE
                + "/facebook/bart-large-mnli";

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", message);
        body.put("parameters", Map.of(
                "candidate_labels", Arrays.asList(
                        "reservation terrain",
                        "prix tarif",
                        "disponibilite horaire",
                        "annulation",
                        "information generale",
                        "probleme reclamation"
                )
        ));

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, buildHeaders());

        try {
            // ✅ BART retourne Map directement
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url, request, Map.class);

            if (response.getBody() != null) {
                Object labelsObj =
                        response.getBody().get("labels");
                if (labelsObj instanceof List) {
                    List<?> labels = (List<?>) labelsObj;
                    if (!labels.isEmpty()) {
                        return labels.get(0).toString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "Classification error: "
                            + e.getMessage());
        }

        return classerIntentionLocale(message);
    }

    // ════════════════════════════════════════════
    // 🔁 FALLBACK LOCAL COMPLET
    // Fonctionne SANS API — TOUJOURS disponible
    // ════════════════════════════════════════════

    public String genererReponseLocale(String prompt) {
        String p = prompt.toLowerCase();
        String intention = classerIntentionLocale(p);

        switch (intention) {
            case "reservation terrain":
                return genererReponseReservation(p);
            case "prix tarif":
                return genererReponseTarif(p);
            case "disponibilite horaire":
                return genererReponseDisponibilite(p);
            case "annulation":
                return "Pour annuler votre réservation ❌\n"
                        + "1. Allez dans 'Mes Réservations'\n"
                        + "2. Trouvez votre réservation\n"
                        + "3. Cliquez sur 'Annuler'\n"
                        + "✅ Gratuit avant la séance !";
            case "probleme reclamation":
                return "Désolé pour ce problème 😔\n"
                        + "Notre équipe est disponible "
                        + "pour vous aider. Contactez-nous ! 📧";
            default:
                return genererReponseGenerale(p);
        }
    }

    private String classerIntentionLocale(String m) {
        m = m.toLowerCase();
        if (m.contains("réserv") || m.contains("reserv")
                || m.contains("book"))
            return "reservation terrain";
        if (m.contains("prix") || m.contains("tarif")
                || m.contains("coût") || m.contains("payer"))
            return "prix tarif";
        if (m.contains("dispo") || m.contains("libre")
                || m.contains("horaire") || m.contains("heure"))
            return "disponibilite horaire";
        if (m.contains("annul") || m.contains("cancel"))
            return "annulation";
        if (m.contains("problème") || m.contains("plainte"))
            return "probleme reclamation";
        return "information generale";
    }

    private Map<String, Object> analyserSentimentLocal(
            String texte) {
        String t = texte.toLowerCase();
        if (t.contains("excellent") || t.contains("super")
                || t.contains("parfait") || t.contains("bien")
                || t.contains("génial") || t.contains("bravo"))
            return Map.of("sentiment","POSITIF","score",0.88);
        if (t.contains("mauvais") || t.contains("nul")
                || t.contains("problème") || t.contains("déçu")
                || t.contains("horrible"))
            return Map.of("sentiment","NEGATIF","score",0.82);
        return Map.of("sentiment", "NEUTRE", "score", 0.60);
    }

    private String genererReponseReservation(String p) {
        if (p.contains("comment") || p.contains("faire")) {
            return "Comment réserver un terrain ⚽\n"
                    + "1. Allez dans 'Terrains'\n"
                    + "2. Choisissez votre sport\n"
                    + "3. Sélectionnez date et créneau\n"
                    + "4. Remplissez vos informations\n"
                    + "5. Confirmez ! Prix calculé auto 💰";
        }
        return "Prêt à réserver ? 🏟️\n"
                + "Allez dans 'Terrains' pour voir "
                + "les créneaux disponibles.\n"
                + "Sport : ⚽🎾🏓🏀🏐";
    }

    private String genererReponseTarif(String p) {
        if (p.contains("weekend") || p.contains("samedi")
                || p.contains("dimanche"))
            return "Tarifs weekend 📅\n"
                    + "+30% le weekend.\n"
                    + "💡 Réservez en semaine pour économiser !";
        if (p.contains("soir") || p.contains("pointe"))
            return "Heures de pointe ⏰\n"
                    + "17h-21h : +50% sur le tarif.\n"
                    + "🌅 Matin = meilleurs prix !";
        return "Tarification SmartPark 💰\n"
                + "• ☀️ Matin 8h-12h : prix de base\n"
                + "• 🔥 Soir 17h-21h : +50%\n"
                + "• 📅 Weekend : +30%\n"
                + "• 📈 Forte demande : +20%\n"
                + "• 🎁 Faible demande : -20%";
    }

    private String genererReponseDisponibilite(String p) {
        if (p.contains("terrain") && (p.contains("quels")
                || p.contains("disponible")))
            return "Terrains disponibles 🏟️\n"
                    + "• ⚽ Football\n• 🎾 Tennis\n"
                    + "• 🏓 Padel\n• 🏀 Basketball\n"
                    + "• 🏐 Volleyball\n"
                    + "Voir 'Terrains' pour disponibilités !";
        return "Horaires SmartPark ⏰\n"
                + "Ouvert 8h-22h tous les jours\n"
                + "• 🟢 Libre · 🔴 Occupé\n"
                + "Consultez le calendrier !";
    }

    private String genererReponseGenerale(String p) {
        if (p.contains("bonjour") || p.contains("salut")
                || p.contains("hello")
                || p.contains("bonsoir"))
            return "Bonjour ! 👋 Je suis l'assistant "
                    + "IA SmartPark.\n"
                    + "Je peux vous aider avec :\n"
                    + "• 📅 Réservations\n"
                    + "• 💰 Tarifs\n"
                    + "• ⏰ Horaires\n"
                    + "• ❌ Annulations\n\n"
                    + "Comment puis-je vous aider ? 😊";
        if (p.contains("merci") || p.contains("parfait"))
            return "Avec plaisir ! 😊\n"
                    + "Bonne séance chez SmartPark ! 🏟️⚽";
        return "Je suis l'assistant SmartPark 🤖\n"
                + "Posez-moi votre question ! 💬";
    }
}