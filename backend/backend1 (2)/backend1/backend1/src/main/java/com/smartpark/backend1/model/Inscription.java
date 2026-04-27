package com.smartpark.backend1.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "inscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inscription {

  @Id
  private String id;

  private String evenementId;
  private String evenementTitre;

  private String participantNom;
  private String participantEmail;
  private String participantTel;

  private LocalDateTime dateInscription;
  private double montantPaye;

  /**
   * Statuts :
   *  CONFIRMEE     → place réservée
   *  EN_ATTENTE    → en attente de validation manuelle
   *  LISTE_ATTENTE → file d'attente
   *  ANNULEE       → annulée
   *  PRESENT       → ✅ NOUVEAU : présence confirmée par scan QR
   */
  private String statut;

  private String numeroBillet;
  private String notes;

  // ── Placement ─────────────────────────────────
  private String typeParticipant;
  private String placeId;
  private String zone;
  private int    numeroPlace;
  private String categoriePlace;

  // ── Liste d'attente ───────────────────────────
  private Integer       positionAttente;
  private LocalDateTime dateAjoutAttente;
  private LocalDateTime datePromotion;

  // ── ✅ NOUVEAU : Présence QR ──────────────────
  /**
   * Date et heure du scan QR par l'ouvrier.
   * null tant que la personne n'a pas été scannée.
   */
  private LocalDateTime datePresence;
}
