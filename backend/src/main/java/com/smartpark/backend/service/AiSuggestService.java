package com.smartpark.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service hybride de suggestion de produits :
 * 1) Essaie Ollama (modèle local, pas d'API key)
 * 2) Fallback vers un moteur local intelligent si Ollama n'est pas disponible
 */
@Service
public class AiSuggestService {

    @Value("${ollama.api-url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiSuggestService() {
        // Timeout de 120 secondes pour laisser Ollama le temps de répondre
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);  // 10s pour se connecter
        factory.setReadTimeout(120_000);    // 120s pour lire la réponse
        this.restTemplate = new RestTemplate(factory);
    }

    // ========================================================================
    // MAIN METHOD
    // ========================================================================

    public List<Map<String, Object>> suggestProducts(String description) {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }

        // --- TENTATIVE 1 : Ollama (local) ---
        try {
            System.out.println("🤖 Tentative Ollama (" + ollamaModel + ")...");
            String prompt = buildPrompt(description);
            String response = callOllama(prompt);
            List<Map<String, Object>> products = parseAiResponse(response);
            if (!products.isEmpty()) {
                System.out.println("✅ Ollama a généré " + products.size() + " produits");
                products.forEach(p -> p.put("source", "Ollama AI (" + ollamaModel + ")"));
                return products;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Ollama échoué: " + e.getMessage());
            System.err.println("   → Assurez-vous qu'Ollama est lancé (ollama serve)");
        }

        // --- TENTATIVE 2 : Moteur local intelligent ---
        System.out.println("🧠 Fallback vers le moteur local...");
        try {
            List<Map<String, Object>> localResults = generateLocalSuggestions(description);
            localResults.forEach(p -> p.put("source", "Moteur IA Local"));
            return localResults;
        } catch (Exception e) {
            return List.of(createErrorResult("Impossible de générer des suggestions."));
        }
    }

    // ========================================================================
    // OLLAMA API
    // ========================================================================

    private String callOllama(String prompt) throws Exception {
        String url = ollamaUrl + "/api/generate";

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");

        // Options : limiter la génération pour plus de vitesse
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", 0.7);
        options.put("num_predict", 1024);  // Limiter la longueur de sortie
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers
        );

        System.out.println("⏳ Envoi à Ollama... (peut prendre 20-60s)");
        long start = System.currentTimeMillis();

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, String.class
        );

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        System.out.println("⏱️ Ollama a répondu en " + elapsed + "s");

        JsonNode root = objectMapper.readTree(response.getBody());
        String text = root.path("response").asText();

        if (text == null || text.isBlank()) {
            throw new RuntimeException("Réponse Ollama vide");
        }

        return text;
    }

    // ========================================================================
    // PROMPT (court et efficace pour Mistral)
    // ========================================================================

    private String buildPrompt(String desc) {
        return """
            Tu es un expert en produits sportifs. Génère 2 produits détaillés pour cette demande: "%s".
            Chaque produit DOIT avoir un nom créatif et une description technique de 2 phrases.
            
            STRUCTURE JSON STRICTE (remplis TOUS les champs):
            {"produits":[{"produit":{"nom":"NOM UNIQUE","description":"DESCRIPTION COMPLETE","prix":45.0,"categorie":"EQUIPEMENT","image":"https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400","actif":true},"pertinence":95,"raison":"RAISON PRECISE","motsClesCorrespondants":["SPORT"],"isGenerated":true}]}
            """.formatted(desc);
    }

    // ========================================================================
    // RESPONSE PARSING
    // ========================================================================

    private List<Map<String, Object>> parseAiResponse(String json) {
        try {
            String cleaned = json.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode productsNode = root.has("produits") ? root.get("produits") : root;

            if (!productsNode.isArray()) {
                throw new RuntimeException("Format JSON non-tableau");
            }

            List<Map<String, Object>> validProducts = new ArrayList<>();
            for (JsonNode node : productsNode) {
                try {
                    Map<String, Object> entry = objectMapper.convertValue(node, new TypeReference<>() {});
                    Map<String, Object> p = (Map<String, Object>) entry.get("produit");
                    
                    // VALIDATION STRICTE
                    if (p != null && 
                        p.get("nom") != null && !p.get("nom").toString().isBlank() &&
                        p.get("description") != null && !p.get("description").toString().isBlank() &&
                        p.get("nom").toString().length() > 2) {

                        // Le stock est saisi manuellement dans le formulaire produit
                        p.remove("stock");
                        
                        // GUÉRISON DE L'IMAGE 🖼️
                        String img = p.get("image") != null ? p.get("image").toString() : "";
                        if (img.isBlank() || img.contains("XXXXX") || !img.startsWith("http")) {
                            // On détecte le type pour mettre une image cohérente
                            String type = detectProductType(p.get("nom").toString().toLowerCase());
                            p.put("image", pickImage(type));
                        }

                        entry.put("isGenerated", true);
                        validProducts.add(entry);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Saut d'un produit mal formé");
                }
            }

            if (validProducts.size() < 1) return Collections.emptyList();
            return validProducts;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ========================================================================
    // MOTEUR LOCAL INTELLIGENT (Fallback)
    // ========================================================================

    private List<Map<String, Object>> generateLocalSuggestions(String description) {
        String normalized = normalize(description);
        Set<String> tokens = tokenize(normalized);

        String sport = detectSport(normalized);
        String productType = detectProductType(normalized);
        String target = detectTarget(normalized);
        String material = detectMaterial(normalized);
        String color = detectColor(normalized);
        String level = detectLevel(normalized);
        String category = mapCategory(sport, productType);

        List<Map<String, Object>> results = new ArrayList<>();
        results.add(buildLocalProduct(1, sport, productType, target, material, color, level, category, tokens, description));
        results.add(buildLocalProduct(2, sport, productType, target, material, color, level, category, tokens, description));
        return results;
    }

    private Map<String, Object> buildLocalProduct(int variant, String sport, String productType,
            String target, String material, String color, String level, String category,
            Set<String> tokens, String originalDesc) {

        Random rng = new Random(originalDesc.hashCode() + variant);
        String[] prefixes = {"Pro", "Elite", "Ultra", "Max", "Prime", "Apex", "Titan"};
        String prefix = prefixes[rng.nextInt(prefixes.length)];

        String type = variant == 1 ? productType : getComplementaryType(productType, sport);
        if (variant == 2) category = mapCategory(sport, type);

        String name = buildProductName(prefix, type, sport, color);
        String desc = buildProductDescription(type, sport, target, material, color, level);
        double price = Math.round(estimatePrice(type, level, material) * (0.85 + rng.nextDouble() * 0.30) * 100.0) / 100.0;
        String image = pickImage(type);

        int matchCount = 0;
        for (String d : new String[]{sport, type, target, material, color}) {
            if (!d.isEmpty()) matchCount++;
        }
        int pertinence = Math.min(95, 55 + matchCount * 8);

        List<String> matchedKw = new ArrayList<>();
        for (String d : new String[]{sport, type, target, material, color}) {
            if (!d.isEmpty()) matchedKw.add(d);
        }

        String raison = "Produit généré pour " + (target.isEmpty() ? "les sportifs" : target)
            + ". Basé sur votre demande de " + (type.isEmpty() ? "équipement" : type)
            + " pour le " + (sport.isEmpty() ? "sport" : sport) + "."
            + (level.isEmpty() ? "" : " Niveau " + level + ".");

        Map<String, Object> produit = new LinkedHashMap<>();
        produit.put("nom", name);
        produit.put("description", desc);
        produit.put("prix", price);
        produit.put("categorie", category);
        produit.put("image", image);
        produit.put("actif", true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("produit", produit);
        result.put("pertinence", pertinence);
        result.put("raison", raison);
        result.put("motsClesCorrespondants", matchedKw);
        result.put("isGenerated", true);
        return result;
    }

    // ========================================================================
    // DÉTECTION
    // ========================================================================

    private static final Map<String, List<String>> SPORT_KEYWORDS = Map.ofEntries(
        Map.entry("football",   List.of("football", "foot", "soccer", "gardien", "crampon")),
        Map.entry("basketball", List.of("basketball", "basket", "panier", "dunk")),
        Map.entry("tennis",     List.of("tennis", "raquette", "cordage")),
        Map.entry("natation",   List.of("natation", "nager", "piscine", "aquatique", "palmes")),
        Map.entry("running",    List.of("running", "course", "jogging", "marathon", "courir", "trail")),
        Map.entry("musculation",List.of("musculation", "gym", "fitness", "haltere", "squat")),
        Map.entry("yoga",       List.of("yoga", "pilates", "meditation", "stretching")),
        Map.entry("cyclisme",   List.of("cyclisme", "velo", "vtt", "pedale")),
        Map.entry("boxe",       List.of("boxe", "boxing", "combat", "mma", "karate"))
    );

    private static final Map<String, List<String>> TYPE_KEYWORDS = Map.ofEntries(
        Map.entry("chaussure",  List.of("chaussure", "basket", "sneaker", "crampon", "semelle")),
        Map.entry("vetement",   List.of("vetement", "tenue", "maillot", "short", "tshirt", "legging", "veste")),
        Map.entry("boisson",    List.of("boisson", "drink", "eau", "hydratation", "isotonique")),
        Map.entry("nutrition",  List.of("proteine", "whey", "barre", "supplement", "nutrition")),
        Map.entry("protection", List.of("protection", "protege", "casque", "genouillere", "gants")),
        Map.entry("sac",        List.of("sac", "sacoche", "rangement", "gourde", "bouteille")),
        Map.entry("ballon",     List.of("ballon", "balle")),
        Map.entry("raquette",   List.of("raquette", "grip")),
        Map.entry("tapis",      List.of("tapis", "sol")),
        Map.entry("haltere",    List.of("haltere", "poids"))
    );

    private String detectSport(String t) { return detectBest(t, SPORT_KEYWORDS); }
    private String detectProductType(String t) { return detectBest(t, TYPE_KEYWORDS); }

    private String detectBest(String text, Map<String, List<String>> map) {
        String best = ""; int bestScore = 0;
        for (var e : map.entrySet()) {
            int score = 0;
            for (String kw : e.getValue()) if (text.contains(kw)) score += 2;
            if (score > bestScore) { bestScore = score; best = e.getKey(); }
        }
        return best;
    }

    private String detectTarget(String t) {
        if (t.contains("femme") || t.contains("fille")) return "femme";
        if (t.contains("homme") || t.contains("garcon")) return "homme";
        if (t.contains("enfant") || t.contains("junior")) return "enfant";
        return "";
    }

    private String detectMaterial(String t) {
        if (t.contains("cuir")) return "en cuir premium";
        if (t.contains("carbone")) return "en fibre de carbone";
        if (t.contains("graphite")) return "en graphite";
        if (t.contains("aluminium")) return "en aluminium";
        if (t.contains("coton")) return "en coton bio";
        return "";
    }

    private String detectColor(String t) {
        if (t.contains("noir")) return "Noir"; if (t.contains("blanc")) return "Blanc";
        if (t.contains("bleu")) return "Bleu"; if (t.contains("rouge")) return "Rouge";
        if (t.contains("vert")) return "Vert"; if (t.contains("rose")) return "Rose";
        return "";
    }

    private String detectLevel(String t) {
        if (t.contains("pro") || t.contains("professionnel")) return "professionnel";
        if (t.contains("debutant")) return "débutant";
        if (t.contains("expert")) return "expert";
        return "";
    }

    // ========================================================================
    // GÉNÉRATION
    // ========================================================================

    private String buildProductName(String prefix, String type, String sport, String color) {
        String typeName = switch (type) {
            case "chaussure" -> "Chaussure"; case "vetement" -> "Maillot";
            case "boisson" -> "Boisson Isotonique"; case "nutrition" -> "Barre Protéinée";
            case "protection" -> "Protection"; case "sac" -> "Sac de Sport";
            case "ballon" -> "Ballon"; case "raquette" -> "Raquette";
            case "tapis" -> "Tapis de Sport"; case "haltere" -> "Kit Haltères";
            default -> "Équipement";
        };
        StringBuilder sb = new StringBuilder(typeName).append(" ").append(prefix);
        if (!sport.isEmpty()) sb.append(" ").append(sport.substring(0, 1).toUpperCase()).append(sport.substring(1));
        if (!color.isEmpty()) sb.append(" ").append(color);
        return sb.toString();
    }

    private String buildProductDescription(String type, String sport, String target, String material, String color, String level) {
        String typeFr = switch (type) {
            case "chaussure" -> "Chaussure de sport"; case "vetement" -> "Vêtement technique";
            case "boisson" -> "Boisson sportive"; case "nutrition" -> "Complément nutritionnel";
            case "protection" -> "Équipement de protection"; case "sac" -> "Sac de sport";
            case "ballon" -> "Ballon officiel"; case "raquette" -> "Raquette de performance";
            case "tapis" -> "Tapis d'exercice"; case "haltere" -> "Équipement de musculation";
            default -> "Équipement sportif";
        };
        StringBuilder sb = new StringBuilder(typeFr);
        if (!level.isEmpty()) sb.append(" de niveau ").append(level);
        if (!sport.isEmpty()) sb.append(" conçu pour le ").append(sport);
        sb.append(". ");
        sb.append(!material.isEmpty() ? "Fabriqué " + material + " pour une durabilité maximale. " : "Matériaux de haute qualité. ");
        if (!target.isEmpty()) sb.append("Spécialement conçu pour ").append(target).append(". ");
        if (!color.isEmpty()) sb.append("Coloris ").append(color).append(". ");
        sb.append("Excellent rapport qualité-prix.");
        return sb.toString();
    }

    private String getComplementaryType(String type, String sport) {
        return switch (type) {
            case "chaussure" -> "sac"; case "vetement" -> "chaussure";
            case "boisson" -> "nutrition"; case "nutrition" -> "boisson";
            case "ballon" -> "chaussure"; case "raquette" -> "sac";
            case "tapis" -> "vetement"; case "haltere" -> "nutrition";
            default -> "sac";
        };
    }

    private double estimatePrice(String type, String level, String material) {
        double base = switch (type) {
            case "chaussure" -> 89.90; case "vetement" -> 39.90;
            case "boisson" -> 4.90; case "nutrition" -> 24.90;
            case "protection" -> 29.90; case "sac" -> 44.90;
            case "ballon" -> 34.90; case "raquette" -> 119.00;
            case "tapis" -> 24.90; case "haltere" -> 59.90;
            default -> 29.90;
        };
        if (level.contains("pro") || level.contains("expert")) base *= 1.4;
        if (material.contains("carbone") || material.contains("titane")) base *= 1.3;
        return base;
    }

    private String pickImage(String type) {
        return switch (type) {
            case "chaussure" -> "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600";
            case "vetement" -> "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600";
            case "boisson" -> "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600";
            case "nutrition" -> "https://images.unsplash.com/photo-1593095191071-82b03ad94ebb?w=600";
            case "protection" -> "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=600";
            case "sac" -> "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600";
            case "ballon" -> "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=600";
            case "raquette" -> "https://images.unsplash.com/photo-1622279457486-62dcc4a4bd13?w=600";
            case "tapis" -> "https://images.unsplash.com/photo-1599058917233-35835263ca7c?w=600";
            case "haltere" -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=600";
            default -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=600";
        };
    }

    private String mapCategory(String sport, String type) {
        if (!sport.isEmpty()) return switch (sport) {
            case "football" -> "Football"; case "basketball" -> "Basketball";
            case "tennis", "badminton" -> "Tennis & Raquettes";
            case "natation" -> "Natation & Aquatique"; case "running" -> "Course & Athlétisme";
            case "musculation" -> "Fitness & Musculation"; case "yoga" -> "Yoga & Bien-être";
            case "cyclisme" -> "Cyclisme & Vélo"; case "boxe" -> "Sports de Combat";
            default -> "Équipement Sportif";
        };
        return switch (type) {
            case "chaussure" -> "Chaussures"; case "vetement" -> "Vêtements & Textiles";
            case "boisson" -> "Boissons & Hydratation"; case "nutrition" -> "Nutrition Sportive";
            case "protection" -> "Protection & Sécurité"; case "sac" -> "Sacs & Rangement";
            default -> "Équipement Sportif";
        };
    }

    // ========================================================================
    // UTILS
    // ========================================================================

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase().trim();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.split("\\s+"))
            .filter(w -> w.length() >= 2)
            .collect(Collectors.toSet());
    }

    private Map<String, Object> createErrorResult(String msg) {
        Map<String, Object> produit = new LinkedHashMap<>();
        produit.put("nom", "Erreur"); produit.put("description", msg);
        produit.put("prix", 0); produit.put("categorie", "Erreur");
        produit.put("image", "https://images.unsplash.com/photo-1594322436404-5a0526db4d13?w=400");
        produit.put("actif", false);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("produit", produit); result.put("pertinence", 0);
        result.put("raison", msg); result.put("motsClesCorrespondants", List.of());
        result.put("isGenerated", false);
        return result;
    }
}
