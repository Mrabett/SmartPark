package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "objectifs")
@Data
@NoArgsConstructor
public class Objectif {

    @Id
    private String id;

    private String titre;
    private String description;
    private String icon;
    private boolean actif;
    private LocalDateTime dateCreation;

    // Liste des produits associés à cet objectif
    private List<String> produitIds;
}
