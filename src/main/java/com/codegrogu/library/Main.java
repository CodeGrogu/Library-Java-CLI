package com.codegrogu.library;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.codegrogu.library.model.Book;
import com.codegrogu.library.model.Fine;
import com.codegrogu.library.model.Librarian;
import com.codegrogu.library.model.LibrarianRole;
import com.codegrogu.library.model.Loan;
import com.codegrogu.library.model.Member;
import com.codegrogu.library.model.Reservation;
import com.codegrogu.library.repository.BookRepository;
import com.codegrogu.library.repository.FineRepository;
import com.codegrogu.library.repository.LibrarianRepository;
import com.codegrogu.library.repository.LoanRepository;
import com.codegrogu.library.repository.MemberRepository;
import com.codegrogu.library.repository.PaymentRepository;
import com.codegrogu.library.repository.ReservationRepository;
import com.codegrogu.library.service.BookService;
import com.codegrogu.library.service.FineService;
import com.codegrogu.library.service.LibrarianService;
import com.codegrogu.library.service.LoanService;
import com.codegrogu.library.service.MemberService;
import com.codegrogu.library.service.PaymentService;
import com.codegrogu.library.service.ReservationService;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final List<String> breadcrumbs = new ArrayList<>();
    private static final LocalDateTime sessionStart = LocalDateTime.now();
    private static boolean applicationRunning = true;

    // === Breadcrumb Helpers ===
    private static void pushBreadcrumb(String menu) {
        breadcrumbs.add(menu);
    }

    private static void popBreadcrumb() {
        if (!breadcrumbs.isEmpty()) {
            breadcrumbs.remove(breadcrumbs.size() - 1);
        }
    }

    private static String getBreadcrumbPath() {
        if (breadcrumbs.isEmpty()) {
            return "Main Menu";
        }
        return "Main Menu > " + String.join(" > ", breadcrumbs);
    }

    // === Enhanced Input Helpers ===
    private static String readLineRaw() {
        return scanner.nextLine().trim();
    }

    private static String readInput() {
        return readLineRaw().toLowerCase();
    }

    private static String sanitizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String promptRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sanitizeText(readLineRaw());
            if (value.equalsIgnoreCase("x") || value.equalsIgnoreCase("exit")) {
                attemptApplicationExit();
                if (!applicationRunning) {
                    return "";
                }
                System.out.println("Input is required. Please try again.");
                continue;
            }
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input is required. Please try again.");
        }
    }

    private static int readInt() {
        while (true) {
            String input = readLineRaw();
            if (input.isEmpty()) {
                System.out.print("Please enter a value: ");
                continue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                if (input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit")) {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        return Integer.MIN_VALUE;
                    }
                }
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        return readIntInRange(prompt, min, max, false);
    }

    private static int readIntInRange(String prompt, int min, int max, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            String input = readLineRaw();
            if (allowZero && "0".equals(input)) {
                return 0;
            }
            if (input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit")) {
                attemptApplicationExit();
                if (!applicationRunning) {
                    return Integer.MIN_VALUE;
                }
                System.out.print("Please enter a valid number: ");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.print("Please enter a number between " + min + " and " + max + ": ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static Integer readIntInRangeOptional(String prompt, int min, int max, Integer defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = readLineRaw();
            if (input.isEmpty()) {
                return defaultValue;
            }
            if (input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit")) {
                attemptApplicationExit();
                if (!applicationRunning) {
                    return Integer.MIN_VALUE;
                }
                System.out.print("Please enter a valid number: ");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.print("Please enter a number between " + min + " and " + max + ": ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLineRaw();
            if (input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit")) {
                attemptApplicationExit();
                if (!applicationRunning) {
                    return Double.NaN;
                }
                System.out.print("Please enter a valid number: ");
                continue;
            }
            try {
                double value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.print("Please enter a non-negative number: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static LocalDate readDate(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt);
            String input = readLineRaw();
            if (input.isEmpty()) {
                if (required) {
                    System.out.println("Date is required. Please try again.");
                    continue;
                }
                return null;
            }
            if (input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit")) {
                attemptApplicationExit();
                if (!applicationRunning) {
                    return null;
                }
                System.out.println("Date is required. Please try again.");
                continue;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private static String promptGenre(String currentValue) {
        while (applicationRunning) {
            System.out.println("Genres:");
            System.out.println("1. Fiction");
            System.out.println("2. Non-Fiction");
            System.out.println("3. Mystery");
            System.out.println("4. Romance");
            System.out.println("5. Sci-Fi");
            System.out.println("6. Biography");
            System.out.println("7. Other");
            if (currentValue == null) {
                int choice = readIntInRange("Choose genre (1-7): ", 1, 7);
                if (choice == Integer.MIN_VALUE) {
                    return currentValue;
                }
                String resolved = resolveGenreChoice(choice);
                return !applicationRunning ? currentValue : resolved;
            }
            Integer choice = readIntInRangeOptional("Choose genre (1-7, Enter to keep " + currentValue + "): ", 1, 7, null);
            if (choice == Integer.MIN_VALUE) {
                return currentValue;
            }
            if (choice == null) {
                return currentValue;
            }
            String resolved = resolveGenreChoice(choice);
            return !applicationRunning ? currentValue : resolved;
        }
        return currentValue;
    }

    private static String resolveGenreChoice(int choice) {
        return switch (choice) {
            case 1 -> "Fiction";
            case 2 -> "Non-Fiction";
            case 3 -> "Mystery";
            case 4 -> "Romance";
            case 5 -> "Sci-Fi";
            case 6 -> "Biography";
            case 7 -> sanitizeText(promptRequired("Enter genre: "));
            default -> "Other";
        };
    }

    private static String promptLanguage(String currentValue) {
        while (applicationRunning) {
            System.out.println("Languages:");
            System.out.println("1. English");
            System.out.println("2. Spanish");
            System.out.println("3. French");
            System.out.println("4. German");
            System.out.println("5. Other");
            if (currentValue == null) {
                int choice = readIntInRange("Choose language (1-5): ", 1, 5);
                if (choice == Integer.MIN_VALUE) {
                    return currentValue;
                }
                String resolved = resolveLanguageChoice(choice);
                return !applicationRunning ? currentValue : resolved;
            }
            Integer choice = readIntInRangeOptional("Choose language (1-5, Enter to keep " + currentValue + "): ", 1, 5, null);
            if (choice == Integer.MIN_VALUE) {
                return currentValue;
            }
            if (choice == null) {
                return currentValue;
            }
            String resolved = resolveLanguageChoice(choice);
            return !applicationRunning ? currentValue : resolved;
        }
        return currentValue;
    }

    private static String resolveLanguageChoice(int choice) {
        return switch (choice) {
            case 1 -> "English";
            case 2 -> "Spanish";
            case 3 -> "French";
            case 4 -> "German";
            case 5 -> sanitizeText(promptRequired("Enter language: "));
            default -> "Other";
        };
    }

    private static String promptCondition(String currentValue) {
        while (applicationRunning) {
            System.out.println("Conditions:");
            System.out.println("1. NEW");
            System.out.println("2. GOOD");
            System.out.println("3. FAIR");
            System.out.println("4. POOR");
            if (currentValue == null) {
                int choice = readIntInRange("Choose condition (1-4): ", 1, 4);
                if (choice == Integer.MIN_VALUE) {
                    return currentValue;
                }
                String resolved = resolveConditionChoice(choice);
                return !applicationRunning ? currentValue : resolved;
            }
            Integer choice = readIntInRangeOptional("Choose condition (1-4, Enter to keep " + currentValue + "): ", 1, 4, null);
            if (choice == Integer.MIN_VALUE) {
                return currentValue;
            }
            if (choice == null) {
                return currentValue;
            }
            String resolved = resolveConditionChoice(choice);
            return !applicationRunning ? currentValue : resolved;
        }
        return currentValue;
    }

    private static String resolveConditionChoice(int choice) {
        return switch (choice) {
            case 1 -> "NEW";
            case 2 -> "GOOD";
            case 3 -> "FAIR";
            case 4 -> "POOR";
            default -> "GOOD";
        };
    }

    private static String promptSize(String currentValue) {
        while (applicationRunning) {
            System.out.println("Sizes:");
            System.out.println("1. STANDARD");
            System.out.println("2. OVERSIZED");
            if (currentValue == null) {
                int choice = readIntInRange("Choose size (1-2): ", 1, 2);
                if (choice == Integer.MIN_VALUE) {
                    return currentValue;
                }
                String resolved = resolveSizeChoice(choice);
                return !applicationRunning ? currentValue : resolved;
            }
            Integer choice = readIntInRangeOptional("Choose size (1-2, Enter to keep " + currentValue + "): ", 1, 2, null);
            if (choice == Integer.MIN_VALUE) {
                return currentValue;
            }
            if (choice == null) {
                return currentValue;
            }
            String resolved = resolveSizeChoice(choice);
            return !applicationRunning ? currentValue : resolved;
        }
        return currentValue;
    }

    private static String resolveSizeChoice(int choice) {
        return switch (choice) {
            case 1 -> "STANDARD";
            case 2 -> "OVERSIZED";
            default -> "STANDARD";
        };
    }

    public static void main(String[] args) {

        // === Repositories ===
        BookRepository bookRepository = new BookRepository();
        MemberRepository memberRepository = new MemberRepository();
        LibrarianRepository librarianRepository = new LibrarianRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        LoanRepository loanRepository = new LoanRepository();
        FineRepository fineRepository = new FineRepository();
        PaymentRepository paymentRepository = new PaymentRepository();

        // === Services ===
        BookService bookService = new BookService(bookRepository);
        MemberService memberService = new MemberService(memberRepository);
        LibrarianService librarianService = new LibrarianService(librarianRepository);
        FineService fineService = new FineService(fineRepository);
        ReservationService reservationService = new ReservationService(reservationRepository, bookRepository);
        LoanService loanService = new LoanService(loanRepository, bookRepository, memberRepository, fineService);
        PaymentService paymentService = new PaymentService(paymentRepository, fineRepository);

        while (applicationRunning) {
            System.out.println("\n=== Library Management System ===");
            System.out.println("Session started: " + sessionStart.toLocalDate() + " at " + sessionStart.toLocalTime().toString().substring(0, 8));
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. Books [B]");
            System.out.println("2. Members [M]");
            System.out.println("3. Librarians [L]");
            System.out.println("4. Reservations [R]");
            System.out.println("5. Loans [O]");
            System.out.println("6. Fines & Payments [F]");
            System.out.println("0. Exit [X]");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "b", "books" -> 1;
                    case "m", "members" -> 2;
                    case "l", "librarians" -> 3;
                    case "r", "reservations" -> 4;
                    case "o", "loans" -> 5;
                    case "f", "fines" -> 6;
                    case "x", "exit" -> 0;
                    case "?" -> -1; // Help
                    default -> -2; // Invalid
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("Books");
                    bookMenu(bookService);
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Members");
                    memberMenu(memberService);
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Librarians");
                    librarianMenu(librarianService);
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Reservations");
                    reservationMenu(reservationService, bookService, memberService);
                    popBreadcrumb();
                }
                case 5 -> {
                    pushBreadcrumb("Loans");
                    loanMenu(loanService, bookService, memberService, fineService, reservationService);
                    popBreadcrumb();
                }
                case 6 -> {
                    pushBreadcrumb("Fines & Payments");
                    finePaymentMenu(fineService, paymentService, loanService, bookService, memberService);
                    popBreadcrumb();
                }
                case 0 -> attemptApplicationExit();
                case -1 -> showHelp();
                default -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Book Menu ===
    private static void bookMenu(BookService bookService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Books ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Books [V]");
            System.out.println("2. Add Book [A]");
            System.out.println("3. Update Book [U]");
            System.out.println("4. Delete Book [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "a", "add" -> 2;
                    case "u", "update" -> 3;
                    case "d", "delete" -> 4;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Books");
                    List<Book> books = bookService.getAllBooks();
                    if (books.isEmpty()) {
                        System.out.println("No books in the library.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (title/author) or press Enter for all: ");
                        String search = readInput();
                        List<Book> filtered = books.stream()
                            .filter(b -> search.isEmpty() ||
                                b.getTitle().toLowerCase().contains(search) ||
                                b.getAuthor().toLowerCase().contains(search))
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No books match your search.");
                        } else {
                            filtered.forEach(System.out::println);
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Add Book");
                    System.out.println("Processing... Adding new book.");
                    BookFormState formState = new BookFormState();
                    collectForm(List.of(
                        formField("Title", true, value -> requireNonEmpty("Title", value), formState::setTitle),
                        formField("Author", true, value -> requireNonEmpty("Author", value), formState::setAuthor),
                        formField("ISBN", true, candidate -> {
                            ValidationResult<String> base = requireNonEmpty("ISBN", candidate);
                            if (!base.valid()) {
                                return base;
                            }
                            if (candidate.length() < 5) {
                                return ValidationResult.failure("ISBN must be at least 5 characters.");
                            }
                            if (isbnExists(bookService, candidate, null)) {
                                return ValidationResult.failure("A book with this ISBN already exists.");
                            }
                            return ValidationResult.success(candidate);
                        }, formState::setIsbn),
                        formField("Publisher", false, Main::optionalText, formState::setPublisher),
                        formField("Location", false, Main::optionalText, formState::setLocation),
                        formField("Keywords (comma separated)", false, Main::parseKeywordList, formState::setKeywords)
                    ));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String genre = promptGenre(null);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    int publicationYear = readIntInRange("Publication Year (1450-" + (LocalDate.now().getYear() + 1) + "): ", 1450, LocalDate.now().getYear() + 1);
                    if (publicationYear == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }

                    String language = promptLanguage(null);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String condition = promptCondition(null);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String size = promptSize(null);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    try {
                        Book book = bookService.addBook(
                            formState.getTitle(),
                            formState.getAuthor(),
                            formState.getIsbn(),
                            genre,
                            formState.getPublisher(),
                            publicationYear,
                            language,
                            condition,
                            formState.getLocation(),
                            size,
                            formState.getKeywords()
                        );
                        System.out.println("Book '" + book.getTitle() + "' by " + book.getAuthor() + " added successfully (ID: " + book.getId() + ")");
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to add book: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Update Book");
                    List<Book> books = bookService.getAllBooks();
                    if (books.isEmpty()) {
                        System.out.println("No books to update.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Books:");
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        System.out.println((i + 1) + ". " + b.getTitle() + " by " + b.getAuthor());
                    }
                    System.out.print("Select a book by number (1-" + books.size() + ") or press 0 to cancel: ");
                    int choice = readInt();
                    if (choice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    if (choice == 0) {
                        popBreadcrumb();
                        break;
                    }
                    if (choice < 1 || choice > books.size()) {
                        System.out.println("Invalid choice. Try again.");
                        popBreadcrumb();
                        break;
                    }
                    Book bookToUpdate = books.get(choice - 1);

                    System.out.println("Current details:");
                    System.out.println(bookToUpdate);
                    BookFormState formState = new BookFormState();
                    formState.setTitle(bookToUpdate.getTitle());
                    formState.setAuthor(bookToUpdate.getAuthor());
                    formState.setIsbn(bookToUpdate.getIsbn());
                    formState.setPublisher(bookToUpdate.getPublisher());
                    formState.setLocation(bookToUpdate.getLocation());
                    formState.setKeywords(bookToUpdate.getKeywords());

                    collectForm(List.of(
                        formField("Title", true, bookToUpdate::getTitle, value -> requireNonEmpty("Title", value), formState::setTitle),
                        formField("Author", true, bookToUpdate::getAuthor, value -> requireNonEmpty("Author", value), formState::setAuthor),
                        formField("ISBN", true, bookToUpdate::getIsbn, candidate -> {
                            ValidationResult<String> base = requireNonEmpty("ISBN", candidate);
                            if (!base.valid()) {
                                return base;
                            }
                            if (candidate.length() < 5) {
                                return ValidationResult.failure("ISBN must be at least 5 characters.");
                            }
                            if (isbnExists(bookService, candidate, bookToUpdate.getId())) {
                                return ValidationResult.failure("A book with this ISBN already exists.");
                            }
                            return ValidationResult.success(candidate);
                        }, formState::setIsbn),
                        formField("Publisher", false, bookToUpdate::getPublisher, Main::optionalText, formState::setPublisher),
                        formField("Location", false, bookToUpdate::getLocation, Main::optionalText, formState::setLocation),
                        formField("Keywords (comma separated)", false, () -> String.join(", ", bookToUpdate.getKeywords()), Main::parseKeywordList, formState::setKeywords)
                    ));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String genre = promptGenre(bookToUpdate.getGenre());
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    int futureYear = LocalDate.now().getYear() + 1;
                    Integer yearChoice = readIntInRangeOptional("Publication Year [" + bookToUpdate.getPublicationYear() + "] (1450-" + futureYear + "): ", 1450, futureYear, bookToUpdate.getPublicationYear());
                    if (yearChoice != null && yearChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    int publicationYear = yearChoice == null ? bookToUpdate.getPublicationYear() : yearChoice;

                    String language = promptLanguage(bookToUpdate.getLanguage());
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String condition = promptCondition(bookToUpdate.getCondition().toString());
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    String size = promptSize(bookToUpdate.getSize().toString());
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    Book updatedBook = new Book(
                        bookToUpdate.getId(),
                        Book.Condition.valueOf(condition),
                        formState.getLocation(),
                        Book.Size.valueOf(size),
                        formState.getTitle(),
                        formState.getAuthor(),
                        formState.getIsbn(),
                        formState.getPublisher(),
                        publicationYear,
                        genre,
                        language,
                        formState.getKeywords(),
                        bookToUpdate.isAvailable(),
                        bookToUpdate.getTimesBorrowed()
                    );

                    try {
                        boolean success = bookService.updateBook(updatedBook);
                        if (success) {
                            System.out.println("Book updated successfully!");
                        } else {
                            System.out.println("Failed to update book.");
                        }
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to update book: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Delete Book");
                    List<Book> books = bookService.getAllBooks();
                    if (books.isEmpty()) {
                        System.out.println("No books to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Books:");
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        System.out.println((i + 1) + ". " + b.getTitle() + " by " + b.getAuthor());
                    }
                    System.out.print("Select a book by number (1-" + books.size() + ") or press 0 to cancel: ");
                    int choice = readInt();
                    if (choice == 0) {
                        popBreadcrumb();
                        break;
                    }
                    if (choice < 1 || choice > books.size()) {
                        System.out.println("Invalid choice. Try again.");
                        popBreadcrumb();
                        break;
                    }
                    Book bookToDelete = books.get(choice - 1);
                    System.out.print("Are you sure you want to delete '" + bookToDelete.getTitle() + "'? (y/n): ");
                    String confirm = readInput();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        boolean success = bookService.deleteBook(bookToDelete.getId());
                        if (success) {
                            System.out.println("Book deleted successfully!");
                        } else {
                            System.out.println("Failed to delete book.");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                default -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Member Menu ===
    private static void memberMenu(MemberService memberService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Members ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Members [V]");
            System.out.println("2. View Members by Type [T]");
            System.out.println("3. Register Member [A]");
            System.out.println("4. Update Member [U]");
            System.out.println("5. Delete Member [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "t", "type" -> 2;
                    case "a", "add", "register" -> 3;
                    case "u", "update" -> 4;
                    case "d", "delete" -> 5;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Members");
                    List<Member> members = memberService.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members registered.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (name/email) or press Enter for all: ");
                        String search = readInput();
                        List<Member> filtered = members.stream()
                            .filter(m -> search.isEmpty() ||
                                m.getName().toLowerCase().contains(search) ||
                                m.getEmail().toLowerCase().contains(search))
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No members match your search.");
                        } else {
                            filtered.forEach(System.out::println);
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("View Members by Type");
                    System.out.println("Membership Types:");
                    System.out.println("1. STUDENT");
                    System.out.println("2. TEACHER");
                    System.out.println("3. PUBLIC");
                    System.out.println("4. STAFF");
                    System.out.print("Choose type to view (1-4): ");
                    int typeChoice = readInt();
                    String selectedType = switch (typeChoice) {
                        case 1 -> "STUDENT";
                        case 2 -> "TEACHER";
                        case 3 -> "PUBLIC";
                        case 4 -> "STAFF";
                        default -> null;
                    };
                    if (selectedType != null) {
                        List<Member> members = memberService.getMembersByType(selectedType);
                        if (members.isEmpty()) {
                            System.out.println("No " + selectedType.toLowerCase() + " members found.");
                        } else {
                            members.forEach(System.out::println);
                        }
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Register Member");
                    System.out.println("Processing... Registering new member.");
                    MemberFormState formState = new MemberFormState();
                    FormField<String> firstNameField = formField("First Name", true, formState::getFirstName, value -> requireNonEmpty("First Name", value), formState::setFirstName);
                    FormField<String> lastNameField = formField("Last Name", true, formState::getLastName, value -> requireNonEmpty("Last Name", value), formState::setLastName);
                    FormField<String> emailField = formField("Email", true, formState::getEmail, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Email", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (!candidate.contains("@")) {
                            return ValidationResult.failure("Email must contain '@'.");
                        }
                        if (emailExists(memberService, candidate, null)) {
                            return ValidationResult.failure("A member with this email already exists.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setEmail);
                    FormField<String> phoneField = formField("Phone", true, formState::getPhone, value -> requireNonEmpty("Phone", value), formState::setPhone);
                    FormField<String> addressField = formField("Address", false, formState::getAddress, Main::optionalText, formState::setAddress);

                    collectForm(List.of(firstNameField, lastNameField, emailField, phoneField, addressField));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    int genderChoice = readIntInRange("Choose gender (1-3): ", 1, 3);
                    if (genderChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String gender = switch (genderChoice) {
                        case 1 -> "Male";
                        case 2 -> "Female";
                        case 3 -> "Other";
                        default -> "Other";
                    };

                    LocalDate dateOfBirth = readDate("Date of Birth (YYYY-MM-DD): ", true);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Membership Types:");
                    System.out.println("1. STUDENT");
                    System.out.println("2. TEACHER");
                    System.out.println("3. PUBLIC");
                    System.out.println("4. STAFF");
                    int typeChoice = readIntInRange("Choose membership type (1-4): ", 1, 4);
                    if (typeChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String memberType = switch (typeChoice) {
                        case 1 -> "STUDENT";
                        case 2 -> "TEACHER";
                        case 3 -> "PUBLIC";
                        case 4 -> "STAFF";
                        default -> "PUBLIC";
                    };

                    try {
                        Member member = memberService.registerMember(
                            formState.getFirstName(),
                            formState.getLastName(),
                            gender,
                            dateOfBirth,
                            formState.getEmail(),
                            formState.getPhone(),
                            formState.getAddress(),
                            memberType
                        );
                        System.out.println("Member '" + member.getName() + "' registered successfully (ID: " + member.getMemberId() + ")");
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to register member: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Update Member");
                    List<Member> members = memberService.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members to update.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Members:");
                    for (int i = 0; i < members.size(); i++) {
                        Member m = members.get(i);
                        System.out.println((i + 1) + ". " + m.getName());
                    }
                    System.out.print("Choose member to update (1-" + members.size() + "): ");
                    int choice = readInt();
                    if (choice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    if (choice < 1 || choice > members.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Member memberToUpdate = members.get(choice - 1);

                    System.out.println("Current details:");
                    System.out.println(memberToUpdate);
                    MemberFormState formState = new MemberFormState();
                    formState.setFirstName(memberToUpdate.getFirstName());
                    formState.setLastName(memberToUpdate.getLastName());
                    formState.setEmail(memberToUpdate.getEmail());
                    formState.setPhone(memberToUpdate.getPhoneNumber());
                    formState.setAddress(memberToUpdate.getAddress());

                    FormField<String> firstNameField = formField("First Name", true, formState::getFirstName, value -> requireNonEmpty("First Name", value), formState::setFirstName);
                    FormField<String> lastNameField = formField("Last Name", true, formState::getLastName, value -> requireNonEmpty("Last Name", value), formState::setLastName);
                    FormField<String> emailField = formField("Email", true, formState::getEmail, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Email", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (!candidate.contains("@")) {
                            return ValidationResult.failure("Email must contain '@'.");
                        }
                        if (emailExists(memberService, candidate, memberToUpdate.getMemberId())) {
                            return ValidationResult.failure("A member with this email already exists.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setEmail);
                    FormField<String> phoneField = formField("Phone", true, formState::getPhone, value -> requireNonEmpty("Phone", value), formState::setPhone);
                    FormField<String> addressField = formField("Address", false, formState::getAddress, Main::optionalText, formState::setAddress);

                    collectForm(List.of(firstNameField, lastNameField, emailField, phoneField, addressField));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Current gender: " + memberToUpdate.getGender());
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    Integer genderChoice = readIntInRangeOptional("Choose new gender (1-3, Enter to keep current): ", 1, 3, null);
                    if (genderChoice != null && genderChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String gender = memberToUpdate.getGender();
                    if (genderChoice != null) {
                        gender = switch (genderChoice) {
                            case 1 -> "Male";
                            case 2 -> "Female";
                            case 3 -> "Other";
                            default -> gender;
                        };
                    }

                    LocalDate dateOfBirth = memberToUpdate.getDateOfBirth();
                    while (applicationRunning) {
                        System.out.print("Date of Birth [" + dateOfBirth + "] (YYYY-MM-DD, Enter to keep current): ");
                        String dobInput = readLineRaw();
                        if (dobInput.isEmpty()) {
                            break;
                        }
                        if (dobInput.equalsIgnoreCase("x") || dobInput.equalsIgnoreCase("exit")) {
                            attemptApplicationExit();
                            if (!applicationRunning) {
                                popBreadcrumb();
                                break;
                            }
                            continue;
                        }
                        try {
                            dateOfBirth = LocalDate.parse(dobInput);
                            break;
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date entered. Please use YYYY-MM-DD.");
                        }
                    }
                    if (!applicationRunning) {
                        break;
                    }

                    System.out.println("Current type: " + memberToUpdate.getMemberType());
                    System.out.println("Membership Types:");
                    System.out.println("1. STUDENT");
                    System.out.println("2. TEACHER");
                    System.out.println("3. PUBLIC");
                    System.out.println("4. STAFF");
                    Integer typeChoice = readIntInRangeOptional("Choose new membership type (1-4, Enter to keep current): ", 1, 4, null);
                    if (typeChoice != null && typeChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String memberType = memberToUpdate.getMemberType().toString();
                    if (typeChoice != null) {
                        memberType = switch (typeChoice) {
                            case 1 -> "STUDENT";
                            case 2 -> "TEACHER";
                            case 3 -> "PUBLIC";
                            case 4 -> "STAFF";
                            default -> memberType;
                        };
                    }

                    Member updatedMember = new Member(
                        memberToUpdate.getMemberId(),
                        memberToUpdate.getCardNumber(),
                        Member.MemberType.valueOf(memberType),
                        formState.getFirstName(),
                        formState.getLastName(),
                        gender,
                        dateOfBirth,
                        formState.getEmail(),
                        formState.getPhone(),
                        formState.getAddress(),
                        memberToUpdate.getDateJoined(),
                        memberToUpdate.isActive(),
                        memberToUpdate.getOutstandingFines(),
                        memberToUpdate.getMembershipStatus(),
                        memberToUpdate.getBorrowedBookIds(),
                        memberToUpdate.getTotalBooksBorrowed()
                    );

                    try {
                        boolean success = memberService.updateMember(updatedMember);
                        if (success) {
                            System.out.println("Member updated successfully!");
                        } else {
                            System.out.println("Failed to update member.");
                        }
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to update member: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 5 -> {
                    pushBreadcrumb("Delete Member");
                    List<Member> members = memberService.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Members:");
                    for (int i = 0; i < members.size(); i++) {
                        Member m = members.get(i);
                        System.out.println((i + 1) + ". " + m.getName());
                    }
                    System.out.print("Choose member to delete (1-" + members.size() + "): ");
                    int choice = readInt();
                    if (choice < 1 || choice > members.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Member memberToDelete = members.get(choice - 1);
                    System.out.print("Are you sure you want to delete '" + memberToDelete.getName() + "'? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        boolean success = memberService.deleteMember(memberToDelete.getMemberId());
                        if (success) {
                            System.out.println("Member deleted successfully!");
                        } else {
                            System.out.println("Failed to delete member.");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Librarian Menu ===
    private static void librarianMenu(LibrarianService librarianService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Librarians ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Librarians [V]");
            System.out.println("2. Add Librarian [A]");
            System.out.println("3. Update Librarian [U]");
            System.out.println("4. Delete Librarian [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "a", "add" -> 2;
                    case "u", "update" -> 3;
                    case "d", "delete" -> 4;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Librarians");
                    List<Librarian> librarians = librarianService.getAllLibrarians();
                    if (librarians.isEmpty()) {
                        System.out.println("No librarians added.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (name/email/username) or press Enter for all: ");
                        String search = readInput();
                        List<Librarian> filtered = librarians.stream()
                            .filter(l -> search.isEmpty() ||
                                l.getName().toLowerCase().contains(search) ||
                                l.getEmail().toLowerCase().contains(search) ||
                                l.getUsername().toLowerCase().contains(search))
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No librarians match your search.");
                        } else {
                            filtered.forEach(System.out::println);
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Add Librarian");
                    System.out.println("Processing... Adding new librarian.");
                    LibrarianFormState formState = new LibrarianFormState();
                    FormField<String> firstNameField = formField("First Name", true, formState::getFirstName, value -> requireNonEmpty("First Name", value), formState::setFirstName);
                    FormField<String> lastNameField = formField("Last Name", true, formState::getLastName, value -> requireNonEmpty("Last Name", value), formState::setLastName);
                    FormField<String> emailField = formField("Email", true, formState::getEmail, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Email", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (!candidate.contains("@")) {
                            return ValidationResult.failure("Email must contain '@'.");
                        }
                        boolean exists = librarianService.getAllLibrarians().stream()
                            .anyMatch(l -> l.getEmail() != null && l.getEmail().equalsIgnoreCase(candidate));
                        if (exists) {
                            return ValidationResult.failure("A librarian with this email already exists.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setEmail);
                    FormField<String> phoneField = formField("Phone", true, formState::getPhone, value -> requireNonEmpty("Phone", value), formState::setPhone);
                    FormField<String> addressField = formField("Address", false, formState::getAddress, Main::optionalText, formState::setAddress);
                    FormField<String> positionField = formField("Position", false, formState::getPosition, Main::optionalText, formState::setPosition);
                    FormField<String> usernameField = formField("Username", true, formState::getUsername, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Username", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (usernameExists(librarianService, candidate, null)) {
                            return ValidationResult.failure("Username is already in use.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setUsername);
                    FormField<String> passwordField = formField("Password", true, formState::getPassword, value -> requireNonEmpty("Password", value), formState::setPassword);

                    collectForm(List.of(firstNameField, lastNameField, emailField, phoneField, addressField, positionField, usernameField, passwordField));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    int genderChoice = readIntInRange("Choose gender (1-3): ", 1, 3);
                    if (genderChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String gender = switch (genderChoice) {
                        case 1 -> "Male";
                        case 2 -> "Female";
                        case 3 -> "Other";
                        default -> "Other";
                    };

                    LocalDate dateOfBirth = readDate("Date of Birth (YYYY-MM-DD): ", true);
                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Roles:");
                    System.out.println("1. ADMIN");
                    System.out.println("2. STAFF");
                    System.out.println("3. INTERN");
                    int roleChoice = readIntInRange("Choose role (1-3): ", 1, 3);
                    if (roleChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    LibrarianRole role = switch (roleChoice) {
                        case 1 -> LibrarianRole.ADMIN;
                        case 2 -> LibrarianRole.STAFF;
                        case 3 -> LibrarianRole.INTERN;
                        default -> LibrarianRole.STAFF;
                    };

                    double salary = readPositiveDouble("Salary: ");
                    if (Double.isNaN(salary)) {
                        popBreadcrumb();
                        break;
                    }

                    try {
                        Librarian librarian = librarianService.addLibrarian(
                            formState.getFirstName(),
                            formState.getLastName(),
                            gender,
                            dateOfBirth,
                            formState.getEmail(),
                            formState.getPhone(),
                            formState.getAddress(),
                            formState.getPosition(),
                            salary,
                            role,
                            formState.getUsername(),
                            formState.getPassword()
                        );
                        System.out.println("Librarian '" + librarian.getName() + "' added successfully (ID: " + librarian.getLibrarianId() + ")");
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to add librarian: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Update Librarian");
                    List<Librarian> librarians = librarianService.getAllLibrarians();
                    if (librarians.isEmpty()) {
                        System.out.println("No librarians to update.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Librarians:");
                    for (int i = 0; i < librarians.size(); i++) {
                        Librarian l = librarians.get(i);
                        System.out.println((i + 1) + ". " + l.getName());
                    }
                    System.out.print("Choose librarian to update (1-" + librarians.size() + "): ");
                    int choice = readInt();
                    if (choice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    if (choice < 1 || choice > librarians.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Librarian librarianToUpdate = librarians.get(choice - 1);

                    System.out.println("Current details:");
                    System.out.println(librarianToUpdate);
                    LibrarianFormState formState = new LibrarianFormState();
                    formState.setFirstName(librarianToUpdate.getFirstName());
                    formState.setLastName(librarianToUpdate.getLastName());
                    formState.setEmail(librarianToUpdate.getEmail());
                    formState.setPhone(librarianToUpdate.getPhoneNumber());
                    formState.setAddress(librarianToUpdate.getAddress());
                    formState.setPosition(librarianToUpdate.getPosition());
                    formState.setUsername(librarianToUpdate.getUsername());
                    formState.setPassword(librarianToUpdate.getPasswordHash());

                    FormField<String> firstNameField = formField("First Name", true, formState::getFirstName, value -> requireNonEmpty("First Name", value), formState::setFirstName);
                    FormField<String> lastNameField = formField("Last Name", true, formState::getLastName, value -> requireNonEmpty("Last Name", value), formState::setLastName);
                    FormField<String> emailField = formField("Email", true, formState::getEmail, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Email", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (!candidate.contains("@")) {
                            return ValidationResult.failure("Email must contain '@'.");
                        }
                        boolean exists = librarianService.getAllLibrarians().stream()
                            .anyMatch(l -> l.getEmail() != null && l.getEmail().equalsIgnoreCase(candidate) && l.getLibrarianId() != librarianToUpdate.getLibrarianId());
                        if (exists) {
                            return ValidationResult.failure("A librarian with this email already exists.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setEmail);
                    FormField<String> phoneField = formField("Phone", true, formState::getPhone, value -> requireNonEmpty("Phone", value), formState::setPhone);
                    FormField<String> addressField = formField("Address", false, formState::getAddress, Main::optionalText, formState::setAddress);
                    FormField<String> positionField = formField("Position", false, formState::getPosition, Main::optionalText, formState::setPosition);
                    FormField<String> usernameField = formField("Username", true, formState::getUsername, candidate -> {
                        ValidationResult<String> base = requireNonEmpty("Username", candidate);
                        if (!base.valid()) {
                            return base;
                        }
                        if (usernameExists(librarianService, candidate, librarianToUpdate.getLibrarianId())) {
                            return ValidationResult.failure("Username is already in use.");
                        }
                        return ValidationResult.success(candidate);
                    }, formState::setUsername);

                    collectForm(List.of(firstNameField, lastNameField, emailField, phoneField, addressField, positionField, usernameField));

                    if (!applicationRunning) {
                        popBreadcrumb();
                        break;
                    }

                    System.out.println("Current gender: " + librarianToUpdate.getGender());
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    Integer genderChoice = readIntInRangeOptional("Choose new gender (1-3, Enter to keep current): ", 1, 3, null);
                    if (genderChoice != null && genderChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    String gender = librarianToUpdate.getGender();
                    if (genderChoice != null) {
                        gender = switch (genderChoice) {
                            case 1 -> "Male";
                            case 2 -> "Female";
                            case 3 -> "Other";
                            default -> gender;
                        };
                    }

                    LocalDate dateOfBirth = librarianToUpdate.getDateOfBirth();
                    while (applicationRunning) {
                        System.out.print("Date of Birth [" + dateOfBirth + "] (YYYY-MM-DD, Enter to keep current): ");
                        String dobInput = readLineRaw();
                        if (dobInput.isEmpty()) {
                            break;
                        }
                        if (dobInput.equalsIgnoreCase("x") || dobInput.equalsIgnoreCase("exit")) {
                            attemptApplicationExit();
                            if (!applicationRunning) {
                                popBreadcrumb();
                                break;
                            }
                            continue;
                        }
                        try {
                            dateOfBirth = LocalDate.parse(dobInput);
                            break;
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date entered. Please use YYYY-MM-DD.");
                        }
                    }
                    if (!applicationRunning) {
                        break;
                    }

                    System.out.println("Current role: " + librarianToUpdate.getRole());
                    System.out.println("Roles:");
                    System.out.println("1. ADMIN");
                    System.out.println("2. STAFF");
                    System.out.println("3. INTERN");
                    Integer roleChoice = readIntInRangeOptional("Choose new role (1-3, Enter to keep current): ", 1, 3, null);
                    if (roleChoice != null && roleChoice == Integer.MIN_VALUE) {
                        popBreadcrumb();
                        break;
                    }
                    LibrarianRole role = librarianToUpdate.getRole();
                    if (roleChoice != null) {
                        role = switch (roleChoice) {
                            case 1 -> LibrarianRole.ADMIN;
                            case 2 -> LibrarianRole.STAFF;
                            case 3 -> LibrarianRole.INTERN;
                            default -> role;
                        };
                    }

                    double salary = librarianToUpdate.getSalary();
                    while (applicationRunning) {
                        System.out.print("Salary [" + salary + "]: ");
                        String salaryInput = readLineRaw();
                        if (salaryInput.isEmpty()) {
                            break;
                        }
                        if (salaryInput.equalsIgnoreCase("x") || salaryInput.equalsIgnoreCase("exit")) {
                            attemptApplicationExit();
                            if (!applicationRunning) {
                                popBreadcrumb();
                                break;
                            }
                            continue;
                        }
                        try {
                            double parsedSalary = Double.parseDouble(salaryInput);
                            if (parsedSalary < 0) {
                                System.out.println("Salary cannot be negative.");
                                continue;
                            }
                            salary = parsedSalary;
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid salary. Please enter a numeric value.");
                        }
                    }
                    if (!applicationRunning) {
                        break;
                    }

                    Librarian updatedLibrarian = new Librarian(
                        librarianToUpdate.getLibrarianId(),
                        librarianToUpdate.getStaffCode(),
                        formState.getFirstName(),
                        formState.getLastName(),
                        gender,
                        dateOfBirth,
                        formState.getEmail(),
                        formState.getPhone(),
                        formState.getAddress(),
                        librarianToUpdate.getDateHired(),
                        formState.getPosition(),
                        salary,
                        librarianToUpdate.isActive(),
                        role,
                        formState.getUsername(),
                        librarianToUpdate.getPasswordHash(),
                        librarianToUpdate.getLastLoginDate()
                    );

                    try {
                        boolean success = librarianService.updateLibrarian(updatedLibrarian);
                        if (success) {
                            System.out.println("Librarian updated successfully!");
                        } else {
                            System.out.println("Failed to update librarian.");
                        }
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to update librarian: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Delete Librarian");
                    List<Librarian> librarians = librarianService.getAllLibrarians();
                    if (librarians.isEmpty()) {
                        System.out.println("No librarians to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Librarians:");
                    for (int i = 0; i < librarians.size(); i++) {
                        Librarian l = librarians.get(i);
                        System.out.println((i + 1) + ". " + l.getName());
                    }
                    System.out.print("Choose librarian to delete (1-" + librarians.size() + "): ");
                    int choice = readInt();
                    if (choice < 1 || choice > librarians.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Librarian librarianToDelete = librarians.get(choice - 1);
                    System.out.print("Are you sure you want to delete '" + librarianToDelete.getName() + "'? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        boolean success = librarianService.deleteLibrarian(librarianToDelete.getLibrarianId());
                        if (success) {
                            System.out.println("Librarian deleted successfully!");
                        } else {
                            System.out.println("Failed to delete librarian.");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Reservation Menu ===
    private static void reservationMenu(ReservationService reservationService, BookService bookService, MemberService memberService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Reservations ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Reservations [V]");
            System.out.println("2. Make Reservation [A]");
            System.out.println("3. Fulfill Reservation [F]");
            System.out.println("4. Cancel Reservation [C]");
            System.out.println("5. Delete Reservation [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "a", "add", "make" -> 2;
                    case "f", "fulfill" -> 3;
                    case "c", "cancel" -> 4;
                    case "d", "delete" -> 5;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Reservations");
                    List<Reservation> reservations = reservationService.getAllReservations();
                    if (reservations.isEmpty()) {
                        System.out.println("No reservations found.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (book title/member name) or press Enter for all: ");
                        String search = readInput();
                        List<Reservation> filtered = reservations.stream()
                            .filter(r -> {
                                Optional<Book> bookOpt = bookService.getBookById(r.getBookId());
                                Optional<Member> memberOpt = memberService.getMemberById(r.getMemberId());
                                String bookTitle = bookOpt.map(Book::getTitle).orElse("");
                                String memberName = memberOpt.map(Member::getName).orElse("");
                                return search.isEmpty() ||
                                    bookTitle.toLowerCase().contains(search) ||
                                    memberName.toLowerCase().contains(search);
                            })
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No reservations match your search.");
                        } else {
                            filtered.forEach(r -> {
                                Optional<Book> bookOpt = bookService.getBookById(r.getBookId());
                                Optional<Member> memberOpt = memberService.getMemberById(r.getMemberId());
                                String bookTitle = bookOpt.map(Book::getTitle).orElse("Unknown Book");
                                String memberName = memberOpt.map(Member::getName).orElse("Unknown Member");
                                System.out.println("Reservation #" + r.getReservationId() + " — Book: " + bookTitle + " — Member: " + memberName + " — Status: " + r.getStatus());
                            });
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Make Reservation");
                    System.out.println("Processing... Making new reservation.");

                    // List all books
                    List<Book> books = bookService.getAllBooks();
                    if (books.isEmpty()) {
                        System.out.println("No books available!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Books:");
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        System.out.println((i + 1) + ". " + b.getTitle() + " by " + b.getAuthor());
                    }
                    System.out.print("Choose book (1-" + books.size() + "): ");
                    int bookChoice = readInt();
                    if (bookChoice < 1 || bookChoice > books.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    int bookId = books.get(bookChoice - 1).getId();

                    // List members
                    List<Member> members = memberService.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members registered!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Members:");
                    for (int i = 0; i < members.size(); i++) {
                        Member m = members.get(i);
                        System.out.println((i + 1) + ". " + m.getName());
                    }
                    System.out.print("Choose member (1-" + members.size() + "): ");
                    int memberChoice = readInt();
                    if (memberChoice < 1 || memberChoice > members.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    int memberId = members.get(memberChoice - 1).getMemberId();

                    boolean success = reservationService.createReservation(bookId, memberId);
                    if (success) {
                        System.out.println("Reservation created!");
                    } else {
                        System.out.println("Failed to create reservation. Book not found.");
                    }
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Fulfill Reservation");
                    List<Reservation> pending = reservationService.getReservationsByStatus("Pending");
                    if (pending.isEmpty()) {
                        System.out.println("No pending reservations.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Pending Reservations:");
                    for (int i = 0; i < pending.size(); i++) {
                        Reservation r = pending.get(i);
                        Optional<Book> bookOpt = bookService.getBookById(r.getBookId());
                        Optional<Member> memberOpt = memberService.getMemberById(r.getMemberId());
                        String bookTitle = bookOpt.map(Book::getTitle).orElse("Unknown Book");
                        String memberName = memberOpt.map(Member::getName).orElse("Unknown Member");
                        System.out.println((i + 1) + ". Reservation #" + r.getReservationId() + " — Book: " + bookTitle + " — Member: " + memberName);
                    }
                    System.out.print("Choose reservation to fulfill (1-" + pending.size() + "): ");
                    int choice = readInt();
                    if (choice >= 1 && choice <= pending.size()) {
                        Reservation r = pending.get(choice - 1);
                        boolean success = reservationService.fulfillReservation(r.getReservationId());
                        if (success) {
                            System.out.println("Reservation fulfilled!");
                        } else {
                            System.out.println("Failed to fulfill reservation.");
                        }
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Cancel Reservation");
                    List<Reservation> allRes = reservationService.getAllReservations();
                    if (allRes.isEmpty()) {
                        System.out.println("No reservations to cancel.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("All Reservations:");
                    for (int i = 0; i < allRes.size(); i++) {
                        Reservation r = allRes.get(i);
                        Optional<Book> bookOpt = bookService.getBookById(r.getBookId());
                        Optional<Member> memberOpt = memberService.getMemberById(r.getMemberId());
                        String bookTitle = bookOpt.map(Book::getTitle).orElse("Unknown Book");
                        String memberName = memberOpt.map(Member::getName).orElse("Unknown Member");
                        System.out.println((i + 1) + ". Reservation #" + r.getReservationId() + " — Book: " + bookTitle + " — Member: " + memberName + " — Status: " + r.getStatus());
                    }
                    System.out.print("Choose reservation to cancel (1-" + allRes.size() + "): ");
                    int choice = readInt();
                    if (choice >= 1 && choice <= allRes.size()) {
                        Reservation r = allRes.get(choice - 1);
                        boolean success = reservationService.cancelReservation(r.getReservationId());
                        if (success) {
                            System.out.println("Reservation cancelled!");
                        } else {
                            System.out.println("Failed to cancel reservation.");
                        }
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    popBreadcrumb();
                }
                case 5 -> {
                    pushBreadcrumb("Delete Reservation");
                    List<Reservation> allRes = reservationService.getAllReservations();
                    if (allRes.isEmpty()) {
                        System.out.println("No reservations to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("All Reservations:");
                    for (int i = 0; i < allRes.size(); i++) {
                        Reservation r = allRes.get(i);
                        Optional<Book> bookOpt = bookService.getBookById(r.getBookId());
                        Optional<Member> memberOpt = memberService.getMemberById(r.getMemberId());
                        String bookTitle = bookOpt.map(Book::getTitle).orElse("Unknown Book");
                        String memberName = memberOpt.map(Member::getName).orElse("Unknown Member");
                        System.out.println((i + 1) + ". Reservation #" + r.getReservationId() + " — Book: " + bookTitle + " — Member: " + memberName + " — Status: " + r.getStatus());
                    }
                    System.out.print("Choose reservation to delete (1-" + allRes.size() + "): ");
                    int choice = readInt();
                    if (choice >= 1 && choice <= allRes.size()) {
                        Reservation r = allRes.get(choice - 1);
                        System.out.print("Are you sure you want to delete reservation #" + r.getReservationId() + "? (y/n): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        if (confirm.equals("y") || confirm.equals("yes")) {
                            boolean success = reservationService.deleteReservation(r.getReservationId());
                            if (success) {
                                System.out.println("Reservation deleted!");
                            } else {
                                System.out.println("Failed to delete reservation.");
                            }
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Loan Menu ===
    private static void loanMenu(LoanService loanService, BookService bookService,
                             MemberService memberService, FineService fineService,
                             ReservationService reservationService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Loans ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Loans [V]");
            System.out.println("2. Issue Loan [I]");
            System.out.println("3. Return Loan [R]");
            System.out.println("4. Delete Loan [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "i", "issue" -> 2;
                    case "r", "return" -> 3;
                    case "d", "delete" -> 4;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Loans");
                    List<Loan> loans = loanService.getAllLoans();
                    if (loans.isEmpty()) {
                        System.out.println("No loans found.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (book title/member name) or press Enter for all: ");
                        String search = readInput();
                        List<Loan> filtered = loans.stream()
                            .filter(l -> {
                                Book book = bookService.getBookById(l.getBookId()).orElse(null);
                                Member member = memberService.getMemberById(l.getMemberId()).orElse(null);
                                String bookTitle = book != null ? book.getTitle() : "";
                                String memberName = member != null ? member.getName() : "";
                                return search.isEmpty() ||
                                    bookTitle.toLowerCase().contains(search) ||
                                    memberName.toLowerCase().contains(search);
                            })
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No loans match your search.");
                        } else {
                            filtered.forEach(System.out::println);
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Issue Loan");
                    System.out.println("Processing... Issuing new loan.");

                    // List available books
                    List<Book> availableBooks = bookService.getAllBooks().stream()
                        .filter(Book::isAvailable)
                        .toList();
                    if (availableBooks.isEmpty()) {
                        System.out.println("No available books!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Available Books:");
                    for (int i = 0; i < availableBooks.size(); i++) {
                        Book b = availableBooks.get(i);
                        System.out.println((i + 1) + ". " + b.getTitle() + " by " + b.getAuthor());
                    }
                    System.out.print("Choose book (1-" + availableBooks.size() + "): ");
                    int bookChoice = readInt();
                    if (bookChoice < 1 || bookChoice > availableBooks.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Book book = availableBooks.get(bookChoice - 1);

                    // List members
                    List<Member> members = memberService.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members registered!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Members:");
                    for (int i = 0; i < members.size(); i++) {
                        Member m = members.get(i);
                        System.out.println((i + 1) + ". " + m.getName());
                    }
                    System.out.print("Choose member (1-" + members.size() + "): ");
                    int memberChoice = readInt();
                    if (memberChoice < 1 || memberChoice > members.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Member member = members.get(memberChoice - 1);
                    int memberId = member.getMemberId();

                    // Check for unpaid fines
                    if (fineService.hasUnpaidFines(memberId)) {
                        System.out.println("Loan blocked! Member has unpaid fines.");
                        popBreadcrumb();
                        break;
                    }

                    boolean issued = loanService.issueLoan(book.getId(), memberId, LocalDate.now());
                    if (issued) {
                        System.out.println("Loan issued!");
                    } else {
                        System.out.println("Unable to issue loan. Verify that the book and member are eligible.");
                    }
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Return Loan");
                    // List active loans
                    List<Loan> activeLoans = loanService.getActiveLoans();
                    if (activeLoans.isEmpty()) {
                        System.out.println("No active loans!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Active Loans:");
                    for (int i = 0; i < activeLoans.size(); i++) {
                        Loan l = activeLoans.get(i);
                        Book book = bookService.getBookById(l.getBookId()).orElse(null);
                        Member member = memberService.getMemberById(l.getMemberId()).orElse(null);
                        String bookTitle = book != null ? book.getTitle() : "Unknown Book";
                        String memberName = member != null ? member.getName() : "Unknown Member";
                        System.out.println((i + 1) + ". " + bookTitle + " - " + memberName + " (Due: " + l.getDueDate() + ")");
                    }
                    System.out.print("Choose loan to return (1-" + activeLoans.size() + "): ");
                    int loanChoice = readInt();
                    if (loanChoice < 1 || loanChoice > activeLoans.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Loan loan = activeLoans.get(loanChoice - 1);
                    int loanId = loan.getLoanId();

                    LocalDate returnDate = LocalDate.now();
                    boolean completed = loanService.returnLoan(loanId, returnDate);
                    if (!completed) {
                        System.out.println("Unable to process loan return.");
                        popBreadcrumb();
                        break;
                    }

                    // Check if fine was applied (loan service handles fine creation internally)
                    long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), returnDate);
                    if (overdueDays > 0) {
                        System.out.println("Book returned. Overdue fine applied for " + overdueDays + " days.");
                    } else {
                        System.out.println("Book returned on time.");
                    }

                    Book returnedBook = bookService.getBookById(loan.getBookId()).orElse(null);
                    if (returnedBook != null) {
                        // Update book condition
                        System.out.println("Update book condition:");
                        System.out.println("1. NEW");
                        System.out.println("2. GOOD");
                        System.out.println("3. FAIR");
                        System.out.println("4. POOR");
                        System.out.print("Choose new condition (1-4): ");
                        int condChoice = readInt();
                        String newCondition = switch (condChoice) {
                            case 1 -> "NEW";
                            case 2 -> "GOOD";
                            case 3 -> "FAIR";
                            case 4 -> "POOR";
                            default -> returnedBook.getCondition().toString();
                        };
                        returnedBook.setCondition(Book.Condition.valueOf(newCondition));

                        // Check reservation queue
                        Reservation nextReservation = reservationService.getNextReservation(returnedBook.getId());
                        if (nextReservation != null) {
                            if (fineService.hasUnpaidFines(nextReservation.getMemberId())) {
                                System.out.println("Next member in queue has unpaid fines. Book remains available.");
                                returnedBook.setAvailable(true);
                            } else {
                                System.out.println("Book is reserved! Assigning to member ID: " + nextReservation.getMemberId());
                                boolean autoIssued = loanService.issueLoan(returnedBook.getId(), nextReservation.getMemberId(), LocalDate.now());
                                if (autoIssued) {
                                    reservationService.fulfillReservation(nextReservation.getReservationId());
                                } else {
                                    System.out.println("Automatic checkout failed. Reservation remains pending.");
                                }
                            }
                        } else {
                            returnedBook.setAvailable(true);
                        }
                        bookService.updateBook(returnedBook);
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Delete Loan");
                    List<Loan> loans = loanService.getAllLoans();
                    if (loans.isEmpty()) {
                        System.out.println("No loans to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Loans:");
                    for (int i = 0; i < loans.size(); i++) {
                        Loan l = loans.get(i);
                        Book book = bookService.getBookById(l.getBookId()).orElse(null);
                        Member member = memberService.getMemberById(l.getMemberId()).orElse(null);
                        String bookTitle = book != null ? book.getTitle() : "Unknown Book";
                        String memberName = member != null ? member.getName() : "Unknown Member";
                        System.out.println((i + 1) + ". " + bookTitle + " - " + memberName + " (Due: " + l.getDueDate() + ")");
                    }
                    System.out.print("Choose loan to delete (1-" + loans.size() + "): ");
                    int choice = readInt();
                    if (choice < 1 || choice > loans.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Loan loanToDelete = loans.get(choice - 1);
                    System.out.print("Are you sure you want to delete this loan? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        boolean success = loanService.deleteLoan(loanToDelete.getLoanId());
                        if (success) {
                            System.out.println("Loan deleted successfully!");
                        } else {
                            System.out.println("Failed to delete loan.");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Fine & Payment Menu ===
    private static void finePaymentMenu(FineService fineService, PaymentService paymentService, LoanService loanService, BookService bookService, MemberService memberService) {
        boolean inMenu = true;
        while (inMenu && applicationRunning) {
            System.out.println("\n--- Fines & Payments ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Fines [V]");
            System.out.println("2. Calculate Fine [C]");
            System.out.println("3. Pay Fine [P]");
            System.out.println("4. Delete Fine [D]");
            System.out.println("0. Back");
            System.out.println("x. Exit");
            System.out.println("?. Help");
            System.out.print("Select an option (number or shortcut): ");

            String input = readInput();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                option = switch (input) {
                    case "v", "view" -> 1;
                    case "c", "calculate" -> 2;
                    case "p", "pay" -> 3;
                    case "d", "delete" -> 4;
                    case "x", "exit" -> -3;
                    case "?" -> -1;
                    default -> -2;
                };
            }

            switch (option) {
                case 1 -> {
                    pushBreadcrumb("View All Fines");
                    List<Fine> fines = fineService.getAllFines();
                    if (fines.isEmpty()) {
                        System.out.println("No fines recorded.");
                    } else {
                        // Simple search
                        System.out.print("Enter search term (member name/reason) or press Enter for all: ");
                        String search = readInput();
                        List<Fine> filtered = fines.stream()
                            .filter(f -> {
                                Member member = memberService.getMemberById(f.getMemberId()).orElse(null);
                                String memberName = member != null ? member.getName() : "";
                                String reason = f.getReason() != null ? f.getReason() : "";
                                return search.isEmpty() ||
                                    memberName.toLowerCase().contains(search) ||
                                    reason.toLowerCase().contains(search);
                            })
                            .toList();
                        if (filtered.isEmpty()) {
                            System.out.println("No fines match your search.");
                        } else {
                            filtered.forEach(System.out::println);
                        }
                    }
                    popBreadcrumb();
                }
                case 2 -> {
                    pushBreadcrumb("Calculate Fine");
                    System.out.println("Processing... Calculating fine.");

                    // List all loans
                    List<Loan> loans = loanService.getAllLoans();
                    if (loans.isEmpty()) {
                        System.out.println("No loans found!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Loans:");
                    for (int i = 0; i < loans.size(); i++) {
                        Loan l = loans.get(i);
                        Book book = bookService.getBookById(l.getBookId()).orElse(null);
                        Member member = memberService.getMemberById(l.getMemberId()).orElse(null);
                        String bookTitle = book != null ? book.getTitle() : "Unknown Book";
                        String memberName = member != null ? member.getName() : "Unknown Member";
                        System.out.println((i + 1) + ". " + bookTitle + " - " + memberName + " (Due: " + l.getDueDate() + ")");
                    }
                    System.out.print("Choose loan (1-" + loans.size() + "): ");
                    int loanChoice = readInt();
                    if (loanChoice < 1 || loanChoice > loans.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Loan loan = loans.get(loanChoice - 1);
                    fineService.calculateFine(loan);
                    System.out.println("Fine calculated!");
                    popBreadcrumb();
                }
                case 3 -> {
                    pushBreadcrumb("Pay Fine");
                    // List unpaid fines
                    List<Fine> unpaidFines = fineService.getAllFines().stream()
                        .filter(f -> !f.isPaid())
                        .toList();
                    if (unpaidFines.isEmpty()) {
                        System.out.println("No unpaid fines!");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Unpaid Fines:");
                    for (int i = 0; i < unpaidFines.size(); i++) {
                        Fine f = unpaidFines.get(i);
                        Member member = memberService.getMemberById(f.getMemberId()).orElse(null);
                        String memberName = member != null ? member.getName() : "Unknown Member";
                        String reason = f.getReason() != null ? f.getReason() : "Unknown";
                        System.out.println((i + 1) + ". " + memberName + " - Amount: " + f.getAmount() + " - Reason: " + reason);
                    }
                    System.out.print("Choose fine (1-" + unpaidFines.size() + "): ");
                    int fineChoice = readInt();
                    if (fineChoice < 1 || fineChoice > unpaidFines.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Fine fine = unpaidFines.get(fineChoice - 1);
                    int fineId = fine.getFineId();
                    double amount = readPositiveDouble("Amount: ");
                    String method = promptRequired("Payment Method: ");
                    LocalDate paymentDate = LocalDate.now();
                    try {
                        boolean success = paymentService.payFine(fineId, amount, paymentDate, method);
                        if (success) {
                            System.out.println("Payment recorded!");
                        } else {
                            System.out.println("Payment could not be recorded. Verify the fine status and amount.");
                        }
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Unable to record payment: " + ex.getMessage());
                    }
                    popBreadcrumb();
                }
                case 4 -> {
                    pushBreadcrumb("Delete Fine");
                    List<Fine> fines = fineService.getAllFines();
                    if (fines.isEmpty()) {
                        System.out.println("No fines to delete.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.println("Fines:");
                    for (int i = 0; i < fines.size(); i++) {
                        Fine f = fines.get(i);
                        Member member = memberService.getMemberById(f.getMemberId()).orElse(null);
                        String memberName = member != null ? member.getName() : "Unknown Member";
                        String reason = f.getReason() != null ? f.getReason() : "Unknown";
                        System.out.println((i + 1) + ". " + memberName + " - Amount: " + f.getAmount() + " - Reason: " + reason + " - Paid: " + f.isPaid());
                    }
                    System.out.print("Choose fine to delete (1-" + fines.size() + "): ");
                    int choice = readInt();
                    if (choice < 1 || choice > fines.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Fine fineToDelete = fines.get(choice - 1);
                    System.out.print("Are you sure you want to delete this fine? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        boolean success = fineService.deleteFine(fineToDelete.getFineId());
                        if (success) {
                            System.out.println("Fine deleted successfully!");
                        } else {
                            System.out.println("Failed to delete fine.");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    popBreadcrumb();
                }
                case 0 -> inMenu = false;
                case -3 -> {
                    attemptApplicationExit();
                    if (!applicationRunning) {
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Utility Methods ===
    private static void attemptApplicationExit() {
        if (confirm("Exit the Library System")) {
            applicationRunning = false;
            System.out.println("Exiting Library System. Goodbye!");
        }
    }

    private static boolean confirm(String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String response = readInput();
            if (response.equals("y") || response.equals("yes")) {
                return true;
            }
            if (response.equals("n") || response.equals("no")) {
                return false;
            }
            System.out.println("Please enter 'y' or 'n'.");
        }
    }

    private static String promptForField(String label, String defaultValue, boolean required) {
        while (applicationRunning) {
            String prompt = defaultValue != null && !defaultValue.isEmpty()
                ? label + " [" + defaultValue + "]: "
                : label + (required ? " *: " : ": ");
            System.out.print(prompt);
            String input = sanitizeText(readLineRaw());
            if (input.isEmpty()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                if (!required) {
                    return "";
                }
                System.out.println("Input is required. Please try again.");
                continue;
            }
            return input;
        }
        return defaultValue != null ? defaultValue : "";
    }

    private static void collectForm(List<FormField<?>> fields) {
        List<FormField<?>> pending = new ArrayList<>(fields);
        while (!pending.isEmpty() && applicationRunning) {
            FormField<?> field = pending.remove(0);
            boolean success = processFormField(field);
            if (!success) {
                pending.add(field);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean processFormField(FormField<?> genericField) {
        FormField<T> field = (FormField<T>) genericField;
        String defaultValue = field.defaultSupplier() != null ? sanitizeText(field.defaultSupplier().get()) : null;
        String response = promptForField(field.label(), defaultValue, field.required());
        ValidationResult<T> result = field.parser().apply(response);
        if (result.valid()) {
            try {
                field.consumer().accept(result.value());
                return true;
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        }
        System.out.println(result.message());
        return false;
    }

    private static <T> FormField<T> formField(String label, boolean required, Function<String, ValidationResult<T>> parser, Consumer<T> consumer) {
        return new FormField<>(label, required, null, parser, consumer);
    }

    private static <T> FormField<T> formField(String label, boolean required, Supplier<String> defaultSupplier, Function<String, ValidationResult<T>> parser, Consumer<T> consumer) {
        return new FormField<>(label, required, defaultSupplier, parser, consumer);
    }

    private static ValidationResult<String> requireNonEmpty(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return ValidationResult.failure(fieldName + " is required.");
        }
        return ValidationResult.success(value);
    }

    private static ValidationResult<String> optionalText(String value) {
        return ValidationResult.success(value == null ? "" : value);
    }

    private static ValidationResult<List<String>> parseKeywordList(String value) {
        List<String> keywords = new ArrayList<>();
        if (value != null && !value.isBlank()) {
            String[] parts = value.split(",");
            for (String part : parts) {
                String cleaned = sanitizeText(part);
                if (!cleaned.isEmpty()) {
                    keywords.add(cleaned);
                }
            }
        }
        return ValidationResult.success(keywords);
    }

    private static boolean emailExists(MemberService memberService, String email, Integer ignoreId) {
        if (memberService == null) {
            return false;
        }
        return memberService.getAllMembers().stream()
            .anyMatch(member -> member.getEmail().equalsIgnoreCase(email) && (ignoreId == null || member.getMemberId() != ignoreId));
    }

    private static boolean isbnExists(BookService bookService, String isbn, Integer ignoreId) {
        if (bookService == null) {
            return false;
        }
        return bookService.getAllBooks().stream()
            .anyMatch(book -> book.getIsbn().equalsIgnoreCase(isbn) && (ignoreId == null || book.getId() != ignoreId));
    }

    private static boolean usernameExists(LibrarianService librarianService, String username, Integer ignoreId) {
        if (librarianService == null) {
            return false;
        }
        return librarianService.getAllLibrarians().stream()
            .anyMatch(librarian -> {
                boolean matches = librarian.getUsername() != null && librarian.getUsername().equalsIgnoreCase(username);
                if (!matches) {
                    return false;
                }
                return ignoreId == null || librarian.getLibrarianId() != ignoreId;
            });
    }

    private record FormField<T>(String label, boolean required, Supplier<String> defaultSupplier,
                                Function<String, ValidationResult<T>> parser, Consumer<T> consumer) {
    }

    private record ValidationResult<T>(boolean valid, T value, String message) {
        static <T> ValidationResult<T> success(T value) {
            return new ValidationResult<>(true, value, null);
        }

        static <T> ValidationResult<T> failure(String message) {
            return new ValidationResult<>(false, null, message);
        }
    }

    private static final class BookFormState {
        private String title;
        private String author;
        private String isbn;
        private String publisher;
        private String location;
        private List<String> keywords = new ArrayList<>();

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        String getAuthor() {
            return author;
        }

        void setAuthor(String author) {
            this.author = author;
        }

        String getIsbn() {
            return isbn;
        }

        void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        String getPublisher() {
            return publisher;
        }

        void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        String getLocation() {
            return location;
        }

        void setLocation(String location) {
            this.location = location;
        }

        List<String> getKeywords() {
            return keywords;
        }

        void setKeywords(List<String> keywords) {
            this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
        }
    }

    private static final class MemberFormState {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;

        String getFirstName() {
            return firstName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }

        String getEmail() {
            return email;
        }

        void setEmail(String email) {
            this.email = email;
        }

        String getPhone() {
            return phone;
        }

        void setPhone(String phone) {
            this.phone = phone;
        }

        String getAddress() {
            return address;
        }

        void setAddress(String address) {
            this.address = address;
        }
    }

    private static final class LibrarianFormState {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String position;
        private String username;
        private String password;

        String getFirstName() {
            return firstName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }

        String getEmail() {
            return email;
        }

        void setEmail(String email) {
            this.email = email;
        }

        String getPhone() {
            return phone;
        }

        void setPhone(String phone) {
            this.phone = phone;
        }

        String getAddress() {
            return address;
        }

        void setAddress(String address) {
            this.address = address;
        }

        String getPosition() {
            return position;
        }

        void setPosition(String position) {
            this.position = position;
        }

        String getUsername() {
            return username;
        }

        void setUsername(String username) {
            this.username = username;
        }

        String getPassword() {
            return password;
        }

        void setPassword(String password) {
            this.password = password;
        }
    }

    // === Help Method ===
    private static void showHelp() {
        System.out.println("\n=== Help ===");
        System.out.println("Navigation:");
        System.out.println("- Use numbers (1-6) or letters (B/M/L/R/Ln/F) to select menus");
        System.out.println("- '0' or 'X' to exit");
        System.out.println("- '?' for this help");
        System.out.println("- 'B' or 'Back' to go back in sub-menus");
        System.out.println("- 'M' or 'Main' to return to main menu from sub-menus");
        System.out.println("Shortcuts in sub-menus:");
        System.out.println("- A: Add, V: View, U: Update, D: Delete, B: Back, M: Main Menu");
        System.out.println("Press Enter to continue...");
        readInput();
    }
}
