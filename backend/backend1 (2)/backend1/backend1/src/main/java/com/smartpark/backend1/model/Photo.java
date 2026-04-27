package com.smartpark.backend1.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un post photo posté par un participant après un événement.
 * Chaque like = 1 point pour l'auteur du post.
 */
@Document(collection = "photos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

  @Id
  private String id;

  // ── Auteur ────────────────────────────────────────────
  private String auteurEmail;
  private String auteurNom;

  // ── Événement lié ─────────────────────────────────────
  private String evenementId;
  private String evenementTitre;
  private String evenementDateDebut;   // pour affichage
  private String evenementLieu;

  // ── Contenu du post ───────────────────────────────────
  private String description;

  /**
   * Photos stockées en Base64 (data:image/jpeg;base64,...)
   * Limite recommandée : 3 photos max par post
   */
  @Builder.Default
  private List<String> photosBase64 = new ArrayList<>();

  // ── Dates ─────────────────────────────────────────────
  private LocalDateTime datePost;

  // ── Likes ─────────────────────────────────────────────
  /**
   * Liste des emails des users qui ont liké.
   * Permet d'empêcher de liker 2 fois.
   */
  @Builder.Default
  private List<String> likesEmails = new ArrayList<>();

  /** Nombre total de likes (= likesEmails.size()) */
  private int nbLikes;
}
