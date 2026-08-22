package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.ReactionRequest;
import com.universite.reseausocial.dto.ReactionResponse;
import com.universite.reseausocial.service.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping
    public ResponseEntity<ReactionResponse> getReactions(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ReactionResponse response = reactionService.getReactionSummary(postId, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReactionResponse> addOrUpdateReaction(
            @PathVariable Long postId,
            @Valid @RequestBody ReactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReactionResponse response = reactionService.addOrUpdateReaction(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ReactionResponse> updateReaction(
            @PathVariable Long postId,
            @Valid @RequestBody ReactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReactionResponse response = reactionService.addOrUpdateReaction(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ReactionResponse> removeReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReactionResponse response = reactionService.removeReaction(postId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
