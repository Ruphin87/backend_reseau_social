package com.universite.reseausocial.repository;

import com.universite.reseausocial.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByStatusOrderByCreatedAtDesc(String status);
    List<Post> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, String status);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<Post> findAllActivePosts();
}
