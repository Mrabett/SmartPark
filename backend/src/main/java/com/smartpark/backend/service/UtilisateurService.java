package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.Utilisateur;
import com.smartpark.backend.repository.UtilisateurRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(String id) {
        return utilisateurRepository.findById(id).orElse(null);
    }

    public Utilisateur create(Utilisateur utilisateur) {
        utilisateur.setId(null);
        return utilisateurRepository.save(utilisateur);
    }

    public Optional<Utilisateur> authenticate(String username, String motDePasse) {
        return utilisateurRepository.findByUsernameAndMotDePasse(username, motDePasse);
    }

    public Optional<Utilisateur> authenticateFlexible(String identifiant, String motDePasse) {
        Optional<Utilisateur> byUsername = utilisateurRepository.findByUsernameIgnoreCaseAndMotDePasse(identifiant, motDePasse);
        if (byUsername.isPresent()) {
            return byUsername;
        }

        Optional<Utilisateur> byEmail = utilisateurRepository.findByEmailIgnoreCaseAndMotDePasse(identifiant, motDePasse);
        if (byEmail.isPresent()) {
            return byEmail;
        }

        return utilisateurRepository.findByNumClientAndMotDePasse(identifiant, motDePasse);
    }

    public Optional<Utilisateur> findByUsername(String username) {
        return utilisateurRepository.findByUsername(username);
    }

    public Utilisateur update(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    public void delete(String id) {
        utilisateurRepository.deleteById(id);
    }
}

