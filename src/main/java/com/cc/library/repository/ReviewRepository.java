package com.cc.library.repository;

import com.cc.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Find all reviews for a specific book, ordered by creation date descending
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);
} 