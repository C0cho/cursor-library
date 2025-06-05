package com.cc.library.service;

import com.cc.library.model.Review;
import com.cc.library.dto.ReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    /**
     * Create a new review.
     * @param reviewRequest The review data.
     * @param userId The ID of the user submitting the review.
     * @return The created Review entity.
     */
    Review createReview(ReviewRequest reviewRequest, Long userId);

    /**
     * Get all approved reviews for a specific book, ordered by creation date descending.
     * @param bookId The ID of the book.
     * @return A list of approved reviews.
     */
    List<Review> getReviewsByBookId(Long bookId);

    /**
     * Get all reviews (for admin), potentially with pagination and filtering.
     * @param pageable Pagination information.
     * @return A page of reviews.
     */
    Page<Review> getAllReviews(Pageable pageable);

    /**
     * Approve a specific review.
     * @param reviewId The ID of the review to approve.
     * @return The approved Review entity.
     */
    Review approveReview(Long reviewId);

    /**
     * Reject a specific review.
     * @param reviewId The ID of the review to reject.
     * @return The rejected Review entity.
     */
    Review rejectReview(Long reviewId);

    /**
     * Get a review by its ID.
     * @param reviewId The ID of the review.
     * @return An Optional containing the Review if found.
     */
    Optional<Review> getReviewById(Long reviewId);
} 