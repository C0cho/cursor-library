package com.cc.library.service.impl;

import com.cc.library.dto.ReviewRequest;
import com.cc.library.entity.Book;
import com.cc.library.entity.User;
import com.cc.library.model.Review;
import com.cc.library.model.ReviewStatus;
import com.cc.library.repository.BookRepository;
import com.cc.library.repository.ReviewRepository;
import com.cc.library.repository.UserRepository;
import com.cc.library.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Review createReview(ReviewRequest reviewRequest, Long userId) {
        Book book = bookRepository.findById(reviewRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found")); // TODO: Use a specific exception

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // TODO: Use a specific exception

        // Optional: Check if user has already reviewed this book
        // if (reviewRepository.existsByBookIdAndUserId(book.getId(), user.getId())) {
        //     throw new RuntimeException("User has already reviewed this book"); // TODO: Specific exception
        // }

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setStatus(ReviewStatus.PENDING); // New reviews are pending moderation

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByBookId(Long bookId) {
        // Fetch only approved reviews for readers
        // If moderation is not needed, remove the status check
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found")); // TODO: Specific exception
        review.setStatus(ReviewStatus.APPROVED);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found")); // TODO: Specific exception
        review.setStatus(ReviewStatus.REJECTED);
        return reviewRepository.save(review);
    }

    @Override
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    // Optional: Add methods for deleting reviews or other admin tasks
} 