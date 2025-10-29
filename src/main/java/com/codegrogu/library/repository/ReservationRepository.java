package com.codegrogu.library.repository;

import com.codegrogu.library.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for managing reservations.
 */
public class ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    // === Create / Add a reservation ===
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    // === Read / Get a reservation by ID ===
    public Optional<Reservation> getReservationById(int reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getReservationId() == reservationId)
                .findFirst();
    }

    // === Read / Get all reservations ===
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations); // Return a copy to prevent external modification
    }

    // === Update a reservation ===
    public boolean updateReservation(Reservation updatedReservation) {
        Optional<Reservation> existingReservationOpt = getReservationById(updatedReservation.getReservationId());
        if (existingReservationOpt.isPresent()) {
            Reservation existingReservation = existingReservationOpt.get();
            existingReservation.setBookId(updatedReservation.getBookId());
            existingReservation.setMemberId(updatedReservation.getMemberId());
            existingReservation.setReservationDate(updatedReservation.getReservationDate());
            existingReservation.setStatus(updatedReservation.getStatus());
            return true;
        }
        return false;
    }

    // === Delete a reservation by ID ===
    public boolean deleteReservation(int reservationId) {
        return reservations.removeIf(reservation -> reservation.getReservationId() == reservationId);
    }

    // === Additional utility methods ===

    // Find reservations by member ID
    public List<Reservation> findReservationsByMemberId(int memberId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getMemberId() == memberId) {
                result.add(reservation);
            }
        }
        return result;
    }

    // Find reservations by book ID
    public List<Reservation> findReservationsByBookId(int bookId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getBookId() == bookId) {
                result.add(reservation);
            }
        }
        return result;
    }

    // Find reservations by status
    public List<Reservation> findReservationsByStatus(String status) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStatus().equalsIgnoreCase(status)) {
                result.add(reservation);
            }
        }
        return result;
    }
}
