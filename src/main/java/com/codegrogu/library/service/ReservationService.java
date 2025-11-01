package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.model.Reservation;
import com.codegrogu.library.repository.BookRepository;
import com.codegrogu.library.repository.ReservationRepository;

/**
 * Service layer for managing book reservations in the library system.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
    }

    // === Create a new reservation ===
    public boolean createReservation(int bookId, int memberId) {
        Optional<Book> bookOpt = bookRepository.getBookById(bookId);
        if (bookOpt.isEmpty()) {
            return false;
        }
        Book book = bookOpt.get();
        if (book.isAvailable()) {
            return false; // Encourage immediate checkout instead
        }

        boolean alreadyQueued = reservationRepository.findReservationsByBookId(bookId).stream()
                .anyMatch(r -> r.getMemberId() == memberId && "PENDING".equalsIgnoreCase(r.getStatus()));
        if (alreadyQueued) {
            return false;
        }

        Reservation reservation = new Reservation();
        reservation.setReservationId(generateReservationId());
        reservation.setBookId(bookId);
        reservation.setMemberId(memberId);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus("Pending");

        reservationRepository.addReservation(reservation);
        return true;
    }

    // === Fulfill a reservation (book becomes available for member) ===
    public boolean fulfillReservation(int reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.getReservationById(reservationId);
        if (reservationOpt.isEmpty()) {
            return false;
        }
        Reservation reservation = reservationOpt.get();
        if (!"FULFILLED".equalsIgnoreCase(reservation.getStatus())) {
            reservation.setStatus("Fulfilled");
            reservationRepository.updateReservation(reservation);
        }
        return true;
    }

    // === Cancel a reservation ===
    public boolean cancelReservation(int reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.getReservationById(reservationId);
        if (reservationOpt.isEmpty()) {
            return false;
        }
        Reservation reservation = reservationOpt.get();
        reservation.setStatus("Cancelled");
        return reservationRepository.updateReservation(reservation);
    }

    // === Get reservations for a member ===
    public List<Reservation> getReservationsByMember(int memberId) {
        return reservationRepository.findReservationsByMemberId(memberId);
    }

    // === Get reservations for a book ===
    public List<Reservation> getReservationsByBook(int bookId) {
        return reservationRepository.findReservationsByBookId(bookId);
    }

    // === Get reservations by status ===
    public List<Reservation> getReservationsByStatus(String status) {
        return reservationRepository.findReservationsByStatus(status);
    }

    // === Get all reservations ===
    public List<Reservation> getAllReservations() {
        return reservationRepository.getAllReservations();
    }

    // === Delete a reservation by ID ===
    public boolean deleteReservation(int reservationId) {
        return reservationRepository.deleteReservation(reservationId);
    }

    // === Get next reservation for a book ===
    public Reservation getNextReservation(int bookId) {
        List<Reservation> reservations = reservationRepository.findReservationsByBookId(bookId);
        return reservations.stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .findFirst()
                .orElse(null);
    }

    // === Utility: generate unique reservation ID ===
    private int generateReservationId() {
        return reservationRepository.getAllReservations().stream()
                .mapToInt(Reservation::getReservationId)
                .max()
                .orElse(0) + 1;
    }
}
