package com.codegrogu.library.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.model.Reservation;
import com.codegrogu.library.repository.BookRepository;
import com.codegrogu.library.repository.ReservationRepository;

class ReservationServiceComprehensiveTest {

    private BookRepository bookRepository;
    private ReservationRepository reservationRepository;
    private ReservationService reservationService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        bookRepository = new BookRepository();
        reservationRepository = new ReservationRepository();
        reservationService = new ReservationService(reservationRepository, bookRepository);
    }

    @Test
    void createReservationRequiresUnavailableBookAndAvoidsDuplicates() {
        Book availableBook = storeBook(1, true);
        Book unavailableBook = storeBook(2, false);

        assertFalse(reservationService.createReservation(availableBook.getId(), 10));

        assertTrue(reservationService.createReservation(unavailableBook.getId(), 20));
        Reservation reservation = reservationRepository.getAllReservations().get(0);
        assertEquals("Pending", reservation.getStatus());
        assertEquals(unavailableBook.getId(), reservation.getBookId());
        assertEquals(20, reservation.getMemberId());
        assertEquals(LocalDate.now(), reservation.getReservationDate());

        assertFalse(reservationService.createReservation(unavailableBook.getId(), 20));
    }

    @Test
    void fulfillReservationMarksAsFulfilled() {
        Book book = storeBook(1, false);
        assertTrue(reservationService.createReservation(book.getId(), 30));
        Reservation reservation = reservationRepository.getAllReservations().get(0);

        assertTrue(reservationService.fulfillReservation(reservation.getReservationId()));
        Reservation stored = reservationRepository.getReservationById(reservation.getReservationId()).orElseThrow();
        assertEquals("Fulfilled", stored.getStatus());
    }

    @Test
    void cancelReservationUpdatesStatus() {
        Book book = storeBook(1, false);
        assertTrue(reservationService.createReservation(book.getId(), 40));
        Reservation reservation = reservationRepository.getAllReservations().get(0);

        assertTrue(reservationService.cancelReservation(reservation.getReservationId()));
        Reservation stored = reservationRepository.getReservationById(reservation.getReservationId()).orElseThrow();
        assertEquals("Cancelled", stored.getStatus());
    }

    @Test
    void queriesReturnExpectedResults() {
        Book first = storeBook(1, false);
        Book second = storeBook(2, false);
        assertTrue(reservationService.createReservation(first.getId(), 50));
        assertTrue(reservationService.createReservation(second.getId(), 60));
        Reservation res1 = reservationRepository.getAllReservations().get(0);
        Reservation res2 = reservationRepository.getAllReservations().get(1);
        reservationService.fulfillReservation(res1.getReservationId());

        List<Reservation> byMember = reservationService.getReservationsByMember(60);
        assertEquals(1, byMember.size());
        assertEquals(res2.getReservationId(), byMember.get(0).getReservationId());

        List<Reservation> byBook = reservationService.getReservationsByBook(first.getId());
        assertEquals(1, byBook.size());
        assertEquals(res1.getReservationId(), byBook.get(0).getReservationId());

        List<Reservation> fulfilled = reservationService.getReservationsByStatus("Fulfilled");
        assertEquals(1, fulfilled.size());
        assertEquals(res1.getReservationId(), fulfilled.get(0).getReservationId());

        Reservation next = reservationService.getNextReservation(second.getId());
        assertNotNull(next);
        assertEquals(res2.getReservationId(), next.getReservationId());
    }

    @Test
    void deleteReservationRemovesRecord() {
        Book book = storeBook(1, false);
        assertTrue(reservationService.createReservation(book.getId(), 70));
        Reservation reservation = reservationRepository.getAllReservations().get(0);

        assertTrue(reservationService.deleteReservation(reservation.getReservationId()));
        assertTrue(reservationRepository.getAllReservations().isEmpty());
    }

    private Book storeBook(int id, boolean available) {
        Book book = new Book();
        book.setBookId(id);
        book.setCondition(Book.Condition.GOOD);
        book.setLocation("Shelf" + id);
        book.setSize(Book.Size.STANDARD);
        book.setTitle("Book" + id);
        book.setAuthor("Author" + id);
        book.setIsbn("ISBN-R" + id);
        book.setPublisher("Publisher" + id);
        book.setPublicationYear(LocalDate.now().getYear());
        book.setGenre("Genre");
        book.setLanguage("English");
        book.setKeywords(new ArrayList<>());
        book.setAvailable(available);
        bookRepository.addBook(book);
        return book;
    }
}
