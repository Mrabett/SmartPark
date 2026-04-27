package com.smartpark.backend1.repository;

import com.smartpark.backend1.model.UserPoints;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointsRepository extends MongoRepository<UserPoints, String> {
  Optional<UserPoints> findByUserEmail(String userEmail);
}
