package com.codegrogu.library.model;

import java.time.LocalDate;

public class Payment {
    private int paymentId;         // Unique payment identifier
    private int fineId;            // Fine being paid
    private int memberId;          // Who made the payment
    private double amountPaid;     // Amount paid
    private LocalDate paymentDate; // When payment was made
    private String paymentMethod;  // e.g., Cash, Card, Mobile Money
    private String reference;      // Optional transaction reference

    // Constructors
    public Payment() {}

    public Payment(int paymentId, int fineId, int memberId, double amountPaid,
                   LocalDate paymentDate, String paymentMethod, String reference) {
        this.paymentId = paymentId;
        this.fineId = fineId;
        this.memberId = memberId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public double getAmount() { return amountPaid; }
    public void setAmount(double amount) { this.amountPaid = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", fineId=" + fineId +
                ", memberId=" + memberId +
                ", amountPaid=" + amountPaid +
                ", paymentDate=" + paymentDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
