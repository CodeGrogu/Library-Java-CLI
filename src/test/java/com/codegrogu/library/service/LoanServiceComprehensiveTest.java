package com.codegrogu.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.model.Loan;
import com.codegrogu.library.model.Member;
import com.codegrogu.library.repository.BookRepository;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.repository.LoanRepository;
import com.codegrogu.library.repository.MemberRepository;

class LoanServiceComprehensiveTest {

    private BookRepository bookRepository;
    private MemberRepository memberRepository;
    private LoanRepository loanRepository;
    private FineRepository fineRepository;
    private FineService fineService;
    private LoanService loanService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        bookRepository = new BookRepository();
        memberRepository = new MemberRepository();
        loanRepository = new LoanRepository();
        fineRepository = new FineRepository();
        fineService = new FineService(fineRepository);
        loanService = new LoanService(loanRepository, bookRepository, memberRepository, fineService);
    }

    @Test
    void issueLoanHappyPathUpdatesBookMemberAndLoan() {
        Book book = storeBook(1, true);
        Member member = storeMember(1, true, Member.MembershipStatus.ACTIVE);

        LocalDate loanDate = LocalDate.now().minusDays(1);
        LocalDate dueDate = loanDate.plusDays(7);

        assertTrue(loanService.issueLoan(book.getId(), member.getMemberId(), loanDate, dueDate));

        Book storedBook = bookRepository.getBookById(book.getId()).orElseThrow();
        assertFalse(storedBook.isAvailable());
        assertEquals(1, storedBook.getTimesBorrowed());

        Member storedMember = memberRepository.getMemberById(member.getMemberId()).orElseThrow();
        assertTrue(storedMember.getBorrowedBookIds().contains(book.getId()));
        assertEquals(1, storedMember.getTotalBooksBorrowed());

        List<Loan> loans = loanRepository.getAllLoans();
        assertEquals(1, loans.size());
        Loan loan = loans.get(0);
        assertEquals(book.getId(), loan.getBookId());
        assertEquals(member.getMemberId(), loan.getMemberId());
        assertEquals(loanDate, loan.getLoanDate());
        assertEquals(dueDate, loan.getDueDate());
        assertFalse(loan.isReturned());
    }

    @Test
    void issueLoanFailsWhenBookUnavailable() {
        storeBook(1, false);
        storeMember(1, true, Member.MembershipStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        assertFalse(loanService.issueLoan(1, 1, today, today.plusDays(7)));
        assertTrue(loanRepository.getAllLoans().isEmpty());
    }

    @Test
    void issueLoanFailsWhenMemberSuspended() {
        storeBook(1, true);
        storeMember(1, false, Member.MembershipStatus.SUSPENDED);
        LocalDate today = LocalDate.now();

        assertFalse(loanService.issueLoan(1, 1, today, today.plusDays(7)));
        assertTrue(loanRepository.getAllLoans().isEmpty());
    }

    @Test
    void issueLoanRejectsInvalidDates() {
        storeBook(1, true);
        storeMember(1, true, Member.MembershipStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        IllegalArgumentException invalidDates = assertThrows(IllegalArgumentException.class, () ->
            loanService.issueLoan(1, 1, today, today.minusDays(1))
        );
        assertTrue(invalidDates.getMessage().toLowerCase().contains("due date"));
    }

    @Test
    void returnLoanCompletesFlowAndGeneratesFineWhenLate() {
        Book book = storeBook(1, true);
        Member member = storeMember(1, true, Member.MembershipStatus.ACTIVE);

        LocalDate loanDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = loanDate.plusDays(3);
        assertTrue(loanService.issueLoan(book.getId(), member.getMemberId(), loanDate, dueDate));
        Loan loan = loanRepository.getAllLoans().get(0);

        LocalDate returnDate = dueDate.plusDays(4);
        assertTrue(loanService.returnLoan(loan.getLoanId(), returnDate));

        Loan storedLoan = loanRepository.getLoanById(loan.getLoanId()).orElseThrow();
        assertTrue(storedLoan.isReturned());
        assertEquals(returnDate, storedLoan.getReturnDate());

        Book storedBook = bookRepository.getBookById(book.getId()).orElseThrow();
        assertTrue(storedBook.isAvailable());

        Member storedMember = memberRepository.getMemberById(member.getMemberId()).orElseThrow();
        assertFalse(storedMember.getBorrowedBookIds().contains(book.getId()));

        assertEquals(1, fineRepository.getAllFines().size());
        double overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        double expectedFine = overdueDays * 2.5; // from application.properties
        assertEquals(expectedFine, fineRepository.getAllFines().get(0).getAmount());

        assertFalse(loanService.returnLoan(loan.getLoanId(), returnDate));
    }

    @Test
    void getActiveLoansFiltersReturnedEntries() {
        Book first = storeBook(1, true);
        Member member = storeMember(1, true, Member.MembershipStatus.ACTIVE);
        LocalDate loanDate = LocalDate.now();

        assertTrue(loanService.issueLoan(first.getId(), member.getMemberId(), loanDate, loanDate.plusDays(14)));
        Loan loan = loanRepository.getAllLoans().get(0);

        assertEquals(1, loanService.getActiveLoans().size());

        assertTrue(loanService.returnLoan(loan.getLoanId(), loanDate.plusDays(3)));
        assertTrue(loanService.getActiveLoans().isEmpty());
    }

    @Test
    void deleteLoanRemovesRecord() {
        Book book = storeBook(1, true);
        Member member = storeMember(1, true, Member.MembershipStatus.ACTIVE);
        LocalDate today = LocalDate.now();
        assertTrue(loanService.issueLoan(book.getId(), member.getMemberId(), today, today.plusDays(7)));
        Loan loan = loanRepository.getAllLoans().get(0);

        assertTrue(loanService.deleteLoan(loan.getLoanId()));
        assertTrue(loanRepository.getAllLoans().isEmpty());
    }

    private Book storeBook(int id, boolean available) {
        Book book = new Book();
        book.setBookId(id);
        book.setCondition(Book.Condition.GOOD);
        book.setLocation("Shelf" + id);
        book.setSize(Book.Size.STANDARD);
        book.setTitle("Book" + id);
        book.setAuthor("Author" + id);
        book.setIsbn("ISBN" + id);
        book.setPublisher("Publisher" + id);
        book.setPublicationYear(LocalDate.now().getYear());
        book.setGenre("Genre");
        book.setLanguage("English");
        book.setKeywords(new ArrayList<>());
        book.setAvailable(available);
        book.setTimesBorrowed(0);
        bookRepository.addBook(book);
        return book;
    }

    private Member storeMember(int id, boolean active, Member.MembershipStatus status) {
        Member member = new Member();
        member.setMemberId(id);
        member.setCardNumber("CARD-" + id);
        member.setMemberType(Member.MemberType.PUBLIC);
        member.setFirstName("First" + id);
        member.setLastName("Last" + id);
        member.setGender("Gender");
        member.setDateOfBirth(LocalDate.now().minusYears(25));
        member.setEmail("member" + id + "@example.com");
        member.setPhoneNumber("+100" + id);
        member.setAddress("Address" + id);
        member.setDateJoined(LocalDate.now().minusDays(1));
        member.setActive(active);
        member.setOutstandingFines(0);
        member.setMembershipStatus(status);
        member.setBorrowedBookIds(new ArrayList<>());
        member.setTotalBooksBorrowed(0);
        memberRepository.addMember(member);
        return member;
    }
}
