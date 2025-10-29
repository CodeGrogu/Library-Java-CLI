package com.codegrogu.library.repository;

import com.codegrogu.library.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            existingMember.setName(updatedMember.getName());
            existingMember.setEmail(updatedMember.getEmail());
            existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
            existingMember.setMembershipDate(updatedMember.getMembershipDate());
            existingMember.setActive(updatedMember.isActive());
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
}
