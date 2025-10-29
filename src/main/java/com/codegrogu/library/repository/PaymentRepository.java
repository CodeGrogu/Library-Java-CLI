package com.codegrogu.library.repository;

import com.codegrogu.library.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for managing payments.
 */
public class PaymentRepository {

    private final List<Payment> payments = new ArrayList<>();

    // === Create / Add a payment ===
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    // === Read / Get a payment by ID ===
    public Optional<Payment> getPaymentById(int paymentId) {
        return payments.stream()
                .filter(payment -> payment.getPaymentId() == paymentId)
                .findFirst();
    }

    // === Read / Get all payments ===
    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments); // Return a copy to prevent external modification
    }

    // === Update a payment ===
    public boolean updatePayment(Payment updatedPayment) {
        Optional<Payment> existingPaymentOpt = getPaymentById(updatedPayment.getPaymentId());
        if (existingPaymentOpt.isPresent()) {
            Payment existingPayment = existingPaymentOpt.get();
            existingPayment.setFineId(updatedPayment.getFineId());
            existingPayment.setMemberId(updatedPayment.getMemberId());
            existingPayment.setAmount(updatedPayment.getAmount());
            existingPayment.setPaymentDate(updatedPayment.getPaymentDate());
            existingPayment.setPaymentMethod(updatedPayment.getPaymentMethod());
            return true;
        }
        return false;
    }

    // === Delete a payment by ID ===
    public boolean deletePayment(int paymentId) {
        return payments.removeIf(payment -> payment.getPaymentId() == paymentId);
    }

    // === Additional utility methods ===

    // Get payments by member ID
    public List<Payment> findPaymentsByMemberId(int memberId) {
        List<Payment> result = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getMemberId() == memberId) {
                result.add(payment);
            }
        }
        return result;
    }

    // Get payments by fine ID
    public List<Payment> findPaymentsByFineId(int fineId) {
        List<Payment> result = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getFineId() == fineId) {
                result.add(payment);
            }
        }
        return result;
    }
}
