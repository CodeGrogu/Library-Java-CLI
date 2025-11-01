package com.codegrogu.library.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        member.setName(name);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setDateJoined(LocalDate.now());
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        member.setActive(true);

        return persistNewMember(member);
    }

    // === Register a new member with full details ===
    public Member registerMember(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address) {
        Member member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setGender(gender);
        member.setDateOfBirth(dateOfBirth);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);
        member.setDateJoined(LocalDate.now());
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        member.setActive(true);

        return persistNewMember(member);
    }

    // === Register a new member with full details and member type ===
    public Member registerMember(String firstName, String lastName, String gender, LocalDate dateOfBirth, String email, String phoneNumber, String address, String memberType) {
        Member member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setGender(gender);
        member.setDateOfBirth(dateOfBirth);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);
        member.setMemberType(parseMemberType(memberType));
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        member.setDateJoined(LocalDate.now());
        member.setActive(true);

        return persistNewMember(member);
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
        if (member == null) {
            throw new IllegalArgumentException("Member must not be null");
        }
        if (member.getMemberId() <= 0) {
            throw new IllegalArgumentException("Member ID must be positive");
        }
        if (memberRepository.getMemberById(member.getMemberId()).isEmpty()) {
            throw new IllegalArgumentException("Member not found");
        }
        sanitizeMember(member);
        validateMember(member, false);
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
        return memberRepository.getAllMembers().stream()
                .mapToInt(Member::getMemberId)
                .max()
                .orElse(0) + 1;
    }

    private Member persistNewMember(Member member) {
        sanitizeMember(member);
        validateMember(member, true);
        member.setMemberId(generateMemberId());
        if (member.getMemberType() == null) {
            member.setMemberType(Member.MemberType.PUBLIC);
        }
        member.setCardNumber(generateCardNumber(member.getMemberId()));
        if (member.getBorrowedBookIds() == null) {
            member.setBorrowedBookIds(new ArrayList<>());
        }
        member.setOutstandingFines(Math.max(0, member.getOutstandingFines()));
        if (member.getDateJoined() == null) {
            member.setDateJoined(LocalDate.now());
        }
        if (member.getMembershipStatus() == null) {
            member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        }

        memberRepository.addMember(member);
        return member;
    }

    private void sanitizeMember(Member member) {
        member.setFirstName(sanitize(member.getFirstName()));
        member.setLastName(sanitize(member.getLastName()));
        member.setGender(sanitize(member.getGender()));
        member.setEmail(sanitize(member.getEmail()));
        member.setPhoneNumber(sanitize(member.getPhoneNumber()));
        member.setAddress(sanitize(member.getAddress()));
        if (member.getBorrowedBookIds() == null) {
            member.setBorrowedBookIds(new ArrayList<>());
        }
    }

    private void validateMember(Member member, boolean isNew) {
        if (member.getFirstName() == null || member.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (member.getLastName() == null || member.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!member.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email must be valid");
        }
        if (memberRepository.existsByEmail(member.getEmail(), isNew ? null : member.getMemberId())) {
            throw new IllegalArgumentException("A member with the same email already exists");
        }
        if (member.getDateOfBirth() != null) {
            if (member.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }
            if (Period.between(member.getDateOfBirth(), LocalDate.now()).getYears() > 120) {
                throw new IllegalArgumentException("Date of birth is unrealistic");
            }
        }
        if (member.getMemberType() == null) {
            member.setMemberType(Member.MemberType.PUBLIC);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private Member.MemberType parseMemberType(String memberType) {
        String value = sanitize(memberType);
        if (value.isEmpty()) {
            return Member.MemberType.PUBLIC;
        }
        try {
            return Member.MemberType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown member type: " + memberType);
        }
    }

    private String generateCardNumber(int memberId) {
        return String.format(Locale.ROOT, "CARD-%05d", memberId);
    }
}
