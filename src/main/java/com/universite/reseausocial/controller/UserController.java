package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.PostResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/posts")
    public ResponseEntity<List<PostResponse>> getCurrentUserPosts(@AuthenticationPrincipal UserDetails userDetails) {
        List<PostResponse> response = userService.getCurrentUserPosts(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
