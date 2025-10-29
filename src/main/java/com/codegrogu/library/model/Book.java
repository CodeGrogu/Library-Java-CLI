package com.codegrogu.library.model;

import java.util.List;

/**
 * Represents a book in the library system, including its physical, bibliographic,
 * and circulation attributes.
 */
public class Book {

    // === Physical Attributes ===
    private int id;                         // Unique barcode or system ID
    private Condition condition;            // NEW, GOOD, FAIR, POOR
    private String location;                // e.g., "Shelf A3, Section B"
    private Size size;                      // STANDARD, OVERSIZED

    // === Bibliographic Attributes ===
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private int publicationYear;
    private String genre;
    private String language;
    private List<String> keywords;          // e.g., ["Fiction", "Science", "Space"]

    // === Circulation Attributes ===
    private boolean isAvailable;
    private int timesBorrowed;              // Number of times this book has been borrowed

    // === Constructors ===
    public Book() {
        // Empty constructor required by frameworks
    }

    public Book(int id, Condition condition, String location, Size size,
                String title, String author, String isbn, String publisher,
                int publicationYear, String genre, String language, List<String> keywords,
                boolean isAvailable, int timesBorrowed) {
        this.id = id;
        this.condition = condition;
        this.location = location;
        this.size = size;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.language = language;
        this.keywords = keywords;
        this.isAvailable = isAvailable;
        this.timesBorrowed = timesBorrowed;
    }

    // === Getters and Setters ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return id; }
    public void setBookId(int bookId) { this.id = bookId; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getTimesBorrowed() { return timesBorrowed; }
    public void setTimesBorrowed(int timesBorrowed) { this.timesBorrowed = timesBorrowed; }

    // === Utility Methods ===
    @Override
    public String toString() {
        return "Book {" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", condition=" + condition +
                ", size=" + size +
                ", keywords=" + keywords +
                ", available=" + isAvailable +
                ", location='" + location + '\'' +
                ", timesBorrowed=" + timesBorrowed +
                '}';
    }

    // === Enum Types ===
    public enum Condition {
        NEW, GOOD, FAIR, POOR
    }

    public enum Size {
        STANDARD, OVERSIZED
    }
}
