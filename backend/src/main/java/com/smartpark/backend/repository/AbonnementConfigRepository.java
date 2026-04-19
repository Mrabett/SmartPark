package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.AbonnementConfig;

import java.util.List;
import java.util.Optional;

public interface AbonnementConfigRepository extends MongoRepository<AbonnementConfig, String> {
    Optional<AbonnementConfig> findByCode(String code);
    List<AbonnementConfig> findByActifTrue();
}

