package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;
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
        librarian.setLibrarianId(librarianRepository.generateLibrarianId());
        librarian.setName(name);
        librarian.setEmail(email);
        librarian.setPhoneNumber(phoneNumber);
        librarian.setActive(true);

        librarianRepository.addLibrarian(librarian);
        return librarian;
    }

    // === Add a new librarian with full details ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary) {
        Librarian librarian = new Librarian();
        librarian.setLibrarianId(librarianRepository.generateLibrarianId());
        librarian.setFirstName(firstName);
        librarian.setLastName(lastName);
        librarian.setGender(gender);
        librarian.setDateOfBirth(dateOfBirth);
        librarian.setEmail(email);
        librarian.setPhoneNumber(phoneNumber);
        librarian.setAddress(address);
        librarian.setPosition(position);
        librarian.setSalary(salary);
        librarian.setDateHired(LocalDate.now());
        librarian.setActive(true);

        librarianRepository.addLibrarian(librarian);
        return librarian;
    }

    // === Add a new librarian with full details including role ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary, LibrarianRole role) {
        Librarian librarian = new Librarian();
        librarian.setLibrarianId(librarianRepository.generateLibrarianId());
        librarian.setStaffCode("LIB" + String.format("%03d", librarian.getLibrarianId()));
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
        librarian.setDateHired(LocalDate.now());
        librarian.setActive(true);

        librarianRepository.addLibrarian(librarian);
        return librarian;
    }

    // === Add a new librarian with full details including credentials ===
    public Librarian addLibrarian(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String position, double salary, LibrarianRole role, String username, String password) {
        Librarian librarian = addLibrarian(firstName, lastName, gender, dateOfBirth, email, phoneNumber, address, position, salary, role);
        librarian.setUsername(username);
        librarian.setPasswordHash(password); // No hashing for simplicity
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
}