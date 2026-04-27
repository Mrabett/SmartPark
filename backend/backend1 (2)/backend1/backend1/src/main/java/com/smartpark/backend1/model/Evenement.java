package com.smartpark.backend1.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "evenements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evenement {

    @Id
    private String id;

    private String titre;
    private String type;          // TOURNOI, ATELIER, CONCERT, AUTRE
    private String description;
    private String lieu;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    private int capaciteMax;
    private double prixBillet;
    private String statut;        // PLANIFIE, EN_COURS, TERMINE, ANNULE

    private String organisateur;
    private List<String> tags;

    // Statistiques
    private int nbInscrits;
    private double revenus;

    // ── NOUVEAUX CHAMPS PLACEMENT ──────────────────────────────────
    // Indique si l'événement a un plan de salle
    private boolean avecPlacement;   // true = placement activé

    // Configuration des zones (stockée comme liste)
    // Chaque zone a : nom, nbPlaces, prix, categorie
    // Zones par défaut : A(VIP), B(STANDARD), C(ECONOMIQUE)
    private int nbPlacesZoneA;        // Zone A - VIP (devant)
    private int nbPlacesZoneB;        // Zone B - Standard (milieu)
    private int nbPlacesZoneC;        // Zone C - Économique (derrière)

    private double prixZoneA;         // Prix zone A (le plus cher)
    private double prixZoneB;         // Prix zone B
    private double prixZoneC;         // Prix zone C (le moins cher)
}