package com.smartpark.backend.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "commandes")
@Data
@NoArgsConstructor
public class Commande {

    @Id
    private String id;

    private String numeroCommande;

    private LocalDateTime dateCommande;
    private double montantTotal;
    private StatutCommande statut;

    private String utilisateurId;    // référence au client
    private String utilisateurNom;
    private String utilisateurPrenom;

    private boolean livraisonDemandee;
    private String lieuLivraison;
    private StatutLivraison statutLivraison;
    private String agentLivraisonId;
    private String agentLivraisonNom;
    private LocalDateTime dateAffectationLivraison;
    private LocalDateTime dateLivraison;

    // Avec MongoDB on peut embarquer les lignes directement
    private List<LigneCommande> lignes;

    public double calculerTotal() {
        if (lignes == null) return 0;
        return lignes.stream()
                .mapToDouble(LigneCommande::getSousTotal)
                .sum();
    }
}
