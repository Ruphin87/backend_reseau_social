package com.universite.reseausocial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    private Long postId;
    private Long commentId;

    @NotBlank(message = "La raison du signalement est obligatoire")
    private String raison; // INAPPROPRIATE, SPAM, HARASSMENT, OFFENSIVE, FALSE_INFORMATION, OTHER

    private String details;
}
