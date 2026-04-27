package com.smartpark.backend1.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
  private String nom;
  private String email;
  private String password;
  private String telephone;
}
