package com.smartpark.backend1.service;

import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.model.Photo;
import com.smartpark.backend1.model.UserPoints;
import com.smartpark.backend1.repository.EvenementRepository;
import com.smartpark.backend1.repository.PhotoRepository;
import com.smartpark.backend1.repository.UserPointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PhotoService {

  @Autowired private PhotoRepository       photoRepository;
  @Autowired private UserPointsRepository  userPointsRepository;
  @Autowired private EvenementRepository   evenementRepository;

  // ══════════════════════════════════════════════════════════
  // BARÈME DE RÉDUCTION
  // ══════════════════════════════════════════════════════════
  /** Retourne le pourcentage de réduction selon les points disponibles */
  public int calculerReduction(int points) {
    if (points >= 100) return 20;
    if (points >= 50)  return 15;
    if (points >= 25)  return 10;
    if (points >= 10)  return 5;
    return 0;
  }

  /** Points nécessaires pour la prochaine réduction */
  public int prochainPalier(int points) {
    if (points < 10)  return 10;
    if (points < 25)  return 25;
    if (points < 50)  return 50;
    if (points < 100) return 100;
    return 100;
  }

  // ══════════════════════════════════════════════════════════
  // POSTS
  // ══════════════════════════════════════════════════════════

  /** Créer un post photo après un événement */
  public Photo creerPost(String auteurEmail, String auteurNom,
                         String evenementId, String description,
                         List<String> photosBase64) {

    Evenement ev = evenementRepository.findById(evenementId)
      .orElseThrow(() -> new RuntimeException("Événement introuvable"));

    // Vérifier que l'événement est terminé ou en cours
    if ("PLANIFIE".equals(ev.getStatut())) {
      throw new RuntimeException(
        "Vous ne pouvez poster des photos que pour les événements terminés ou en cours.");
    }

    if (photosBase64 == null || photosBase64.isEmpty()) {
      throw new RuntimeException("Ajoutez au moins une photo.");
    }
    if (photosBase64.size() > 5) {
      throw new RuntimeException("Maximum 5 photos par post.");
    }

    Photo photo = Photo.builder()
      .auteurEmail(auteurEmail)
      .auteurNom(auteurNom)
      .evenementId(evenementId)
      .evenementTitre(ev.getTitre())
      .evenementDateDebut(ev.getDateDebut().toString())
      .evenementLieu(ev.getLieu())
      .description(description)
      .photosBase64(photosBase64)
      .datePost(LocalDateTime.now())
      .likesEmails(new ArrayList<>())
      .nbLikes(0)
      .build();

    return photoRepository.save(photo);
  }

  /** Récupérer tous les posts (feed global) */
  public List<Photo> getFeed() {
    return photoRepository.findAllByOrderByDatePostDesc();
  }

  /** Récupérer les posts d'un événement */
  public List<Photo> getPostsEvenement(String evenementId) {
    return photoRepository.findByEvenementIdOrderByDatePostDesc(evenementId);
  }

  /** Récupérer les posts d'un user */
  public List<Photo> getMesPosts(String userEmail) {
    return photoRepository.findByAuteurEmailOrderByDatePostDesc(userEmail);
  }

  /** Supprimer un post (seulement par l'auteur) */
  public void supprimerPost(String photoId, String userEmail) {
    Photo photo = photoRepository.findById(photoId)
      .orElseThrow(() -> new RuntimeException("Post introuvable"));
    if (!photo.getAuteurEmail().equals(userEmail)) {
      throw new RuntimeException("Vous ne pouvez supprimer que vos propres posts.");
    }
    photoRepository.deleteById(photoId);
  }

  // ══════════════════════════════════════════════════════════
  // LIKES
  // ══════════════════════════════════════════════════════════

  /**
   * Liker ou unliker un post.
   * Retourne l'état actuel : { liked: true/false, nbLikes: N }
   */
  public Map<String, Object> toggleLike(String photoId, String userEmail) {
    Photo photo = photoRepository.findById(photoId)
      .orElseThrow(() -> new RuntimeException("Post introuvable"));

    // Pas de like sur son propre post
    if (photo.getAuteurEmail().equals(userEmail)) {
      throw new RuntimeException("Vous ne pouvez pas liker votre propre post.");
    }

    boolean dejaLike = photo.getLikesEmails().contains(userEmail);

    if (dejaLike) {
      // ── UNLIKE ──────────────────────────────────────
      photo.getLikesEmails().remove(userEmail);
      photo.setNbLikes(photo.getLikesEmails().size());
      photoRepository.save(photo);

      // Retirer 1 point à l'auteur
      retirerPoint(photo.getAuteurEmail(), photo.getAuteurNom(),
        "Unlike sur \"" + photo.getEvenementTitre() + "\"");

      return Map.of("liked", false, "nbLikes", photo.getNbLikes());
    } else {
      // ── LIKE ────────────────────────────────────────
      photo.getLikesEmails().add(userEmail);
      photo.setNbLikes(photo.getLikesEmails().size());
      photoRepository.save(photo);

      // Ajouter 1 point à l'auteur
      ajouterPoint(photo.getAuteurEmail(), photo.getAuteurNom(),
        "Like reçu sur \"" + photo.getEvenementTitre() + "\"");

      return Map.of("liked", true, "nbLikes", photo.getNbLikes());
    }
  }

  // ══════════════════════════════════════════════════════════
  // POINTS
  // ══════════════════════════════════════════════════════════

  /** Obtenir les points d'un user (crée si n'existe pas) */
  public UserPoints getPoints(String userEmail, String userNom) {
    return userPointsRepository.findByUserEmail(userEmail)
      .orElseGet(() -> creerComptePoints(userEmail, userNom));
  }

  /**
   * Utiliser des points pour obtenir une réduction.
   * Retourne le pourcentage de réduction obtenu.
   */
  public Map<String, Object> utiliserPoints(String userEmail, String userNom,
                                            String evenementTitre) {
    UserPoints up = getPoints(userEmail, userNom);
    int reduction = calculerReduction(up.getPointsDisponibles());

    if (reduction == 0) {
      throw new RuntimeException(
        "Vous n'avez pas assez de points (minimum 10 points requis). " +
          "Vous avez " + up.getPointsDisponibles() + " points.");
    }

    // Calculer combien de points utiliser (on retire le palier utilisé)
    int pointsAUtiliser = palierMinimum(up.getPointsDisponibles());

    up.setPointsDisponibles(up.getPointsDisponibles() - pointsAUtiliser);

    UserPoints.PointTransaction tx = new UserPoints.PointTransaction(
      "UTILISE",
      -pointsAUtiliser,
      "Réduction " + reduction + "% sur \"" + evenementTitre + "\"",
      LocalDateTime.now()
    );
    up.getHistorique().add(0, tx);
    userPointsRepository.save(up);

    return Map.of(
      "reduction",      reduction,
      "pointsUtilises", pointsAUtiliser,
      "pointsRestants", up.getPointsDisponibles()
    );
  }

  // ── HELPERS POINTS ─────────────────────────────────────
  private void ajouterPoint(String email, String nom, String description) {
    UserPoints up = userPointsRepository.findByUserEmail(email)
      .orElseGet(() -> creerComptePoints(email, nom));
    up.setPointsTotal(up.getPointsTotal() + 1);
    up.setPointsDisponibles(up.getPointsDisponibles() + 1);
    UserPoints.PointTransaction tx = new UserPoints.PointTransaction(
      "GAIN", 1, description, LocalDateTime.now());
    up.getHistorique().add(0, tx);
    userPointsRepository.save(up);
  }

  private void retirerPoint(String email, String nom, String description) {
    userPointsRepository.findByUserEmail(email).ifPresent(up -> {
      if (up.getPointsTotal() > 0)      up.setPointsTotal(up.getPointsTotal() - 1);
      if (up.getPointsDisponibles() > 0) up.setPointsDisponibles(up.getPointsDisponibles() - 1);
      UserPoints.PointTransaction tx = new UserPoints.PointTransaction(
        "RETRAIT", -1, description, LocalDateTime.now());
      up.getHistorique().add(0, tx);
      userPointsRepository.save(up);
    });
  }

  private UserPoints creerComptePoints(String email, String nom) {
    UserPoints up = UserPoints.builder()
      .userEmail(email)
      .userNom(nom)
      .pointsTotal(0)
      .pointsDisponibles(0)
      .historique(new ArrayList<>())
      .build();
    return userPointsRepository.save(up);
  }

  private int palierMinimum(int points) {
    if (points >= 100) return 100;
    if (points >= 50)  return 50;
    if (points >= 25)  return 25;
    return 10;
  }
}
