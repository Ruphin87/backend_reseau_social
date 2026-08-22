package com.universite.reseausocial.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionResponse {
    private Long postId;
    private String userReaction;
    private long totalReactions;
    private Map<String, Long> details;
}
