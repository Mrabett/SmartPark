package com.smartpark.backend.service;

import com.smartpark.backend.dto.ReviewStatsDTO;
import com.smartpark.backend.model.Commande;
import com.smartpark.backend.model.Review;
import com.smartpark.backend.model.StatutCommande;
import com.smartpark.backend.repository.CommandeRepository;
import com.smartpark.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CommandeRepository commandeRepository;

    public ReviewService(ReviewRepository reviewRepository, CommandeRepository commandeRepository) {
        this.reviewRepository = reviewRepository;
        this.commandeRepository = commandeRepository;
    }

    public List<Review> findByProduitId(String produitId) {
        return reviewRepository.findByProduitIdOrderByDateCreationDesc(produitId);
    }

    public Review createOrUpdate(String produitId, String utilisateurId, String utilisateurNom, int note, String commentaire) {
        if (produitId == null || produitId.isBlank()) {
            throw new IllegalArgumentException("Produit invalide");
        }
        if (utilisateurId == null || utilisateurId.isBlank()) {
            throw new IllegalArgumentException("Utilisateur invalide");
        }
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }

        if (!hasPurchasedProduct(utilisateurId, produitId)) {
            throw new IllegalStateException("Seuls les clients ayant acheté ce produit peuvent laisser un avis.");
        }

        String cleanComment = commentaire == null ? "" : commentaire.trim();
        String displayName = (utilisateurNom == null || utilisateurNom.isBlank()) ? "Client" : utilisateurNom.trim();

        Review review = reviewRepository
                .findByProduitIdAndUtilisateurId(produitId, utilisateurId)
                .orElseGet(Review::new);

        if (review.getId() == null) {
            review.setProduitId(produitId);
            review.setUtilisateurId(utilisateurId);
            review.setDateCreation(LocalDateTime.now());
        }

        review.setUtilisateurNom(displayName);
        review.setNote(note);
        review.setCommentaire(cleanComment);
        review.setDateMiseAJour(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    private boolean hasPurchasedProduct(String utilisateurId, String produitId) {
        List<Commande> commandes = commandeRepository.findByUtilisateurIdAndStatutIn(
                utilisateurId,
                List.of(StatutCommande.PAYEE, StatutCommande.LIVREE)
        );

        return commandes.stream().anyMatch(c -> c.getLignes() != null && c.getLignes().stream()
                .anyMatch(l -> produitId.equals(l.getProduitId())));
    }

    public void deleteReview(String reviewId, String utilisateurId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review introuvable"));

        if (!isAdmin && (utilisateurId == null || !utilisateurId.equals(review.getUtilisateurId()))) {
            throw new IllegalArgumentException("Suppression non autorisée");
        }

        reviewRepository.deleteById(reviewId);
    }

    public ReviewStatsDTO getStatsByProduitId(String produitId) {
        List<Review> reviews = reviewRepository.findByProduitIdOrderByDateCreationDesc(produitId);

        long total = reviews.size();
        double average = 0.0;
        if (total > 0) {
            int sum = reviews.stream().mapToInt(Review::getNote).sum();
            average = Math.round((sum * 100.0 / total)) / 100.0;
        }

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribution.put(i, reviewRepository.countByProduitIdAndNote(produitId, i));
        }

        return new ReviewStatsDTO(average, total, distribution);
    }
}
