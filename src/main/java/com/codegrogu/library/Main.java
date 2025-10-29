package com.codegrogu.library;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
    private static String readInput() {
        String input = scanner.nextLine().trim().toLowerCase();
        return input;
    }

    private static int readInt() {
        while (true) {
            try {
                String input = readInput();
                if (input.equals("b") || input.equals("back")) {
                    return 0; // Treat 'b' or 'back' as back
                }
                if (input.equals("m") || input.equals("main")) {
                    return 9; // Treat 'm' or 'main' as back to main
                }
                return Integer.parseInt(input);
            } catch (Exception e) {
                System.out.print("Please enter a valid number or shortcut: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(readInput());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
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

        boolean running = true;

        while (running) {
            System.out.println("\n=== Library Management System ===");
            System.out.println("Session started: " + sessionStart.toLocalDate() + " at " + sessionStart.toLocalTime().toString().substring(0, 8));
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. Books (B)");
            System.out.println("2. Members (M)");
            System.out.println("3. Librarians (L)");
            System.out.println("4. Reservations (R)");
            System.out.println("5. Loans (Ln)");
            System.out.println("6. Fines & Payments (F)");
            System.out.println("0. Exit (X)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "ln", "loans" -> 5;
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
                case 0 -> {
                    System.out.print("Are you sure you want to exit? (y/n): ");
                    String confirm = readInput();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        running = false;
                        System.out.println("Exiting Library System. Goodbye!");
                    }
                }
                case -1 -> showHelp();
                default -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Book Menu ===
    private static void bookMenu(BookService bookService) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- Books ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Books (V)");
            System.out.println("2. Add Book (A)");
            System.out.println("3. Update Book (U)");
            System.out.println("4. Delete Book (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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
                    System.out.print("Title: "); String title = scanner.nextLine();
                    System.out.print("Author: "); String author = scanner.nextLine();
                    System.out.print("ISBN: "); String isbn = scanner.nextLine();

                    // Genre selection
                    System.out.println("Genres:");
                    System.out.println("1. Fiction");
                    System.out.println("2. Non-Fiction");
                    System.out.println("3. Mystery");
                    System.out.println("4. Romance");
                    System.out.println("5. Sci-Fi");
                    System.out.println("6. Biography");
                    System.out.println("7. Other");
                    System.out.print("Choose genre (1-7): ");
                    int genreChoice = readInt();
                    String genre;
                    if (genreChoice == 7) {
                        System.out.print("Enter genre: ");
                        genre = scanner.nextLine();
                    } else {
                        genre = switch (genreChoice) {
                            case 1 -> "Fiction";
                            case 2 -> "Non-Fiction";
                            case 3 -> "Mystery";
                            case 4 -> "Romance";
                            case 5 -> "Sci-Fi";
                            case 6 -> "Biography";
                            default -> "Other";
                        };
                    }

                    System.out.print("Publisher: "); String publisher = scanner.nextLine();
                    System.out.print("Publication Year: "); int publicationYear = readInt();

                    // Language selection
                    System.out.println("Languages:");
                    System.out.println("1. English");
                    System.out.println("2. Spanish");
                    System.out.println("3. French");
                    System.out.println("4. German");
                    System.out.println("5. Other");
                    System.out.print("Choose language (1-5): ");
                    int langChoice = readInt();
                    String language;
                    if (langChoice == 5) {
                        System.out.print("Enter language: ");
                        language = scanner.nextLine();
                    } else {
                        language = switch (langChoice) {
                            case 1 -> "English";
                            case 2 -> "Spanish";
                            case 3 -> "French";
                            case 4 -> "German";
                            default -> "Other";
                        };
                    }

                    // Condition selection
                    System.out.println("Conditions:");
                    System.out.println("1. NEW");
                    System.out.println("2. GOOD");
                    System.out.println("3. FAIR");
                    System.out.println("4. POOR");
                    System.out.print("Choose condition (1-4): ");
                    int condChoice = readInt();
                    String condition = switch (condChoice) {
                        case 1 -> "NEW";
                        case 2 -> "GOOD";
                        case 3 -> "FAIR";
                        case 4 -> "POOR";
                        default -> "GOOD";
                    };

                    // Size selection
                    System.out.println("Sizes:");
                    System.out.println("1. STANDARD");
                    System.out.println("2. OVERSIZED");
                    System.out.print("Choose size (1-2): ");
                    int sizeChoice = readInt();
                    String size = switch (sizeChoice) {
                        case 1 -> "STANDARD";
                        case 2 -> "OVERSIZED";
                        default -> "STANDARD";
                    };

                    System.out.print("Location: "); String location = scanner.nextLine();

                    // Keywords input
                    System.out.print("Keywords (comma-separated, optional): ");
                    String keywordsInput = scanner.nextLine();
                    List<String> keywords = new ArrayList<>();
                    if (keywordsInput != null && !keywordsInput.trim().isEmpty()) {
                        String[] keywordArray = keywordsInput.split(",");
                        for (String keyword : keywordArray) {
                            String trimmed = keyword.trim();
                            if (!trimmed.isEmpty()) {
                                keywords.add(trimmed);
                            }
                        }
                    }

                    Book book = bookService.addBook(title, author, isbn, genre, publisher, publicationYear, language, condition, location, size, keywords);
                    System.out.println("Book '" + book.getTitle() + "' by " + book.getAuthor() + " added successfully (ID: " + book.getId() + ")");
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
                    System.out.println("Enter new details (press Enter to keep current):");

                    System.out.print("Title [" + bookToUpdate.getTitle() + "]: ");
                    String title = scanner.nextLine();
                    if (title.trim().isEmpty()) title = bookToUpdate.getTitle();

                    System.out.print("Author [" + bookToUpdate.getAuthor() + "]: ");
                    String author = scanner.nextLine();
                    if (author.trim().isEmpty()) author = bookToUpdate.getAuthor();

                    System.out.print("ISBN [" + bookToUpdate.getIsbn() + "]: ");
                    String isbn = scanner.nextLine();
                    if (isbn.trim().isEmpty()) isbn = bookToUpdate.getIsbn();

                    // Genre selection
                    System.out.println("Current genre: " + bookToUpdate.getGenre());
                    System.out.println("Genres:");
                    System.out.println("1. Fiction");
                    System.out.println("2. Non-Fiction");
                    System.out.println("3. Mystery");
                    System.out.println("4. Romance");
                    System.out.println("5. Sci-Fi");
                    System.out.println("6. Biography");
                    System.out.println("7. Other");
                    System.out.print("Choose new genre (1-7, or 0 to keep current): ");
                    int genreChoice = readInt();
                    String genre = bookToUpdate.getGenre();
                    if (genreChoice != 0) {
                        if (genreChoice == 7) {
                            System.out.print("Enter genre: ");
                            genre = scanner.nextLine();
                        } else {
                            genre = switch (genreChoice) {
                                case 1 -> "Fiction";
                                case 2 -> "Non-Fiction";
                                case 3 -> "Mystery";
                                case 4 -> "Romance";
                                case 5 -> "Sci-Fi";
                                case 6 -> "Biography";
                                default -> "Other";
                            };
                        }
                    }

                    System.out.print("Publisher [" + bookToUpdate.getPublisher() + "]: ");
                    String publisher = scanner.nextLine();
                    if (publisher.trim().isEmpty()) publisher = bookToUpdate.getPublisher();

                    System.out.print("Publication Year [" + bookToUpdate.getPublicationYear() + "]: ");
                    String yearStr = scanner.nextLine();
                    int publicationYear = yearStr.trim().isEmpty() ? bookToUpdate.getPublicationYear() : Integer.parseInt(yearStr);

                    // Language selection
                    System.out.println("Current language: " + bookToUpdate.getLanguage());
                    System.out.println("Languages:");
                    System.out.println("1. English");
                    System.out.println("2. Spanish");
                    System.out.println("3. French");
                    System.out.println("4. German");
                    System.out.println("5. Other");
                    System.out.print("Choose new language (1-5, or 0 to keep current): ");
                    int langChoice = readInt();
                    String language = bookToUpdate.getLanguage();
                    if (langChoice != 0) {
                        if (langChoice == 5) {
                            System.out.print("Enter language: ");
                            language = scanner.nextLine();
                        } else {
                            language = switch (langChoice) {
                                case 1 -> "English";
                                case 2 -> "Spanish";
                                case 3 -> "French";
                                case 4 -> "German";
                                default -> "Other";
                            };
                        }
                    }

                    // Condition selection
                    System.out.println("Current condition: " + bookToUpdate.getCondition());
                    System.out.println("Conditions:");
                    System.out.println("1. NEW");
                    System.out.println("2. GOOD");
                    System.out.println("3. FAIR");
                    System.out.println("4. POOR");
                    System.out.print("Choose new condition (1-4, or 0 to keep current): ");
                    int condChoice = readInt();
                    String condition = bookToUpdate.getCondition().toString();
                    if (condChoice != 0) {
                        condition = switch (condChoice) {
                            case 1 -> "NEW";
                            case 2 -> "GOOD";
                            case 3 -> "FAIR";
                            case 4 -> "POOR";
                            default -> "GOOD";
                        };
                    }

                    // Size selection
                    System.out.println("Current size: " + bookToUpdate.getSize());
                    System.out.println("Sizes:");
                    System.out.println("1. STANDARD");
                    System.out.println("2. OVERSIZED");
                    System.out.print("Choose new size (1-2, or 0 to keep current): ");
                    int sizeChoice = readInt();
                    String size = bookToUpdate.getSize().toString();
                    if (sizeChoice != 0) {
                        size = switch (sizeChoice) {
                            case 1 -> "STANDARD";
                            case 2 -> "OVERSIZED";
                            default -> "STANDARD";
                        };
                    }

                    System.out.print("Location [" + bookToUpdate.getLocation() + "]: ");
                    String location = scanner.nextLine();
                    if (location.trim().isEmpty()) location = bookToUpdate.getLocation();

                    // Keywords input
                    System.out.print("Keywords [" + String.join(", ", bookToUpdate.getKeywords()) + "] (comma-separated): ");
                    String keywordsInput = scanner.nextLine();
                    List<String> keywords = new ArrayList<>();
                    if (keywordsInput.trim().isEmpty()) {
                        keywords = bookToUpdate.getKeywords();
                    } else {
                        String[] keywordArray = keywordsInput.split(",");
                        for (String keyword : keywordArray) {
                            String trimmed = keyword.trim();
                            if (!trimmed.isEmpty()) {
                                keywords.add(trimmed);
                            }
                        }
                    }

                    Book updatedBook = new Book(bookToUpdate.getId(), Book.Condition.valueOf(condition), location, Book.Size.valueOf(size), title, author, isbn, publisher, publicationYear, genre, language, keywords, bookToUpdate.isAvailable(), bookToUpdate.getTimesBorrowed());
                    boolean success = bookService.updateBook(updatedBook);
                    if (success) {
                        System.out.println("Book updated successfully!");
                    } else {
                        System.out.println("Failed to update book.");
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
                case 9 -> {
                    breadcrumbs.clear();
                    inMenu = false;
                }
                case -1 -> showHelp();
                default -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Member Menu ===
    private static void memberMenu(MemberService memberService) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- Members ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Members (V)");
            System.out.println("2. View Members by Type (T)");
            System.out.println("3. Register Member (A)");
            System.out.println("4. Update Member (U)");
            System.out.println("5. Delete Member (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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
                    System.out.print("First Name: "); String firstName = scanner.nextLine();
                    System.out.print("Last Name: "); String lastName = scanner.nextLine();

                    // Gender selection
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    System.out.print("Choose gender (1-3): ");
                    int genderChoice = readInt();
                    String gender = switch (genderChoice) {
                        case 1 -> "Male";
                        case 2 -> "Female";
                        case 3 -> "Other";
                        default -> "Other";
                    };

                    LocalDate dateOfBirth;
                    System.out.print("Date of Birth (YYYY-MM-DD): ");
                    try {
                        dateOfBirth = LocalDate.parse(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("Invalid date format. Use YYYY-MM-DD.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.print("Email: "); String email = scanner.nextLine();
                    System.out.print("Phone: "); String phone = scanner.nextLine();
                    System.out.print("Address: "); String address = scanner.nextLine();

                    // Membership type selection
                    System.out.println("Membership Types:");
                    System.out.println("1. STUDENT");
                    System.out.println("2. TEACHER");
                    System.out.println("3. PUBLIC");
                    System.out.println("4. STAFF");
                    System.out.print("Choose membership type (1-4): ");
                    int typeChoice = readInt();
                    String memberType = switch (typeChoice) {
                        case 1 -> "STUDENT";
                        case 2 -> "TEACHER";
                        case 3 -> "PUBLIC";
                        case 4 -> "STAFF";
                        default -> "PUBLIC";
                    };

                    Member member = memberService.registerMember(firstName, lastName, gender, dateOfBirth, email, phone, address, memberType);
                    System.out.println("Member '" + member.getName() + "' registered successfully (ID: " + member.getMemberId() + ")");
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
                    if (choice < 1 || choice > members.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Member memberToUpdate = members.get(choice - 1);

                    System.out.println("Current details:");
                    System.out.println(memberToUpdate);
                    System.out.println("Enter new details (press Enter to keep current):");

                    System.out.print("First Name [" + memberToUpdate.getFirstName() + "]: ");
                    String firstName = scanner.nextLine();
                    if (firstName.trim().isEmpty()) firstName = memberToUpdate.getFirstName();

                    System.out.print("Last Name [" + memberToUpdate.getLastName() + "]: ");
                    String lastName = scanner.nextLine();
                    if (lastName.trim().isEmpty()) lastName = memberToUpdate.getLastName();

                    // Gender selection
                    System.out.println("Current gender: " + memberToUpdate.getGender());
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    System.out.print("Choose new gender (1-3, or 0 to keep current): ");
                    int genderChoice = readInt();
                    String gender = memberToUpdate.getGender();
                    if (genderChoice != 0) {
                        gender = switch (genderChoice) {
                            case 1 -> "Male";
                            case 2 -> "Female";
                            case 3 -> "Other";
                            default -> "Other";
                        };
                    }

                    System.out.print("Date of Birth [" + memberToUpdate.getDateOfBirth() + "] (YYYY-MM-DD): ");
                    String dobStr = scanner.nextLine();
                    LocalDate dateOfBirth = dobStr.trim().isEmpty() ? memberToUpdate.getDateOfBirth() : LocalDate.parse(dobStr);

                    System.out.print("Email [" + memberToUpdate.getEmail() + "]: ");
                    String email = scanner.nextLine();
                    if (email.trim().isEmpty()) email = memberToUpdate.getEmail();

                    System.out.print("Phone [" + memberToUpdate.getPhoneNumber() + "]: ");
                    String phone = scanner.nextLine();
                    if (phone.trim().isEmpty()) phone = memberToUpdate.getPhoneNumber();

                    System.out.print("Address [" + memberToUpdate.getAddress() + "]: ");
                    String address = scanner.nextLine();
                    if (address.trim().isEmpty()) address = memberToUpdate.getAddress();

                    // Membership type selection
                    System.out.println("Current type: " + memberToUpdate.getMemberType());
                    System.out.println("Membership Types:");
                    System.out.println("1. STUDENT");
                    System.out.println("2. TEACHER");
                    System.out.println("3. PUBLIC");
                    System.out.println("4. STAFF");
                    System.out.print("Choose new membership type (1-4, or 0 to keep current): ");
                    int typeChoice = readInt();
                    String memberType = memberToUpdate.getMemberType().toString();
                    if (typeChoice != 0) {
                        memberType = switch (typeChoice) {
                            case 1 -> "STUDENT";
                            case 2 -> "TEACHER";
                            case 3 -> "PUBLIC";
                            case 4 -> "STAFF";
                            default -> "PUBLIC";
                        };
                    }

                    Member updatedMember = new Member(memberToUpdate.getMemberId(), memberToUpdate.getCardNumber(), Member.MemberType.valueOf(memberType), firstName, lastName, gender, dateOfBirth, email, phone, address, memberToUpdate.getDateJoined(), memberToUpdate.isActive(), memberToUpdate.getOutstandingFines(), memberToUpdate.getMembershipStatus(), memberToUpdate.getBorrowedBookIds(), memberToUpdate.getTotalBooksBorrowed());
                    boolean success = memberService.updateMember(updatedMember);
                    if (success) {
                        System.out.println("Member updated successfully!");
                    } else {
                        System.out.println("Failed to update member.");
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
                case 0 -> {
                    inMenu = false;
                }
                case 9 -> {
                    if (confirmExit()) {
                        clearBreadcrumbs();
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
        while (inMenu) {
            System.out.println("\n--- Librarians ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Librarians (V)");
            System.out.println("2. Add Librarian (A)");
            System.out.println("3. Update Librarian (U)");
            System.out.println("4. Delete Librarian (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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
                    System.out.print("First Name: "); String firstName = scanner.nextLine();
                    System.out.print("Last Name: "); String lastName = scanner.nextLine();

                    // Gender selection
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    System.out.print("Choose gender (1-3): ");
                    int genderChoice = readInt();
                    String gender = switch (genderChoice) {
                        case 1 -> "Male";
                        case 2 -> "Female";
                        case 3 -> "Other";
                        default -> "Other";
                    };

                    LocalDate dateOfBirth;
                    System.out.print("Date of Birth (YYYY-MM-DD): ");
                    try {
                        dateOfBirth = LocalDate.parse(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("Invalid date format. Use YYYY-MM-DD.");
                        popBreadcrumb();
                        break;
                    }
                    System.out.print("Email: "); String email = scanner.nextLine();
                    System.out.print("Phone: "); String phone = scanner.nextLine();
                    System.out.print("Address: "); String address = scanner.nextLine();

                    // Role selection
                    System.out.println("Roles:");
                    System.out.println("1. ADMIN");
                    System.out.println("2. STAFF");
                    System.out.println("3. INTERN");
                    System.out.print("Choose role (1-3): ");
                    int roleChoice = readInt();
                    LibrarianRole role = switch (roleChoice) {
                        case 1 -> LibrarianRole.ADMIN;
                        case 2 -> LibrarianRole.STAFF;
                        case 3 -> LibrarianRole.INTERN;
                        default -> LibrarianRole.STAFF;
                    };

                    System.out.print("Position: "); String position = scanner.nextLine();
                    System.out.print("Salary: "); double salary = readDouble();
                    System.out.print("Username: "); String username = scanner.nextLine();
                    System.out.print("Password: "); String password = scanner.nextLine();
                    Librarian librarian = librarianService.addLibrarian(firstName, lastName, gender, dateOfBirth, email, phone, address, position, salary, role, username, password);
                    System.out.println("Librarian '" + librarian.getName() + "' added successfully (ID: " + librarian.getLibrarianId() + ")");
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
                    if (choice < 1 || choice > librarians.size()) {
                        System.out.println("Invalid choice!");
                        popBreadcrumb();
                        break;
                    }
                    Librarian librarianToUpdate = librarians.get(choice - 1);

                    System.out.println("Current details:");
                    System.out.println(librarianToUpdate);
                    System.out.println("Enter new details (press Enter to keep current):");

                    System.out.print("First Name [" + librarianToUpdate.getFirstName() + "]: ");
                    String firstName = scanner.nextLine();
                    if (firstName.trim().isEmpty()) firstName = librarianToUpdate.getFirstName();

                    System.out.print("Last Name [" + librarianToUpdate.getLastName() + "]: ");
                    String lastName = scanner.nextLine();
                    if (lastName.trim().isEmpty()) lastName = librarianToUpdate.getLastName();

                    // Gender selection
                    System.out.println("Current gender: " + librarianToUpdate.getGender());
                    System.out.println("Genders:");
                    System.out.println("1. Male");
                    System.out.println("2. Female");
                    System.out.println("3. Other");
                    System.out.print("Choose new gender (1-3, or 0 to keep current): ");
                    int genderChoice = readInt();
                    String gender = librarianToUpdate.getGender();
                    if (genderChoice != 0) {
                        gender = switch (genderChoice) {
                            case 1 -> "Male";
                            case 2 -> "Female";
                            case 3 -> "Other";
                            default -> "Other";
                        };
                    }

                    System.out.print("Date of Birth [" + librarianToUpdate.getDateOfBirth() + "] (YYYY-MM-DD): ");
                    String dobStr = scanner.nextLine();
                    LocalDate dateOfBirth = dobStr.trim().isEmpty() ? librarianToUpdate.getDateOfBirth() : LocalDate.parse(dobStr);

                    System.out.print("Email [" + librarianToUpdate.getEmail() + "]: ");
                    String email = scanner.nextLine();
                    if (email.trim().isEmpty()) email = librarianToUpdate.getEmail();

                    System.out.print("Phone [" + librarianToUpdate.getPhoneNumber() + "]: ");
                    String phone = scanner.nextLine();
                    if (phone.trim().isEmpty()) phone = librarianToUpdate.getPhoneNumber();

                    System.out.print("Address [" + librarianToUpdate.getAddress() + "]: ");
                    String address = scanner.nextLine();
                    if (address.trim().isEmpty()) address = librarianToUpdate.getAddress();

                    // Role selection
                    System.out.println("Current role: " + librarianToUpdate.getRole());
                    System.out.println("Roles:");
                    System.out.println("1. ADMIN");
                    System.out.println("2. STAFF");
                    System.out.println("3. INTERN");
                    System.out.print("Choose new role (1-3, or 0 to keep current): ");
                    int roleChoice = readInt();
                    LibrarianRole role = librarianToUpdate.getRole();
                    if (roleChoice != 0) {
                        role = switch (roleChoice) {
                            case 1 -> LibrarianRole.ADMIN;
                            case 2 -> LibrarianRole.STAFF;
                            case 3 -> LibrarianRole.INTERN;
                            default -> LibrarianRole.STAFF;
                        };
                    }

                    System.out.print("Position [" + librarianToUpdate.getPosition() + "]: ");
                    String position = scanner.nextLine();
                    if (position.trim().isEmpty()) position = librarianToUpdate.getPosition();

                    System.out.print("Salary [" + librarianToUpdate.getSalary() + "]: ");
                    String salaryStr = scanner.nextLine();
                    double salary = salaryStr.trim().isEmpty() ? librarianToUpdate.getSalary() : Double.parseDouble(salaryStr);

                    System.out.print("Username [" + librarianToUpdate.getUsername() + "]: ");
                    String username = scanner.nextLine();
                    if (username.trim().isEmpty()) username = librarianToUpdate.getUsername();

                    Librarian updatedLibrarian = new Librarian(librarianToUpdate.getLibrarianId(), librarianToUpdate.getStaffCode(), firstName, lastName, gender, dateOfBirth, email, phone, address, librarianToUpdate.getDateHired(), position, salary, librarianToUpdate.isActive(), role, username, librarianToUpdate.getPasswordHash(), librarianToUpdate.getLastLoginDate());
                    boolean success = librarianService.updateLibrarian(updatedLibrarian);
                    if (success) {
                        System.out.println("Librarian updated successfully!");
                    } else {
                        System.out.println("Failed to update librarian.");
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
                case 0 -> {
                    inMenu = false;
                }
                case 9 -> {
                    if (confirmExit()) {
                        clearBreadcrumbs();
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
        while (inMenu) {
            System.out.println("\n--- Reservations ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Reservations (V)");
            System.out.println("2. Make Reservation (A)");
            System.out.println("3. Fulfill Reservation (F)");
            System.out.println("4. Cancel Reservation (C)");
            System.out.println("5. Delete Reservation (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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
                case 0 -> {
                    inMenu = false;
                }
                case 9 -> {
                    if (confirmExit()) {
                        clearBreadcrumbs();
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
        while (inMenu) {
            System.out.println("\n--- Loans ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Loans (V)");
            System.out.println("2. Issue Loan (I)");
            System.out.println("3. Return Loan (R)");
            System.out.println("4. Delete Loan (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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

                    loanService.issueLoan(book.getId(), memberId, LocalDate.now());
                    book.setAvailable(false);
                    bookService.updateBook(book);
                    System.out.println("Loan issued!");
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
                    loanService.returnLoan(loanId, returnDate);

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
                                loanService.issueLoan(returnedBook.getId(), nextReservation.getMemberId(), LocalDate.now());
                                reservationService.fulfillReservation(nextReservation.getReservationId());
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
                case 0 -> {
                    inMenu = false;
                }
                case 9 -> {
                    if (confirmExit()) {
                        clearBreadcrumbs();
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
        while (inMenu) {
            System.out.println("\n--- Fines & Payments ---");
            System.out.println("Current location: " + getBreadcrumbPath());
            System.out.println("1. View All Fines (V)");
            System.out.println("2. Calculate Fine (C)");
            System.out.println("3. Pay Fine (P)");
            System.out.println("4. Delete Fine (D)");
            System.out.println("0. Back (B)");
            System.out.println("9. Main Menu (M)");
            System.out.println("?. Help");
            System.out.print("Select an option (number or letter): ");

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
                    case "b", "back" -> 0;
                    case "m", "main" -> 9;
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
                    System.out.print("Amount: "); double amount = readDouble();
                    System.out.print("Payment Method: "); String method = scanner.nextLine();
                    LocalDate paymentDate = LocalDate.now();
                    paymentService.payFine(fineId, amount, paymentDate, method);
                    System.out.println("Payment recorded!");
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
                case 0 -> {
                    inMenu = false;
                }
                case 9 -> {
                    if (confirmExit()) {
                        clearBreadcrumbs();
                        inMenu = false;
                    }
                }
                case -1 -> showHelp();
                case -2 -> System.out.println("Invalid option. Type '?' for help.");
            }
        }
    }

    // === Utility Methods ===
    private static boolean confirmExit() {
        System.out.print("Are you sure you want to exit to main menu? (y/n): ");
        String confirm = readInput();
        return confirm.equals("y") || confirm.equals("yes");
    }

    private static void clearBreadcrumbs() {
        breadcrumbs.clear();
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
