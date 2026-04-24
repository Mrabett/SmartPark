package com.smartpark.backend.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "points_fidelite")
@Data
@NoArgsConstructor
public class PointsFidelite {

    @Id
    private String id;

    private int pointsTotal;
    private int pointsUtilises;
    private int pointsDisponibles;
    private NiveauFidelite niveau;
    private String abonnementActifCodeProchaineCommande;
    private String abonnementActifLabelProchaineCommande;
    private double abonnementActifReductionProchaineCommande;
    private LocalDateTime dateDerniereMiseAJour;

    @Indexed(unique = true)
    private String utilisateurId;   // référence au client

    public void calculerNiveau() {
        if (this.pointsTotal >= 25) {
            this.niveau = NiveauFidelite.PLATINUM;
        } else if (this.pointsTotal >= 20) {
            this.niveau = NiveauFidelite.GOLD;
        } else if (this.pointsTotal >= 15) {
            this.niveau = NiveauFidelite.SILVER;
        } else {
            this.niveau = NiveauFidelite.BRONZE;
        }
    }

    public void ajouterPoints(int pointsGagnes) {
        this.pointsTotal += pointsGagnes;
        this.pointsDisponibles += pointsGagnes;
        this.dateDerniereMiseAJour = LocalDateTime.now();
        this.calculerNiveau();
    }

    public boolean utiliserPoints(int pointsAUtiliser) {
        if (this.pointsDisponibles >= pointsAUtiliser) {
            this.pointsDisponibles -= pointsAUtiliser;
            this.pointsUtilises += pointsAUtiliser;
            this.dateDerniereMiseAJour = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public double getPourcentageReduction() {
        if (this.abonnementActifReductionProchaineCommande <= 0) {
            return 0.0;
        }
        return this.abonnementActifReductionProchaineCommande;
    }
}
