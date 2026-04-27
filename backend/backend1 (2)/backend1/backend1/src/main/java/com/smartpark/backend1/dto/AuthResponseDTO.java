package com.smartpark.backend1.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
  private String token;
  private String email;
  private String nom;
  private String role;
  private String telephone;
  private String message;
}
