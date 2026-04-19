package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Promotion;

public interface PromotionRepository extends MongoRepository<Promotion, String> {
}

