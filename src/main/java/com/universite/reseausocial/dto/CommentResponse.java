package com.universite.reseausocial.dto;

import lombok.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private UserResponse author;
    private String contenu;
    private ZonedDateTime createdAt;
}
