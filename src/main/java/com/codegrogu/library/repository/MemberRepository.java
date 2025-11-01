package com.codegrogu.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Member;

/**
 * In-memory repository for managing library members.
 */
public class MemberRepository {

    private final List<Member> members = new ArrayList<>();

    // === Create / Add a member ===
    public void addMember(Member member) {
        members.add(member);
    }

    // === Read / Get a member by ID ===
    public Optional<Member> getMemberById(int memberId) {
        return members.stream()
                .filter(member -> member.getMemberId() == memberId)
                .findFirst();
    }

    // === Read / Get all members ===
    public List<Member> getAllMembers() {
        return new ArrayList<>(members); // Return a copy to prevent external modification
    }

    // === Update a member ===
    public boolean updateMember(Member updatedMember) {
        Optional<Member> existingMemberOpt = getMemberById(updatedMember.getMemberId());
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            existingMember.setFirstName(updatedMember.getFirstName());
            existingMember.setLastName(updatedMember.getLastName());
            existingMember.setGender(updatedMember.getGender());
            existingMember.setDateOfBirth(updatedMember.getDateOfBirth());
            existingMember.setEmail(updatedMember.getEmail());
            existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
            existingMember.setAddress(updatedMember.getAddress());
            existingMember.setMemberType(updatedMember.getMemberType());
            existingMember.setCardNumber(updatedMember.getCardNumber());
            existingMember.setMembershipDate(updatedMember.getMembershipDate());
            existingMember.setDateJoined(updatedMember.getDateJoined());
            existingMember.setActive(updatedMember.isActive());
            existingMember.setOutstandingFines(updatedMember.getOutstandingFines());
            existingMember.setMembershipStatus(updatedMember.getMembershipStatus());
            existingMember.setBorrowedBookIds(updatedMember.getBorrowedBookIds() != null
                    ? new ArrayList<>(updatedMember.getBorrowedBookIds())
                    : new ArrayList<>());
            existingMember.setTotalBooksBorrowed(updatedMember.getTotalBooksBorrowed());
            return true;
        }
        return false;
    }

    // === Delete a member by ID ===
    public boolean deleteMember(int memberId) {
        return members.removeIf(member -> member.getMemberId() == memberId);
    }

    // === Additional utility methods ===

    // Find members by name (partial match)
    public List<Member> findMembersByName(String name) {
        List<Member> result = new ArrayList<>();
        for (Member member : members) {
            if (member.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(member);
            }
        }
        return result;
    }

    // Find active members
    public List<Member> findActiveMembers() {
        List<Member> result = new ArrayList<>();
        for (Member member : members) {
            if (member.isActive()) {
                result.add(member);
            }
        }
        return result;
    }

    public boolean existsByEmail(String email, Integer ignoreMemberId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return members.stream()
                .anyMatch(member -> email.equalsIgnoreCase(member.getEmail())
                        && (ignoreMemberId == null || member.getMemberId() != ignoreMemberId));
    }
}
