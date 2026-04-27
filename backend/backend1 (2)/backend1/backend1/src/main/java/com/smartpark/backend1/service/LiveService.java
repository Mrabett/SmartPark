package com.smartpark.backend1.service;

import com.smartpark.backend1.dto.EvenementLiveDto;
import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LiveService {

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private EvenementRepository evenementRepository;

  /**
   * Compteur de visiteurs par événement.
   * ConcurrentHashMap + AtomicInteger = thread-safe
   * clé = evenementId, valeur = nb visiteurs actuellement sur la page
   */
  private final ConcurrentHashMap<String, AtomicInteger> visiteurs =
    new ConcurrentHashMap<>();

  // ── Visiteur arrive sur la page ────────────────────────────
  public void visiteurRejoint(String evenementId) {
    visiteurs.computeIfAbsent(evenementId, k -> new AtomicInteger(0))
      .incrementAndGet();
    broadcast(evenementId);
  }

  // ── Visiteur quitte la page ────────────────────────────────
  public void visiteurPart(String evenementId) {
    AtomicInteger counter = visiteurs.get(evenementId);
    if (counter != null) {
      int val = counter.decrementAndGet();
      if (val < 0) counter.set(0);
    }
    broadcast(evenementId);
  }

  // ── Broadcast après inscription/annulation ─────────────────
  public void broadcastMiseAJour(String evenementId) {
    broadcast(evenementId);
  }

  // ── Calcul + envoi du payload ──────────────────────────────
  private void broadcast(String evenementId) {
    Evenement ev = evenementRepository.findById(evenementId).orElse(null);
    if (ev == null) return;

    int nbInscrits      = ev.getNbInscrits();
    int capaciteMax     = ev.getCapaciteMax();
    int placesRestantes = Math.max(0, capaciteMax - nbInscrits);
    double taux         = capaciteMax > 0
      ? Math.min(100.0, (nbInscrits * 100.0) / capaciteMax)
      : 0.0;
    int visiteursCourants = visiteurs.getOrDefault(evenementId,
      new AtomicInteger(0)).get();

    EvenementLiveDto payload = new EvenementLiveDto(
      evenementId,
      nbInscrits,
      capaciteMax,
      placesRestantes,
      Math.round(taux * 10.0) / 10.0,
      visiteursCourants,
      placesRestantes <= 5 && placesRestantes > 0,  // urgence
      placesRestantes <= 0                           // complet
    );

    // Envoyer à tous les abonnés du topic de cet événement
    messagingTemplate.convertAndSend(
      "/topic/evenement/" + evenementId + "/live",
      payload
    );
  }

  public int getVisiteurs(String evenementId) {
    return visiteurs.getOrDefault(evenementId, new AtomicInteger(0)).get();
  }
}
