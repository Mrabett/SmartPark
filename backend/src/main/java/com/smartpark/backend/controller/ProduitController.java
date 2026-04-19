package com.smartpark.backend.controller;

import com.smartpark.backend.dto.ProduitExpirationAlertDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.service.AiSuggestService;
import com.smartpark.backend.service.ProduitService;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;
    private final AiSuggestService aiSuggestService;

    public ProduitController(ProduitService produitService, AiSuggestService aiSuggestService) {
        this.produitService = produitService;
        this.aiSuggestService = aiSuggestService;
    }

    // ✅ AI — Suggestion de produits par description textuelle
    @PostMapping("/ai/suggest")
    public List<Map<String, Object>> suggestProducts(@RequestBody Map<String, String> request) {
        String description = request.getOrDefault("description", "");
        if (description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La description est requise");
        }
        return aiSuggestService.suggestProducts(description);
    }

    @GetMapping
    public List<Produit> findAll() {
        return produitService.findAll();
    }

    @GetMapping("/alertes/expiration")
    public List<ProduitExpirationAlertDTO> findExpirationAlerts(
            @RequestParam(name = "jours", defaultValue = "10") int jours
    ) {
        return produitService.findExpirationAlerts(jours);
    }

    @GetMapping("/{id}")
    public Produit findById(@PathVariable String id) {
        Produit produit = produitService.findById(id);
        if (produit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }
        return produit;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produit create(@RequestBody Produit produit) {
        return produitService.create(produit);
    }

    @PutMapping("/{id}/stock/decrease")
    public Produit decreaseStock(@PathVariable String id, @RequestBody Map<String, Object> request) {
        int quantite = Integer.parseInt(request.get("quantite").toString());
        return produitService.decreaseStock(id, quantite);
    }

    @PutMapping("/{id}")
    public Produit update(@PathVariable String id, @RequestBody Produit produit) {
        Produit existing = produitService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }

        existing.setNom(produit.getNom());
        existing.setDescription(produit.getDescription());
        existing.setPrix(produit.getPrix());
        existing.setCategorie(produit.getCategorie());
        existing.setImage(produit.getImage());
        existing.setActif(produit.isActif());
        existing.setDateExpiration(produit.getDateExpiration());
        existing.setStock(produit.getStock());
        existing.setPromotionIds(produit.getPromotionIds());

        return produitService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Produit existing = produitService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }
        produitService.delete(id);
    }
}

