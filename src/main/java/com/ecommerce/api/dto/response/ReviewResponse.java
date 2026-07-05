package com.ecommerce.api.dto.response;

import com.ecommerce.api.model.Review;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.productId = review.getProduct().getId();
        this.userId = review.getUser().getId();
        this.userName = review.getUser().getUsername();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
