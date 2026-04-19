package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.PointsFidelite;

import java.util.Optional;

public interface PointsFideliteRepository extends MongoRepository<PointsFidelite, String> {
    Optional<PointsFidelite> findByUtilisateurId(String utilisateurId);
    Optional<PointsFidelite> findFirstByUtilisateurId(String utilisateurId);
}

