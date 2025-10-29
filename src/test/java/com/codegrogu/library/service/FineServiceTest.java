package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Loan;
import com.codegrogu.library.repository.FineRepository;

/**
 * Unit tests for FineService
 */
class FineServiceTest {

    private FineService fineService;
    private FineRepository fineRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    public void setup() {
        fineRepository = new FineRepository();
        fineService = new FineService(fineRepository);
    }

    @Test
    void testCalculateFineAmount_NoOverdue() {
        // Given
        Loan loan = createLoan(LocalDate.now().minusDays(5), LocalDate.now().minusDays(2));

        // When
        double fineAmount = fineService.calculateFineAmount(loan);

        // Then
        assertEquals(0.0, fineAmount);
    }

    @Test
    void testCalculateFineAmount_Overdue() {
        // Given
        Loan loan = createLoan(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        // This should be 8 days overdue (10 - 2 = 8)

        // When
        double fineAmount = fineService.calculateFineAmount(loan);

        // Then
        assertEquals(2.5, fineAmount); // 1 day * 2.5 per day (from config)
    }

    @Test
    void testCreateFineForLoan() {
        // Given
        Loan loan = createLoan(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));

        // When
        fineService.createFineForLoan(loan);

        // Then
        List<Fine> allFines = fineService.getAllFines();
        assertEquals(1, allFines.size());

        Fine fine = allFines.get(0);
        assertEquals(1, fine.getFineId());
        assertEquals(loan.getLoanId(), fine.getLoanId());
        assertEquals(loan.getMemberId(), fine.getMemberId());
        assertEquals(2.5, fine.getAmount());
        assertEquals("Late return", fine.getReason());
        assertNotNull(fine.getIssuedDate());
        assertNotNull(fine.getDueDate());
        assertEquals("UNPAID", fine.getStatus());
    }

    @Test
    void testPayFine() {
        // Given
        Loan loan = createLoan(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        fineService.createFineForLoan(loan);
        Fine fine = fineService.getAllFines().get(0);
        LocalDate paymentDate = LocalDate.now();

        // When
        boolean result = fineService.payFine(fine.getFineId(), paymentDate);

        // Then
        assertTrue(result);
        // Verify the fine is marked as paid by checking unpaid fines
        List<Fine> unpaidFines = fineService.getUnpaidFines();
        assertFalse(unpaidFines.stream().anyMatch(f -> f.getFineId() == fine.getFineId()));
    }

    @Test
    void testHasUnpaidFines() {
        // Given
        Loan loan = createLoan(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        fineService.createFineForLoan(loan);

        // When & Then
        assertTrue(fineService.hasUnpaidFines(loan.getMemberId()));
    }

    private Loan createLoan(LocalDate loanDate, LocalDate returnDate) {
        Loan loan = new Loan();
        loan.setLoanId(1);
        loan.setBookId(1);
        loan.setMemberId(1);
        loan.setLoanDate(loanDate);
        loan.setDueDate(loanDate.plusDays(7));
        loan.setReturnDate(returnDate);
        loan.setReturned(true);
        return loan;
    }
}