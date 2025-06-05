package com.cc.library.repository;

import com.cc.library.model.Reservation;
import com.cc.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Find reservations by user, ordered by reservation date descending
    Page<Reservation> findByUserIdOrderByReservationDateDesc(Long userId, Pageable pageable);

    // Find reservations by book, ordered by reservation date ascending
    List<Reservation> findByBookIdOrderByReservationDateAsc(Long bookId);

    // Find reservations by book and status
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    // Check if a user has a pending reservation for a specific book
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);

    // Count pending reservations for a specific book
    long countByBookIdAndStatus(Long bookId, ReservationStatus status);
}
