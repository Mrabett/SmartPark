package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "promotions")
@Data
@NoArgsConstructor
public class Promotion {

    @Id
    private String id;

    private String titre;
    private String description;
    private double pourcentageReduction;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String heureDebut;
    private String heureFin;
    private boolean active;

    private List<String> produitIds;  // ids des produits concernés

    public boolean estActive() {
        if (!active || dateDebut == null || dateFin == null) {
            return false;
        }

        LocalTime startTime = (heureDebut == null || heureDebut.isBlank())
            ? LocalTime.MIN
            : LocalTime.parse(heureDebut);
        LocalTime endTime = (heureFin == null || heureFin.isBlank())
            ? LocalTime.of(23, 59, 59)
            : LocalTime.parse(heureFin);

        LocalDateTime start = dateDebut.atTime(startTime);
        LocalDateTime end = dateFin.atTime(endTime);
        LocalDateTime now = LocalDateTime.now();

        return !now.isBefore(start) && !now.isAfter(end);
    }
}
