package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.AbonnementConfig;
import com.smartpark.backend.model.HistoriquePoints;
import com.smartpark.backend.model.PointsFidelite;
import com.smartpark.backend.service.MarketplaceFideliteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace/fidelite")
@CrossOrigin(origins = "http://localhost:4200")
public class MarketplaceFideliteController {

    private final MarketplaceFideliteService fideliteService;

    public MarketplaceFideliteController(MarketplaceFideliteService fideliteService) {
        this.fideliteService = fideliteService;
    }

    // ✅ Voir les points d'un client
    @GetMapping("/client/{utilisateurId}")
    public PointsFidelite getPoints(@PathVariable String utilisateurId) {
        return fideliteService.getPointsClient(utilisateurId);
    }

    // ✅ Voir la réduction d'un client
    @GetMapping("/reduction/{utilisateurId}")
    public Map<String, Object> getReduction(@PathVariable String utilisateurId) {
        double reduction = fideliteService.getReductionClient(utilisateurId);
        PointsFidelite points = fideliteService.getPointsClient(utilisateurId);
        return Map.of(
                "utilisateurId", utilisateurId,
                "niveau", points.getNiveau(),
                "pointsDisponibles", points.getPointsDisponibles(),
                "pourcentageReduction", reduction
        );
    }

    // ✅ Ajouter des points après commande
    @PostMapping({"/ajouter-points", "/add-purchase-points"})
    public PointsFidelite ajouterPoints(@RequestBody Map<String, Object> request) {
        String utilisateurId = (String) request.get("utilisateurId");
        String commandeId = request.get("commandeId") != null ? request.get("commandeId").toString() : "commande";
        Object nombreProduitsValue = request.get("nombreProduits") != null
                ? request.get("nombreProduits")
                : request.get("montantCommande") != null ? request.get("montantCommande") : request.get("montantTotal");
        int nombreProduits = nombreProduitsValue == null ? 0 : (int) Math.max(Double.parseDouble(nombreProduitsValue.toString()), 0);
        return fideliteService.ajouterPointsApresCommande(utilisateurId, commandeId, nombreProduits);
    }

    // ✅ Utiliser des points
    @PostMapping("/utiliser-points")
    public PointsFidelite utiliserPoints(@RequestBody Map<String, Object> request) {
        String utilisateurId = (String) request.get("utilisateurId");
        String commandeId = request.get("commandeId") != null ? request.get("commandeId").toString() : "commande";
        int pointsAUtiliser = Integer.parseInt(request.get("points").toString());
        try {
            return fideliteService.utiliserPoints(utilisateurId, commandeId, pointsAUtiliser);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping({"/acheter-abonnement", "/purchase-subscription"})
    public PointsFidelite acheterAbonnement(@RequestBody Map<String, Object> request) {
        String utilisateurId = (String) request.get("utilisateurId");
        String abonnement = request.get("abonnement") != null ? request.get("abonnement").toString() : "Abonnement";
        int pointsAUtiliser = request.get("points") == null ? 0 : Integer.parseInt(request.get("points").toString());
        try {
            return fideliteService.acheterAbonnement(utilisateurId, abonnement, pointsAUtiliser);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/abonnements")
    public List<AbonnementConfig> getAbonnements(@RequestParam(defaultValue = "false") boolean actifsSeulement) {
        return fideliteService.getAbonnements(actifsSeulement);
    }

    @PostMapping("/abonnements")
    public AbonnementConfig createAbonnement(@RequestBody AbonnementConfig abonnement) {
        try {
            return fideliteService.createAbonnement(abonnement);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/abonnements/{id}")
    public AbonnementConfig updateAbonnement(@PathVariable String id, @RequestBody AbonnementConfig abonnement) {
        try {
            return fideliteService.updateAbonnement(id, abonnement);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/abonnements/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAbonnement(@PathVariable String id) {
        fideliteService.deleteAbonnement(id);
    }

    // ✅ Historique des points d'un client
    @GetMapping("/historique/{utilisateurId}")
    public List<HistoriquePoints> getHistorique(@PathVariable String utilisateurId) {
        return fideliteService.getHistoriqueClient(utilisateurId);
    }
}
