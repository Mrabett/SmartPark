package com.smartpark.backend.service;

import com.smartpark.backend.model.Reservation;
import com.smartpark.backend.repository.ReservationRepository;
import com.smartpark.backend.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationSchedulerService {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired private FideliteService  fideliteService;
    @Autowired private UserRepository   userRepository;
    @Autowired private EmailService     emailService;

    // ════════════════════════════════════════════
    // ✅ Toutes les 10 minutes — réservations
    // terminées → points fidélité
    // ════════════════════════════════════════════
    @Scheduled(fixedDelay = 600000) // 10 minutes
    public void distribuerPointsReservationsTerminees() {
                try {

        List<Reservation> reservations =
                reservationRepository.findAll()
                        .stream()
                        .filter(r ->
                                "CONFIRMEE".equals(r.getStatut())
                                        && !r.isPointsDistribues()
                        )
                        .toList();

        for (Reservation r : reservations) {
            try {
                if (reservationEstTerminee(r)) {
                    System.out.println(
                            "✅ Réservation terminée : "
                                    + r.getTerrainNom()
                                    + " pour " + r.getClientEmail());

                    // ✅ Points fidélité post-match
                    String nom = getNomFromEmail(
                            r.getClientEmail());

                    fideliteService
                            .ajouterPointsReservation(
                                    r.getClientEmail(),
                                    nom,
                                    r.getMontantTotal(),
                                    r.getTerrainNom()
                                            + " (post-match)");

                    // Marquer comme distribué
                    r.setPointsDistribues(true);
                    reservationRepository.save(r);

                    System.out.println(
                            "  ✅ Points distribués à "
                                    + r.getClientEmail());
                }
            } catch (Exception e) {
                System.err.println(
                        "❌ Erreur réservation "
                                + r.getId() + ": "
                                + e.getMessage());
            }
        }
                } catch (DataAccessException ex) {
                        System.err.println("⚠️ Scheduler réservations: Mongo indisponible, cycle ignoré.");
                }
    }

    private boolean reservationEstTerminee(
            Reservation r) {
        try {
            LocalDateTime finReservation =
                    LocalDateTime.of(
                            r.getDateReservation(),
                            r.getHeureFin());
            return LocalDateTime.now()
                    .isAfter(finReservation);
        } catch (Exception e) {
            return false;
        }
    }

    private String getNomFromEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getNom() != null
                        ? u.getNom()
                        : email.split("@")[0])
                .orElse(email.split("@")[0]);
    }
}