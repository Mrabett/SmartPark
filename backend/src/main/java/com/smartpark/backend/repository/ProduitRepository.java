package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Produit;

import java.time.LocalDate;
import java.util.List;

public interface ProduitRepository extends MongoRepository<Produit, String> {
	List<Produit> findByCategorieIn(List<String> categories);
	List<Produit> findByDateExpirationBetweenOrderByDateExpirationAsc(LocalDate startDate, LocalDate endDate);
}

