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
                     LocalDate dateHired, String position, double salary, boolean isActive,
                     LibrarianRole role, String username, String passwordHash, LocalDate lastLoginDate) {
        this.librarianId = librarianId;
        this.staffCode = staffCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateHired = dateHired;
        this.position = position;
        this.salary = salary;
        this.isActive = isActive;
        this.role = role;
        this.username = username;
        this.passwordHash = passwordHash;
        this.lastLoginDate = lastLoginDate;
    }

    // Getters and Setters
    public int getLibrarianId() { return librarianId; }
    public void setLibrarianId(int librarianId) { this.librarianId = librarianId; }

    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getName() { return firstName + " " + lastName; }
    public void setName(String name) {
        String[] parts = name.split(" ", 2);
        if (parts.length >= 1) this.firstName = parts[0];
        if (parts.length >= 2) this.lastName = parts[1];
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateHired() { return dateHired; }
    public void setDateHired(LocalDate dateHired) { this.dateHired = dateHired; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LibrarianRole getRole() { return role; }
    public void setRole(LibrarianRole role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDate getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDate lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    @Override
    public String toString() {
        return "Librarian{" +
                "librarianId=" + librarianId +
                ", name='" + getName() + '\'' +
                ", position='" + position + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
