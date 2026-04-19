package com.smartpark.backend.service;

import com.smartpark.backend.dto.ProduitExpirationAlertDTO;
import org.springframework.stereotype.Service;
import com.smartpark.backend.model.Produit;
import com.smartpark.backend.repository.ProduitRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public List<Produit> findAll() {
        return produitRepository.findAll();
    }

    public Produit findById(String id) {
        return produitRepository.findById(id).orElse(null);
    }

    public Produit create(Produit produit) {
        produit.setId(null);
        if (produit.getDateCreation() == null) {
            produit.setDateCreation(LocalDateTime.now());
        }
        return produitRepository.save(produit);
    }

    public Produit update(Produit produit) {
        return produitRepository.save(produit);
    }

    public List<ProduitExpirationAlertDTO> findExpirationAlerts(int daysWindow) {
        int safeWindow = Math.max(daysWindow, 0);
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(safeWindow);

        return produitRepository
                .findByDateExpirationBetweenOrderByDateExpirationAsc(today, deadline)
                .stream()
                .map(produit -> new ProduitExpirationAlertDTO(
                        produit.getId(),
                        produit.getNom(),
                        produit.getCategorie(),
                        produit.getDateExpiration(),
                        ChronoUnit.DAYS.between(today, produit.getDateExpiration()),
                        produit.getStock() != null ? produit.getStock().getQuantiteDisponible() : null
                ))
                .collect(Collectors.toList());
    }

    public Produit decreaseStock(String produitId, int quantite) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + produitId));

        if (produit.getStock() == null) {
            throw new RuntimeException("Stock introuvable pour le produit: " + produit.getNom());
        }

        if (produit.getStock().getQuantiteDisponible() < quantite) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + produit.getNom());
        }

        produit.getStock().setQuantiteDisponible(produit.getStock().getQuantiteDisponible() - quantite);
        return produitRepository.save(produit);
    }

    public void delete(String id) {
        produitRepository.deleteById(id);
    }
}

