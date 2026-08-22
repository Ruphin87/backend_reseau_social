package com.universite.reseausocial.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String filiere;
    private String niveau;
    private String photoProfil;
    private String role;
}
