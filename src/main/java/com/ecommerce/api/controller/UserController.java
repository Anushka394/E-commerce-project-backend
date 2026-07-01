package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.UpdateProfileRequest;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.model.User;
import com.ecommerce.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Returns the authenticated user's profile. Auth required.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserByUsername(userDetails.getUsername()));
    }

    /**
     * PUT /api/users/me
     * Update username, email, password, or address. Auth required.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.findEntityByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }
}
