package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.ReactionRequest;
import com.universite.reseausocial.dto.ReactionResponse;
import com.universite.reseausocial.entity.Post;
import com.universite.reseausocial.entity.Reaction;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
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
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReactionResponse addOrUpdateReaction(Long postId, ReactionRequest request, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable avec l'id : " + postId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        String type = request.getType().toUpperCase();
        if (!type.matches("LIKE|LOVE|HAHA|SAD|ANGRY")) {
            throw new IllegalArgumentException("Type de réaction invalide. Choix : LIKE, LOVE, HAHA, SAD, ANGRY");
        }

        Optional<Reaction> existing = reactionRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getType().equals(type)) {
                // Si même réaction, on supprime (toggle)
                reactionRepository.delete(reaction);
            } else {
                reaction.setType(type);
                reactionRepository.save(reaction);
            }
        } else {
            Reaction newReaction = Reaction.builder()
                    .post(post)
                    .user(user)
                    .type(type)
                    .build();
            reactionRepository.save(newReaction);
        }

        return getReactionSummary(postId, userEmail);
    }

    @Transactional
    public ReactionResponse removeReaction(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        reactionRepository.deleteByPostIdAndUserId(postId, user.getId());
        return getReactionSummary(postId, userEmail);
    }

    public ReactionResponse getReactionSummary(Long postId, String userEmail) {
        List<Reaction> reactions = reactionRepository.findByPostId(postId);
        User user = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;

        String currentUserReaction = null;
        if (user != null) {
            Optional<Reaction> userReactionOpt = reactions.stream()
                    .filter(r -> r.getUser().getId().equals(user.getId()))
                    .findFirst();
            if (userReactionOpt.isPresent()) {
                currentUserReaction = userReactionOpt.get().getType();
            }
        }

        Map<String, Long> details = reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getType, Collectors.counting()));

        return ReactionResponse.builder()
                .postId(postId)
                .userReaction(currentUserReaction)
                .totalReactions(reactions.size())
                .details(details)
                .build();
    }
}
