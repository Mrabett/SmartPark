package com.smartpark.backend.controller;

import com.smartpark.backend.dto.ReviewStatsDTO;
import com.smartpark.backend.dto.ReviewInsightDTO;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.model.Review;
import com.smartpark.backend.service.ProduitService;
import com.smartpark.backend.service.ReviewInsightService;
import com.smartpark.backend.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final ProduitService produitService;
    private final ReviewInsightService reviewInsightService;

    public ReviewController(ReviewService reviewService, ProduitService produitService, ReviewInsightService reviewInsightService) {
        this.reviewService = reviewService;
        this.produitService = produitService;
        this.reviewInsightService = reviewInsightService;
    }

    @GetMapping("/produits/{produitId}/reviews")
    public List<Review> getProduitReviews(@PathVariable String produitId) {
        return reviewService.findByProduitId(produitId);
    }

    @GetMapping("/produits/{produitId}/reviews/stats")
    public ReviewStatsDTO getProduitReviewStats(@PathVariable String produitId) {
        return reviewService.getStatsByProduitId(produitId);
    }

    @GetMapping("/produits/{produitId}/reviews/insights")
    public ReviewInsightDTO getProduitReviewInsights(@PathVariable String produitId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé aux admins");
        }

        Produit produit = produitService.findById(produitId);
        if (produit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }

        try {
            return reviewInsightService.generateInsight(produitId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/produits/{produitId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public Review createOrUpdateReview(@PathVariable String produitId, @RequestBody Map<String, Object> payload) {
        Produit produit = produitService.findById(produitId);
        if (produit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }

        String utilisateurId = payload.get("utilisateurId") != null ? payload.get("utilisateurId").toString() : "";
        String utilisateurNom = payload.get("utilisateurNom") != null ? payload.get("utilisateurNom").toString() : "Client";
        String commentaire = payload.get("commentaire") != null ? payload.get("commentaire").toString() : "";

        int note;
        try {
            note = Integer.parseInt(payload.get("note").toString());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note est invalide");
        }

        try {
            return reviewService.createOrUpdate(produitId, utilisateurId, utilisateurNom, note, commentaire);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable String reviewId,
            @RequestParam(name = "utilisateurId", required = false) String utilisateurId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        try {
            reviewService.deleteReview(reviewId, utilisateurId, isAdmin);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
