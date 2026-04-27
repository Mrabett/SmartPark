package com.smartpark.backend1.controller;

import com.smartpark.backend1.dto.EvenementCreateRequest;
import com.smartpark.backend1.dto.EvenementUpdateRequest;
import com.smartpark.backend1.dto.EvenementResponse;
import com.smartpark.backend1.service.EvenementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evenements")
@CrossOrigin(origins = "http://localhost:4200")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @GetMapping
    public List<EvenementResponse> getAll() {
        return evenementService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponse> getById(@PathVariable String id) {
        return evenementService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EvenementCreateRequest request) {
        try {
            return ResponseEntity.ok(evenementService.create(request));
        } catch (RuntimeException e) {
            // Retourne 400 avec le message d'erreur lisible (conflit lieu etc.)
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @Valid @RequestBody EvenementUpdateRequest request) {
        try {
            return ResponseEntity.ok(evenementService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        evenementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public List<EvenementResponse> getByType(@PathVariable String type) {
        return evenementService.getByType(type);
    }

    @GetMapping("/statut/{statut}")
    public List<EvenementResponse> getByStatut(@PathVariable String statut) {
        return evenementService.getByStatut(statut);
    }

    @GetMapping("/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        return evenementService.getDashboardStats();
    }
}