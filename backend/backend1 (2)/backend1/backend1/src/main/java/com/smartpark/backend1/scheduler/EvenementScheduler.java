package com.smartpark.backend1.scheduler;

import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Job planifié qui vérifie toutes les minutes les événements
 * et met à jour automatiquement leur statut selon les dates :
 *
 *   PLANIFIE  → EN_COURS  quand dateDebut = aujourd'hui ET heureDebut <= maintenant
 *   EN_COURS  → TERMINE   quand dateFin < aujourd'hui
 *                      OU (dateFin = aujourd'hui ET heureFin <= maintenant)
 *
 * Les événements ANNULES sont ignorés.
 */
@Component
public class EvenementScheduler {

  @Autowired
  private EvenementRepository evenementRepository;

  /**
   * Tourne toutes les minutes.
   * Cron : "0 * * * * *" = à la seconde 0 de chaque minute.
   *
   * Pour tester rapidement, changer en "0/10 * * * * *"
   * (toutes les 10 secondes) puis remettre toutes les minutes.
   */
  @Scheduled(cron = "0 * * * * *")
  public void mettreAJourStatuts() {

    LocalDate aujourdHui  = LocalDate.now();
    LocalTime maintenant  = LocalTime.now();

    // Récupérer uniquement les événements actifs (pas ANNULE ni TERMINE)
    List<Evenement> actifs = evenementRepository.findAll()
      .stream()
      .filter(e -> !"ANNULE".equals(e.getStatut())
        && !"TERMINE".equals(e.getStatut()))
      .toList();

    int nbMisAJour = 0;

    for (Evenement ev : actifs) {

      LocalDate dateDebut = ev.getDateDebut();
      LocalDate dateFin   = ev.getDateFin();
      LocalTime heureDebut = ev.getHeureDebut();
      LocalTime heureFin   = ev.getHeureFin();
      String ancienStatut  = ev.getStatut();

      // ── Règle 1 : PLANIFIE → EN_COURS ────────────────
      // Condition : dateDebut = aujourd'hui ET heure >= heureDebut
      //          OU dateDebut < aujourd'hui ET dateFin >= aujourd'hui
      if ("PLANIFIE".equals(ancienStatut)) {
        boolean debutAtteint = estDebutAtteint(dateDebut, heureDebut, aujourdHui, maintenant);
        boolean pasEncoreTermine = !estFinPassee(dateFin, heureFin, aujourdHui, maintenant);

        if (debutAtteint && pasEncoreTermine) {
          ev.setStatut("EN_COURS");
          evenementRepository.save(ev);
          nbMisAJour++;
          System.out.printf("✅ [Scheduler] '%s' → EN_COURS (début: %s %s)%n",
            ev.getTitre(), dateDebut, heureDebut != null ? heureDebut : "");
          continue;
        }
      }

      // ── Règle 2 : EN_COURS → TERMINE ─────────────────
      // Condition : dateFin < aujourd'hui
      //          OU (dateFin = aujourd'hui ET heureFin <= maintenant)
      if ("EN_COURS".equals(ancienStatut) || "PLANIFIE".equals(ancienStatut)) {
        boolean finPassee = estFinPassee(dateFin, heureFin, aujourdHui, maintenant);

        if (finPassee) {
          ev.setStatut("TERMINE");
          evenementRepository.save(ev);
          nbMisAJour++;
          System.out.printf("🏁 [Scheduler] '%s' → TERMINE (fin: %s %s)%n",
            ev.getTitre(), dateFin, heureFin != null ? heureFin : "");
        }
      }
    }

    if (nbMisAJour > 0) {
      System.out.printf("[Scheduler] %d événement(s) mis à jour à %s%n",
        nbMisAJour, maintenant.withSecond(0).withNano(0));
    }
  }

  // ══════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════

  /**
   * Vérifie si le début d'un événement est atteint.
   *
   * Cas 1 : dateDebut < aujourd'hui → déjà commencé depuis un autre jour
   * Cas 2 : dateDebut = aujourd'hui ET (heureDebut null OU heureDebut <= maintenant)
   */
  private boolean estDebutAtteint(LocalDate dateDebut, LocalTime heureDebut,
                                  LocalDate aujourdHui, LocalTime maintenant) {
    if (dateDebut.isBefore(aujourdHui)) {
      return true;
    }
    if (dateDebut.isEqual(aujourdHui)) {
      // Si pas d'heure de début → démarre dès le début de la journée
      return heureDebut == null || !maintenant.isBefore(heureDebut);
    }
    return false;
  }

  /**
   * Vérifie si la fin d'un événement est passée.
   *
   * Cas 1 : dateFin < aujourd'hui → terminé depuis un autre jour
   * Cas 2 : dateFin = aujourd'hui ET (heureFin null OU heureFin <= maintenant)
   */
  private boolean estFinPassee(LocalDate dateFin, LocalTime heureFin,
                               LocalDate aujourdHui, LocalTime maintenant) {
    if (dateFin.isBefore(aujourdHui)) {
      return true;
    }
    if (dateFin.isEqual(aujourdHui)) {
      // Si pas d'heure de fin → se termine à la fin de la journée
      return heureFin != null && !maintenant.isBefore(heureFin);
    }
    return false;
  }
}
