package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.HistoriquePoints;
import com.smartpark.backend.service.HistoriquePointsService;

import java.util.List;

@RestController
@RequestMapping("/api/historique-points")
public class HistoriquePointsController {

    private final HistoriquePointsService historiquePointsService;

    public HistoriquePointsController(HistoriquePointsService historiquePointsService) {
        this.historiquePointsService = historiquePointsService;
    }

    @GetMapping
    public List<HistoriquePoints> findAll() {
        return historiquePointsService.findAll();
    }

    @GetMapping("/{id}")
    public HistoriquePoints findById(@PathVariable String id) {
        HistoriquePoints historiquePoints = historiquePointsService.findById(id);
        if (historiquePoints == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Historique points non trouvé");
        }
        return historiquePoints;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoriquePoints create(@RequestBody HistoriquePoints historiquePoints) {
        return historiquePointsService.create(historiquePoints);
    }

    @PutMapping("/{id}")
    public HistoriquePoints update(@PathVariable String id, @RequestBody HistoriquePoints historiquePoints) {
        HistoriquePoints existing = historiquePointsService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Historique points non trouvé");
        }

        existing.setPointsGagnes(historiquePoints.getPointsGagnes());
        existing.setPointsUtilises(historiquePoints.getPointsUtilises());
        existing.setMotif(historiquePoints.getMotif());
        existing.setDate(historiquePoints.getDate());
        existing.setClientId(historiquePoints.getClientId());
        existing.setCommandeId(historiquePoints.getCommandeId());

        return historiquePointsService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        HistoriquePoints existing = historiquePointsService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Historique points non trouvé");
        }
        historiquePointsService.delete(id);
    }
}
