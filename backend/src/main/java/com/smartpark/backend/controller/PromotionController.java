package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Promotion;
import com.smartpark.backend.service.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<Promotion> findAll() {
        return promotionService.findAll();
    }

    @GetMapping("/{id}")
    public Promotion findById(@PathVariable String id) {
        Promotion promotion = promotionService.findById(id);
        if (promotion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion non trouvée");
        }
        return promotion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion create(@RequestBody Promotion promotion) {
        try {
            return promotionService.create(promotion);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Promotion update(@PathVariable String id, @RequestBody Promotion promotion) {
        Promotion existing = promotionService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion non trouvée");
        }

        existing.setTitre(promotion.getTitre());
        existing.setDescription(promotion.getDescription());
        existing.setPourcentageReduction(promotion.getPourcentageReduction());
        existing.setDateDebut(promotion.getDateDebut());
        existing.setDateFin(promotion.getDateFin());
        existing.setHeureDebut(promotion.getHeureDebut());
        existing.setHeureFin(promotion.getHeureFin());
        existing.setActive(promotion.isActive());
        existing.setProduitIds(promotion.getProduitIds());

        try {
            return promotionService.update(existing);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Promotion existing = promotionService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion non trouvée");
        }
        promotionService.delete(id);
    }
}

