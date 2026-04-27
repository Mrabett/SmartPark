package com.smartpark.backend1.controller;

import com.smartpark.backend1.model.Inscription;
import com.smartpark.backend1.repository.InscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Endpoint HTML dédié au scan QR.
 * Quand l'ouvrier scanne le QR → son navigateur ouvre cette URL
 * → reçoit une page HTML lisible directement (pas de JSON, pas d'Angular).
 *
 * URL : GET /scan/{numeroBillet}
 */
@RestController
@RequestMapping("/scan")
@CrossOrigin(origins = "*")
public class ScanController {

  @Autowired
  private InscriptionRepository inscriptionRepository;

  @GetMapping(value = "/{numeroBillet}", produces = MediaType.TEXT_HTML_VALUE)
  public String scanQrCode(@PathVariable String numeroBillet) {

    Optional<Inscription> opt = inscriptionRepository.findByNumeroBillet(numeroBillet);

    // ── Billet introuvable ─────────────────────────────────
    if (opt.isEmpty()) {
      return buildPage(
        "INVALIDE",
        "❌",
        "Billet invalide",
        "Ce numéro de billet n'existe pas dans le système.",
        numeroBillet,
        null, null, null, null
      );
    }

    Inscription ins = opt.get();

    return switch (ins.getStatut()) {

      // ── Première présentation → valider ───────────────
      case "CONFIRMEE" -> {
        ins.setStatut("PRESENT");
        ins.setDatePresence(LocalDateTime.now());
        inscriptionRepository.save(ins);

        String place = (ins.getZone() != null && ins.getNumeroPlace() > 0)
          ? "Zone " + ins.getZone() + " — Place " + ins.getNumeroPlace()
          : null;

        yield buildPage(
          "VERIFIE",
          "✅",
          "Entrée validée !",
          "Bienvenue " + ins.getParticipantNom(),
          ins.getNumeroBillet(),
          ins.getParticipantNom(),
          ins.getEvenementTitre(),
          place,
          null
        );
      }

      // ── Déjà scanné ───────────────────────────────────
      case "PRESENT" -> {
        String quand = ins.getDatePresence() != null
          ? ins.getDatePresence().toLocalDate()
          + " à "
          + ins.getDatePresence().toLocalTime()
          .withSecond(0).withNano(0)
          : "inconnu";
        yield buildPage(
          "DEJA_PRESENT",
          "⚠️",
          "Déjà scanné",
          "Ce billet a déjà été utilisé le " + quand,
          ins.getNumeroBillet(),
          ins.getParticipantNom(),
          ins.getEvenementTitre(),
          null,
          null
        );
      }

      // ── Liste d'attente ───────────────────────────────
      case "LISTE_ATTENTE" -> buildPage(
        "LISTE_ATTENTE",
        "⏳",
        "Entrée refusée",
        "Ce participant est en liste d'attente. Il n'a pas de place confirmée.",
        ins.getNumeroBillet(),
        ins.getParticipantNom(),
        ins.getEvenementTitre(),
        null,
        null
      );

      // ── Annulé ────────────────────────────────────────
      case "ANNULEE" -> buildPage(
        "ANNULE",
        "❌",
        "Inscription annulée",
        "Cette inscription a été annulée. Entrée refusée.",
        ins.getNumeroBillet(),
        ins.getParticipantNom(),
        ins.getEvenementTitre(),
        null,
        null
      );

      default -> buildPage(
        "INVALIDE",
        "❓",
        "Statut inconnu",
        "Statut : " + ins.getStatut(),
        ins.getNumeroBillet(),
        null, null, null, null
      );
    };
  }

  // ══════════════════════════════════════════════════════════
  // Générateur de page HTML — lisible sur téléphone
  // ══════════════════════════════════════════════════════════
  private String buildPage(
    String statut, String emoji, String titre, String message,
    String numeroBillet, String nom, String evenement,
    String place, String extra) {

    String bgColor, borderColor, textColor, bgLight;

    switch (statut) {
      case "VERIFIE" -> {
        bgColor     = "#065f46";
        borderColor = "#059669";
        textColor   = "#6ee7b7";
        bgLight     = "#f0fdf4";
      }
      case "DEJA_PRESENT" -> {
        bgColor     = "#78350f";
        borderColor = "#d97706";
        textColor   = "#fde68a";
        bgLight     = "#fffbeb";
      }
      default -> {
        bgColor     = "#7f1d1d";
        borderColor = "#dc2626";
        textColor   = "#fca5a5";
        bgLight     = "#fef2f2";
      }
    }

    StringBuilder details = new StringBuilder();
    if (nom != null) {
      details.append("""
                <div class="detail-row">
                  <span class="d-label">👤 Participant</span>
                  <span class="d-val">%s</span>
                </div>
            """.formatted(nom));
    }
    if (evenement != null) {
      details.append("""
                <div class="detail-row">
                  <span class="d-label">🎪 Événement</span>
                  <span class="d-val">%s</span>
                </div>
            """.formatted(evenement));
    }
    if (place != null) {
      details.append("""
                <div class="detail-row">
                  <span class="d-label">💺 Place</span>
                  <span class="d-val">%s</span>
                </div>
            """.formatted(place));
    }
    if (numeroBillet != null) {
      details.append("""
                <div class="detail-row">
                  <span class="d-label">🎫 Billet</span>
                  <span class="d-val billet-num">%s</span>
                </div>
            """.formatted(numeroBillet));
    }

    return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>SmartPark — Scan Billet</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              background: #0d1117;
              color: #e8ecf3;
              min-height: 100vh;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              padding: 1.5rem 1rem;
            }
            .card {
              background: #13181f;
              border: 2px solid %s;
              border-radius: 24px;
              padding: 2.5rem 2rem;
              max-width: 420px;
              width: 100%%;
              text-align: center;
              animation: pop .4s ease;
            }
            .emoji-big { font-size: 5rem; display: block; margin-bottom: 1rem; animation: bounce .5s ease; }
            .status-title {
              font-size: 1.8rem;
              font-weight: 800;
              color: %s;
              margin-bottom: 0.5rem;
              font-family: 'Segoe UI', sans-serif;
            }
            .message {
              font-size: 1rem;
              color: #8892a4;
              line-height: 1.6;
              margin-bottom: 1.75rem;
            }
            .details-box {
              background: #1a2030;
              border: 1px solid rgba(255,255,255,0.07);
              border-radius: 14px;
              padding: 1.1rem;
              text-align: left;
              margin-bottom: 1.5rem;
            }
            .detail-row {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 0.6rem 0;
              border-bottom: 1px solid rgba(255,255,255,0.05);
              gap: 1rem;
            }
            .detail-row:last-child { border-bottom: none; }
            .d-label { font-size: 0.8rem; color: #4a5568; font-weight: 500; flex-shrink: 0; }
            .d-val   { font-size: 0.88rem; font-weight: 700; color: #e8ecf3; text-align: right; }
            .billet-num { font-family: 'Courier New', monospace; color: #00d97e; letter-spacing: 0.05em; }
            .brand {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 0.5rem;
              margin-bottom: 2rem;
              font-size: 1rem;
              font-weight: 800;
              color: #8892a4;
            }
            .brand-icon {
              width: 32px; height: 32px;
              background: linear-gradient(135deg, #00d97e, #009e55);
              border-radius: 8px;
              display: flex; align-items: center; justify-content: center;
              font-size: 1rem;
            }
            .brand span.g { color: #00d97e; }
            .timestamp {
              font-size: 0.72rem;
              color: #4a5568;
              margin-top: 1rem;
            }
            @keyframes pop    { from { opacity: 0; transform: scale(0.85); } to { opacity: 1; transform: scale(1); } }
            @keyframes bounce { 0%% { transform: scale(0.4); } 70%% { transform: scale(1.2); } 100%% { transform: scale(1); } }
          </style>
        </head>
        <body>
          <div class="brand">
            <div class="brand-icon">🏟️</div>
            Smart<span class="g">Park</span>
          </div>

          <div class="card">
            <span class="emoji-big">%s</span>
            <div class="status-title">%s</div>
            <div class="message">%s</div>

            %s

            <div class="timestamp">
              Scanné le %s
            </div>
          </div>
        </body>
        </html>
        """.formatted(
      borderColor,
      textColor,
      emoji,
      titre,
      message,
      details.length() > 0
        ? "<div class=\"details-box\">" + details + "</div>"
        : "",
      java.time.LocalDateTime.now()
        .toLocalDate() + " à "
        + java.time.LocalDateTime.now()
        .toLocalTime().withSecond(0).withNano(0)
    );
  }
}
