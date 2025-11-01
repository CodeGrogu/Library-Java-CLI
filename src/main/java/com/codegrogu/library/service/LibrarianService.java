package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.codegrogu.library.model.Librarian;
import com.codegrogu.library.model.LibrarianRole;
import com.codegrogu.library.repository.LibrarianRepository;

/**
 * Service layer for managing librarians in the library system.
 */
public class LibrarianService {

    private final LibrarianRepository librarianRepository;

    public LibrarianService(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    // === Add a new librarian ===
    public Librarian addLibrarian(String name, String email, String phoneNumber) {
        Librarian librarian = new Librarian();
        librarian.setName(name);
        librarian.setEmail(email);
        librarian.setPhoneNumber(phoneNumber);
        librarian.setActive(true);

        return persistNewLibrarian(librarian);
    }

    // === Add a new librarian with full details ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary) {
        Librarian librarian = new Librarian();
        librarian.setFirstName(firstName);
        librarian.setLastName(lastName);
        librarian.setGender(gender);
        librarian.setDateOfBirth(dateOfBirth);
        librarian.setEmail(email);
        librarian.setPhoneNumber(phoneNumber);
        librarian.setAddress(address);
        librarian.setPosition(position);
        librarian.setSalary(salary);
        librarian.setActive(true);

        return persistNewLibrarian(librarian);
    }

    // === Add a new librarian with full details including role ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary, LibrarianRole role) {
        Librarian librarian = new Librarian();
        librarian.setFirstName(firstName);
        librarian.setLastName(lastName);
        librarian.setGender(gender);
        librarian.setDateOfBirth(dateOfBirth);
        librarian.setEmail(email);
        librarian.setPhoneNumber(phoneNumber);
        librarian.setAddress(address);
        librarian.setPosition(position);
        librarian.setSalary(salary);
        librarian.setRole(role);
        librarian.setActive(true);

        return persistNewLibrarian(librarian);
    }

    // === Add a new librarian with full details including credentials ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary, LibrarianRole role, String username, String password) {
        Librarian librarian = addLibrarian(firstName, lastName, gender, dateOfBirth, email, phoneNumber, address, position, salary, role);
        librarian.setUsername(sanitize(username));
        librarian.setPasswordHash(sanitize(password)); // No hashing for simplicity
        librarian.setLastLoginDate(null);

        librarianRepository.updateLibrarian(librarian);
        return librarian;
    }

    // === Activate a librarian account ===
    public boolean activateLibrarian(int librarianId) {
        Optional<Librarian> librarianOpt = librarianRepository.getLibrarianById(librarianId);
        if (librarianOpt.isPresent()) {
            Librarian librarian = librarianOpt.get();
            librarian.setActive(true);
            return librarianRepository.updateLibrarian(librarian);
        }
        return false;
    }

    // === Deactivate a librarian account ===
    public boolean deactivateLibrarian(int librarianId) {
        Optional<Librarian> librarianOpt = librarianRepository.getLibrarianById(librarianId);
        if (librarianOpt.isPresent()) {
            Librarian librarian = librarianOpt.get();
            librarian.setActive(false);
            return librarianRepository.updateLibrarian(librarian);
        }
        return false;
    }

    // === Get a librarian by ID ===
    public Optional<Librarian> getLibrarianById(int librarianId) {
        return librarianRepository.getLibrarianById(librarianId);
    }

    // === Get all librarians ===
    public List<Librarian> getAllLibrarians() {
        return librarianRepository.getAllLibrarians();
    }

    // === Update a librarian ===
    public boolean updateLibrarian(Librarian librarian) {
        if (librarian == null) {
            throw new IllegalArgumentException("Librarian must not be null");
        }
        if (librarian.getLibrarianId() <= 0) {
            throw new IllegalArgumentException("Librarian ID must be positive");
        }
        if (librarianRepository.getLibrarianById(librarian.getLibrarianId()).isEmpty()) {
            throw new IllegalArgumentException("Librarian not found");
        }
        sanitizeLibrarian(librarian);
    validateLibrarian(librarian);
        return librarianRepository.updateLibrarian(librarian);
    }

    // === Delete a librarian by ID ===
    public boolean deleteLibrarian(int librarianId) {
        return librarianRepository.deleteLibrarian(librarianId);
    }

    // === Search librarians by name ===
    public List<Librarian> searchLibrariansByName(String name) {
        return librarianRepository.findLibrariansByName(name);
    }

    // === Get all active librarians ===
    public List<Librarian> getActiveLibrarians() {
        return librarianRepository.findActiveLibrarians();
    }

    private Librarian persistNewLibrarian(Librarian librarian) {
        sanitizeLibrarian(librarian);
    validateLibrarian(librarian);
        librarian.setLibrarianId(librarianRepository.generateLibrarianId());
        librarian.setStaffCode(generateStaffCode(librarian.getLibrarianId()));
        if (librarian.getDateHired() == null) {
            librarian.setDateHired(LocalDate.now());
        }
        if (librarian.getRole() == null) {
            librarian.setRole(LibrarianRole.STAFF);
        }
        librarianRepository.addLibrarian(librarian);
        return librarian;
    }

    private void sanitizeLibrarian(Librarian librarian) {
        librarian.setFirstName(sanitize(librarian.getFirstName()));
        librarian.setLastName(sanitize(librarian.getLastName()));
        librarian.setGender(sanitize(librarian.getGender()));
        librarian.setEmail(sanitize(librarian.getEmail()));
        librarian.setPhoneNumber(sanitize(librarian.getPhoneNumber()));
        librarian.setAddress(sanitize(librarian.getAddress()));
        librarian.setPosition(sanitize(librarian.getPosition()));
        librarian.setUsername(sanitize(librarian.getUsername()));
        if (librarian.getRole() == null) {
            librarian.setRole(LibrarianRole.STAFF);
        }
        if (librarian.getPasswordHash() != null) {
            librarian.setPasswordHash(librarian.getPasswordHash().trim());
        }
    }

    private void validateLibrarian(Librarian librarian) {
        if (librarian.getFirstName() == null || librarian.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (librarian.getLastName() == null || librarian.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (librarian.getEmail() == null || librarian.getEmail().isBlank() || !librarian.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (librarian.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        if (librarian.getDateOfBirth() != null && librarian.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        if (librarian.getRole() == null) {
            librarian.setRole(LibrarianRole.STAFF);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String generateStaffCode(int librarianId) {
        return String.format(Locale.ROOT, "LIB%03d", librarianId);
    }
}