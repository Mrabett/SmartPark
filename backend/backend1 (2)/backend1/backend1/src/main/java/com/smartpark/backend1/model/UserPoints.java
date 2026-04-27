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
 * Suivi des points de fidélité par utilisateur.
 * 1 like reçu = 1 point.
 *
 * Barème de réduction :
 *  ≥  10 points →  5% de réduction
 *  ≥  25 points → 10% de réduction
 *  ≥  50 points → 15% de réduction
 *  ≥ 100 points → 20% de réduction
 */
@Document(collection = "user_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPoints {

  @Id
  private String id;

  private String userEmail;
  private String userNom;

  /** Total des points accumulés (jamais diminue) */
  private int pointsTotal;

  /** Points disponibles (diminue quand utilisés) */
  private int pointsDisponibles;

  /** Historique des transactions */
  @Builder.Default
  private List<PointTransaction> historique = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PointTransaction {
    private String type;          // GAIN | UTILISE
    private int points;
    private String description;   // "Like sur [post]" | "Réduction événement [titre]"
    private LocalDateTime date;
  }
}
