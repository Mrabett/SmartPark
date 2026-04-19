package com.smartpark.backend.controller;

import com.smartpark.backend.model.Objectif;
import com.smartpark.backend.service.ObjectifService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/objectifs")
public class ObjectifController {

    private final ObjectifService objectifService;

    public ObjectifController(ObjectifService objectifService) {
        this.objectifService = objectifService;
    }

    @GetMapping
    public List<Objectif> findAll() {
        return objectifService.findAll();
    }

    @GetMapping("/actifs")
    public List<Objectif> findActifs() {
        return objectifService.findActifs();
    }

    @GetMapping("/{id}")
    public Objectif findById(@PathVariable String id) {
        Objectif objectif = objectifService.findById(id);
        if (objectif == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objectif non trouvé");
        }
        return objectif;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Objectif create(@RequestBody Objectif objectif, Authentication authentication) {
        ensureAdmin(authentication);
        return objectifService.create(objectif);
    }

    @PutMapping("/{id}")
    public Objectif update(@PathVariable String id, @RequestBody Objectif objectif, Authentication authentication) {
        ensureAdmin(authentication);

        Objectif existing = objectifService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objectif non trouvé");
        }

        existing.setTitre(objectif.getTitre());
        existing.setDescription(objectif.getDescription());
        existing.setIcon(objectif.getIcon());
        existing.setActif(objectif.isActif());
        existing.setProduitIds(objectif.getProduitIds());

        return objectifService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, Authentication authentication) {
        ensureAdmin(authentication);

        Objectif existing = objectifService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objectif non trouvé");
        }

        objectifService.delete(id);
    }

    private void ensureAdmin(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès admin requis");
        }
    }
}
