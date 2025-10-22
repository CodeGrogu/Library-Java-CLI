package com.codegrogu.library.model;

import java.time.LocalDate;

public class Fine {
    private int fineId;            // Unique identifier for each fine
    private int memberId;          // Member responsible for the fine
    private int loanId;            // The loan that caused the fine
    private double amount;         // Fine amount in currency
    private String reason;         // Reason for fine: "Late return", "Damaged book", etc.
    private LocalDate issuedDate;  // When fine was created
    private LocalDate dueDate;     // When payment is expected
    private String status;         // UNPAID, PAID, WAIVED

    // Constructors
    public Fine() {}

    public Fine(int fineId, int memberId, int loanId, double amount, String reason,
                LocalDate issuedDate, LocalDate dueDate, String status) {
        this.fineId = fineId;
        this.memberId = memberId;
        this.loanId = loanId;
        this.amount = amount;
        this.reason = reason;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and Setters
    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Fine{" +
                "fineId=" + fineId +
                ", memberId=" + memberId +
                ", loanId=" + loanId +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                ", issuedDate=" + issuedDate +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                '}';
    }
}
