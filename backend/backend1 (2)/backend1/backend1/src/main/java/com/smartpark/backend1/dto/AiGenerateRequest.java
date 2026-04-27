// src/main/java/com/smartpark/evenements/dto/AiGenerateRequest.java
package com.smartpark.backend1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiGenerateRequest {

  @NotBlank(message = "Le titre est obligatoire")
  private String titre;

  @NotBlank(message = "Le type est obligatoire")
  private String type;          // TOURNOI | ATELIER | CONCERT | AUTRE

  private String lieu;
  private String date;
  private String keywords;

  private String ton       = "professionnel"; // professionnel | enthousiaste | élégant | décontracté | accrocheur
  private String variant   = "courte";        // courte | longue | réseaux
}
