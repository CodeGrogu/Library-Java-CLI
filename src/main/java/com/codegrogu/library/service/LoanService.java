package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.model.Loan;
import com.codegrogu.library.model.Member;
import com.codegrogu.library.repository.BookRepository;
import com.codegrogu.library.repository.LoanRepository;
import com.codegrogu.library.repository.MemberRepository;
import com.codegrogu.library.util.ConfigUtil;

/**
 * Service layer for managing loans in the library system.
 */
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FineService fineService;
    private final int maxLoanDays;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository, FineService fineService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.fineService = fineService;
        this.maxLoanDays = ConfigUtil.getIntProperty("library.maxLoanDays", 14);
    }

    // === Borrow a book ===
    public boolean issueLoan(int bookId, int memberId, LocalDate loanDate, LocalDate dueDate) {
        Optional<Book> bookOpt = bookRepository.getBookById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.isAvailable()) {
                // Mark book as unavailable
                book.setAvailable(false);
                book.setTimesBorrowed(book.getTimesBorrowed() + 1);
                bookRepository.updateBook(book);

                // Update member borrowing history
                Optional<Member> memberOpt = memberRepository.getMemberById(memberId);
                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    // Initialize borrowedBookIds list if null
                    if (member.getBorrowedBookIds() == null) {
                        member.setBorrowedBookIds(new ArrayList<>());
                    }
                    member.getBorrowedBookIds().add(bookId);
                    member.setTotalBooksBorrowed(member.getTotalBooksBorrowed() + 1);
                    memberRepository.updateMember(member);
                }

                // Create new loan
                Loan loan = new Loan();
                loan.setLoanId(generateLoanId());
                loan.setBookId(bookId);
                loan.setMemberId(memberId);
                loan.setLoanDate(loanDate);
                loan.setDueDate(dueDate);
                loan.setReturned(false);
                loan.setReturnDate(null);

                loanRepository.addLoan(loan);
                return true;
            }
        }
        return false; // Book not available or not found
    }

    // === Borrow a book with default due date ===
    public boolean issueLoan(int bookId, int memberId, LocalDate loanDate) {
        LocalDate dueDate = loanDate.plusDays(maxLoanDays);
        return issueLoan(bookId, memberId, loanDate, dueDate);
    }

    // === Return a book ===
    public boolean returnLoan(int loanId, LocalDate returnDate) {
        Optional<Loan> loanOpt = loanRepository.getLoanById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            if (!loan.isReturned()) {
                // Mark loan as returned
                loan.setReturned(true);
                loan.setReturnDate(returnDate);
                loanRepository.updateLoan(loan);

                // Update book availability
                Optional<Book> bookOpt = bookRepository.getBookById(loan.getBookId());
                bookOpt.ifPresent(book -> {
                    book.setAvailable(true);
                    bookRepository.updateBook(book);
                });

                // Update member borrowing history
                Optional<Member> memberOpt = memberRepository.getMemberById(loan.getMemberId());
                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    if (member.getBorrowedBookIds() != null) {
                        member.getBorrowedBookIds().remove(Integer.valueOf(loan.getBookId()));
                        memberRepository.updateMember(member);
                    }
                }

                // Generate fine if overdue
                fineService.createFineForLoan(loan);

                return true;
            }
        }
        return false; // Loan not found or already returned
    }

    // === Get all loans for a member ===
    public List<Loan> getLoansByMember(int memberId) {
        return loanRepository.findLoansByMemberId(memberId);
    }

    // === Get all active loans ===
    public List<Loan> getActiveLoans() {
        return loanRepository.findActiveLoans();
    }

    // === Get loan by ID ===
    public Optional<Loan> getLoanById(int loanId) {
        return loanRepository.getLoanById(loanId);
    }

    // === Get all loans ===
    public List<Loan> getAllLoans() {
        return loanRepository.getAllLoans();
    }

    // === Delete a loan by ID ===
    public boolean deleteLoan(int loanId) {
        return loanRepository.deleteLoan(loanId);
    }

    // === Utility: generate unique loan ID ===
    private int generateLoanId() {
        List<Loan> allLoans = loanRepository.getAllLoans();
        return allLoans.isEmpty() ? 1 : allLoans.get(allLoans.size() - 1).getLoanId() + 1;
    }
}
