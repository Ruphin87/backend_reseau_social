package com.universite.reseausocial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "Veuillez renseigner un email valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
