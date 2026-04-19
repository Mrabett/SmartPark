package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Utilisateur;
import com.smartpark.backend.service.UtilisateurService;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public List<Utilisateur> findAll() {
        return utilisateurService.findAll();
    }

    @GetMapping("/{id}")
    public Utilisateur findById(@PathVariable String id) {
        Utilisateur utilisateur = utilisateurService.findById(id);
        if (utilisateur == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        return utilisateur;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur create(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.create(utilisateur);
    }

    @PutMapping("/{id}")
    public Utilisateur update(@PathVariable String id, @RequestBody Utilisateur utilisateur) {
        Utilisateur existing = utilisateurService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }

        existing.setNom(utilisateur.getNom());
        existing.setPrenom(utilisateur.getPrenom());
        existing.setEmail(utilisateur.getEmail());
        existing.setTelephone(utilisateur.getTelephone());
        existing.setUsername(utilisateur.getUsername());
        existing.setMotDePasse(utilisateur.getMotDePasse());
        existing.setRole(utilisateur.getRole());
        existing.setNumClient(utilisateur.getNumClient());
        existing.setDateInscription(utilisateur.getDateInscription());

        return utilisateurService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Utilisateur existing = utilisateurService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        utilisateurService.delete(id);
    }
}

