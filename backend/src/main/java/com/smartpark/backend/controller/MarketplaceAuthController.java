package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Utilisateur;
import com.smartpark.backend.service.UtilisateurService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/marketplace")
@CrossOrigin(origins = "http://localhost:4200")
public class MarketplaceAuthController {

    private final UtilisateurService utilisateurService;

    public MarketplaceAuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public Utilisateur login(@RequestBody Map<String, Object> request) {
        String identifiant = request.get("username") != null
            ? request.get("username").toString()
            : (request.get("identifiant") != null ? request.get("identifiant").toString() : null);

        String motDePasse = request.get("motDePasse") != null
            ? request.get("motDePasse").toString()
            : (request.get("password") != null ? request.get("password").toString() : null);

        if (identifiant == null || identifiant.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiants requis");
        }

        return utilisateurService.authenticateFlexible(identifiant, motDePasse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur register(@RequestBody Utilisateur utilisateur) {
        if (utilisateur.getUsername() == null || utilisateur.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username requis");
        }

        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe requis");
        }

        if (utilisateurService.findByUsername(utilisateur.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username deja utilise");
        }

        if (utilisateur.getRole() == null || utilisateur.getRole().isBlank()) {
            utilisateur.setRole("CLIENT");
        }

        if (utilisateur.getDateInscription() == null) {
            utilisateur.setDateInscription(LocalDate.now());
        }

        return utilisateurService.create(utilisateur);
    }
}
