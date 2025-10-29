package com.codegrogu.library.service;

import java.util.ArrayList;
import java.util.List;
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
        bookRepository.addBook(book);
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
        if (condition == null || condition.trim().isEmpty() || condition.equals("null")) condition = "GOOD";
        try {
            book.setCondition(Book.Condition.valueOf(condition));
        } catch (IllegalArgumentException e) {
            book.setCondition(Book.Condition.GOOD);
        }
        if (size == null || size.trim().isEmpty() || size.equals("null")) size = "STANDARD";
        try {
            book.setSize(Book.Size.valueOf(size));
        } catch (IllegalArgumentException e) {
            book.setSize(Book.Size.STANDARD);
        }
        if (location == null || location.trim().isEmpty() || location.equals("null")) location = "Unknown";
        book.setLocation(location);
        book.setKeywords(keywords != null ? new ArrayList<>(keywords) : new ArrayList<>());
        book.setAvailable(true);
        bookRepository.addBook(book);
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
        List<Book> allBooks = bookRepository.getAllBooks();
        return allBooks.isEmpty() ? 1 : allBooks.get(allBooks.size() - 1).getBookId() + 1;
    }
}
