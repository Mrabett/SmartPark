package com.smartpark.backend.repository;

import com.smartpark.backend.model.Objectif;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ObjectifRepository extends MongoRepository<Objectif, String> {
    List<Objectif> findByActifTrueOrderByDateCreationDesc();
}
