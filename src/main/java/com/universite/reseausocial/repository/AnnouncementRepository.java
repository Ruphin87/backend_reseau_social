package com.universite.reseausocial.repository;

import com.universite.reseausocial.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Récupérer toutes les annonces actives
    List<Announcement> findByStatusNotOrderByCreatedAtDesc(String status);

    // Récupérer les annonces d'un utilisateur
    List<Announcement> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, String status);

    // Récupérer les annonces par catégorie
    List<Announcement> findByCategoryAndStatusNotOrderByCreatedAtDesc(String category, String status);

    // Récupérer les annonces par visibilité
    List<Announcement> findByVisibilityAndStatusNotOrderByCreatedAtDesc(String visibility, String status);

    // Rechercher les annonces par titre ou description
    @Query("SELECT a FROM Announcement a WHERE " +
            "(a.titre ILIKE %:keyword% OR a.description ILIKE %:keyword%) " +
            "AND a.status != :status ORDER BY a.createdAt DESC")
    List<Announcement> searchByKeyword(@Param("keyword") String keyword, @Param("status") String status);

    // Récupérer les annonces d'une période de temps
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.startDate <= :endDate AND a.endDate >= :startDate " +
            "AND a.status != :status ORDER BY a.startDate ASC")
    List<Announcement> findByDateRange(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            @Param("status") String status
    );

    // Récupérer les annonces expirées
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.endDate < CURRENT_TIMESTAMP " +
            "AND a.status = 'ACTIVE' ORDER BY a.endDate DESC")
    List<Announcement> findExpiredAnnouncements();

    // Vérifier si l'utilisateur est propriétaire de l'annonce
    boolean existsByIdAndUserId(Long id, Long userId);

    // Récupérer une annonce par ID si elle n'est pas supprimée
    Optional<Announcement> findByIdAndStatusNot(Long id, String status);
}
