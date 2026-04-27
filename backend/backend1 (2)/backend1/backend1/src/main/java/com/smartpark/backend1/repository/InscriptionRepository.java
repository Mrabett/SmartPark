package com.smartpark.backend1.repository;

import com.smartpark.backend1.model.Inscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends MongoRepository<Inscription, String> {

    List<Inscription> findByEvenementId(String evenementId);
    List<Inscription> findByParticipantEmail(String email);
    List<Inscription> findByStatut(String statut);

    // Compte uniquement les inscrits confirmés (pas la waitlist)
    long countByEvenementIdAndStatut(String evenementId, String statut);

    // Utilisé pour les stats globales
    long countByEvenementId(String evenementId);

    // Vérifier si un email est déjà inscrit ou en attente pour un événement
    List<Inscription> findByEvenementIdAndStatutNot(String evenementId, String statut);

    // Vérifier place occupée
    boolean existsByEvenementIdAndPlaceIdAndStatutNot(String evenementId, String placeId, String statut);

    // Compter par zone
    long countByEvenementIdAndZoneAndStatutNot(String evenementId, String zone, String statut);

    // ── Waitlist ────────────────────────────────────────────────

    /**
     * Récupère toute la liste d'attente d'un événement,
     * triée par position (le premier à rejoindre = position 1).
     */
    List<Inscription> findByEvenementIdAndStatutOrderByPositionAttenteAsc(
            String evenementId, String statut);

    /**
     * Compte combien de personnes sont en liste d'attente pour un événement.
     */
    long countByEvenementIdAndStatut(String evenementId);

    /**
     * Trouve le premier de la liste d'attente (position = 1).
     */
    Optional<Inscription> findFirstByEvenementIdAndStatutOrderByPositionAttenteAsc(
            String evenementId, String statut);

    /**
     * Trouve la position maximale dans la waitlist d'un événement
     * (pour calculer la prochaine position à assigner).
     */
    Optional<Inscription> findFirstByEvenementIdAndStatutOrderByPositionAttenteDesc(
            String evenementId, String statut);

  // ✅ NOUVEAU : Scan QR — chercher par numéro de billet
  Optional<Inscription> findByNumeroBillet(String numeroBillet);
}
