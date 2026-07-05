package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ReviewRequest;
import com.ecommerce.api.dto.response.ReviewResponse;
import com.ecommerce.api.exception.ConflictException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Review;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository,
                        ProductService productService,
                        UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        // Verify product exists
        productService.findEntityById(productId);
        return reviewRepository.findByProductId(productId, pageable).map(ReviewResponse::new);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Long getReviewCount(Long productId) {
        return reviewRepository.getReviewCountByProductId(productId);
    }

    public ReviewResponse createReview(Long productId, String username, ReviewRequest request) {
        Product product = productService.findEntityById(productId);
        User user = userService.findEntityByUsername(username);

        // Check if user already reviewed this product
        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new ConflictException("You have already reviewed this product");
        }

        Review review = new Review(product, user, request.getRating(), request.getComment());
        Review saved = reviewRepository.save(review);
        log.info("Review created. ProductId: {}, UserId: {}, Rating: {}", productId, user.getId(), request.getRating());

        return new ReviewResponse(saved);
    }

    public ReviewResponse updateReview(Long reviewId, String username, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User user = userService.findEntityByUsername(username);
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ConflictException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review updated = reviewRepository.save(review);
        log.info("Review updated. ReviewId: {}, NewRating: {}", reviewId, request.getRating());

        return new ReviewResponse(updated);
    }

    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User user = userService.findEntityByUsername(username);
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ConflictException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(reviewId);
        log.info("Review deleted. ReviewId: {}", reviewId);
    }
}
