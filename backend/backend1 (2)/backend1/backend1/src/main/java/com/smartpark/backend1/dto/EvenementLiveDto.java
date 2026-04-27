package com.smartpark.backend1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload envoyé en temps réel via WebSocket à tous les abonnés.
 * Topic : /topic/evenement/{evenementId}/live
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvenementLiveDto {

  private String evenementId;

  /** Nombre de personnes avec statut CONFIRMEE */
  private int nbInscrits;

  /** Capacité maximale de l'événement */
  private int capaciteMax;

  /** Places restantes = capaciteMax - nbInscrits */
  private int placesRestantes;

  /** Taux d'occupation en % */
  private double tauxOccupation;

  /** Nombre de visiteurs actuellement sur la page de cet événement */
  private int visiteursCourants;

  /** true si placesRestantes <= 5 (urgence) */
  private boolean urgence;

  /** true si placesRestantes <= 0 */
  private boolean complet;
}
