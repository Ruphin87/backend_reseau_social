package com.universite.reseausocial.dto;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private UserResponse author;
    private String contenu;
    private String imageUrl;
    private String type;
    private String visibility;
    private String status;
    private ZonedDateTime createdAt;
    private long likesCount;
    private long commentsCount;
    private String currentUserReaction;
    private Map<String, Long> reactionCounts;
}
