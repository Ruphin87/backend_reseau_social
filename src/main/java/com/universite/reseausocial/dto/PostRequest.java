package com.universite.reseausocial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    @NotBlank(message = "Le contenu de la publication ne peut pas être vide")
    private String contenu;

    private String imageUrl;

    @Builder.Default
    private String type = "PUBLICATION"; // PUBLICATION ou ANNONCE

    @Builder.Default
    private String visibility = "PUBLIC"; // PUBLIC, FRIENDS, PRIVATE
}
