package com.codegrogu.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Payment;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.repository.PaymentRepository;

class PaymentServiceComprehensiveTest {

    private PaymentRepository paymentRepository;
    private FineRepository fineRepository;
    private PaymentService paymentService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        paymentRepository = new PaymentRepository();
        fineRepository = new FineRepository();
        paymentService = new PaymentService(paymentRepository, fineRepository);
    }

    @Test
    void payFineMarksFineAsPaidAndRecordsPayment() {
        Fine fine = storeFine(1, 10, false);
        LocalDate today = LocalDate.now();

        assertTrue(paymentService.payFine(fine.getFineId(), 10.0, today, "Cash"));

        Fine storedFine = fineRepository.getFineById(fine.getFineId()).orElseThrow();
        assertTrue(storedFine.isPaid());
        assertEquals(today, storedFine.getPaymentDate());

        List<Payment> payments = paymentRepository.getAllPayments();
        assertEquals(1, payments.size());
        Payment payment = payments.get(0);
        assertEquals(fine.getFineId(), payment.getFineId());
        assertEquals(fine.getMemberId(), payment.getMemberId());
        assertEquals(10.0, payment.getAmount());
        assertEquals(today, payment.getPaymentDate());
        assertEquals("Cash", payment.getPaymentMethod());
    }

    @Test
    void payFineRejectsInsufficientAmountOrAlreadyPaid() {
        Fine fine = storeFine(1, 15, false);
        LocalDate today = LocalDate.now();

        assertFalse(paymentService.payFine(fine.getFineId(), 10.0, today, "Card"));
        assertTrue(paymentRepository.getAllPayments().isEmpty());

        assertTrue(paymentService.payFine(fine.getFineId(), 15.0, today, "Card"));
        assertFalse(paymentService.payFine(fine.getFineId(), 15.0, today, "Card"));
    }

    @Test
    void payFineRejectsInvalidArguments() {
        Fine fine = storeFine(1, 5, false);
        LocalDate today = LocalDate.now();

        IllegalArgumentException nonPositive = assertThrows(IllegalArgumentException.class, () ->
            paymentService.payFine(fine.getFineId(), 0.0, today, "Cash")
        );
        assertTrue(nonPositive.getMessage().toLowerCase().contains("amount"));

        IllegalArgumentException missingMethod = assertThrows(IllegalArgumentException.class, () ->
            paymentService.payFine(fine.getFineId(), 5.0, today, " ")
        );
        assertTrue(missingMethod.getMessage().toLowerCase().contains("method"));
    }

    @Test
    void paymentQueriesReturnExpectedResults() {
        Fine first = storeFine(1, 8, false);
        Fine second = storeFine(2, 12, false);
        LocalDate today = LocalDate.now();

        assertTrue(paymentService.payFine(first.getFineId(), 8.0, today, "Cash"));
        assertTrue(paymentService.payFine(second.getFineId(), 12.0, today, "Card"));

        List<Payment> byMember = paymentService.getPaymentsByMember(first.getMemberId());
        assertEquals(1, byMember.size());
        assertEquals(first.getFineId(), byMember.get(0).getFineId());

        List<Payment> byFine = paymentService.getPaymentsByFine(second.getFineId());
        assertEquals(1, byFine.size());
        assertEquals(second.getFineId(), byFine.get(0).getFineId());

        List<Payment> all = paymentService.getAllPayments();
        assertEquals(2, all.size());
    }

    private Fine storeFine(int id, double amount, boolean paid) {
        Fine fine = new Fine();
        fine.setFineId(id);
        fine.setMemberId(100 + id);
        fine.setLoanId(200 + id);
        fine.setAmount(amount);
        fine.setReason("Late return");
        fine.setIssuedDate(LocalDate.now().minusDays(5));
        fine.setDueDate(LocalDate.now().plusDays(5));
        fine.setPaid(paid);
        fine.setPaymentDate(paid ? LocalDate.now().minusDays(1) : null);
        fineRepository.addFine(fine);
        return fine;
    }
}
