package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.HistoriquePoints;
import com.smartpark.backend.repository.HistoriquePointsRepository;

import java.util.List;

@Service
public class HistoriquePointsService {

    private final HistoriquePointsRepository historiquePointsRepository;

    public HistoriquePointsService(HistoriquePointsRepository historiquePointsRepository) {
        this.historiquePointsRepository = historiquePointsRepository;
    }

    public List<HistoriquePoints> findAll() {
        return historiquePointsRepository.findAll();
    }

    public HistoriquePoints findById(String id) {
        return historiquePointsRepository.findById(id).orElse(null);
    }

    public HistoriquePoints create(HistoriquePoints historiquePoints) {
        historiquePoints.setId(null);
        return historiquePointsRepository.save(historiquePoints);
    }

    public HistoriquePoints update(HistoriquePoints historiquePoints) {
        return historiquePointsRepository.save(historiquePoints);
    }

    public void delete(String id) {
        historiquePointsRepository.deleteById(id);
    }
}

