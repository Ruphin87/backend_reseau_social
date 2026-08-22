package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.CommentRequest;
import com.universite.reseausocial.dto.CommentResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.entity.Comment;
import com.universite.reseausocial.entity.Post;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
import com.universite.reseausocial.exception.UnauthorizedException;
import com.universite.reseausocial.repository.CommentRepository;
import com.universite.reseausocial.repository.PostRepository;
import com.universite.reseausocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Publication introuvable");
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'id : " + postId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .contenu(request.getContenu())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToCommentResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable avec l'id : " + commentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals("MODERATOR")) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }

        comment.setContenu(request.getContenu());
        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable avec l'id : " + commentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals("MODERATOR")) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        commentRepository.delete(comment);
    }

    public CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(UserResponse.builder()
                        .id(comment.getUser().getId())
                        .nom(comment.getUser().getNom())
                        .prenom(comment.getUser().getPrenom())
                        .email(comment.getUser().getEmail())
                        .filiere(comment.getUser().getFiliere())
                        .niveau(comment.getUser().getNiveau())
                        .photoProfil(comment.getUser().getPhotoProfil())
                        .role(comment.getUser().getRole())
                        .build())
                .contenu(comment.getContenu())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
