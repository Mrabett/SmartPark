package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

// Pas de @Document ici — embarquée dans Commande
@Data
@NoArgsConstructor
public class LigneCommande {

    private String produitId;
    private String nomProduit;   // copie du nom pour l'historique
    private String image;
    private int quantite;
    private double prixUnitaire;
    private double sousTotal;

    public void calculerSousTotal() {
        this.sousTotal = this.quantite * this.prixUnitaire;
    }
}
