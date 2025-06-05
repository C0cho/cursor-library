package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.model.Reservation;
import com.cc.library.service.ReservationService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService; // Assuming UserService is needed to get user ID from username

    /**
     * Endpoint for users to create a reservation.
     * @param bookId The ID of the book to reserve.
     * @return The created Reservation.
     */
    @PostMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reservation> createReservation(@RequestParam Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
             String username = ((UserDetails) authentication.getPrincipal()).getUsername();
             User user = userService.getUserByUsername(username);
             if (user != null) {
                 userId = user.getId();
             }
        } else if (authentication != null && authentication.getPrincipal() instanceof String) { // Handle case where principal is just the username string
             String username = (String) authentication.getPrincipal();
             User user = userService.getUserByUsername(username);
             if (user != null) {
                 userId = user.getId();
             }
        }

        if (userId == null) {
             // Return an appropriate error response if user ID cannot be determined
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Or a more specific error body
        }

        Reservation createdReservation = reservationService.createReservation(bookId, userId);
        return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
    }

    /**
     * Endpoint for users to cancel their own reservation.
     * @param reservationId The ID of the reservation to cancel.
     * @return ResponseEntity with status.
     */
    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         Long userId = null;
         if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
              String username = ((UserDetails) authentication.getPrincipal()).getUsername();
              User user = userService.getUserByUsername(username);
              if (user != null) {
                  userId = user.getId();
              }
         } else if (authentication != null && authentication.getPrincipal() instanceof String) { // Handle case where principal is just the username string
              String username = (String) authentication.getPrincipal();
              User user = userService.getUserByUsername(username);
              if (user != null) {
                  userId = user.getId();
              }
         }

         if (userId == null) {
              // Return an appropriate error response if user ID cannot be determined
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Or a more specific error body
         }
        reservationService.cancelReservation(reservationId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for users to get their own reservations.
     * @param pageable Pagination information.
     * @return A page of user's reservations.
     */
    @GetMapping("/user/reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Reservation>> getUserReservations(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         Long userId = null;
         if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
              String username = ((UserDetails) authentication.getPrincipal()).getUsername();
              User user = userService.getUserByUsername(username);
              if (user != null) {
                  userId = user.getId();
              }
         } else if (authentication != null && authentication.getPrincipal() instanceof String) { // Handle case where principal is just the username string
              String username = (String) authentication.getPrincipal();
              User user = userService.getUserByUsername(username);
              if (user != null) {
                  userId = user.getId();
              }
         }

         if (userId == null) {
              // Return an appropriate error response if user ID cannot be determined
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Or a more specific error body
         }
        Page<Reservation> reservations = reservationService.getUserReservations(userId, pageable);
        return ResponseEntity.ok(reservations);
    }

    // Admin endpoints for reservation management

    /**
     * Endpoint for admins to get all reservations.
     * @param pageable Pagination information.
     * @return A page of all reservations.
     */
    @GetMapping("/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Reservation>> getAllReservations(Pageable pageable) {
        Page<Reservation> reservations = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Admin endpoint to fulfill a reservation.
     * @param reservationId The ID of the reservation to fulfill.
     * @return The fulfilled Reservation.
     */
    @PutMapping("/admin/reservations/{reservationId}/fulfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reservation> fulfillReservation(@PathVariable Long reservationId) {
        Reservation fulfilledReservation = reservationService.fulfillReservation(reservationId);
        return ResponseEntity.ok(fulfilledReservation);
    }
} 