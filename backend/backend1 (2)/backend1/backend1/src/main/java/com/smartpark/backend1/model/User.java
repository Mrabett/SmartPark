package com.smartpark.backend1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  private String id;

  private String nom;
  private String prenom;

  @Indexed(unique = true)
  private String email;

  private String password;
  private String telephone;
  private String role; // "ADMIN" ou "USER"
  private boolean actif = true;
}
