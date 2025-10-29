package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Member;
import com.codegrogu.library.repository.MemberRepository;

/**
 * Service layer for managing library members.
 */
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // === Register a new member ===
    public Member registerMember(String name, String email, String phoneNumber) {
        Member member = new Member();
        member.setMemberId(generateMemberId());
        member.setName(name);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setMembershipDate(LocalDate.now());
        member.setActive(true);

        memberRepository.addMember(member);
        return member;
    }

    // === Register a new member with full details ===
    public Member registerMember(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address) {
        Member member = new Member();
        member.setMemberId(generateMemberId());
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setGender(gender);
        member.setDateOfBirth(dateOfBirth);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);
        member.setDateJoined(LocalDate.now());
        member.setActive(true);
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);

        memberRepository.addMember(member);
        return member;
    }

    // === Register a new member with full details and member type ===
    public Member registerMember(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String memberType) {
        Member member = new Member();
        member.setMemberId(generateMemberId());
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setGender(gender);
        member.setDateOfBirth(dateOfBirth);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);
        member.setMemberType(Member.MemberType.valueOf(memberType));
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        member.setDateJoined(LocalDate.now());
        member.setActive(true);

        memberRepository.addMember(member);
        return member;
    }

    // === Activate a member account ===
    public boolean activateMember(int memberId) {
        Optional<Member> memberOpt = memberRepository.getMemberById(memberId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setActive(true);
            return memberRepository.updateMember(member);
        }
        return false;
    }

    // === Deactivate a member account ===
    public boolean deactivateMember(int memberId) {
        Optional<Member> memberOpt = memberRepository.getMemberById(memberId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setActive(false);
            return memberRepository.updateMember(member);
        }
        return false;
    }

    // === Get a member by ID ===
    public Optional<Member> getMemberById(int memberId) {
        return memberRepository.getMemberById(memberId);
    }

    // === Update a member ===
    public boolean updateMember(Member member) {
        return memberRepository.updateMember(member);
    }

    // === Delete a member by ID ===
    public boolean deleteMember(int memberId) {
        return memberRepository.deleteMember(memberId);
    }

    // === Get all members ===
    public List<Member> getAllMembers() {
        return memberRepository.getAllMembers();
    }

    // === Search members by name ===
    public List<Member> searchMembersByName(String name) {
        return memberRepository.findMembersByName(name);
    }

    // === Get all active members ===
    public List<Member> getActiveMembers() {
        return memberRepository.findActiveMembers();
    }

    // === Get members by type ===
    public List<Member> getMembersByType(String type) {
        return memberRepository.getAllMembers().stream()
                .filter(m -> m.getMemberType() != null && type.equalsIgnoreCase(m.getMemberType().toString()))
                .collect(java.util.stream.Collectors.toList());
    }

    // === Utility: generate unique member ID ===
    private int generateMemberId() {
        List<Member> allMembers = memberRepository.getAllMembers();
        return allMembers.isEmpty() ? 1 : allMembers.get(allMembers.size() - 1).getMemberId() + 1;
    }
}
