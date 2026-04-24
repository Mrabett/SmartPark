package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "abonnement_configs")
@Data
@NoArgsConstructor
public class AbonnementConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String label;
    private int pointsRequis;
    private double pourcentageReduction;
    private boolean actif;
}

