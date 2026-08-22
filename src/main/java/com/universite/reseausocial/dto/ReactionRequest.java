package com.universite.reseausocial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionRequest {

    @NotBlank(message = "Le type de réaction est obligatoire (LIKE, LOVE, HAHA, SAD, ANGRY)")
    private String type;
}
