package com.codegrogu.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Fine;

/**
 * In-memory repository for managing fines.
 */
public class FineRepository {

    private final List<Fine> fines = new ArrayList<>();

    // === Create / Add a fine ===
    public void addFine(Fine fine) {
        fines.add(fine);
    }

    // === Read / Get a fine by ID ===
    public Optional<Fine> getFineById(int fineId) {
        return fines.stream()
                .filter(fine -> fine.getFineId() == fineId)
                .findFirst();
    }

    // === Read / Get all fines ===
    public List<Fine> getAllFines() {
        return new ArrayList<>(fines); // Return a copy to prevent external modification
    }

    // === Update a fine ===
    public boolean updateFine(Fine updatedFine) {
        Optional<Fine> existingFineOpt = getFineById(updatedFine.getFineId());
        if (existingFineOpt.isPresent()) {
            Fine existingFine = existingFineOpt.get();
            existingFine.setMemberId(updatedFine.getMemberId());
            existingFine.setLoanId(updatedFine.getLoanId());
            existingFine.setAmount(updatedFine.getAmount());
            existingFine.setReason(updatedFine.getReason());
            existingFine.setIssuedDate(updatedFine.getIssuedDate());
            existingFine.setDueDate(updatedFine.getDueDate());
            existingFine.setPaid(updatedFine.isPaid());
            existingFine.setPaymentDate(updatedFine.getPaymentDate());
            return true;
        }
        return false;
    }

    // === Delete a fine by ID ===
    public boolean deleteFine(int fineId) {
        return fines.removeIf(fine -> fine.getFineId() == fineId);
    }

    // === Additional utility methods ===

    // Find fines by member ID
    public List<Fine> findFinesByMemberId(int memberId) {
        List<Fine> result = new ArrayList<>();
        for (Fine fine : fines) {
            if (fine.getMemberId() == memberId) {
                result.add(fine);
            }
        }
        return result;
    }

    // Find unpaid fines
    public List<Fine> getUnpaidFines() {
        List<Fine> result = new ArrayList<>();
        for (Fine fine : fines) {
            if (!fine.isPaid()) {
                result.add(fine);
            }
        }
        return result;
    }
}
