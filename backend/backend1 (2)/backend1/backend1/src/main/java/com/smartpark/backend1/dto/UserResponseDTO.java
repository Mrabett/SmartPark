package com.smartpark.backend1.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
  private String id;
  private String nom;
  private String email;
  private String telephone;
  private String role;
  private boolean actif;
}
