package com.smartpark.backend1.repository;

import com.smartpark.backend1.model.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends MongoRepository<Photo, String> {

  // Tous les posts d'un événement (du plus récent au plus ancien)
  List<Photo> findByEvenementIdOrderByDatePostDesc(String evenementId);

  // Posts d'un auteur spécifique
  List<Photo> findByAuteurEmailOrderByDatePostDesc(String auteurEmail);

  // Tous les posts (feed global)
  List<Photo> findAllByOrderByDatePostDesc();
}
