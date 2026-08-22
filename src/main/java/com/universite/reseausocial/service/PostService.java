package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.PostRequest;
import com.universite.reseausocial.dto.PostResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.entity.Post;
import com.universite.reseausocial.entity.Reaction;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
import com.universite.reseausocial.exception.UnauthorizedException;
import com.universite.reseausocial.repository.PostRepository;
import com.universite.reseausocial.repository.ReactionRepository;
import com.universite.reseausocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;

    public List<PostResponse> getAllActivePosts(String currentUserEmail) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        List<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        return posts.stream()
                .filter(post -> canViewPost(post, currentUser))
                .map(post -> mapToPostResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long id, String currentUserEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'identifiant : " + id));
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        if (!canViewPost(post, currentUser)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à consulter cette publication");
        }
        return mapToPostResponse(post, currentUser);
    }

    @Transactional
    public PostResponse createPost(PostRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        String type = "PUBLICATION";
        if (request.getType() != null && request.getType().equalsIgnoreCase("ANNONCE")) {
            if (!"MODERATOR".equals(user.getRole())) {
                throw new UnauthorizedException("Seul un modérateur peut publier une annonce");
            }
            type = "ANNONCE";
        }

        Post post = Post.builder()
                .user(user)
                .contenu(request.getContenu())
                .imageUrl(request.getImageUrl())
                .type(type)
                .visibility(normalizeVisibility(request.getVisibility()))
                .status("ACTIVE")
                .build();

        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost, user);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals("MODERATOR")) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette publication");
        }

        post.setContenu(request.getContenu());
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }
        if (request.getType() != null) {
            String requestedType = request.getType().equalsIgnoreCase("ANNONCE") ? "ANNONCE" : "PUBLICATION";
            if (requestedType.equals("ANNONCE") && !"MODERATOR".equals(user.getRole())) {
                throw new UnauthorizedException("Seul un modérateur peut publier une annonce");
            }
            post.setType(requestedType);
        }
        if (request.getVisibility() != null) {
            post.setVisibility(normalizeVisibility(request.getVisibility()));
        }

        Post updatedPost = postRepository.save(post);
        return mapToPostResponse(updatedPost, user);
    }

    @Transactional
    public void deletePost(Long id, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals("MODERATOR")) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette publication");
        }

        post.setStatus("DELETED");
        postRepository.save(post);
    }

    @Transactional
    public void hidePost(Long id, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'id : " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!user.getRole().equals("MODERATOR")) {
            throw new UnauthorizedException("Seul un modérateur peut masquer une publication");
        }

        post.setStatus("HIDDEN");
        postRepository.save(post);
    }

    public PostResponse mapToPostResponse(Post post, User currentUser) {
        List<Reaction> reactions = reactionRepository.findByPostId(post.getId());
        long totalLikes = reactions.size();
        
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getType, Collectors.counting()));

        String currentUserReaction = null;
        if (currentUser != null) {
            Optional<Reaction> userReactionOpt = reactions.stream()
                    .filter(r -> r.getUser().getId().equals(currentUser.getId()))
                    .findFirst();
            if (userReactionOpt.isPresent()) {
                currentUserReaction = userReactionOpt.get().getType();
            }
        }

        long commentsCount = post.getComments() != null 
                ? post.getComments().size() 
                : 0;

        return PostResponse.builder()
                .id(post.getId())
                .author(UserResponse.builder()
                        .id(post.getUser().getId())
                        .nom(post.getUser().getNom())
                        .prenom(post.getUser().getPrenom())
                        .email(post.getUser().getEmail())
                        .filiere(post.getUser().getFiliere())
                        .niveau(post.getUser().getNiveau())
                        .photoProfil(post.getUser().getPhotoProfil())
                        .role(post.getUser().getRole())
                        .build())
                .contenu(post.getContenu())
                .imageUrl(post.getImageUrl())
                .type(post.getType())
                .visibility(post.getVisibility())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .likesCount(totalLikes)
                .commentsCount(commentsCount)
                .currentUserReaction(currentUserReaction)
                .reactionCounts(reactionCounts)
                .build();
    }

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

    private boolean canViewPost(Post post, User currentUser) {
        if (!"ACTIVE".equals(post.getStatus())) {
            return currentUser != null && "MODERATOR".equals(currentUser.getRole());
        }
        String visibility = normalizeVisibility(post.getVisibility());
        if ("PUBLIC".equals(visibility)) {
            return true;
        }
        if (currentUser == null) {
            return false;
        }
        if ("MODERATOR".equals(currentUser.getRole())) {
            return true;
        }
        return post.getUser().getId().equals(currentUser.getId());
    }
}
