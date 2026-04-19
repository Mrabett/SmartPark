package com.smartpark.backend.service;

import com.smartpark.backend.model.Match;
import com.smartpark.backend.repository.MatchRepository;
import com.smartpark.backend.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MatchSchedulerService {

    @Autowired private MatchRepository  matchRepository;
    @Autowired private FideliteService  fideliteService;
    @Autowired private UserRepository   userRepository;
    @Autowired private EmailService     emailService;

    // ════════════════════════════════════════════
    // ✅ Vérifie toutes les 5 minutes
    // les matchs terminés pour distribuer les points
    // ════════════════════════════════════════════
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void distribuerPointsMatchsTermines() {
                try {

        System.out.println(
                "🔄 Scheduler — vérification matchs "
                        + "terminés : "
                        + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm")));

        List<Match> matchsActifs =
                matchRepository.findAll()
                        .stream()
                        .filter(m ->
                                // Pas encore distribué
                                !m.isPointsDistribues()
                                        // Match ouvert ou complet
                                        && ("OUVERT".equals(m.getStatut())
                                        || "COMPLET"
                                        .equals(m.getStatut()))
                                        // A des joueurs
                                        && m.getJoueurs() != null
                                        && !m.getJoueurs().isEmpty()
                        )
                        .toList();

        System.out.println(
                "📋 Matchs à vérifier : "
                        + matchsActifs.size());

        for (Match match : matchsActifs) {
            try {
                if (estTermine(match)) {
                    System.out.println(
                            "✅ Match terminé détecté : "
                                    + match.getTitre()
                                    + " — distribution des points...");

                    distribuerPointsPourMatch(match);
                }
            } catch (Exception e) {
                System.err.println(
                        "❌ Erreur scheduler pour match "
                                + match.getId() + " : "
                                + e.getMessage());
            }
        }
                } catch (DataAccessException ex) {
                        System.err.println("⚠️ Scheduler matchs: Mongo indisponible, cycle ignoré.");
                }
    }

    // ════════════════════════════════════════════
    // ✅ Vérifier si un match est terminé
    // Date + Heure passées
    // ════════════════════════════════════════════
    private boolean estTermine(Match match) {
        try {
            if (match.getDate() == null
                    || match.getHeure() == null) {
                return false;
            }

            // Parser date et heure
            LocalDate dateMatch = LocalDate.parse(
                    match.getDate());
            LocalTime heureMatch = LocalTime.parse(
                    match.getHeure());

            // Supposer durée de 1h30 par défaut
            LocalDateTime finMatch = LocalDateTime.of(
                            dateMatch, heureMatch)
                    .plusHours(1)
                    .plusMinutes(30);

            LocalDateTime maintenant =
                    LocalDateTime.now();

            return maintenant.isAfter(finMatch);

        } catch (Exception e) {
            System.err.println(
                    "⚠️ Erreur parsing date match "
                            + match.getId() + " : "
                            + e.getMessage());
            return false;
        }
    }

    // ════════════════════════════════════════════
    // ✅ Distribuer les points à tous les joueurs
    // ════════════════════════════════════════════
    private void distribuerPointsPourMatch(
            Match match) {

        int joueursRecompenses = 0;

        for (String emailJoueur : match.getJoueurs()) {
            try {
                String nomJoueur =
                        getNomFromEmail(emailJoueur);

                // ✅ +10 points fidélité
                // + incrémente carte 10 matchs
                fideliteService.ajouterPointsMatch(
                        emailJoueur,
                        nomJoueur,
                        match.getTitre());

                joueursRecompenses++;

                System.out.println(
                        "  ✅ +10 pts pour "
                                + emailJoueur);

                // ✅ Email notification points reçus
                try {
                    emailService
                            .envoyerPointsMatchTermine(
                                    emailJoueur,
                                    nomJoueur,
                                    match.getTitre(),
                                    match.getSport(),
                                    match.getDate(),
                                    match.getHeure(),
                                    10);
                } catch (Exception emailEx) {
                    System.err.println(
                            "⚠️ Email ignoré pour "
                                    + emailJoueur + ": "
                                    + emailEx.getMessage());
                }

            } catch (Exception e) {
                System.err.println(
                        "❌ Erreur points pour "
                                + emailJoueur + " : "
                                + e.getMessage());
            }
        }

        // ✅ Marquer le match comme "points distribués"
        // pour éviter de donner les points 2 fois
        match.setPointsDistribues(true);
        match.setStatut("TERMINE");
        matchRepository.save(match);

        System.out.println(
                "🎉 Match '"
                        + match.getTitre()
                        + "' terminé ! Points distribués à "
                        + joueursRecompenses + " joueur(s).");
    }

    private String getNomFromEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getNom() != null
                        && !u.getNom().isBlank()
                        ? u.getNom()
                        : email.split("@")[0])
                .orElse(email.split("@")[0]);
    }
}