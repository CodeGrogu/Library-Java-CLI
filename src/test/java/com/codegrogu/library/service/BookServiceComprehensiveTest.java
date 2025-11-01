package com.codegrogu.library.service;

import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.repository.BookRepository;

class BookServiceComprehensiveTest {

    private BookRepository bookRepository;
    private BookService bookService;
    private int currentYear;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        bookRepository = new BookRepository();
        bookService = new BookService(bookRepository);
        currentYear = Year.now().getValue();
    }

    @Test
    void addBookWithFullDetailsSanitizesAndDefaultsValues() {
        Book added = bookService.addBook(
            "  Sample Title  ",
            "  Sample Author  ",
            "  12345ABC  ",
            "  Fiction  ",
            "  Sample Publisher  ",
            currentYear,
            "  English  ",
            "unknown",
            "   ",
            "???",
            List.of("  sci-fi  ", "  ", "Drama")
        );

        Book stored = bookService.getBookById(added.getId()).orElseThrow();

        assertEquals("Sample Title", stored.getTitle());
        assertEquals("Sample Author", stored.getAuthor());
        assertEquals("12345ABC", stored.getIsbn());
        assertEquals("Fiction", stored.getGenre());
        assertEquals("Sample Publisher", stored.getPublisher());
        assertEquals("English", stored.getLanguage());
        assertEquals("Unknown", stored.getLocation());
        assertEquals(Book.Condition.GOOD, stored.getCondition());
        assertEquals(Book.Size.STANDARD, stored.getSize());
        assertEquals(List.of("sci-fi", "Drama"), stored.getKeywords());
        assertTrue(stored.isAvailable());
        assertEquals(0, stored.getTimesBorrowed());
    }

    @Test
    void addBookRejectsDuplicateIsbn() {
        Book existing = addSampleBook("ISBN-1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            bookService.addBook("Another", "Author", existing.getIsbn(), "Genre", "Publisher", currentYear, "English", "NEW", "Shelf", "STANDARD", List.of("keyword"))
        );
        assertTrue(ex.getMessage().contains("ISBN"));
    }

    @Test
    void addBookRejectsPublicationYearOutOfRange() {
        int yearTooLow = 1449;
        IllegalArgumentException low = assertThrows(IllegalArgumentException.class, () ->
            bookService.addBook("Title", "Author", "ISBN-A", "Genre", "Publisher", yearTooLow, "English", "NEW", "Shelf", "STANDARD", List.of("keyword"))
        );
        assertTrue(low.getMessage().contains("Publication year"));

        int yearTooHigh = Year.now().plusYears(2).getValue();
        IllegalArgumentException high = assertThrows(IllegalArgumentException.class, () ->
            bookService.addBook("Title", "Author", "ISBN-B", "Genre", "Publisher", yearTooHigh, "English", "NEW", "Shelf", "STANDARD", List.of("keyword"))
        );
        assertTrue(high.getMessage().contains("Publication year"));
    }

    @Test
    void updateBookOverwritesAndSanitizesData() {
        Book original = addSampleBook("ORIGINAL");

        Book updated = new Book(
            original.getId(),
            Book.Condition.POOR,
            "  Shelf B2  ",
            Book.Size.OVERSIZED,
            "  Updated Title  ",
            "  Updated Author  ",
            "  NEWISBN123  ",
            "  New Publisher  ",
            currentYear,
            "  Updated Genre  ",
            "  English  ",
            List.of("  one  ", "two"),
            false,
            5
        );

        boolean changed = bookService.updateBook(updated);
        assertTrue(changed);

        Book stored = bookService.getBookById(original.getId()).orElseThrow();
        assertEquals("Updated Title", stored.getTitle());
        assertEquals("Updated Author", stored.getAuthor());
        assertEquals("NEWISBN123", stored.getIsbn());
        assertEquals("New Publisher", stored.getPublisher());
        assertEquals("Updated Genre", stored.getGenre());
        assertEquals("English", stored.getLanguage());
        assertEquals("Shelf B2", stored.getLocation());
        assertEquals(List.of("one", "two"), stored.getKeywords());
        assertFalse(stored.isAvailable());
        assertEquals(5, stored.getTimesBorrowed());
        assertEquals(Book.Condition.POOR, stored.getCondition());
        assertEquals(Book.Size.OVERSIZED, stored.getSize());
    }

    @Test
    void updateBookRejectsDuplicateIsbn() {
        Book first = addSampleBook("ISBN-1");
        Book second = addSampleBook("ISBN-2");

        Book conflicting = new Book(
            second.getId(),
            Book.Condition.GOOD,
            "Shelf",
            Book.Size.STANDARD,
            "Second",
            "Author",
            first.getIsbn(),
            "Publisher",
            currentYear,
            "Genre",
            "English",
            List.of("keyword"),
            true,
            0
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(conflicting));
        assertTrue(ex.getMessage().contains("ISBN"));
    }

    @Test
    void updateBookRejectsUnknownBook() {
        Book missing = new Book(
            999,
            Book.Condition.GOOD,
            "Shelf",
            Book.Size.STANDARD,
            "Missing",
            "Author",
            "MISSING-ISBN",
            "Publisher",
            currentYear,
            "Genre",
            "English",
            List.of("keyword"),
            true,
            0
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(missing));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void borrowAndReturnFlowUpdatesAvailabilityAndCounters() {
        Book book = addSampleBook("FLOW");

        assertTrue(bookService.borrowBook(book.getId()));
        Book afterBorrow = bookService.getBookById(book.getId()).orElseThrow();
        assertFalse(afterBorrow.isAvailable());
        assertEquals(1, afterBorrow.getTimesBorrowed());

        assertFalse(bookService.borrowBook(book.getId()));

        assertTrue(bookService.returnBook(book.getId()));
        Book afterReturn = bookService.getBookById(book.getId()).orElseThrow();
        assertTrue(afterReturn.isAvailable());

        assertFalse(bookService.returnBook(book.getId()));
    }

    @Test
    void deleteBookRemovesEntry() {
        Book book = addSampleBook("DELETE");
        assertTrue(bookService.deleteBook(book.getId()));
        assertTrue(bookService.getAllBooks().isEmpty());
    }

    @Test
    void searchOperationsAreCaseInsensitive() {
        Book match = addSampleBook("Case-Title");
        assertTrue(bookService.updateBook(new Book(
            match.getId(),
            match.getCondition(),
            match.getLocation(),
            match.getSize(),
            "Case Test",
            "Jane Doe",
            match.getIsbn(),
            match.getPublisher(),
            match.getPublicationYear(),
            match.getGenre(),
            match.getLanguage(),
            match.getKeywords(),
            match.isAvailable(),
            match.getTimesBorrowed()
        )));

        List<Book> byTitle = bookService.searchByTitle("case test");
        assertEquals(1, byTitle.size());

        List<Book> byAuthor = bookService.searchByAuthor("JANE DOE");
        assertEquals(1, byAuthor.size());
    }

    @Test
    void addBookAssignsIncrementingIdentifiers() {
        Book first = addSampleBook("SEQ-1");
        Book second = addSampleBook("SEQ-2");
        assertEquals(first.getId() + 1, second.getId());
    }

    private Book addSampleBook(String isbn) {
        String effectiveIsbn = isbn.length() >= 5 ? isbn : isbn + "-ISBN";
        Book book = bookService.addBook(
            "Title " + isbn,
            "Author " + isbn,
            effectiveIsbn,
            "Genre",
            "Publisher",
            currentYear,
            "English",
            "NEW",
            "Shelf",
            "STANDARD",
            List.of("keyword")
        );
        assertNotNull(bookService.getBookById(book.getId()).orElse(null));
        return book;
    }
}
