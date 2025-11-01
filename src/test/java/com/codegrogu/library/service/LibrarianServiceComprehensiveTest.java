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

import com.codegrogu.library.model.Librarian;
import com.codegrogu.library.model.LibrarianRole;
import com.codegrogu.library.repository.LibrarianRepository;

class LibrarianServiceComprehensiveTest {

    private LibrarianRepository librarianRepository;
    private LibrarianService librarianService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        librarianRepository = new LibrarianRepository();
        librarianService = new LibrarianService(librarianRepository);
    }

    @Test
    void addLibrarianWithFullDetailsAppliesDefaults() {
        Librarian librarian = librarianService.addLibrarian(
            "  Dana  ",
            "  Barrett  ",
            " Female ",
            LocalDate.now().minusYears(28),
            "  dana@library.org  ",
            "  +123456  ",
            "  1 Ghost Ave  ",
            "  Archivist  ",
            42000.0
        );

        assertEquals(1, librarian.getLibrarianId());
        assertEquals("Dana", librarian.getFirstName());
        assertEquals("Barrett", librarian.getLastName());
        assertEquals("Female", librarian.getGender());
        assertEquals("dana@library.org", librarian.getEmail());
        assertEquals("+123456", librarian.getPhoneNumber());
        assertEquals("1 Ghost Ave", librarian.getAddress());
        assertEquals("Archivist", librarian.getPosition());
        assertEquals(42000.0, librarian.getSalary());
        assertTrue(librarian.isActive());
        assertEquals(LibrarianRole.STAFF, librarian.getRole());
        assertEquals("LIB001", librarian.getStaffCode());
        assertNotNull(librarian.getDateHired());
    }

    @Test
    void addLibrarianWithCredentialsSanitizesUsernameAndPassword() {
        Librarian librarian = librarianService.addLibrarian(
            "Peter",
            "Venkman",
            "Male",
            LocalDate.now().minusYears(35),
            "peter@library.org",
            "+789",
            "Firehouse",
            "Manager",
            52000.0,
            LibrarianRole.ADMIN,
            "  pv  ",
            "  secret  "
        );

        Librarian stored = librarianService.getLibrarianById(librarian.getLibrarianId()).orElseThrow();
        assertEquals("pv", stored.getUsername());
        assertEquals("secret", stored.getPasswordHash());
        assertEquals(LibrarianRole.ADMIN, stored.getRole());
    }

    @Test
    void addLibrarianRejectsNegativeSalary() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            librarianService.addLibrarian(
                "Ray",
                "Stantz",
                "Male",
                LocalDate.now().minusYears(32),
                "ray@library.org",
                "+1010",
                "HQ",
                "Technician",
                -1.0
            )
        );
        assertTrue(ex.getMessage().toLowerCase().contains("salary"));
    }

    @Test
    void updateLibrarianSanitizesChanges() {
        Librarian librarian = librarianService.addLibrarian(
            "Egon",
            "Spengler",
            "Male",
            LocalDate.now().minusYears(33),
            "egon@library.org",
            "+2020",
            "HQ",
            "Scientist",
            60000.0
        );

        librarian.setFirstName("  Dr. Egon  ");
        librarian.setLastName("  Spengler  ");
        librarian.setEmail("  egon.science@library.org  ");
        librarian.setPosition("  Chief Scientist ");
        librarian.setSalary(75000.0);
        librarian.setRole(LibrarianRole.ADMIN);

        assertTrue(librarianService.updateLibrarian(librarian));

        Librarian stored = librarianService.getLibrarianById(librarian.getLibrarianId()).orElseThrow();
        assertEquals("Dr. Egon", stored.getFirstName());
        assertEquals("Spengler", stored.getLastName());
        assertEquals("egon.science@library.org", stored.getEmail());
        assertEquals("Chief Scientist", stored.getPosition());
        assertEquals(75000.0, stored.getSalary());
        assertEquals(LibrarianRole.ADMIN, stored.getRole());
    }

    @Test
    void updateLibrarianRejectsUnknownRecord() {
        Librarian ghost = new Librarian();
        ghost.setLibrarianId(99);
        ghost.setFirstName("Ghost");
        ghost.setLastName("Librarian");
        ghost.setEmail("ghost@library.org");
        ghost.setSalary(1000.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> librarianService.updateLibrarian(ghost));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void activateAndDeactivateLibrarianToggleStatus() {
    Librarian librarian = librarianService.addLibrarian("Winston Zeddemore", "winston@library.org", "+404");
        assertTrue(librarianService.deactivateLibrarian(librarian.getLibrarianId()));
        assertFalse(librarianService.getLibrarianById(librarian.getLibrarianId()).orElseThrow().isActive());

        assertTrue(librarianService.activateLibrarian(librarian.getLibrarianId()));
        assertTrue(librarianService.getLibrarianById(librarian.getLibrarianId()).orElseThrow().isActive());
    }

    @Test
    void searchAndFilterLibrarians() {
    Librarian first = librarianService.addLibrarian("Janine Melnitz", "janine@library.org", "+505");
    Librarian second = librarianService.addLibrarian("Louis Tully", "louis@library.org", "+606");
        librarianService.deactivateLibrarian(second.getLibrarianId());

        List<Librarian> matches = librarianService.searchLibrariansByName("jan");
        assertEquals(1, matches.size());
        assertEquals(first.getLibrarianId(), matches.get(0).getLibrarianId());

        List<Librarian> activeOnly = librarianService.getActiveLibrarians();
        assertEquals(1, activeOnly.size());
        assertTrue(activeOnly.stream().allMatch(Librarian::isActive));
    }

    @Test
    void deleteLibrarianRemovesRecord() {
    Librarian librarian = librarianService.addLibrarian("Library Bot", "bot@library.org", "+707");
        assertTrue(librarianService.deleteLibrarian(librarian.getLibrarianId()));
        assertTrue(librarianService.getAllLibrarians().isEmpty());
    }
}
