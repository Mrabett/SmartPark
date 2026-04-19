package com.smartpark.backend.service;

import com.smartpark.backend.model.Objectif;
import com.smartpark.backend.repository.ObjectifRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ObjectifService {

    private final ObjectifRepository objectifRepository;

    public ObjectifService(ObjectifRepository objectifRepository) {
        this.objectifRepository = objectifRepository;
    }

    public List<Objectif> findAll() {
        return objectifRepository.findAll();
    }

    public List<Objectif> findActifs() {
        return objectifRepository.findByActifTrueOrderByDateCreationDesc();
    }

    public Objectif findById(String id) {
        return objectifRepository.findById(id).orElse(null);
    }

    public Objectif create(Objectif objectif) {
        objectif.setId(null);
        objectif.setDateCreation(LocalDateTime.now());
        normalize(objectif);
        return objectifRepository.save(objectif);
    }

    public Objectif update(Objectif objectif) {
        normalize(objectif);
        return objectifRepository.save(objectif);
    }

    public void delete(String id) {
        objectifRepository.deleteById(id);
    }

    private void normalize(Objectif objectif) {
        if (objectif.getProduitIds() == null) {
            objectif.setProduitIds(Collections.emptyList());
        }

        if (objectif.getTitre() != null) {
            objectif.setTitre(objectif.getTitre().trim());
        }

        if (objectif.getDescription() != null) {
            objectif.setDescription(objectif.getDescription().trim());
        }

        if (objectif.getIcon() == null || objectif.getIcon().isBlank()) {
            objectif.setIcon("🎯");
        } else {
            objectif.setIcon(objectif.getIcon().trim());
        }
    }
}
