package com.universite.reseausocial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementRequest {

    @NotBlank(message = "Le titre de l'annonce ne peut pas être vide")
    private String titre;

    @NotBlank(message = "La description de l'annonce ne peut pas être vide")
    private String description;

    private String imageUrl;

    @Builder.Default
    private String category = "AUTRE"; // EMPLOI, LOGEMENT, COURS, EVENEMENT, AUTRE

    @Builder.Default
    private String visibility = "PUBLIC"; // PUBLIC, FRIENDS, PRIVATE

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private String location;

    private String contactInfo;
}
