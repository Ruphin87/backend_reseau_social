package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.AnnouncementRequest;
import com.universite.reseausocial.dto.AnnouncementResponse;
import com.universite.reseausocial.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * Récupérer toutes les annonces actives
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAllAnnouncements(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<AnnouncementResponse> announcements = announcementService.getAllActiveAnnouncements(email);
        return ResponseEntity.ok(announcements);
    }

    /**
     * Récupérer une annonce par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        AnnouncementResponse announcement = announcementService.getAnnouncementById(id, email);
        return ResponseEntity.ok(announcement);
    }

    /**
     * Créer une nouvelle annonce
     */
    @PostMapping
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AnnouncementResponse announcement = announcementService.createAnnouncement(
            request, 
            userDetails.getUsername()
        );
        return new ResponseEntity<>(announcement, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour une annonce
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AnnouncementResponse announcement = announcementService.updateAnnouncement(
            id, 
            request, 
            userDetails.getUsername()
        );
        return ResponseEntity.ok(announcement);
    }

    /**
     * Supprimer une annonce
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        announcementService.deleteAnnouncement(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Masquer une annonce (modérateur uniquement)
     */
    @PutMapping("/{id}/hide")
    public ResponseEntity<Void> hideAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        announcementService.hideAnnouncement(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer les annonces par catégorie
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<AnnouncementResponse> announcements = announcementService.getAnnouncementsByCategory(category, email);
        return ResponseEntity.ok(announcements);
    }

    /**
     * Rechercher les annonces
     */
    @GetMapping("/search")
    public ResponseEntity<List<AnnouncementResponse>> searchAnnouncements(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<AnnouncementResponse> announcements = announcementService.searchAnnouncements(keyword, email);
        return ResponseEntity.ok(announcements);
    }

    /**
     * Récupérer les annonces par période de temps
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByDateRange(
            @RequestParam ZonedDateTime startDate,
            @RequestParam ZonedDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<AnnouncementResponse> announcements = announcementService.getAnnouncementsByDateRange(startDate, endDate, email);
        return ResponseEntity.ok(announcements);
    }

    /**
     * Récupérer les annonces d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnnouncementResponse>> getUserAnnouncements(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<AnnouncementResponse> announcements = announcementService.getUserAnnouncements(userId, email);
        return ResponseEntity.ok(announcements);
    }
}
