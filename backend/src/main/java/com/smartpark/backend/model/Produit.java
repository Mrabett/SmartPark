package com.smartpark.backend.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "produits")
@Data
@NoArgsConstructor
public class Produit {

    @Id
    private String id;        // String avec MongoDB (pas Long)

    private String nom;
    private String description;
    private double prix;
    private String categorie;
    private String image;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDate dateExpiration;

    // Avec MongoDB on peut embarquer le stock directement
    private Stock stock;

    // Liste des ids de promotions (pas de @ManyToMany)
    private List<String> promotionIds;
}

