package com.codegrogu.library.service;

import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Payment;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.repository.PaymentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
                payment.setPaymentMethod(paymentMethod);

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
        List<Payment> allPayments = paymentRepository.getAllPayments();
        return allPayments.isEmpty() ? 1 : allPayments.get(allPayments.size() - 1).getPaymentId() + 1;
    }
}
