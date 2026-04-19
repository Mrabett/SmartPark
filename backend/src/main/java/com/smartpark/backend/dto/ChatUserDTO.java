package com.smartpark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatUserDTO {
    private String id;
    private String nom;
    private String prenom;
    private String role;
}
