package com.cc.library.service;

import com.cc.library.model.Reservation;
import com.cc.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
    /**
     * Create a new reservation for a book.
     * @param bookId The ID of the book to reserve.
     * @param userId The ID of the user making the reservation.
     * @return The created Reservation entity.
     */
    Reservation createReservation(Long bookId, Long userId);

    /**
     * Cancel a user's pending reservation.
     * @param reservationId The ID of the reservation to cancel.
     * @param userId The ID of the user requesting cancellation (for authorization).
     */
    void cancelReservation(Long reservationId, Long userId);

    /**
     * Get all reservations for a specific user, ordered by reservation date descending.
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A page of user's reservations.
     */
    Page<Reservation> getUserReservations(Long userId, Pageable pageable);

    /**
     * Get all reservations (for admin), potentially with pagination and filtering.
     * @param pageable Pagination information.
     * @return A page of all reservations.
     */
    Page<Reservation> getAllReservations(Pageable pageable);

    /**
     * Get a reservation by its ID.
     * @param reservationId The ID of the reservation.
     * @return An Optional containing the Reservation if found.
     */
    Optional<Reservation> getReservationById(Long reservationId);

    /**
     * Admin method to fulfill a reservation.
     * This should be called when a book is returned and needs to be offered to the next person in the reservation queue.
     * @param reservationId The ID of the reservation to fulfill.
     * @return The fulfilled Reservation entity.
     */
    Reservation fulfillReservation(Long reservationId);
} 