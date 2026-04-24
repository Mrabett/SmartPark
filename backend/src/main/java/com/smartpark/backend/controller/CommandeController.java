package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Commande;
import com.smartpark.backend.model.StatutCommande;
import com.smartpark.backend.model.StatutLivraison;
import com.smartpark.backend.service.CommandeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "http://localhost:4200")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @GetMapping
    public List<Commande> findAll() {
        return commandeService.findAll();
    }

    @GetMapping("/{id}")
    public Commande findById(@PathVariable String id) {
        Commande commande = commandeService.findById(id);
        if (commande == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande non trouvée");
        }
        return commande;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Commande create(@RequestBody Commande commande) {
        return commandeService.create(commande);
    }

    @PutMapping("/{id}")
    public Commande update(@PathVariable String id, @RequestBody Commande commande) {
        Commande existing = commandeService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande non trouvée");
        }

        existing.setDateCommande(commande.getDateCommande());
        existing.setStatut(commande.getStatut());
        existing.setUtilisateurId(commande.getUtilisateurId());
        existing.setUtilisateurNom(commande.getUtilisateurNom());
        existing.setUtilisateurPrenom(commande.getUtilisateurPrenom());
        existing.setNumeroCommande(commande.getNumeroCommande());
        existing.setLignes(commande.getLignes());
        existing.setMontantTotal(existing.calculerTotal());

        return commandeService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Commande existing = commandeService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande non trouvée");
        }
        commandeService.delete(id);
    }

    // Commandes d'un client spécifique
    @GetMapping("/client/{utilisateurId}")
    public List<Commande> findByClient(@PathVariable String utilisateurId) {
        return commandeService.findByUtilisateurId(utilisateurId);
    }

    // Commandes par statut
    @GetMapping("/statut/{statut}")
    public List<Commande> findByStatut(@PathVariable StatutCommande statut) {
        return commandeService.findByStatut(statut);
    }

    @GetMapping("/livraisons")
    public List<Commande> findLivraisons() {
        return commandeService.findLivraisons();
    }

    @PutMapping("/{id}/livraison/request")
    public Commande requestLivraison(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            return commandeService.requestLivraison(id, payload.getOrDefault("lieuLivraison", ""));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/livraison/assign")
    public Commande assignerLivraison(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            return commandeService.assignerAgentLivraison(
                    id,
                    payload.getOrDefault("agentId", ""),
                    payload.getOrDefault("agentNom", "")
            );
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/livraison/status")
    public Commande updateStatutLivraison(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            StatutLivraison statut = StatutLivraison.valueOf(payload.getOrDefault("statutLivraison", ""));
            return commandeService.updateStatutLivraison(id, statut);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut livraison invalide");
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}

