package com.codegrogu.library.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a library member, including their personal details,
 * contact information, account status, and borrowing history.
 */
public class Member {

    // === Identification Attributes ===
    private int memberId;                  // Unique library member ID
    private String cardNumber;             // Physical or digital library card number
    private MemberType memberType;         // STUDENT, TEACHER, PUBLIC, etc.

    // === Personal Information ===
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;

    // === Contact Information ===
    private String email;
    private String phoneNumber;
    private String address;

    // === Account Information ===
    private LocalDate dateJoined;
    private boolean isActive;              // Account active or suspended
    private double outstandingFines;       // Amount due for late returns, damages, etc.
    private MembershipStatus membershipStatus; // ACTIVE, SUSPENDED, EXPIRED

    // === Borrowing History ===
    private List<Integer> borrowedBookIds; // IDs of currently borrowed books
    private int totalBooksBorrowed;        // Total number of books ever borrowed

    // === Constructors ===
    public Member() {
        // Empty constructor for frameworks and serialization
    }

    public Member(int memberId, String cardNumber, MemberType memberType,
                  String firstName, String lastName, String gender, LocalDate dateOfBirth,
                  String email, String phoneNumber, String address,
                  LocalDate dateJoined, boolean isActive, double outstandingFines,
                  MembershipStatus membershipStatus, List<Integer> borrowedBookIds,
                  int totalBooksBorrowed) {
        this.memberId = memberId;
        this.cardNumber = cardNumber;
        this.memberType = memberType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateJoined = dateJoined;
        this.isActive = isActive;
        this.outstandingFines = outstandingFines;
        this.membershipStatus = membershipStatus;
        this.borrowedBookIds = borrowedBookIds;
        this.totalBooksBorrowed = totalBooksBorrowed;
    }

    // === Getters and Setters ===
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public MemberType getMemberType() { return memberType; }
    public void setMemberType(MemberType memberType) { this.memberType = memberType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

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

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public double getOutstandingFines() { return outstandingFines; }
    public void setOutstandingFines(double outstandingFines) { this.outstandingFines = outstandingFines; }

    public MembershipStatus getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(MembershipStatus membershipStatus) { this.membershipStatus = membershipStatus; }

    public List<Integer> getBorrowedBookIds() { return borrowedBookIds; }
    public void setBorrowedBookIds(List<Integer> borrowedBookIds) { this.borrowedBookIds = borrowedBookIds; }

    public int getTotalBooksBorrowed() { return totalBooksBorrowed; }
    public void setTotalBooksBorrowed(int totalBooksBorrowed) { this.totalBooksBorrowed = totalBooksBorrowed; }

    // === Utility Methods ===
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addFine(double amount) {
        this.outstandingFines += amount;
    }

    public void payFine(double amount) {
        this.outstandingFines = Math.max(0, this.outstandingFines - amount);
    }

    public void incrementBorrowCount() {
        this.totalBooksBorrowed++;
    }

    @Override
    public String toString() {
        return "Member {" +
                "memberId=" + memberId +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", active=" + isActive +
                ", fines=" + outstandingFines +
                ", totalBooksBorrowed=" + totalBooksBorrowed +
                ", membershipStatus=" + membershipStatus +
                '}';
    }

    // === Enum Types ===
    public enum MemberType {
        STUDENT, TEACHER, PUBLIC, STAFF
    }

    public enum MembershipStatus {
        ACTIVE, SUSPENDED, EXPIRED
    }
}
