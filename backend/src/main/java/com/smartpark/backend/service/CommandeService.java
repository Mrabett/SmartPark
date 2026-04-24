package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.LigneCommande;
import com.smartpark.backend.model.Commande;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.model.StatutCommande;
import com.smartpark.backend.model.StatutLivraison;
import com.smartpark.backend.repository.CommandeRepository;
import com.smartpark.backend.service.MarketplaceFideliteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitService produitService;
    private final MarketplaceFideliteService fideliteService;
    private final PromotionService promotionService;

    public CommandeService(CommandeRepository commandeRepository,
                           ProduitService produitService,
                           MarketplaceFideliteService fideliteService,
                           PromotionService promotionService) {
        this.commandeRepository = commandeRepository;
        this.produitService = produitService;
        this.fideliteService = fideliteService;
        this.promotionService = promotionService;
    }

    public List<Commande> findAll() {
        return commandeRepository.findAll();
    }

    public Commande findById(String id) {
        return commandeRepository.findById(id).orElse(null);
    }

    public Commande create(Commande commande) {
        commande.setId(null);
        if (commande.getUtilisateurId() == null || commande.getUtilisateurId().isBlank()) {
            throw new RuntimeException("Utilisateur requis pour créer une commande");
        }
        commande.setUtilisateurId(commande.getUtilisateurId().trim());
        if (commande.getDateCommande() == null) {
            commande.setDateCommande(LocalDateTime.now());
        }
        if (commande.getNumeroCommande() == null || commande.getNumeroCommande().isBlank()) {
            commande.setNumeroCommande(generateNumeroCommande());
        }

        if (commande.isLivraisonDemandee()) {
            if (commande.getLieuLivraison() == null || commande.getLieuLivraison().isBlank()) {
                throw new RuntimeException("Le lieu de livraison est obligatoire");
            }
            commande.setLieuLivraison(commande.getLieuLivraison().trim());
            if (commande.getStatutLivraison() == null) {
                commande.setStatutLivraison(StatutLivraison.EN_ATTENTE_AFFECTATION);
            }
        } else {
            commande.setLieuLivraison(null);
            commande.setStatutLivraison(null);
            commande.setAgentLivraisonId(null);
            commande.setAgentLivraisonNom(null);
            commande.setDateAffectationLivraison(null);
            commande.setDateLivraison(null);
        }

        if (commande.getLignes() != null) {
            for (LigneCommande ligne : commande.getLignes()) {
                if (ligne == null || ligne.getProduitId() == null || ligne.getProduitId().isBlank()) {
                    continue;
                }

                Produit produit = produitService.findById(ligne.getProduitId());
                if (produit == null) {
                    throw new RuntimeException("Produit introuvable pour la ligne: " + ligne.getProduitId());
                }

                double basePrice = produit.getPrix();
                double reduction = promotionService.getActiveReductionForProduit(produit.getId());
                double discountedPrice = basePrice * (1 - (Math.max(reduction, 0) / 100.0));

                ligne.setPrixUnitaire(roundMoney(discountedPrice));
                ligne.setNomProduit((ligne.getNomProduit() == null || ligne.getNomProduit().isBlank()) ? produit.getNom() : ligne.getNomProduit());
                ligne.setImage((ligne.getImage() == null || ligne.getImage().isBlank()) ? produit.getImage() : ligne.getImage());
                ligne.calculerSousTotal();
            }
        }

        double montantBrut = commande.calculerTotal();
        double pourcentageReduction = fideliteService.getReductionClient(commande.getUtilisateurId());
        double montantNet = montantBrut - (montantBrut * pourcentageReduction / 100.0);
        commande.setMontantTotal(roundMoney(Math.max(montantNet, 0)));

        int totalProduitsAchetes = 0;
        if (commande.getLignes() != null) {
            for (LigneCommande ligne : commande.getLignes()) {
                produitService.decreaseStock(ligne.getProduitId(), ligne.getQuantite());
            totalProduitsAchetes += Math.max(ligne.getQuantite(), 1);
            }
        }

        Commande savedCommande = commandeRepository.save(commande);
        if (pourcentageReduction > 0) {
            fideliteService.consommerAbonnementProchaineCommande(savedCommande.getUtilisateurId(), savedCommande.getId());
        }
        int totalProduitsFromSavedCommande = savedCommande.getLignes() == null
            ? 0
            : savedCommande.getLignes().stream()
            .filter(Objects::nonNull)
            .mapToInt(ligne -> Math.max(ligne.getQuantite(), 1))
            .sum();

        int pointsCommande = Math.max(totalProduitsAchetes, totalProduitsFromSavedCommande);
        System.out.println("=== AJOUT POINTS APRÈS COMMANDE ===");
        System.out.println("UtilisateurId: " + savedCommande.getUtilisateurId());
        System.out.println("CommandeId: " + savedCommande.getId());
        System.out.println("Points à ajouter: " + pointsCommande);
        try {
            fideliteService.ajouterPointsApresCommande(
                    savedCommande.getUtilisateurId(),
                    savedCommande.getId(),
                pointsCommande
            );
            System.out.println("✅ Points ajoutés avec succès");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'ajout des points: " + e.getMessage());
            e.printStackTrace();
        }
        return savedCommande;
    }

    public Commande update(Commande commande) {
        if (commande.getNumeroCommande() == null || commande.getNumeroCommande().isBlank()) {
            commande.setNumeroCommande(generateNumeroCommande());
        }
        commande.setMontantTotal(commande.calculerTotal());
        return commandeRepository.save(commande);
    }

    public void delete(String id) {
        commandeRepository.deleteById(id);
    }

    public List<Commande> findByUtilisateurId(String utilisateurId) {
        return commandeRepository.findByUtilisateurId(utilisateurId);
    }

    public List<Commande> findByStatut(StatutCommande statut) {
        return commandeRepository.findByStatut(statut);
    }

    public List<Commande> findLivraisons() {
        return commandeRepository.findByLivraisonDemandeeTrue();
    }

    public Commande requestLivraison(String commandeId, String lieuLivraison) {
        Commande commande = findById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande introuvable");
        }
        if (lieuLivraison == null || lieuLivraison.isBlank()) {
            throw new RuntimeException("Le lieu de livraison est obligatoire");
        }

        commande.setLivraisonDemandee(true);
        commande.setLieuLivraison(lieuLivraison.trim());
        commande.setStatutLivraison(StatutLivraison.EN_ATTENTE_AFFECTATION);
        commande.setAgentLivraisonId(null);
        commande.setAgentLivraisonNom(null);
        commande.setDateAffectationLivraison(null);
        commande.setDateLivraison(null);
        return commandeRepository.save(commande);
    }

    public Commande assignerAgentLivraison(String commandeId, String agentId, String agentNom) {
        Commande commande = findById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande introuvable");
        }
        if (!commande.isLivraisonDemandee()) {
            throw new RuntimeException("Cette commande n'est pas en livraison");
        }
        if ((agentId == null || agentId.isBlank()) && (agentNom == null || agentNom.isBlank())) {
            throw new RuntimeException("Un agent est requis");
        }

        commande.setAgentLivraisonId(agentId == null ? null : agentId.trim());
        commande.setAgentLivraisonNom(agentNom == null ? null : agentNom.trim());
        commande.setStatutLivraison(StatutLivraison.AFFECTEE);
        commande.setDateAffectationLivraison(LocalDateTime.now());
        return commandeRepository.save(commande);
    }

    public Commande updateStatutLivraison(String commandeId, StatutLivraison statutLivraison) {
        Commande commande = findById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande introuvable");
        }
        if (!commande.isLivraisonDemandee()) {
            throw new RuntimeException("Cette commande n'est pas en livraison");
        }

        commande.setStatutLivraison(statutLivraison);
        if (statutLivraison == StatutLivraison.LIVREE) {
            commande.setDateLivraison(LocalDateTime.now());
            commande.setStatut(StatutCommande.LIVREE);
        }
        if (statutLivraison == StatutLivraison.ANNULEE) {
            commande.setStatut(StatutCommande.ANNULEE);
        }
        return commandeRepository.save(commande);
    }

    private String generateNumeroCommande() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CMD-" + datePart + "-" + randomPart;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

