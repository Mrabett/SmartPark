package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.Commande;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.model.Recommandation;
import com.smartpark.backend.repository.CommandeRepository;
import com.smartpark.backend.repository.ProduitRepository;
import com.smartpark.backend.repository.RecommandationRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommandationService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final RecommandationRepository recommandationRepository;

    public RecommandationService(CommandeRepository commandeRepository,
                                 ProduitRepository produitRepository,
                                 RecommandationRepository recommandationRepository) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.recommandationRepository = recommandationRepository;
    }

    // ✅ Générer les recommandations pour un client
    public List<Recommandation> genererRecommandations(String clientId) {

        // 1. Trouver tous les produits achetés par ce client
        List<Commande> commandesClient = commandeRepository
                .findByUtilisateurId(clientId);

        Set<String> produitsAchetes = commandesClient.stream()
                .flatMap(c -> c.getLignes().stream())
                .map(l -> l.getProduitId())
                .collect(Collectors.toSet());

        // 2. Trouver les clients similaires
        //    (clients qui ont acheté les mêmes produits)
        List<Commande> toutesCommandes = commandeRepository.findAll();

        Map<String, Set<String>> produitsParClient = new HashMap<>();
        for (Commande c : toutesCommandes) {
            if (c.getUtilisateurId() == null) continue;
            produitsParClient
                    .computeIfAbsent(c.getUtilisateurId(), k -> new HashSet<>())
                    .addAll(c.getLignes().stream()
                            .map(l -> l.getProduitId())
                            .collect(Collectors.toSet()));
        }

        // 3. Trouver les produits achetés par clients similaires
        //    mais pas encore achetés par notre client
        Map<String, Integer> scoresProduits = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : produitsParClient.entrySet()) {
            if (entry.getKey().equals(clientId)) continue;

            Set<String> produitsSimilaire = entry.getValue();

            // Calculer la similarité
            Set<String> intersection = new HashSet<>(produitsSimilaire);
            intersection.retainAll(produitsAchetes);

            if (!intersection.isEmpty()) {
                // Ce client est similaire — ses produits non achetés = recommandations
                for (String produitId : produitsSimilaire) {
                    if (!produitsAchetes.contains(produitId)) {
                        scoresProduits.merge(produitId, intersection.size(), Integer::sum);
                    }
                }
            }
        }

        // 4. Créer les recommandations triées par score
        List<Recommandation> recommandations = new ArrayList<>();

        scoresProduits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)  // Top 5 recommandations
                .forEach(entry -> {
                    Recommandation r = new Recommandation();
                    r.setUtilisateurId(clientId);
                    r.setProduitId(entry.getKey());
                    r.setScore(entry.getValue() / 10.0);
                    r.setRaison("Les clients comme vous achètent aussi ce produit");
                    r.setDateGeneree(LocalDate.now());
                    r.setVue(false);
                    r.setAchetee(false);
                    recommandationRepository.save(r);
                    recommandations.add(r);
                });

        return recommandations;
    }

    // ✅ Voir les recommandations d'un client
    public List<Recommandation> getRecommandations(String clientId) {
        return recommandationRepository
                .findByUtilisateurIdOrderByScoreDesc(clientId);
    }

    // ✅ Marquer une recommandation comme vue
    public Recommandation marquerVue(String recommandationId) {
        Recommandation r = recommandationRepository
                .findById(recommandationId)
                .orElseThrow(() -> new RuntimeException("Recommandation non trouvée"));
        r.setVue(true);
        return recommandationRepository.save(r);
    }

    // ✅ Marquer une recommandation comme achetée
    public Recommandation marquerAchetee(String recommandationId) {
        Recommandation r = recommandationRepository
                .findById(recommandationId)
                .orElseThrow(() -> new RuntimeException("Recommandation non trouvée"));
        r.setAchetee(true);
        return recommandationRepository.save(r);
    }

    // ✅ Produits recommandés basés sur les catégories déjà achetées
    public List<Produit> getProduitsRecommandesParCategorie(String clientId) {
        List<Commande> commandesClient = commandeRepository.findByUtilisateurId(clientId);
        if (commandesClient == null || commandesClient.isEmpty()) {
            return getProduitsTendanceGlobal();
        }

        Set<String> produitIdsAchetes = commandesClient.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getLignes() != null)
                .flatMap(c -> c.getLignes().stream())
                .filter(Objects::nonNull)
                .map(l -> l.getProduitId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (produitIdsAchetes.isEmpty()) {
            return getProduitsTendanceGlobal();
        }

        List<Produit> produitsAchetes = produitRepository.findAllById(produitIdsAchetes);
        Map<String, Long> scoreCategorie = produitsAchetes.stream()
                .filter(Objects::nonNull)
                .map(Produit::getCategorie)
                .filter(cat -> cat != null && !cat.isBlank())
                .collect(Collectors.groupingBy(cat -> cat, Collectors.counting()));

        if (scoreCategorie.isEmpty()) {
            return getProduitsTendanceGlobal();
        }

        List<String> categoriesTriees = scoreCategorie.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<Produit> candidats = produitRepository.findByCategorieIn(categoriesTriees).stream()
                .filter(Objects::nonNull)
                .filter(Produit::isActif)
                .filter(p -> p.getId() != null && !produitIdsAchetes.contains(p.getId()))
                .toList();

        List<Produit> recommandes = candidats.stream()
                .sorted(Comparator.comparingLong((Produit p) -> scoreCategorie.getOrDefault(p.getCategorie(), 0L)).reversed())
                .limit(12)
                .collect(Collectors.toList());

        if (recommandes.isEmpty()) {
            return getProduitsTendanceGlobal();
        }

        return recommandes;
    }

    private List<Produit> getProduitsTendanceGlobal() {
        List<Commande> toutesCommandes = commandeRepository.findAll();

        Map<String, Integer> scoreProduits = new HashMap<>();
        for (Commande commande : toutesCommandes) {
            if (commande == null || commande.getLignes() == null) {
                continue;
            }
            for (var ligne : commande.getLignes()) {
                if (ligne == null || ligne.getProduitId() == null) {
                    continue;
                }
                scoreProduits.merge(ligne.getProduitId(), Math.max(ligne.getQuantite(), 1), Integer::sum);
            }
        }

        if (scoreProduits.isEmpty()) {
            return produitRepository.findAll().stream()
                    .filter(Produit::isActif)
                    .limit(12)
                    .collect(Collectors.toList());
        }

        List<String> topProduitIds = scoreProduits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();

        Map<String, Produit> produitsById = produitRepository.findAllById(topProduitIds).stream()
                .filter(Objects::nonNull)
                .filter(Produit::isActif)
                .collect(Collectors.toMap(Produit::getId, p -> p, (a, b) -> a));

        List<Produit> tries = new ArrayList<>();
        for (String id : topProduitIds) {
            Produit p = produitsById.get(id);
            if (p != null) {
                tries.add(p);
            }
            if (tries.size() >= 12) {
                break;
            }
        }

        if (tries.isEmpty()) {
            return produitRepository.findAll().stream()
                    .filter(Produit::isActif)
                    .limit(12)
                    .collect(Collectors.toList());
        }

        return tries;
    }
}
