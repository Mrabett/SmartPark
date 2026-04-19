package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    private String id;

    private String produitId;
    private String utilisateurId;
    private String utilisateurNom;

    private int note; // 1..5
    private String commentaire;

    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
}
