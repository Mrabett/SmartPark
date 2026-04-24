package com.smartpark.backend.controller;

import org.springframework.web.bind.annotation.*;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.model.Recommandation;
import com.smartpark.backend.service.RecommandationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommandations")
public class RecommandationController {

    private final RecommandationService recommandationService;

    public RecommandationController(RecommandationService recommandationService) {
        this.recommandationService = recommandationService;
    }

    // ✅ Générer recommandations pour un client
    @PostMapping("/generer/{clientId}")
    public List<Recommandation> generer(@PathVariable String clientId) {
        return recommandationService.genererRecommandations(clientId);
    }

    // ✅ Voir les recommandations d'un client
    @GetMapping("/client/{clientId}")
    public List<Recommandation> getRecommandations(@PathVariable String clientId) {
        return recommandationService.getRecommandations(clientId);  // ✅ pas de changement ici
    }

    @GetMapping("/produits/{clientId}")
    public List<Produit> getProduitsRecommandes(@PathVariable String clientId) {
        return recommandationService.getProduitsRecommandesParCategorie(clientId);
    }

    // ✅ Marquer comme vue
    @PutMapping("/{id}/vue")
    public Recommandation marquerVue(@PathVariable String id) {
        return recommandationService.marquerVue(id);
    }

    // ✅ Marquer comme achetée
    @PutMapping("/{id}/achetee")
    public Recommandation marquerAchetee(@PathVariable String id) {
        return recommandationService.marquerAchetee(id);
    }
}
