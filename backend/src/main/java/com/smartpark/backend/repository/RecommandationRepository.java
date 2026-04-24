package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Recommandation;
import java.util.List;

public interface RecommandationRepository extends MongoRepository<Recommandation, String> {

    // ✅ Trouver par utilisateurId
    List<Recommandation> findByUtilisateurId(String utilisateurId);

    // ✅ Trouver par utilisateurId trié par score
    List<Recommandation> findByUtilisateurIdOrderByScoreDesc(String utilisateurId);
}
