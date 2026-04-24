package com.smartpark.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smartpark.backend.model.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends MongoRepository<Utilisateur, String> {
	Optional<Utilisateur> findByUsername(String username);
	Optional<Utilisateur> findByUsernameAndMotDePasse(String username, String motDePasse);
	Optional<Utilisateur> findByUsernameIgnoreCaseAndMotDePasse(String username, String motDePasse);
	Optional<Utilisateur> findByEmailIgnoreCaseAndMotDePasse(String email, String motDePasse);
	Optional<Utilisateur> findByNumClientAndMotDePasse(String numClient, String motDePasse);
}

