package com.universite.reseausocial.repository;

import com.universite.reseausocial.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByPostIdAndUserId(Long postId, Long userId);
    List<Reaction> findByPostId(Long postId);
    long countByPostId(Long postId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
