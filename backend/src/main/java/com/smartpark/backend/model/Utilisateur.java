package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "utilisateurs")
@Data
@NoArgsConstructor
public class Utilisateur {

    @Id
    private String id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String username;
    private String motDePasse;
    private String role;        // "ADMIN" ou "CLIENT"
    private String numClient;
    private LocalDate dateInscription;
}
