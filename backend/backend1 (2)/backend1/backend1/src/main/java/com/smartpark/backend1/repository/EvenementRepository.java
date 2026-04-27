package com.smartpark.backend1.repository;

import com.smartpark.backend1.model.Evenement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvenementRepository extends MongoRepository<Evenement, String> {

    List<Evenement> findByType(String type);
    List<Evenement> findByStatut(String statut);
    List<Evenement> findByDateDebutBetween(LocalDate debut, LocalDate fin);
    List<Evenement> findByOrganisateur(String organisateur);

    // Chercher tous les événements d'un lieu donné (exact, case-insensitive géré en Java)
    List<Evenement> findByLieuIgnoreCase(String lieu);
}