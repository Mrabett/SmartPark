package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "recommandations")
@Data
@NoArgsConstructor
public class Recommandation {

    @Id
    private String id;

    private double score;
    private LocalDate dateGeneree;
    private String raison;
    private boolean vue;
    private boolean achetee;

    private String utilisateurId;
    private String produitId;
}
