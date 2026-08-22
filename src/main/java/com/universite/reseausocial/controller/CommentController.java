package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.CommentRequest;
import com.universite.reseausocial.dto.CommentResponse;
import com.universite.reseausocial.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse comment = commentService.addComment(postId, request, userDetails.getUsername());
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse comment = commentService.updateComment(id, request, userDetails.getUsername());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.deleteComment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
