package com.codegrogu.library.model;

import java.time.LocalDate;

/**
 * Represents a book loan transaction in the library system,
 * linking a Book and a Member along with loan and return details.
 */
public class Loan {

    // === Identification Attributes ===
    private int loanId;                     // Unique loan transaction ID

    // === Relational Attributes ===
    private int bookId;                     // ID of the borrowed book
    private int memberId;                   // ID of the borrowing member

    // === Loan Details ===
    private LocalDate issueDate;            // Date when the book was borrowed
    private LocalDate dueDate;              // Date when the book is due for return
    private LocalDate returnDate;           // Actual return date, if returned
    private LoanStatus status;              // ACTIVE, RETURNED, OVERDUE, LOST
    private double fineAmount;              // Fine for late return or loss

    // === Constructors ===
    public Loan() {
        // Empty constructor for frameworks
    }

    public Loan(int loanId, int bookId, int memberId,
                LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                LoanStatus status, double fineAmount) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
    }

    // === Getters and Setters ===
    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    // === Utility Methods ===

    /**
     * Checks if the loan is overdue relative to today's date.
     */
    public boolean isOverdue() {
        return (status == LoanStatus.ACTIVE) && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the fine based on days overdue and rate per day.
     * @param dailyRate The fine rate per day.
     */
    public void calculateFine(double dailyRate) {
        if (isOverdue()) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            fineAmount = daysOverdue * dailyRate;
        } else {
            fineAmount = 0;
        }
    }

    /**
     * Marks the loan as returned and updates relevant fields.
     */
    public void markAsReturned() {
        this.status = LoanStatus.RETURNED;
        this.returnDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "Loan {" +
                "loanId=" + loanId +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                ", fineAmount=" + fineAmount +
                '}';
    }

    // === Enum Types ===
    public enum LoanStatus {
        ACTIVE, RETURNED, OVERDUE, LOST
    }
}
