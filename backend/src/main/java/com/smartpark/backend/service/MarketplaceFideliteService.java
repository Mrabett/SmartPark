package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.AbonnementConfig;
import com.smartpark.backend.model.HistoriquePoints;
import com.smartpark.backend.model.PointsFidelite;
import com.smartpark.backend.model.NiveauFidelite;
import com.smartpark.backend.repository.AbonnementConfigRepository;
import com.smartpark.backend.repository.HistoriquePointsRepository;
import com.smartpark.backend.repository.PointsFideliteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketplaceFideliteService {

    private final PointsFideliteRepository pointsFideliteRepository;
    private final HistoriquePointsRepository historiquePointsRepository;
    private final AbonnementConfigRepository abonnementConfigRepository;

    public MarketplaceFideliteService(PointsFideliteRepository pointsFideliteRepository,
                           HistoriquePointsRepository historiquePointsRepository,
                           AbonnementConfigRepository abonnementConfigRepository) {
        this.pointsFideliteRepository = pointsFideliteRepository;
        this.historiquePointsRepository = historiquePointsRepository;
        this.abonnementConfigRepository = abonnementConfigRepository;
    }

    public PointsFidelite ajouterPointsApresCommande(
            String utilisateurId,
            String commandeId,
            int nombreProduitsAchetes) {

        System.out.println("🔵 [MarketplaceFideliteService] ajouterPointsApresCommande() appelée");
        
        if (utilisateurId == null || utilisateurId.isBlank()) {
            throw new RuntimeException("Utilisateur requis pour créditer les points");
        }
        utilisateurId = utilisateurId.trim();

        if (commandeId != null && !commandeId.isBlank() && historiquePointsRepository.existsByCommandeId(commandeId)) {
            return pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElse(creerNouveauCompte(utilisateurId));
        }

        PointsFidelite points = pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElse(creerNouveauCompte(utilisateurId));

        int pointsGagnes = Math.max(nombreProduitsAchetes, 0);
        if (pointsGagnes == 0 && commandeId != null && !commandeId.isBlank()) {
            pointsGagnes = 1;
        }

        points.ajouterPoints(pointsGagnes);
        
        PointsFidelite saved = pointsFideliteRepository.save(points);

        HistoriquePoints historique = new HistoriquePoints();
        historique.setPointsGagnes(pointsGagnes);
        historique.setPointsUtilises(0);
        historique.setMotif("Commande #" + commandeId);
        historique.setDate(LocalDateTime.now());
        historique.setClientId(utilisateurId);
        historique.setCommandeId(commandeId);
        historiquePointsRepository.save(historique);

        return saved;
    }

    public PointsFidelite ajouterPointsApresCommande(String utilisateurId, String commandeId, Number montantCommande) {
        return ajouterPointsApresCommande(utilisateurId, commandeId, montantCommande.intValue());
    }

    public PointsFidelite utiliserPoints(String utilisateurId,
                                         String commandeId,
                                         int pointsAUtiliser) {

        PointsFidelite points = pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité non trouvé"));

        boolean succes = points.utiliserPoints(pointsAUtiliser);
        if (!succes) {
            throw new RuntimeException("Points insuffisants");
        }

        pointsFideliteRepository.save(points);

        HistoriquePoints historique = new HistoriquePoints();
        historique.setPointsGagnes(0);
        historique.setPointsUtilises(pointsAUtiliser);
        historique.setMotif("Réduction appliquée sur commande #" + commandeId);
        historique.setDate(LocalDateTime.now());
        historique.setClientId(utilisateurId);
        historique.setCommandeId(commandeId);
        historiquePointsRepository.save(historique);

        return points;
    }

    public PointsFidelite acheterAbonnement(String utilisateurId, String abonnement, int pointsAUtiliser) {
        ensureDefaultAbonnements();

        PointsFidelite points = pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité non trouvé"));

        AbonnementConfig config = abonnementConfigRepository.findByCode(abonnement == null ? "" : abonnement.trim().toUpperCase())
                .filter(AbonnementConfig::isActif)
                .orElseThrow(() -> new RuntimeException("Abonnement inconnu ou inactif: " + abonnement));

        int pointsRequis = config.getPointsRequis();

        boolean succes = points.utiliserPoints(pointsRequis);
        if (!succes) {
            throw new RuntimeException("Points insuffisants pour l'abonnement " + abonnement + " (" + pointsRequis + " requis)");
        }

        points.setAbonnementActifCodeProchaineCommande(config.getCode());
        points.setAbonnementActifLabelProchaineCommande(config.getLabel());
        points.setAbonnementActifReductionProchaineCommande(config.getPourcentageReduction());

        pointsFideliteRepository.save(points);

        HistoriquePoints historique = new HistoriquePoints();
        historique.setPointsGagnes(0);
        historique.setPointsUtilises(pointsRequis);
        historique.setMotif("Abonnement " + abonnement);
        historique.setDate(LocalDateTime.now());
        historique.setClientId(utilisateurId);
        historique.setCommandeId(null);
        historiquePointsRepository.save(historique);

        return points;
    }

    public void consommerAbonnementProchaineCommande(String utilisateurId, String commandeId) {
        PointsFidelite points = pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElse(null);

        if (points == null || points.getAbonnementActifReductionProchaineCommande() <= 0) {
            return;
        }

        String abonnementConsomme = points.getAbonnementActifLabelProchaineCommande() != null
            ? points.getAbonnementActifLabelProchaineCommande()
            : points.getAbonnementActifCodeProchaineCommande();

        points.setAbonnementActifCodeProchaineCommande(null);
        points.setAbonnementActifLabelProchaineCommande(null);
        points.setAbonnementActifReductionProchaineCommande(0);
        pointsFideliteRepository.save(points);

        HistoriquePoints historique = new HistoriquePoints();
        historique.setPointsGagnes(0);
        historique.setPointsUtilises(0);
        historique.setMotif("Abonnement " + abonnementConsomme + " consomme sur commande #" + commandeId);
        historique.setDate(LocalDateTime.now());
        historique.setClientId(utilisateurId);
        historique.setCommandeId(commandeId);
        historiquePointsRepository.save(historique);
    }

    public List<AbonnementConfig> getAbonnements(boolean actifsSeulement) {
        ensureDefaultAbonnements();
        if (actifsSeulement) {
            return abonnementConfigRepository.findByActifTrue();
        }
        return abonnementConfigRepository.findAll();
    }

    public AbonnementConfig createAbonnement(AbonnementConfig abonnement) {
        ensureDefaultAbonnements();
        validateAbonnement(abonnement, true);
        abonnement.setId(null);
        abonnement.setCode(abonnement.getCode().trim().toUpperCase());
        return abonnementConfigRepository.save(abonnement);
    }

    public AbonnementConfig updateAbonnement(String id, AbonnementConfig abonnement) {
        AbonnementConfig existing = abonnementConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));

        if (abonnement.getCode() != null && !abonnement.getCode().isBlank()) {
            existing.setCode(abonnement.getCode().trim().toUpperCase());
        }
        if (abonnement.getLabel() != null && !abonnement.getLabel().isBlank()) {
            existing.setLabel(abonnement.getLabel().trim());
        }
        existing.setPointsRequis(abonnement.getPointsRequis());
        existing.setPourcentageReduction(abonnement.getPourcentageReduction());
        existing.setActif(abonnement.isActif());

        validateAbonnement(existing, false);
        return abonnementConfigRepository.save(existing);
    }

    public void deleteAbonnement(String id) {
        abonnementConfigRepository.deleteById(id);
    }

    private void validateAbonnement(AbonnementConfig abonnement, boolean checkCodeUniq) {
        if (abonnement.getCode() == null || abonnement.getCode().isBlank()) {
            throw new RuntimeException("Code abonnement requis");
        }
        if (abonnement.getLabel() == null || abonnement.getLabel().isBlank()) {
            throw new RuntimeException("Label abonnement requis");
        }
        if (abonnement.getPointsRequis() <= 0) {
            throw new RuntimeException("Points requis invalides");
        }
        if (abonnement.getPourcentageReduction() < 0 || abonnement.getPourcentageReduction() > 100) {
            throw new RuntimeException("Pourcentage de reduction invalide");
        }
        if (checkCodeUniq && abonnementConfigRepository.findByCode(abonnement.getCode().trim().toUpperCase()).isPresent()) {
            throw new RuntimeException("Code abonnement deja existant");
        }
    }

    private void ensureDefaultAbonnements() {
        if (!abonnementConfigRepository.findAll().isEmpty()) {
            return;
        }
        abonnementConfigRepository.save(buildDefault("BRONZE", "Bronze", 10, 2));
        abonnementConfigRepository.save(buildDefault("SILVER", "Silver", 15, 5));
        abonnementConfigRepository.save(buildDefault("GOLD", "Gold", 20, 10));
        abonnementConfigRepository.save(buildDefault("PLATINUM", "Platinum", 25, 15));
    }

    private AbonnementConfig buildDefault(String code, String label, int points, double reduction) {
        AbonnementConfig config = new AbonnementConfig();
        config.setCode(code);
        config.setLabel(label);
        config.setPointsRequis(points);
        config.setPourcentageReduction(reduction);
        config.setActif(true);
        return config;
    }

    public PointsFidelite getPointsClient(String utilisateurId) {
        return pointsFideliteRepository
                .findFirstByUtilisateurId(utilisateurId)
                .orElse(creerNouveauCompte(utilisateurId));
    }

    public List<HistoriquePoints> getHistoriqueClient(String utilisateurId) {
        return historiquePointsRepository.findByClientId(utilisateurId);
    }

    public double getReductionClient(String utilisateurId) {
        PointsFidelite points = getPointsClient(utilisateurId);
        return points.getPourcentageReduction();
    }

    private PointsFidelite creerNouveauCompte(String utilisateurId) {
        PointsFidelite nouveau = new PointsFidelite();
        nouveau.setUtilisateurId(utilisateurId);
        nouveau.setPointsTotal(0);
        nouveau.setPointsDisponibles(0);
        nouveau.setPointsUtilises(0);
        nouveau.setNiveau(NiveauFidelite.BRONZE);
        nouveau.setAbonnementActifCodeProchaineCommande(null);
        nouveau.setAbonnementActifLabelProchaineCommande(null);
        nouveau.setAbonnementActifReductionProchaineCommande(0);
        nouveau.setDateDerniereMiseAJour(LocalDateTime.now());
        return pointsFideliteRepository.save(nouveau);
    }
}
