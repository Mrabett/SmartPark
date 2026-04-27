// src/main/java/com/smartpark/evenements/dto/AiGenerateResponse.java
package com.smartpark.backend1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGenerateResponse {
  private String description;
  private int    characters;
}
