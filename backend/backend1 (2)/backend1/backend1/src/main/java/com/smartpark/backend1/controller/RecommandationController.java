package com.smartpark.backend1.controller;

import com.smartpark.backend1.service.RecommandationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommandations")
@CrossOrigin(origins = "*")
public class RecommandationController {

  @Autowired
  private RecommandationService recommandationService;

  /**
   * GET /api/recommandations/{email}
   *
   * Analyse le profil de l'utilisateur et retourne :
   * - Liste des événements recommandés avec scores et raisons
   * - Message IA personnalisé
   * - Profil déduit de l'utilisateur
   */
  @GetMapping("/{email}")
  public ResponseEntity<?> getRecommandations(
    @PathVariable String email,
    @RequestParam(defaultValue = "") String nom) {
    try {
      Map<String, Object> result =
        recommandationService.genererRecommandations(email, nom);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }
}
