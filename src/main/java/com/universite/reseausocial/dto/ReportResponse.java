package com.universite.reseausocial.dto;

import lombok.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private Long postId;
    private String postContent;
    private String postAuthor;
    private Long commentId;
    private String commentContent;
    private String commentAuthor;
    private UserResponse reportedBy;
    private String raison;
    private String details;
    private String status; // PENDING, RESOLVED, REJECTED
    private ZonedDateTime createdAt;
}
