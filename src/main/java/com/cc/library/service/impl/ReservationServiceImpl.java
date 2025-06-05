package com.cc.library.service.impl;

import com.cc.library.dto.ReviewRequest;
import com.cc.library.entity.Book;
import com.cc.library.entity.User;
import com.cc.library.model.Reservation;
import com.cc.library.model.ReservationStatus;
import com.cc.library.model.Review;
import com.cc.library.model.ReviewStatus;
import com.cc.library.repository.BookRepository;
import com.cc.library.repository.ReservationRepository;
import com.cc.library.repository.ReviewRepository;
import com.cc.library.repository.UserRepository;
import com.cc.library.service.ReservationService;
import com.cc.library.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Reservation createReservation(Long bookId, Long userId) {
        // Check if book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the book is available for reservation
        if (book.getStatus() != Book.BookStatus.UNAVAILABLE) {
            throw new RuntimeException("Book is not available for reservation");
        }

        // Check if the user already has a pending reservation for this book
        if (reservationRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, ReservationStatus.PENDING)) {
            throw new RuntimeException("User already has a pending reservation for this book");
        }

        // Get the current position in the reservation queue
        long queuePosition = reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.PENDING) + 1;

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);
        
        // Set expiration date (7 days from now)
        reservation.setExpirationDate(LocalDateTime.now().plusDays(7));

        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check if the user is the owner of the reservation
        if (!reservation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this reservation");
        }

        // Only pending reservations can be cancelled
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public Page<Reservation> getUserReservations(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable);
    }

    @Override
    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Override
    public Optional<Reservation> getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Override
    @Transactional
    public Reservation fulfillReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not pending");
        }

        // Check if the book is actually available
        Book book = reservation.getBook();
        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            throw new RuntimeException("Book is not yet available");
        }

        // Check if the reservation has expired
        if (reservation.getExpirationDate() != null && 
            reservation.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reservation has expired");
        }

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setFulfillmentDate(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    /**
     * Scheduled task to check and handle expired reservations
     * Runs every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void handleExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING &&
                        r.getExpirationDate() != null &&
                        r.getExpirationDate().isBefore(now))
                .collect(Collectors.toList());

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
    }

    /**
     * Get the next pending reservation for a book
     * @param bookId The ID of the book
     * @return The next pending reservation, if any
     */
    public Optional<Reservation> getNextPendingReservation(Long bookId) {
        List<Reservation> pendingReservations = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.PENDING);
        return pendingReservations.isEmpty() ? Optional.empty() : Optional.of(pendingReservations.get(0));
    }
} 