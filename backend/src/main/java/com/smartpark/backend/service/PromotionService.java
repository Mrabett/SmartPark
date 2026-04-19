package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.Promotion;
import com.smartpark.backend.repository.PromotionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    public Promotion findById(String id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public Promotion create(Promotion promotion) {
        promotion.setId(null);
        validatePromotion(promotion);
        return promotionRepository.save(promotion);
    }

    public Promotion update(Promotion promotion) {
        validatePromotion(promotion);
        return promotionRepository.save(promotion);
    }

    public void delete(String id) {
        promotionRepository.deleteById(id);
    }

    public double getActiveReductionForProduit(String produitId) {
        if (produitId == null || produitId.isBlank()) {
            return 0;
        }

        return findAll().stream()
                .filter(Objects::nonNull)
                .filter(Promotion::estActive)
                .filter(promotion -> promotion.getProduitIds() != null && promotion.getProduitIds().contains(produitId))
                .mapToDouble(Promotion::getPourcentageReduction)
                .max()
                .orElse(0);
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion.getDateDebut() == null || promotion.getDateFin() == null) {
            throw new RuntimeException("Dates de promotion obligatoires");
        }

        if (promotion.getDateDebut().isBefore(LocalDate.now())) {
            throw new RuntimeException("La date de début ne peut pas être dans le passé");
        }

        if (promotion.getDateFin().isBefore(promotion.getDateDebut())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        if (promotion.getHeureDebut() != null && !promotion.getHeureDebut().isBlank()) {
            LocalTime.parse(promotion.getHeureDebut());
        }

        if (promotion.getHeureFin() != null && !promotion.getHeureFin().isBlank()) {
            LocalTime.parse(promotion.getHeureFin());
        }

        if (promotion.getDateDebut().isEqual(promotion.getDateFin())
                && promotion.getHeureDebut() != null && !promotion.getHeureDebut().isBlank()
                && promotion.getHeureFin() != null && !promotion.getHeureFin().isBlank()) {
            LocalTime heureDebut = LocalTime.parse(promotion.getHeureDebut());
            LocalTime heureFin = LocalTime.parse(promotion.getHeureFin());
            if (!heureFin.isAfter(heureDebut)) {
                throw new RuntimeException("L'heure de fin doit être après l'heure de début");
            }
        }
    }
}

