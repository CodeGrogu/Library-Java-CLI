# Library Java CLI

A command-line library management system built with Java.

## Features

- Book management (add, view, search)
- Member management (register, view by type)
- Librarian management
- Loan system (issue, return, overdue tracking)
- Reservation system
- Fine and payment management

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building and Running

### Using Maven (Recommended)

```bash
# Compile the project
mvn compile

# Run the application
mvn exec:java

# Create executable JAR
mvn package

# Run the JAR
java -jar target/library-cli-1.0.0.jar

# Run tests
mvn test
```

### Manual Compilation (Alternative)

```bash
# Compile all Java files
javac -cp . src/main/java/com/codegrogu/library/*.java src/main/java/com/codegrogu/library/*/*.java

# Run the application
java -cp .:src/main/java com.codegrogu.library.Main
```

## Project Structure

```
src/
├── main/
│   ├── java/com/codegrogu/library/
│   │   ├── Main.java                 # CLI entry point and menu system
│   │   ├── model/                    # Domain models (Book, Member, Loan, etc.)
│   │   ├── repository/               # Data access layer (in-memory storage)
│   │   ├── service/                  # Business logic layer
│   │   └── util/                     # Utility classes (ConfigUtil)
│   └── resources/
│       └── application.properties    # Configuration settings
└── test/                             # Unit tests (JUnit 5 framework configured)
```

## Architecture

The application follows a **layered architecture** with clear separation of concerns:

### Model Layer (`model/`)
Domain objects representing business entities:
- **Book**: Title, author, ISBN, genre, condition, availability, keywords
- **Member**: Personal details, membership type, borrowing history
- **Loan**: Book lending transactions with due dates and return tracking
- **Fine**: Overdue penalties with payment tracking
- **Reservation**: Book reservation system
- **Payment**: Fine payment records
- **Librarian**: Library staff management
- **Audit**: System activity logging

### Repository Layer (`repository/`)
In-memory data persistence using `ArrayList<T>` collections:
- CRUD operations for all entities
- ID generation using `max(existing) + 1` pattern
- Defensive copying to prevent external modification
- Optional return types for safe null handling

### Service Layer (`service/`)
Business logic and validation:
- Constructor dependency injection
- Input validation and business rules
- Cross-entity relationship management
- Fine calculation and payment processing

### CLI Layer (`Main.java`)
Command-line interface with menu-driven navigation:
- Shared `Scanner` instance for input handling
- Safe parsing with error recovery (`readInt()`, `readDouble()`)
- Numbered list selection patterns
- Service integration for all operations

### Utility Layer (`util/`)
Supporting utilities:
- **ConfigUtil**: Properties file loading with type-safe access
- Configuration-driven business rules (loan periods, fine rates)

## Key Design Patterns

### ID Generation
All services generate unique IDs using: `max(existing IDs) + 1`

### Input Validation
Safe parsing helpers prevent crashes from invalid user input:
```java
private static int readInt() {
    while (true) {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            System.out.print("Enter valid number: ");
        }
    }
}
```

### Menu Selection
Consistent pattern for user choices:
```java
for (int i = 0; i < items.size(); i++) {
    System.out.println((i + 1) + ". " + items.get(i).getName());
}
System.out.print("Choose (1-" + items.size() + "): ");
int choice = readInt();
```

### Configuration Management
Business rules loaded from `application.properties`:
- `library.maxLoanDays`: Default loan period (14 days)
- `library.finePerDay`: Daily fine rate (2.5 units)

## Development

### Code Conventions

#### Model Classes
- Full getter/setter pairs for all fields
- Enums for status fields (`Book.Condition`, `Loan.LoanStatus`, `Member.MemberType`)
- Optional fields may be null (check before use)
- Comprehensive `toString()` methods for debugging

#### Repository Pattern
- `private final List<T> items = new ArrayList<>()`
- `Optional<T>` for single queries, `List<T>` copies for collections
- Manual field copying in `updateXxx()` methods (no reflection)

#### Service Layer
- Constructor injection: `public XxxService(XxxRepository repository)`
- Business logic methods with validation
- Maintain all model relationships (e.g., member borrowing history)

#### CLI Patterns
- Single shared `Scanner scanner` instance
- `scanner.nextLine()` after `nextInt()` to consume newline
- Numbered menu selections with bounds checking

### Adding New Features

1. **Create Model**: Add domain class to `model/` with enums and full getters/setters
2. **Create Repository**: Implement CRUD operations in `repository/` package
3. **Create Service**: Add business logic in `service/` with dependency injection
4. **Update CLI**: Add menu options and input handling in `Main.java`
5. **Update Configuration**: Add new settings to `application.properties` if needed

### Configuration

Application settings are stored in `src/main/resources/application.properties`:

```properties
# Library configuration
library.maxLoanDays=14
library.finePerDay=2.5

# Console application settings
console.prompt=Select an option:
console.invalidOption=Invalid option. Please try again.
```

Settings are loaded using `ConfigUtil` with fallback defaults for robustness.

### Running Tests

```bash
mvn test
```

*Note: Test directory includes JUnit 5 framework. Add unit tests for business logic as needed.*

## Implementation Status

### Completed Features
- ✅ Complete book management (CRUD, search, keywords)
- ✅ Member management with borrowing history tracking
- ✅ Loan system with due date calculation and return processing
- ✅ Fine calculation and payment system
- ✅ Reservation system for book holds
- ✅ Librarian management
- ✅ Configuration-driven business rules
- ✅ Comprehensive CLI with input validation
- ✅ Layered architecture with proper separation of concerns

### Data Persistence
- **Current**: In-memory storage (data lost on restart)
- **Planned**: File-based persistence (mentioned in `Persistence.java` stub)

### Testing
- **Current**: JUnit 5 framework configured, basic structure in place
- **Planned**: Unit tests for all business logic and edge cases

### Future Enhancements
- Database integration (PostgreSQL/MySQL)
- REST API for web interface
- Advanced search and filtering
- Email notifications for due dates
- Multi-user authentication
- Audit logging for all operations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Run `mvn test` to ensure tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.
