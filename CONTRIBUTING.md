# Contributing to SiteWatch

Thank you for contributing to SiteWatch! This document outlines our development practices and code quality requirements.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help maintain a positive development environment

## Development Workflow

### 1. Setup Development Environment

```bash
# Clone the repository
git clone <repository-url>
cd SemanticPageSearch

# Install Git hooks
./scripts/install-git-hooks.sh

# Start local PostgreSQL
docker-compose up -d postgres
```

### 2. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 3. Make Changes

Write code following our standards (see below).

### 4. Run Quality Checks

```bash
# Run all checks (Checkstyle, PMD, SpotBugs, tests)
mvn verify

# Run only Checkstyle
mvn checkstyle:check

# Run only tests
mvn test
```

### 5. Commit and Push

Pre-commit hooks will automatically run validation. If checks fail, fix issues before committing.

```bash
git add .
git commit -m "feat: add RSS feed parsing support"
git push origin feature/your-feature-name
```

### 6. Create Pull Request

- Provide clear description of changes
- Reference related issues
- Ensure CI passes

## Code Quality Requirements

### Javadoc Requirements (MANDATORY)

Every public class, interface, and enum **MUST** have Javadoc documentation:

```java
/**
 * ArticleCrawlerService
 *
 * Responsible for crawling configured sources and extracting items
 * that match user-defined filter rules.
 *
 * Key responsibilities:
 * - Respects robots.txt
 * - Uses Jsoup for HTML parsing
 * - Handles RSS/Atom feeds via Rome
 *
 * @author Your Name
 */
public class ArticleCrawlerService {
    // ...
}
```

Every public method **MUST** have Javadoc with:
- Purpose summary
- `@param` for each parameter
- `@return` for non-void methods
- `@throws` for checked exceptions

```java
/**
 * Fetch and parse an RSS feed and return discovered items.
 *
 * @param feedUrl the RSS/Atom feed URL (must be valid HTTP/HTTPS)
 * @param filters filter rules to apply during parsing
 * @return list of parsed items matching filters (never null, may be empty)
 * @throws IOException on network or parse errors
 * @throws IllegalArgumentException if feedUrl is invalid
 */
public List<Item> fetchFromRss(String feedUrl, FilterRules filters) throws IOException {
    // ...
}
```

### Complex Private Methods

Private methods with:
- More than 20 lines, OR
- Cyclomatic complexity > 5

**MUST** have an inline comment block explaining intent:

```java
// Use SHA-256 of main text to detect small edits vs new content.
// We ignore inline ad sections by extracting main article element first.
// Returns lowercase hex string for consistent comparison.
private String computeContentHash(String mainText) {
    // ...
}
```

### Getters and Setters

Simple getters/setters in DTOs are **exempt** from Javadoc (configured in checkstyle-suppressions.xml).

### Test Classes

Test classes are **exempt** from strict Javadoc requirements but should have clear test method names:

```java
@Test
void shouldReturnEmptyListWhenNoItemsMatchFilters() {
    // ...
}
```

## Code Style

### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: Always use braces, even for single-line blocks
- **Imports**: No wildcard imports (`import java.util.*` ❌)

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `ArticleCrawlerService`)
- **Methods**: `camelCase` (e.g., `fetchFromRss`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRIES`)
- **Variables**: `camelCase` (e.g., `contentHash`)

### Best Practices

- **Minimize complexity**: Keep methods under 150 lines
- **Cyclomatic complexity**: Target < 15
- **DRY principle**: Extract common logic into reusable methods
- **Null safety**: Use `Optional` or explicit null checks
- **Immutability**: Prefer immutable objects where possible

## Automated Enforcement

### Pre-commit Hooks

The `.githooks/pre-commit` script runs:
1. Checkstyle validation
2. Basic Javadoc presence check

Install hooks:
```bash
./scripts/install-git-hooks.sh
```

### CI Pipeline

GitHub Actions runs on every PR:
1. `mvn clean verify` (includes Checkstyle, PMD, SpotBugs)
2. Unit tests
3. Integration tests (Testcontainers)
4. Build Docker image

**All checks must pass** before merge.

## Exceptions to Documentation Rules

Rare cases may warrant exceptions:
1. Add `@SuppressWarnings("NoJavadoc")` with linked issue
2. Requires explicit approval in code review
3. Must be documented in the issue why Javadoc is not applicable

Example:
```java
@SuppressWarnings("NoJavadoc") // See issue #123 - generated code
public class GeneratedDTO {
    // ...
}
```

## Testing Guidelines

### Test Coverage

- **Minimum coverage**: 80% line coverage
- **Critical paths**: 100% coverage for security, data persistence
- **Integration tests**: Required for all API endpoints

### Test Categories

```java
// Unit test
@Test
void shouldParseRssFeedSuccessfully() {
    // ...
}

// Integration test with Testcontainers
@Testcontainers
@SpringBootTest
class ArticleRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Test
    void shouldPersistAndRetrieveArticle() {
        // ...
    }
}
```

## Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style/formatting (no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build/tooling changes

**Examples**:
```
feat(crawler): add support for Atom feeds
fix(api): validate URL format before saving source
docs(readme): update deployment instructions
```

## Questions?

Open an issue or reach out to the maintainers.

Thank you for contributing! 🎉
