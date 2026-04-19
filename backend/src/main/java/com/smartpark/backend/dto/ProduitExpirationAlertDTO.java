package com.smartpark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ProduitExpirationAlertDTO {
    private String produitId;
    private String nomProduit;
    private String categorie;
    private LocalDate dateExpiration;
    private long joursRestants;
    private Integer quantiteDisponible;
}
