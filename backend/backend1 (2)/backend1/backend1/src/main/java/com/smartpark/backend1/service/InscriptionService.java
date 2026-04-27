package com.smartpark.backend1.service;

import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.model.Inscription;
import com.smartpark.backend1.repository.EvenementRepository;
import com.smartpark.backend1.repository.InscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InscriptionService {

  @Autowired private InscriptionRepository inscriptionRepository;
  @Autowired private EvenementRepository   evenementRepository;
  @Autowired private EvenementService      evenementService;
  @Autowired private EmailService          emailService;
  @Autowired private LiveService liveService;


  public List<Inscription> getAll() { return inscriptionRepository.findAll(); }
  public Optional<Inscription> getById(String id) { return inscriptionRepository.findById(id); }
  public List<Inscription> getByEvenement(String evenementId) { return inscriptionRepository.findByEvenementId(evenementId); }
  public List<Inscription> getWaitlist(String evenementId) {
    return inscriptionRepository.findByEvenementIdAndStatutOrderByPositionAttenteAsc(evenementId, "LISTE_ATTENTE");
  }
  public List<Inscription> getByParticipant(String email) { return inscriptionRepository.findByParticipantEmail(email); }

  // ── INSCRIPTION ────────────────────────────────────────────
  public Inscription inscrire(Inscription inscription) {
    Evenement evenement = evenementRepository.findById(inscription.getEvenementId())
      .orElseThrow(() -> new RuntimeException("Événement introuvable"));

    boolean dejaInscrit = inscriptionRepository
      .findByParticipantEmail(inscription.getParticipantEmail())
      .stream()
      .anyMatch(i -> i.getEvenementId().equals(inscription.getEvenementId())
        && !"ANNULEE".equals(i.getStatut()));
    if (dejaInscrit) {
      throw new RuntimeException("Vous êtes déjà inscrit ou en liste d'attente pour cet événement !");
    }

    long nbConfirmes = inscriptionRepository.countByEvenementIdAndStatut(inscription.getEvenementId(), "CONFIRMEE");

    inscription.setEvenementTitre(evenement.getTitre());
    inscription.setDateInscription(LocalDateTime.now());
    inscription.setNumeroBillet("SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    if (inscription.getTypeParticipant() == null) inscription.setTypeParticipant("SPECTATEUR");

    Inscription saved;

    if (nbConfirmes < evenement.getCapaciteMax()) {
      if (evenement.isAvecPlacement() && "SPECTATEUR".equals(inscription.getTypeParticipant())
        && inscription.getPlaceId() != null) {
        boolean placeOccupee = inscriptionRepository.existsByEvenementIdAndPlaceIdAndStatutNot(
          inscription.getEvenementId(), inscription.getPlaceId(), "ANNULEE");
        if (placeOccupee) throw new RuntimeException("Cette place est déjà occupée !");
      }
      inscription.setStatut("CONFIRMEE");
      inscription.setMontantPaye(calculerPrix(evenement, inscription));
      inscription.setPositionAttente(null);
      inscription.setDateAjoutAttente(null);
      saved = inscriptionRepository.save(inscription);
      emailService.envoyerEmailConfirmation(saved, evenement);
    } else {
      int prochainePosition = calculerProchainePosition(inscription.getEvenementId());
      inscription.setStatut("LISTE_ATTENTE");
      inscription.setMontantPaye(0);
      inscription.setPositionAttente(prochainePosition);
      inscription.setDateAjoutAttente(LocalDateTime.now());
      inscription.setPlaceId(null);
      inscription.setZone(null);
      inscription.setNumeroPlace(0);
      saved = inscriptionRepository.save(inscription);
      emailService.envoyerEmailListeAttente(saved, evenement);
    }

    evenementService.updateStats(inscription.getEvenementId());
    liveService.broadcastMiseAJour(inscription.getEvenementId()); // ← AJOUTER

    return saved;
  }

  // ── ANNULATION ─────────────────────────────────────────────
  public Inscription annuler(String id) {
    Inscription inscription = inscriptionRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
    String ancienStatut = inscription.getStatut();
    inscription.setStatut("ANNULEE");
    inscriptionRepository.save(inscription);
    if ("CONFIRMEE".equals(ancienStatut)) promouvoirPremierDeLaWaitlist(inscription.getEvenementId());
    evenementService.updateStats(inscription.getEvenementId());
    liveService.broadcastMiseAJour(inscription.getEvenementId()); // ← AJOUTER

    return inscription;
  }

  // ── SUPPRESSION ────────────────────────────────────────────
  public void delete(String id) {
    Inscription inscription = inscriptionRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
    String statut      = inscription.getStatut();
    String evenementId = inscription.getEvenementId();
    inscriptionRepository.deleteById(id);
    if ("CONFIRMEE".equals(statut) || "PRESENT".equals(statut)) {
      promouvoirPremierDeLaWaitlist(evenementId);
    } else if ("LISTE_ATTENTE".equals(statut)) {
      reorganiserWaitlist(evenementId);
    }
    evenementService.updateStats(evenementId);
    liveService.broadcastMiseAJour(evenementId); // ← AJOUTER

  }

  // ══════════════════════════════════════════════════════════
  // ✅ SCAN QR CODE — Logique principale
  // ══════════════════════════════════════════════════════════
  /**
   * Appelé quand l'ouvrier scanne le QR code d'un billet.
   *
   * Retourne une Map avec :
   *   - statut : "VERIFIE" | "DEJA_PRESENT" | "LISTE_ATTENTE" | "ANNULE" | "INVALIDE"
   *   - message : message lisible par l'ouvrier
   *   - nom    : nom du participant (si trouvé)
   *   - billet : numéro du billet
   *   - evenement : titre de l'événement
   */
  public Map<String, Object> scannerBillet(String numeroBillet) {
    Map<String, Object> result = new HashMap<>();

    // 1. Chercher le billet
    Optional<Inscription> opt = inscriptionRepository.findByNumeroBillet(numeroBillet);

    if (opt.isEmpty()) {
      result.put("statut",  "INVALIDE");
      result.put("message", "Billet introuvable. Code QR invalide ou non reconnu.");
      return result;
    }

    Inscription inscription = opt.get();
    result.put("nom",       inscription.getParticipantNom());
    result.put("email",     inscription.getParticipantEmail());
    result.put("billet",    inscription.getNumeroBillet());
    result.put("evenement", inscription.getEvenementTitre());

    // 2. Vérifier le statut
    switch (inscription.getStatut()) {

      case "CONFIRMEE":
        // ✅ Première scan → marquer comme PRESENT
        inscription.setStatut("PRESENT");
        inscription.setDatePresence(LocalDateTime.now());
        inscriptionRepository.save(inscription);
        result.put("statut",  "VERIFIE");
        result.put("message", "✅ Entrée validée ! Bienvenue " + inscription.getParticipantNom());
        if (inscription.getZone() != null) {
          result.put("place", "Zone " + inscription.getZone() + " — Place " + inscription.getNumeroPlace());
        }
        break;

      case "PRESENT":
        // ⚠️ Déjà scanné
        result.put("statut",  "DEJA_PRESENT");
        result.put("message", "⚠️ Ce billet a déjà été utilisé le "
          + inscription.getDatePresence().toLocalDate()
          + " à " + inscription.getDatePresence().toLocalTime().withSecond(0).withNano(0));
        break;

      case "LISTE_ATTENTE":
        result.put("statut",  "LISTE_ATTENTE");
        result.put("message", "❌ Ce participant est en liste d'attente. Entrée non autorisée.");
        break;

      case "ANNULEE":
        result.put("statut",  "ANNULE");
        result.put("message", "❌ Cette inscription a été annulée. Entrée refusée.");
        break;

      default:
        result.put("statut",  "INVALIDE");
        result.put("message", "❌ Statut inconnu : " + inscription.getStatut());
    }

    return result;
  }

  // ── PROMOTION AUTOMATIQUE WAITLIST ─────────────────────────
  private void promouvoirPremierDeLaWaitlist(String evenementId) {
    Optional<Inscription> premierOpt = inscriptionRepository
      .findFirstByEvenementIdAndStatutOrderByPositionAttenteAsc(evenementId, "LISTE_ATTENTE");
    if (premierOpt.isEmpty()) return;

    Inscription premier  = premierOpt.get();
    Evenement   evenement = evenementRepository.findById(evenementId).orElse(null);

    premier.setStatut("CONFIRMEE");
    premier.setDatePromotion(LocalDateTime.now());
    premier.setPositionAttente(null);
    premier.setDateAjoutAttente(null);
    if (evenement != null) premier.setMontantPaye(calculerPrix(evenement, premier));
    inscriptionRepository.save(premier);
    if (evenement != null) emailService.envoyerEmailPromotion(premier, evenement);
    reorganiserWaitlist(evenementId);
  }

  private void reorganiserWaitlist(String evenementId) {
    List<Inscription> waitlist = inscriptionRepository
      .findByEvenementIdAndStatutOrderByPositionAttenteAsc(evenementId, "LISTE_ATTENTE");
    int position = 1;
    for (Inscription i : waitlist) {
      if (i.getPositionAttente() == null || i.getPositionAttente() != position) {
        i.setPositionAttente(position);
        inscriptionRepository.save(i);
      }
      position++;
    }
  }

  private int calculerProchainePosition(String evenementId) {
    return inscriptionRepository
      .findFirstByEvenementIdAndStatutOrderByPositionAttenteDesc(evenementId, "LISTE_ATTENTE")
      .map(i -> i.getPositionAttente() + 1).orElse(1);
  }

  private double calculerPrix(Evenement ev, Inscription inscription) {
    if (ev.isAvecPlacement() && "SPECTATEUR".equals(inscription.getTypeParticipant())
      && inscription.getZone() != null) {
      switch (inscription.getZone()) {
        case "A": return ev.getPrixZoneA() > 0 ? ev.getPrixZoneA() : ev.getPrixBillet() * 1.5;
        case "B": return ev.getPrixZoneB() > 0 ? ev.getPrixZoneB() : ev.getPrixBillet();
        case "C": return ev.getPrixZoneC() > 0 ? ev.getPrixZoneC() : ev.getPrixBillet() * 0.7;
      }
    }
    return ev.getPrixBillet();
  }
}
