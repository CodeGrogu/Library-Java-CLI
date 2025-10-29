package com.codegrogu.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Loan;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.util.ConfigUtil;

/**
 * Service layer for managing fines in the library system.
 */
public class FineService {

    private final FineRepository fineRepository;

    // Fine rate per day loaded from configuration
    private final double fineRatePerDay;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
        this.fineRatePerDay = ConfigUtil.getDoubleProperty("library.finePerDay", 5.0);
    }

    // === Calculate fine for a specific loan ===
    public void calculateFine(Loan loan) {
        double amount = calculateFineAmount(loan);
        if (amount > 0) {
            createFineForLoan(loan);
        }
    }

    // === Calculate fine amount ===
    public double calculateFineAmount(Loan loan) {
        LocalDate dueDate = loan.getDueDate();
        LocalDate returnDate = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDate.now();

        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (overdueDays > 0) {
            return overdueDays * fineRatePerDay;
        }
        return 0.0;
    }

    // === Create fine for a loan if overdue ===
    public void createFineForLoan(Loan loan) {
        double amount = calculateFineAmount(loan);
        if (amount > 0) {
            Fine fine = new Fine();
            fine.setFineId(generateFineId());
            fine.setLoanId(loan.getLoanId());
            fine.setMemberId(loan.getMemberId());
            fine.setAmount(amount);
            fine.setReason("Late return");
            fine.setIssuedDate(LocalDate.now());
            fine.setDueDate(loan.getDueDate().plusDays(7)); // 7 days to pay fine
            fine.setPaid(false); // This sets status to "UNPAID"
            fine.setPaymentDate(null);

            fineRepository.addFine(fine);
        }
    }

    // === Mark fine as paid ===
    public boolean payFine(int fineId, LocalDate paymentDate) {
        Optional<Fine> fineOpt = fineRepository.getFineById(fineId);
        if (fineOpt.isPresent()) {
            Fine fine = fineOpt.get();
            if (!fine.isPaid()) {
                fine.setPaid(true);
                fine.setPaymentDate(paymentDate);
                fineRepository.updateFine(fine);
                return true;
            }
        }
        return false;
    }

    // === Get all fines for a member ===
    public List<Fine> getFinesByMember(int memberId) {
        return fineRepository.findFinesByMemberId(memberId);
    }

    // === Get all unpaid fines ===
    public List<Fine> getUnpaidFines() {
        return fineRepository.getUnpaidFines();
    }

    // === Get all fines ===
    public List<Fine> getAllFines() {
        return fineRepository.getAllFines();
    }

    // === Delete a fine by ID ===
    public boolean deleteFine(int fineId) {
        return fineRepository.deleteFine(fineId);
    }

    // === Utility: generate unique fine ID ===
    private int generateFineId() {
        List<Fine> allFines = fineRepository.getAllFines();
        return allFines.isEmpty() ? 1 : allFines.get(allFines.size() - 1).getFineId() + 1;
    }

    // === Check if a member has unpaid fines ===
    public boolean hasUnpaidFines(int memberId) {
        List<Fine> fines = fineRepository.getAllFines();
        return fines.stream()
                .anyMatch(f -> f.getMemberId() == memberId && !f.isPaid());
    }
}
