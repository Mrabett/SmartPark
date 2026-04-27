package com.smartpark.backend1.controller;

import com.smartpark.backend1.model.Photo;
import com.smartpark.backend1.model.UserPoints;
import com.smartpark.backend1.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/galerie")
@CrossOrigin(origins = "*")
public class PhotoController {

  @Autowired
  private PhotoService photoService;

  // ── Posts ──────────────────────────────────────────────

  /** POST /api/galerie/posts — créer un post */
  @PostMapping("/posts")
  public ResponseEntity<?> creerPost(@RequestBody Map<String, Object> body) {
    try {
      String auteurEmail    = (String) body.get("auteurEmail");
      String auteurNom      = (String) body.get("auteurNom");
      String evenementId    = (String) body.get("evenementId");
      String description    = (String) body.get("description");
      List<String> photos   = (List<String>) body.get("photos");

      Photo post = photoService.creerPost(
        auteurEmail, auteurNom, evenementId, description, photos);
      return ResponseEntity.ok(post);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  /** GET /api/galerie/feed — tous les posts */
  @GetMapping("/feed")
  public List<Photo> getFeed() {
    return photoService.getFeed();
  }

  /** GET /api/galerie/evenement/{id} — posts d'un événement */
  @GetMapping("/evenement/{evenementId}")
  public List<Photo> getPostsEvenement(@PathVariable String evenementId) {
    return photoService.getPostsEvenement(evenementId);
  }

  /** GET /api/galerie/mes-posts/{email} — mes posts */
  @GetMapping("/mes-posts/{email}")
  public List<Photo> getMesPosts(@PathVariable String email) {
    return photoService.getMesPosts(email);
  }

  /** DELETE /api/galerie/posts/{id} — supprimer un post */
  @DeleteMapping("/posts/{id}")
  public ResponseEntity<?> supprimerPost(
    @PathVariable String id,
    @RequestParam String userEmail) {
    try {
      photoService.supprimerPost(id, userEmail);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  // ── Likes ──────────────────────────────────────────────

  /** POST /api/galerie/posts/{id}/like — like/unlike */
  @PostMapping("/posts/{id}/like")
  public ResponseEntity<?> toggleLike(
    @PathVariable String id,
    @RequestBody Map<String, String> body) {
    try {
      String userEmail = body.get("userEmail");
      Map<String, Object> result = photoService.toggleLike(id, userEmail);
      return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  // ── Points ─────────────────────────────────────────────

  /** GET /api/galerie/points/{email} — solde de points */
  @GetMapping("/points/{email}")
  public ResponseEntity<?> getPoints(
    @PathVariable String email,
    @RequestParam(defaultValue = "") String nom) {
    try {
      UserPoints up = photoService.getPoints(email, nom);
      int reduction    = photoService.calculerReduction(up.getPointsDisponibles());
      int prochainPalier = photoService.prochainPalier(up.getPointsDisponibles());
      return ResponseEntity.ok(Map.of(
        "pointsTotal",       up.getPointsTotal(),
        "pointsDisponibles", up.getPointsDisponibles(),
        "reductionActuelle", reduction,
        "prochainPalier",    prochainPalier,
        "historique",        up.getHistorique()
      ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  /** POST /api/galerie/points/utiliser — utiliser des points */
  @PostMapping("/points/utiliser")
  public ResponseEntity<?> utiliserPoints(@RequestBody Map<String, String> body) {
    try {
      Map<String, Object> result = photoService.utiliserPoints(
        body.get("userEmail"),
        body.get("userNom"),
        body.get("evenementTitre")
      );
      return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }
}
