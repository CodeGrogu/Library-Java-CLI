package com.codegrogu.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Librarian;

/**
 * In-memory repository for managing librarians in the library system.
 */
public class LibrarianRepository {

    private final List<Librarian> librarians = new ArrayList<>();

    // === Create / Add a librarian ===
    public void addLibrarian(Librarian librarian) {
        librarians.add(librarian);
    }

    // === Read / Get a librarian by ID ===
    public Optional<Librarian> getLibrarianById(int librarianId) {
        return librarians.stream()
                .filter(l -> l.getLibrarianId() == librarianId)
                .findFirst();
    }

    // === Read / Get all librarians ===
    public List<Librarian> getAllLibrarians() {
        return new ArrayList<>(librarians); // Return a copy to prevent external modification
    }

    // === Update a librarian ===
    public boolean updateLibrarian(Librarian updatedLibrarian) {
        Optional<Librarian> existingOpt = getLibrarianById(updatedLibrarian.getLibrarianId());
        if (existingOpt.isPresent()) {
            Librarian existing = existingOpt.get();
            existing.setName(updatedLibrarian.getName());
            existing.setEmail(updatedLibrarian.getEmail());
            existing.setPhoneNumber(updatedLibrarian.getPhoneNumber());
            existing.setActive(updatedLibrarian.isActive());
            return true;
        }
        return false;
    }

    // === Delete a librarian by ID ===
    public boolean deleteLibrarian(int librarianId) {
        return librarians.removeIf(l -> l.getLibrarianId() == librarianId);
    }

    // === Additional utility methods ===

    // Find librarians by name (partial match)
    public List<Librarian> findLibrariansByName(String name) {
        List<Librarian> result = new ArrayList<>();
        for (Librarian librarian : librarians) {
            if (librarian.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(librarian);
            }
        }
        return result;
    }

    // Find active librarians
    public List<Librarian> findActiveLibrarians() {
        List<Librarian> result = new ArrayList<>();
        for (Librarian librarian : librarians) {
            if (librarian.isActive()) {
                result.add(librarian);
            }
        }
        return result;
    }

    // Generate unique librarian ID
    public int generateLibrarianId() {
        return librarians.isEmpty() ? 1 : librarians.get(librarians.size() - 1).getLibrarianId() + 1;
    }
}
