package com.codegrogu.library.model;

import java.time.LocalDate;

public class Reservation {
    private int reservationId;      // Unique ID for the reservation
    private int bookId;             // ID of the reserved book
    private int memberId;           // ID of the member who made the reservation
    private LocalDate reservationDate; // When the reservation was made
    private String status;          // e.g., Pending, Fulfilled, Cancelled

    // Constructors
    public Reservation() {}

    public Reservation(int reservationId, int bookId, int memberId, LocalDate reservationDate, String status) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Utility Method
    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", reservationDate=" + reservationDate +
                ", status='" + status + '\'' +
                '}';
    }
}
