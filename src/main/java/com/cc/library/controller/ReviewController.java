package com.cc.library.controller;

import com.cc.library.dto.ReviewRequest;
import com.cc.library.model.Review;
import com.cc.library.service.ReviewService;
import com.cc.library.service.UserService;
import com.cc.library.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api") // Or just "/" depending on your API gateway/proxy setup
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Helper method to get the current user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Handle unauthenticated user
            throw new IllegalStateException("User is not authenticated"); // Consider a custom exception
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                 throw new IllegalStateException("User not found in database"); // Should not happen if authenticated
            }
            return user.getId();
        } else if (principal instanceof String) { // Potentially username directly
             String username = (String) principal;
             User user = userService.getUserByUsername(username);
             if (user == null) {
                 throw new IllegalStateException("User not found in database"); // Should not happen if authenticated
            }
            return user.getId();
        }
        // Add other principal types if necessary based on your SecurityConfig
        
        throw new IllegalStateException("Unable to get user ID from authentication principal"); // Fallback for unhandled principal types
    }

    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        // Get current authenticated user's ID
        Long userId = getCurrentUserId();

        Review createdReview = reviewService.createReview(reviewRequest, userId);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    // Admin endpoints for review management
    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Review>> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/admin/reviews/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> approveReview(@PathVariable Long reviewId) {
        Review approvedReview = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(approvedReview);
    }

    @PutMapping("/admin/reviews/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> rejectReview(@PathVariable Long reviewId) {
        Review rejectedReview = reviewService.rejectReview(reviewId);
        return ResponseEntity.ok(rejectedReview);
    }

    @GetMapping("/admin/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> getReviewById(@PathVariable Long reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 