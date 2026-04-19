package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "stocks")
@Data
@NoArgsConstructor
public class Stock {

    @Id
    private String id;

    private int quantiteDisponible;
    private int quantiteMin;
    private int quantiteMax;
    private LocalDate dernierReapprovisionnement;

    private String produitId;   // référence au produit

    public boolean estEnRupture() {
        return quantiteDisponible <= quantiteMin;
    }
}

