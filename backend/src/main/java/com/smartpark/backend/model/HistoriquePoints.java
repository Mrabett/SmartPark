package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "historique_points")
@Data
@NoArgsConstructor
public class HistoriquePoints {

    @Id
    private String id;

    private int pointsGagnes;
    private int pointsUtilises;
    private String motif;
    private LocalDateTime date;

    private String clientId;
    private String commandeId;
}
