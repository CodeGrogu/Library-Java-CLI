package com.codegrogu.library.model;

import java.time.LocalDate;

/**
 * Represents a librarian or staff member responsible for managing
 * library operations such as cataloging, issuing, and returning books.
 */
public class Librarian {

    // === Identification Attributes ===
    private int librarianId;             // Unique staff ID
    private String staffCode;            // e.g., "LIB001", useful for internal reference

    // === Personal Information ===
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;

    // === Contact Information ===
    private String email;
    private String phoneNumber;
    private String address;

    // === Employment Details ===
    private LocalDate dateHired;
    private String position;             // e.g., "Administrator", "Assistant", "Archivist"
    private double salary;
    private boolean isActive;            // Whether the librarian is currently employed
    private LibrarianRole role;          // ADMIN, STAFF, INTERN

    // === System Access Information ===
    private String username;             // Login username
    private String passwordHash;         // Encrypted or hashed password
    private LocalDate lastLoginDate;     // For auditing and activity logs

    // === Constructors ===
    public Librarian() {
        // Empty constructor for frameworks
    }

    public Librarian(int librarianId, String staffCode, String firstName, String lastName, String gender,
                     LocalDate dateOfBirth, String email, String phoneNumber, String address,
