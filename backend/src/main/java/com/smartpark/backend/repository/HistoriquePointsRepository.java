package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.HistoriquePoints;

import java.util.List;

public interface HistoriquePointsRepository extends MongoRepository<HistoriquePoints, String> {
    List<HistoriquePoints> findByClientId(String clientId);
    List<HistoriquePoints> findByCommandeId(String commandeId);
    boolean existsByCommandeId(String commandeId);
}
