package com.smartpark.backend1.controller;

import com.smartpark.backend1.model.Inscription;
import com.smartpark.backend1.service.InscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscriptions")
@CrossOrigin(origins = "http://localhost:4200")
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;

    @GetMapping
    public List<Inscription> getAll() {
        return inscriptionService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inscription> getById(@PathVariable String id) {
        return inscriptionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> inscrire(@RequestBody Inscription inscription) {
        try {
            return ResponseEntity.ok(inscriptionService.inscrire(inscription));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<Inscription> annuler(@PathVariable String id) {
        return ResponseEntity.ok(inscriptionService.annuler(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evenement/{evenementId}")
    public List<Inscription> getByEvenement(@PathVariable String evenementId) {
        return inscriptionService.getByEvenement(evenementId);
    }

    @GetMapping("/participant/{email}")
    public List<Inscription> getByParticipant(@PathVariable String email) {
        return inscriptionService.getByParticipant(email);
    }

    // ── Endpoint dédié waitlist ─────────────────────────────────
    /**
     * GET /api/inscriptions/evenement/{id}/waitlist
     * Retourne la liste d'attente complète, triée par position.
     */
    @GetMapping("/evenement/{evenementId}/waitlist")
    public List<Inscription> getWaitlist(@PathVariable String evenementId) {
        return inscriptionService.getWaitlist(evenementId);
    }

  // ════════════════════════════════════════════════════
  // ✅ SCAN QR CODE — Endpoint appelé par l'ouvrier
  // ════════════════════════════════════════════════════
  /**
   * GET /api/inscriptions/{numeroBillet}/scan
   *
   * L'ouvrier scanne le QR code → son téléphone appelle cette URL.
   * Le système :
   *   1. Cherche l'inscription par numéro de billet
   *   2. Vérifie que le statut est CONFIRMEE
   *   3. Passe le statut à PRESENT
   *   4. Retourne le résultat (VERIFIE / DEJA_PRESENT / INVALIDE)
   */
  @GetMapping("/{numeroBillet}/scan")
  public ResponseEntity<Map<String, Object>> scanQrCode(
    @PathVariable String numeroBillet) {
    try {
      Map<String, Object> result = inscriptionService.scannerBillet(numeroBillet);
      return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of(
          "statut", "INVALIDE",
          "message", e.getMessage()
        ));
    }
  }
}
