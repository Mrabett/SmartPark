package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Stock;

public interface StockRepository extends MongoRepository<Stock, String> {
}

