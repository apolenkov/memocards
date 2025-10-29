# Contributing to Memocards

Thank you for your interest in contributing to Memocards! This document provides guidelines for contributing to this
source-available educational project.

**Note on License**: This project uses a proprietary Source Available License. You can view the code and contribute
improvements via Pull Requests, but forking for independent use or redistribution is not permitted.
See [LICENSE](../LICENSE) for complete details.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## Code of Conduct

This project follows a simple code of conduct:

- **Be respectful** and considerate in your communication
- **Be collaborative** and open to feedback
- **Be patient** with newcomers
- **Be constructive** in your criticism

## Getting Started

1. **Clone the repository** locally:
   ```bash
   git clone https://github.com/apolenkov/memocards.git
   cd memocards
   ```
2. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Prerequisites

- Java 21+ (Eclipse Temurin or Amazon Corretto recommended)
- Docker + Docker Compose
- Git
- (Optional) Node.js 20+ for frontend development

### Initial Setup

```bash
# 1. Copy environment template
cp env.sample .env

# 2. Edit .env with your settings
# At minimum, set:
# DB_PASSWORD=your_password

# 3. Start PostgreSQL
docker-compose up -d postgres

# 4. Run application
./gradlew bootRun
```

### Verify Setup

```bash
# Run tests
./gradlew test

# Run code quality checks
./gradlew codeQuality

# Run all checks
./gradlew check
```

## How to Contribute

### Reporting Bugs

When reporting bugs, please include:

- **Summary**: Clear, descriptive title
- **Steps to reproduce**: Detailed steps to trigger the issue
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Environment**: OS, Java version, Docker version
- **Screenshots**: If applicable

### Suggesting Features

When suggesting features:

- Check existing issues to avoid duplicates
- Explain the problem you're trying to solve
- Describe your proposed solution
- Consider alternative solutions
- Discuss potential impact on existing functionality

### Code Contributions

We welcome contributions in these areas:

- **Bug fixes**: Resolve reported issues
- **Features**: Implement new functionality from roadmap
- **Tests**: Improve test coverage
- **Documentation**: Enhance README, Javadoc, or guides
- **Refactoring**: Improve code quality without changing behavior
- **Translations**: Add or improve i18n messages

## Coding Standards

### Java Code Style

This project follows strict coding standards enforced by:

- **Spotless**: Auto-formatting (Palantir Java Format)
- **Checkstyle**: Style rules (config/checkstyle/checkstyle.xml)
- **SpotBugs**: Bug detection
- **SonarLint**: Code quality

### Running Code Quality Checks

```bash
# Auto-format code
./gradlew spotlessApply

# Check style
./gradlew checkstyleMain checkstyleTest

# Run SpotBugs
./gradlew spotbugsMain spotbugsTest

# Run all checks
./gradlew codeQuality
```

### Project-Specific Rules

1. **Clean Architecture**: Respect layer boundaries
    - `domain/`: Pure business logic, no external dependencies
    - `service/`: Use cases implementation
    - `infrastructure/`: Technical implementations (JDBC, etc.)
    - `views/`: UI layer (Vaadin components)

2. **Dependency Injection**: Constructor injection only
   ```java
   // ✅ GOOD
   public class MyService {
       private final MyRepository repository;
       
       public MyService(MyRepository repository) {
           this.repository = repository;
       }
   }
   
   // ❌ BAD
   public class MyService {
       @Autowired
       private MyRepository repository;
   }
   ```

3. **Immutability**: Prefer Records for DTOs
   ```java
   // ✅ GOOD
   public record UserDto(Long id, String email, String name) {}
   
   // ❌ BAD (for DTOs)
   public class UserDto {
       private Long id;
       // getters, setters...
   }
   ```

4. **SQL Queries**: Use text blocks in `*SqlQueries` classes
   ```java
   // ✅ GOOD
   static final String FIND_BY_ID = """
       SELECT id, email, name
       FROM users
       WHERE id = ?
       """;
   ```

5. **Javadoc**: Required for all public APIs
   ```java
   /**
    * Returns user by unique identifier.
    *
    * @param id user ID to search for
    * @return optional containing user if found
    * @throws IllegalArgumentException if id is null or negative
    */
   Optional<User> findById(Long id);
   ```

6. **i18n**: NO hardcoded strings in UI
   ```java
   // ✅ GOOD
   new H1(getTranslation("welcome.title"));
   
   // ❌ BAD
   new H1("Welcome to Memocards");
   ```

7. **Transactions**: `@Transactional` on service layer only
   ```java
   // ✅ GOOD (Service)
   @Transactional
   public void saveCard(Card card) { ... }
   
   // ❌ BAD (Repository)
   @Transactional
   public Card save(Card card) { ... }
   ```

## Commit Messages

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```
feat(deck): add card filtering by status

Implement filtering cards by known/unknown status in deck view.
Adds FilterOption enum and updates CardRepository with dynamic query builder.

Closes #42

---

fix(auth): prevent password reset token reuse

Reset tokens can now only be used once and expire after 24 hours.

Fixes #58

---

docs(readme): add Docker Compose setup instructions

Add section explaining how to run full stack with Docker Compose.
```

## Pull Request Process

1. **Update your branch** with latest changes:
   ```bash
   git fetch origin
   git rebase origin/master
   ```

2. **Ensure all checks pass**:
   ```bash
   ./gradlew clean check
   ```

3. **Update documentation** if needed:
    - README.md for user-facing changes
    - Javadoc for API changes
    - CHANGELOG.md with your changes

4. **Create Pull Request**:
    - Use descriptive title
    - Reference related issues
    - Describe what changed and why
    - Include screenshots for UI changes
    - List breaking changes if any

5. **Respond to feedback**:
    - Address review comments promptly
    - Push updates to same branch
    - Be open to suggestions

### PR Checklist

Before submitting your PR, verify:

- [ ] Code follows project style (Spotless applied)
- [ ] All tests pass (`./gradlew test`)
- [ ] New tests added for new features
- [ ] Code quality checks pass (`./gradlew codeQuality`)
- [ ] Javadoc updated for public APIs
- [ ] i18n messages added for UI strings
- [ ] README updated if needed
- [ ] No merge conflicts with master branch

## Testing Guidelines

### Test Structure

- **Unit Tests**: Test single class in isolation (Mockito)
- **Integration Tests**: Test with real database (TestContainers)
- **UI Tests**: Test Vaadin components (TestBench)

### Naming Conventions

```java
// Unit test
class CardUseCaseServiceTest {
    @Test
    @DisplayName("Should save card with valid data")
    void shouldSaveCardWithValidData() { ...}
}

// Integration test
class CardJdbcAdapterIntegrationTest extends BaseIntegrationTest {
    @Test
    @DisplayName("Should persist card to database")
    void shouldPersistCardToDatabase() { ...}
}
```

### Test Coverage

Aim for:

- **Service layer**: 80%+ coverage
- **Repository layer**: Integration tests for all operations
- **Critical paths**: 100% coverage (authentication, payments, etc.)

## Documentation

### Javadoc Requirements

Required for:

- All public classes
- All public methods
- All public fields (constants)

Not required for:

- Private methods
- Test classes
- Getters/setters (unless behavior is non-obvious)

### Documentation Style

```java
/**
 * Validates and saves a card to the repository.
 *
 * <p>This method performs validation using Jakarta Validation
 * and invalidates relevant caches after successful save.</p>
 *
 * @param card card to save (must have valid deckId and texts)
 * @throws IllegalArgumentException if card validation fails
 * @throws DataAccessException if database operation fails
 */
@Transactional
public void saveCard(Card card) { ...}
```

## Questions?

- 💬 **Discussions**: Use GitHub Discussions for questions
- 🐛 **Issues**: Create an issue for bugs or feature requests
- 📧 **Email**: Contact maintainers via issue comments

---

Thank you for contributing to Memocards! Your efforts help make this project better for everyone.

