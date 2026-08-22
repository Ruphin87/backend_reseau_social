package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.AnnouncementRequest;
import com.universite.reseausocial.dto.AnnouncementResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.entity.Announcement;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
import com.universite.reseausocial.exception.UnauthorizedException;
import com.universite.reseausocial.repository.AnnouncementRepository;
import com.universite.reseausocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    /**
     * Récupérer toutes les annonces actives
     */
    public List<AnnouncementResponse> getAllActiveAnnouncements(String currentUserEmail) {
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        List<Announcement> announcements = announcementRepository.findByStatusNotOrderByCreatedAtDesc("DELETED");
        
        // Filtrer les annonces expirées
        List<Announcement> activeAnnouncements = announcements.stream()
                .filter(announcement -> !announcement.getStatus().equals("EXPIRED"))
                .filter(announcement -> canViewAnnouncement(announcement, currentUser))
                .collect(Collectors.toList());
        
        return activeAnnouncements.stream()
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une annonce par ID
     */
    public AnnouncementResponse getAnnouncementById(Long id, String currentUserEmail) {
        Announcement announcement = announcementRepository.findByIdAndStatusNot(id, "DELETED")
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable avec l'identifiant : " + id));
        
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        if (!canViewAnnouncement(announcement, currentUser)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à consulter cette annonce");
        }
        
        return mapToAnnouncementResponse(announcement);
    }

    /**
     * Créer une nouvelle annonce
     */
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Announcement announcement = Announcement.builder()
                .user(user)
                .titre(request.getTitre())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory() != null ? request.getCategory() : "AUTRE")
                .visibility(normalizeVisibility(request.getVisibility()))
                .status("ACTIVE")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return mapToAnnouncementResponse(savedAnnouncement);
    }

    /**
     * Mettre à jour une annonce
     */
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request, String userEmail) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Vérifier que l'utilisateur est propriétaire ou modérateur
        if (!announcement.getUser().getId().equals(user.getId()) && !"MODERATOR".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        announcement.setTitre(request.getTitre());
        announcement.setDescription(request.getDescription());
        
        if (request.getImageUrl() != null) {
            announcement.setImageUrl(request.getImageUrl());
        }
        
        if (request.getCategory() != null) {
            announcement.setCategory(request.getCategory());
        }
        
        if (request.getVisibility() != null) {
            announcement.setVisibility(normalizeVisibility(request.getVisibility()));
        }
        
        if (request.getStartDate() != null) {
            announcement.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            announcement.setEndDate(request.getEndDate());
        }
        
        if (request.getLocation() != null) {
            announcement.setLocation(request.getLocation());
        }
        
        if (request.getContactInfo() != null) {
            announcement.setContactInfo(request.getContactInfo());
        }

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        return mapToAnnouncementResponse(updatedAnnouncement);
    }

    /**
     * Supprimer une annonce (soft delete)
     */
    @Transactional
    public void deleteAnnouncement(Long id, String userEmail) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!announcement.getUser().getId().equals(user.getId()) && !"MODERATOR".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        announcement.setStatus("DELETED");
        announcementRepository.save(announcement);
    }

    /**
     * Masquer une annonce (modérateurs uniquement)
     */
    @Transactional
    public void hideAnnouncement(Long id, String userEmail) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!"MODERATOR".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedException("Seul un modérateur ou administrateur peut masquer une annonce");
        }

        announcement.setStatus("HIDDEN");
        announcementRepository.save(announcement);
    }

    /**
     * Récupérer les annonces par catégorie
     */
    public List<AnnouncementResponse> getAnnouncementsByCategory(String category, String currentUserEmail) {
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        List<Announcement> announcements = announcementRepository.findByCategoryAndStatusNotOrderByCreatedAtDesc(category, "DELETED");
        
        return announcements.stream()
                .filter(announcement -> canViewAnnouncement(announcement, currentUser))
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher les annonces par mot-clé
     */
    public List<AnnouncementResponse> searchAnnouncements(String keyword, String currentUserEmail) {
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        List<Announcement> announcements = announcementRepository.searchByKeyword(keyword, "DELETED");
        
        return announcements.stream()
                .filter(announcement -> canViewAnnouncement(announcement, currentUser))
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les annonces d'une période de temps
     */
    public List<AnnouncementResponse> getAnnouncementsByDateRange(
            ZonedDateTime startDate, 
            ZonedDateTime endDate, 
            String currentUserEmail) {
        
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        List<Announcement> announcements = announcementRepository.findByDateRange(startDate, endDate, "DELETED");
        
        return announcements.stream()
                .filter(announcement -> canViewAnnouncement(announcement, currentUser))
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les annonces d'un utilisateur
     */
    public List<AnnouncementResponse> getUserAnnouncements(Long userId, String currentUserEmail) {
        User currentUser = currentUserEmail != null 
            ? userRepository.findByEmail(currentUserEmail).orElse(null) 
            : null;
        
        List<Announcement> announcements = announcementRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, "DELETED");
        
        return announcements.stream()
                .filter(announcement -> canViewAnnouncement(announcement, currentUser))
                .map(this::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertir une annonce en réponse DTO
     */
    public AnnouncementResponse mapToAnnouncementResponse(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .author(UserResponse.builder()
                        .id(announcement.getUser().getId())
                        .nom(announcement.getUser().getNom())
                        .prenom(announcement.getUser().getPrenom())
                        .email(announcement.getUser().getEmail())
                        .filiere(announcement.getUser().getFiliere())
                        .niveau(announcement.getUser().getNiveau())
                        .photoProfil(announcement.getUser().getPhotoProfil())
                        .role(announcement.getUser().getRole())
                        .build())
                .titre(announcement.getTitre())
                .description(announcement.getDescription())
                .imageUrl(announcement.getImageUrl())
                .category(announcement.getCategory())
                .visibility(announcement.getVisibility())
                .status(announcement.getStatus())
                .startDate(announcement.getStartDate())
                .endDate(announcement.getEndDate())
                .location(announcement.getLocation())
                .contactInfo(announcement.getContactInfo())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }

    /**
     * Normaliser la visibilité
     */
    private String normalizeVisibility(String visibility) {
        if (visibility == null) {
            return "PUBLIC";
        }
        String normalized = visibility.trim().toUpperCase();
        if (normalized.equals("PRIVATE") || normalized.equals("FRIENDS")) {
            return normalized;
        }
        return "PUBLIC";
    }

    /**
     * Vérifier si l'utilisateur peut voir l'annonce
     */
    private boolean canViewAnnouncement(Announcement announcement, User currentUser) {
        if (announcement.getStatus().equals("HIDDEN")) {
            return currentUser != null && ("MODERATOR".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole()));
        }
        
        if (announcement.getStatus().equals("EXPIRED")) {
            return false;
        }
        
        String visibility = normalizeVisibility(announcement.getVisibility());
        
        if (visibility.equals("PUBLIC")) {
            return true;
        }
        
        if (currentUser == null) {
            return false;
        }
        
        if (visibility.equals("PRIVATE")) {
            return announcement.getUser().getId().equals(currentUser.getId()) || "MODERATOR".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole());
        }
        
        return true; // FRIENDS
    }
}
