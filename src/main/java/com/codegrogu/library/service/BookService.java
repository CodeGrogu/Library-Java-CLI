package com.codegrogu.library.service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.repository.BookRepository;

/**
 * Service layer for managing books.
 * Handles business logic, borrowing, returning, and search operations.
 */
public class BookService {

    private final BookRepository bookRepository;

    // Constructor
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // === Add a new book ===
    public void addBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }
        sanitizeBook(book);
        validateBook(book, true);
        book.setBookId(generateBookId());
        if (book.getCondition() == null) {
            book.setCondition(Book.Condition.GOOD);
        }
        if (book.getSize() == null) {
            book.setSize(Book.Size.STANDARD);
        }
        book.setAvailable(true);
        bookRepository.addBook(book);
    }

    // === Add a new book with details ===
    public Book addBook(String title, String author, String isbn) {
        Book book = new Book();
        book.setBookId(generateBookId());
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setAvailable(true);
        addBook(book);
        return book;
    }

    // === Add a new book with full details ===
    public Book addBook(String title, String author, String isbn, String genre, String publisher, int publicationYear, String language, String condition, String location, String size, List<String> keywords) {
        Book book = new Book();
        book.setBookId(generateBookId());
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setGenre(genre);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setLanguage(language);
        book.setCondition(parseCondition(condition));
        book.setSize(parseSize(size));
        book.setLocation(location);
        book.setKeywords(keywords != null ? new ArrayList<>(keywords) : new ArrayList<>());
        addBook(book);
        return book;
    }

    // === Get a book by ID ===
    public Optional<Book> getBookById(int id) {
        return bookRepository.getBookById(id);
    }

    // === Get all books ===
    public List<Book> getAllBooks() {
        return bookRepository.getAllBooks();
    }

    // === Update a book ===
    public boolean updateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }
        if (book.getId() <= 0) {
            throw new IllegalArgumentException("Book ID must be positive");
        }
        if (bookRepository.getBookById(book.getId()).isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }
        sanitizeBook(book);
        validateBook(book, false);
        return bookRepository.updateBook(book);
    }

    // === Delete a book by ID ===
    public boolean deleteBook(int id) {
        return bookRepository.deleteBook(id);
    }

    // === Borrow a book ===
    public boolean borrowBook(int id) {
        Optional<Book> bookOpt = bookRepository.getBookById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.isAvailable()) {
                book.setAvailable(false);
                book.setTimesBorrowed(book.getTimesBorrowed() + 1);
                bookRepository.updateBook(book);
                return true;
            }
        }
        return false; // Book not found or not available
    }

    // === Return a book ===
    public boolean returnBook(int id) {
        Optional<Book> bookOpt = bookRepository.getBookById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (!book.isAvailable()) {
                book.setAvailable(true);
                bookRepository.updateBook(book);
                return true;
            }
        }
        return false; // Book not found or already available
    }

    // === Search books by title ===
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    // === Search books by author ===
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    // === Check if a book is available ===
    public boolean isBookAvailable(int id) {
        return bookRepository.isAvailable(id);
    }

    // === Utility: generate unique book ID ===
    private int generateBookId() {
        return bookRepository.getAllBooks().stream()
                .mapToInt(Book::getBookId)
                .max()
                .orElse(0) + 1;
    }

    private void sanitizeBook(Book book) {
        book.setTitle(sanitize(book.getTitle()));
        book.setAuthor(sanitize(book.getAuthor()));
        book.setIsbn(sanitize(book.getIsbn()));
        book.setGenre(sanitize(book.getGenre()));
        book.setPublisher(sanitize(book.getPublisher()));
        book.setLanguage(sanitize(book.getLanguage()));
        book.setLocation(sanitizeOptional(book.getLocation(), "Unknown"));

        if (book.getKeywords() == null) {
            book.setKeywords(new ArrayList<>());
        } else {
            List<String> cleaned = new ArrayList<>();
            for (String keyword : book.getKeywords()) {
                String sanitized = sanitize(keyword);
                if (!sanitized.isEmpty()) {
                    cleaned.add(sanitized);
                }
            }
            book.setKeywords(cleaned);
        }

        if (book.getCondition() == null) {
            book.setCondition(Book.Condition.GOOD);
        }
        if (book.getSize() == null) {
            book.setSize(Book.Size.STANDARD);
        }
    }

    private void validateBook(Book book, boolean isNew) {
        if (book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (book.getAuthor().isBlank()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (book.getIsbn().isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        if (book.getIsbn().length() < 5) {
            throw new IllegalArgumentException("ISBN must be at least 5 characters");
        }

        int currentYear = Year.now().plusYears(1).getValue();
        if (book.getPublicationYear() < 1450 || book.getPublicationYear() > currentYear) {
            throw new IllegalArgumentException("Publication year must be between 1450 and " + currentYear);
        }

        if (bookRepository.existsByIsbn(book.getIsbn(), isNew ? null : book.getId())) {
            throw new IllegalArgumentException("A book with the same ISBN already exists");
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String sanitizeOptional(String value, String fallback) {
        String sanitized = sanitize(value);
        return sanitized.isEmpty() ? fallback : sanitized;
    }

    private Book.Condition parseCondition(String condition) {
        String candidate = sanitize(condition);
        if (candidate.isEmpty()) {
            return Book.Condition.GOOD;
        }
        try {
            return Book.Condition.valueOf(candidate.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Book.Condition.GOOD;
        }
    }

    private Book.Size parseSize(String size) {
        String candidate = sanitize(size);
        if (candidate.isEmpty()) {
            return Book.Size.STANDARD;
        }
        try {
            return Book.Size.valueOf(candidate.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Book.Size.STANDARD;
        }
    }
}
