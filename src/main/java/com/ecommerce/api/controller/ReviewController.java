package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.ReviewRequest;
import com.ecommerce.api.dto.response.ReviewResponse;
import com.ecommerce.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * GET /api/products/{productId}/reviews
     * Get all reviews for a product (public)
     */
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    /**
     * POST /api/products/{productId}/reviews
     * Create a review (authenticated user)
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.createReview(productId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * PUT /api/products/{productId}/reviews/{reviewId}
     * Update own review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse updated = reviewService.updateReview(reviewId, userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/products/{productId}/reviews/{reviewId}
     * Delete own review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
