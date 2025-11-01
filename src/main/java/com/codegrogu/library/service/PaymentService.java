package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Payment;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.repository.PaymentRepository;

/**
 * Service layer for managing payments in the library system.
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FineRepository fineRepository;

    public PaymentService(PaymentRepository paymentRepository, FineRepository fineRepository) {
        this.paymentRepository = paymentRepository;
        this.fineRepository = fineRepository;
    }

    // === Make a payment for a fine ===
    public boolean payFine(int fineId, double amount, LocalDate paymentDate, String paymentMethod) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        Optional<Fine> fineOpt = fineRepository.getFineById(fineId);
        if (fineOpt.isPresent()) {
            Fine fine = fineOpt.get();
            if (!fine.isPaid() && amount >= fine.getAmount()) {
                // Mark fine as paid
                fine.setPaid(true);
                fine.setPaymentDate(paymentDate);
                fineRepository.updateFine(fine);

                // Record the payment
                Payment payment = new Payment();
                payment.setPaymentId(generatePaymentId());
                payment.setFineId(fineId);
                payment.setMemberId(fine.getMemberId());
                payment.setAmount(amount);
                payment.setPaymentDate(paymentDate);
                payment.setPaymentMethod(paymentMethod.trim());

                paymentRepository.addPayment(payment);
                return true;
            }
        }
        return false; // Fine not found, already paid, or amount insufficient
    }

    // === Get all payments for a member ===
    public List<Payment> getPaymentsByMember(int memberId) {
        return paymentRepository.findPaymentsByMemberId(memberId);
    }

    // === Get all payments ===
    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

    // === Get payments for a specific fine ===
    public List<Payment> getPaymentsByFine(int fineId) {
        return paymentRepository.findPaymentsByFineId(fineId);
    }

    // === Generate unique payment ID ===
    private int generatePaymentId() {
        return paymentRepository.getAllPayments().stream()
                .mapToInt(Payment::getPaymentId)
                .max()
                .orElse(0) + 1;
    }
}
