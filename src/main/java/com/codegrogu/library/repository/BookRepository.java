package com.codegrogu.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codegrogu.library.model.Book;

/**
 * In-memory repository for managing books.
 * Handles CRUD operations.
 */
public class BookRepository {

    private final List<Book> books = new ArrayList<>();

    // === Create / Add a book ===
    public void addBook(Book book) {
        books.add(book);
    }

    // === Read / Get a book by ID ===
    public Optional<Book> getBookById(int id) {
        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst();
    }

    // === Read / Get all books ===
    public List<Book> getAllBooks() {
        return new ArrayList<>(books); // Return a copy to prevent external modification
    }

    // === Update a book ===
    public boolean updateBook(Book updatedBook) {
        Optional<Book> existingBookOpt = getBookById(updatedBook.getId());
        if (existingBookOpt.isPresent()) {
            Book existingBook = existingBookOpt.get();
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setIsbn(updatedBook.getIsbn());
            existingBook.setPublisher(updatedBook.getPublisher());
            existingBook.setPublicationYear(updatedBook.getPublicationYear());
            existingBook.setGenre(updatedBook.getGenre());
        existingBook.setLanguage(updatedBook.getLanguage());
        List<String> updatedKeywords = updatedBook.getKeywords() != null
            ? new ArrayList<>(updatedBook.getKeywords())
            : new ArrayList<>();
        existingBook.setKeywords(updatedKeywords);
            existingBook.setCondition(updatedBook.getCondition());
            existingBook.setLocation(updatedBook.getLocation());
            existingBook.setSize(updatedBook.getSize());
            existingBook.setAvailable(updatedBook.isAvailable());
            existingBook.setTimesBorrowed(updatedBook.getTimesBorrowed());
            return true;
        }
        return false;
    }

    // === Delete a book by ID ===
    public boolean deleteBook(int id) {
        return books.removeIf(book -> book.getId() == id);
    }

    // === Additional utility methods ===

    // Find books by title (case-insensitive)
    public List<Book> findByTitle(String title) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                result.add(book);
            }
        }
        return result;
    }

    // Find books by author (case-insensitive)
    public List<Book> findByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().equalsIgnoreCase(author)) {
                result.add(book);
            }
        }
        return result;
    }

    // Check if a book is available
    public boolean isAvailable(int id) {
        Optional<Book> bookOpt = getBookById(id);
        return bookOpt.map(Book::isAvailable).orElse(false);
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return Optional.empty();
        }
        return books.stream()
                .filter(book -> book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn))
                .findFirst();
    }

    public boolean existsByIsbn(String isbn, Integer ignoreBookId) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        return books.stream()
                .anyMatch(book -> book.getIsbn() != null
                        && book.getIsbn().equalsIgnoreCase(isbn)
                        && (ignoreBookId == null || book.getId() != ignoreBookId));
    }
}
