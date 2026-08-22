package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.PostRequest;
import com.universite.reseausocial.dto.PostResponse;
import com.universite.reseausocial.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<PostResponse> posts = postService.getAllActivePosts(email);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        PostResponse post = postService.getPostById(id, email);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse post = postService.createPost(request, userDetails.getUsername());
        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse post = postService.updatePost(id, request, userDetails.getUsername());
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/hide")
    public ResponseEntity<Void> hidePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.hidePost(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
