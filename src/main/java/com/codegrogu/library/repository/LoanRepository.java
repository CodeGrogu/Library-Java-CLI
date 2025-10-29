package com.codegrogu.library.repository;

import com.codegrogu.library.model.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for managing loans.
 */
public class LoanRepository {

    private final List<Loan> loans = new ArrayList<>();

    // === Create / Add a loan ===
    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    // === Read / Get a loan by ID ===
    public Optional<Loan> getLoanById(int loanId) {
        return loans.stream()
                .filter(loan -> loan.getLoanId() == loanId)
                .findFirst();
    }

    // === Read / Get all loans ===
    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans); // Return a copy to prevent external modification
    }

    // === Update a loan ===
    public boolean updateLoan(Loan updatedLoan) {
        Optional<Loan> existingLoanOpt = getLoanById(updatedLoan.getLoanId());
        if (existingLoanOpt.isPresent()) {
            Loan existingLoan = existingLoanOpt.get();
            existingLoan.setBookId(updatedLoan.getBookId());
            existingLoan.setMemberId(updatedLoan.getMemberId());
            existingLoan.setLoanDate(updatedLoan.getLoanDate());
            existingLoan.setDueDate(updatedLoan.getDueDate());
            existingLoan.setReturnDate(updatedLoan.getReturnDate());
            existingLoan.setReturned(updatedLoan.isReturned());
            return true;
        }
        return false;
    }

    // === Delete a loan by ID ===
    public boolean deleteLoan(int loanId) {
        return loans.removeIf(loan -> loan.getLoanId() == loanId);
    }

    // === Additional utility methods ===

    // Find loans by member ID
    public List<Loan> findLoansByMemberId(int memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getMemberId() == memberId) {
                result.add(loan);
            }
        }
        return result;
    }

    // Find loans by book ID
    public List<Loan> findLoansByBookId(int bookId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getBookId() == bookId) {
                result.add(loan);
            }
        }
        return result;
    }

    // Find active loans (not yet returned)
    public List<Loan> findActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                result.add(loan);
            }
        }
        return result;
    }
}
