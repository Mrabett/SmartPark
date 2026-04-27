package com.smartpark.backend1.controller;

import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.model.Inscription;
import com.smartpark.backend1.repository.EvenementRepository;
import com.smartpark.backend1.repository.InscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/placement")
@CrossOrigin(origins = "http://localhost:4200")
public class PlacementController {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    /**
     * GET /api/placement/{evenementId}/disponibilite
     * Retourne toutes les places avec leur statut (libre/occupée)
     */
    @GetMapping("/{evenementId}/disponibilite")
    public ResponseEntity<Map<String, Object>> getDisponibilite(@PathVariable String evenementId) {
        Evenement ev = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        // Récupérer les places déjà occupées (non annulées)
        List<Inscription> inscriptions = inscriptionRepository
                .findByEvenementIdAndStatutNot(evenementId, "ANNULEE");

        Set<String> placesOccupees = new HashSet<>();
        for (Inscription i : inscriptions) {
            if (i.getPlaceId() != null) {
                placesOccupees.add(i.getPlaceId());
            }
        }

        // Construire la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("evenementId", evenementId);
        response.put("avecPlacement", ev.isAvecPlacement());
        response.put("placesOccupees", placesOccupees);

        // Config zones
        Map<String, Object> zones = new HashMap<>();

        Map<String, Object> zoneA = new HashMap<>();
        zoneA.put("nom", "Zone A");
        zoneA.put("categorie", "VIP");
        zoneA.put("nbPlaces", ev.getNbPlacesZoneA() > 0 ? ev.getNbPlacesZoneA() : 20);
        zoneA.put("prix", ev.getPrixZoneA() > 0 ? ev.getPrixZoneA() : ev.getPrixBillet() * 1.5);
        zoneA.put("couleur", "#6366f1");
        zoneA.put("description", "Places VIP — Vue parfaite, accès prioritaire");
        zones.put("A", zoneA);

        Map<String, Object> zoneB = new HashMap<>();
        zoneB.put("nom", "Zone B");
        zoneB.put("categorie", "STANDARD");
        zoneB.put("nbPlaces", ev.getNbPlacesZoneB() > 0 ? ev.getNbPlacesZoneB() : 30);
        zoneB.put("prix", ev.getPrixZoneB() > 0 ? ev.getPrixZoneB() : ev.getPrixBillet());
        zoneB.put("couleur", "#10b981");
        zoneB.put("description", "Places Standard — Bon emplacement");
        zones.put("B", zoneB);

        Map<String, Object> zoneC = new HashMap<>();
        zoneC.put("nom", "Zone C");
        zoneC.put("categorie", "ECONOMIQUE");
        zoneC.put("nbPlaces", ev.getNbPlacesZoneC() > 0 ? ev.getNbPlacesZoneC() : 50);
        zoneC.put("prix", ev.getPrixZoneC() > 0 ? ev.getPrixZoneC() : ev.getPrixBillet() * 0.7);
        zoneC.put("couleur", "#f59e0b");
        zoneC.put("description", "Places Économiques — Tarif réduit");
        zones.put("C", zoneC);

        response.put("zones", zones);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/placement/{evenementId}/places-occupees
     * Retourne juste la liste des placeId occupés (léger)
     */
    @GetMapping("/{evenementId}/places-occupees")
    public ResponseEntity<Set<String>> getPlacesOccupees(@PathVariable String evenementId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByEvenementIdAndStatutNot(evenementId, "ANNULEE");

        Set<String> placesOccupees = new HashSet<>();
        for (Inscription i : inscriptions) {
            if (i.getPlaceId() != null) {
                placesOccupees.add(i.getPlaceId());
            }
        }
        return ResponseEntity.ok(placesOccupees);
    }

    /**
     * POST /api/placement/{evenementId}/verifier-place
     * Vérifie si une place spécifique est encore disponible
     */
    @PostMapping("/{evenementId}/verifier-place")
    public ResponseEntity<Map<String, Object>> verifierPlace(
            @PathVariable String evenementId,
            @RequestBody Map<String, String> body) {

        String placeId = body.get("placeId");
        boolean occupee = inscriptionRepository
                .existsByEvenementIdAndPlaceIdAndStatutNot(evenementId, placeId, "ANNULEE");

        Map<String, Object> result = new HashMap<>();
        result.put("placeId", placeId);
        result.put("disponible", !occupee);

        return ResponseEntity.ok(result);
    }
}