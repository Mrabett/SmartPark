package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Commande;
import com.smartpark.backend.model.StatutCommande;

import java.util.List;

public interface CommandeRepository extends MongoRepository<Commande, String> {
    // Trouver les commandes d'un client
    List<Commande> findByUtilisateurId(String utilisateurId);

    // Trouver les commandes d'un client par statuts
    List<Commande> findByUtilisateurIdAndStatutIn(String utilisateurId, List<StatutCommande> statuts);

    // Trouver par statut
    List<Commande> findByStatut(StatutCommande statut);

    List<Commande> findByLivraisonDemandeeTrue();
}

