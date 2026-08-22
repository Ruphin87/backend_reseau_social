package com.universite.reseausocial.dto;

import lombok.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {
    private Long id;
    private UserResponse author;
    private String titre;
    private String description;
    private String imageUrl;
    private String category;
    private String visibility;
    private String status;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String location;
    private String contactInfo;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
