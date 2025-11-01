package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Member;
import com.codegrogu.library.repository.MemberRepository;

class MemberServiceComprehensiveTest {

    private MemberRepository memberRepository;
    private MemberService memberService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        memberRepository = new MemberRepository();
        memberService = new MemberService(memberRepository);
    }

    @Test
    void registerMemberWithTypeAppliesDefaultsAndSanitization() {
        Member member = memberService.registerMember(
            "  Alice  ",
            "  Walker  ",
            "  female  ",
            LocalDate.now().minusYears(30),
            "  alice@example.com  ",
            "  +123456789  ",
            "  10 Main St  ",
            " student "
        );

        assertEquals(1, member.getMemberId());
        assertEquals("Alice", member.getFirstName());
        assertEquals("Walker", member.getLastName());
        assertEquals("FEMALE", member.getGender().toUpperCase());
        assertEquals("alice@example.com", member.getEmail());
        assertEquals("+123456789", member.getPhoneNumber());
        assertEquals("10 Main St", member.getAddress());
        assertEquals(Member.MemberType.STUDENT, member.getMemberType());
        assertEquals("CARD-00001", member.getCardNumber());
        assertEquals(Member.MembershipStatus.ACTIVE, member.getMembershipStatus());
        assertTrue(member.isActive());
        assertNotNull(member.getBorrowedBookIds());
        assertEquals(0, member.getBorrowedBookIds().size());
        assertEquals(0, member.getOutstandingFines());
        assertNotNull(member.getDateJoined());
    }

    @Test
    void registerMemberRejectsDuplicateEmails() {
        memberService.registerMember("John Doe", "john@example.com", "+100");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            memberService.registerMember("Jane Doe", "john@example.com", "+101")
        );
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    void registerMemberRejectsUnknownMemberType() {
        IllegalArgumentException invalidType = assertThrows(IllegalArgumentException.class, () ->
            memberService.registerMember(
                "Mary",
                "Smith",
                "Female",
                LocalDate.now().minusYears(25),
                "mary@example.com",
                "+123",
                "Address",
                "vip"
            )
        );
        assertTrue(invalidType.getMessage().toLowerCase().contains("unknown"));
    }

    @Test
    void registerMemberRejectsFutureBirthdays() {
        IllegalArgumentException futureDob = assertThrows(IllegalArgumentException.class, () ->
            memberService.registerMember(
                "Future",
                "Person",
                "Non-binary",
                LocalDate.now().plusDays(1),
                "future@example.com",
                "+999",
                "Somewhere",
                "public"
            )
        );
        assertTrue(futureDob.getMessage().toLowerCase().contains("birth"));
    }

    @Test
    void updateMemberSanitizesAndPersistsChanges() {
        Member member = memberService.registerMember(
            "Sarah",
            "Connor",
            "Female",
            LocalDate.now().minusYears(35),
            "sarah@example.com",
            "+111",
            "Skynet HQ",
            "public"
        );

        member.setFirstName("  Sarah-Jane  ");
        member.setLastName("  O\'Connor ");
        member.setEmail("  sarah.oconnor@example.com ");
        member.setPhoneNumber("  +222  ");
        member.setAddress("  Tech Park  ");
        member.setMemberType(Member.MemberType.TEACHER);

        assertTrue(memberService.updateMember(member));

        Member stored = memberService.getMemberById(member.getMemberId()).orElseThrow();
        assertEquals("Sarah-Jane", stored.getFirstName());
        assertEquals("O\'Connor", stored.getLastName());
        assertEquals("sarah.oconnor@example.com", stored.getEmail());
        assertEquals("+222", stored.getPhoneNumber());
        assertEquals("Tech Park", stored.getAddress());
        assertEquals(Member.MemberType.TEACHER, stored.getMemberType());
    }

    @Test
    void updateMemberRejectsUnknownMember() {
        Member ghost = new Member();
        ghost.setMemberId(99);
        ghost.setFirstName("Ghost");
        ghost.setLastName("Member");
        ghost.setEmail("ghost@example.com");
        ghost.setMemberType(Member.MemberType.PUBLIC);
    IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, () -> memberService.updateMember(ghost));
    assertTrue(missing.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void activateAndDeactivateMemberToggleStatus() {
    Member member = memberService.registerMember("Rick Deckard", "rick@blade.run", "+120");
        assertTrue(memberService.deactivateMember(member.getMemberId()));
        assertFalse(memberService.getMemberById(member.getMemberId()).orElseThrow().isActive());

        assertTrue(memberService.activateMember(member.getMemberId()));
        assertTrue(memberService.getMemberById(member.getMemberId()).orElseThrow().isActive());
    }

    @Test
    void searchAndFilterMembersByNameStatusAndType() {
        Member first = memberService.registerMember(
            "Dana",
            "Scully",
            "Female",
            LocalDate.now().minusYears(32),
            "dana@fbi.gov",
            "+101",
            "DC",
            "staff"
        );
    Member second = memberService.registerMember("Fox Mulder", "fox@fbi.gov", "+202");
        memberService.deactivateMember(second.getMemberId());

        List<Member> nameMatches = memberService.searchMembersByName("dana");
        assertEquals(1, nameMatches.size());
        assertEquals(first.getMemberId(), nameMatches.get(0).getMemberId());

        List<Member> activeOnly = memberService.getActiveMembers();
        assertEquals(1, activeOnly.size());
        assertTrue(activeOnly.stream().allMatch(Member::isActive));

        List<Member> staffMembers = memberService.getMembersByType("STAFF");
        assertEquals(1, staffMembers.size());
        assertEquals(first.getMemberId(), staffMembers.get(0).getMemberId());
    }

    @Test
    void deleteMemberRemovesRecord() {
    Member member = memberService.registerMember("Ellen Ripley", "ellen@weyland.com", "+303");
        assertTrue(memberService.deleteMember(member.getMemberId()));
        assertTrue(memberService.getAllMembers().isEmpty());
    }
}
