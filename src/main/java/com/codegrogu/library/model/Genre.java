package com.codegrogu.library.model;

/**
 * Represents a book genre or category in the library system.
 * Books can reference this genre to ensure consistency.
 */
public class Genre {
    private int genreId;         // Unique ID for the genre
    private String name;         // e.g., Fiction, Science, History
    private String description;  // Optional description of the genre

    // Constructors
    public Genre() {}

    public Genre(int genreId, String name, String description) {
        this.genreId = genreId;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public int getGenreId() { return genreId; }
    public void setGenreId(int genreId) { this.genreId = genreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Utility
    @Override
    public String toString() {
        return "Genre{" +
                "genreId=" + genreId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
