package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.PostResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
import com.universite.reseausocial.repository.PostRepository;
import com.universite.reseausocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    public UserResponse getCurrentUserProfile(String email) {
        User user = getUserByEmail(email);
        return mapToUserResponse(user);
    }

    public List<PostResponse> getCurrentUserPosts(String email) {
        User user = getUserByEmail(email);
        return postRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(user.getId(), "DELETED")
                .stream()
                .map(post -> postService.mapToPostResponse(post, user))
                .collect(Collectors.toList());
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .filiere(user.getFiliere())
                .niveau(user.getNiveau())
                .photoProfil(user.getPhotoProfil())
                .role(user.getRole())
                .build();
    }
}
