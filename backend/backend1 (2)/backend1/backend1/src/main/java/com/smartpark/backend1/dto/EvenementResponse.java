package com.smartpark.backend1.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementResponse {

    private String id;
    private String titre;
    private String type;
    private String description;
    private String lieu;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    private int    capaciteMax;
    private double prixBillet;
    private String statut;
    private String organisateur;
    private List<String> tags;

    // Statistiques
    private int    nbInscrits;
    private double revenus;
    private int    placesRestantes;   // calculé : capaciteMax - nbInscrits
    private double tauxOccupation;    // calculé : (nbInscrits / capaciteMax) * 100

    // Placement
    private boolean avecPlacement;
    private int    nbPlacesZoneA;
    private int    nbPlacesZoneB;
    private int    nbPlacesZoneC;
    private double prixZoneA;
    private double prixZoneB;
    private double prixZoneC;
}