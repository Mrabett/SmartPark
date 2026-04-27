package com.smartpark.backend1.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class EvenementUpdateRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String titre;

    @NotBlank(message = "Le type est obligatoire")
    @Pattern(regexp = "TOURNOI|ATELIER|CONCERT|AUTRE",
            message = "Le type doit être : TOURNOI, ATELIER, CONCERT ou AUTRE")
    private String type;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    private LocalTime heureDebut;
    private LocalTime heureFin;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être d'au moins 1")
    private Integer capaciteMax;

    @NotNull(message = "Le prix du billet est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    private Double prixBillet;

    @Pattern(regexp = "PLANIFIE|EN_COURS|TERMINE|ANNULE",
            message = "Le statut doit être : PLANIFIE, EN_COURS, TERMINE ou ANNULE")
    private String statut;

    private String organisateur;
    private List<String> tags;

    // ── Placement ─────────────────────────────────────────────
    private boolean avecPlacement;

    @Min(value = 0)
    private int nbPlacesZoneA;

    @Min(value = 0)
    private int nbPlacesZoneB;

    @Min(value = 0)
    private int nbPlacesZoneC;

    @DecimalMin(value = "0.0")
    private double prixZoneA;

    @DecimalMin(value = "0.0")
    private double prixZoneB;

    @DecimalMin(value = "0.0")
    private double prixZoneC;
}