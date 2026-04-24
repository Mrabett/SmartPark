package com.smartpark.backend.repository;

import com.smartpark.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProduitIdOrderByDateCreationDesc(String produitId);
    Optional<Review> findByProduitIdAndUtilisateurId(String produitId, String utilisateurId);
    long countByProduitId(String produitId);
    long countByProduitIdAndNote(String produitId, int note);
}
